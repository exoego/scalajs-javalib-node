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
import org.scalatest.funsuite.AnyFunSuite

class FileDescriptorTest extends AnyFunSuite with BeforeAndAfterEach {
  private val platformId = "JDK" + System.getProperty("java.vm.version").replace('.', '-')

  private[io] var fos: FileOutputStream    = _
  private[io] var os: BufferedOutputStream = _
  private[io] var fis: FileInputStream     = _
  private[io] var f: File                  = _

  override def afterEach(): Unit = {
    Seq(fos, os, fis).foreach { closable =>
      try closable.close()
      catch { case _: Exception => }
    }
  }

  test("Constructor") {
    val fd = new FileDescriptor()
    assert(fd.isInstanceOf[FileDescriptor])
  }

  test("sync") {
    f = new File(System.getProperty("user.dir"), "fd" + platformId + ".tst")
    f.delete
    fos = new FileOutputStream(f.getPath)
    fos.write("Test String".getBytes)
    fis = new FileInputStream(f.getPath)
    var fd = fos.getFD
    fd.sync()

    val length = "Test String".length
    assert(length == fis.available, "Bytes were not written after sync")

    // Regression test for Harmony-1494
    fd = fis.getFD
    fd.sync()
    assert(length == fis.available, "Bytes were not written after sync")
    val raf = new RandomAccessFile(f, "r")
    fd = raf.getFD
    fd.sync()
    raf.close()
  }

  test("valid") {
    f = new File(System.getProperty("user.dir"), "fd.tst")
    f.delete
    fos = new FileOutputStream(f.getPath)
    os = new BufferedOutputStream(fos, 4096)
    val fd = fos.getFD
    assert(fd.valid)
    os.close()
    assert(!fd.valid)
  }
}
