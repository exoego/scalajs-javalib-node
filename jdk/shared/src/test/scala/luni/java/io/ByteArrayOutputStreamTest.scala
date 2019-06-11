package luni.java.io

import java.io.ByteArrayOutputStream

import org.scalatest.{ BeforeAndAfterEach, FunSuite }

class ByteArrayOutputStreamTest extends FunSuite with BeforeAndAfterEach {
  private[io] var bos: ByteArrayOutputStream = _

  var fileString =
    "Test_All_Tests\nTest_java_io_BufferedInputStream\nTest_java_io_BufferedOutputStream\nTest_java_io_ByteArrayInputStream\nTest_ByteArrayOutputStream\nTest_java_io_DataInputStream\n"

  override def afterEach(): Unit = {
    try {
      bos.close()
    } catch {
      case _: Exception =>
    }
  }

  test("ConstructorI") {
    bos = new ByteArrayOutputStream(100)
    assert(0 == bos.size)
  }

  test("Constructor") {
    bos = new ByteArrayOutputStream
    assert(0 == bos.size)
  }

  ignore("close") {
    // invalid spec
    bos = new ByteArrayOutputStream()
    bos.write(fileString.getBytes(), 0, 100)

    bos.close()
    bos.write(fileString.getBytes(), 0, 100)
    assertThrows[Exception] {
      bos.toByteArray()
    }
  }

  test("reset") {
    bos = new ByteArrayOutputStream
    bos.write(fileString.getBytes, 0, 100)
    bos.reset()
    assert(0 == bos.size)
  }

  test("size") {
    bos = new ByteArrayOutputStream
    bos.write(fileString.getBytes, 0, 100)
    assert(100 == bos.size)
    bos.reset()
    assert(0 == bos.size)
  }

  test("toByteArray") {
    val sbytes = fileString.getBytes
    bos = new ByteArrayOutputStream
    bos.write(sbytes, 0, fileString.length)
    val bytes = bos.toByteArray
    assert(bytes.sameElements(sbytes))
  }

  test("toStringLjava_lang_String") {
    bos = new ByteArrayOutputStream
    bos.write(fileString.getBytes("UTF-8"), 0, fileString.length)
    assert(bos.toString("8859_1") == fileString)
  }

  // ISO8859-2 i not implemented
  ignore("toStringLjava_lang_String: 8859_2") {
    bos = new ByteArrayOutputStream
    bos.write(fileString.getBytes("UTF-8"), 0, fileString.length)
    assert(bos.toString("8859_2") == fileString)
  }

  test("toString") {
    bos = new ByteArrayOutputStream
    bos.write(fileString.getBytes, 0, fileString.length)
    assert(bos.toString == fileString)
  }

  test("writeI") {
    bos = new ByteArrayOutputStream
    bos.write('t')
    val result = bos.toByteArray
    assert("t" == new String(result, 0, result.length, "UTF-8"))
  }

  test("write$BII") {
    bos = new ByteArrayOutputStream
    bos.write(fileString.getBytes, 0, 100)
    val result = bos.toByteArray
    assert(new String(result, 0, result.length) == fileString.substring(0, 100))
  }

  test("write$BII_2: Regression for HARMONY-387") {
    val obj = new ByteArrayOutputStream
    val ex = intercept[IndexOutOfBoundsException] {
      obj.write(Array[Byte](0x00.toByte), -1, 0)
    }
    assert(
      ex.isInstanceOf[IndexOutOfBoundsException],
      "IndexOutOfBoundsException rather than a subclass expected"
    )
  }

  test("writeToLjava_io_OutputStream") {
    val bos  = new ByteArrayOutputStream
    val bos2 = new ByteArrayOutputStream
    bos.write(fileString.getBytes, 0, 100)
    bos.writeTo(bos2)
    assert(bos2.toString == fileString.substring(0, 100))
  }
}
