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

import ru.avplatonov.keter.core.task.resource.ResourceManager

/**
  * Coordinator API represents main service for workers in cluster registration in application. Coordinator
  * is responsible for workers lifecycle, working with other coordinators in cluster.
  */
trait Coordinator {
    /**
      * System resources manager.
      */
    val resourceManager : ResourceManager

    /**
      * Starts coordinator on worker node. Should be called in main thread.
      *
      * @param workers workers to registration.
      * @param settings settings.
      */
    def start(workers: List[WorkerNode], settings: Coordinator.Settings): Unit

    /**
      * Shutdown coordinator node and all workers on local system.
      */
    def shutdown(): Unit
}

/** */
object Coordinator {
    /** */
    case class Settings()
}
