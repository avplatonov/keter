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

import java.io._
import java.nio.file.{Files, Path}

import org.slf4j.Logger

import scala.util.{Failure, Success, Try}

trait LockObject

case class WithoutLock() extends LockObject

trait FSResource extends AutoCloseable {
    def asInputStream(): InputStream

    def asOutputStream(): OutputStream
}

case class LocalFSResource(file: File) extends FSResource {
    override def asInputStream(): InputStream = Files.newInputStream(file.toPath)

    override def asOutputStream(): OutputStream = Files.newOutputStream(file.toPath)

    override def close(): Unit =
        if (file.exists())
            file.delete()
}

trait KVBasedFS {
    def set(key: Path, value: FSResource, lock: LockObject = WithoutLock())

    def get(key: Path): Option[FSResource]

    def contains(key: Path): Boolean

    def sync[T](key: Path)(f: LockObject => T): T = {
        try {
            lock(key)
            f()
        }
        finally {
            unlock(key)
        }
    }

    protected def lock(key: Path): Boolean

    protected def unlock(key: Path): Boolean

}

case class FSMeta(path: Path, isDirectory: Boolean, children: List[Path])

trait FileSystem {
    private val META_FILE_NAME = "meta"

    def exists(path: Path) = kv.contains(path)

    def write(path: Path, file: FSResource): Boolean = {
        if(path.getFileName.toString.equals(META_FILE_NAME))
            return false

        writeWithParentDirCheck(path, false) { (parMeta, plo, lo) =>
            resource.managed(writeMetaToTmpFile(parMeta.copy(children = path :: parMeta.children))).acquireAndGet(parRes => {
                resource.managed(writeMetaToTmpFile(FSMeta(path, isDirectory = false, Nil))).acquireAndGet(res => {
                    kv.set(path.getParent, parRes, plo) //directory path == metaPath
                    kv.set(path, file, lo)
                    kv.set(meta(path), res, lo)
                })
            })

            true
        }
    }

    def read(path: Path): Option[FSResource] = {
        if (isDirectory(path))
            None
        else kv.sync(path) { _ =>
            kv.get(path)
        }
    }

    def createDir(path: Path): Boolean = {
        if(path.getFileName.toString.equals(META_FILE_NAME))
            return false

        writeWithParentDirCheck(path, false) { (parMeta, plo, lo) =>
            resource.managed(writeMetaToTmpFile(parMeta.copy(children = path :: parMeta.children))).acquireAndGet(parRes => {
                resource.managed(writeMetaToTmpFile(FSMeta(path, isDirectory = true, Nil))).acquireAndGet(res => {
                    kv.set(path.getParent, parRes, plo) //directory path == metaPath
                    kv.set(path, res, lo)
                })
            })

            true
        }
    }

    def isDirectory(path: Path): Boolean = exists(path) && !exists(meta(path))

    protected def kv: KVBasedFS

    protected def log: Logger

    private def metaFor(path: Path): Option[FSMeta] = {
        val keyForMeta = path.resolve(META_FILE_NAME)
        if (kv.contains(keyForMeta)) {
            kv.get(keyForMeta).flatMap(readMeta)
        }
        else {
            None
        }
    }

    private def meta(path: Path) = path.resolve(META_FILE_NAME)

    private def readMeta(res: FSResource): Option[FSMeta] = Try {
        resource.managed(new ObjectInputStream(res.asInputStream())).acquireAndGet(ois => {
            ois.readObject().asInstanceOf[FSMeta]
        })
    } match {
        case Success(meta) => Some(meta)
        case Failure(e) =>
            log.error("Error while reading meta file", e)
            None
    }

    private def writeMetaToTmpFile(meta: FSMeta): FSResource = Try {
        val res = LocalFSResource(Files.createTempFile("meta-", ".tmp").toFile)
        resource.managed(res.asOutputStream())
            .flatMap(os => resource.managed(new ObjectOutputStream(os)))
            .acquireAndGet(os => os.writeObject(meta))
        res
    } get

    private def writeWithParentDirCheck[T](path: Path, default: T)(f: (FSMeta, LockObject, LockObject) => T): T = {
        val parent = path.getParent

        kv.sync(parent) { plo =>
            ifDirectoryExists(parent) { parMeta =>
                kv.sync(path) { lo =>
                    metaFor(path) match {
                        case None =>
                            f(parMeta, plo, lo)
                        case _ => default
                    }
                }
            } getOrElse default
        }
    }

    private def ifDirectoryExists[T](path: Path)(f: FSMeta => T): Option[T] = metaFor(path) match {
        case Some(meta) if meta.isDirectory =>
            Some(f(FSMeta))
        case _ =>
            None
    }
}
