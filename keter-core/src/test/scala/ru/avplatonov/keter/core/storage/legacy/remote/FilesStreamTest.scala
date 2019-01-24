package ru.avplatonov.keter.core.storage.legacy.remote

import java.io.{FileOutputStream, PrintWriter}
import java.nio.file.{Files, Path}

import org.apache.commons.io.FileUtils
import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.curator.test.TestingServer
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}
import ru.avplatonov.keter.core.discovery.{DiscoveryService, Node, RemoteNode, ZookeeperDiscoveryService}
import ru.avplatonov.keter.core.storage.legacy.local.LocalFileDescriptorParser
import ru.avplatonov.keter.core.storage.legacy.remote.stream.{DirectoryCopyingException, DownloadedFile, FilesStream, FilesStreamOnTcp}

import scala.collection.mutable
import scala.concurrent.duration._
import scala.io.Source
import scala.util.{Failure, Random, Success}

class FilesStreamTest extends FlatSpec with Matchers with BeforeAndAfter {
    behavior of "FilesStream"

    val zkPort = 9999
    val connString = s"127.0.0.1:$zkPort"
    val zkConnSettings = ZookeeperDiscoveryService.Settings(
        connectionString = connString,
        new ExponentialBackoffRetry(1000, 3),
        "/root/nodes",
        discoveryTimeout = 1 second
    )

    var zkTestingServer: TestingServer = null
    var zk: CuratorFramework = null

    var files: List[(Path, Long)] = _ //paths with file sizes
    var firstWD: Path = _
    var secondWD: Path = _

    var firstTempDir: Path = _
    var sedondTempDir: Path = _

    var firstDiscovery: DiscoveryService = _
    var secondDiscovery: DiscoveryService = _

    var firstStream: FilesStream = _
    var secondStream: FilesStream = _

    var filesInSecondWD: List[(Path, Long)] = _

    before {
        new TestingServer(9999, true)
        zk = CuratorFrameworkFactory.newClient(zkConnSettings.connectionString, zkConnSettings.retryPolicy)
        zk.start()

        firstDiscovery = ZookeeperDiscoveryService(zkConnSettings, Node.Settings("127.0.0.1", 8081))
        secondDiscovery = ZookeeperDiscoveryService(zkConnSettings, Node.Settings("127.0.0.1", 8082))

        firstWD = Files.createTempDirectory("wd-1-")
        secondWD = Files.createTempDirectory("wd-2-")
        firstTempDir = Files.createTempDirectory("temp-1-")
        sedondTempDir = Files.createTempDirectory("temp-2-")

        firstStream = new FilesStreamOnTcp(firstDiscovery, FilesStreamOnTcp.Settings(firstTempDir, firstWD, 8090, 8100, 5))
        secondStream = new FilesStreamOnTcp(secondDiscovery, FilesStreamOnTcp.Settings(sedondTempDir, secondWD, 8101, 8111, 5))

        val buffer = mutable.Buffer[(Path, Long)]()
        for (fileId <- 0 until Random.nextInt(10)) {
            val sourceFilePath = secondWD.resolve(s"file-$fileId")
            val sizeOfFile = createRandomFile(sourceFilePath)
            buffer += (sourceFilePath -> sizeOfFile)
        }
        filesInSecondWD = buffer.toList

        firstDiscovery.start()
        secondDiscovery.start()

        firstStream.start()
        secondStream.start()
    }

    after {
        FileUtils.deleteDirectory(firstTempDir.toFile)
        FileUtils.deleteDirectory(sedondTempDir.toFile)
        FileUtils.deleteDirectory(firstWD.toFile)
        FileUtils.deleteDirectory(secondWD.toFile)

        firstStream.stop()
        secondStream.stop()

        firstDiscovery.stop()
        secondDiscovery.stop()

        zk.close()
    }

    it should "pass files between streams" in {
        assert(firstTempDir.toFile.listFiles().length == 0)
        assert(sedondTempDir.toFile.listFiles().length == 0)

        val files = filesInSecondWD.map(_._1.toFile.getName)
            .flatMap(name => LocalFileDescriptorParser(s"//" + name))
        val remoteFiles = firstStream.download(files, getSecondNode())
        val downloadedFilesT = remoteFiles.get()
        val downloadedFiles = downloadedFilesT.get

        assert(downloadedFiles.size == files.size)
        assert(firstTempDir.toFile.listFiles().length == secondWD.toFile.listFiles().length)
        downloadedFiles.foreach({
            case DownloadedFile(desc, file) =>
                val originalFile = secondWD.resolve(desc.key)
                checkContent(file.toPath, originalFile)
        })
    }

