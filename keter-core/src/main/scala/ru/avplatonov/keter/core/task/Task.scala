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
  * Represents dependencies between tasks.
  *
  * @param tasks tasks list with their local ids in graphTemplate.
  * @param edges dependencies in graphTemplate.
  */
case class Graph(tasks: Map[Long, Task], edges: Map[Long, Long])

/**
  * Represents task in queue.
  *
  * @param id unique id in system.
  * @param status status.
  */
case class Task(id: UUID, status: TaskStatus)

/**
  * Represents abstract task field.
  *
  * @param name field name.
  * @param value field value.
  */
case class TaskField(name: String, value: Any)

object TaskField {
    def extract(name: String)(task: Task): TaskField = ???
}
