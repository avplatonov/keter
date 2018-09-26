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

package ru.avplatonov.keter.core.task

import ru.avplatonov.keter.core.worker.Worker

case class Settings(timeout: Long)

/**
  * Task queue interface providing general API for getting new tasks and updating gotten tasks.
  */
trait TaskQueue {
    /**
      * Reset worker queue. It may be useful after restarts or mass edit.
      *
      * @param worker worker instance owning tasks.
      * @param withStatus reset task statuses to this value.
      */
    def resetQueue(worker: Worker)(withStatus: TaskStatus): Unit

    /**
      * @param worker worker.
      * @return list of all tasks processing by worker.
      */
    def getTasks(worker: Worker): List[Task]

    /***
      * Takes a description of task dependencies and task descriptions, creates all tasks in queue,
      * asssign unique id for them and sets dependencies on this unique ids.
      *
      * @param taskGraph task graphTemplate.
      * @return true if operation was successful.
      */
    def createTasks(taskGraph: Graph): Boolean

    /**
      * Takes a some task with status PENDING and lock it in queue with workerId.
      *
      * @param worker worker.
      * @return locked task in status PENDING.
      */
    def lockTask(worker: Worker)(): Option[Task]

    /**
      * Rewrites fields of the task and sets up new status for it
      * in according to finite-state machine of task statuses.
      *
      * @param worker worker.
      * @param task task.
      * @param status set status to this value.
      * @return true if operation was successful.
      */
    def update(worker: Worker)(task: Task, status: TaskStatus, comment: Comment): Boolean

    /**
      * Case of task updating, only for pretty interface.
      *
      * @param worker worker.
      * @param task task.
      * @return true if operation was successful.
      */
    def complete(worker: Worker)(task: Task): Boolean =
        update(worker)(task, TaskStatus.COMPLETED, Comment.Empty)

    /**
      * Case of task updating, only for pretty interface.
      *
      * @param worker worker.
      * @param task task.
      * @param cause cause of error.
      * @return true if operation was successful.
      */
    def fail(worker: Worker)(task: Task, cause: Exception): Boolean =
        update(worker)(task, TaskStatus.FAILED, Comment.Error(cause))
}
