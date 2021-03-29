package java.nio.channels

import java.nio.ByteBuffer

trait SeekableByteChannel extends ByteChannel {
  def position(): Long
  def position(newPosition: Long): SeekableByteChannel
  def size(): Long
  def truncate(size: Long): SeekableByteChannel
}
