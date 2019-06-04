package luni.java.io

import java.io.{
  BufferedWriter,
  ByteArrayOutputStream,
  IOException,
  OutputStreamWriter,
  StringWriter,
  Writer
}

import org.scalatest.{BeforeAndAfterEach, FunSuite}

class BufferedWriterTest extends FunSuite with BeforeAndAfterEach {
  private[io] var bw: BufferedWriter = _
  private[io] var sw: StringWriter   = _

  override def beforeEach(): Unit = {
    sw = new StringWriter()
    bw = new BufferedWriter(sw, 500)
  }

  override def afterEach(): Unit = {
    bw.close()
  }

  private class MockWriter extends Writer {
    private[io] val sb          = new StringBuffer
    private[io] var flushCalled = false
    def write(buf: Array[Char], off: Int, len: Int): Unit = {
      var i = off
      while ({ i < off + len }) {
        sb.append(buf(i))

        { i += 1; i - 1 }
      }
    }
    def close(): Unit = {
      // Empty
    }
    def flush(): Unit          = { flushCalled = true }
    def getWritten: String     = sb.toString
    def isFlushCalled: Boolean = flushCalled
  }

  var testString =
    "Test_All_Tests\nTest_java_io_BufferedInputStream\nTest_java_io_BufferedOutputStream\nTest_java_io_ByteArrayInputStream\nTest_java_io_ByteArrayOutputStream\nTest_java_io_DataInputStream\nTest_java_io_File\nTest_java_io_FileDescriptor\nTest_java_io_FileInputStream\nTest_java_io_FileNotFoundException\nTest_java_io_FileOutputStream\nTest_java_io_FilterInputStream\nTest_java_io_FilterOutputStream\nTest_java_io_InputStream\nTest_java_io_IOException\nTest_java_io_OutputStream\nTest_java_io_PrintStream\nTest_java_io_RandomAccessFile\nTest_java_io_SyncFailedException\nTest_java_lang_AbstractMethodError\nTest_java_lang_ArithmeticException\nTest_java_lang_ArrayIndexOutOfBoundsException\nTest_java_lang_ArrayStoreException\nTest_java_lang_Boolean\nTest_java_lang_Byte\nTest_java_lang_Character\nTest_java_lang_Class\nTest_java_lang_ClassCastException\nTest_java_lang_ClassCircularityError\nTest_java_lang_ClassFormatError\nTest_java_lang_ClassLoader\nTest_java_lang_ClassNotFoundException\nTest_java_lang_CloneNotSupportedException\nTest_java_lang_Double\nTest_java_lang_Error\nTest_java_lang_Exception\nTest_java_lang_ExceptionInInitializerError\nTest_java_lang_Float\nTest_java_lang_IllegalAccessError\nTest_java_lang_IllegalAccessException\nTest_java_lang_IllegalArgumentException\nTest_java_lang_IllegalMonitorStateException\nTest_java_lang_IllegalThreadStateException\nTest_java_lang_IncompatibleClassChangeError\nTest_java_lang_IndexOutOfBoundsException\nTest_java_lang_InstantiationError\nTest_java_lang_InstantiationException\nTest_java_lang_Integer\nTest_java_lang_InternalError\nTest_java_lang_InterruptedException\nTest_java_lang_LinkageError\nTest_java_lang_Long\nTest_java_lang_Math\nTest_java_lang_NegativeArraySizeException\nTest_java_lang_NoClassDefFoundError\nTest_java_lang_NoSuchFieldError\nTest_java_lang_NoSuchMethodError\nTest_java_lang_NullPointerException\nTest_java_lang_Number\nTest_java_lang_NumberFormatException\nTest_java_lang_Object\nTest_java_lang_OutOfMemoryError\nTest_java_lang_RuntimeException\nTest_java_lang_SecurityManager\nTest_java_lang_Short\nTest_java_lang_StackOverflowError\nTest_java_lang_String\nTest_java_lang_StringBuffer\nTest_java_lang_StringIndexOutOfBoundsException\nTest_java_lang_System\nTest_java_lang_Thread\nTest_java_lang_ThreadDeath\nTest_java_lang_ThreadGroup\nTest_java_lang_Throwable\nTest_java_lang_UnknownError\nTest_java_lang_UnsatisfiedLinkError\nTest_java_lang_VerifyError\nTest_java_lang_VirtualMachineError\nTest_java_lang_vm_Image\nTest_java_lang_vm_MemorySegment\nTest_java_lang_vm_ROMStoreException\nTest_java_lang_vm_VM\nTest_java_lang_Void\nTest_java_net_BindException\nTest_java_net_ConnectException\nTest_java_net_DatagramPacket\nTest_java_net_DatagramSocket\nTest_java_net_DatagramSocketImpl\nTest_java_net_InetAddress\nTest_java_net_NoRouteToHostException\nTest_java_net_PlainDatagramSocketImpl\nTest_java_net_PlainSocketImpl\nTest_java_net_Socket\nTest_java_net_SocketException\nTest_java_net_SocketImpl\nTest_java_net_SocketInputStream\nTest_java_net_SocketOutputStream\nTest_java_net_UnknownHostException\nTest_java_util_ArrayEnumerator\nTest_java_util_Date\nTest_java_util_EventObject\nTest_java_util_HashEnumerator\nTest_java_util_Hashtable\nTest_java_util_Properties\nTest_java_util_ResourceBundle\nTest_java_util_tm\nTest_java_util_Vector\n"

