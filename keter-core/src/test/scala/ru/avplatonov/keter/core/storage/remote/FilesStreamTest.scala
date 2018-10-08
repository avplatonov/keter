package ru.avplatonov.keter.core.storage.remote

import java.io.{FileOutputStream, PrintWriter}
import java.nio.file.{Files, Path}

import org.apache.commons.io.FileUtils
import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.curator.test.TestingServer
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}
import ru.avplatonov.keter.core.discovery.{DiscoveryService, Node, RemoteNode, ZookeeperDiscoveryService}
import ru.avplatonov.keter.core.storage.local.LocalFileDescriptorParser

import scala.collection.mutable
import scala.concurrent.duration._
import scala.io.Source
import scala.util.Random

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

        firstStream = new FilesStream(firstDiscovery, FilesStream.Settings(firstTempDir, firstWD, 8090, 8100, 5))
        secondStream = new FilesStream(secondDiscovery, FilesStream.Settings(sedondTempDir, secondWD, 8101, 8111, 5))

        val buffer = mutable.Buffer[(Path, Long)]()
        for(fileId <- 0 until Random.nextInt(10)) {
            val sourceFilePath = secondWD.resolve(s"file-$fileId")
            resource.managed(new PrintWriter(new FileOutputStream(sourceFilePath.toFile))) acquireAndGet { out =>
                for(line <- 0 until Random.nextInt(1000)) {
                    out.println(Random.nextLong())
                }
            }
            val sizeOfFile = FileUtils.sizeOf(sourceFilePath.toFile)
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
        val remoteFiles = firstStream.download(files, firstDiscovery.allNodes.find(x => !x.isLocal).get.asInstanceOf[RemoteNode])
        val downloadedFilesT = remoteFiles.get()
        val downloadedFiles = downloadedFilesT.get

        assert(downloadedFiles.size == files.size)
        assert(firstTempDir.toFile.listFiles().length == secondWD.toFile.listFiles().length)
        downloadedFiles.foreach({
            case DownloadedFile(desc, file) =>
                val originalFile = secondWD.resolve(desc.key)
                assert(Files.size(file.toPath) == Files.size(originalFile))
                Source.fromFile(file).getLines().zip(Source.fromFile(originalFile.toFile).getLines()) foreach {
                    case (copiedLine, originalLine) =>
                        assert(copiedLine == originalLine, s"files are not equal [$desc, $originalFile]")
                }
        })
    }

    //TODO: check paths longer than roots
    //TODO: check start-stop conditions
    //TODO: check sending protocol with IO errors
}
