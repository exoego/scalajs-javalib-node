package java.io

class BufferedWriter(writer: Writer) extends Writer {

  private var isClosed = false

  override def write(chars: Array[Char], i: Int, i1: Int): Unit = {
    if (isClosed) throw new IOException("closed")
    // TODO: not implemented
  }

  override def flush(): Unit = {
    if (isClosed) throw new IOException("closed")
    // TODO: not implemented
  }

  override def close(): Unit = {
    this.isClosed = true
  }
}
