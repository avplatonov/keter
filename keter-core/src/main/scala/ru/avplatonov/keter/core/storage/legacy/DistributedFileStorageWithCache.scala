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

package ru.avplatonov.keter.core.storage.legacy

import java.nio.file.Path

/**
  * Interface for distributed file storage with local file system caching.
  */
trait DistributedFileStorageWithCache[T <: FileDescriptor] extends FileStorage[T] {
    /**
      * Download file from distributed storage to local file system.
      *
      * @param remoteDileDesc Remote file descriptor.
      * @param locPath        Local path for caching.
      * @return true if operation was successful.
      */
    def pull(remoteDileDesc: T, locPath: Path): Boolean

    /**
      * Flush file to distributed storage from local file system.
      *
      * @param locPath        Local path in cache.
      * @param remoteDileDesc Remote file descriptor.
      * @return true if operation was successful.
      */
    def push(locPath: Path, remoteDileDesc: T): Boolean
}
