package ru.avplatonov.keter.core.storage.remote

import java.io.{FileOutputStream, PrintWriter}
import java.nio.file.{Files, Path}

import org.apache.commons.io.FileUtils
import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.curator.test.TestingServer
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}
import ru.avplatonov.keter.core.discovery.{DiscoveryService, Node, ZookeeperDiscoveryService}

import scala.collection.mutable
import scala.concurrent.duration._
import scala.util.Random

class FilesStreamTest extends FlatSpec with Matchers with BeforeAndAfter {
    behavior of "FilesStream"

    val zkPort = 9999
    val connString = s"127.0.0.1:$zkPort"
    val zkConnSettings = ZookeeperDiscoveryService.Settings(
        connectionString = connString,
        new ExponentialBackoffRetry(1000, 3),
        "/root/nodes",
        discoveryTimeout = 50 millis
    )

    var zkTestingServer: TestingServer = null
    var zk: CuratorFramework = null

    var files: List[(Path, Long)] = null //paths with file sizes
    var sourceTempDir: Path = null
    var downloaderTempDir: Path = null

    var firstDiscovery: DiscoveryService = null
    var secondDiscovery: DiscoveryService = null

    before {
        new TestingServer(9999, true)
        zk = CuratorFrameworkFactory.newClient(zkConnSettings.connectionString, zkConnSettings.retryPolicy)
        zk.start()

        firstDiscovery = ZookeeperDiscoveryService(zkConnSettings, Node.Settings("127.0.0.1", 8081))
        secondDiscovery = ZookeeperDiscoveryService(zkConnSettings, Node.Settings("127.0.0.1", 8082))

        sourceTempDir = Files.createTempDirectory("source-")
        downloaderTempDir = Files.createTempDirectory("downloader-")

        val buffer = mutable.Buffer[(Path, Long)]()
        for(fileId <- 0 until Random.nextInt(10)) {
            val sourceFilePath = sourceTempDir.resolve(s"file-$fileId")
            resource.managed(new PrintWriter(new FileOutputStream(sourceFilePath.toFile))) acquireAndGet { out =>
                for(line <- 0 until Random.nextInt(1000)) {
                    out.println(Random.nextLong())
                }
            }
            val sizeOfFile = FileUtils.sizeOf(sourceFilePath.toFile)
            buffer += (sourceFilePath -> sizeOfFile)
        }
    }

    after {
        FileUtils.deleteDirectory(sourceTempDir.toFile)
        FileUtils.deleteDirectory(downloaderTempDir.toFile)

        firstDiscovery.stop()
        secondDiscovery.stop()

        zk.close()
    }

    it should "send message to remote node and open port" in {
        assert(false)
    }
}
