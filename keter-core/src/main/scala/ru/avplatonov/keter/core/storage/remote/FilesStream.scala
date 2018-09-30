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

package ru.avplatonov.keter.core.storage.remote

import java.io.File
import java.util.UUID
import java.util.concurrent.{ConcurrentLinkedQueue, Executors}

import com.google.common.util.concurrent.ThreadFactoryBuilder
import ru.avplatonov.keter.core.discovery.messaging.{Message, MessageType}
import ru.avplatonov.keter.core.discovery.{DiscoveryService, RemoteNode}
import ru.avplatonov.keter.core.storage.FileDescriptor

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

trait RemoteFile {
    def isReady: Boolean

    def get(): Try[File]
}

case class DownloadFileMessage(files: List[FileDescriptor], listenerPort: Int) extends Message {
    override val `type`: MessageType = MessageType.FILE_REQUEST
    override val id: String = UUID.randomUUID().toString
}

case class FilesStream(discoveryService: DiscoveryService, portsFrom: Int, portsTo: Int) {
    private val availablePorts = new ConcurrentLinkedQueue[Int]((portsFrom to portsTo).asJava)

    private val threadPool = Executors.newFixedThreadPool(portsTo - portsFrom + 1,
        new ThreadFactoryBuilder()
            .setNameFormat(s"files-stream-pool-[$portsFrom:$portsTo]-%d")
            .build())

    private val executionContext = ExecutionContext.fromExecutor(threadPool)

    private def download(files: List[FileDescriptor], from: RemoteNode): RemoteFile = {
        val fut = Future[File]({
            val port = availablePorts.remove()
            from.sendMsg(DownloadFileMessage(files, port))
            null
        })(executionContext)

        new RemoteFile {
            override def isReady: Boolean = fut.isCompleted

            override def get(): Try[File] = Failure(null)
        }
    }
}
