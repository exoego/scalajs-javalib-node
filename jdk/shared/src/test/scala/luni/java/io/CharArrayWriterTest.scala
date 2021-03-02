package luni.java.io

import java.io.{CharArrayReader, CharArrayWriter, StringWriter}

import org.scalatest.BeforeAndAfterEach
import org.scalatest.freespec.AnyFreeSpec

class CharArrayWriterTest extends AnyFreeSpec with BeforeAndAfterEach {
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

  "Constructor" in {
    cw = new CharArrayWriter()
    assert(0 === cw.size)
  }

  "ConstructorI" in {
    cw = new CharArrayWriter(90)
    assert(0 === cw.size)
  }

  "close" in {
    cw.close()
  }

  "flush" in {
    cw.flush()
  }

  "reset" in {
    cw.write("HelloWorld", 5, 5)
    cw.reset()
    cw.write("HelloWorld", 0, 5)
    cr = new CharArrayReader(cw.toCharArray)
    val c = new Array[Char](100)
    cr.read(c, 0, 5)
    assert("Hello" === new String(c, 0, 5))
  }

  "size" in {
    assert(0 === cw.size)
    cw.write(hw, 5, 5)
    assert(5 === cw.size)
  }

  "toCharArray" in {
    cw.write("HelloWorld", 0, 10)
    cr = new CharArrayReader(cw.toCharArray)
    val c = new Array[Char](100)
    cr.read(c, 0, 10)
    assert("HelloWorld" === new String(c, 0, 10))
  }

  "toString" in {
    cw.write("HelloWorld", 5, 5)
    cr = new CharArrayReader(cw.toCharArray)
    assert("World" === cw.toString)
  }

  "write$CII" in {
    cw.write(hw, 5, 5)
    cr = new CharArrayReader(cw.toCharArray)
    val c = new Array[Char](100)
    cr.read(c, 0, 5)
    assert("World" === new String(c, 0, 5))
  }

  "write$CII_2" in {
    assertThrows[IndexOutOfBoundsException] {
      cw.write(Array[Char]('0'), 0, -1)
    }
  }

  "writeI" in {
    cw.write('T')
    cr = new CharArrayReader(cw.toCharArray)
    assert('T' === cr.read)
  }

  "writeLjava_lang_StringII" in {
    cw.write("HelloWorld", 5, 5)
    cr = new CharArrayReader(cw.toCharArray)
    val c = new Array[Char](100)
    cr.read(c, 0, 5)
    assert("World" === new String(c, 0, 5))
  }

  "writeLjava_lang_StringII_2" in {
    assertThrows[NullPointerException] {
      cw.write(null.asInstanceOf[String], -1, 0)
    }
  }

  "writeToLjava_io_Writer" in {
    cw.write("HelloWorld", 0, 10)
    val sw = new StringWriter
    cw.writeTo(sw)
    assert("HelloWorld" === sw.toString)
  }

  "appendChar" in {
    val testChar = ' '
    cw = new CharArrayWriter(10)
    cw.append(testChar)
    cw.flush()
    assert(String.valueOf(testChar) === cw.toString)
  }

  "appendCharSequence" in {
    val testString = "My Test String"
    cw = new CharArrayWriter(10)
    cw.append(testString)
    cw.flush()
    assert(testString === cw.toString)
  }

  "appendCharSequenceIntInt" in {
    val testString = "My Test String"
    cw = new CharArrayWriter(10)
    cw.append(testString, 1, 3)
    cw.flush()
    assert(testString.substring(1, 3) === cw.toString)
  }
}
