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

/**
  * API for other nodes discovery.
  */
trait DiscoveryService {
    /**
      * Start service.
      */
    def start(): Node

    /**
      * Stop service.
      */
    def stop()

    /**
      * @return list of all nodes for current cluster version.
      */
    def allNodes: List[Node]

    /**
      * @param nodeId node id in cluster.
      * @return node in cluster.
      */
    def get(nodeId: NodeId): Option[Node]

    /**
      * Subscribe to topology changing events.
      *
      * @param eventListener listener.
      */
    def subscribe(eventListener: EventListener): Unit

    /**
      * Unsubscribe from topology changing events.
      *
      * @param eventListener listener.
      */
    def unsubscribe(eventListener: EventListener): Unit

    /**
      * @return true if service was started.
      */
    def isStarted(): Boolean

    /**
      * @return local node if service was started.
      */
    def getLocalNode(): Option[LocalNode]
}

/**
  * Topology changes listener.
  */
trait EventListener {
    /**
      * Topology change callback.
      *
      * @param newTopology new topology state.
      * @param topologyDiff diff with last version.
      */
    def apply(newTopology: Topology, topologyDiff: TopologyDiff): Unit
}

/**
  * Cluster topology representation.
  */
case class Topology(nodes: Map[NodeId, Node]) {
    /**
      * Returns diff between topologies of several versions.
      *
      * @param other other state.
      * @return diff.
      */
    def diff(other: Topology): TopologyDiff = {
        val leftKeys = this.nodes.keySet
        val rightKeys = other.nodes.keySet

        TopologyDiff(
            newNodes = rightKeys.filterNot(leftKeys).map(k => k -> other.nodes(k)).toMap,
            removedNodes = leftKeys.filterNot(rightKeys).map(k => k -> this.nodes(k)).toMap
        )
    }
}

/** */
case class TopologyDiff(
    newNodes: Map[NodeId, Node],
    removedNodes: Map[NodeId, Node]
)
