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

import org.apache.curator.RetryPolicy
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.zookeeper.CreateMode
import resource.ManagedResource
import ru.avplatonov.keter.core.storage.{FileDescriptor, FileStorage, PathScheme}

object ZKFilesystem {
    case class Settings(
        currentAddress: String,
        connectionString: String,
        retryPolicy: RetryPolicy
    )
}

case class DistributedFileDescriptor(path: List[String], key: String, isDir: Option[Boolean]) extends FileDescriptor {
    /** Type of file system. */
    override val scheme: PathScheme = PathScheme.zookeeper
}

case class ZKFilesystem(settings: ZKFilesystem.Settings) extends FileStorage[DistributedFileDescriptor] {
    val client = CuratorFrameworkFactory.newClient(settings.connectionString, settings.retryPolicy)

    def start() = client.start()

    def stop() = client.close()

    /** */
    override def exists(fileDesc: DistributedFileDescriptor): Boolean = client.checkExists()
        .forPath(mkPath(fileDesc)) != null

    /** */
    override def create(currName: DistributedFileDescriptor, ignoreExisting: Boolean): Boolean = {
        val path = mkPathWithMachine(currName)
        client.create().creatingParentContainersIfNeeded()
            .withMode(CreateMode.PERSISTENT)
            .forPath(path)
            .equals(path)
    }

    /** */
    override def move(from: DistributedFileDescriptor, to: DistributedFileDescriptor, ignoreExisting: Boolean): Boolean = ???

    /** */
    override def copy(from: DistributedFileDescriptor, to: DistributedFileDescriptor, ignoreExisting: Boolean): Boolean = ???

    /** */
    override def delete(desc: DistributedFileDescriptor): Boolean = ???

    /** */
    override def getFilesInDirectory(desc: DistributedFileDescriptor): List[DistributedFileDescriptor] = ???

    /** */
    override def read(desc: DistributedFileDescriptor): ManagedResource[InputStream] = ???

    /** */
    override def write(desc: DistributedFileDescriptor): ManagedResource[OutputStream] = ???

    private def mkPath(fileDesc: DistributedFileDescriptor) = {
        fileDesc.path.mkString("/", "/", "/") + fileDesc.key
    }

    private def mkPathWithMachine(fileDesc: DistributedFileDescriptor) = {
        fileDesc.path.mkString("/", "/", "/") + fileDesc.key + "/" + settings.currentAddress
    }
}
