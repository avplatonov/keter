/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.avplatonov.keter.core.discovery

import java.util.UUID
import java.util.concurrent.atomic.{AtomicBoolean, AtomicLong}
import java.util.concurrent.{ConcurrentHashMap, Executors, TimeUnit}

import org.apache.curator.RetryPolicy
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.framework.api.CuratorWatcher
import org.apache.curator.framework.imps.CuratorFrameworkState
import org.apache.zookeeper.{CreateMode, WatchedEvent}
import org.slf4j.LoggerFactory
import ru.avplatonov.keter.core.util.SerializedSettings

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

object ZookeeperDiscoveryService {

    case class Settings(
        connectionString: String,
        retryPolicy: RetryPolicy,
        discoveryRoot: String,
        discoveryTimeout: Duration = 1 minute
    )

}

//todo: enents need to be classified - new node, remove node etc.
//todo: we need to periodically check manually of cluster state
case class ZookeeperDiscoveryService(settings: ZookeeperDiscoveryService.Settings, localNodeSettings: Node.Settings)
    extends DiscoveryService {

    /** */
    val serviceId: UUID = UUID.randomUUID()

    /** */
    private val started = new AtomicBoolean(false)

    /** */
    private val zk = CuratorFrameworkFactory.newClient(settings.connectionString, settings.retryPolicy)

    /** Repeated starting protection. */
    private val wasStarted = new AtomicBoolean(false)

    /** */
    @volatile private var localNode: LocalNode = null

    /**
      * Discovered nodes.
      */
    private val nodes = new ConcurrentHashMap[NodeId, Node]().asScala

    /**
      * Topology changes listener.
      */
    private val listeners = new ConcurrentHashMap[Long, EventListener]().asScala

    /**
      * Thread pool for discovery events listeners.
      */
    private val listenersPool = Executors.newSingleThreadExecutor()

    /**
      * Watchdog pool.
      */
    private val watchdogPool = Executors.newCachedThreadPool()

    /** */
    private val lastDiscoveringTime = new AtomicLong(System.currentTimeMillis())

    private val logger = LoggerFactory.getLogger(s"${getClass.getSimpleName}-[${localNodeSettings.address}:${localNodeSettings.listenedPort}]")

    /**
      * @return true if service was started.
      */
    override def isStarted(): Boolean = started.get()

    /**
      * @return local node if service was started.
      */
    override def getLocalNode(): Option[LocalNode] = {
        if (isStarted()) {
            assert(localNode != null)
            Some(localNode)
        }
        else {
            None
        }
    }

    /**
      * Start service.
      */
    override def start(): Node = synchronized {
        try {
            if (!started.get() && !wasStarted.get()) {
                logger.info("Starting discovery service")

                logger.info("Starting zookeeper")
                zk.start()

                logger.info("Create and register local node")
                localNode = createLocalNode(localNodeSettings)
                nodes.put(localNode.id, localNode)
                logger.info(s"Starting local node [id = ${localNode.id}]")
                localNode.start()
                logger.info(s"Start watching root [${settings.discoveryRoot}]")
                watchRoot()
                logger.info("Initial nodes discovery")
                discoverNodes()
                logger.info("Starts periodically discovering")
                runPeriodicallyDiscover()
                started.set(true)
                wasStarted.set(true)
            }
            else if (wasStarted.get()) {
                logger.warn("Repeated discovery starting")
                throw RepeatedStartException(null)
            }

            localNode
        }
        catch {
            case e: Throwable =>
                logger.error("Error while starting discovery service", e)
                stop()
                throw e
        }
    }

    private def createLocalNode(nodeSettings: Node.Settings): LocalNode = {
        val nodeID = toNodeId {
            zk.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                .forPath(
                    nodesPath,
                    nodeSettings.serialize()
                )
        }

        LocalNode(nodeID, nodeSettings)
    }

    private def watchRoot(): Unit = {
        zk.getChildren.usingWatcher(new CuratorWatcher {
            override def process(event: WatchedEvent): Unit = {
                if (!started.get())
                    return

                logger.info("Discovering event")
                discoverNodes()
                watchRoot()
            }
        }).forPath(settings.discoveryRoot)
    }

    private def discoverNodes(): Unit = {
        val nodeKeys = zk.getChildren.forPath(settings.discoveryRoot).asScala
        logger.debug(s"Nodes in ZK root [${nodeKeys.mkString(",")}]")
        val newNodes = nodeKeys
            .flatMap(child => getNode(child))
            .map(n => n.id -> n).toMap

        assert(newNodes.contains(localNode.id))
        val newTopology = Topology(newNodes)
        val diff = Topology(nodes.toMap) diff newTopology

        logger.info(s"New topology [diff = ${diff}]")

        nodes synchronized {
            nodes.clear()
            nodes ++= newNodes
        }

        listeners.values.foreach(listener => {
            if (isStarted()) {
                listenersPool.submit(new Runnable {
                    override def run(): Unit = {
                        listener.apply(newTopology, diff)
                    }
                })
            }
        })

        lastDiscoveringTime.set(System.currentTimeMillis())
    }

    private def runPeriodicallyDiscover(): Unit = {
        watchdogPool.submit(new Runnable {
            override def run(): Unit = {
                while (isStarted()) {
                    Thread.sleep(settings.discoveryTimeout.toMillis)
                    if ((System.currentTimeMillis() - lastDiscoveringTime.get()) > settings.discoveryTimeout.toMillis) {
                        logger.info("Discovering timeout. Check cluster status.")
                        discoverNodes()
                    }
                }
            }
        })
    }

    private def getNode(childId: String): Option[Node] = {
        val childPath = s"${settings.discoveryRoot}/$childId"
        Try(zk.getData.forPath(childPath)) match {
            case Success(nodeData) =>
                Some(createNode(childPath, SerializedSettings.deserialize[Node.Settings](nodeData)))
            case Failure(e) =>
                logger.error("Error getting node data", e)
                None
        }

    }

    private def createNode(path: String, nodeAddresses: Node.Settings): Node = {
        val nodeId = toNodeId(path)
        if (nodeId == localNode.id)
            localNode
        else
            createRemoteNode(nodeId, nodeAddresses)
    }

    private def createRemoteNode(nodeID: NodeId, settings: Node.Settings): Node = RemoteNode(nodeID, settings)

    private val nodesPath: String = s"${settings.discoveryRoot}/node_"

    private def toNodeId(path: String): NodeId = NodeId(path.split("_").last.toLong)

    /**
      * Stop service.
      */
    override def stop(): Unit = synchronized {
        try {
            if(started.get()) {
                logger.info("Stopping discovery service.")
                if (zk.getState == CuratorFrameworkState.STARTED) {
                    logger.info("Shutdown Zookeeper Client.")
                    zk.close()
                }

                logger.info("Shutdown listeners pool.")
                listenersPool.shutdown()
                listenersPool.awaitTermination(1, TimeUnit.HOURS)

                if (started.get() && localNode != null) {
                    logger.info("Stopping local node server.")
                    localNode.stop()
                }
            }
        }
        finally {
            started.set(false)
        }
    }

    /**
      * @return list of all nodes for current cluster version.
      */
    override def allNodes: List[Node] = nodes.values.toList

    /**
      * @param nodeId node id in cluster.
      * @return node in cluster.
      */
    override def get(nodeId: NodeId): Option[Node] = nodes.get(nodeId)

    /**
      * Subscribe to topology changing events.
      *
      * @param eventListener listener.
      */
    override def subscribe(eventListener: EventListener): Unit =
        listeners.put(eventListener.hashCode(), eventListener)

    /**
      * Unsubscribe from topology changing events.
      *
      * @param eventListener listener.
      */
    override def unsubscribe(eventListener: EventListener): Unit =
        listeners.remove(eventListener.hashCode())
}
