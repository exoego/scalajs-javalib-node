package luni.java.io

import java.io.{
  File,
  FileDescriptor,
  FileInputStream,
  FileNotFoundException,
  FileOutputStream,
  IOException
}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AnyFunSuite
import support.Support_PlatformFile

import java.nio.channels.ClosedChannelException

class FileOutputStreamTest extends AnyFunSuite with BeforeAndAfterEach with Support_PlatformFile {
  private[io] var fileName: String = _

  private[io] var fos: FileOutputStream = _

  private[io] var fis: FileInputStream = _

  private[io] var f: File = _

  private[io] var bytes: Array[Byte] = _

  private[io] val ibuf = new Array[Byte](4096)

  var fileString =
    "Test_All_Tests\nTest_java_io_BufferedInputStream\nTest_java_io_BufferedOutputStream\nTest_java_io_ByteArrayInputStream\nTest_java_io_ByteArrayOutputStream\nTest_java_io_DataInputStream\nTest_java_io_File\nTest_java_io_FileDescriptor\nTest_java_io_FileInputStream\nTest_java_io_FileNotFoundException\nTest_FileOutputStream\nTest_java_io_FilterInputStream\nTest_java_io_FilterOutputStream\nTest_java_io_InputStream\nTest_java_io_IOException\nTest_java_io_OutputStream\nTest_java_io_PrintStream\nTest_java_io_RandomAccessFile\nTest_java_io_SyncFailedException\nTest_java_lang_AbstractMethodError\nTest_java_lang_ArithmeticException\nTest_java_lang_ArrayIndexOutOfBoundsException\nTest_java_lang_ArrayStoreException\nTest_java_lang_Boolean\nTest_java_lang_Byte\nTest_java_lang_Character\nTest_java_lang_Class\nTest_java_lang_ClassCastException\nTest_java_lang_ClassCircularityError\nTest_java_lang_ClassFormatError\nTest_java_lang_ClassLoader\nTest_java_lang_ClassNotFoundException\nTest_java_lang_CloneNotSupportedException\nTest_java_lang_Double\nTest_java_lang_Error\nTest_java_lang_Exception\nTest_java_lang_ExceptionInInitializerError\nTest_java_lang_Float\nTest_java_lang_IllegalAccessError\nTest_java_lang_IllegalAccessException\nTest_java_lang_IllegalArgumentException\nTest_java_lang_IllegalMonitorStateException\nTest_java_lang_IllegalThreadStateException\nTest_java_lang_IncompatibleClassChangeError\nTest_java_lang_IndexOutOfBoundsException\nTest_java_lang_InstantiationError\nTest_java_lang_InstantiationException\nTest_java_lang_Integer\nTest_java_lang_InternalError\nTest_java_lang_InterruptedException\nTest_java_lang_LinkageError\nTest_java_lang_Long\nTest_java_lang_Math\nTest_java_lang_NegativeArraySizeException\nTest_java_lang_NoClassDefFoundError\nTest_java_lang_NoSuchFieldError\nTest_java_lang_NoSuchMethodError\nTest_java_lang_NullPointerException\nTest_java_lang_Number\nTest_java_lang_NumberFormatException\nTest_java_lang_Object\nTest_java_lang_OutOfMemoryError\nTest_java_lang_RuntimeException\nTest_java_lang_SecurityManager\nTest_java_lang_Short\nTest_java_lang_StackOverflowError\nTest_java_lang_String\nTest_java_lang_StringBuffer\nTest_java_lang_StringIndexOutOfBoundsException\nTest_java_lang_System\nTest_java_lang_Thread\nTest_java_lang_ThreadDeath\nTest_java_lang_ThreadGroup\nTest_java_lang_Throwable\nTest_java_lang_UnknownError\nTest_java_lang_UnsatisfiedLinkError\nTest_java_lang_VerifyError\nTest_java_lang_VirtualMachineError\nTest_java_lang_vm_Image\nTest_java_lang_vm_MemorySegment\nTest_java_lang_vm_ROMStoreException\nTest_java_lang_vm_VM\nTest_java_lang_Void\nTest_java_net_BindException\nTest_java_net_ConnectException\nTest_java_net_DatagramPacket\nTest_java_net_DatagramSocket\nTest_java_net_DatagramSocketImpl\nTest_java_net_InetAddress\nTest_java_net_NoRouteToHostException\nTest_java_net_PlainDatagramSocketImpl\nTest_java_net_PlainSocketImpl\nTest_java_net_Socket\nTest_java_net_SocketException\nTest_java_net_SocketImpl\nTest_java_net_SocketInputStream\nTest_java_net_SocketOutputStream\nTest_java_net_UnknownHostException\nTest_java_util_ArrayEnumerator\nTest_java_util_Date\nTest_java_util_EventObject\nTest_java_util_HashEnumerator\nTest_java_util_Hashtable\nTest_java_util_Properties\nTest_java_util_ResourceBundle\nTest_java_util_tm\nTest_java_util_Vector\n"

