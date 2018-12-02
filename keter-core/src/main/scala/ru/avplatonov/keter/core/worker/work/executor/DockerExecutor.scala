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

import java.io.FileOutputStream
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicReference

import com.spotify.docker.client.DockerClient
import com.spotify.docker.client.messages.{ContainerConfig, ContainerCreation, HostConfig}
import org.slf4j.Logger

import scala.util.Try

/**
  * Before using such executor make sure that you configured Docker like this:
  *
  * 1) Create a file at /etc/systemd/system/docker.service.d/startup_options.conf with the below contents:
  * # /etc/systemd/system/docker.service.d/override.conf
  * [Service]
  * ExecStart=
  * ExecStart=/usr/bin/dockerd -H fd:// -H tcp://0.0.0.0:2375
  *
  * 2) sudo systemctl daemon-reload
  * 3) sudo systemctl restart docker.service
  * 4) Check that Docker open socket:
  *     nmap -p 2375 localhost
  *
  * And use docker URL like this: http://localhost:2375.
  *
  * @param loggerFactory
  * @param client
  * @param imageName
  */
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
                val dockerWorkdir = "/workdir"

                val wdBind = HostConfig.Bind.builder().from(workdir.toString).to(dockerWorkdir).build()
                val hostConfig = HostConfig.builder().binds(wdBind).build()

                val containerConfig = ContainerConfig.builder()
                    .hostConfig(hostConfig)
                    .image(imageName)
                    .workingDir(dockerWorkdir)
                    .cmd("sh", "-c", "while :; do sleep 1; done")
                    .build()

                val creation = client.createContainer(containerConfig)
                creationDescrRef.set(creation)
                client.startContainer(creation.id())
                try {
                    val execCreation = client.execCreate(
                        creation.id(),
                        Array("bash", s"$dockerWorkdir/${sh.getFileName.toString}"),
                        DockerClient.ExecCreateParam.attachStdout(),
                        DockerClient.ExecCreateParam.attachStderr()
                    )

                    resource.managed(client.execStart(execCreation.id())).foreach(log => {
                        resource.managed(new FileOutputStream(stdout.toFile)).foreach(stdout => {
                            resource.managed(new FileOutputStream(stderr.toFile)).foreach(stderr => {
                                log.attach(stdout, stderr)
                            })
                        })
                    })

                    val state = client.execInspect(execCreation.id())
                    Int.unbox(state.exitCode()) match {
                        case 0 => ExecutorResult(stdout, stderr, getFilesInWD() -- envFiles)
                        case code => throw NonZeroStatusCode(code, stderr)
                    }
                } finally {
                    client.killContainer(creation.id())
                    client.removeContainer(creation.id())
                }
            }
        }
    }
}
