package ru.avplatonov.keter.core.storage.legacy.remote.index

import java.nio.file.{Files, Path}

import org.apache.commons.io.FileUtils
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}
import ru.avplatonov.keter.core.discovery._

import scala.collection.mutable
import scala.util.Random

class FilesIndexOnDBTest extends FlatSpec with Matchers with BeforeAndAfter {
    val db = FilesDBOnHashMap()
    var wd: Path = _
    var files: List[Path] = _
    var index: FilesIndexOnDB = _

    behavior of "FilesIndexOnDB"

    before {
        wd = Files.createTempDirectory("files-index")

        val dirsBuffer = mutable.Buffer[Path]()
        (0 until Random.nextInt(10)).foreach({
            case dirIdx =>
                val newSubDir = randomDir(dirsBuffer).getOrElse(wd).resolve(dirIdx.toString)
                Files.createDirectories(newSubDir)
                dirsBuffer += newSubDir
        })

        dirsBuffer += wd

        files = (
            for (i <- 0 until Random.nextInt(1000)) yield {
                val dir = randomDir(dirsBuffer, withNone = false).get
                val rndFile = dir.resolve(s"file-$i")
                Files.createFile(rndFile)
                rndFile
            }).toList

        index = FilesIndexOnDB(db, wd, fakeDiscovery)
    }

    after {
        FileUtils.deleteDirectory(wd.toFile)
    }

    it should "index all files" in {
        index.rebuildIndex()
        val builtIndex = db.idx()
        assert(builtIndex.size == files.size)
        val indexedKeys = builtIndex.keys.map(_._1)
        val filesWithWDRoot = files.map(_.toAbsolutePath.toString.replaceAll(wd.toAbsolutePath.toString, "/")).toSet
        assert(indexedKeys == filesWithWDRoot)
    }

    //TODO: all tests

    private def randomDir(dirsBuffer: mutable.Buffer[Path], withNone: Boolean = true): Option[Path] = {
        val i = Random.nextInt(if (withNone) dirsBuffer.length + 1
        else dirsBuffer.length)
        if (i == dirsBuffer.length)
            None
        else
            Some(dirsBuffer(i))
    }

    private val fakeDiscovery = new DiscoveryService {
        /** */
        override def start(): Node = ???

        /** */
        override def stop(): Unit = ???

        /** */
        override def allNodes: List[Node] = ???

        /** */
        override def get(nodeId: NodeId): Option[Node] = ???

        /** */
        override def subscribe(eventListener: EventListener): Unit = ???

        /** */
        override def unsubscribe(eventListener: EventListener): Unit = ???

        /** */
        override def isStarted(): Boolean = ???

        /** */
        override def getLocalNode(): Option[LocalNode] = ???

        /**
          * @return local node id if service was started.
          */
        override def getLocalNodeId(): Option[NodeId] = Some(NodeId(0))
    }
}
