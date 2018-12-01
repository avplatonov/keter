package ru.avplatonov.keter.core.worker.work

import java.nio.file.Paths

import org.scalatest.{FlatSpec, Matchers}
import ru.avplatonov.keter.core.worker._
import ru.avplatonov.keter.core.worker.work.script._

class ScriptTemplateTest extends FlatSpec with Matchers {
    behavior of "ScriptTemplate"

    implicit def toParameters(values: Map[String, ParameterDescriptor]): ParameterDescriptors =
        ParameterDescriptors(values)

    it must "return empty string" in {
        ScriptTemplate("").toCommand(Map[String, ParameterDescriptor](), ResourcesDescriptor(Map.empty)) should equal("")
    }

    it must "return same string" in {
        ScriptTemplate("boo").toCommand(Map[String, ParameterDescriptor](), ResourcesDescriptor(Map.empty)) should equal("boo")
    }

    it must "return int pasted value from parameter" in {
        ScriptTemplate("${PARAM.SOME_PARAM}")
            .toCommand(
                Map("SOME_PARAM" -> ParameterDescriptor(42, ParameterType.INT)),
                ResourcesDescriptor(Map.empty)
            ) should equal("42")
    }

    it must "return double pasted value from parameter" in {
        ScriptTemplate("${PARAM.SOME_PARAM}")
            .toCommand(
                Map("SOME_PARAM" -> ParameterDescriptor(42, ParameterType.INT)),
                ResourcesDescriptor(Map.empty)
            ) should equal("42")
    }

    it must "return double pasted value from parameter with rounding" in {
        ScriptTemplate("${PARAM.SOME_PARAM}")
            .toCommand(
                Map("SOME_PARAM" -> ParameterDescriptor(4.2000001, ParameterType.DOUBLE)),
                ResourcesDescriptor(Map.empty)
            ) should equal("4.20")
    }

    it must "return string pasted value from parameter" in {
        ScriptTemplate("${PARAM.SOME_PARAM}")
            .toCommand(
                Map("SOME_PARAM" -> ParameterDescriptor("42", ParameterType.STRING)),
                ResourcesDescriptor(Map.empty)
            ) should equal("'42'")
    }

    it must "return pasted path value from IN-files" in {
        ScriptTemplate("${IN.SOME_FILE}")
            .toCommand(
                Map[String, ParameterDescriptor](),
                ResourcesDescriptor(Map("SOME_FILE" -> (Paths.get("/tmp"), ResourceType.IN)))
            ) should equal("'/tmp'")
    }

    it must "return pasted path value from OUT-files" in {
        ScriptTemplate("${OUT.SOME_FILE}")
            .toCommand(
                Map[String, ParameterDescriptor](),
                ResourcesDescriptor(Map("SOME_FILE" -> (Paths.get("/tmp"), ResourceType.OUT)))
            ) should equal("'/tmp'")
    }

    it must "work with several parameters" in {
        ScriptTemplate(
            "p1=${PARAM.SOME_PARAM_1} p2=${PARAM.SOME_PARAM_2} p3=${PARAM.SOME_PARAM_3}"
        ).toCommand(
            Map(
                "SOME_PARAM_1" -> ParameterDescriptor(42, ParameterType.INT),
                "SOME_PARAM_2" -> ParameterDescriptor(4.2, ParameterType.DOUBLE),
                "SOME_PARAM_3" -> ParameterDescriptor("some string", ParameterType.STRING)
            ),
            ResourcesDescriptor(Map.empty)
        ) should equal("p1=42 p2=4.20 p3='some string'")
    }

    it must "work with several files" in {
        ScriptTemplate("in=${IN.IN_FILE} out=${OUT.OUT_FILE}")
            .toCommand(
                Map[String, ParameterDescriptor](),
                ResourcesDescriptor(Map(
                    "IN_FILE" -> (Paths.get("/in_dir/in"), ResourceType.IN),
                    "OUT_FILE" -> (Paths.get("/out_dir/out"), ResourceType.OUT)
                ))
            ) should equal("in='/in_dir/in' out='/out_dir/out'")
    }

    it must "work with several files and parameters" in {
        ScriptTemplate("in=${IN.IN_FILE} out=${OUT.OUT_FILE} log=${OUT.LOG_FILE} " +
            "param1=${PARAM.PARAM_1} param2=${PARAM.PARAM_2} param3=${PARAM.PARAM_3} param4=${PARAM.PARAM_4}")
            .toCommand(
                Map(
                    "PARAM_1" -> ParameterDescriptor(42, ParameterType.INT),
                    "PARAM_2" -> ParameterDescriptor(4.2, ParameterType.DOUBLE),
                    "PARAM_3" -> ParameterDescriptor("42", ParameterType.STRING),
                    "PARAM_4" -> ParameterDescriptor("fuck you", ParameterType.STRING)
                ),
                ResourcesDescriptor(Map(
                    "IN_FILE" -> (Paths.get("/in_dir/in"), ResourceType.IN),
                    "OUT_FILE" -> (Paths.get("/out_dir/out"), ResourceType.OUT),
                    "LOG_FILE" -> (Paths.get("/out_dir/log"), ResourceType.OUT)
                ))
            ) should equal("in='/in_dir/in' out='/out_dir/out' log='/out_dir/log' param1=42 param2=4.20 param3='42' param4='fuck you'")
    }

    it must "with duplicates in parameters in script" in {
        ScriptTemplate(
            "p1=${PARAM.42_PARAM} p2=${PARAM.42_PARAM}"
        ).toCommand(
            Map("42_PARAM" -> ParameterDescriptor(42, ParameterType.INT)),
            ResourcesDescriptor(Map.empty)
        ) should equal("p1=42 p2=42")
    }

    it must "work with duplicates in files" in {
        ScriptTemplate("out_1=${OUT.FILE} out_2=${OUT.FILE}")
            .toCommand(
                Map[String, ParameterDescriptor](),
                ResourcesDescriptor(Map(
                    "FILE" -> (Paths.get("/out_dir/out"), ResourceType.OUT)
                ))
            ) should equal("out_1='/out_dir/out' out_2='/out_dir/out'")
    }

    it must "throws exception if there are no several parameters" in {
        intercept[MissingParametersException] {
            ScriptTemplate(
                "p1=${PARAM.42_PARAM} p2=${PARAM.MISSING_PARAMETER}"
            ).toCommand(
                Map("42_PARAM" -> ParameterDescriptor(42, ParameterType.INT)),
                ResourcesDescriptor(Map.empty)
            )
        }.missingParams.toSet should equal(Set("MISSING_PARAMETER"))
    }

    it must "throws exception if there are no several files" in {
        intercept[MissingFilesException] {
            ScriptTemplate("f1=${IN.FILE_1} f2=${OUT.MISSING_FILE}")
                .toCommand(
                    Map[String, ParameterDescriptor](),
                    ResourcesDescriptor(Map.empty)
                )
        }.missingFiles.toSet should equal(Set("FILE_1", "MISSING_FILE"))
    }
}
