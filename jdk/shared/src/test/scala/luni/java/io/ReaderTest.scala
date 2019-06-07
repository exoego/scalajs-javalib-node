package luni.java.io

import java.io.{IOException, Reader}
import java.nio.CharBuffer

import org.scalatest.FunSuite

class ReaderTest extends FunSuite {
  private val isScalaJS = System.getProperty("java.vm.name") == "Scala.js"

  test("Reader_CharBuffer_null") {
    val s          = "MY TEST STRING"
    val mockReader = new MockReader(s.toCharArray)

    val charBuffer: CharBuffer = null
    val ex = intercept[Exception] {
      mockReader.read(charBuffer)
    }
    if (isScalaJS)
      assert(ex.getMessage.contains("null"))
    else
      assert(ex.isInstanceOf[NullPointerException])
  }

  test("Reader_CharBuffer_ZeroChar") {
    //the charBuffer has the capacity of 0, then there the number of char read
    // to the CharBuffer is 0. Furthermore, the MockReader is intact in its content.
    val s          = "MY TEST STRING"
    val srcBuffer  = s.toCharArray
    val mockReader = new MockReader(srcBuffer)
    val charBuffer = CharBuffer.allocate(0)
    val result     = mockReader.read(charBuffer)
    assert(0 == result)
    val destBuffer = new Array[Char](srcBuffer.length)
    mockReader.read(destBuffer)
    assert(s == String.valueOf(destBuffer))
  }

  test("Reader_CharBufferChar") {
    val s               = "MY TEST STRING"
    val srcBuffer       = s.toCharArray
    val CHARBUFFER_SIZE = 10
    val mockReader      = new MockReader(srcBuffer)
    val charBuffer      = CharBuffer.allocate(CHARBUFFER_SIZE)
    charBuffer.append('A')
    val CHARBUFFER_REMAINING = charBuffer.remaining
    val result               = mockReader.read(charBuffer)
    assert(CHARBUFFER_REMAINING == result)

    charBuffer.rewind
    assert(
      s.substring(0, CHARBUFFER_REMAINING) == charBuffer
        .subSequence(CHARBUFFER_SIZE - CHARBUFFER_REMAINING, CHARBUFFER_SIZE)
        .toString
    )

    val destBuffer = new Array[Char](srcBuffer.length - CHARBUFFER_REMAINING)
    mockReader.read(destBuffer)
    assert(s.substring(CHARBUFFER_REMAINING) == String.valueOf(destBuffer))
  }

  test("mark") {
    val mockReader = new MockReader()
    assertThrows[IOException] {
      mockReader.mark(0)
    }
  }

  test("read") {
    val reader = new MockReader
    assert(-1 == reader.read())

    val string     = "MY TEST STRING"
    val srcBuffer  = string.toCharArray
    val mockReader = new MockReader(srcBuffer)
    // normal read
    for (c <- srcBuffer) {
      assert(c == mockReader.read().toChar)
    }
    // return -1 when read Out of Index
    assert(-1 == mockReader.read())
    assert(-1 == reader.read())
  }

  test("ready") {
    val mockReader = new MockReader()
    assert(!mockReader.ready)
  }

  test("reset") {
    val mockReader = new MockReader
    assertThrows[IOException] {
      mockReader.reset()
    }
  }

  // TODO: Wait Reader#skip fixed
  ignore("skip") {
    val string     = "MY TEST STRING"
    val srcBuffer  = string.toCharArray
    val length     = srcBuffer.length
    val mockReader = new MockReader(srcBuffer)
    assert('M' == mockReader.read())
    // normal skip
    mockReader.skip(length / 2)
    assert('S' == mockReader.read().toChar)
    // try to skip a bigger number of characters than the total
    // Should do nothing
    mockReader.skip(length)
    // try to skip a negative number of characters throw IllegalArgumentException
    assertThrows[IllegalArgumentException] {
      mockReader.skip(-1)
    }
  }

  private[io] class MockReader() extends Reader {
    private var contents: Array[Char] = _

    private var current_offset = 0
    private var length         = 0

    def this(data: Array[Char]) {
      this()
      contents = data
      length = contents.length
    }

    override def close(): Unit = {
      contents = null
    }

    override def read(buf: Array[Char], offset: Int, count: Int): Int = {
      if (null == contents) return -1
      if (length <= current_offset) return -1
      if (buf.length < offset + count) throw new IndexOutOfBoundsException()
      val readSie = Math.min(count, length - current_offset)
      for (i <- 0 until readSie) {
        buf(offset + i) = contents(current_offset + i)
      }
      current_offset += readSie
      readSie
    }
  }

}
