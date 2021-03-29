package luni.java.io

import luni.java.nio.channels.{ReadableByteChannelTest, WritableByteChannelTest}

import java.io.RandomAccessFile
import java.nio.channels.{FileChannel, WritableByteChannel}
import java.nio.file.{Files, Path}
import scala.jdk.CollectionConverters._

class RandomAccessFileTest extends ReadableByteChannelTest with WritableByteChannelTest {
  private val path = Files.createTempFile("rw", ".md")

  override def readFactory(): FileChannel = {
    val path = Files.createTempFile("r", ".md")
    Files.write(path, Seq("txt").asJava)
    new RandomAccessFile(path.toFile, "rw").getChannel()
  }

  override def writeChannelPath(): Path = path

  override def writeFactory(): WritableByteChannel = {
    new RandomAccessFile(path.toFile, "rw").getChannel()
  }
}
