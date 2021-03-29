package java.io

import io.scalajs.nodejs.buffer.Buffer
import io.scalajs.nodejs.fs.Fs

import java.nio._
import java.nio.channels._
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

  def getChannel(): FileChannel = {
    new FileOutputStreamChannel(this)
  }
}

private[io] final class FileOutputStreamChannel(val stream: FileOutputStream) extends FileChannel {

  override def read(dst: ByteBuffer): Int = throw new NonReadableChannelException()

  override def read(dsts: Array[ByteBuffer], offset: Int, length: Int): Long =
    throw new NonReadableChannelException()

  override def write(src: ByteBuffer): Int = {
    if (stream.getFD.valid()) {
      val writeBuffer = src.array()
      stream.write(writeBuffer)
      writeBuffer.length
    } else {
      throw new ClosedChannelException()
    }
  }

  override def write(srcs: Array[ByteBuffer], offset: Int, length: Int): Long = ???

  override def position(): Long = ???

  override def position(newPosition: Long): FileChannel = ???

  override def size(): Long = ???

  override def truncate(size: Long): FileChannel = ???

  override def force(metaData: Boolean): Unit = ???

  override def transferTo(position: Long, count: Long, target: WritableByteChannel): Long = ???

  override def transferFrom(src: ReadableByteChannel, position: Long, count: Long): Long = ???

  override def read(dst: ByteBuffer, position: Long): Int = ???

  override def write(src: ByteBuffer, position: Long): Int = ???

  override def map(
      mode: _root_.java.nio.channels.FileChannel.MapMode,
      position: Long,
      size: Long
  ): MappedByteBuffer = ???

  override def lock(position: Long, size: Long, shared: Boolean): FileLock = ???

  override def tryLock(position: Long, size: Long, shared: Boolean): FileLock = ???

  override def implCloseChannel(): Unit = {
    stream.getFD.invalidate()
  }
}
