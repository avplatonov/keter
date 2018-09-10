package ru.avplatonov.keter.core.storage

import java.io.{BufferedReader, File, InputStreamReader, PrintWriter}
import java.nio.file.Path
import java.util.UUID

import com.google.common.io.Files
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

import scala.io.Source

class LocalFilesStorageTest extends FlatSpec with Matchers with BeforeAndAfter {
    private var tempDir: File = _
    private var tempFile: File = _

    before {
        tempDir = Files.createTempDir()
        tempFile = File.createTempFile("test-", "-tmp", tempDir)
    }

    after {
        tempFile.delete()
        tempDir.delete()
    }

    "local file parser" should "return valid descs" in {
        val tempFilePath = tempFile.toPath
        val tempFilename = tempFilePath.getFileName.toString

        val pathStr = tempFilePath.toString
        val split = pathStr.split("/").map(_.trim).filterNot(_.isEmpty).toList
        var descOpt = LocalFileDescriptorParser(tempFile.toPath.toString)

        descOpt.isDefined should equal(true)

        var desc = descOpt.get
        desc.isDir.isDefined should equal(true)
        desc.isDir should be(Some(false))
        desc.filepath should be(tempFilePath)
        desc.key should be(tempFilename)
        desc.scheme should be(PathScheme.local)
        desc.path should be(split.init)

        descOpt = LocalFileDescriptorParser(tempDir.toPath.toString)
        descOpt.isDefined should equal(true)
        desc = descOpt.get
        desc.isDir.isDefined should equal(true)
        desc.isDir should be(Some(true))
    }

    "local FS exists" should "return true" in {
        LocalFilesStorage.exists(descOf(tempFile)) should be(true)
        LocalFilesStorage.exists(descOf(tempDir)) should be(true)
    }

    "local FS exists" should "return false" in {
        LocalFilesStorage.exists(descOf(tempDir.toPath.resolve("child").toFile)) should be(false)
    }

    "local FS" should "create new file" in {
        withTempPath(temp => {
            val desc = LocalFileDescriptorParser.parse(temp.toString)
            LocalFilesStorage.create(desc, ignoreExisting = true) should be(true)
            LocalFilesStorage.create(desc, ignoreExisting = false) should be(false)
            LocalFilesStorage.exists(desc) should be(true)
        })
    }

    "local FS" should "move file" in {
        withTempPath(temp => {
            val from = descOf(tempFile)
            val to = LocalFileDescriptorParser.parse(temp.toString)
            LocalFilesStorage.move(from, to, ignoreExisting = true) should be(true)
            LocalFilesStorage.exists(from) should be(false)
            LocalFilesStorage.exists(to) should be(true)
        })
    }

    "local FS" should "copy file" in {
        withTempPath(temp => {
            val from = descOf(tempFile)
            val to = LocalFileDescriptorParser.parse(temp.toString)
            LocalFilesStorage.copy(from, to, ignoreExisting = true) should be(true)
            LocalFilesStorage.copy(from, to, ignoreExisting = false) should be(false)
            LocalFilesStorage.exists(from) should be(true)
            LocalFilesStorage.exists(to) should be(true)
        })
    }

    "local FS" should "return valid files list in dir" in {
        withTempPath(temp => {
            val tempDesc = descOf(temp.toFile)
            LocalFilesStorage.create(tempDesc, ignoreExisting = true)
            LocalFilesStorage.getFilesInDirectory(descOf(tempDir)).toSet should be(Set(
                descOf(tempFile),
                tempDesc
            ))
        })
    }

    "local FS" should "work with IO-streams" in {
        withTempPath(temp => {
            val content = "Hello, world!"
            val desc = descOf(temp.toFile)

            LocalFilesStorage.write(desc)
                .flatMap(s => resource.managed(new PrintWriter(s)))
                .foreach(out => out.println(content))
            Source.fromFile(temp.toFile).getLines().mkString("") should be(content)
            LocalFilesStorage.read(desc)
                .flatMap(s => resource.managed(new BufferedReader(new InputStreamReader(s))))
                .foreach(r => r.readLine() should be(content))
        })
    }

    private def descOf(file: File): LocalFileDescriptor =
        LocalFileDescriptorParser(file.toPath.toString).get

    private def withTempPath(f: Path => Unit): Unit = {
        val temp = tempDir.toPath.resolve(UUID.randomUUID().toString)

        try {
            f(temp)
        } finally {
            temp.toFile.delete()
        }
    }
}
