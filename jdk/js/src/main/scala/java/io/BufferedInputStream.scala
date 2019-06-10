package java.io

import scala.scalajs.js.JavaScriptException

class BufferedInputStream protected (
    in: InputStream,
    protected var buf: Array[Byte],
    protected var count: Int,
    protected var marklimit: Int,
    protected var markpos: Int,
    protected var pos: Int
) extends FilterInputStream(in) {

  private var isClosed = false

  def this(in: InputStream, size: Int) {
    this(in, {
      if (size <= 0) throw new IllegalArgumentException()
      Array.ofDim(size)
    }, count = 0, marklimit = 0, markpos = -1, pos = 0)
  }

  def this(in: InputStream) {
    this(in, 8192)
  }

  override def available: Int = {
    if (buf == null || this.isClosed) throw new IOException()
    count - pos + in.available
  }

  override def close(): Unit = {
    this.buf = null
    this.isClosed = true
    if (in != null)
      in.close()
  }

  private def fillbuf(localIn: InputStream, localBuf_ : Array[Byte]): Int = {
    var localBuf = localBuf_

    if (this.markpos == -1 || this.pos - this.markpos >= this.marklimit) {
      /* Mark position not set or exceeded readlimit */
      val result = localIn.read(localBuf)
      if (result > 0) {
        this.markpos = -1
        this.pos = 0
        this.count = if (result == -1) 0 else result
      }
      return result
    }

    if ((markpos == 0) && marklimit > localBuf.length) {
      /* Increase buffer size to accommodate the readlimit */
      var newLength = localBuf.length * 2
      if (newLength > marklimit) newLength = marklimit
      val newbuf = new Array[Byte](newLength)
      System.arraycopy(localBuf, 0, newbuf, 0, localBuf.length)
      // Reassign buf, which will invalidate any local references
      // FIXME: what if buf was null?
      buf = newbuf
      localBuf = newbuf
    } else if (markpos > 0)
      System.arraycopy(localBuf, markpos, localBuf, 0, localBuf.length - markpos)

    /* Set the new position and mark position */
    this.pos -= this.markpos
    this.count = 0
    this.markpos = 0
    val bytesread = localIn.read(localBuf, pos, localBuf.length - pos)
    count =
      if (bytesread <= 0) pos
      else pos + bytesread
    count
  }

  override def mark(readlimit: Int): Unit = {
    this.marklimit = readlimit
    this.markpos = pos
  }

  override val markSupported = true

  override def read(): Int = {
    try {
      if (buf == null || this.isClosed) {
        throw new IOException()
      }
      if (pos >= count && (fillbuf(in, buf) == -1))
        -1
      /* Did filling the buffer fail with -1 (EOF)? */
      else if (count - pos > 0)
        buf({ pos += 1; pos - 1 }) & 0xFF
      else
        -1
    } catch {
      case e: JavaScriptException => throw new IOException(e)
    }
  }

  override def read(buffer: Array[Byte], _offset: Int, length: Int): Int = {
    var offset   = _offset
    var localBuf = buf
    if (localBuf == null) throw new IOException()
    if (buffer == null) throw new NullPointerException
    // avoid int overflow
    if (offset > buffer.length - length || offset < 0 || length < 0)
      throw new IndexOutOfBoundsException
    if (length == 0) return 0
    if (isClosed) throw new IOException()

    var required =
      if (pos < count) {
        /* There are bytes available in the buffer. */
        val copylength =
          if (count - pos >= length) length
          else count - pos
        System.arraycopy(localBuf, pos, buffer, offset, copylength)
        pos += copylength
        if (copylength == length || in.available == 0) return copylength
        offset += copylength
        length - copylength
      } else length

    while (true) {
      var read = 0
      /*
       * If we're not marked and the required size is greater than the
       * buffer, simply read the bytes directly bypassing the buffer.
       */
      if ((markpos == -1) && required >= localBuf.length) {
        read = in.read(buffer, offset, required)
        if (read == -1)
          return if (required == length) -1
          else length - required
      } else {
        if (fillbuf(in, localBuf) == -1)
          return if (required == length) -1
          else length - required
        // localBuf may have been invalidated by fillbuf
        if (localBuf ne buf) {
          localBuf = buf
          if (localBuf == null) throw new IOException()
        }
        read =
          if (count - pos >= required) required
          else count - pos
        System.arraycopy(localBuf, pos, buffer, offset, read)
        pos += read
      }
      required -= read
      if (required == 0) return length
      if (in.available == 0) return length - required
      offset += read
    }
    -1
  }

  override def reset(): Unit = {
    if (buf == null) {
      throw new IOException()
    }
    if (-1 == markpos) {
      throw new IOException()
    }
    pos = markpos
  }

  override def skip(amount: Long): Long = {
    if (buf == null) {
      throw new IOException()
    }
    if (amount < 1) return 0
    if (in == null) throw new IOException()
    if (count - pos >= amount) {
      this.pos += amount.toInt
      return amount
    }
    var read = count - pos
    this.pos = count

    if (markpos != -1 && amount <= marklimit) {
      if (fillbuf(in, buf) == -1) return read
      if (count - pos >= amount - read) {
        this.pos += (amount - read).toInt
        return amount
      }
      // Couldn't get all the bytes, skip what we read
      read += (count - pos)
      this.pos = count
      return read
    }
    read + in.skip(amount - read)
  }
}
