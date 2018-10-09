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

package ru.avplatonov.keter.core.storage.remote.index

import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DriverManagerDataSource
import ru.avplatonov.keter.core.discovery.NodeId
import ru.avplatonov.keter.core.storage.FileDescriptor

case class RowKey(path: String, nodeId: NodeId)

case class FilesIndexRow(fileDescriptor: FileDescriptor, replicas: Set[NodeId])

object FilesDB {
    case class Settings(
        connectionString: String,
        user: String,
        passwd: String,
        driver: String,
        indexTableName: String = "files_index"
    )
}

trait FilesDB {
    def find(path: String): Option[FilesIndexRow]

    def insert(key: String, filesIndexRow: FilesIndexRow): Unit

    def insert(localFiles: Stream[(String, FilesIndexRow)]): Unit

    def delete(rowKey: RowKey): Unit

    def deleteAllFor(nodeId: NodeId): Unit
}

case class FilesSqlDB(driverName: String, connString: String, user: String, passwd: String) extends FilesDB {
    private val logger = LoggerFactory.getLogger(getClass)

    private val jdbc: JdbcTemplate = new JdbcTemplate({
        val driver = new DriverManagerDataSource(connString, user, passwd)
        driver.setDriverClassName(driverName)
        driver
    }, true)

    override def find(path: String): Option[FilesIndexRow] = ???

    override def insert(key: String, filesIndexRow: FilesIndexRow): Unit = ???

    override def insert(localFiles: Stream[(String, FilesIndexRow)]): Unit = ???

    override def delete(rowKey: RowKey): Unit = ???

    override def deleteAllFor(nodeId: NodeId): Unit = ???
}
