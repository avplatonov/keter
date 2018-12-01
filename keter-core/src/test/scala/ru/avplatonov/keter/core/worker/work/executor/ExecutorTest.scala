package ru.avplatonov.keter.core.worker.work.executor

import java.nio.file.{Files, Path, Paths}
import java.util.UUID

import org.apache.commons.io.FileUtils
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import org.slf4j.{Logger, LoggerFactory}

import scala.io.Source

class ExecutorTest extends FlatSpec with Matchers with BeforeAndAfterAll {
    behavior of "Executor"

    val testWDRoot = Paths.get("/tmp/executor-tests/")
    val executor = getTestExecutor()
    val logger = getLogger()

    it must "save echo result from std as file" in {
        val content = "hello world"
        val resultT = executor.process(wd, "")("echo '" + content + "'")

        resultT.isSuccess should equal(true)
        val result = resultT.get

        result match {
            case ExecutorResult(stdout, errorLog, otherCreatedFiles) =>
                Source.fromFile(stdout.toFile).mkString should equal(content)
                Source.fromFile(errorLog.toFile).mkString should equal("")
                otherCreatedFiles.size should equal(0)
        }
    }

    override protected def beforeAll(): Unit = {
        if (Files.exists(testWDRoot))
            FileUtils.deleteDirectory(testWDRoot.toFile)
    }

    protected def getTestExecutor(): Executor = BashExecutor(_ => getLogger())()

    protected def wd: Path = {
        val localWD = testWDRoot.resolve(UUID.randomUUID().toString)
        Files.createDirectories(localWD)
        logger.info(s"Local WD: $localWD")
        localWD
    }

    private def getLogger(): Logger = LoggerFactory.getLogger(classOf[Executor])
}
