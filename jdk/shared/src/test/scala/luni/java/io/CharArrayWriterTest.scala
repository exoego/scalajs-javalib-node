package luni.java.io

import java.io.{CharArrayReader, CharArrayWriter, StringWriter}

import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AnyFunSuite

class CharArrayWriterTest extends AnyFunSuite with BeforeAndAfterEach {
  private[io] val hw = Array('H', 'e', 'l', 'l', 'o', 'W', 'o', 'r', 'l', 'd')

  private[io] var cw: CharArrayWriter = _

  private[io] var cr: CharArrayReader = _

  override protected def beforeEach(): Unit = {
    cw = new CharArrayWriter()
  }

  override protected def afterEach(): Unit = {
    if (cr != null) cr.close()
    cw.close()
  }

  test("Constructor") {
    cw = new CharArrayWriter()
    assert(0 === cw.size)
  }

  test("ConstructorI") {
    cw = new CharArrayWriter(90)
    assert(0 === cw.size)
  }

  test("close") {
    cw.close()
  }

  test("flush") {
    cw.flush()
  }

  test("reset") {
    cw.write("HelloWorld", 5, 5)
    cw.reset()
    cw.write("HelloWorld", 0, 5)
    cr = new CharArrayReader(cw.toCharArray)
    val c = new Array[Char](100)
    cr.read(c, 0, 5)
    assert("Hello" === new String(c, 0, 5))
  }

  test("size") {
    assert(0 === cw.size)
    cw.write(hw, 5, 5)
    assert(5 === cw.size)
  }

  test("toCharArray") {
    cw.write("HelloWorld", 0, 10)
    cr = new CharArrayReader(cw.toCharArray)
    val c = new Array[Char](100)
    cr.read(c, 0, 10)
    assert("HelloWorld" === new String(c, 0, 10))
  }

  test("toString") {
    cw.write("HelloWorld", 5, 5)
    cr = new CharArrayReader(cw.toCharArray)
    assert("World" === cw.toString)
  }

  test("write$CII") {
    cw.write(hw, 5, 5)
    cr = new CharArrayReader(cw.toCharArray)
    val c = new Array[Char](100)
    cr.read(c, 0, 5)
    assert("World" === new String(c, 0, 5))
  }

  test("write$CII_2") {
    assertThrows[IndexOutOfBoundsException] {
      cw.write(Array[Char]('0'), 0, -1)
    }
  }

  test("writeI") {
    cw.write('T')
    cr = new CharArrayReader(cw.toCharArray)
    assert('T' === cr.read)
  }

  test("writeLjava_lang_StringII") {
    cw.write("HelloWorld", 5, 5)
    cr = new CharArrayReader(cw.toCharArray)
    val c = new Array[Char](100)
    cr.read(c, 0, 5)
    assert("World" === new String(c, 0, 5))
  }

  test("writeLjava_lang_StringII_2") {
    assertThrows[NullPointerException] {
      cw.write(null.asInstanceOf[String], -1, 0)
    }
  }

  test("writeToLjava_io_Writer") {
    cw.write("HelloWorld", 0, 10)
    val sw = new StringWriter
    cw.writeTo(sw)
    assert("HelloWorld" === sw.toString)
  }

  test("appendChar") {
    val testChar = ' '
    cw = new CharArrayWriter(10)
    cw.append(testChar)
    cw.flush()
    assert(String.valueOf(testChar) === cw.toString)
  }

  test("appendCharSequence") {
    val testString = "My Test String"
    cw = new CharArrayWriter(10)
    cw.append(testString)
    cw.flush()
    assert(testString === cw.toString)
  }

  test("appendCharSequenceIntInt") {
    val testString = "My Test String"
    cw = new CharArrayWriter(10)
    cw.append(testString, 1, 3)
    cw.flush()
    assert(testString.substring(1, 3) === cw.toString)
  }
}
