package luni.java.io

import java.io.{
  BufferedReader,
  ByteArrayInputStream,
  CharArrayReader,
  IOException,
  InputStreamReader,
  Reader,
  StringReader
}

import org.scalatest.{BeforeAndAfterEach, FunSuite}

class BufferedReaderTest extends FunSuite with BeforeAndAfterEach {
  private[io] var br: BufferedReader = _
  private[io] val testString =
    "Test_All_Tests\nTest_java_io_BufferedInputStream\nTest_java_io_BufferedOutputStream\nTest_java_io_ByteArrayInputStream\nTest_java_io_ByteArrayOutputStream\nTest_java_io_DataInputStream\nTest_java_io_File\nTest_java_io_FileDescriptor\nTest_java_io_FileInputStream\nTest_java_io_FileNotFoundException\nTest_java_io_FileOutputStream\nTest_java_io_FilterInputStream\nTest_java_io_FilterOutputStream\nTest_java_io_InputStream\nTest_java_io_IOException\nTest_java_io_OutputStream\nTest_java_io_PrintStream\nTest_java_io_RandomAccessFile\nTest_java_io_SyncFailedException\nTest_java_lang_AbstractMethodError\nTest_java_lang_ArithmeticException\nTest_java_lang_ArrayIndexOutOfBoundsException\nTest_java_lang_ArrayStoreException\nTest_java_lang_Boolean\nTest_java_lang_Byte\nTest_java_lang_Character\nTest_java_lang_Class\nTest_java_lang_ClassCastException\nTest_java_lang_ClassCircularityError\nTest_java_lang_ClassFormatError\nTest_java_lang_ClassLoader\nTest_java_lang_ClassNotFoundException\nTest_java_lang_CloneNotSupportedException\nTest_java_lang_Double\nTest_java_lang_Error\nTest_java_lang_Exception\nTest_java_lang_ExceptionInInitializerError\nTest_java_lang_Float\nTest_java_lang_IllegalAccessError\nTest_java_lang_IllegalAccessException\nTest_java_lang_IllegalArgumentException\nTest_java_lang_IllegalMonitorStateException\nTest_java_lang_IllegalThreadStateException\nTest_java_lang_IncompatibleClassChangeError\nTest_java_lang_IndexOutOfBoundsException\nTest_java_lang_InstantiationError\nTest_java_lang_InstantiationException\nTest_java_lang_Integer\nTest_java_lang_InternalError\nTest_java_lang_InterruptedException\nTest_java_lang_LinkageError\nTest_java_lang_Long\nTest_java_lang_Math\nTest_java_lang_NegativeArraySizeException\nTest_java_lang_NoClassDefFoundError\nTest_java_lang_NoSuchFieldError\nTest_java_lang_NoSuchMethodError\nTest_java_lang_NullPointerException\nTest_java_lang_Number\nTest_java_lang_NumberFormatException\nTest_java_lang_Object\nTest_java_lang_OutOfMemoryError\nTest_java_lang_RuntimeException\nTest_java_lang_SecurityManager\nTest_java_lang_Short\nTest_java_lang_StackOverflowError\nTest_java_lang_String\nTest_java_lang_StringBuffer\nTest_java_lang_StringIndexOutOfBoundsException\nTest_java_lang_System\nTest_java_lang_Thread\nTest_java_lang_ThreadDeath\nTest_java_lang_ThreadGroup\nTest_java_lang_Throwable\nTest_java_lang_UnknownError\nTest_java_lang_UnsatisfiedLinkError\nTest_java_lang_VerifyError\nTest_java_lang_VirtualMachineError\nTest_java_lang_vm_Image\nTest_java_lang_vm_MemorySegment\nTest_java_lang_vm_ROMStoreException\nTest_java_lang_vm_VM\nTest_java_lang_Void\nTest_java_net_BindException\nTest_java_net_ConnectException\nTest_java_net_DatagramPacket\nTest_java_net_DatagramSocket\nTest_java_net_DatagramSocketImpl\nTest_java_net_InetAddress\nTest_java_net_NoRouteToHostException\nTest_java_net_PlainDatagramSocketImpl\nTest_java_net_PlainSocketImpl\nTest_java_net_Socket\nTest_java_net_SocketException\nTest_java_net_SocketImpl\nTest_java_net_SocketInputStream\nTest_java_net_SocketOutputStream\nTest_java_net_UnknownHostException\nTest_java_util_ArrayEnumerator\nTest_java_util_Date\nTest_java_util_EventObject\nTest_java_util_HashEnumerator\nTest_java_util_Hashtable\nTest_java_util_Properties\nTest_java_util_ResourceBundle\nTest_java_util_tm\nTest_java_util_Vector\n"

  override def afterEach(): Unit = {
    try br.close()
    catch {
      case _: Exception =>
    }
  }

  test("readLine_IgnoresEbcdic85Characters") {
    assertLines("A\u0085B", "A\u0085B")
  }

