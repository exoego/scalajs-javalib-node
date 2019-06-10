package luni.java.io

import java.io.{
  File,
  FileDescriptor,
  FileInputStream,
  FileNotFoundException,
  FileOutputStream,
  IOException,
  InputStream,
  InputStreamReader,
  OutputStream
}
import java.nio.channels.ClosedChannelException

import org.scalatest.{BeforeAndAfterEach, FunSuite}
import support.Support_PlatformFile

class FileInputStreamTest extends FunSuite with BeforeAndAfterEach {
  var fileName: String = _

  private var is: FileInputStream = _

  private[io] val ibuf = new Array[Byte](4096)

  var fileString =
    "Test_All_Tests\nTest_java_io_BufferedInputStream\nTest_java_io_BufferedOutputStream\nTest_java_io_ByteArrayInputStream\nTest_java_io_ByteArrayOutputStream\nTest_java_io_DataInputStream\nTest_java_io_File\nTest_java_io_FileDescriptor\nTest_FileInputStream\nTest_java_io_FileNotFoundException\nTest_java_io_FileOutputStream\nTest_java_io_FilterInputStream\nTest_java_io_FilterOutputStream\nTest_java_io_InputStream\nTest_java_io_IOException\nTest_java_io_OutputStream\nTest_java_io_PrintStream\nTest_java_io_RandomAccessFile\nTest_java_io_SyncFailedException\nTest_java_lang_AbstractMethodError\nTest_java_lang_ArithmeticException\nTest_java_lang_ArrayIndexOutOfBoundsException\nTest_java_lang_ArrayStoreException\nTest_java_lang_Boolean\nTest_java_lang_Byte\nTest_java_lang_Character\nTest_java_lang_Class\nTest_java_lang_ClassCastException\nTest_java_lang_ClassCircularityError\nTest_java_lang_ClassFormatError\nTest_java_lang_ClassLoader\nTest_java_lang_ClassNotFoundException\nTest_java_lang_CloneNotSupportedException\nTest_java_lang_Double\nTest_java_lang_Error\nTest_java_lang_Exception\nTest_java_lang_ExceptionInInitializerError\nTest_java_lang_Float\nTest_java_lang_IllegalAccessError\nTest_java_lang_IllegalAccessException\nTest_java_lang_IllegalArgumentException\nTest_java_lang_IllegalMonitorStateException\nTest_java_lang_IllegalThreadStateException\nTest_java_lang_IncompatibleClassChangeError\nTest_java_lang_IndexOutOfBoundsException\nTest_java_lang_InstantiationError\nTest_java_lang_InstantiationException\nTest_java_lang_Integer\nTest_java_lang_InternalError\nTest_java_lang_InterruptedException\nTest_java_lang_LinkageError\nTest_java_lang_Long\nTest_java_lang_Math\nTest_java_lang_NegativeArraySizeException\nTest_java_lang_NoClassDefFoundError\nTest_java_lang_NoSuchFieldError\nTest_java_lang_NoSuchMethodError\nTest_java_lang_NullPointerException\nTest_java_lang_Number\nTest_java_lang_NumberFormatException\nTest_java_lang_Object\nTest_java_lang_OutOfMemoryError\nTest_java_lang_RuntimeException\nTest_java_lang_SecurityManager\nTest_java_lang_Short\nTest_java_lang_StackOverflowError\nTest_java_lang_String\nTest_java_lang_StringBuffer\nTest_java_lang_StringIndexOutOfBoundsException\nTest_java_lang_System\nTest_java_lang_Thread\nTest_java_lang_ThreadDeath\nTest_java_lang_ThreadGroup\nTest_java_lang_Throwable\nTest_java_lang_UnknownError\nTest_java_lang_UnsatisfiedLinkError\nTest_java_lang_VerifyError\nTest_java_lang_VirtualMachineError\nTest_java_lang_vm_Image\nTest_java_lang_vm_MemorySegment\nTest_java_lang_vm_ROMStoreException\nTest_java_lang_vm_VM\nTest_java_lang_Void\nTest_java_net_BindException\nTest_java_net_ConnectException\nTest_java_net_DatagramPacket\nTest_java_net_DatagramSocket\nTest_java_net_DatagramSocketImpl\nTest_java_net_InetAddress\nTest_java_net_NoRouteToHostException\nTest_java_net_PlainDatagramSocketImpl\nTest_java_net_PlainSocketImpl\nTest_java_net_Socket\nTest_java_net_SocketException\nTest_java_net_SocketImpl\nTest_java_net_SocketInputStream\nTest_java_net_SocketOutputStream\nTest_java_net_UnknownHostException\nTest_java_util_ArrayEnumerator\nTest_java_util_Date\nTest_java_util_EventObject\nTest_java_util_HashEnumerator\nTest_java_util_Hashtable\nTest_java_util_Properties\nTest_java_util_ResourceBundle\nTest_java_util_tm\nTest_java_util_Vector\n"

