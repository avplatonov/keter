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

package ru.avplatonov.keter.core.storage.legacy.remote.index

import java.nio.file.Path
import java.util.concurrent.atomic.AtomicReference

import org.slf4j.LoggerFactory
import ru.avplatonov.keter.core.discovery
import ru.avplatonov.keter.core.discovery.DiscoveryService
import ru.avplatonov.keter.core.storage.legacy.FileDescriptor
import ru.avplatonov.keter.core.storage.legacy.local.{LocalFileDescriptor, LocalFileDescriptorParser, LocalFilesStorage}

import scala.util.Random

case class FilesIndexOnDB(db: FilesDB, localWorkingDir: Path, discoveryService: DiscoveryService) extends FilesIndex {
    assert(localWorkingDir.isAbsolute, "Workdir path should be absolute")

    private val logger = LoggerFactory.getLogger(getClass)

    private val localIndexRef: AtomicReference[Set[Long]] = new AtomicReference(Set()) //hash codes of all local files

    override def localNodeId: discovery.NodeId = discoveryService.getLocalNodeId().get

    override def rebuildIndex(): Unit = {
        val localFiles: Map[Long, LocalFileDescriptor] = LocalFilesStorage.scanFiles(localWorkingDir).map({
            case file: Path =>
                val withoutWDPrefix: String = removeWDPrefix(file.toAbsolutePath.toString)
                withoutWDPrefix.hashCode.toLong -> LocalFileDescriptorParser.parse(withoutWDPrefix)
                    .copy(isDir = Some(false))
        }).toMap

        db.insert(localFiles.values.map(desc => makeIndexKey(desc) -> FilesIndexRow(desc, Set(localNodeId))).toStream)
        db.rebuildDistributedIndex()
        localIndexRef.set(localFiles.keySet)
    }

    private def removeWDPrefix(str: String): String =
        str.replaceFirst(localWorkingDir.toString, "/")

    override def defineLocation(desc: FileDescriptor): Option[discovery.NodeId] = {
        val key = makeIndexKey(desc)
        if (localIndexRef.get().contains(key.hashCode)) Some(localNodeId)
        else db.find(key).map(r => selectRandomElement(r.replicas))
    }

    override def index(desc: FileDescriptor): Unit =
        db.insert(makeIndexKey(desc), FilesIndexRow(desc, Set(localNodeId)))

    override def remove(target: discovery.NodeId): Unit =
        db.deleteAllFor(target)

    override def remove(target: FileDescriptor): Unit =
        db.delete(RowKey(makeIndexKey(target), localNodeId))

    private def makeIndexKey(desc: FileDescriptor): String =
        if (desc.path.isEmpty) s"//${desc.key}"
        else s"//${desc.path.mkString("/")}/${desc.key}"

    private def selectRandomElement[T](set: Set[T]): T = {
        assert(set.nonEmpty)
        set.find(_ => Random.nextBoolean()).getOrElse(set.head)
    }
}
