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

import java.io.{ByteArrayOutputStream, DataOutputStream}
import java.util.UUID

import io.netty.buffer.ByteBuf
import ru.avplatonov.keter.core.discovery.NodeId
import ru.avplatonov.keter.core.messages.Messages
import ru.avplatonov.keter.core.storage.remote.{DownloadFilesMessage, RemoteFileDescriptor}

import scala.collection.JavaConverters._

/** Message decoding i-face.
  * TODO: it's a creepy code. We need to replace custom message serialization to protobuf or other frameforks.
  */
object Message {
    /** */
    def read(buf: ByteBuf): Message = {
        val msgSize = buf.readInt()
        val bytes: Array[Byte] = Array.fill(msgSize)(0)
        buf.readBytes(bytes)

        val msg = Messages.Message.parseFrom(bytes)
        val from = NodeId(msg.getFrom.getId)

        if (msg.hasHelloMsg)
            return HelloMessage(from)
        if (msg.hasFilesRequest) {
            val filesRequest = msg.getFilesRequest
            val filesList = filesRequest.getFilesList.asScala.map(file => {
                RemoteFileDescriptor(
                    path = file.getPathList.asByteStringList().asScala.map(_.toStringUtf8).toList,
                    key = file.getKey,
                    isDir = Some(file.getIsDirectory)
                )
            }).toList
            return DownloadFilesMessage(filesList, filesRequest.getListeningPort, from)
        }

        throw new NotImplementedError()
    }

    /** */
    def serialize(msg: Message): Array[Byte] = {
        val packageOS = new ByteArrayOutputStream()
        val packageDOS = new DataOutputStream(packageOS)

        val msgBuilder = Messages.Message.newBuilder()
        msgBuilder.setFrom(msg.from.toProto)

        msg match {
            case _: HelloMessage =>
                msgBuilder.setHelloMsg(Messages.HelloMessage.newBuilder().build())
            case m: DownloadFilesMessage =>
                val filesReqBuilder = Messages.FilesRequestMessage.newBuilder()
                m.files.foreach(desc => filesReqBuilder.addFiles(desc.toProto))
                msgBuilder.setFilesRequest(filesReqBuilder
                    .setListeningPort(m.listenerPort)
                    .build())
        }

        val msgBytes = msgBuilder.build().toByteArray
        packageDOS.writeInt(msgBytes.length)
        packageDOS.write(msgBytes)
        packageDOS.flush()
        packageOS.toByteArray
    }
}

/** *
  * System message.
  */
trait Message {
    val id: String
    val from: NodeId
}

/** Just for debugging. */
case class HelloMessage(from: NodeId) extends Message {
    override val id: String = UUID.randomUUID().toString
}

