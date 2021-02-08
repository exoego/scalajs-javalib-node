package luni.java.io

import org.scalatest.funsuite.AnyFunSuite

import java.io.{IOException, StringReader}
import org.scalatest.BeforeAndAfterEach

class StringReaderTest extends AnyFunSuite with BeforeAndAfterEach {

  private[io] val testString = "This is a test string"

  private[io] var sr: StringReader = _

  test("ConstructorLjava_lang_String") {
    assert(true)
  }

  test("close") {
    sr = new StringReader(testString)
    sr.close()
    val buf = new Array[Char](10)
    assertThrows[IOException] {
      sr.read(buf, 0, 2)
    }
  }

  test("markI") {
    sr = new StringReader(testString)
    sr.skip(5)
    sr.mark(0)
    sr.skip(5)
    sr.reset()
    val buf = new Array[Char](10)
    sr.read(buf, 0, 2)
    assert(new String(buf, 0, 2) == testString.substring(5, 7))
  }

  test("markSupported") {
    sr = new StringReader(testString)
    assert(sr.markSupported)
  }

  test("read") {
    sr = new StringReader(testString)
    val r = sr.read
    assert('T' == r)
    sr = new StringReader(new String(Array[Char]('\u8765')))
    assert(sr.read == '\u8765')
  }

  test("read$CII") {
    sr = new StringReader(testString)
    val buf = new Array[Char](testString.length)
    val r   = sr.read(buf, 0, testString.length)
    assert(r == testString.length)
    assert(new String(buf, 0, r).equals(testString))
  }

  test("ready") {
    sr = new StringReader(testString)
    assert(sr.ready)
    sr.close()
    assertThrows[IOException] {
      sr.ready
    }
  }

  test("reset") {
    sr = new StringReader(testString)
    sr.skip(5)
    sr.mark(0)
    sr.skip(5)
    sr.reset()
    val buf = new Array[Char](10)
    sr.read(buf, 0, 2)
    assert(new String(buf, 0, 2).equals(testString.substring(5, 7)))
  }

  test("skipJ") {
    sr = new StringReader(testString)
    sr.skip(5)
    val buf = new Array[Char](10)
    sr.read(buf, 0, 2)
    assert(new String(buf, 0, 2).equals(testString.substring(5, 7)))
  }

  // Regression test for HARMONY-5077
  private[io] var finish = false

  ignore("todo: Thread.yield") {
    //  test("synchronization") {
    //    val anything = "Hello world"
    //    val sr = new StringReader(anything)
    //    val other = new Thread(() => {
    //      sr.close()
    //      finish = true
    //    })
    //    anything.synchronized {
    //      other.start()
    //      while ( {
    //        !finish
    //      }) Thread.`yield`()
    //    }
    //  }
  }
}
