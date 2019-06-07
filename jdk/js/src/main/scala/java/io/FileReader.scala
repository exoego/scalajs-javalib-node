package java.io

import java.nio.charset.Charset

class FileReader private (in: FileInputStream, charset: Charset)
    extends InputStreamReader(in, charset) {

  def this(file: File, charset: Charset) {
    this(new FileInputStream(file), charset)
  }

  def this(file: File) {
    this(file, Charset.defaultCharset())
  }

  def this(fileName: String, charset: Charset) {
    this(new File(fileName), charset)
  }
  def this(fileName: String) {
    this(fileName, Charset.defaultCharset())
  }

  def this(fd: FileDescriptor) {
    this("")
    throw new UnsupportedOperationException()
  }
}
