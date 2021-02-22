package java.io

import io.scalajs.nodejs.buffer.Buffer
import io.scalajs.nodejs.fs.Fs

import java.nio.channels.FileChannel
import scala.scalajs.js
import scala.scalajs.js.JSConverters._

class FileOutputStream(private[this] val fd: FileDescriptor) extends OutputStream {

  def this(file: File, append: Boolean) = {
    this({
      if (file.isDirectory()) {
        throw new IOException("Got a directory")
      }
      if (file.getPath().isEmpty) {
        throw new FileNotFoundException()
      }
      FileDescriptorFactory.openWrite(file.getPath(), append)
    })
  }

  def this(file: File) = this(file, false)
  def this(file: String) = this(new File(file))
  def this(file: String, append: Boolean) = this(new File(file), append)

  def write(byte: Int): Unit = {
    openCheck()
    val buffer: Buffer = Buffer.from(js.Array[Int](byte))
    Fs.writeSync(this.fd.internal, buffer)
  }

  override def write(buffer: Array[Byte]): Unit = {
    write(buffer, 0, buffer.length)
  }

  override def write(buffer: Array[Byte], offset: Int, count: Int): Unit = {
    if (buffer == null) throw new NullPointerException
    if (count < 0 || offset < 0 || offset > buffer.length || count > buffer.length - offset)
      throw new IndexOutOfBoundsException
    if (count == 0) return
    openCheck()
    val jsBuffer: Buffer = Buffer.from(buffer.toJSArray.asInstanceOf[js.Array[Int]])
    Fs.writeSync(this.fd.internal, jsBuffer, offset, count)
  }

  private def openCheck(): Unit = {
    if (!fd.valid()) throw new IOException("stream is closed")
  }

  override def close(): Unit = {
    if (fd.valid()) {
      Fs.closeSync(fd.internal)
      fd.invalidate()
    }
  }

  val getFD: FileDescriptor = fd

  def getChannel(): FileChannel = throw new NotImplementedError("getChannel")
}
