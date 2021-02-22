package luni.java.io

import java.io.{
  BufferedInputStream,
  ByteArrayInputStream,
  File,
  FileInputStream,
  FileOutputStream,
  IOException,
  InputStream,
  InputStreamReader,
  OutputStream
}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AnyFunSuite
import support.TestSupport

class BufferedInputStreamTest extends AnyFunSuite with BeforeAndAfterEach with TestSupport {
  var fileName: String = _

  private var is: BufferedInputStream = _

  private var isFile: FileInputStream = _

  private[io] val ibuf = new Array[Byte](4096)

  override protected def beforeEach(): Unit = {
    fileName = System.getProperty("user.dir", "./")
    val separator = System.getProperty("file.separator")
    fileName =
      if (fileName.charAt(fileName.length - 1) == separator.charAt(0))
        getNewPlatformFile(fileName, "input.tst")
      else
        getNewPlatformFile(fileName + separator, "input.tst")
    val fos: OutputStream = new FileOutputStream(fileName)
    fos.write(fileString.getBytes)
    fos.close()
    isFile = new FileInputStream(fileName)
    is = new BufferedInputStream(isFile)
  }

  override protected def afterEach(): Unit = {
    try is.close()
    catch { case _: Exception => }
    try {
      val f = new File(fileName)
      f.delete
    } catch {
      case _: Exception =>
    }
  }

  var fileString =
    "Test_All_Tests\nTest_BufferedInputStream\nTest_java_io_BufferedOutputStream\nTest_java_io_ByteArrayInputStream\nTest_java_io_ByteArrayOutputStream\nTest_java_io_DataInputStream\nTest_java_io_File\nTest_java_io_FileDescriptor\nTest_java_io_FileInputStream\nTest_java_io_FileNotFoundException\nTest_java_io_FileOutputStream\nTest_java_io_FilterInputStream\nTest_java_io_FilterOutputStream\nTest_java_io_InputStream\nTest_java_io_IOException\nTest_java_io_OutputStream\nTest_java_io_PrintStream\nTest_java_io_RandomAccessFile\nTest_java_io_SyncFailedException\nTest_java_lang_AbstractMethodError\nTest_java_lang_ArithmeticException\nTest_java_lang_ArrayIndexOutOfBoundsException\nTest_java_lang_ArrayStoreException\nTest_java_lang_Boolean\nTest_java_lang_Byte\nTest_java_lang_Character\nTest_java_lang_Class\nTest_java_lang_ClassCastException\nTest_java_lang_ClassCircularityError\nTest_java_lang_ClassFormatError\nTest_java_lang_ClassLoader\nTest_java_lang_ClassNotFoundException\nTest_java_lang_CloneNotSupportedException\nTest_java_lang_Double\nTest_java_lang_Error\nTest_java_lang_Exception\nTest_java_lang_ExceptionInInitializerError\nTest_java_lang_Float\nTest_java_lang_IllegalAccessError\nTest_java_lang_IllegalAccessException\nTest_java_lang_IllegalArgumentException\nTest_java_lang_IllegalMonitorStateException\nTest_java_lang_IllegalThreadStateException\nTest_java_lang_IncompatibleClassChangeError\nTest_java_lang_IndexOutOfBoundsException\nTest_java_lang_InstantiationError\nTest_java_lang_InstantiationException\nTest_java_lang_Integer\nTest_java_lang_InternalError\nTest_java_lang_InterruptedException\nTest_java_lang_LinkageError\nTest_java_lang_Long\nTest_java_lang_Math\nTest_java_lang_NegativeArraySizeException\nTest_java_lang_NoClassDefFoundError\nTest_java_lang_NoSuchFieldError\nTest_java_lang_NoSuchMethodError\nTest_java_lang_NullPointerException\nTest_java_lang_Number\nTest_java_lang_NumberFormatException\nTest_java_lang_Object\nTest_java_lang_OutOfMemoryError\nTest_java_lang_RuntimeException\nTest_java_lang_SecurityManager\nTest_java_lang_Short\nTest_java_lang_StackOverflowError\nTest_java_lang_String\nTest_java_lang_StringBuffer\nTest_java_lang_StringIndexOutOfBoundsException\nTest_java_lang_System\nTest_java_lang_Thread\nTest_java_lang_ThreadDeath\nTest_java_lang_ThreadGroup\nTest_java_lang_Throwable\nTest_java_lang_UnknownError\nTest_java_lang_UnsatisfiedLinkError\nTest_java_lang_VerifyError\nTest_java_lang_VirtualMachineError\nTest_java_lang_vm_Image\nTest_java_lang_vm_MemorySegment\nTest_java_lang_vm_ROMStoreException\nTest_java_lang_vm_VM\nTest_java_lang_Void\nTest_java_net_BindException\nTest_java_net_ConnectException\nTest_java_net_DatagramPacket\nTest_java_net_DatagramSocket\nTest_java_net_DatagramSocketImpl\nTest_java_net_InetAddress\nTest_java_net_NoRouteToHostException\nTest_java_net_PlainDatagramSocketImpl\nTest_java_net_PlainSocketImpl\nTest_java_net_Socket\nTest_java_net_SocketException\nTest_java_net_SocketImpl\nTest_java_net_SocketInputStream\nTest_java_net_SocketOutputStream\nTest_java_net_UnknownHostException\nTest_java_util_ArrayEnumerator\nTest_java_util_Date\nTest_java_util_EventObject\nTest_java_util_HashEnumerator\nTest_java_util_Hashtable\nTest_java_util_Properties\nTest_java_util_ResourceBundle\nTest_java_util_tm\nTest_java_util_Vector\n"

