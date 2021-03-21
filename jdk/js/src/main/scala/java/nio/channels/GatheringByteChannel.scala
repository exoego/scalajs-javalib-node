package java.nio.channels

import java.nio.ByteBuffer

trait GatheringByteChannel extends WritableByteChannel {
  def write(src: Array[ByteBuffer]): Long
  def write(src: Array[ByteBuffer], offset: Int, length: Int): Long
}
