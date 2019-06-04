package java.io

class BufferedWriter(private[this] var out: Writer, size: Int) extends Writer {
  if (size <= 0) throw new IllegalArgumentException()

  private final val lineSeparator = System.getProperty("line.separator")

  private var buf = new Array[Char](size)
  private var pos = 0

  private def isClosed = {
    out == null
  }

  def this(out: Writer) {
    this(out, 8192)
  }

  override def close(): Unit = {
    if (isClosed) return

    var thrown: Throwable = null
    try flushInternal()
    catch {
      case e: Throwable => thrown = e
    }
    buf = null

    try out.close()
    catch {
      case e: Throwable =>
        if (thrown == null) thrown = e
    }
    out = null
    if (thrown != null) throw thrown
  }

  def flush(): Unit = {
    if (isClosed) throw new IOException()
    flushInternal()
    out.flush()
  }

  private def flushInternal(): Unit = {
    if (pos > 0) out.write(buf, 0, pos)
    pos = 0
  }

  def newLine(): Unit = {
    write(lineSeparator, 0, lineSeparator.length)
  }

  def write(cbuf: Array[Char], offset: Int, count: Int): Unit = {
    if (isClosed) throw new IOException()
    if (offset < 0 || {
          if (cbuf == null) throw new NullPointerException
          offset > cbuf.length - count || count < 0
        }) {
      throw new IndexOutOfBoundsException
    }
    if (pos == 0 && count >= this.buf.length) {
      out.write(cbuf, offset, count)
    } else {
      var available = this.buf.length - pos
      if (count < available) available = count
      if (available > 0) {
        System.arraycopy(cbuf, offset, this.buf, pos, available)
        pos += available
      }

      if (pos == this.buf.length) {
        out.write(this.buf, 0, this.buf.length)
        pos = 0
        if (count > available) {
          val offset2 = offset + available
          available = count - available
          if (available >= this.buf.length) {
            out.write(cbuf, offset2, available)
          } else {
            System.arraycopy(cbuf, offset2, this.buf, pos, available)
            pos += available
          }
        }
      }
    }
  }

  override def write(oneChar: Int): Unit = {
    if (isClosed) throw new IOException()
    if (pos >= buf.length) {
      out.write(buf, 0, buf.length)
      pos = 0
    }
    buf({ pos += 1; pos - 1 }) = oneChar.toChar
  }

  override def write(str: String, offset: Int, count: Int): Unit = {
    if (isClosed) throw new IOException()
    if (count > 0) {
      if (str == null) throw new NullPointerException()
      if (offset > str.length - count || offset < 0) throw new StringIndexOutOfBoundsException()
      if (pos == 0 && count >= buf.length) {
        val chars = new Array[Char](count)
        str.getChars(offset, offset + count, chars, 0)
        out.write(chars, 0, count)
      } else {
        var available = buf.length - pos
        if (count < available) available = count
        if (available > 0) {
          str.getChars(offset, offset + available, buf, pos)
          pos += available
        }
        if (pos == buf.length) {
          out.write(this.buf, 0, this.buf.length)
          pos = 0
          if (count > available) {
            val offset2 = offset + available
            available = count - available
            if (available >= buf.length) {
              val chars = new Array[Char](count)
              str.getChars(offset2, offset2 + available, chars, 0)
              out.write(chars, 0, available)
            } else {
              str.getChars(offset2, offset2 + available, buf, pos)
              pos += available
            }
          }
        }
      }
    }
  }
}
