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

package ru.avplatonov.keter.core.worker.context

/**
  * Key for representing type of context.
  * For example, it may be name of docker container name.
  *
  * @tparam T type of context descriptor.
  */
abstract class ContextDescriptor[T] {
    /**
      * Context name.
      */
    val ctxName: String

    /**
      * Contetx unique key.
      */
    val ctxKey: String
}

/**
  * Context descriptor for default contexts.
  *
  * @param ctxType Default context name.
  */
case class DefaultContext(ctxType: DefaultContexts) extends ContextDescriptor[DefaultContexts] {
    override val ctxName: String = ctxType.name()
    override val ctxKey: String = ctxType.name()
}

/**
  * User-defined context descriptor.
  *
  * @param ctxName Name.
  * @param ctxKey Unique key.
  */
case class CustomContext(ctxName: String, ctxKey: String) extends ContextDescriptor[String]

/**
  * Context trait.
  * It may be Docker container with own fields and parameters.
  */
trait Context {
    /**
      * Descriptor.
      */
    val key: ContextDescriptor[AnyRef]
}

/**
  * API for finding working contexts.
  * Worker should get context by descriptor and process
  * task in given contetx.
  *
  * @tparam T type of context. For example DockerContext.
  */
trait ContextAPI[T <: Context] {
    def find(key: ContextDescriptor[AnyRef]): Option[T]
}
