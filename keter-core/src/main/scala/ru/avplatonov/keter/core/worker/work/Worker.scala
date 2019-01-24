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
import ru.avplatonov.keter.core.storage.legacy.local.LocalFileDescriptor
import ru.avplatonov.keter.core.storage.legacy.{FileDescriptor, FileStorage}
import ru.avplatonov.keter.core.worker.work.executor.{Executor, ExecutorResult}
import ru.avplatonov.keter.core.worker.work.script.ScriptTemplate
import ru.avplatonov.keter.core.worker.{LocalResourceDescriptors, ParameterDescriptors, ResourceType}

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
        initLocalSession(environment)
            .map(ses => ses.copy(cmd = script))
            .flatMap(ses => prepareScript(ses).map(cmd => ses.copy(cmd = cmd)))
            .flatMap(ses => initWorkingDir(ses).map(res => ses.copy(localRes = res)))
            .flatMap(ses => executor.process(ses.localWD, null)(ses.cmd))
            .flatMap(processWorkingResult)
            .get
    }

    def initLocalSession(environment: WorkEnvironment): Try[LocalSession] = Try {
        val abcentFiles = environment.input.filterNot(fileStorage.exists).map(_.key)
        if (abcentFiles.nonEmpty)
            throw new IllegalArgumentException(s"These input files doesn't exists: [${abcentFiles.mkString(",")}]")

        val alreadyExistsFiles = environment.output.filter(fileStorage.exists)
        if (alreadyExistsFiles.nonEmpty)
            throw new IllegalArgumentException(s"These output files already exists: [${alreadyExistsFiles.mkString(",")}]")

        val localWD = environment.workdir.resolve(UUID.randomUUID().toString)
        logger.info(s"Current working directory: ${localWD.toAbsolutePath.toString}")
        LocalSession(environment, localWD, null, "")
    }

    def initWorkingDir(session: LocalSession): Try[LocalResourceDescriptors] = Try {
        val keyToDesc: Map[String, FileDescriptor] = session.environment.input.map(desc => desc.key -> desc).toMap

        val inputFiles = session.environment.input
            .map(desc => descToLocalFile(session.localWD, desc, ResourceType.IN))
            .toMap

        val ouputFiles = session.environment.output
            .map(desc => descToLocalFile(session.localWD, desc, ResourceType.OUT))
            .toMap

        inputFiles.foreach({
            case (key, (localPath, _)) =>
                val remoteDesc = keyToDesc(key)
                val localDesc = LocalFileDescriptor(localPath, remoteDesc.path, remoteDesc.key, None)
                val localDir = localPath.getParent
                localDir.toFile.mkdirs()
                fileStorage.localCopy(remoteDesc, localDesc)
        })

        LocalResourceDescriptors(inputFiles ++ ouputFiles)
    }

    def descToLocalFile(localWD: Path, desc: FileDescriptor, `type`: ResourceType.Value): (String, (Path, ResourceType.Value)) = {
        val localPath = desc.path.foldLeft(localWD)((acc, key) => acc.resolve(key)).resolve(desc.key)
        val localDir = localPath.getParent
        (desc.key, (localPath, `type`))
    }

    def prepareScript(session: LocalSession): Try[String] = Try {
        ScriptTemplate(session.cmd).toCommand(session.environment.parameters, session.localRes)
    }

    def processWorkingResult(exRes: ExecutorResult): Try[WorkResult] = Try {
        null
    }
}
