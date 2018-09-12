package ru.avplatonov.keter.core.storage

import java.nio.file.{Files, Path, Paths}
import java.util.UUID

import org.scalatest.{FlatSpec, Matchers}
import ru.avplatonov.keter.core.storage.local.LocalTemporaryFilesStorage.Settings
import ru.avplatonov.keter.core.storage.local._

class LocalTemporaryFilesStorageTest extends FlatSpec with Matchers {
    val tmpdir: Path = Paths.get("/tmp/test")
    if(tmpdir.toFile.exists())
        tmpdir.toFile.delete()
    Files.createDirectory(tmpdir)
    val storage: OnCountersTempObjsCache[Path, LocalFileDescriptor] = new LocalTemporaryFilesStorage(Settings(tmpdir))

    "temp FS" should "create move file and delete it after processing" in {
        withTmpFile(tmp => {
            val filename = tmp.filepath.getFileName.toString
            tmp.filepath.toFile.exists() should be(true)
            val holder = storage.put(tmp.filepath, tmp)
            tmp.filepath.toFile.exists() should be(false)
            tmpdir.resolve(filename).toFile.exists() should be(true)
            holder.foreach(desc => {
                desc.filepath should be(tmpdir.resolve(filename))
            })
            tmpdir.resolve(filename).toFile.exists() should be(false)
            tmp.filepath.toFile.exists() should be(false)
        })
    }

    "temp FS" should "work with several holders and delete file after all holders released" in {
        withTmpFile(tmp => {
            val filename = tmp.filepath.getFileName.toString

            val firstHolder = storage.put(tmp.filepath, tmp)

            val otherHolders = (0 until 10).map(i => storage.get(tmp.filepath)).toList
            otherHolders.forall(_.isDefined) should be(true)

            val yetAnotherHolder = storage.get(tmp.filepath).get

            tmpdir.resolve(filename).toFile.exists() should be(true)
            otherHolders.flatten.foreach(checkHolder(filename, _))
            tmpdir.resolve(filename).toFile.exists() should be(true)
            checkHolder(filename, firstHolder)
            tmpdir.resolve(filename).toFile.exists() should be(true)
            checkHolder(filename, yetAnotherHolder)
            tmpdir.resolve(filename).toFile.exists() should be(false)
        })
    }

    private def checkHolder(filename: String, holder: Holder[Path, LocalFileDescriptor]): Unit = {
        holder.foreach(desc => {
            desc.filepath should be(tmpdir.resolve(filename))
        })
    }

    private def withTmpFile(f: LocalFileDescriptor => Unit): Unit = {
        val desc = LocalFileDescriptorParser.parse("/tmp/" + UUID.randomUUID().toString)
        LocalFilesStorage.create(desc, ignoreExisting = true)
        try {
            f(desc)
        } finally {
            LocalFilesStorage.delete(desc)
        }
    }
}
