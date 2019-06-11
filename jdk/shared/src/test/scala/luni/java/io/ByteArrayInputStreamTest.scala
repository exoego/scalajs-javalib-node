package luni.java.io

import java.io.{ByteArrayInputStream, InputStream, InputStreamReader}

import org.scalatest.{BeforeAndAfterEach, FunSuite}

class ByteArrayInputStreamTest extends FunSuite with BeforeAndAfterEach {
  private var is: InputStream = _
  var fileString =
    "Test_All_Tests\nTest_java_io_BufferedInputStream\nTest_java_io_BufferedOutputStream\nTest_ByteArrayInputStream\nTest_java_io_ByteArrayOutputStream\nTest_java_io_DataInputStream\n"

  override def beforeEach(): Unit = {
    is = new ByteArrayInputStream(fileString.getBytes)
  }

  override def afterEach(): Unit = {
    try {
      is.close()
    } catch {
      case _: Exception => // do nothing
    }
  }

  test("Constructor$B") {
    val bis = new ByteArrayInputStream(fileString.getBytes)
    assert(bis.available == fileString.length)
  }

  test("Constructor$BII") {
    val zz  = fileString.getBytes
    val bis = new ByteArrayInputStream(zz, 0, 100)
    assert(100 == bis.available)
  }

  ignore("Constructor$BII: Regression test for Harmony-2405") {
    new SubByteArrayInputStream(Array[Byte](1, 2), 444, 13)
    assert(444 == SubByteArrayInputStream.pos)
    assert(444 == SubByteArrayInputStream.mark)
    assert(2 == SubByteArrayInputStream.count)
  }

  private[io] object SubByteArrayInputStream {
    var buf: Array[Byte] = null
    var mark             = 0
    var pos              = 0
    var count            = 0
  }

  private[io] class SubByteArrayInputStream(
      buf: Array[Byte],
      val offset: Int,
      val length: Int
  ) extends ByteArrayInputStream(buf, offset, length) {
    SubByteArrayInputStream.buf = buf
    SubByteArrayInputStream.mark = mark
    SubByteArrayInputStream.pos = pos
    SubByteArrayInputStream.count = count
  }

  test("available") {
    assert(is.available == fileString.length)
  }

  test("close") {
    is.read()
    is.close()
    is.read()
  }

  test("markI") {
    val buf1 = new Array[Byte](100)
    val buf2 = new Array[Byte](100)
    is.skip(3000)
    is.mark(1000)
    is.read(buf1, 0, buf1.length)
    is.reset()
    is.read(buf2, 0, buf2.length)
    is.reset()
    assert(new String(buf1, 0, buf1.length) == new String(buf2, 0, buf2.length))
  }

  test("markSupported") {
    assert(is.markSupported)
  }

  test("read") {
    val isr = new InputStreamReader(is)
    val c   = isr.read()
    is.reset()
    assert(c == fileString.charAt(0))
  }

  test("read$BII") {
    val buf1 = new Array[Byte](20)
    is.skip(50)
    is.mark(100)
    is.read(buf1, 0, buf1.length)
    assert(new String(buf1, 0, buf1.length) == fileString.substring(50, 70))
  }

  test("reset") {
    val buf1 = new Array[Byte](10)
    val buf2 = new Array[Byte](10)
    is.mark(200)
    is.read(buf1, 0, 10)
    is.reset()
    is.read(buf2, 0, 10)
    is.reset()
    assert(new String(buf1, 0, buf1.length) == new String(buf2, 0, buf2.length))
  }

  test("skipJ") {
    val buf1 = new Array[Byte](10)
    is.skip(100)
    is.read(buf1, 0, buf1.length)
    assert(new String(buf1, 0, buf1.length) == fileString.substring(100, 110))
  }

}
