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

import java.util.UUID

/**
  * API of core scheduler.
  */
trait Scheduler {
    /** */
    val settings: Scheduler.Settings

    /**
      * Starts task scheduling.
      */
    def run(): Unit

    /**
      * Stops service.
      * @param immediately if true then scheduler will not await running tasks
      *                    and shutdown them.
      */
    def stop(immediately: Boolean = false): Unit

    /**
      * Submit task to schedule.
      *
      * @param task task.
      * @return true if operation was successful.
      */
    def submit(task: Task): Boolean

    /**
      * Cancel task.
      *
      * @param taskId task id.
      * @param stopRunning if then then stops task even it is running.
      * @return true if operation was successful.
      */
    def cancel(taskId: UUID, stopRunning: Boolean = true): Boolean
}

/** */
object Scheduler {
    /** */
    case class Settings()
}
