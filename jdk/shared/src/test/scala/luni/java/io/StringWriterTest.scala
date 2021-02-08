package luni.java.io

import java.io.StringWriter

import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AnyFunSuite

class StringWriterTest extends AnyFunSuite with BeforeAndAfterEach {
  private[io] var sw: StringWriter = _

  override def beforeEach(): Unit = {
    sw = new StringWriter()
  }

  override def afterEach(): Unit = {
    if (sw != null) sw.close()
  }

  test("Constructor") {
    assert(true)
  }

  test("close") {
    // always success, not throwing IOException
    sw.close()
    sw.close()
  }

  test("flush") {
    sw.flush()
    sw.write('c')
    assert("c" == sw.toString)
  }

  test("getBuffer") {
    sw.write("This is a test string")
    val sb = sw.getBuffer
    assert("This is a test string" == sb.toString)
  }

  test("toString") {
    sw.write("This is a test string")
    assert("This is a test string" == sw.toString)
  }

  test("write$CII") {
    val c = new Array[Char](1000)
    "This is a test string".getChars(0, 21, c, 0)
    sw.write(c, 0, 21)
    assert("This is a test string" == sw.toString)
  }

  test("write$CII_2") {
    assertThrows[IndexOutOfBoundsException] {
      sw.write(new Array[Char](0), 0, -1)
      fail("IndexOutOfBoundsException expected")
    }
  }

  test("write$CII_3") {
    assertThrows[IndexOutOfBoundsException] {
      sw.write(new Array[Char](0), -1, 0)
    }
  }

  test("write$CII_4") {
    assertThrows[IndexOutOfBoundsException] {
      sw.write(new Array[Char](0), -1, -1)
    }
  }

  test("writeI") {
    sw.write('c')
    assert("c" == sw.toString)
  }

  test("writeLjava_lang_String") {
    sw.write("This is a test string")
    assert("This is a test string" == sw.toString)
  }

  test("writeLjava_lang_StringII") {
    sw.write("This is a test string", 2, 2)
    assert("is" == sw.toString)
  }

  test("appendChar") {
    val testChar = ' '
    sw = new StringWriter(20)
    sw.append(testChar)
    assert(String.valueOf(testChar) == sw.toString)
  }

  test("appendCharSequence") {
    val testString = "My Test String"
    sw = new StringWriter(20)
    sw.append(testString)
    assert(String.valueOf(testString) == sw.toString)
  }

  test("appendCharSequenceIntInt") {
    val testString = "My Test String"
    sw = new StringWriter(20)
    sw.append(testString, 1, 3)
    assert(testString.substring(1, 3) == sw.toString)
  }
}
