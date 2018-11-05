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

import java.util.UUID

import ru.avplatonov.keter.core.discovery.NodeId
import ru.avplatonov.keter.core.discovery.messaging.Message
import ru.avplatonov.keter.core.storage.FileDescriptor

case class ExchangeFileIndexesMessage(index: FilesIndex, from: NodeId) extends Message {
    override val id: String = UUID.randomUUID().toString
}

trait FilesIndex {
    def localNodeId: NodeId

    def rebuildIndex(): Unit

    def defineLocation(desc: FileDescriptor): Option[NodeId] = defineLocations(desc).headOption.flatMap(_._2)

    def defineLocations[T >: FileDescriptor](desc: T*): Map[FileDescriptor, Option[NodeId]] = ???

    def index(desc: FileDescriptor): Unit

    def remove(target: NodeId): Unit

    def remove(target: FileDescriptor): Unit
}
