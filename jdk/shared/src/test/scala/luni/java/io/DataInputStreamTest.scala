package luni.java.io

import java.io.{
  ByteArrayInputStream,
  ByteArrayOutputStream,
  DataInput,
  DataInputStream,
  DataOutputStream,
  EOFException
}

import org.scalatest.BeforeAndAfterEach
import org.scalatest.freespec.AnyFreeSpec

class DataInputStreamTest extends AnyFreeSpec with BeforeAndAfterEach {
  private var os: DataOutputStream = _

  private var dis: DataInputStream = _

  private var bos: ByteArrayOutputStream = _

  private[io] val unihw = "\u0048\u0065\u006C\u006C\u006F\u0020\u0057\u006F\u0072\u006C\u0064"

  var fileString =
    "Test_All_Tests\nTest_java_io_BufferedInputStream\nTest_java_io_BufferedOutputStream\nTest_java_io_ByteArrayInputStream\nTest_java_io_ByteArrayOutputStream\nTest_DataInputStream\n"

  override def beforeEach(): Unit = {
    bos = new ByteArrayOutputStream()
    os = new DataOutputStream(bos)
  }

  override protected def afterEach(): Unit = {
    try os.close()
    catch {
      case _: Exception =>
    }
    try dis.close()
    catch {
      case _: Exception =>
    }
  }

  private def openDataInputStream(): Unit = {
    dis = new DataInputStream(new ByteArrayInputStream(bos.toByteArray))
  }

  "ConstructorLjava_io_InputStream" in {
    try {
      os.writeChar('t')
      os.close()
      openDataInputStream()
    } finally {
      dis.close()
    }
  }

  "read$B" in {
    os.write(fileString.getBytes())
    os.close()
    openDataInputStream()
    val rbytes = new Array[Byte](fileString.length())
    dis.read(rbytes)
    assert(new String(rbytes, 0, fileString.length()) === fileString)
  }

  "read$BII" in {
    os.write(fileString.getBytes())
    os.close()
    openDataInputStream()
    val rbytes = new Array[Byte](fileString.length())
    dis.read(rbytes, 0, rbytes.length)
    assert(new String(rbytes, 0, fileString.length()) === fileString)
  }

  "readBoolean" in {
    os.writeBoolean(true)
    os.close()
    openDataInputStream()
    assert(dis.readBoolean())
  }

  "readByte" in {
    os.writeByte(127.toByte)
    os.close()
    openDataInputStream()
    assert(dis.readByte() === 127.toByte)
  }

  "readChar" in {
    os.writeChar('t')
    os.close()
    openDataInputStream()
    assert('t' === dis.readChar())
  }

  "readDouble" in {
    os.writeDouble(2345.76834720202)
    os.close()
    openDataInputStream()
    assert(2345.76834720202 === dis.readDouble())
  }

  "readFloat" in {
    os.writeFloat(29.08764f)
    os.close()
    openDataInputStream()
    assert(dis.readFloat() === 29.08764f)
  }

  "readFully$B" in {
    os.write(fileString.getBytes())
    os.close()
    openDataInputStream()
    val rbytes = new Array[Byte](fileString.length())
    dis.readFully(rbytes)
    assert(new String(rbytes, 0, fileString.length()) === fileString)
  }

  "readFully$BII" in {
    os.write(fileString.getBytes())
    os.close()
    openDataInputStream()
    val rbytes = new Array[Byte](fileString.length())
    dis.readFully(rbytes, 0, fileString.length())
    assert(new String(rbytes, 0, fileString.length()) === fileString)
  }

  "readFully$BII_Exception" ignore {
    val is        = new DataInputStream(new ByteArrayInputStream(new Array[Byte](fileString.length)))
    val byteArray = new Array[Byte](fileString.length)
    assertThrows[IndexOutOfBoundsException] {
      is.readFully(byteArray, -1, -1)
    }
    assertThrows[IndexOutOfBoundsException] {
      is.readFully(byteArray, 0, -1)
    }
    assertThrows[IndexOutOfBoundsException] {
      is.readFully(byteArray, 1, -1)
    }

    is.readFully(byteArray, -1, 0)
    is.readFully(byteArray, 0, 0)
    is.readFully(byteArray, 1, 0)

    assertThrows[IndexOutOfBoundsException] {
      is.readFully(byteArray, -1, 1)
    }

    is.readFully(byteArray, 0, 1)
    is.readFully(byteArray, 1, 1)

    assertThrows[IndexOutOfBoundsException] {
      is.readFully(byteArray, 0, Integer.MAX_VALUE)
    }
  }

