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

import java.nio.file.Path

/** */
object LocalTemporaryFilesStorage {
    /** */
    case class Settings(tempDir: Path)
}

/** File storage in local FS for temp files. */
class LocalTemporaryFilesStorage(settings: LocalTemporaryFilesStorage.Settings)
    extends OnCountersTempObjsCache[Path, LocalFileDescriptor] {

    /**
      * Will be fired when object is pasted to cache.
      *
      * @param key   key.
      * @param value value.
      */
    override protected def onPut(key: Path, value: LocalFileDescriptor): LocalFileDescriptor = {
        val tmpDesc = tmp(value)
        LocalFilesStorage.move(value, tmpDesc, ignoreExisting = true)
        tmpDesc
    }

    /**
      * Will be fired when object removed from cache.
      *
      * @param key   key.
      * @param value value.
      */
    protected override def onRemove(key: Path, value: LocalFileDescriptor): Unit = LocalFilesStorage.delete(tmp(value))

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