  override def beforeEach(): Unit = {
    bytes = new Array[Byte](10)
    for (i <- bytes.indices) {
      bytes(i) = i.toByte
    }
  }

  override def afterEach(): Unit = {
    if (f != null) f.delete()
    if (fis != null) fis.close()
    if (fos != null) fos.close()
  }

  test("ConstructorLjava_io_File") {
    f = new File(System.getProperty("user.home"), "fos.tst")
    fos = new FileOutputStream(f)
  }

  test("ConstructorLjava_io_FileDescriptor") {
    f = new File(System.getProperty("user.home"), "fos.tst")
    fileName = f.getAbsolutePath
    fos = new FileOutputStream(fileName)
    fos.write('l')
    fos.close()
    fis = new FileInputStream(fileName)
    fos = new FileOutputStream(fis.getFD)
    fos.close()
    fis.close()
  }

  test("ConstructorLjava_lang_String") {
    f = new File(System.getProperty("user.home"), "fos.tst")
    fileName = f.getAbsolutePath
    fos = new FileOutputStream(fileName)
    // Regression test for HARMONY-4012
    new FileOutputStream("nul")
  }

  test("ConstructorLjava_lang_StringZ") {
    f = new File(System.getProperty("user.home"), "fos.tst")
    fos = new FileOutputStream(f.getPath(), false)
    fos.write("HI".getBytes, 0, 2)
    fos.close()
    fos = new FileOutputStream(f.getPath(), true)
    fos.write(fileString.getBytes)
    fos.close()
    val buf = new Array[Byte](fileString.length + 2)
    fis = new FileInputStream(f.getPath())
    fis.read(buf, 0, buf.length)
    assert(new String(buf, 0, buf.length) == ("HI" + fileString))
  }

  test("ConstructorLjava_lang_String_I") {
    assertThrows[FileNotFoundException] {
      fos = new FileOutputStream("")
    }
    if (fos != null) fos.close()
  }

  test("ConstructorLjava_lang_String_I_2") {
    assertThrows[FileNotFoundException] {
      fos = new FileOutputStream(new File(""))
    }
    if (fos != null) fos.close()
  }

  test("close") {
    f = new File(System.getProperty("user.home", "./"), "output.tst")
    fos = new FileOutputStream(f.getPath())
    fos.close()
    assertThrows[IOException] {
      fos.write(fileString.getBytes)
    }
  }

  test("getFD") {
    f = new File(System.getProperty("user.home", "./"), "testfd")
    fileName = f.getAbsolutePath()
    fos = new FileOutputStream(f)
    assert(fos.getFD.valid)
    fos.close()
    assert(!fos.getFD.valid)
  }

  test("write$B") {
    f = new File(System.getProperty("user.home"), "output.tst")
    fos = new FileOutputStream(f.getPath())
    fos.write(fileString.getBytes)
    fis = new FileInputStream(f.getPath())
    val rbytes = new Array[Byte](4000)
    fis.read(rbytes, 0, fileString.length)
    assert(new String(rbytes, 0, fileString.length) == fileString)
  }

  test("write$BII") {
    f = new File(System.getProperty("user.home"), "output.tst")
    fos = new FileOutputStream(f.getPath())
    fos.write(fileString.getBytes, 0, fileString.length)
    fis = new FileInputStream(f.getPath())
    val rbytes = new Array[Byte](4000)
    fis.read(rbytes, 0, fileString.length)
    assert(new String(rbytes, 0, fileString.length) == fileString)
  }

  test("write$BII: Regression test for HARMONY-285") {
    f = new File("FileOutputStream.tmp")
    fos = new FileOutputStream(f)
    assertThrows[NullPointerException] {
      fos.write(null, 0, 0)
    }
  }

  test("writeI") {
    f = new File(System.getProperty("user.home"), "output.tst")
    fos = new FileOutputStream(f.getPath())
    fos.write('t')
    fis = new FileInputStream(f.getPath())
    assert('t' == fis.read)
  }

