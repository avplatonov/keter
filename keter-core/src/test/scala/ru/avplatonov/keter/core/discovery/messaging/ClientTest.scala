package ru.avplatonov.keter.core.discovery.messaging

import java.util.concurrent.{CountDownLatch, TimeUnit}

import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.duration._

class ClientTest extends FlatSpec with Matchers {
    val client = new Client(Client.Settings(
        serverHost = "127.0.0.1",
        serverPort = 8081,
        sendTimeout = 1 second
    ))

    "client" should "consider sending timeouts" in {
        assertThrows[SendingDataException] {
            client.send(HelloMessage())
        }
    }

    "client" should "send data to server" in {
        val latch = new CountDownLatch(1)
        val server = new NettyServerMngr(8081, m => if(m.isInstanceOf[HelloMessage]) latch.countDown())
        server.run()
        try {
            client.send(HelloMessage())
            latch.await(1, TimeUnit.SECONDS) should equal(true)
        } finally {
            server.stop()
        }
    }
}
