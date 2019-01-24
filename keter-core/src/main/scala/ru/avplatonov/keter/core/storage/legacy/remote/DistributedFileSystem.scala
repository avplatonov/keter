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

package ru.avplatonov.keter.core.storage.legacy.remote

import java.util.concurrent.atomic.AtomicReference

import ru.avplatonov.keter.core.discovery.{DiscoveryService, NodeId, Topology, TopologyDiff}
import ru.avplatonov.keter.core.messages.Messages
import ru.avplatonov.keter.core.storage.PathScheme
import ru.avplatonov.keter.core.storage.legacy.FileDescriptor
import ru.avplatonov.keter.core.storage.legacy.local.{LocalFileDescriptor, LocalFilesStorage}
import ru.avplatonov.keter.core.storage.legacy.remote.index.FilesIndex
import ru.avplatonov.keter.core.storage.legacy.remote.stream.FilesStream

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.util.Try

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

case class Holder[T](supplier: () => Future[T], releaser: T => Unit, awaitingDuration: Duration = Duration.Inf) {
    def process[R](body: T => R): Try[R] = {
        Try {
            try {

            }
            finally {
//                releaser(value)
            }
        }

        null
    }
}

//TODO: there is no replication, we should do it later
//TODO: it seems that there is need to create batch operations for read-write
case class DistributedFileSystem(discovery: DiscoveryService, index: FilesIndex, filesStream: FilesStream) {
    private var localNode: NodeId = _

    private val topologyState: AtomicReference[Topology] = new AtomicReference[Topology]()

    def start(): Unit = {
        localNode = discovery.getLocalNodeId().get
        discovery.subscribe(topologyChange)
        filesStream.start()
    }

    def stop(): Unit = {
        filesStream.stop()
    }

    def exists(descs: RemoteFileDescriptor*): Boolean = index.defineLocations(descs).values
        .forall(nodeId => nodeId.isDefined && topologyState.get().nodes.contains(nodeId.get))

    def read(descs: RemoteFileDescriptor*): Holder[List[LocalFileDescriptor]] = Holder(downloadFiles(descs.toList), releaseFiles)

    def write(descs: LocalFileDescriptor*): Unit = descs.foreach(index.index)

    private def topologyChange(top: Topology, diff: TopologyDiff) = topologyState.set(top)

    private def downloadFiles(descriptors: List[RemoteFileDescriptor])(): Future[List[LocalFileDescriptor]] = {
        val nodes = index.defineLocations(descriptors)
        if(nodes.values.exists(_.nonEmpty))
            throw new IllegalArgumentException("There is no all files in FS")

        nodes.mapValues(_.get).groupBy(_._2).mapValues(_.keySet) map {
            case (nodeId, descriptors) =>
            //TODO:
            null
        }
        null
    }

    private def releaseFiles(descs: Seq[LocalFileDescriptor]): Unit = descs.foreach(LocalFilesStorage.delete)
}
