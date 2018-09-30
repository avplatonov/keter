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

package ru.avplatonov.keter.core.storage.remote

import ru.avplatonov.keter.core.discovery.messaging.{Message, MessageType}
import ru.avplatonov.keter.core.discovery.{DiscoveryService, Topology, TopologyDiff}

class RemoteFileSystem(discovery: DiscoveryService) {
    private var localIndex: FilesIndex = ???
    private var index: FilesIndex = ???

    def start(): Unit = {
        localIndex = collectLocalIndex()
        discovery.subscribe(topologyChange)
        discovery.getLocalNode().get.registerProcessor(MessageType.FILE_REQUEST, onFileRequest)
        val otherIndexes = indexesExchange(localIndex)
        index = otherIndexes.foldLeft(localIndex)((i1, i2) => i1.merge(i2))
    }

    private def collectLocalIndex(): FilesIndex = ???

    private def indexesExchange(index: FilesIndex): Seq[FilesIndex] = {
        discovery.getLocalNode().get.registerProcessor(MessageType.INDEXES_EXCHANGE, onExchange)
        ???
    }

    private def topologyChange(top: Topology, diff: TopologyDiff) = ???

    private def onExchange(msg: Message) = ???

    private def onFileRequest(msg: Message) = ???
}
