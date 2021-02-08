package luni.java.io

import java.io.CharArrayReader
import java.io.IOException
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AnyFunSuite

class CharArrayReaderTest extends AnyFunSuite with BeforeAndAfterEach {
  private[io] val hw                  = Array('H', 'e', 'l', 'l', 'o', 'W', 'o', 'r', 'l', 'd')
  private[io] var cr: CharArrayReader = _

  override def afterEach(): Unit = {
    if (cr != null) cr.close()
  }

  test("Constructor$C") {
    cr = new CharArrayReader(hw)
    assert(cr.ready)
  }

  test("Constructor$CII") {
    cr = new CharArrayReader(hw, 5, 5)
    assert(cr.ready)
    val c = cr.read
    assert(c == 'W')
  }

  test("close") {
    cr = new CharArrayReader(hw)
    cr.close()
    assertThrows[IOException] {
      cr.read
    }
    // No-op
    cr.close()
  }

  test("markI") {
    cr = new CharArrayReader(hw)
    cr.skip(5L)
    cr.mark(100)
    cr.read()
    cr.reset()
    assert('W' == cr.read)
  }

  test("markSupported") {
    cr = new CharArrayReader(hw)
    assert(cr.markSupported)
  }

  test("read") {
    cr = new CharArrayReader(hw)
    assert('H' == cr.read)
    cr = new CharArrayReader(Array[Char]('\u8765'))
    assert(cr.read == '\u8765')
  }

  test("read$CII") {
    val c = new Array[Char](11)
    cr = new CharArrayReader(hw)
    cr.read(c, 1, 10)
    assert(new String(c, 1, 10) == new String(hw, 0, 10))
  }

  test("ready") {
    cr = new CharArrayReader(hw)
    assert(cr.ready)
    cr.skip(1000)
    assert(!cr.ready)
    cr.close()
    assertThrows[IOException] {
      cr.ready
    }
    cr = new CharArrayReader(hw)
    cr.close()
    assertThrows[IOException] {
      cr.ready()
    }
  }

  test("reset") {
    cr = new CharArrayReader(hw)
    cr.skip(5L)
    cr.mark(100)
    cr.read()
    cr.reset()
    assert('W' == cr.read)

    // Regression for HARMONY-4357
    val str  = "offsetHello world!"
    val data = new Array[Char](str.length)
    str.getChars(0, str.length, data, 0)
    val offsetLength = 6
    val length       = data.length - offsetLength
    val reader       = new CharArrayReader(data, offsetLength, length)
    reader.reset()
    for (i <- 0 until length) {
      assert(data(offsetLength + i) == reader.read.asInstanceOf[Char])
    }
  }

  test("skipJ") {
    cr = new CharArrayReader(hw)
    val skipped = cr.skip(5L)
    assert(5L == skipped)
    assert('W' == cr.read)
  }
}
