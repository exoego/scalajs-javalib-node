package java.io

import io.scalajs.nodejs.buffer.Buffer
import io.scalajs.nodejs.fs.Fs

import scala.scalajs.js
import scala.scalajs.js.JSConverters._

class FileInputStream(descriptor: FileDescriptor) extends InputStream {

  def this(file: File) {
    this({
      if (file.getPath.isEmpty) {
        throw new FileNotFoundException()
      }

      if (file.isDirectory) {
        throw new IOException("Got a directory")
      }
      if (file.exists()) {
        FileDescriptorFactory.createInternal(Fs.openSync(file.getPath, "r"), readOnly = true)
      } else {
        new FileDescriptor
      }
    })
  }

  def this(file: String) {
    this(new File(file))
  }

  private def openCheck(): Unit = {
    if (!descriptor.valid()) throw new IOException("stream is closed")
  }

  override def available(): Int = {
    openCheck()
    if (!descriptor.valid()) throw new IOException()
    Fs.fstatSync(this.descriptor.internal).size.toInt
  }

  override def close(): Unit = {
    if (descriptor.valid()) {
      Fs.closeSync(descriptor.internal)
      descriptor.invalidate()
    }
  }

  override def read(): Int = {
    openCheck()
    val buffer     = new Array[Byte](1)
    val bufferSize = buffer.length
    val singleByte = Buffer.from(buffer.toJSArray.asInstanceOf[js.Array[Int]])
    val bytesRead = Fs
      .asInstanceOf[js.Dynamic]
      .readSync(this.descriptor.internal, singleByte, 0, bufferSize, null)
      .asInstanceOf[Int]
    if (bytesRead > 0) {
      singleByte(0)
    } else {
      -1
    }
  }

  override def read(b: Array[Byte], off: Int, len: Int): Int = {
    if (b == null) {
      throw new NullPointerException
    }
    super.read(b, off, len)
  }

  override def skip(n: Long): Long = {
    if (n < 0) throw new IOException()
    super.skip(n)
  }

  def getFD(): FileDescriptor = this.descriptor
}