  override protected def beforeEach(): Unit = {
    fileName = System.getProperty("user.dir", "./")
    val separator = System.getProperty("file.separator")
    fileName =
      if (fileName.charAt(fileName.length - 1) == separator.charAt(0))
        Support_PlatformFile.getNewPlatformFile(fileName, "input.tst")
      else
        Support_PlatformFile.getNewPlatformFile(fileName + separator, "input.tst")
    val fos = new FileOutputStream(fileName)
    fos.write(fileString.getBytes)
    fos.close()
  }

  override def afterEach(): Unit = {
//    new File(fileName).delete()
    is = null
  }

  test("ConstructorLjava_io_File") {
    val f = new File(fileName)
    is = new FileInputStream(f)
    is.close()
  }

  // TODO: FileDescriptor
//  test("ConstructorLjava_io_FileDescriptor") {
//    val fos = new FileOutputStream(fileName)
//    val fis = new FileInputStream(fos.getFD)
//    fos.close()
//    fis.close()
//  }

  test("ConstructorLjava_lang_String") {
    is = new FileInputStream(fileName)
    is.close()
  }

  test("ConstructorLjava_lang_String_I") {
    assertThrows[FileNotFoundException] {
      is = new FileInputStream("")
    }
    if (is != null) is.close()

    assertThrows[FileNotFoundException] {
      is = new FileInputStream(new File(""))
    }
    if (is != null) is.close()
  }

  test("close") {
    is = new FileInputStream(fileName)
    is.close()
    assertThrows[IOException] {
      is.read
    }

    // Regression test for HARMONY-6642
    val fis  = new FileInputStream(fileName)
    val fis2 = new FileInputStream(fis.getFD)
    fis2.close()
    assert(!fis.getFD.valid() && !fis2.getFD.valid())
    assertThrows[IOException] {
      fis.read()
    }
    try fis.close()
    catch {
      case _: IOException =>
    }
  }

  // TODO: FileDescriptor.in
  //  test("close: stdin") {
  //    var stdin = new FileInputStream(FileDescriptor.in)
  //    stdin.close()
  //    stdin = new FileInputStream(FileDescriptor.in)
  //    assertThrows[IOException] {
  //      stdin.read()
  //    }
  //  }

  test("getFD") {
    val fis = new FileInputStream(fileName)
    assert(fis.getFD.valid)
    fis.close()
    assert(!fis.getFD.valid)
  }

  test("read") {
    val isr = new InputStreamReader(new FileInputStream(fileName))
    val c   = isr.read()
    isr.close()
    assert(c == fileString.charAt(0))
  }

  test("read$B") {
    val buf1 = new Array[Byte](100)
    is = new FileInputStream(fileName)
    is.skip(3000)
    is.read(buf1)
    is.close()
    assert(new String(buf1, 0, buf1.length) == fileString.substring(3000, 3100))
  }

  ignore("read$BII") {
    val buf1 = new Array[Byte](100)
    is = new FileInputStream(fileName)
    is.skip(3000)
    is.read(buf1, 0, buf1.length)
    is.close()
    assert(new String(buf1, 0, buf1.length) == fileString.substring(3000, 3100))
  }

  test("read$BII: Regression test for HARMONY-285") {
    val file = new File("FileInputStream.tmp")
    file.createNewFile()
    file.deleteOnExit()
    val in = new FileInputStream(file)
    assertThrows[NullPointerException] {
      in.read(null, 0, 0)
    }
    in.close()
    file.delete()
  }

