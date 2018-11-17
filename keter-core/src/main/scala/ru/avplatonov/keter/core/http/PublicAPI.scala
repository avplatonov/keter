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

package ru.avplatonov.keter.core.http

import java.util.UUID

import ru.avplatonov.keter.core.storage.FileDescriptor
import ru.avplatonov.keter.core.task.TaskStatus
import ru.avplatonov.keter.core.task.resource.ResourceType

case class TaskState()

/**
  * Public API for HTTP REST API.
  * Just facade for main subsystems of worker.
  */
trait PublicAPI {
    /**
      * @return resource count map.
      */
    def currentResourcesState(): Map[ResourceType, Long]

    /**
      * @return statistics over task statuses.
      */
    def workingQueueState(): Map[TaskStatus, Long]

    /**
      * @param desc file descriptor in cluster FS.
      * @return array of bytes representing file.
      */
    def getFile(desc: FileDescriptor): Array[Byte]

    /**
      * Save file in cluster FS.
      *
      * @param desc file descriptor in cluster FS.
      * @param data file content.
      */
    def putFile(desc: FileDescriptor, data: Array[Byte]): Unit

    /**
      * Gets task description and save it as template in system.
      *
      * @param task template.
      * @return id of task.
      */
    def createTaskTemplate(task: Any): UUID

    /**
      * Gets task description and save it as template in system.
      *
      * @param graph template.
      * @return id of graph.
      */
    def createGraphTemplate(graph: Any): UUID

    /**
      * Run graph by id and its parameters for run.
      *
      * @param graphId template id.
      * @param parameters parameters map.
      * @return running graph id.
      */
    def runGraph(graphId: UUID, parameters: Map[String, Object]): UUID

    /**
      * By running graph id returns map of each task in graph with their states description.
      *
      * @param graphId running graph id.
      * @return task states map.
      */
    def checkGraphState(graphId: UUID): Map[UUID, TaskState]
}
