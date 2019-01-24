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

package ru.avplatonov.keter.core.storage.legacy.remote.stream

import java.io._
import java.net.ServerSocket
import java.nio.file.Path
import java.util.UUID
import java.util.concurrent.{ConcurrentLinkedQueue, Executors, LinkedBlockingQueue, TimeoutException}

import com.google.common.util.concurrent.ThreadFactoryBuilder
import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import ru.avplatonov.keter.core.discovery.messaging.{Client, Message}
import ru.avplatonov.keter.core.discovery.{DiscoveryService, NodeId, RemoteNode}
import ru.avplatonov.keter.core.storage.legacy.FileDescriptor
import ru.avplatonov.keter.core.storage.legacy.local.{LocalFileDescriptor, LocalFileDescriptorParser, LocalFilesStorage}

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/**
  * Request for sending files to current node.
  *
  * @param files        list of remote files.
  * @param listenerPort port of downloader.
  * @param from         current node id.
  */
case class DownloadFilesMessage(files: List[FileDescriptor], listenerPort: Int, from: NodeId) extends Message {
    override val id: String = UUID.randomUUID().toString
}

/** */
object FilesStreamOnTcp {

    /** */
    case class Settings(
        savingDirectory: Path, //downloaded files path
        workingDirectory: Path, //saved files path
        downloadPortsFrom: Int,
        downloadPortsTo: Int,
        sendingPoolSize: Int,
        filesAwaitingTimeout: Duration = Duration.Inf
    )

}

/**
  * Abstraction for files exchanging.
  */
