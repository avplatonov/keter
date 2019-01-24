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

package ru.avplatonov.keter.core.storage.legacy.remote.stream

import java.io.File

import ru.avplatonov.keter.core.discovery.RemoteNode
import ru.avplatonov.keter.core.service.Service
import ru.avplatonov.keter.core.storage.legacy.FileDescriptor

import scala.util.Try

/**
  * Remote files abstraction.
  * Just wrapper for future.
  */
trait RemoteFiles {
    /**
      * @return true if files was downloaded.
      */
    def isReady: Boolean

    /**
      * Awaits files and return downloaded files list.
      *
      * @return downloaded files list.
      */
    def get(): Try[List[DownloadedFile]]
}

case class DownloadedFile(originalDescriptor: FileDescriptor, file: File)

trait FilesStream extends Service {
    def download(files: List[FileDescriptor], from: RemoteNode): RemoteFiles
}