  test("ConstructorLjava_io_InputStream") {
    assertThrows[IOException] {
      val str = new BufferedInputStream(null)
      str.read()
    }
  }

  test("ConstructorLjava_io_InputStreamI") {
    assertThrows[IOException] {
      val str = new BufferedInputStream(null, 1)
      str.read()
    }

    // Test for method java.io.BufferedInputStream(java.io.InputStream, int)
    // Create buffer with exact size of file
    is = new BufferedInputStream(isFile, this.fileString.length)
    // Ensure buffer gets filled by evaluating one read
    is.read()
    // Close underlying FileInputStream, all but 1 buffered bytes should still be available.
    isFile.close()
    // Read the remaining buffered characters, no IOException should occur.
    is.skip(this.fileString.length - 2)
    is.read()
    assertThrows[IOException] {
      // is.read should now throw an exception because it will have to be filled.
      is.read
    }
  }

  test("regression test for harmony-2407") {
    new MockBufferedInputStream(null)
    assert(MockBufferedInputStream.buf !== null)
    MockBufferedInputStream.buf = null
    new MockBufferedInputStream(null, 100)
    assert(MockBufferedInputStream.buf !== null)
  }

  test("available") {
    assert(is.available === fileString.length)
    // Test that a closed stream throws an IOE for available()
    val bis = new BufferedInputStream(
      new ByteArrayInputStream(Array[Byte]('h', 'e', 'l', 'l', 'o', ' ', 't', 'i', 'm'))
    )
    val available = bis.available
    bis.close()
    assert(available !== 0)
    assertThrows[IOException] {
      bis.available()
    }
  }

  // NOTE: Removed Thread since no multi-thread in Scala.js
  test("close") {
    new BufferedInputStream(isFile).close()
    // regression for HARMONY-667
    val buf = new BufferedInputStream(null, 5)
    buf.close()

    val in = new InputStream() {
      override def read                                                  = 1
      override def read(buf: Array[Byte], offset: Int, length: Int): Int = 1
      override def close(): Unit                                         = {}
    }
    val bufin = new BufferedInputStream(in, 10)
    bufin.close()
    assertThrows[IOException] {
      bufin.read(new Array[Byte](100), 0, 100)
    }
  }

  // FIXME: do not end :(
  ignore("markI") {
    val buf1 = new Array[Byte](100)
    val buf2 = new Array[Byte](100)
    is.skip(3000)
    is.mark(1000)
    is.read(buf1, 0, buf1.length)
    is.reset()
    is.read(buf2, 0, buf2.length)
    is.reset()
    assert(new String(buf1, 0, buf1.length) === new String(buf2, 0, buf2.length))

    val bytes = new Array[Byte](256)
    for (i <- 0 until 256) {
      bytes(i) = i.toByte
    }
    var in = new BufferedInputStream(new ByteArrayInputStream(bytes), 12)
    in.skip(6)
    in.mark(14)
    in.read(new Array[Byte](14), 0, 14)
    in.reset()
    assert(in.read === 6 && in.read === 7)

    in = new BufferedInputStream(new ByteArrayInputStream(bytes), 12)
    in.skip(6)
    in.mark(8)
    in.skip(7)
    in.reset()
    assert(in.read === 6 && in.read === 7)
  }

  test("markI and read bytes") {
    var buf = new BufferedInputStream(new ByteArrayInputStream(Array[Byte](0, 1, 2, 3, 4)), 2)
    buf.mark(3)
    var bytes  = new Array[Byte](3)
    var result = buf.read(bytes)
    assert(3 === result)
    assert(0 === bytes(0))
    assert(1 === bytes(1))
    assert(2 === bytes(2))
    assert(3 === buf.read)

    buf = new BufferedInputStream(new ByteArrayInputStream(Array[Byte](0, 1, 2, 3, 4)), 2)
    buf.mark(3)
    bytes = new Array[Byte](4)
    result = buf.read(bytes)
    assert(4 === result)
    assert(0 === bytes(0))
    assert(1 === bytes(1))
    assert(2 === bytes(2))
    assert(3 === bytes(3))
    assert(4 === buf.read)
    assert(-1 === buf.read)
  }

