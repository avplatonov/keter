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

object NettyServer {

    case class Settings(
        bossPoolSize: Int = 3, //why such values? I don't know, we should investigate optimal parameters
        workerPoolSize: Int = 3
    )

}

case class NettyServer(port: Int,
    messageProcessor: Message => Unit,
    settings: NettyServer.Settings = NettyServer.Settings()) {

    @volatile private var wasStarted = false
    @volatile private var wasStopped = false
    @volatile private var channel: Channel = null

    val startingLatch = new CountDownLatch(1)
    val serverError = new AtomicReference[Exception]()


    private val msgTypeMapping = MessageType.values().map(x => x.ordinal() -> x).toMap

    private val serverPool = Executors.newSingleThreadExecutor(
        new ThreadFactoryBuilder()
            .setNameFormat("nio-server-main-%d")
            .setUncaughtExceptionHandler(uncaughtExceptionHandler)
            .build()
    )

    def run(): Unit = {
        if(!wasStarted) {
            serverPool.submit(new Runnable {
                override def run(): Unit = {
                    val bossGroup = new NioEventLoopGroup(settings.bossPoolSize, //incoming connections accepting thread-pool
                        new ThreadFactoryBuilder()
                            .setNameFormat("nio-server-boss-%d")
                            .setUncaughtExceptionHandler(uncaughtExceptionHandler)
                            .build())
                    val workerGroup = new NioEventLoopGroup(settings.workerPoolSize, //messages after accepting connection processing pool
                        new ThreadFactoryBuilder()
                            .setNameFormat("nio-server-worker-%d")
                            .setUncaughtExceptionHandler(uncaughtExceptionHandler)
                            .build())

                    try {
                        val bootstrap = new ServerBootstrap()
                        bootstrap.group(bossGroup, workerGroup)
                            .channel(classOf[NioServerSocketChannel])
                            .childHandler(new ChannelInitializer[SocketChannel] {
                                override def initChannel(c: SocketChannel): Unit = {
                                    c.pipeline().addLast(new MessagesDecoder(), new ReceiveMessageHandler())
                                }
                            })
                            .option(ChannelOption.SO_BACKLOG, Int.box(128))
                            .childOption(ChannelOption.SO_KEEPALIVE, Boolean.box(true))

                        val channelFuture = bootstrap.bind(port).sync()
                        val channel = channelFuture.channel()
                        init(channel)
                        startingLatch.countDown()
                        channel.closeFuture().sync()
                    } catch {
                        case e: Exception =>
                            serverError.set(e)
                    } finally {
                        bossGroup.shutdownGracefully()
                        workerGroup.shutdownGracefully()
                        startingLatch.countDown()
                        wasStopped = true
                    }
                }
            })

            startingLatch.await()
            if(serverError.get() != null)
                throw serverError.get()
        }
    }

    def stop(force: Boolean): Unit = synchronized {
        if (!wasStopped && channel != null) {
            channel.close()
            if(force) serverPool.shutdownNow()
            else serverPool.shutdown()
            serverPool.awaitTermination(1, TimeUnit.HOURS)
        }
        wasStopped = true
    }

    private def init(channel: Channel): Unit = {
        wasStarted = true
        this.channel = channel
    }

    private def uncaughtExceptionHandler(thread: Thread, e: Throwable) = stop(force = true)

    private class MessagesDecoder() extends ByteToMessageDecoder {
        override def decode(context: ChannelHandlerContext, buf: ByteBuf, list: util.List[AnyRef]): Unit = {
            val bytesInBuffer = buf.readableBytes()
            while(bytesInBuffer >= 4) {
                val msgType = msgTypeMapping(buf.getInt(buf.readerIndex()))
                if(bytesInBuffer == 4 + Message.sizeof(msgType)) {
                    buf.readInt()
                    val msg = Message.read(msgType, buf)
                    list.add(msg)
                }
            }
        }
    }

    private class ReceiveMessageHandler() extends ChannelInboundHandlerAdapter {
        override def channelRead(ctx: ChannelHandlerContext, chMsg: scala.Any): Unit = {
            try {
                val msg = chMsg.asInstanceOf[Message]
                messageProcessor(msg)
            } finally {
                ctx.close()
            }
        }

        override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
            ctx.close()
            super.exceptionCaught(ctx, cause)
        }
    }

}