  test("ConstructorLjava_io_Writer") {
    assume(sw != null && bw != null)
    bw = new BufferedWriter(sw)
    sw.write("Hi")
    assert("Hi" == sw.toString)
  }

  test("ConstructorLjava_io_WriterI") {
    assert(true, "Used in tests")
  }

  test("close") {
    bw.close()
    assertThrows[IOException] {
      bw.write(testString)
    }
    assert(!sw.toString.equals(testString), "Write after close")

    // Regression test for HARMONY-4178
    val mw  = new MockWriter
    val bw2 = new BufferedWriter(mw)
    bw2.write('a')
    bw2.close()

    // flush should not be called on underlying stream
    assert(!mw.isFlushCalled, "Flush was called in the underlying stream")

    // on the other hand the BufferedWriter itself should flush the buffer
    assert("a" == mw.getWritten, "BufferdWriter do not flush itself before close")
  }

  test("close2") {
    val bw = new BufferedWriter(new OutputStreamWriter(new ByteArrayOutputStream))
    bw.close()
  }

  test("flush") {
    bw.write("This should not cause a flush")
    assert(sw.toString.equals(""), "Bytes written without flush")
    bw.flush()
    assert("This should not cause a flush" == sw.toString, "Bytes not flushed")
  }

  test("newLine") {
    val separator = System.getProperty("line.separator")
    bw.write("Hello")
    bw.newLine()
    bw.write("World")
    bw.flush()
    assert(sw.toString == "Hello" + separator + "World", "Incorrect string written: " + sw.toString)
  }

  test("write$CII") {
    val testCharArray = testString.toCharArray
    bw.write(testCharArray, 500, 1000)
    bw.flush()
    assert(sw.toString == testString.substring(500, 1500), "Incorrect string written")
  }

  test("write_$CII_Exception") {
    val bWriter                    = new BufferedWriter(sw)
    val nullCharArray: Array[Char] = null
    assertThrows[IndexOutOfBoundsException](bWriter.write(nullCharArray, -1, -1))
    assertThrows[IndexOutOfBoundsException](bWriter.write(nullCharArray, -1, 0))

    assertThrows[NullPointerException](bWriter.write(nullCharArray, 0, -1))
    assertThrows[NullPointerException](bWriter.write(nullCharArray, 0, 0))

    val testCharArray = testString.toCharArray
    bWriter.write(testCharArray, 0, 0)
    bWriter.write(testCharArray, testCharArray.length, 0)
    assertThrows[IndexOutOfBoundsException](
      bWriter.write(testCharArray, testCharArray.length + 1, 0)
    )
    bWriter.close()
    assertThrows[IOException](bWriter.write(nullCharArray, -1, 0))
  }

  test("writeI") {
    bw.write('T')
    assert(sw.toString.equals(""), "Char written without flush")
    bw.flush()
    assert("T" == sw.toString, "Incorrect char written")
  }

  test("writeLjava_lang_StringII") {
    bw.write(testString)
    bw.flush()
    assert(sw.toString == testString, "Incorrect string written")
  }

  test("write_LStringII_Exception") {
    val bWriter = new BufferedWriter(sw)
    bWriter.write(null.asInstanceOf[String], -1, -1)
    bWriter.write(null.asInstanceOf[String], -1, 0)
    bWriter.write(null.asInstanceOf[String], 0, -1)
    bWriter.write(null.asInstanceOf[String], 0, 0)
    assertThrows[NullPointerException](bWriter.write(null.asInstanceOf[String], -1, 1))

    bWriter.write(testString, 0, 0)
    bWriter.write(testString, testString.length, 0)
    bWriter.write(testString, testString.length + 1, 0)
    assertThrows[StringIndexOutOfBoundsException](
      bWriter.write(testString, testString.length + 1, 1)
    )

    bWriter.close()
    assertThrows[IOException](bWriter.write(null.asInstanceOf[String], -1, -1))
    assertThrows[IOException](bWriter.write(null.asInstanceOf[String], -1, 1))
    assertThrows[IOException](bWriter.write(testString, -1, -1))
  }
}
