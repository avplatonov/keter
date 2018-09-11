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

package ru.avplatonov.keter.core.storage.local

import java.io.{InputStream, OutputStream}
import java.nio.file.Path

import resource.ManagedResource
import ru.avplatonov.keter.core.storage.FileStorage

import scala.concurrent.duration.Duration

/** */
object LocalTemporaryFilesStorage {
    /** */
    case class Settings(
        tempDir: Path,
        deletingTimeout: Duration
    )
}

/** File storage in local FS for temp files. */
class LocalTemporaryFilesStorage(settings: LocalTemporaryFilesStorage.Settings)
    extends FileStorage[LocalFileDescriptor] with OnCountersTempObjsCache[Path, LocalFileDescriptor] {

    /** */
    override def exists(fileDesc: LocalFileDescriptor): Boolean = LocalFilesStorage.exists(tmp(fileDesc))

    /**  */
    override def create(currName: LocalFileDescriptor, ignoreExisting: Boolean): Boolean =
        LocalFilesStorage.create(tmp(currName), ignoreExisting)

    /** */
    override def move(from: LocalFileDescriptor, to: LocalFileDescriptor, ignoreExisting: Boolean): Boolean =
        LocalFilesStorage.move(tmp(from), tmp(to), ignoreExisting)

    /** */
    override def copy(from: LocalFileDescriptor, to: LocalFileDescriptor, ignoreExisting: Boolean): Boolean =
        LocalFilesStorage.copy(tmp(from), tmp(to), ignoreExisting)

    /** */
    override def getFilesInDirectory(desc: LocalFileDescriptor): List[LocalFileDescriptor] =
        LocalFilesStorage.getFilesInDirectory(tmp(desc))

    /** */
    override def delete(desc: LocalFileDescriptor): Boolean = LocalFilesStorage.delete(desc)

    /** */
    override def read(desc: LocalFileDescriptor): ManagedResource[InputStream] = LocalFilesStorage.read(tmp(desc))

    /** */
    override def write(desc: LocalFileDescriptor): ManagedResource[OutputStream] = LocalFilesStorage.write(tmp(desc))

    /**
      * Will be fired when object removed from cache.
      *
      * @param key   key.
      * @param value value.
      */
    override def onRemove(key: Path, value: LocalFileDescriptor): Unit = delete(value)

    /**
      * Remplace path to file with temp directory path.
      *
      * @param desc file desc.
      * @return file desc in tmp dir.
      */
    private def tmp(desc: LocalFileDescriptor): LocalFileDescriptor = desc.copy(
        filepath = settings.tempDir.resolve(desc.key)
    )
}
