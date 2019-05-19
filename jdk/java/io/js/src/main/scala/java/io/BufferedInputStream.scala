package java.io

class BufferedInputStream protected (
    in: InputStream,
    protected var buf: Array[Byte],
    protected var count: Int,
    protected var marklimit: Int,
    protected var markpos: Int,
    protected var pos: Int
) extends FilterInputStream(in) {
  def this(in: InputStream, size: Int) {
    this(in, Array.ofDim(size), 0, 0, 0, 0)
  }

  def this(in: InputStream) {
    this(in, 4096)
  }

}