    it should "work with paths with long prefix" in {
        assert(firstTempDir.toFile.listFiles().length == 0)
        assert(sedondTempDir.toFile.listFiles().length == 0)

        val localRoot = secondWD.resolve("child_1")
        val dir1 = localRoot.resolve("child_11")
        val dir2 = dir1.resolve("child_111")

        Files.createDirectories(dir1)
        Files.createDirectories(dir2)

        val file01 = localRoot.resolve("file01")
        val file02 = localRoot.resolve("file02")
        val file11 = dir1.resolve("file11")
        val file12 = dir1.resolve("file12")
        val file21 = dir2.resolve("file21")

        try {
            val randomFiles: List[(Path, Long)] = List(file01, file02, file11, file12, file21)
                .map(p => p -> createRandomFile(p))

            val descriptors = randomFiles.map(_._1.toString.replaceAll("/tmp/wd.*?/", ""))
                .map(x => s"//" + x)
                .map(LocalFileDescriptorParser.parse)
            val result = firstStream.download(descriptors, getSecondNode()).get()

            result match {
                case Failure(e) => throw e
                case Success(res) =>
                    assert(res.size == randomFiles.size)
                    res foreach {
                        case DownloadedFile(desc, path) =>
                            val pathWithoutTempSuffix = path.toPath.toAbsolutePath.toString.replaceAll("(?<=file\\d{1,3})-.*?$", "")
                            assert(pathWithoutTempSuffix == firstTempDir.resolve(desc.key).toAbsolutePath.toString)
                            val originalFile = secondWD.resolve(desc.path.mkString("/") + "/" + desc.key)
                            checkContent(originalFile, path.toPath)
                    }
            }
        }
        finally {
            FileUtils.deleteDirectory(localRoot.toFile)
        }
    }

    it should "resolve names conflict" in {
        assert(firstTempDir.toFile.listFiles().length == 0)
        assert(sedondTempDir.toFile.listFiles().length == 0)

        val dir1 = secondWD.resolve("child_1")
        val dir2 = secondWD.resolve("child_2")

        Files.createDirectories(dir1)
        Files.createDirectories(dir2)

        val FILENAME = "file"
        val file1 = dir1.resolve(FILENAME)
        val file2 = dir2.resolve(FILENAME)

        try {
            val randomFiles: List[(Path, Long)] = List(file1, file2)
                .map(p => p -> createRandomFile(p))

            val descriptors = randomFiles.map(_._1.toString.replaceAll("/tmp/wd.*?/", ""))
                .map(x => s"//" + x)
                .map(LocalFileDescriptorParser.parse)
            val result = firstStream.download(descriptors, getSecondNode()).get()

            result match {
                case Failure(e) => throw e
                case Success(res) =>
                    assert(res.size == randomFiles.size)
                    res foreach {
                        case DownloadedFile(desc, path) =>
                            val pathWithoutTempSuffix = path.toPath.toAbsolutePath.toString.replaceAll("(?<=file)-.*?$", "")
                            assert(pathWithoutTempSuffix == firstTempDir.resolve(desc.key).toAbsolutePath.toString)
                            val originalFile = secondWD.resolve(desc.path.mkString("/") + "/" + desc.key)
                            checkContent(originalFile, path.toPath)
                    }
            }
        }
        finally {
            FileUtils.deleteDirectory(dir1.toFile)
            FileUtils.deleteDirectory(dir2.toFile)
        }
    }

    it should "deny directories copying" in {
        assert(firstTempDir.toFile.listFiles().length == 0)
        assert(sedondTempDir.toFile.listFiles().length == 0)

        val dir1 = secondWD.resolve("child_1")

        Files.createDirectories(dir1)

        val file1 = dir1.resolve("file1")
        val file2 = dir1.resolve("file2")

        try {
            val randomFiles: List[(Path, Long)] = List(file1, file2)
                .map(p => p -> createRandomFile(p))

            val descriptors = List(LocalFileDescriptorParser.parse("//child_1").copy(isDir = Some(true)))
            try {
                firstStream.download(descriptors, getSecondNode()).get()
                throw new IllegalStateException("Protocol doesn't support directories")
            } catch {
                case e: DirectoryCopyingException => assert(true)
            }
        }
        finally {
            FileUtils.deleteDirectory(dir1.toFile)
        }
    }

    private def createRandomFile(path: Path): Long = {
        resource.managed(new PrintWriter(new FileOutputStream(path.toFile))) acquireAndGet { out =>
            for (line <- 0 until Random.nextInt(1000)) {
                out.println(Random.nextLong())
            }
        }

        FileUtils.sizeOf(path.toFile)
    }

    private def getSecondNode(): RemoteNode = {
        firstDiscovery.allNodes.find(x => !x.isLocal).get.asInstanceOf[RemoteNode]
    }

    private def checkContent(file1: Path, file2: Path): Unit = {
        assert(Files.size(file1) == Files.size(file2))
        Source.fromFile(file1.toFile).getLines().zip(Source.fromFile(file2.toFile).getLines()) foreach {
            case (copiedLine, originalLine) =>
                assert(copiedLine == originalLine, s"files are not equal [$file1, $file2]")
        }
    }

    //TODO: check port pool
    //TODO: check start-stop conditions
    //TODO: check sending protocol with IO errors
}
