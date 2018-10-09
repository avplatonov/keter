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

package ru.avplatonov.keter.core.storage.remote.index

import ru.avplatonov.keter.core.discovery.NodeId
import ru.avplatonov.keter.core.storage.FileDescriptor

import scala.collection.mutable

case class FilesDBOnHashMap() extends FilesDB {
    private val index: mutable.Map[(String, NodeId), FileDescriptor] = mutable.Map()

    override def find(path: String): Option[FilesIndexRow] = {
        val rows = index.filter({ case ((p, _), _) => p.equals(path) })
        rows.headOption.map({
            case (_, desc) => FilesIndexRow(desc, rows.map(_._1._2).toSet)
        })
    }

    override def insert(key: String, filesIndexRow: FilesIndexRow): Unit = {
        filesIndexRow.replicas.foreach({
            case nodeId => index.put((key, nodeId), filesIndexRow.fileDescriptor)
        })
    }

    override def insert(localFiles: Stream[(String, FilesIndexRow)]): Unit = localFiles.foreach({
        case (key, row) => insert(key, row)
    })

    override def delete(rowKey: RowKey): Unit = index.remove(rowKey.path -> rowKey.nodeId)

    override def deleteAllFor(nodeId: NodeId): Unit =
        index.keysIterator.filter(_._2 == nodeId).foreach(index.remove)

    def idx(): Map[(String, NodeId), FileDescriptor] = index.toMap
}