  test("write$BII2: Regression for HARMONY-437") {
    f = new File(System.getProperty("user.home"), "output.tst")
    fos = new FileOutputStream(f.getPath())
    assertThrows[NullPointerException] {
      fos.write(null, 1, 1)
    }
    assertThrows[IndexOutOfBoundsException] {
      fos.write(new Array[Byte](1), -1, 1)
    }
    assertThrows[IndexOutOfBoundsException] {
      fos.write(new Array[Byte](1), 0, -1)
    }
    assertThrows[IndexOutOfBoundsException] {
      fos.write(new Array[Byte](1), 0, 5)
    }
    assertThrows[IndexOutOfBoundsException] {
      fos.write(new Array[Byte](10), Integer.MAX_VALUE, 5)
    }
    assertThrows[IndexOutOfBoundsException] {
      fos.write(new Array[Byte](10), 5, Integer.MAX_VALUE)
    }
  }

  test("write$BII3: Regression for HARMONY-834") {
    // no exception expected
    new FileOutputStream(new FileDescriptor).write(new Array[Byte](1), 0, 0)
  }

  test("getChannel* Regression for HARMONY-508") {
    assume(!isScalaJS, "not implemented yey")
    val tmpfile = File.createTempFile("FileOutputStream", "tmp")
    tmpfile.deleteOnExit()
    val fos = new FileOutputStream(tmpfile)
    fos.write(bytes)
    fos.flush()
    fos.close()
    val f = new FileOutputStream(tmpfile, true)
    assert(10 == f.getChannel.position)
  }

  test("getChannel_Append") {
    assume(!isScalaJS, "not implemented yey")
    val tmpfile = File.createTempFile("FileOutputStream", "tmp");
    tmpfile.deleteOnExit();
    val fos = new FileOutputStream(tmpfile, true)
    assert(0 === fos.getChannel().position())
    fos.write(bytes);
    assert(fos.getChannel().position() === 10)
    fos.write(bytes);
    assert(fos.getChannel().position() === 20)
    fos.write(bytes);
    assert(fos.getChannel().position() == 30)
    fos.close()

    assertThrows[ClosedChannelException] {
      fos.getChannel().position();
    }
  }

  test("getChannel_UnAppend") {
    assume(!isScalaJS, "not implemented yey")

    val tmpfile = File.createTempFile("FileOutputStream", "tmp");
    tmpfile.deleteOnExit();
    val fos = new FileOutputStream(tmpfile, false);
    assert(fos.getChannel().position() === 0)
    fos.write(bytes);
    assert(fos.getChannel().position() === 10);
    fos.write(bytes);
    assert(fos.getChannel().position() === 20)
    fos.write(bytes);
    assert(fos.getChannel().position() === 30);
    fos.close();

    assertThrows[ClosedChannelException] {
      fos.getChannel().position();
    }
  }

  test("getChannel_Unappend_Unappend") {
    assume(!isScalaJS, "not implemented yey")
    val tmpfile = File.createTempFile("FileOutputStream", "tmp");
    tmpfile.deleteOnExit();
    val fos = new FileOutputStream(tmpfile, false);
    assert(fos.getChannel().position() === 0)
    fos.write(bytes);
    assert(fos.getChannel().position() === 10)
    fos.close();

    val fos2 = new FileOutputStream(tmpfile, false);
    assert(fos2.getChannel().position() === 0)
    fos2.close();
  }

  test("getChannel_Unappend_Append") {
    assume(!isScalaJS, "not implemented yey")
    val tmpfile = File.createTempFile("FileOutputStream", "tmp");
    tmpfile.deleteOnExit();
    val fos = new FileOutputStream(tmpfile, false);
    assert(fos.getChannel().position() === 0)
    fos.write(bytes);
    assert(fos.getChannel().position() === 10);
    fos.close();

    val fos2 = new FileOutputStream(tmpfile, true);
    assert(fos2.getChannel().position() === 10);
    fos2.close();
  }

  test("getChannel_Append_Unappend") {
    assume(!isScalaJS, "not implemented yey")
    val tmpfile = File.createTempFile("FileOutputStream", "tmp");
    tmpfile.deleteOnExit();
    val fos = new FileOutputStream(tmpfile, true);
    assert(fos.getChannel().position() === 0)
    fos.write(bytes);
    assert(fos.getChannel().position() === 10);
    fos.close();

    val fos2 = new FileOutputStream(tmpfile, false);
    assert(fos2.getChannel().position() === 0)
    fos2.close();
  }

  test("getChanne_Append_Append") {
    assume(!isScalaJS, "not implemented yey")
    val tmpfile = File.createTempFile("FileOutputStream", "tmp");
    tmpfile.deleteOnExit();
    val fos = new FileOutputStream(tmpfile, true);
    assert(fos.getChannel().position() === 0)
    fos.write(bytes);
    assert(fos.getChannel().position() === 10);
    fos.close();

    val fos2 = new FileOutputStream(tmpfile, true);
    assert(fos2.getChannel().position() === 10);
    fos2.close();
  }
}
