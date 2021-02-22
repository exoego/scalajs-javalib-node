package java.nio.channels

import java.nio.{ByteBuffer, MappedByteBuffer}

object FileChannel {
  object MapMode {
    val PRIVATE    = new MapMode("PRIVATE")
    val READ_ONLY  = new MapMode("READ_ONLY")
    val READ_WRITE = new MapMode("READ_WRITE")
  }

  final class MapMode private (name: String)
}

abstract class FileChannel {
  def force(metadata: Boolean): Unit

  final def lock(): FileLock = this.lock(0L, Long.MaxValue, false)

  def lock(position: Long, size: Long, stared: Boolean): FileLock

  def map(mode: FileChannel.MapMode, position: Long, size: Long): MappedByteBuffer

  def position(): Long

  def position(newPosition: Long): FileChannel

  def read(dst: ByteBuffer): Int

  final def read(dsts: Array[ByteBuffer]): Long = this.read(dsts, 0, dsts.length)

  def read(dsts: Array[ByteBuffer], offset: Int, length: Int): Long

  def read(dst: ByteBuffer, position: Long): Int

  def size(): Long

  def transferFrom(src: ReadableByteChannel, position: Long, count: Long): Long

  def transferTo(position: Long, count: Long, target: WritableByteChannel): Long

  def truncate(size: Long): FileChannel

  final def tryLock(): FileLock = this.tryLock(0L, Long.MaxValue, false)

  def tryLock(position: Long, size: Long, shared: Boolean): FileLock

  def write(src: ByteBuffer): Int

  final def write(srcs: Array[ByteBuffer]): Long = this.write(srcs, 0, srcs.length)

  def write(srcs: Array[ByteBuffer], offset: Int, length: Int): Long

  def write(src: Array[ByteBuffer], position: Long): Int
}
