package luni.java.nio.channels

import org.scalatest.freespec.AnyFreeSpec

import java.nio.ByteBuffer
import java.nio.channels.{ClosedChannelException, WritableByteChannel}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import scala.jdk.CollectionConverters._

trait WritableByteChannelTest extends AnyFreeSpec {
  def writeChannelPath(): Path
  def writeFactory(): WritableByteChannel

  private val utf8 = StandardCharsets.UTF_8

  "writable" in {
    val writeOnlyChannel = writeFactory()

    val data = ByteBuffer.wrap("foo".getBytes(utf8))
    assert(writeOnlyChannel.write(data) > 0)
    assert(Files.readAllLines(writeChannelPath()).asScala === Seq("foo"))
    writeOnlyChannel.close()
  }

  "read throw ClosedChannelException" in {
    val readonlyChannel = writeFactory()

    readonlyChannel.close()
    assertThrows[ClosedChannelException] {
      readonlyChannel.write(ByteBuffer.allocate(1))
    }
    readonlyChannel.close()
  }
}
