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

import java.io.{InputStream, OutputStream}

import ru.avplatonov.keter.core.discovery.messaging.Message
import ru.avplatonov.keter.core.util.SerializedSettings

/**
  * Remote node.
  */
trait Node {
    val settings: Node.Settings
    val isLocal: Boolean
    val id: NodeId

    def addresses(): List[(String, Int)] = settings.addressesWithPorts

    def sendMsg(header: Message, bodyWriter: OutputStream => Unit)

    def processMsg(header: Message, bodyReader: InputStream => Unit)
}

case class NodeId(value: Long)

object Node {
    case class Settings(addressesWithPorts: List[(String, Int)]) extends SerializedSettings
}

case class LocalNode(id: NodeId, settings: Node.Settings) extends Node {
    override val isLocal: Boolean = true

    override def sendMsg(header: Message, bodyWriter: OutputStream => Unit): Unit = ???

    override def processMsg(header: Message, bodyReader: InputStream => Unit): Unit = ???
}

case class RemoteNode(id: NodeId, settings: Node.Settings) extends Node {
    override val isLocal: Boolean = false

    override def sendMsg(header: Message, bodyWriter: OutputStream => Unit): Unit = ???

    override def processMsg(header: Message, bodyReader: InputStream => Unit): Unit = ???
}
