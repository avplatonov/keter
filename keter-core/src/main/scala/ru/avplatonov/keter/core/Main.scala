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

import org.apache.curator.retry.ExponentialBackoffRetry
import ru.avplatonov.keter.core.discovery.{Node, ZookeeperDiscoveryService}

object Main {
    def main(args: Array[String]): Unit = {
        val service = ZookeeperDiscoveryService(ZookeeperDiscoveryService.Settings(
            connectionString = "127.0.0.1:2181",
            retryPolicy = new ExponentialBackoffRetry(1000, 3),
            "/root/nodes"
        ))

        service.subscribe((newTopology: List[Node]) => {
            newTopology.foreach(println)
        })

        val localNode = service.start(Node.Settings(
            addressesWithPorts = List("127.0.0.1" -> 8080)
        ))

        println("==========")
        startStop()
        println("==========")
        startStop()
        println("==========")
        service.stop()
        println("==========")
    }

    def startStop(): Unit = {
        val service = ZookeeperDiscoveryService(ZookeeperDiscoveryService.Settings(
            connectionString = "127.0.0.1:2181",
            retryPolicy = new ExponentialBackoffRetry(1000, 3),
            "/root/nodes"
        ))

        service.start(Node.Settings(
            addressesWithPorts = List("127.0.0.1" -> 8080)
        ))

        service.stop()
    }
}
