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

package ru.avplatonov.keter.core.worker

import java.util.UUID

import ru.avplatonov.keter.core.storage.FileStorage
import ru.avplatonov.keter.core.task.Task

case class Settings(timeout: Long)

/**
  * Trait for worker running task on local machine and providing sandbox for this tasks, manipulating with FS for
  * task working, uploading output of task.
  */
trait Worker {
    /**
      * Unique worker id in cluster.
      */
    val id: UUID

    /**
      * Settings for worker like timeouts to task completing.
      */
    val settings: Settings

    /**
      * Resource manager for worker.
      */
    protected val resourceManager: ResourceManager

    /**
      * File storage for worker.
      */
    protected val fileStorage: FileStorage

    /**
      * Gets task description, prepares sandbox for it, downloads files for task running
      * and runs task.
      *
      * @param task task.
      * @return work object, contains unique workId on current node,
      */
    def run(task: Task): Work

    /**
      * Stops work by task.
      *
      * @param task task.
      * @return true if operation was successful, it may returns false if there is no word for task.
      */
    def stop(task: Task): Boolean = findWork(task).exists(w => stop(w))

    /**
      * Stops all task on worker.
      */
    def stopAll(): Unit

    /**
      * Returns status of task running on worker.
      *
      * @param task task.
      * @return task status or UNKNOWN status if task isn't exist.
      */
    def checkStatus(task: Task): WorkStatus = findWork(task).map(checkStatus).getOrElse(WorkStatus.UNKNOWN)

    /**
      * Find work in running task list.
      *
      * @param task task.
      * @return work.
      */
    protected def findWork(task: Task): Option[Work]

    /**
      * Stops work.
      *
      * @param work work.
      * @return true if operation was successful.
      */
    protected def stop(work: Work): Boolean

    /**
      * Returns status of work.
      *
      * @param work work.
      * @return work status.
      */
    protected def checkStatus(work: Work): WorkStatus
}
