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

import java.io.{DataOutputStream, IOException}
import java.net.{Socket, SocketTimeoutException}

import org.slf4j.LoggerFactory

import scala.concurrent.duration.{Duration, _}

/** */
object Client {
    case class Settings(
        serverHost: String,
        serverPort: Int,
        sendTimeout: Duration = 30 seconds,
        timeoutHandler: SocketTimeoutException => Unit = _.printStackTrace()
    )
}

/** */
case class SendingDataException(e: Exception)
    extends RuntimeException("Sending data from client to server error", e)

/**
  * Simple discovery client.
  * Just open socket, send data to it and close it.
  */
class Client(settings: Client.Settings) {
    /** */
    private val logger = LoggerFactory.getLogger(s"${getClass.getSimpleName}-${settings.serverHost}:${settings.serverPort}")

    /** */
    def send(message: Message): Unit = {
        logger.debug(s"Sending message to server with type ${message.`type`} [$message]")
        send(os => {
            os.writeInt(message.`type`.ordinal())
            os.write(Message.serialize(message))
        })
    }

    /** */
    def send(f: DataOutputStream => Unit): Unit = {
        logger.debug(s"Sending data to server")
        try {
            resource.managed(socket())
                .flatMap(s => resource.managed(s.getOutputStream))
                .flatMap(oos => resource.managed(new DataOutputStream(oos))) foreach { os =>

                f(os)
                os.flush()
            }
        } catch {
            case e: IOException =>
                logger.error("Sending data exception", e)
                throw SendingDataException(e)
        }
    }

    /**
      * Creates socket.
      */
    private def socket(): Socket = {
        val socket = new Socket(settings.serverHost, settings.serverPort)
        socket.setSoTimeout(settings.sendTimeout.toMillis.toInt)
        socket
    }
}