  test("readLine_Separators") {
    assertLines("A\nB\nC", "A", "B", "C")
    assertLines("A\rB\rC", "A", "B", "C")
    assertLines("A\r\nB\r\nC", "A", "B", "C")
    assertLines("A\n\rB\n\rC", "A", "", "B", "", "C")
    assertLines("A\n\nB\n\nC", "A", "", "B", "", "C")
    assertLines("A\r\rB\r\rC", "A", "", "B", "", "C")
    assertLines("A\n\n", "A", "")
    assertLines("A\n\r", "A", "")
    assertLines("A\r\r", "A", "")
    assertLines("A\r\n", "A")
    assertLines("A\r\n\r\n", "A", "")
  }

  private def assertLines(in: String, lines: String*): Unit = {
    val bufferedReader = new BufferedReader(new StringReader(in))
    for (line <- lines) {
      assert(line == bufferedReader.readLine)
    }
    assert(bufferedReader.readLine == null)
  }

  test("ConstructorLjava_io_Reader") {
    assert(true, "Used in tests")
  }

  test("ConstructorLjava_io_ReaderI") {
    assert(true, "Used in tests")
  }

  test("close") {
    br = new BufferedReader(new StringReader(testString))
    br.close()
    assertThrows[IOException] {
      br.read
    }
  }

  test("markI") {
    br = new BufferedReader(new StringReader(testString))
    br.skip(500)
    br.mark(1000)
    br.skip(250)
    br.reset()
    val buf = new Array[Char](testString.length)
    br.read(buf, 0, 500)
    assert(testString.substring(500, 1000) == new String(buf, 0, 500))

    br = new BufferedReader(new StringReader(testString), 800)
    br.skip(500)
    br.mark(250)
    br.read(buf, 0, 1000)
    assertThrows[IOException] {
      br.reset()
    }

    val chars = new Array[Char](256)
    for (i <- 0 until 256) {
      chars(i) = i.toChar
    }

    var in = new BufferedReader(new StringReader(new String(chars)), 12)
    in.skip(6)
    in.mark(14)
    in.read(new Array[Char](14), 0, 14)
    in.reset()
    assert((in.read == 6.toChar) && (in.read == 7.toChar))

    in = new BufferedReader(new StringReader(new String(chars)), 12)
    in.skip(6)
    in.mark(8)
    in.skip(7)
    in.reset()
    assert((in.read == 6.toChar) && (in.read == 7.toChar))

    br = new BufferedReader(new StringReader("01234"), 2)
    br.mark(3)
    var carray = new Array[Char](3)
    var result = br.read(carray)
    assert(3 == result)
    assert('0' == carray(0))
    assert('1' == carray(1))
    assert('2' == carray(2))
    assert('3' == br.read)
    br = new BufferedReader(new StringReader("01234"), 2)
    br.mark(3)
    carray = new Array[Char](4)
    result = br.read(carray)
    assert(4 == result)
    assert('0' == carray(0))
    assert('1' == carray(1))
    assert('2' == carray(2))
    assert('3' == carray(3))
    assert('4' == br.read)
    assert(-1 == br.read)
  }

  // OpenJDK 11 does not pass this
  ignore("mark_huge") {
    val reader = new BufferedReader(new StringReader("01234"))
    reader.mark(Integer.MAX_VALUE)
    reader.read()
    reader.close()
  }

  test("markSupported") {
    br = new BufferedReader(new StringReader(testString))
    assert(br.markSupported)
  }

  test("read") {
    br = new BufferedReader(new StringReader(testString))
    val r = br.read
    assert(testString.charAt(0) == r)
    br = new BufferedReader(new StringReader(new String(Array[Char]('\u8765'))))
    assert(br.read == '\u8765')

    val chars = new Array[Char](256)
    for (i <- 0 until 256) {
      chars(i) = i.toChar
    }
    val in = new BufferedReader(new StringReader(new String(chars)), 12)
    assert(0 == in.read) // Fill the buffer
    val buf = new Array[Char](14)
    in.read(buf, 0, 14) // Read greater than the buffer
    // On JS, conversion to String causes char corruption on test report, so comparing via sameElements instead of String
    // assert(new String(buf) == new String(chars, 1, 14), "Wrong block read data")
    assert(buf.sameElements(chars.slice(1, 15)), "Wrong block read data")
    assert(15 == in.read) // Check next byte
  }

  // FIXME: Issue in CharArrayReader ?
  ignore("regression test for HARMONY-841"){
    assert(new BufferedReader(new CharArrayReader(new Array[Char](5), 1, 0), 2).read == -1)
  }

