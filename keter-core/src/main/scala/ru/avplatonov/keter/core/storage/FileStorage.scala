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

package ru.avplatonov.keter.core.storage

import java.io.{InputStream, OutputStream}

import resource.ManagedResource

/**
  * Generic representation of file in file system.
  */
abstract class FileDescriptor {
    /** Type of file system. */
    val scheme: PathScheme

    /** Sequence of directories to file. */
    val path: List[String]

    /** Unique name for file in directory. */
    val key: String

    /** true if file descriptor points to directory, Empty if isDir is unknown. */
    val isDir: Option[Boolean]
}

/**
  * API for File Storage than must be implemented by all File Systems [distributed or local].
  */
trait FileStorage[T <: FileDescriptor] {
    /**
      * Checks existing file in File System by descriptor.
      *
      * @param fileDesc File descriptor.
      * @return true if file exists.
      */
    def exists(fileDesc: T): Boolean

    /**
      * Creates new file in File System.
      *
      * @param currName       File descriptor.
      * @param ignoreExisting if ignoreExisting == true then FS rewrite already created file.
      * @return true if operation was successful.
      */
    def create(currName: T, ignoreExisting: Boolean): Boolean

    /**
      * Moves file to target destination.
      *
      * @param from           From.
      * @param to             To.
      * @param ignoreExisting if ignoreExisting == true then FS rewrite already created file.
      * @return true if operation was successful.
      */
    def move(from: T, to: T, ignoreExisting: Boolean): Boolean

    /**
      * Copy file to target destination.
      *
      * @param from           From.
      * @param to             To.
      * @param ignoreExisting if ignoreExisting == true then FS rewrite already created file.
      * @return true if operation was successful.
      */
    def copy(from: T, to: T, ignoreExisting: Boolean): Boolean

    /**
      * Returns list of files for directory.
      * If file is not a directory, then FileIsNotDirectory will be thrown.
      *
      * @param desc Directory path.
      * @return list of files for directory.
      */
    def getFilesInDirectory(desc: T): List[T]

    /**
      * Opens file by descriptor for reading and returns InputStream.
      *
      * @param desc file descriptor.
      * @return stream resource.
      */
    def read(desc: T): ManagedResource[InputStream]

    /**
      * Opens file by descriptor for writing and returns InputStream.
      *
      * @param desc file descriptor.
      * @return stream resource.
      */
    def write(desc: T): ManagedResource[OutputStream]
}
