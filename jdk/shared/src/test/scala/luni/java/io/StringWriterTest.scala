package luni.java.io

import java.io.StringWriter

import org.scalatest.BeforeAndAfterEach
import org.scalatest.freespec.AnyFreeSpec

class StringWriterTest extends AnyFreeSpec with BeforeAndAfterEach {
  private[io] var sw: StringWriter = _

  override def beforeEach(): Unit = {
    sw = new StringWriter()
  }

  override def afterEach(): Unit = {
    if (sw != null) sw.close()
  }

  "Constructor" in {
    assert(true)
  }

  "close" in {
    // always success, not throwing IOException
    sw.close()
    sw.close()
  }

  "flush" in {
    sw.flush()
    sw.write('c')
    assert("c" === sw.toString)
  }

  "getBuffer" in {
    sw.write("This is a test string")
    val sb = sw.getBuffer
    assert("This is a test string" === sb.toString)
  }

  "toString" in {
    sw.write("This is a test string")
    assert("This is a test string" === sw.toString)
  }

  "write$CII" in {
    val c = new Array[Char](1000)
    "This is a test string".getChars(0, 21, c, 0)
    sw.write(c, 0, 21)
    assert("This is a test string" === sw.toString)
  }

  "write$CII_2" in {
    assertThrows[IndexOutOfBoundsException] {
      sw.write(new Array[Char](0), 0, -1)
      fail("IndexOutOfBoundsException expected")
    }
  }

  "write$CII_3" in {
    assertThrows[IndexOutOfBoundsException] {
      sw.write(new Array[Char](0), -1, 0)
    }
  }

  "write$CII_4" in {
    assertThrows[IndexOutOfBoundsException] {
      sw.write(new Array[Char](0), -1, -1)
    }
  }

  "writeI" in {
    sw.write('c')
    assert("c" === sw.toString)
  }

  "writeLjava_lang_String" in {
    sw.write("This is a test string")
    assert("This is a test string" === sw.toString)
  }

  "writeLjava_lang_StringII" in {
    sw.write("This is a test string", 2, 2)
    assert("is" === sw.toString)
  }

  "appendChar" in {
    val testChar = ' '
    sw = new StringWriter(20)
    sw.append(testChar)
    assert(String.valueOf(testChar) === sw.toString)
  }

  "appendCharSequence" in {
    val testString = "My Test String"
    sw = new StringWriter(20)
    sw.append(testString)
    assert(String.valueOf(testString) === sw.toString)
  }

  "appendCharSequenceIntInt" in {
    val testString = "My Test String"
    sw = new StringWriter(20)
    sw.append(testString, 1, 3)
    assert(testString.substring(1, 3) === sw.toString)
  }
}