  test("read_$BII_IOException") {
    val buf = new Array[Byte](1000)
    assertThrows[IOException] {
      is = new FileInputStream(fileName)
      is.close()
      is.read(buf, 0, 100)
    }
    is.close()

    is = new FileInputStream(fileName)
    is.close()
    is.read(buf, 0, 0)
    is.close()
  }

  test("read_$BII_NullPointerException") {
    val buf = null
    assertThrows[NullPointerException] {
      is = new FileInputStream(fileName)
      is.read(buf, -1, 0)
    }
    is.close()
  }

  test("read_$BII_IndexOutOfBoundsException") {
    val buf = new Array[Byte](1000)
    assertThrows[IndexOutOfBoundsException] {
      is = new FileInputStream(fileName)
      is.read(buf, -1, 0)
    }
    is.close()

    assertThrows[IndexOutOfBoundsException] {
      is = new FileInputStream(fileName)
      is.read(buf, 0, -1)
    }
    is.close()

    assertThrows[IndexOutOfBoundsException] {
      is = new FileInputStream(fileName)
      is.read(buf, -1, -1)
    }
    is.close()

    assertThrows[IndexOutOfBoundsException] {
      is = new FileInputStream(fileName)
      is.read(buf, 0, 1001)
    }
    is.close()

    assertThrows[IndexOutOfBoundsException] {
      is = new FileInputStream(fileName)
      is.read(buf, 1001, 0)
    }
    is.close()

    assertThrows[IndexOutOfBoundsException] {
      is = new FileInputStream(fileName)
      is.read(buf, 500, 501)
    }
    is.close()
  }

  test("regressionNNN: Regression for HARMONY-434") {
    val fis = new FileInputStream(fileName)
    assertThrows[IndexOutOfBoundsException] {
      fis.read(new Array[Byte](1), -1, 1)
    }
    assertThrows[IndexOutOfBoundsException] {
      fis.read(new Array[Byte](1), 0, -1)
    }
    assertThrows[IndexOutOfBoundsException] {
      fis.read(new Array[Byte](1), 0, 5)
    }
    assertThrows[IndexOutOfBoundsException] {
      fis.read(new Array[Byte](10), Integer.MAX_VALUE, 5)
    }
    assertThrows[IndexOutOfBoundsException] {
      fis.read(new Array[Byte](10), 5, Integer.MAX_VALUE)
    }
    fis.close()
  }

  test("available") {
    try {
      is = new FileInputStream(fileName)
      assert(is.available == fileString.length)
    } finally {
      try is.close()
      catch {
        case _: Exception => // ignore
      }
    }
  }

  test("skipJ") {
    val buf1 = new Array[Byte](10)
    is = new FileInputStream(fileName)
    is.skip(1000)
    is.read(buf1, 0, buf1.length)
    is.close()
    assert(new String(buf1, 0, buf1.length) == fileString.substring(1000, 1010))
  }

  test("skipNegativeArgumentJ") {
    val fis = new FileInputStream(fileName)
    assertThrows[IOException] {
      fis.skip(-5)
    }
    fis.close()
  }

// TODO: FileChannel
//  test("getChannel") {
//    var fis = new FileInputStream(fileName)
//    assert(0 == fis.getChannel.position)
//    var r     = 0
//    var count = 1
//    while ({
//      r = fis.read
//      r != -1
//    }) {
//      assert({
//        count += 1; count - 1
//      } == fis.getChannel.position)
//    }
//    fis.close()
//
//    assertThrows[ClosedChannelException] {
//      fis.getChannel.position
//    }
//
//    fis = new FileInputStream(fileName)
//    assert(0 == fis.getChannel.position)
//    var bs = new Array[Byte](10)
//    r = fis.read(bs)
//    assert(10 == fis.getChannel.position)
//    fis.close()
//
//    fis = new FileInputStream(fileName)
//    assert(0 == fis.getChannel.position)
//    bs = new Array[Byte](10)
//    fis.skip(100)
//    assert(100 == fis.getChannel.position)
//    r = fis.read(bs)
//    assert(110 == fis.getChannel.position)
//    fis.close()
//  }

}
