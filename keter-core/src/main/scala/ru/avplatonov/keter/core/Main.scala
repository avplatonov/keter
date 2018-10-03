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

package ru.avplatonov.keter.core

import java.util.concurrent.TimeUnit

import org.apache.curator.retry.ExponentialBackoffRetry
import ru.avplatonov.keter.core.discovery.messaging.{Client, HelloMessage}
import ru.avplatonov.keter.core.discovery.{Node, Topology, TopologyDiff, ZookeeperDiscoveryService}

/**
  * Main class.
  * Use it for application starting.
  *
  * TODO: currently it's used for manual testing.
  */
object Main {
    /** */
    def main(args: Array[String]): Unit = {
        val service = ZookeeperDiscoveryService(
            ZookeeperDiscoveryService.Settings(
                connectionString = "127.0.0.1:2181",
                retryPolicy = new ExponentialBackoffRetry(1000, 3),
                "/root/nodes"),
            Node.Settings("127.0.0.1", 8081)
        )

        service.subscribe((newTopology: Topology, diff: TopologyDiff) => {
            println("Topology changed")
            diff.removedNodes.foreach(n => println(s"+ $n"))
            diff.newNodes.foreach(n => println(s"- $n"))
            println()
        })

        service.start()
        service.getLocalNode.foreach(localNode => {
            localNode.registerProcessor(classOf[HelloMessage], msg => {
                println("Hello message")
            })
        })

        Thread.sleep(TimeUnit.SECONDS.toMillis(1))
        var i = 0
        val client = new Client(Client.Settings(serverHost = "127.0.0.1", serverPort = 8081))
        while (i < 10) {
            client.send(HelloMessage(service.getLocalNode.get.id))
            i = i + 1
        }
        Thread.sleep(TimeUnit.SECONDS.toMillis(1))
        service.stop()
    }
}
