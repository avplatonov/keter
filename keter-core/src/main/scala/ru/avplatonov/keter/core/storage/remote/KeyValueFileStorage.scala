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

package ru.avplatonov.keter.core.storage.remote

import java.io.{InputStream, OutputStream}
import java.nio.file.Path

import resource.ManagedResource
import ru.avplatonov.keter.core.storage.local.{LocalFileDescriptor, LocalTemporaryFilesStorage}
import ru.avplatonov.keter.core.storage.{DistributedFileStorageWithCache, FileDescriptor, PathScheme}

/** */
case class RemoteFileDescriptor(path: List[String], key: String, isDir: Option[Boolean])
    extends FileDescriptor {

    /** Type of file system. */
    override val scheme: PathScheme = PathScheme.cassandra
}

/**
  * Working with remote file logic holder.
  */
trait RemoteFile {
    /**
      * Remote file descriptor.
      */
    val desc: FileDescriptor

    /**
      * Downloads file from remote storage and save it as local file.
      *
      * @param desc local file descriptor.
      * @return true if operation was successful.
      */
    def downloadTo(desc: LocalFileDescriptor): Boolean

    /**
      * Uploads file to remote storage and save it as local file.
      *
      * @param desc local file descriptor.
      * @return true if operation was successful.
      */
    def uploadFrom(desc: LocalFileDescriptor): Boolean

    /**
      * @return true if file exists in remote FS.
      */
    def exists(): Boolean
}

//TODO: need to implement over holders
case class KeyValueFileStorage(kv: KVStorage[RemoteFileDescriptor, RemoteFile],
    localTempStorage: LocalTemporaryFilesStorage, remoteFileTree: RemoteFileTree)

    extends DistributedFileStorageWithCache[RemoteFileDescriptor] {
    /** */
    override def pull(remoteDileDesc: RemoteFileDescriptor, locPath: Path): Boolean = ???

    /** */
    override def push(locPath: Path, remoteDileDesc: RemoteFileDescriptor): Boolean = ???

    /** */
    override def exists(fileDesc: RemoteFileDescriptor): Boolean = kv.get(fileDesc).exists()

    /** */
    override def create(currName: RemoteFileDescriptor, ignoreExisting: Boolean): Boolean = ???

    /** */
    override def move(from: RemoteFileDescriptor, to: RemoteFileDescriptor, ignoreExisting: Boolean): Boolean = ???

    /** */
    override def copy(from: RemoteFileDescriptor, to: RemoteFileDescriptor, ignoreExisting: Boolean): Boolean = ???

    /** */
    override def delete(desc: RemoteFileDescriptor): Boolean =
        kv.remove(desc)

    /** */
    override def getFilesInDirectory(desc: RemoteFileDescriptor): List[RemoteFileDescriptor] =
        remoteFileTree.listOfFiles(desc)

    /** */
    override def read(desc: RemoteFileDescriptor): ManagedResource[InputStream] = ???

    /** */
    override def write(desc: RemoteFileDescriptor): ManagedResource[OutputStream] = ???
}
