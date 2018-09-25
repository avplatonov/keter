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

import java.util.concurrent.ConcurrentHashMap

import ru.avplatonov.keter.core.discovery.messaging.{Client, Message, MessageType, NettyServer}
import ru.avplatonov.keter.core.util.SerializedSettings

import scala.collection.JavaConverters._

/**
  * Remote node.
  */
trait Node {
    val settings: Node.Settings
    val isLocal: Boolean
    val id: NodeId

    def addresses(): List[String] = settings.addresses

    def sendMsg(header: Message)

    def processMsg(header: Message)
}

case class NodeId(value: Long)

object Node {
    case class Settings(addresses: List[String], listenedPort: Int) extends SerializedSettings
}

case class LocalNode(id: NodeId, settings: Node.Settings) extends Node {
    private val netty = NettyServer(settings.listenedPort, processMsg)

    private val msgProcessors = new ConcurrentHashMap[MessageType, Message => Unit]().asScala

    override val isLocal: Boolean = true

    override def sendMsg(header: Message): Unit =
        throw new NotImplementedError("Local node doesn't support sending to self")

    def start(): Unit = netty.run()

    def stop(): Unit = netty.stop(force = false)

    override def processMsg(message: Message): Unit =
        msgProcessors.getOrElse(
            message.`type`,
            throw new RuntimeException(s"Cannot find registered processor for message with type '${message.`type`}'")
        ).apply(message)

    def registerProcessor(msgType: MessageType, proc: Message => Unit): Unit =
        msgProcessors.put(msgType, proc)
}

case class RemoteNode(id: NodeId, settings: Node.Settings) extends Node {
    private val client = new Client(Client.Settings(
        serverHost = settings.addresses.head,
        serverPort = settings.listenedPort)
    )

    override val isLocal: Boolean = false

    override def sendMsg(message: Message): Unit = client.send(message)

    override def processMsg(message: Message): Unit =
        throw new NotImplementedError("Input message should be processed in other system in its local node.")

}