  test("markI: Massive readLimit") {
    val buf = new BufferedInputStream(new ByteArrayInputStream(Array[Byte](0, 1, 2, 3, 4)), 2)
    buf.mark(Integer.MAX_VALUE)
    buf.read()
    buf.close()
  }

  test("markSupported") {
    assert(is.markSupported)
  }

  test("read") {
    val isr = new InputStreamReader(is)
    assert(isr.read === fileString.charAt(0))

    val bytes = new Array[Byte](256)
    for (i <- 0 until 256) {
      bytes(i) = i.toByte
    }
    val in = new BufferedInputStream(new ByteArrayInputStream(bytes), 12)
    assert(0 === in.read) // Fill the buffer

    val buf = new Array[Byte](14)
    in.read(buf, 0, 14) // Read greater than the buffer
    assert(new String(buf, 0, 14) === new String(bytes, 1, 14))
    assert(15 === in.read) // Check next byte
  }

  test("read$BII_Exception") {
    val bis = new BufferedInputStream(null)
    assertThrows[NullPointerException] {
      bis.read(null, -1, -1)
    }
    assertThrows[IndexOutOfBoundsException] {
      bis.read(new Array[Byte](0), -1, -1)
    }
    assertThrows[IndexOutOfBoundsException] {
      bis.read(new Array[Byte](0), 1, -1)
    }
    assertThrows[IndexOutOfBoundsException] {
      bis.read(new Array[Byte](0), 1, 1)
    }
    bis.close()
    assertThrows[IOException] {
      bis.read(null, -1, -1)
    }
  }

  // FIXME: do not end :(
  ignore("read$BII") {
    val buf1 = new Array[Byte](100)
    is.skip(3000)
    is.mark(1000)
    is.read(buf1, 0, buf1.length)
    assert(new String(buf1, 0, buf1.length) === fileString.substring(3000, 3100))

    val bufin = new BufferedInputStream(new InputStream() {
      val size     = 2
      var pos      = 0
      val contents = new Array[Byte](size)

      override def read: Int = {
        if (pos >= size) throw new IOException("Read past end of data")
        contents({
          pos += 1; pos - 1
        })
      }

      override def read(buf: Array[Byte], off: Int, len: Int): Int = {
        if (pos >= size) throw new IOException("Read past end of data")
        var toRead = len
        if (toRead > available) toRead = available
        System.arraycopy(contents, pos, buf, off, toRead)
        pos += toRead
        toRead
      }

      override def available: Int = size - pos
    })
    bufin.read()
    val result = bufin.read(new Array[Byte](2), 0, 2)
    assert(result === 1)
  }

  test("reset") {
    val buf1 = new Array[Byte](10)
    val buf2 = new Array[Byte](10)
    is.mark(2000)
    is.read(buf1, 0, 10)
    is.reset()
    is.read(buf2, 0, 10)
    is.reset()
    assert(new String(buf1, 0, buf1.length) === new String(buf2, 0, buf2.length))

    val bIn = new BufferedInputStream(new ByteArrayInputStream("1234567890".getBytes))
    bIn.mark(10)
    for (_ <- 0 until 11) {
      bIn.read()
    }
    bIn.reset()
    assert(bIn.read().toChar === '1')
  }

  test("reset_Exception") {
    val bis = new BufferedInputStream(null)

    // throws IOException with message "Mark has been invalidated"
    assertThrows[IOException] {
      bis.reset()
    }

    // does not throw IOException
    bis.mark(1)
    bis.reset()
    bis.close()

    // throws IOException with message "stream is closed"
    assertThrows[IOException] {
      bis.reset()
    }
  }

  test("reset_scenario1") {
    val input  = "12345678900".getBytes
    val buffis = new BufferedInputStream(new ByteArrayInputStream(input))
    buffis.read()
    buffis.mark(5)
    buffis.skip(5)
    buffis.reset()
  }

  test("reset_scenario2") {
    val input  = "12345678900".getBytes
    val buffis = new BufferedInputStream(new ByteArrayInputStream(input))
    buffis.mark(5)
    buffis.skip(6)
    buffis.read()
    buffis.reset()
  }

  test("skipJ") {
    val buf1 = new Array[Byte](10)
    is.mark(2000)
    is.skip(1000)
    is.read(buf1, 0, buf1.length)
    is.reset()
    assert(new String(buf1, 0, buf1.length) === fileString.substring(1000, 1010))
  }

  test("skipJ: regression for HARMONY-667") {
    assertThrows[IOException] {
      val buf = new BufferedInputStream(null, 5)
      buf.skip(10)
    }
  }

  test("skip_NullInputStream") {
    val buf = new BufferedInputStream(null, 5)
    assert(0 === buf.skip(0))
  }
}

object MockBufferedInputStream {
  var buf: Array[Byte] = _
}

class MockBufferedInputStream(is: InputStream, size: Int) extends BufferedInputStream(is, size) {
  MockBufferedInputStream.buf = this.buf

  def this(is: InputStream) = {
    this(is, 4096)
  }
}
