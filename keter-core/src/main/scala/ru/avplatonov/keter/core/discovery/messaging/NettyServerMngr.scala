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

import java.util
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.{CountDownLatch, Executors, TimeUnit}

import com.google.common.util.concurrent.ThreadFactoryBuilder
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBuf
import io.netty.channel._
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.ByteToMessageDecoder
import org.slf4j.LoggerFactory

/** */
object NettyServerMngr {
    /** */
    case class Settings(
        bossPoolSize: Int = 3, //why such values? I don't know, we should investigate optimal parameters
        workerPoolSize: Int = 3
    )
}

//todo: replace sync blocks
/**
  * Manager for Netty server. It initializes and starts Netty server, stops it, create decoders for messages, manages
  * pools of server.
  */
case class NettyServerMngr(port: Int,
    messageProcessor: Message => Unit,
    settings: NettyServerMngr.Settings = NettyServerMngr.Settings()) {

    /** */
    private val logger = LoggerFactory.getLogger(s"${getClass.getSimpleName}-$port")

    /** */
    private var wasStarted = false
    /** */
    private var wasStopped = false

    /**
      * Server listening channel.
      */
    private var channel: Channel = _

    /**
      * Equals zero if server was started of failed.
      */
    private val startingLatch = new CountDownLatch(1)

    /**
      * Last error in Netty thread.
      */
    private val workerError = new AtomicReference[Exception]()

    /** */
    private val serverPool = Executors.newSingleThreadExecutor(
        new ThreadFactoryBuilder()
            .setNameFormat("nio-server-main-%d")
            .setUncaughtExceptionHandler(uncaughtExceptionHandler)
            .build()
    )

    /**
      * Runs netty server.
      */
    def run(): Unit = synchronized {
        if(!wasStarted) {
            logger.info(s"Starting netty server on port $port")
            serverPool.submit(new NettyServerWorker())

            logger.info("Await server thread starting")
            startingLatch.await()

            val exception = workerError.get()
            if(exception != null) {
                logger.error("Server starting error. Shutdown server", exception)
                throw exception
            }
        } else {
            logger.warn(s"Repeated staring netty server on port $port", new Exception())
        }
    }

    /**
      * Stops server.
      *
      * @param force true for forced stop of server threads without awaiting of them termination.
      */
    def stop(force: Boolean = false): Unit = synchronized {
        startingLatch.countDown()

        if (!wasStopped && channel != null) {
            logger.info("Stop server request. Close channel.")
            channel.close()

            logger.info("Stop server request. Shutdown pool.")
            if(force) serverPool.shutdownNow()
            else serverPool.shutdown()

            logger.info("Stop server request. Await pool termination.")
            serverPool.awaitTermination(1, TimeUnit.HOURS)

            wasStopped = true
            logger.info("Stop server request. Server was stopped")
        } else {
            logger.warn("Repeated server stop or stopping non-started server", new Exception())
        }
    }

    /** */
    private def initServerMgr(channel: Channel): Unit = {
        wasStarted = true
        this.channel = channel
        startingLatch.countDown()
    }

    /** */
    private def uncaughtExceptionHandler(thread: Thread, e: Throwable): Unit = stop()

    /** */
    private class MessagesDecoder() extends ByteToMessageDecoder {
        /** */
        override def decode(context: ChannelHandlerContext, buf: ByteBuf, list: util.List[AnyRef]): Unit = {
            var bytesInBuffer = buf.readableBytes()
            var break = false
            while(bytesInBuffer >= 4 && !break) {
                val msgSize = buf.getInt(buf.readerIndex())
                if(bytesInBuffer >= 4 + msgSize) {
                    val msg = Message.read(buf)
                    logger.debug(s"Read message $msg")
                    list.add(msg)
                    bytesInBuffer = buf.readableBytes()
                } else {
                    break = true
                }
            }
        }

        /** */
        override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
            ctx.close()
            logger.error("Error while message decoding", cause)
        }
    }

    /** */
    private class ReceiveMessageHandler() extends ChannelInboundHandlerAdapter {
        /** */
        override def channelRead(ctx: ChannelHandlerContext, chMsg: scala.Any): Unit = {
            try {
                val msg = chMsg.asInstanceOf[Message]
                logger.debug(s"New message of type ${msg.getClass.getSimpleName} [$msg]")
                messageProcessor(msg)
            } finally {
                ctx.close()
            }
        }

        /** */
        override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
            ctx.close()
            logger.error("Exception while message reading", cause)
        }
    }

    /**
      * Netty server thread.
      */
    private class NettyServerWorker extends Runnable {
        private val logger = LoggerFactory.getLogger(s"${getClass.getSimpleName}-$port")

        /** */
        override def run(): Unit = {
            logger.info("Starting netty server worker.")

            /** */
            def createPool(name: String, size: Int) = {
                new NioEventLoopGroup(size,
                    new ThreadFactoryBuilder()
                        .setNameFormat(name)
                        .setUncaughtExceptionHandler(uncaughtExceptionHandler)
                        .build())
            }

            val connAcceptPool = createPool(s"nio-server-boss-$port-%d", settings.bossPoolSize)
            val workerGroup = createPool(s"nio-server-worker-$port-%d", settings.workerPoolSize)

            try {
                val bootstrap = new ServerBootstrap()
                bootstrap.group(connAcceptPool, workerGroup)
                    .channel(classOf[NioServerSocketChannel])
                    .childHandler(new ChannelInitializer[SocketChannel] {
                        override def initChannel(c: SocketChannel): Unit = {
                            c.pipeline().addLast(new MessagesDecoder(), new ReceiveMessageHandler())
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, Int.box(128))
                    .childOption(ChannelOption.SO_KEEPALIVE, Boolean.box(true))

                logger.info(s"Binding to port $port")
                val channelFuture = bootstrap.bind(port).sync()
                val channel = channelFuture.channel()
                initServerMgr(channel)
                logger.info(s"Start listening port $port")
                channel.closeFuture().sync()
            } catch {
                case e: Exception =>
                    logger.error("Error while server starting", e)
                    workerError.set(e)
            } finally {
                logger.info("Shutdown server worker.")
                connAcceptPool.shutdownGracefully()
                workerGroup.shutdownGracefully()
                startingLatch.countDown()
                wasStopped = true
            }
        }
    }
}
