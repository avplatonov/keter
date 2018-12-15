 package ru.avplatonov.keter.core.worker.work.executor

import java.io.PrintWriter
import java.nio.file.{Files, Path, Paths}
import java.util.UUID
import java.util.stream.Collectors

import org.apache.commons.io.FileUtils
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._
import scala.io.Source

abstract class ExecutorTest extends FlatSpec with Matchers with BeforeAndAfterAll {
    behavior of "Executor"

    val testWDRoot = Paths.get("/tmp/executor-tests/")
    val logger = getLogger()

    it must "save echo result from std as file" in {
        val content = "hello world"
        val resultT = getTestExecutor().process(wd, "")("echo '" + content + "'")

        if(resultT.isFailure) resultT.get
        resultT.isSuccess should equal(true)
        val result = resultT.get

        result match {
            case ExecutorResult(stdout, errorLog, otherCreatedFiles) =>
                Source.fromFile(stdout.toFile).mkString should equal(content + "\n")
                Source.fromFile(errorLog.toFile).mkString should equal("")
                otherCreatedFiles should equal(Set.empty)
        }
    }

    it must "initialize system files" in {
        val content = "hello world"
        val command = "echo '" + content + "'"
        val workdir = wd
        val resultT = getTestExecutor().process(workdir, "")(command)

        if(resultT.isFailure) resultT.get
        resultT.isSuccess should equal(true)
        val result = resultT.get

        val envFiles = Files.list(workdir).collect(Collectors.toSet()).asScala -- result.otherCreatedFiles
        envFiles.exists(_.toString.endsWith(".error.log")) should equal(true)
        envFiles.exists(_.toString.endsWith(".out")) should equal(true)
        envFiles.exists(_.toString.endsWith(".sh")) should equal(true)

        envFiles.find(_.endsWith(".sh")).map(p => Source.fromFile(p.toFile).mkString)
            .foreach(cmd => cmd should equal(command))
    }

    it must "create new files if need" in {
        val content = "hello world"
        val command = "echo '" + content + "' > content.txt"
        val workdir = wd
        val resultT = getTestExecutor().process(workdir, "")(command)

        if(resultT.isFailure) resultT.get
        resultT.isSuccess should equal(true)
        val result = resultT.get
        val contentPath = workdir.resolve("content.txt")

        Files.exists(contentPath) should equal(true)
        result.otherCreatedFiles.contains(contentPath) should equal(true)
        Source.fromFile(contentPath.toFile).mkString should equal(content + "\n")
        Source.fromFile(result.stdout.toFile).mkString should equal("")
    }

    it must "run bash in bash" in {
        val content = "hello world"
        val command = "bash -c 'echo \"" + content + "\" > content.txt'"
        val workdir = wd
        val resultT = getTestExecutor().process(workdir, "")(command)

        if(resultT.isFailure) resultT.get
        resultT.isSuccess should equal(true)
        val result = resultT.get
        val contentPath = workdir.resolve("content.txt")

        Files.exists(contentPath) should equal(true)
        result.otherCreatedFiles.contains(contentPath) should equal(true)
        Source.fromFile(contentPath.toFile).mkString should equal(content + "\n")
        Source.fromFile(result.stdout.toFile).mkString should equal("")
    }

    it must "return non-zero exception" in {
        intercept[NonZeroStatusCode] {
            val content = "hello world"
            val command = "bash -c 'cat not-found-file'"
            val workdir = wd
            getTestExecutor().process(workdir, "")(command).get
        }
    }

    it must "see other files in dir" in {
        val content = "hello world"
        val command = "bash -c 'cat content.in > content.out'"
        val workdir = wd
        val contentIn = workdir.resolve("content.in")
        resource.managed(new PrintWriter(contentIn.toFile)).foreach(_.println(content))
        val resultT = getTestExecutor().process(workdir, "")(command)

        if(resultT.isFailure) resultT.get
        resultT.isSuccess should equal(true)
        val result = resultT.get
        val contentPath = workdir.resolve("content.out")

        Files.exists(contentPath) should equal(true)
        result.otherCreatedFiles.contains(contentPath) should equal(true)
        Source.fromFile(contentPath.toFile).mkString should equal(content + "\n")
        Source.fromFile(result.stdout.toFile).mkString should equal("")
    }

    override protected def beforeAll(): Unit = {
        if (Files.exists(testWDRoot))
            FileUtils.deleteDirectory(testWDRoot.toFile)
    }

    protected def getTestExecutor(): Executor

    protected def wd: Path = {
        val localWD = testWDRoot.resolve(UUID.randomUUID().toString)
        Files.createDirectories(localWD)
        logger.info(s"Local WD: $localWD")
        localWD
    }

    protected def getLogger(): Logger = LoggerFactory.getLogger(classOf[Executor])
}
