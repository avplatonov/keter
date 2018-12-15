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

package ru.avplatonov.keter.core.worker.work.script

import ru.avplatonov.keter.core.worker._

case class ScriptTemplate(body: String) {
    private val parametersRegex = "\\$\\{PARAM\\.(.*?)\\}".r
    private val filesRegex = "\\$\\{(?:(?:IN)|(?:OUT))\\.(.*?)\\}".r

    def toCommand(parameters: ParameterDescriptors, resDesc: LocalResourceDescriptors): String = {
        val withParams: String => String = pasteValues(parameters)
        val withFiles: String => String = pasteFilePaths(resDesc)
        (withParams andThen withFiles)(body)
    }

    private def pasteValues(parameters: ParameterDescriptors)(body: String): String =
        checkMissingParameters(
            parameters.values.foldLeft(body)({
                case (script, (paramName, desc)) =>
                    script.replaceAllLiterally("$" + s"{PARAM.$paramName}", toString(desc))
            })
        )

    private def checkMissingParameters(body: String): String = {
        val missingParameters = parametersRegex.findAllMatchIn(body).map(_.group(1)).toList
        if (missingParameters.nonEmpty)
            throw MissingParametersException(missingParameters)
        body
    }

    private def pasteFilePaths(resDesc: LocalResourceDescriptors)(body: String): String = checkMissingFiles(
        resDesc.values.foldLeft(body)({
            case (script, (filename, (path, fileType))) =>
                script.replaceAllLiterally("$" + s"{$fileType.$filename}", s"'${path.toAbsolutePath}'")
        })
    )

    private def checkMissingFiles(body: String): String = {
        val missingParameters = filesRegex.findAllMatchIn(body).map(_.group(1)).toList
        if (missingParameters.nonEmpty)
            throw MissingFilesException(missingParameters)
        body
    }

    private def toString(desc: ParameterDescriptor): String = desc.`type` match {
        case ParameterType.INT =>
            assert(desc.value.isInstanceOf[Int], "INT parameter should have integer value")
            "%d".format(desc.value.asInstanceOf[Int])
        case ParameterType.DOUBLE =>
            assert(desc.value.isInstanceOf[Double], "DOUBLE parameter should have double value")
            "%.2f".format(desc.value.asInstanceOf[Double])
        case ParameterType.STRING =>
            assert(desc.value.isInstanceOf[String], "STRING parameter should have string value")
            s"'${desc.value.asInstanceOf[String]}'"
    }
}
