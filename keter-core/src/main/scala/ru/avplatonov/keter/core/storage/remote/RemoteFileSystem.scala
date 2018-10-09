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

import ru.avplatonov.keter.core.discovery.messaging.Message
import ru.avplatonov.keter.core.discovery.{DiscoveryService, Topology, TopologyDiff}
import ru.avplatonov.keter.core.messages.Messages
import ru.avplatonov.keter.core.storage.remote.index.{ExchangeFileIndexesMessage, FilesIndex}
import ru.avplatonov.keter.core.storage.remote.stream.FilesStream
import ru.avplatonov.keter.core.storage.{FileDescriptor, PathScheme}

import scala.collection.JavaConverters._

case class RemoteFileDescriptor(path: List[String], key: String, isDir: Option[Boolean])
    extends FileDescriptor {

    /** Type of file system. */
    override val scheme: PathScheme = PathScheme.remote

    override def toProto: Messages.FileDescriptor = {
        val builder = Messages.FileDescriptor.newBuilder()
        builder.setKey(key)
        builder.setIsDirectory(isDir.getOrElse(false))
        builder.addAllPath(path.asJava)
        builder.build()
    }
}

class RemoteFileSystem(discovery: DiscoveryService) {
    private var localIndex: FilesIndex = ???
    private var index: FilesIndex = ???
    private var filesStream: FilesStream = ???

    def start(): Unit = {
        localIndex = collectLocalIndex()
        discovery.subscribe(topologyChange)
        val otherIndexes = indexesExchange(localIndex)
//        index = otherIndexes.foldLeft(localIndex)((i1, i2) => i1.merge(i2))
        filesStream.start()
    }

    def stop(): Unit = {
        filesStream.stop()
    }

    private def collectLocalIndex(): FilesIndex = ???

    private def indexesExchange(index: FilesIndex): Seq[FilesIndex] = {
        discovery.getLocalNode().get.registerProcessor(classOf[ExchangeFileIndexesMessage], onExchange)
        ???
    }

    private def topologyChange(top: Topology, diff: TopologyDiff) = ???

    private def onExchange(msg: Message) = ???
}
