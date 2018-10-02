package ru.avplatonov.keter.core.discovery.messaging

import java.net.{BindException, ServerSocket}
import java.util.concurrent.{CountDownLatch, TimeUnit}

import org.scalatest.concurrent.Timeouts._
import org.scalatest.time.SpanSugar._
import org.scalatest.{FlatSpec, Matchers}
import ru.avplatonov.keter.core.discovery.NodeId

class NettyServerMngrTest extends FlatSpec with Matchers {
    "server" should "started in non-blocking way" in {
        failAfter(5 seconds) {
            val netty = server()
            netty.run()
            netty.stop(force = false)
        }
    }

    "server" should "bind port" in {
        assertThrows[BindException] {
            val netty = server()
            try {
                netty.run()
                Thread.sleep(200)
                val socket = new ServerSocket(8081)
                socket.accept()
            }
            finally {
                netty.stop(force = false)
            }
        }
    }

    "server" should "process input messages" in {
        val MESSAGES_CNT = 100
        val msgsLatch = new CountDownLatch(MESSAGES_CNT)
        val netty = server(m => {
            assert(m.isInstanceOf[HelloMessage])
            assert(m.from.value == -1)
            msgsLatch.countDown()
        })

        try {
            netty.run()

            val client = new Client(Client.Settings("127.0.0.1", 8081))
            for (i <- 0 until MESSAGES_CNT) {
                client.send(HelloMessage(NodeId(-1)))
            }

            msgsLatch.await(1, TimeUnit.SECONDS) should equal(true)
        } finally {
            netty.stop(force = false)
        }
    }

    private def server(
        messageProcessor: Message => Unit = m => {}): NettyServerMngr = new NettyServerMngr(8081, messageProcessor)
}
