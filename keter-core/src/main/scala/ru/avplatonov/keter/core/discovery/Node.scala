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

import ru.avplatonov.keter.core.discovery.messaging.{Client, Message, MessageType, NettyServerMngr}
import ru.avplatonov.keter.core.util.SerializedSettings

import scala.collection.JavaConverters._

/**
  * Node id in topology.
  */
case class NodeId(value: Long)

/**
  * Abstract node in topology.
  */
trait Node {
    /**
      * Host and port of node.
      */
    val settings: Node.Settings
    /**
      * True if node is local.
      */
    val isLocal: Boolean
    /**
      * Id of node in topology.
      */
    val id: NodeId

    /**
      * Send message to node.
      *
      * @param msg message.
      */
    def sendMsg(msg: Message)

    /**
      * Message processing callback.
      *
      * @param msg message.
      */
    def processMsg(msg: Message)
}

/** */
object Node {
    /** */
    case class Settings(address: String, listenedPort: Int) extends SerializedSettings
}

/**
  * Local node is just Netty server accepting new messages from remote nodes.
  */
case class LocalNode(id: NodeId, settings: Node.Settings) extends Node {
    /**
      * Netty server bound to local node port.
      */
    private val netty = NettyServerMngr(settings.listenedPort, processMsg)

    /**
      * Map of message processors (callbacks).
      * Message processor is implemented by some sub system in core for specific message of sub-system.
      */
    private val msgProcessors = new ConcurrentHashMap[MessageType, Message => Unit]().asScala

    /** */
    override val isLocal: Boolean = true

    /** */
    override def sendMsg(header: Message): Unit =
        throw new NotImplementedError("Local node doesn't support sending to self")

    /**
      * Runs server.
      */
    def start(): Unit = netty.run()

    /**
      * Stops server.
      */
    def stop(): Unit = netty.stop()

    /** */
    override def processMsg(message: Message): Unit =
        msgProcessors.getOrElse(
            message.`type`,
            throw new RuntimeException(s"Cannot find registered processor for message with type '${message.`type`}'")
        ).apply(message)

    /***
      * Add/replace message processor to callbacks.
      *
      * @param msgType message type.
      * @param proc processor.
      */
    def registerProcessor(msgType: MessageType, proc: Message => Unit): Unit =
        msgProcessors.put(msgType, proc)
}

/**
  * Remote node is just client can send messages to other local-node on other machine.
  */
case class RemoteNode(id: NodeId, settings: Node.Settings) extends Node {
    /**
      * Network client.
      */
    private val client = new Client(Client.Settings(
        serverHost = settings.address,
        serverPort = settings.listenedPort)
    )

    /** */
    override val isLocal: Boolean = false

    /** */
    override def sendMsg(message: Message): Unit = client.send(message)

    /** */
    override def processMsg(message: Message): Unit =
        throw new NotImplementedError("Input message should be processed in other system in its local node.")

}
