package java.nio.channels

import java.io._
import java.nio.charset.{Charset, CharsetDecoder}

object Channels {
  def newChannel(in: InputStream): ReadableByteChannel   = ???
  def newChannel(out: OutputStream): WritableByteChannel = ???

  def newInputStream(ch: AsynchronousByteChannel): InputStream = ???
  def newInputStream(ch: ReadableByteChannel): InputStream     = ???

  def newOutputStream(ch: AsynchronousByteChannel): OutputStream = ???
  def newOutputStream(ch: WritableByteChannel): OutputStream     = ???

  def newReader(ch: ReadableByteChannel, csName: String): Reader                         = ???
  def newReader(ch: ReadableByteChannel, charset: Charset): Reader                       = ???
  def newReader(ch: ReadableByteChannel, dec: CharsetDecoder, minBufferCap: Int): Reader = ???

  def newWriter(ch: WritableByteChannel, csName: String): Writer                         = ???
  def newWriter(ch: WritableByteChannel, charset: Charset): Writer                       = ???
  def newWriter(ch: WritableByteChannel, dec: CharsetDecoder, minBufferCap: Int): Writer = ???
}
