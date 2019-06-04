package java.io

import io.scalajs.nodejs.buffer.Buffer
import io.scalajs.nodejs.fs.Fs

import scala.scalajs.js
import scala.scalajs.js.JSConverters._

class FileOutputStream(file: File, append: Boolean) extends OutputStream {

  def this(file: File) = this(file, false)
  def this(file: String) = this(new File(file))
  def this(file: String, append: Boolean) = this(new File(file), append)

  if (file.isDirectory) {
    throw new IOException("Got a directory")
  }
  private var fd = Fs.openSync(file.getPath, if (append) "a" else "w")

  override def write(bytes: Array[Byte]): Unit = {
    val buffer: Buffer = Buffer.from(bytes.toJSArray.asInstanceOf[js.Array[Int]])
    Fs.writeSync(this.fd, buffer)
  }

  def write(byte: Int): Unit = {
    val buffer: Buffer = Buffer.from(js.Array[Int](byte))
    Fs.writeSync(this.fd, buffer)
  }

  override def close(): Unit = {
    Fs.closeSync(fd)
    this.fd = -1
    // TODO: not implemented
  }

}
