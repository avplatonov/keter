package ru.avplatonov.keter.core.discovery

import java.io.{ByteArrayInputStream, ObjectInputStream}
import java.net.BindException

import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.curator.test.TestingServer
import org.apache.curator.utils.ZKPaths
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import ru.avplatonov.keter.core.discovery

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.duration._

class ZookeeperDiscoveryServiceTest extends FlatSpec with Matchers with BeforeAndAfterAll {
    behavior of "ZK discovery service"

    val zkPort = 9999
    val connString = s"127.0.0.1:$zkPort"
    val zkConnSettings = ZookeeperDiscoveryService.Settings(
        connectionString = connString,
        new ExponentialBackoffRetry(1000, 3),
        "/root/nodes",
        discoveryTimeout = 50 millis
    )

    var zkTestingServer: TestingServer = null
    var zk: CuratorFramework = null

    override protected def beforeAll(): Unit = {
        zkTestingServer = new TestingServer(9999, true)
        zk = CuratorFrameworkFactory.newClient(zkConnSettings.connectionString, zkConnSettings.retryPolicy)
        zk.start()
    }

    it should "starts and register in ZK" in {
        val discovery = ZookeeperDiscoveryService(zkConnSettings, Node.Settings("127.0.0.1", 8081))

        try {
            discovery.start()
            checkServiceStarting(discovery)

            val nodeIds = ZKPaths.getSortedChildren(zk.getZookeeperClient.getZooKeeper, "/root/nodes").asScala
                .map(x => x.replaceAll("node_", "").toLong)
                .toSet
            val localNode = discovery.getLocalNode.get
            nodeIds.contains(localNode.id.value) should equal(true)
            val bytes = zk.getData.forPath("/root/nodes/node_0000000000")
            val settings = resource.managed(new ObjectInputStream(new ByteArrayInputStream(bytes)))
                .map(x => x.readObject().asInstanceOf[Node.Settings])
                .opt

            settings.isDefined should equal(true)
            settings.get should equal(localNode.settings)
        }
        finally {
            discovery.stop()
            checkServiceStopping(discovery)
        }
    }

    it should "may start several services on several ports and register nodes" in {
        val discoverySettings = (0 until 10).map(i => Node.Settings("127.0.0.1", 8081 + i))
        val discoveries = discoverySettings.map(s => ZookeeperDiscoveryService(zkConnSettings, s))

        try {
            (discoveries zip discoverySettings) foreach {
                case (service, settings) =>
                    service.start()
                    checkServiceStarting(service)
            }

            val allnodeIds = discoveries.flatMap(_.getLocalNode).map(_.id).toSet
            discoveries foreach {
                case discovery =>
                    discovery.allNodes.map(_.id).toSet should equal(allnodeIds)
                    discovery.allNodes.count(_.isLocal) should equal(1)
            }
        }
        finally {
            var error: Throwable = null
            discoveries.foreach(d => try {
                d.stop()
                checkServiceStopping(d)
            }
            catch {
                case e: Throwable =>
                    error = e
            })

            if (error != null)
                throw error
        }
    }

    it should "fire envents on topology changes" in {
        val AWAITING_TIME = 1000
        val firstService = ZookeeperDiscoveryService(zkConnSettings, Node.Settings("127.0.0.1", 8081))

        val incomings = mutable.Buffer[NodeId]()
        val outcomings = mutable.Buffer[NodeId]()

        firstService.subscribe((top, diff) => {
            incomings ++= diff.newNodes.keySet
            outcomings ++= diff.removedNodes.keySet
        })

        try {
            firstService.start()

            val firstStartId = startStop(ZookeeperDiscoveryService(zkConnSettings, Node.Settings("127.0.0.1", 8082)))
            val secondStartId = startStop(ZookeeperDiscoveryService(zkConnSettings, Node.Settings("127.0.0.1", 8082)))

            Thread.sleep(AWAITING_TIME)
            incomings.size should equal(2)
            outcomings.size should equal(2)
            firstStartId shouldNot equal(secondStartId)
        }
        finally {
            firstService.stop()
        }
    }

    private def startStop(service: DiscoveryService): NodeId = {
        service.start()
        val nodeID = service.getLocalNode.get.id
        service.stop()
        nodeID
    }

    it should "bind to 8081 port" in {
        val firstService = ZookeeperDiscoveryService(zkConnSettings, Node.Settings("127.0.0.1", 8081))
        try {
            firstService.start()
            assertThrows[BindException] {
                startStop(ZookeeperDiscoveryService(zkConnSettings, Node.Settings("127.0.0.1", 8081)))
            }
        } finally {
            firstService.stop()
        }
    }

    it should "deny restart one service several times" in {
        assertThrows[RepeatedStartException] {
            val service = ZookeeperDiscoveryService(zkConnSettings, new discovery.Node.Settings("127.0.0.1", 8081))
            startStop(service)
            startStop(service)
        }
    }

    private def checkServiceStarting(discovery: DiscoveryService): Unit = {
        discovery.isStarted should equal(true)
        val localNode = discovery.getLocalNode
        localNode.isDefined should equal(true)
        localNode.get.isLocal should equal(true)
        val localNodeId = localNode.get.id
        discovery.get(localNodeId).map(_.id) should equal(Some(localNodeId))
        discovery.allNodes.map(_.id).contains(localNodeId)
    }

    private def checkServiceStopping(discovery: DiscoveryService): Unit = {
        discovery.isStarted should equal(false)
    }

    override protected def afterAll(): Unit = {
        zk.close()
        zkTestingServer.stop()
    }
}
