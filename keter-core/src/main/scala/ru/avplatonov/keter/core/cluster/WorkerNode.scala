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

package ru.avplatonov.keter.core.cluster

/**
  * API for worker registering in cluster of workers.
  * Cluster coordinator uses this objects as representations of some workers in cluster and
  * call methods of this API at several steps of worker lifecycle.
  */
trait WorkerNode {
    /**
      * Unique ID in cluster.
      *
      * @return id.
      */
    def uniqueID(): String

    /**
      * It is called while coordinator starting.
      */
    def onStarting(): Unit

    /**
      * It is called before coordinator register node in cluster.
      */
    def beforeRegistering(): Unit

    /**
      * It is called after coordinator connect to cluster.
      */
    def onConnect(): Unit

    /**
      * It is called after coordinator successful register node.
      */
    def onRegister(): Unit

    /**
      * It is called after coordinator disconnect from cluster.
      */
    def onDisconnect(): Unit

    /**
      * It is called after coordinator started reconnecting procedure node in cluster.
      */
    def beforeReconnect(): Unit

    /**
      * It is called after coordinator reconnect to cluster.
      * NOTE that onRegister will be fired again.
      */
    def onReconnect(): Unit

    /**
      * It is called when coordinator shutdown.
      */
    def onShutdown(): Unit
}
