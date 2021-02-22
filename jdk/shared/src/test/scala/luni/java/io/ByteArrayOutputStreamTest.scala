package luni.java.io

import java.io.ByteArrayOutputStream
import org.scalatest.BeforeAndAfterEach
import org.scalatest.freespec.AnyFreeSpec
import support.TestSupport

class ByteArrayOutputStreamTest extends AnyFreeSpec with BeforeAndAfterEach with TestSupport {
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

  "ConstructorI" in {
    bos = new ByteArrayOutputStream(100)
    assert(0 === bos.size)
  }

  "Constructor" in {
    bos = new ByteArrayOutputStream
    assert(0 === bos.size)
  }

  "close" in {
    // invalid spec
    bos = new ByteArrayOutputStream()
    bos.write(fileString.getBytes(), 0, 100)

    bos.close()
    bos.write(fileString.getBytes(), 0, 100)
    // Apache Harmony expects Exception, but OpenJDK 8+ not throwing
    assert(bos.toByteArray().nonEmpty)
  }

  "reset" in {
    bos = new ByteArrayOutputStream
    bos.write(fileString.getBytes, 0, 100)
    bos.reset()
    assert(0 === bos.size)
  }

  "size" in {
    bos = new ByteArrayOutputStream
    bos.write(fileString.getBytes, 0, 100)
    assert(100 === bos.size)
    bos.reset()
    assert(0 === bos.size)
  }

  "toByteArray" in {
    val sbytes = fileString.getBytes
    bos = new ByteArrayOutputStream
    bos.write(sbytes, 0, fileString.length)
    val bytes = bos.toByteArray
    assert(bytes.sameElements(sbytes))
  }

  "toStringLjava_lang_String" in {

    bos = new ByteArrayOutputStream
    bos.write(fileString.getBytes("UTF-8"), 0, fileString.length)
    assert(bos.toString("8859_1") === fileString)
  }

  "toStringLjava_lang_String: 8859_2" in {
    assume(!isScalaJS, " ISO8859-2 is not implemented in Scala-js")
    bos = new ByteArrayOutputStream
    bos.write(fileString.getBytes("UTF-8"), 0, fileString.length)
    assert(bos.toString("8859_2") === fileString)
  }

  "toString" in {
    bos = new ByteArrayOutputStream
    bos.write(fileString.getBytes, 0, fileString.length)
    assert(bos.toString === fileString)
  }

  "writeI" in {
    bos = new ByteArrayOutputStream
    bos.write('t')
    val result = bos.toByteArray
    assert("t" === new String(result, 0, result.length, "UTF-8"))
  }

  "write$BII" in {
    bos = new ByteArrayOutputStream
    bos.write(fileString.getBytes, 0, 100)
    val result = bos.toByteArray
    assert(new String(result, 0, result.length) === fileString.substring(0, 100))
  }

  "write$BII_2: Regression for HARMONY-387" in {
    val obj = new ByteArrayOutputStream
    val ex = intercept[IndexOutOfBoundsException] {
      obj.write(Array[Byte](0x00.toByte), -1, 0)
    }
    assert(
      ex.isInstanceOf[IndexOutOfBoundsException],
      "IndexOutOfBoundsException rather than a subclass expected"
    )
  }

  "writeToLjava_io_OutputStream" in {
    val bos  = new ByteArrayOutputStream
    val bos2 = new ByteArrayOutputStream
    bos.write(fileString.getBytes, 0, 100)
    bos.writeTo(bos2)
    assert(bos2.toString === fileString.substring(0, 100))
  }
}
