package luni.java.io

import org.scalatest.freespec.AnyFreeSpec

import java.io.{IOException, StringReader}
import org.scalatest.BeforeAndAfterEach

class StringReaderTest extends AnyFreeSpec with BeforeAndAfterEach {

  private[io] val testString = "This is a test string"

  private[io] var sr: StringReader = _

  "ConstructorLjava_lang_String" in {
    assert(true)
  }

  "close" in {
    sr = new StringReader(testString)
    sr.close()
    val buf = new Array[Char](10)
    assertThrows[IOException] {
      sr.read(buf, 0, 2)
    }
  }

  "markI" in {
    sr = new StringReader(testString)
    sr.skip(5)
    sr.mark(0)
    sr.skip(5)
    sr.reset()
    val buf = new Array[Char](10)
    sr.read(buf, 0, 2)
    assert(new String(buf, 0, 2) === testString.substring(5, 7))
  }

  "markSupported" in {
    sr = new StringReader(testString)
    assert(sr.markSupported)
  }

  "read" in {
    sr = new StringReader(testString)
    val r = sr.read
    assert('T' === r)
    sr = new StringReader(new String(Array[Char]('\u8765')))
    assert(sr.read === '\u8765')
  }

  "read$CII" in {
    sr = new StringReader(testString)
    val buf = new Array[Char](testString.length)
    val r   = sr.read(buf, 0, testString.length)
    assert(r === testString.length)
    assert(new String(buf, 0, r).equals(testString))
  }

  "ready" in {
    sr = new StringReader(testString)
    assert(sr.ready)
    sr.close()
    assertThrows[IOException] {
      sr.ready
    }
  }

  "reset" in {
    sr = new StringReader(testString)
    sr.skip(5)
    sr.mark(0)
    sr.skip(5)
    sr.reset()
    val buf = new Array[Char](10)
    sr.read(buf, 0, 2)
    assert(new String(buf, 0, 2).equals(testString.substring(5, 7)))
  }

  "skipJ" in {
    sr = new StringReader(testString)
    sr.skip(5)
    val buf = new Array[Char](10)
    sr.read(buf, 0, 2)
    assert(new String(buf, 0, 2).equals(testString.substring(5, 7)))
  }

  // Regression test for HARMONY-5077
  private[io] var finish = false
}
