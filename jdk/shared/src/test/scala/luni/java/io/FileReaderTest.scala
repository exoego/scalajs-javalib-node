package luni.java.io

import java.io.{BufferedWriter, File, FileInputStream, FileReader, FileWriter}

import org.scalatest.{BeforeAndAfterEach, FunSuite}

class FileReaderTest extends FunSuite with BeforeAndAfterEach {

  private var br: FileReader       = _
  private var bw: BufferedWriter   = _
  private var fis: FileInputStream = _
  private var f: File              = _

  override def beforeEach(): Unit = {
    f = new File(System.getProperty("user.home"), "reader.tst")
    assert(!f.exists || f.delete)
  }

  override def afterEach(): Unit = {
    try bw.close()
    catch {
      case _: Exception => // Ignore
    }
    try br.close()
    catch {
      case _: Exception => // Ignore
    }
    try if (fis != null) fis.close()
    catch {
      case _: Exception => // Ignore
    }
    f.delete()
  }

  test("ConstructorLjava_io_File") {
    bw = new BufferedWriter(new FileWriter(f.getPath))
    bw.write(" After test string", 0, 18)
    bw.close()
    br = new FileReader(f)
    val buf = new Array[Char](100)
    val r   = br.read(buf)
    br.close()
    assert(" After test string" == new String(buf, 0, r))
  }

  // TODO: FileDescriptor
  ignore("ConstructorLjava_io_FileDescriptor") {
    bw = new BufferedWriter(new FileWriter(f.getPath))
    bw.write(" After test string", 0, 18)
    bw.close()
    val fis = new FileInputStream(f.getPath)
    br = new FileReader(fis.getFD)
    val buf = new Array[Char](100)
    val r   = br.read(buf)
    br.close()
    fis.close()
    assert(" After test string" == new String(buf, 0, r))
  }

  test("ConstructorLjava_lang_String") {
    bw = new BufferedWriter(new FileWriter(f.getPath))
    bw.write(" After test string", 0, 18)
    bw.close()
    br = new FileReader(f.getPath)
    val buf = new Array[Char](100)
    val r   = br.read(buf)
    br.close()
    assert(" After test string" == new String(buf, 0, r))
  }
}
