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

package ru.avplatonov.keter.core.worker.work

import java.nio.file.Path
import java.util.UUID

import org.slf4j.LoggerFactory
import ru.avplatonov.keter.core.storage.{FileDescriptor, FileStorage}
import ru.avplatonov.keter.core.worker.work.executor.{Executor, ExecutorResult}
import ru.avplatonov.keter.core.worker.work.script.ScriptTemplate
import ru.avplatonov.keter.core.worker.{LocalResourceDescriptors, ParameterDescriptors}

import scala.util.Try

case class WorkEnvironment(
    parameters: ParameterDescriptors,
    workdir: Path,
    input: List[FileDescriptor],
    output: List[FileDescriptor]
)

case class LocalSession(environment: WorkEnvironment, localWD: Path, localRes: LocalResourceDescriptors, cmd: String)

trait WorkResult

case class Worker(executor: Executor, fileStorage: FileStorage[FileDescriptor]) {
    val logger = LoggerFactory.getLogger("worker")

    def apply(environment: WorkEnvironment, script: String): WorkResult = {
        createLocalSession(environment)
            .flatMap(prepareWD)
            .map(ses => ses.copy(cmd = script))
            .flatMap(prepareScript)
            .flatMap(ses => executor.process(ses.localWD, null)(ses.cmd))
            .flatMap(processWorkingResult)
            .get
    }

    def createLocalSession(environment: WorkEnvironment): Try[LocalSession] = Try {
        val abcentFiles = environment.input.filterNot(fileStorage.exists).map(_.key)
        if(abcentFiles.nonEmpty)
            throw new IllegalArgumentException(s"These input files doesn't exists: [${abcentFiles.mkString(",")}]")

        val alreadyExistsFiles = environment.output.filter(fileStorage.exists)
        if(alreadyExistsFiles.nonEmpty)
            throw new IllegalArgumentException(s"These output files already exists: [${alreadyExistsFiles.mkString(",")}]")

        val localWD = environment.workdir.resolve(UUID.randomUUID().toString)
        logger.info(s"Current working directory: ${localWD.toAbsolutePath.toString}")
        LocalSession(environment, localWD, null, "")
    }

    def prepareWD(session: LocalSession): Try[LocalSession] = {
        Try(session)
    }

    def prepareScript(session: LocalSession): Try[LocalSession] = Try(session.copy(cmd =
        ScriptTemplate(session.cmd).toCommand(session.environment.parameters, session.localRes)
    ))

    def processWorkingResult(exRes: ExecutorResult): Try[WorkResult] = Try {
        null
    }
}
