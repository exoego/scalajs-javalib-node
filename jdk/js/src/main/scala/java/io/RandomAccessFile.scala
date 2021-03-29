package java.io

import java.nio.{ByteBuffer, MappedByteBuffer}
import java.nio.channels._
import io.scalajs.nodejs.fs.Fs

import scala.scalajs.js
import js.typedarray._

class RandomAccessFile(file: File, mode: String) extends Closeable {
  private[io] val isReadOnly: Boolean = mode == "r"

  private val nodeFD: FileDescriptor = {
    val rawPath = file.toPath().toString
    mode match {
      case "r" =>
        if (!file.exists()) {
          throw new FileNotFoundException(rawPath)
        }
        FileDescriptorFactory.openRead(rawPath)
      case "rw" => FileDescriptorFactory.openReadAppend(rawPath, failIfExists = false)
      case "rws" =>
        FileDescriptorFactory.openReadAppend(rawPath, failIfExists = false) // TODO: synchronous
      case "rwd" =>
        FileDescriptorFactory.openReadAppend(rawPath, failIfExists = false) // TODO: synchronous
      case _ => throw new IllegalArgumentException(s"unsupported mode: <$mode>")
    }
  }

  if (file.isDirectory()) {
    throw new IOException("Got a directory")
  }

  def this(filename: String, mode: String) = {
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

  def getFD(): FileDescriptor = this.nodeFD

  def getChannel(): FileChannel =
    if (isReadOnly) {
      new ReadOnlyRandomAccessFileChannel(this)
    } else {
      new RandomAccessFileChannel(this)
    }
}

private final class RandomAccessFileChannel(override val raFile: RandomAccessFile)
    extends ReadOnlyRandomAccessFileChannel(raFile) {

  override def write(src: ByteBuffer): Int = {
    if (raFile.getFD().valid()) {
      val writeBuffer = src.array().toTypedArray
      Fs.writeSync(raFile.getFD().internal, writeBuffer)
      writeBuffer.length
    } else {
      throw new ClosedChannelException()
    }
  }

  override def write(srcs: Array[ByteBuffer], offset: Int, length: Int): Long = ???

  override def write(src: ByteBuffer, position: Long): Int = ???

  override def truncate(size: Long): FileChannel = ???

  override def transferFrom(src: ReadableByteChannel, position: Long, count: Long): Long = ???
}

private class ReadOnlyRandomAccessFileChannel(val raFile: RandomAccessFile) extends FileChannel {
  override def read(dst: ByteBuffer, position: Long): Int = ???

  override def read(dst: ByteBuffer): Int =
    if (raFile.getFD().valid()) {
      val jsBuffer = dst.array().toTypedArray
      val i =
        Fs.asInstanceOf[js.Dynamic].readSync(raFile.getFD().internal, jsBuffer).asInstanceOf[Int]
      dst.put(jsBuffer.to(Array))
      i
    } else {
      throw new ClosedChannelException()
    }

  override def read(dsts: Array[ByteBuffer], offset: Int, length: Int): Long = ???

  override def write(src: ByteBuffer): Int = throw new NonWritableChannelException()

  override def write(srcs: Array[ByteBuffer], offset: Int, length: Int): Long =
    throw new NonWritableChannelException()

  override def write(src: ByteBuffer, position: Long): Int = throw new NonWritableChannelException()

  override def position(): Long = ???

  override def position(newPosition: Long): FileChannel = ???

  override def size(): Long = ???

  override def truncate(size: Long): FileChannel = ???

  override def force(metaData: Boolean): Unit = ???

  override def transferTo(position: Long, count: Long, target: WritableByteChannel): Long = ???

  override def transferFrom(src: ReadableByteChannel, position: Long, count: Long): Long = ???

  override def map(
      mode: _root_.java.nio.channels.FileChannel.MapMode,
      position: Long,
      size: Long
  ): MappedByteBuffer = ???

  override def lock(position: Long, size: Long, shared: Boolean): FileLock = ???

  override def tryLock(position: Long, size: Long, shared: Boolean): FileLock = ???

  override def implCloseChannel(): Unit = {
    raFile.getFD().invalidate()
  }
}
