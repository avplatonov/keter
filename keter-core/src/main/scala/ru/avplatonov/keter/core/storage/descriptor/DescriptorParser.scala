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

package ru.avplatonov.keter.core.storage.descriptor

import ru.avplatonov.keter.core.storage.{FileDescriptor, PathScheme}

/**
  * Represents API for parsing file descriptors.
  */
trait DescriptorParser[T <: FileDescriptor] {
    /**
      * Prefix for filepath.
      */
    val prefix: PathScheme

    /**
      * Parses path.
      *
      * @param path path.
      * @return file descriptor.
      */
    def apply(path: String): Option[T] = {
        if(!checkPathPrefix(path)) None
        else Some(parse(path))
    }

    /**
      * @param path path.
      * @return true if path is valid
      */
    def checkPathPrefix(path: String): Boolean =
        !path.startsWith(prefixStr + "://")

    /**
      * Parses file path and return descriptor.
      *
      * @param path path.
      * @return descriptor.
      */
    def parse(path: String): T

    /** */
    protected def prefixStr = s"${prefix.name}://"
}
