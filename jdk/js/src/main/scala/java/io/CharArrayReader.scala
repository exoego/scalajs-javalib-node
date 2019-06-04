package java.io

class CharArrayReader protected (
    val buf: Array[Char],
    val count: Int,
    var markedPos: Int,
    var pos: Int
) extends Reader {

  private[this] var closed = false

  def this(buf: Array[Char]) {
    this(buf, buf.length, 0, 0)
  }

  def this(buf: Array[Char], offset: Int, length: Int) {
    this(buf, buf.length, 0, 0)
  }

  override def read(cbuf: Array[Char], off: Int, len: Int): Int = {
    ensureOpen()

    if (off < 0 || len < 0 || len > cbuf.length - off) {
      throw new IndexOutOfBoundsException
    }

    if (len == 0) {
      0
    } else {
      val count = Math.min(len, buf.length - pos)
      var i     = 0
      while (i < count) {
        cbuf(off + i) = buf.charAt(pos + i)
        i += 1
      }
      pos += count
      if (count == 0) -1 else count
    }
  }

  override def close(): Unit = {
    this.closed = true
  }

  private def ensureOpen(): Unit = {
    if (this.closed)
      throw new IOException("Operation on closed stream")
  }

}
