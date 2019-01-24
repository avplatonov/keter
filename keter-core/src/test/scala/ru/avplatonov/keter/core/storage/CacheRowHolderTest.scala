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

import java.nio.file.Paths

import org.scalatest.{FlatSpec, Matchers}
import ru.avplatonov.keter.core.storage.legacy.local.{CacheRowHolder, LocalFileDescriptor, LocalFileDescriptorParser, LocalTemporaryFilesStorage}

import scala.collection.mutable

class CacheRowHolderTest extends FlatSpec with Matchers {
    val tempFS = new LocalTemporaryFilesStorage(LocalTemporaryFilesStorage.Settings(Paths.get("/tmp")))

    "holder" should "release all sub-holders after flatten" in {
        withTmp("/tmp/f1") { f1 =>
            withTmp("/tmp/f2") { f2 =>
                val h1 = tempFS.put(f1.filepath, f1)
                val h2 = tempFS.put(f2.filepath, f2)
                val names = mutable.Set[String]()

                CacheRowHolder.flatten(List(h1, h2)).foreach(fs => {
                    fs.foreach(p => {
                        p._1.toFile.exists() should be(true)
                        names.add(p._1.getFileName.toString)
                    })
                })

                f1.filepath.toFile.exists() should be(false)
                f2.filepath.toFile.exists() should be(false)
                names should be(Set("f1", "f2"))
            }
        }
    }

    private def withTmp(path: String)(f: LocalFileDescriptor => Unit): Unit = {
        val file = LocalFileDescriptorParser.parse(path)
        file.filepath.toFile.createNewFile()
        f(file)
    }
}
