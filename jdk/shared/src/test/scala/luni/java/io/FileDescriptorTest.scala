package luni.java.io

import java.io.{
  BufferedOutputStream,
  File,
  FileDescriptor,
  FileInputStream,
  FileOutputStream,
  RandomAccessFile
}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.freespec.AnyFreeSpec
import support.TestSupport

class FileDescriptorTest extends AnyFreeSpec with BeforeAndAfterEach with TestSupport {
  private val platformId = getNewPlatformFile("JDK", "")

  private[io] var fos: FileOutputStream    = _
  private[io] var os: BufferedOutputStream = _
  private[io] var fis: FileInputStream     = _
  private[io] var f: File                  = _

  override def afterEach(): Unit = {
    Seq(fos, os, fis).foreach { closable =>
      try closable.close()
      catch { case _: Exception => }
    }
    if (f != null) {
      f.delete()
    }
  }

  "Constructor" in {
    val fd = new FileDescriptor()
    assert(fd.isInstanceOf[FileDescriptor])
  }

  "sync" in {
    f = new File(System.getProperty("user.dir"), "fd" + platformId + ".tst")
    fos = new FileOutputStream(f.getPath)
    fos.write("Test String".getBytes)
    fis = new FileInputStream(f.getPath)
    var fd = fos.getFD
    fd.sync()

    val length = "Test String".length
    assert(length === fis.available, "Bytes were not written after sync")

    // Regression test for Harmony-1494
    fd = fis.getFD
    fd.sync()
    assert(length === fis.available, "Bytes were not written after sync")
    val raf = new RandomAccessFile(f, "r")
    fd = raf.getFD
    fd.sync()
    raf.close()
  }

  "valid" in {
    f = new File(System.getProperty("user.dir"), "fd.tst")
    fos = new FileOutputStream(f.getPath)
    os = new BufferedOutputStream(fos, 4096)
    val fd = fos.getFD
    assert(fd.valid)
    os.close()
    assert(!fd.valid)
  }
}
