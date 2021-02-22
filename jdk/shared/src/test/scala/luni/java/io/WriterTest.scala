package luni.java.io

import java.io.{IOException, Writer}

import org.scalatest.funsuite.AnyFunSuite

class WriterTest extends AnyFunSuite {

  test("appendChar") {
    val testChar = ' '
    val writer   = new MockWriter(20)
    writer.append(testChar)
    assert(String.valueOf(testChar) === String.valueOf(writer.getContents))
    writer.close()
  }

  test("appendCharSequence") {
    val testString = "My Test String"
    val writer     = new MockWriter(20)
    writer.append(testString)
    assert(testString === String.valueOf(writer.getContents))
    writer.close()
  }

  test("appendCharSequenceIntInt") {
    val testString = "My Test String"
    val writer     = new MockWriter(20)
    writer.append(testString, 1, 3)
    assert(testString.substring(1, 3) === String.valueOf(writer.getContents))
    writer.close()
  }

  private[io] class MockWriter private[io] (var length: Int) extends Writer {
    private var contents = new Array[Char](length)
    private var offset   = 0

    def close(): Unit = {
      flush()
      contents = null
    }

    def flush(): Unit = {
      // do nothing
    }

    def write(buffer: Array[Char], offset: Int, _count: Int): Unit = {
      if (null == contents) throw new IOException
      if (offset < 0 || _count < 0 || offset >= buffer.length) throw new IndexOutOfBoundsException
      val read1    = Math.min(_count, buffer.length - offset)
      val byteRead = Math.min(read1, this.length - this.offset)
      for (i <- 0 until byteRead) {
        contents(this.offset + i) = buffer(offset + i)
      }
      this.offset += byteRead
    }

    def getContents: Array[Char] = {
      val result = new Array[Char](offset)
      for (i <- 0 until offset) {
        result(i) = contents(i)
      }
      result
    }
  }

}