  "readFully$BII_NullArray" ignore {
    val is            = new DataInputStream(new ByteArrayInputStream(new Array[Byte](fileString.length)))
    val nullByteArray = null
    assertThrows[IndexOutOfBoundsException] {
      is.readFully(nullByteArray, -1, -1)
    }
    assertThrows[IndexOutOfBoundsException] {
      is.readFully(nullByteArray, 0, -1)
    }
    assertThrows[IndexOutOfBoundsException] {
      is.readFully(nullByteArray, 1, -1)
    }

    is.readFully(nullByteArray, -1, 0)
    is.readFully(nullByteArray, 0, 0)
    is.readFully(nullByteArray, 1, 0)

    assertThrows[NullPointerException] {
      is.readFully(nullByteArray, -1, 1)
    }
    assertThrows[NullPointerException] {
      is.readFully(nullByteArray, 0, 1)
    }
    assertThrows[NullPointerException] {
      is.readFully(nullByteArray, 1, 1)
    }
    assertThrows[NullPointerException] {
      is.readFully(nullByteArray, 0, Integer.MAX_VALUE)
    }
  }

  "readFully$BII_NullStream_NullArray" ignore {
    val is            = new DataInputStream(null)
    val nullByteArray = null

    assertThrows[IndexOutOfBoundsException] {
      is.readFully(nullByteArray, -1, -1)
    }
    assertThrows[IndexOutOfBoundsException] {
      is.readFully(nullByteArray, 0, -1)
      fail("should throw IndexOutOfBoundsException")
    }
    assertThrows[IndexOutOfBoundsException] {
      is.readFully(nullByteArray, 1, -1)
    }

    is.readFully(nullByteArray, -1, 0)
    is.readFully(nullByteArray, 0, 0)
    is.readFully(nullByteArray, 1, 0)

    assertThrows[NullPointerException] {
      is.readFully(nullByteArray, -1, 1)
    }
    assertThrows[NullPointerException] {
      is.readFully(nullByteArray, 0, 1)
    }
    assertThrows[NullPointerException] {
      is.readFully(nullByteArray, 1, 1)
    }
    assertThrows[NullPointerException] {
      is.readFully(nullByteArray, 0, Integer.MAX_VALUE)
    }
  }

  "readInt" in {
    os.writeInt(768347202)
    os.close()
    openDataInputStream()
    assert(768347202 === dis.readInt)
  }

  "readLong" in {
    os.writeLong(9875645283333L)
    os.close()
    openDataInputStream()
    assert(9875645283333L === dis.readLong)
  }

  "readShort" in {
    os.writeShort(9875)
    os.close()
    openDataInputStream()
    assert(dis.readShort === 9875.toShort)
  }

  "readUnsignedByte" in {
    os.writeByte(-127.toByte)
    os.close()
    openDataInputStream()
    assert(129 === dis.readUnsignedByte)
  }

  "readUnsignedShort" in {
    os.writeShort(9875)
    os.close()
    openDataInputStream()
    assert(9875 === dis.readUnsignedShort)
  }

  "readUTF" in {
    os.writeUTF(unihw)
    os.close()
    openDataInputStream()
    assert(dis.available === unihw.length + 2)
    assert(dis.readUTF === unihw)
  }

  "readUTFLjava_io_DataInput" in {
    os.writeUTF(unihw)
    os.close()
    openDataInputStream()
    assert(dis.available === unihw.length + 2)
//    assert(DataInputStream.readUTF(dis) === unihw)
  }

  // TODO: DataInputStream.readUTF is not implemented in Scala-js
//  "readUTFLjava_io_DataInput: Regression test for HARMONY-5336" ignore {
//    new TestDataInputStream().readUTF
//  }

  "skipBytesI" in {
    val fileBytes = fileString.getBytes
    os.write(fileBytes)
    os.close()
    openDataInputStream()
    dis.skipBytes(100)
    val rbytes = new Array[Byte](fileString.length)
    dis.read(rbytes, 0, 50)
    dis.close()
    assert(new String(rbytes, 0, 50) === fileString.substring(100, 150))

    var skipped = 0
    openDataInputStream()
    try skipped = dis.skipBytes(50000)
    catch {
      case _: EOFException =>
    }
    assert(skipped === fileString.length)
  }
}

class TestDataInputStream extends DataInput {
  def readBoolean = false

  def readByte: Byte = 0.toByte

  def readChar: Char = 0.toChar

  def readDouble = 0.0

  def readFloat: Float = 0.0.toFloat

  def readFully(buffer: Array[Byte]): Unit = {}

  def readFully(buffer: Array[Byte], offset: Int, count: Int): Unit = {}

  def readInt = 0

  def readLine: String = null

  def readLong: Long = 0.toLong

  def readShort: Short = 0.toShort

  def readUnsignedByte = 0

  def readUnsignedShort = 0

  def readUTF: String = DataInputStream.readUTF(this)

  def skipBytes(count: Int) = 0
}
