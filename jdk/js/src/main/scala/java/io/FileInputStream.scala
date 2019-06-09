package java.io

import io.scalajs.nodejs
import io.scalajs.nodejs.buffer.Buffer
import io.scalajs.nodejs.fs.Fs

import scala.scalajs.js

class FileInputStream(file: File) extends InputStream {

  if (file.getPath().isEmpty) {
    throw new FileNotFoundException()
  }

  if (file.isDirectory) {
    throw new IOException("Got a directory")
  }

  private[this] var closed = false
  private[this] val fileDescriptor: nodejs.FileDescriptor = if (file.exists()) {
    Fs.openSync(file.getPath, "r")
  } else {
    -1
  }

  def this(file: String) {
    this(new File(file))
  }

  override def available(): Int = {
    if (closed) throw new IOException("stream is closed")
    Fs.fstatSync(this.fileDescriptor).size.toInt
  }

  override def close(): Unit = {
    this.closed = true
    Fs.closeSync(this.fileDescriptor)
  }

  override def read(): Int = {
    val bufferSize = 1
    val singleByte = Buffer.alloc(bufferSize)
    val bytesRead = Fs
      .asInstanceOf[js.Dynamic]
      .readSync(this.fileDescriptor, singleByte, 0, bufferSize, null)
      .asInstanceOf[Int]
    if (bytesRead > 0) {
      singleByte(0)
    } else {
      -1
    }
  }

  def getFD(): FileDescriptor = FileDescriptorFactory.createInternal(this.fileDescriptor)
}
