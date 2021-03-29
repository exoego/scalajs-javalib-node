package luni.java.nio.channels

import org.scalatest.freespec.AnyFreeSpec

import java.io.{FileInputStream, FileOutputStream, RandomAccessFile}
import java.nio.ByteBuffer
import java.nio.channels.{
  ClosedChannelException,
  FileChannel,
  NonReadableChannelException,
  NonWritableChannelException
}
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import scala.jdk.CollectionConverters._

class FileChannelTest extends AnyFreeSpec {
  private val utf8 = StandardCharsets.UTF_8

  "conversion" - {
    "from FileInputStream" in {
      val readonlyChannel: FileChannel =
        new FileInputStream("jdk/shared/src/test/resources/regular.txt").getChannel()

      val byteBuffer = ByteBuffer.allocate(16)
      assert(readonlyChannel.read(byteBuffer) > 0)
      assert(new String(byteBuffer.array(), utf8).trim === "txt")

      assertThrows[NonWritableChannelException] {
        readonlyChannel.write(ByteBuffer.allocate(10))
      }

      readonlyChannel.close()
      assertThrows[ClosedChannelException] {
        readonlyChannel.read(byteBuffer)
      }
      readonlyChannel.close()
    }

    "from FileOutputStream" in {
      val path                          = Files.createTempFile("write", ".md")
      val writeOnlyChannel: FileChannel = new FileOutputStream(path.toFile).getChannel()

      val data = ByteBuffer.wrap("foo".getBytes(utf8))
      assert(writeOnlyChannel.write(data) > 0)
      assert(Files.readAllLines(path).asScala === Seq("foo"))

      assertThrows[NonReadableChannelException] {
        writeOnlyChannel.read(ByteBuffer.allocate(10))
      }

      writeOnlyChannel.close()
      assertThrows[ClosedChannelException] {
        writeOnlyChannel.write(data)
      }
      writeOnlyChannel.close()
    }

    "from RandomAccessFile" in {
      val path      = Files.createTempFile("rw", ".md")
      val rwFile    = new RandomAccessFile(path.toFile, "rw")
      val rwChannel = rwFile.getChannel()

      val data = ByteBuffer.wrap("bar".getBytes(utf8))
      assert(rwChannel.write(data) > 0)
      assert(Files.readAllLines(path).asScala === Seq("bar"))

      rwChannel.close()
      assertThrows[ClosedChannelException] {
        rwFile.getChannel.read(data)
      }
      assertThrows[ClosedChannelException] {
        rwFile.getChannel.write(data)
      }
      rwChannel.close()

      val rwChannel2 = new RandomAccessFile(path.toFile, "r").getChannel()
      val byteBuffer = ByteBuffer.allocate(16)
      assert(rwChannel2.read(byteBuffer) > 0)
      assert(new String(byteBuffer.array(), utf8).trim === "bar")
    }
  }
}
