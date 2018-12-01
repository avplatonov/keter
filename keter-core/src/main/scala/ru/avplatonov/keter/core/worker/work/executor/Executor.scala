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

import java.io.PrintWriter
import java.nio.file.{Files, Path}
import java.util.UUID
import java.util.stream.Collectors

import org.slf4j.Logger

import scala.collection.JavaConverters._
import scala.io.Source
import scala.util.{Failure, Success, Try}

case class ExecutorResult(stdout: Path, errorLog: Path, otherCreatedFiles: Set[Path])

trait Executor {
    /**
      * Execute script and returns resulting files.
      *
      * @param workdir    working directory for script.
      * @param syslogName name of logging file with execution tracking.
      * @param cmd        script.
      * @return output files.
      */
    def process(workdir: Path, syslogName: String)(cmd: String): Try[ExecutorResult]

    protected def getFilesInWD()(implicit wd: Path): Set[Path] = Files.list(wd).collect(Collectors.toSet()).asScala.toSet

    protected def generateStdOutErrAndSh(cmd: String)(implicit logger: Logger, workdir: Path): Try[(Path, Path, Path)] = {
        createScriptFile(cmd)(logger, workdir) map { sh =>
            val errorLog = workdir.resolve(UUID.randomUUID().toString + ".error.log")
            val stdout = workdir.resolve(UUID.randomUUID().toString + ".out")
            (stdout, errorLog, sh)
        }
    }

    private def createScriptFile(cmd: String)(implicit logger: Logger, workdir: Path): Try[Path] = {
        val scriptFile = workdir.resolve(UUID.randomUUID().toString + ".sh")
        resource.managed(new PrintWriter(scriptFile.toFile)).acquireAndGet(out => Try {
            logger.info(s"Create script file [name = ${scriptFile.toAbsolutePath}]")
            out.print(cmd)
            scriptFile
        })
    }
}

case class NonZeroStatusCode(status: Int, errorLog: Path) extends RuntimeException

case class BashExecutor(loggerFactory: Path => Logger)(val env: Array[String] = Array("PATH=/bin:/usr/bin"))
    extends Executor {

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
            case (stdout, stderr, sh) =>
                val envFiles = getFilesInWD() + stderr + stdout
                logger.info(s"Environment [${envFiles.map(_.getFileName.toString).mkString(",")}]")

                run(sh, stdout, stderr)
                    .flatMap(awaiting)
                    .flatMap(processStatus(_, stdout, stderr, envFiles + sh)) match {

                    case res: Success[ExecutorResult] => res
                    case error: Failure[ExecutorResult] =>
                        logger.error("Error during command processing", error.exception)
                        error
                }
        }
    }

    private def run(scriptFile: Path, stdout: Path, errorLog: Path)(implicit logger: Logger, workdir: Path) = Try {
        val builder = new ProcessBuilder("/bin/bash", scriptFile.getFileName.toString)
            .redirectError(errorLog.toFile)
            .redirectOutput(stdout.toFile)
            .directory(workdir.toFile)
        builder.environment().put("PATH", "/bin:/usr/bin")

        logger.info(s"Start cmd ['/bin/bash ${scriptFile.getFileName.toString}']")
        builder.start()
    }

    private def awaiting(process: Process)(implicit logger: Logger) = Try({
        logger.info(s"Wait cmd")
        process.waitFor()
    })

    private def processStatus(status: Int, stdout: Path, errorLog: Path, prevFiles: Set[Path])
        (implicit logger: Logger, workdir: Path) = {

        Try(status match {
            case 0 =>
                logger.info(s"Cmd was complete successfully")
                val newFiles = getFilesInWD()(workdir) -- prevFiles
                logger.info(s"New files [${newFiles.map(_.getFileName.toString).mkString(",")}]")
                ExecutorResult(stdout, errorLog, newFiles)
            case code =>
                logger.warn("Non-zero status code")
                Source.fromFile(errorLog.toFile).getLines().foreach(logger.warn)
                throw NonZeroStatusCode(code, errorLog)
        })
    }
}
