package java.io

import io.scalajs.nodejs.buffer.Buffer
import io.scalajs.nodejs.fs.{BigIntStats, Fs, Stats}

import scala.scalajs.js
import scala.scalajs.js.JSConverters._

class FileInputStream(descriptor: FileDescriptor) extends InputStream {

  def this(file: File) = {
    this({
      if (file.getPath().isEmpty) {
        throw new FileNotFoundException()
      }

      if (file.isDirectory()) {
        throw new IOException("Got a directory")
      }
      if (file.exists()) {
        FileDescriptorFactory.createInternal(Fs.openSync(file.getPath(), "r"), readOnly = true)
      } else {
        new FileDescriptor
      }
    })
  }

  def this(file: String) = {
    this(new File(file))
  }

  private def openCheck(): Unit = {
    if (!descriptor.valid()) throw new IOException("stream is closed")
  }

  override def available(): Int = {
    openCheck()
    if (!descriptor.valid()) throw new IOException()

    Fs.fstatSync(this.descriptor.internal).asInstanceOf[Stats].size.toInt
  }

  override def close(): Unit = {
    if (descriptor.valid()) {
      Fs.closeSync(descriptor.internal)
      descriptor.invalidate()
    }
  }

  override def read(): Int = {
    val buffer = Array[Byte](1)
    if (read(buffer) == -1) {
      -1
    } else {
      buffer(0)
    }
  }

  private var position: Long = 0

  override def read(buffer: Array[Byte], off: Int, len: Int): Int = {
    if (buffer == null) {
      throw new NullPointerException
    }
    openCheck()
    val bufferSize = buffer.length
    val jsBuffer   = Buffer.alloc(bufferSize)
    val bytesRead  = Fs.readSync(this.descriptor.internal, jsBuffer, 0, bufferSize, position.toInt)
    for (i <- buffer.indices) {
      buffer(i) = jsBuffer(i).toByte
    }
    position += bytesRead
    if (bytesRead > 0) {
      bytesRead
    } else {
      -1
    }
  }

  override def skip(n: Long): Long = {
    if (n < 0) throw new IOException()
    position += n
    super.skip(n)
  }

  def getFD(): FileDescriptor = this.descriptor
}
