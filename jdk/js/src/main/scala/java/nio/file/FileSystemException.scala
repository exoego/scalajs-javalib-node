package java.nio.file

import java.io.IOException

class FileSystemException(file: String, other: String, reason: String) extends IOException(reason) {
  def this(file: String) = this(file, null, null)
}
