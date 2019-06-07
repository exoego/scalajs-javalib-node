package java.io

class RandomAccessFile(file: File, mode: String) extends Closeable {

  if (file.isDirectory) {
    throw new IOException("Got a directory")
  }

  def this(filename: String, mode: String) {
    this(new File(filename), mode)
  }

  def writeBytes(bytes: String): Unit = {
    // TODO: not implemented
  }

  def seek(n: Long): Unit = {
    // TODO: not implemented
  }

  def close(): Unit = {
    // TODO: not implemented
  }

  def length(): Long = {
    // TODO: not implemented
    0L
  }

  def getFD(): FileDescriptor = {
    // TODO: not implemented
    new FileDescriptor
  }
}
