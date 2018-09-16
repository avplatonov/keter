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

package ru.avplatonov.keter.core.storage.remote.cassandra

import com.datastax.driver.core.{Cluster, Session}
import ru.avplatonov.keter.core.storage.FileDescriptor
import ru.avplatonov.keter.core.storage.remote.{KVStorage, RemoteFile}

object CassandraKVStorage {
    case class Settings(contactPoints: Seq[String], keyspace: String, tableName: String)
}

case class CassandraKVStorage(settings: CassandraKVStorage.Settings, sessionsPool: String => Session)
    extends KVStorage[FileDescriptor, RemoteFile] {

    val cluster = {
        val clusterBuilder = Cluster.builder()
        settings.contactPoints.foreach(clusterBuilder.addContactPoint)

        clusterBuilder.build()
    }

    /** */
    override def put(key: FileDescriptor, value: RemoteFile): Unit = ???

    /** */
    override def get(key: FileDescriptor): RemoteFile = ???

    /** */
    override def remove(key: FileDescriptor): Boolean = ???

    /** */
    override def contains(key: FileDescriptor): Boolean = ???
}
