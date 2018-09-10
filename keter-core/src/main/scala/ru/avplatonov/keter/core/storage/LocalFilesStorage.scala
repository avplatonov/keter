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
import java.nio.file.{Files, Path, Paths, StandardOpenOption}
import java.util.stream.Collectors

import resource.ManagedResource
import ru.avplatonov.keter.core.storage.descriptor.DescriptorParser

import scala.collection.JavaConverters._

case class LocalFileDescriptor(filepath: Path, path: List[String], key: String, isDir: Option[Boolean])
    extends FileDescriptor {

    /** Type of file system. */
    override val scheme: PathScheme = PathScheme.local
}

/**
  * Parser for local files.
  */
object LocalFileDescriptorParser extends DescriptorParser[LocalFileDescriptor] {
    /**
      * Prefix for filepath.
      */
    override val prefix: PathScheme = PathScheme.local

    /**
      * @inheritdoc
      */
    override def parse(path: String): LocalFileDescriptor = {
        val localPath = path.replaceFirst(prefixStr, "/")
        val jpath = Paths.get(localPath)
        val tokens = jpath.iterator().asScala.map(_.toString).toList
        assert(tokens.size > 1, "path should consists of at least two elements")

        LocalFileDescriptor(
            filepath = jpath,
            path = tokens.init,
            key = tokens.last,
            isDir = Some(Files.isDirectory(jpath))
        )
    }
}

object LocalFilesStorage extends FileStorage[LocalFileDescriptor] {
    /**
      * Checks existing file in File System by descriptor.
      *
      * @param fileDesc File descriptor.
      * @return true if file exists.
      */
    override def exists(fileDesc: LocalFileDescriptor): Boolean = Files.exists(fileDesc.filepath)

    /**
      * Creates new file in File System.
      *
      * @param currName       File descriptor.
      * @param ignoreExisting if ignoreExisting == true then FS rewrite already created file.
      * @return true if operation was successful.
      */
    override def create(currName: LocalFileDescriptor, ignoreExisting: Boolean): Boolean = {
        doIfIgnoreExisting(currName, ignoreExisting) {
            currName.isDir match {
                case Some(false) => Files.createFile(currName.filepath)
                case Some(true) => Files.createDirectory(currName.filepath)
                case None => throw new IllegalArgumentException("Cannot create file or directory, isDir = None")
            }
        }
    }

    /**
      * Moves file to target destination.
      *
      * @param from           From.
      * @param to             To.
      * @param ignoreExisting if ignoreExisting == true then FS rewrite already created file.
      * @return true if operation was successful.
      */
    override def move(from: LocalFileDescriptor, to: LocalFileDescriptor, ignoreExisting: Boolean): Boolean = {
        doIfIgnoreExisting(to, ignoreExisting) {
            Files.move(from.filepath, to.filepath)
        }
    }

    /**
      * Copy file to target destination.
      *
      * @param from           From.
      * @param to             To.
      * @param ignoreExisting if ignoreExisting == true then FS rewrite already created file.
      * @return true if operation was successful.
      */
    override def copy(from: LocalFileDescriptor, to: LocalFileDescriptor, ignoreExisting: Boolean): Boolean =
        doIfIgnoreExisting(to, ignoreExisting) {
            Files.copy(from.filepath, to.filepath)
        }

    /**
      * Returns list of files for directory.
      * If file is not a directory, then FileIsNotDirectory will be thrown.
      *
      * @param desc Directory path.
      * @return list of files for directory.
      */
    override def getFilesInDirectory(desc: LocalFileDescriptor): List[LocalFileDescriptor] = {
        assert(desc.isDir.isDefined, "type of file should be pointed")
        desc.isDir match {
            case Some(false) => throw new IllegalArgumentException("cannot return files in directory for file")
            case Some(true) => Files.list(desc.filepath).collect(Collectors.toList()).asScala.toList.map(toDesc)
        }
    }

    /**
      * Opens file by descriptor for reading and returns InputStream.
      *
      * @param desc file descriptor.
      * @return stream resource.
      */
    override def read(desc: LocalFileDescriptor): ManagedResource[InputStream] =
        resource.managed(Files.newInputStream(desc.filepath, StandardOpenOption.READ))

    /**
      * Opens file by descriptor for writing and returns InputStream.
      *
      * @param desc file descriptor.
      * @return stream resource.
      */
    override def write(desc: LocalFileDescriptor): ManagedResource[OutputStream] =
        resource.managed(Files.newOutputStream(desc.filepath))

    /** */
    private def doIfIgnoreExisting(fileToCheck: LocalFileDescriptor, ignoreExisting: Boolean)(call: => Unit) =
        if(!ignoreExisting && exists(fileToCheck)) false
        else {
            call
            true
        }

    /** */
    private def toDesc(jpath: Path): LocalFileDescriptor = {
        LocalFileDescriptorParser.parse(jpath.toString).copy(
            isDir = Some(Files.isDirectory(jpath))
        )
    }
}
