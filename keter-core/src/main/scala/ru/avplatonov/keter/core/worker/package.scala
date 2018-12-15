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

import java.nio.file.Path

import ru.avplatonov.keter.core.worker.docker.ContainerDescriptor
import ru.avplatonov.keter.core.worker.work.script.ScriptTemplate

package object worker {
    object ParameterType extends Enumeration {
        type ParameterType = Value

        val INT, DOUBLE, STRING = Value
    }

    case class ParameterDescriptors(values: Map[String, ParameterDescriptor])

    case class ParameterDescriptor(value: Any, `type`: ParameterType.Value)

    object ResourceType extends Enumeration {
        type ResourceType = Value
        val IN, OUT = Value
    }

    case class ResourcesDescriptor(values: Map[String, (Path, ResourceType.Value)])

    case class ResourceHandler()

    case class TaskDescriptor(script: ScriptTemplate, containerDesc: ContainerDescriptor)

    case class Work(desc: TaskDescriptor, environment: ResourcesDescriptor)
}
