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

package ru.avplatonov.keter.core.worker.work.executor

import java.nio.file.Path
import java.util.concurrent.atomic.AtomicReference

import com.spotify.docker.client.DockerClient
import com.spotify.docker.client.messages.{ContainerConfig, ContainerCreation, HostConfig}
import org.slf4j.Logger

import scala.util.Try

class DockerExecutor(loggerFactory: Path => Logger)(client: DockerClient, imageName: String) extends Executor {
    val creationDescrRef: AtomicReference[ContainerCreation] = new AtomicReference[ContainerCreation]()

    /**
      * Execute script and returns resulting files.
      *
      * @param workdir    working directory for script.
      * @param syslogName name of logging file with execution tracking.
      * @param cmd        script.
      * @return output files.
      */
    override def process(workdir: Path, syslogName: String)(cmd: String): Try[ExecutorResult] = {
        implicit val wd = workdir
        implicit val logger = loggerFactory(workdir.resolve(syslogName))

        logger.info(s"Start processing script.")
        logger.info(cmd)

        generateStdOutErrAndSh(cmd) flatMap {
            case (stdout, stderr, sh) => Try {
                val envFiles = getFilesInWD() + stderr + stdout
                logger.info(s"Environment [${envFiles.map(_.getFileName.toString).mkString(",")}]")
                val wdBind = HostConfig.Bind.builder().from(workdir.toString).to(".").build()
                val hostConfig = HostConfig.builder().binds(wdBind).build()

                val containerConfig = ContainerConfig.builder()
                    .hostConfig(hostConfig)
                    .image(imageName)
                    .cmd("bash", sh.getFileName.toString,
                        ">", stdout.getFileName.toString,
                        "2>", stderr.getFileName.toString)
                    .build()
                val creation = client.createContainer(containerConfig)
                creationDescrRef.set(creation)
                client.startContainer(creation.id())
                val result = client.waitContainer(creation.id())

                Int.unbox(result.statusCode()) match {
                    case 0 => ExecutorResult(stdout, stderr, getFilesInWD() -- envFiles)
                    case code => throw NonZeroStatusCode(code, stderr)
                }
            }
        }
    }

    def status(): String = inspectContainer()
        .map(_.state().status())
        .getOrElse("unknown")

    def exitCode(docker: DockerClient): Int = inspectContainer().map(_.state().exitCode()) match {
        case None => 0
        case Some(value) => value
    }

    private def inspectContainer() =
        creationDescr.map(_.id()).map(client.inspectContainer)

    private def creationDescr = creationDescrRef.get() match {
        case null => None
        case value => Some(value)
    }
}