case class FilesStreamOnTcp(discoveryService: DiscoveryService, settings: FilesStreamOnTcp.Settings) extends FilesStream {
    assert(settings.downloadPortsTo - settings.downloadPortsFrom > 0)

    private val logger = LoggerFactory.getLogger(s"${getClass.getSimpleName}[${settings.downloadPortsFrom}:${settings.downloadPortsTo}]")

    /** Ports for files downloading. */
    private val availablePorts = new LinkedBlockingQueue[Int]((settings.downloadPortsFrom to settings.downloadPortsTo).asJava)

    /** Download files pool. */
    private val downloadPool = Executors.newFixedThreadPool(settings.downloadPortsTo - settings.downloadPortsFrom + 1,
        new ThreadFactoryBuilder()
            .setNameFormat(s"download-files-stream-pool-[${settings.downloadPortsFrom}:${settings.downloadPortsTo}]-%d")
            .build())

    /** Sending files pool. */
    private val sendingPool = Executors.newFixedThreadPool(settings.sendingPoolSize, new ThreadFactoryBuilder()
        .setNameFormat(s"sending-files-stream-pool-[${settings.downloadPortsFrom}:${settings.downloadPortsTo}]-%d")
        .build())

    /** */
    private val downloadExecutionContext = ExecutionContext.fromExecutor(downloadPool)
    /** */
    private val sendingExecutionContext = ExecutionContext.fromExecutor(sendingPool)

    /** */
    //TODO: create watching to sending queue and add fut to it
    private val sendingQueue = new ConcurrentLinkedQueue[SendingFileFuture]()

    /**
      * Send request to remote node with list of files and open socket on free port for files awaiting.
      *
      * @param files files list.
      * @param from  remote node with files.
      * @return remote files wrapper.
      */
    override def download(files: List[FileDescriptor], from: RemoteNode): RemoteFiles = {
        files.find(desc => desc.isDir.getOrElse(false)) match {
            case Some(dir) => throw DirectoryCopyingException(dir)
            case None =>
        }

        val filesWithSuffix = files.map(f => f -> UUID.randomUUID().toString)
        val fut = Future[List[DownloadedFile]]({
            val port = getFreePort()
            try {
                from.sendMsg(DownloadFilesMessage(files, port, discoveryService.getLocalNode().get.id))
                resource.managed(new ServerSocket(port))
                    .flatMap(s => resource.managed(s.accept()))
                    .flatMap(s => resource.managed(s.getInputStream))
                    .foreach(saveFiles(_, filesWithSuffix))
                filesWithSuffix.map({
                    case (file, suffix) => DownloadedFile(file, downloadedFile(file, suffix))
                })
            }
            finally {
                releasePort(port)
            }
        })(downloadExecutionContext)

        RemoteFilesWithAwaitingTimeout(fut, settings.filesAwaitingTimeout)
    }

    /**
      * Send files from LocalFS to host:port
      *
      * @param files files list.
      * @param to    target host port.
      * @return awaitable future.
      */
    private def send(files: List[LocalFileDescriptor], to: DownloadTarget): Future[Unit] = {
        Future({
            new Client(Client.Settings(serverHost = to.host, serverPort = to.port)).send(os => {
                os.writeInt(files.size)
                files.foreach(f => os.writeLong(LocalFilesStorage.sizeOf(f)))
                files.foreach(f => sendFile(f, os))
            })
        })(sendingExecutionContext)
    }

    /**
      * Send file to output stream.
      *
      * @param desc local file desc.
      * @param out  out.
      */
    private def sendFile(desc: LocalFileDescriptor, out: OutputStream): Unit = {
        resource.managed(new FileInputStream(desc.filepath.toFile)) foreach { from =>
            IOUtils.copyLarge(from, out)
        }
    }

    def start(): Unit = {
        if (discoveryService.isStarted() && discoveryService.getLocalNode().isDefined) {
            val Some(node) = discoveryService.getLocalNode()
            node.registerProcessor(classOf[DownloadFilesMessage], downloadFileMessageCallback)
            wasStartedFlag.set(true)
        }
        else {
            throw new IllegalStateException("Cannot start service before discovery starting")
        }
    }

    def stop(): Unit = {
        wasStoppedFlag.set(true)
    }

    private def downloadFileMessageCallback(msg: Message): Unit = {
        if (isWorking) Try {
            val downloadFilesMsg = msg.asInstanceOf[DownloadFilesMessage]
            //TODO: we should consider that files may be deleted in node or something other - we should implement error protocol

            val localFiles = downloadFilesMsg.files
                .map(desc => desc.path.foldLeft(
                    settings.workingDirectory)(
                    (dir, tok) => dir.resolve(tok)).resolve(desc.key))
                .map(filePath => LocalFileDescriptorParser.parse(filePath.toString))

            localFiles.filter(f => !LocalFilesStorage.exists(f)) match {
                case Nil =>
                    discoveryService.get(msg.from) match {
                        case Some(node) =>
                            //TODO: create watching to sending queue and add fut to it
                            send(localFiles, DownloadTarget(node.settings.address, downloadFilesMsg.listenerPort))
                        case None =>
                            throw new IllegalStateException(s"Cannot send message to node that left topology [${msg.from}]")
                    }
                case missingFiles =>
                    val message = s"Cannot find these files: ${missingFiles.map(_.filepath.toString).mkString(",")}"
                    throw new IllegalArgumentException(message)
            }
        } match {
            case Success(fut) =>
                logger.debug(s"Successfuly init sending procedure to node ${msg.from}")
            case Failure(e) =>
                logger.error("Sending error", e)
                onSendingFailed(msg.asInstanceOf[DownloadFilesMessage])
        }
    }

    private def onSendingFailed(msg: DownloadFilesMessage): Unit = {
        //TODO: we should consider that files may be deleted in node or something other - we should implement error protocol
    }

    /**
      * @return request new port for files awaiting.
      */
    private def getFreePort(): Int = availablePorts.poll()

    /**
      * @param port release listening port.
      */
    private def releasePort(port: Int) = availablePorts.put(port)

    //TODO: we should consider that files may be deleted in node or something other - we should implement error protocol
    /**
      * Save files from input stream.
      *
      * @param in         input stream.
      * @param filesWithSuffix  file names in stream with unique suffixes.
      */
    private def saveFiles(in: InputStream, filesWithSuffix: List[(FileDescriptor, String)]): Unit = {
        resource.managed(new DataInputStream(in)) foreach {
            dis =>
                val countOfFiles = dis.readInt()
                assert(countOfFiles == filesWithSuffix.size, "Count of files from remote node should correspond to request")
                val fileSizes = (0 until countOfFiles).map(x => dis.readLong()).toList
                filesWithSuffix zip fileSizes foreach {
                    case ((name, suffix), size) =>
                        saveFile(name, size, dis, suffix)
                }
        }
    }

    /**
      * Saves file from input stream.
      *
      * @param name   file name.
      * @param size   size of file in stream.
      * @param from   input stream.
      * @param suffix suffix for temp file.
      */
    private def saveFile(name: FileDescriptor, size: Long, from: DataInputStream, suffix: String): Unit = {
        resource.managed(new FileOutputStream(downloadedFile(name, suffix))) foreach {
            to =>
                IOUtils.copyLarge(from, to, 0, size)
        }
    }

    /**
      * @return filename for temp file.
      */
    private def downloadedFile(desc: FileDescriptor, suffix: String): File =
        settings.savingDirectory.resolve(s"${
            desc.key
        }-$suffix").toFile
}

case class RemoteFilesWithAwaitingTimeout(fut: Future[List[DownloadedFile]], awaitingTimeout: Duration) extends RemoteFiles {
    /**
      * @return true if files was downloaded.
      */
    override def isReady: Boolean = fut.isCompleted

    /**
      * Awaits files and return downloaded files list.
      *
      * @return downloaded files list.
      */
    override def get(): Try[List[DownloadedFile]] = {
        val awaitingStartTs = System.currentTimeMillis()
        while (!isReady) {
            Thread.sleep((1 second).toMillis)

            if((System.currentTimeMillis() - awaitingStartTs) > awaitingTimeout.toMillis) {
                //TODO: make a futures in scala cancelable
                //fut.cancel()
                return Failure(new TimeoutException())
            }
        }

        fut.value match {
            case Some(res) => res
            case None => throw new IllegalStateException("Future was done but value is None")
        }
    }
}


case class DirectoryCopyingException(dir: FileDescriptor) extends RuntimeException(
    s"Prototol doesn't support directories [${dir.path.mkString("/") + "/" + dir.key}]"
)

/** Future with starting time. */
case class SendingFileFuture(startSendingTs: Long, fut: Future[Unit])

/** */
case class DownloadTarget(host: String, port: Int)
