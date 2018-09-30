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

package ru.avplatonov.keter.core.discovery.messaging

import java.util.UUID

import io.netty.buffer.ByteBuf

/** Message decoding i-face.
  * TODO: it's a creepy code. We need to replace custom message serialization to protobuf or other frameforks.
  */
object Message {
    /** */
    def read(messageType: MessageType, buf: ByteBuf): Message = messageType match {
        case MessageType.HELLO_MSG =>
        HelloMessage()
    }

    /** */
    def serialize(msg: Message): Array[Byte] = msg.`type` match {
        case MessageType.HELLO_MSG =>
        Array()
        //skip wiring
    }

    /** */
    def sizeof(messageType: MessageType): Int = messageType match {
        case MessageType.HELLO_MSG => 0
    }
}

/***
  * System message.
  */
trait Message {
    val `type`: MessageType
    val id: String
}

/** Just for debugging. */
case class HelloMessage() extends Message {
    override val `type`: MessageType = MessageType.HELLO_MSG
    override val id: String = UUID.randomUUID().toString
}
