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

package ru.avplatonov.keter.core.task.resource

import ru.avplatonov.keter.core.task.TaskField

/**
  * Represents a tuple consists of some object with their ordering.
  *
  * @param ord object ordering among other objects of its type.
  * @param obj object.
  * @tparam T type of ordering.
  */
case class TaskOrdering[T](ord: Long, obj: T)

/**
  * Trait represents the interface of acquiring current priorities in system for several
  * system resources and task fields.
  */
trait TaskPriorityEstimator {
    /**
      * Current resource ordering with values and restrictions.
      */
    val resourceOrdering: Seq[TaskOrdering[Resource]]

    /**
      * Current task fields ordering.
      */
    val taskFieldsOrdering: Seq[TaskOrdering[TaskField]]
}
