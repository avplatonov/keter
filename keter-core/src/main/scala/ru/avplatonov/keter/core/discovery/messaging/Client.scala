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

import scala.concurrent.duration.{Duration, _}

object Client {
    case class Settings(
        serverHost: String,
        serverPort: Int,
        sendTimeout: Duration = 30 seconds,
        timeoutHandler: SocketTimeoutException => Unit = _.printStackTrace()
    )
}

case class SendingDataException(e: Exception)
    extends RuntimeException("Sending data from client to server error", e)

class Client(settings: Client.Settings) {
    def send(message: Message): Unit = {
        try {
            resource.managed(socket())
                .flatMap(s => resource.managed(s.getOutputStream))
                .flatMap(oos => resource.managed(new DataOutputStream(oos))) foreach { os =>

                os.writeInt(message.`type`.ordinal())
                os.write(Message.serialize(message))
                os.flush()
            }
        } catch {
            case e: IOException =>
                throw SendingDataException(e)
        }
    }

    private def socket(): Socket = {
        val socket = new Socket(settings.serverHost, settings.serverPort)
        socket.setSoTimeout(settings.sendTimeout.toMillis.toInt)
        socket
    }
}