  test("read$CII") {
    val ca    = new Array[Char](2)
    var toRet = new BufferedReader(new StringReader(new String(new Array[Byte](0))))
    assertThrows[NullPointerException] {
      toRet.read(null, 1, 0)
    }
    toRet.close()
    assertThrows[IOException] {
      toRet.read(null, 1, 0)
    }
    assertThrows[IOException] {
      toRet.read(ca, 0, 0)
    }
    assertThrows[IOException] {
      toRet.read(ca, 1, 5)
    }

    // Test to ensure that a drained stream returns 0 at EOF
    toRet = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(new Array[Byte](2))))
    assert(2 == toRet.read(ca, 0, 2), "Emptying the reader should return two bytes")
    assert(-1 == toRet.read(ca, 0, 2), "EOF on a reader should be -1")
    assert(0 == toRet.read(ca, 0, 0), "Reading zero bytes at EOF should work")

    // Test for method int java.io.BufferedReader.read(char [], int, int)
    val buf = new Array[Char](testString.length)
    br = new BufferedReader(new StringReader(testString))
    br.read(buf, 50, 500)
    assert(new String(buf, 50, 500) == testString.substring(0, 500))

    val bufin = new BufferedReader(new Reader() {
      private[io] val size     = 2
      private[io] var pos      = 0
      private[io] val contents = new Array[Char](size)

      override def read: Int = {
        if (pos >= size) throw new IOException("Read past end of data")
        contents({
          pos += 1; pos - 1
        })
      }

      override def read(buf: Array[Char], off: Int, len: Int): Int = {
        if (pos >= size) throw new IOException("Read past end of data")
        var toRead = len
        if (toRead > (size - pos)) toRead = size - pos
        System.arraycopy(contents, pos, buf, off, toRead)
        pos += toRead
        toRead
      }

      override def ready: Boolean = size - pos > 0

      override def close(): Unit = {}
    })

    bufin.read
    val result = bufin.read(new Array[Char](2), 0, 2)
    assert(result == 1)

// TODO: PipedReader
//    //regression for HARMONY-831
//    assertThrows[IndexOutOfBoundsException] {
//      new BufferedReader(new PipedReader(), 9).read(Array[Char](), 7, 0)
//    }

    // Regression for HARMONY-54
    val ch     = Array[Char]()
    val reader = new BufferedReader(new CharArrayReader(ch))
    assertThrows[NullPointerException] {
      reader.read(null, 1, 0)
    }

    reader.close()
    assertThrows[IOException] {
      reader.read(null, 1, 0)
    }
    assertThrows[IOException] {
      reader.read(ch, 0, 42)
    }
  }

  test("read_$CII_Exception") {
    br = new BufferedReader(new StringReader(testString))
    val nullCharArray: Array[Char] = null
    val charArray                  = testString.toCharArray
    assertThrows[IndexOutOfBoundsException] {
      br.read(nullCharArray, -1, -1)
    }
    assertThrows[IndexOutOfBoundsException] {
      br.read(nullCharArray, -1, 0)
    }
    assertThrows[NullPointerException] {
      br.read(nullCharArray, 0, -1)
    }
    assertThrows[NullPointerException] {
      br.read(nullCharArray, 0, 0)
    }
    assertThrows[NullPointerException] {
      br.read(nullCharArray, 0, 1)
    }
    assertThrows[IndexOutOfBoundsException] {
      br.read(charArray, -1, -1)
    }
    assertThrows[IndexOutOfBoundsException] {
      br.read(charArray, -1, 0)
    }
    br.read(charArray, 0, 0)
    br.read(charArray, 0, charArray.length)
    br.read(charArray, charArray.length, 0)

    assertThrows[IndexOutOfBoundsException] {
      br.read(charArray, charArray.length + 1, 0)
    }
    assertThrows[IndexOutOfBoundsException] {
      br.read(charArray, charArray.length + 1, 1)
    }
    br.close()
    assertThrows[IOException] {
      br.read(nullCharArray, -1, -1)
    }
    assertThrows[IOException] {
      br.read(charArray, -1, 0)
    }
    assertThrows[IOException] {
      br.read(charArray, 0, -1)
    }
  }

  test("readLine") {
    br = new BufferedReader(new StringReader(testString))
    val r = br.readLine
    assert("Test_All_Tests" == r)
  }

  test("ready") {
    br = new BufferedReader(new StringReader(testString))
    assert(br.ready)
  }

  test("reset") {
    br = new BufferedReader(new StringReader(testString))
    br.skip(500)
    br.mark(900)
    br.skip(500)
    br.reset()
    val buf = new Array[Char](testString.length)
    br.read(buf, 0, 500)
    assert(testString.substring(500, 1000) == new String(buf, 0, 500))

    br = new BufferedReader(new StringReader(testString))
    br.skip(500)
    assertThrows[IOException] {
      br.reset()
    }
  }

  test("reset_IOException") {
    val expected = Array[Int]('1', '2', '3', '4', '5', '6', '7', '8', '9', '0', -1)
    br = new BufferedReader(new StringReader("1234567890"), 9)
    br.mark(9)
    for (i <- 0 until 11) {
      assert(expected(i) == br.read)
    }
    assertThrows[IOException] {
      br.reset()
    }
    for (i <- 0 until 11) {
      assert(-1 == br.read)
    }
    br = new BufferedReader(new StringReader("1234567890"))
    br.mark(10)
    for (i <- 0 until 10) {
      assert(expected(i) == br.read)
    }
    br.reset()
    for (i <- 0 until 11) {
      assert(expected(i) == br.read)
    }
  }

  test("skipJ") {
    br = new BufferedReader(new StringReader(testString))
    br.skip(500)
    val buf = new Array[Char](testString.length)
    br.read(buf, 0, 500)
    assert(testString.substring(500, 1000) == new String(buf, 0, 500))
  }
}
