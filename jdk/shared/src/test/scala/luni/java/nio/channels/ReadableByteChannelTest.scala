package luni.java.nio.channels

import org.scalatest.freespec.AnyFreeSpec

import java.nio.ByteBuffer
import java.nio.channels._
import java.nio.charset.StandardCharsets

trait ReadableByteChannelTest extends AnyFreeSpec {
  def readFactory(): ReadableByteChannel

  private val utf8 = StandardCharsets.UTF_8

  "readable" in {
    val readonlyChannel = readFactory()

    val byteBuffer = ByteBuffer.allocate(16)
    assert(readonlyChannel.read(byteBuffer) > 0)
    assert(new String(byteBuffer.array(), utf8).trim === "txt")
    readonlyChannel.close()
  }

  "write throw ClosedChannelException" in {
    val readonlyChannel = readFactory()

    readonlyChannel.close()
    assertThrows[ClosedChannelException] {
      readonlyChannel.read(ByteBuffer.allocate(1))
    }
    readonlyChannel.close()
  }
}
