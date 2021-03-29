package java.nio.channels

import java.nio.ByteBuffer

trait AsynchronousByteChannel extends AsynchronousChannel {
  def read(dst: ByteBuffer): java.util.concurrent.Future[Integer]
  def read[A](dst: ByteBuffer, attachment: A, handler: CompletionHandler[Integer, _ >: A]): Unit

  def write(src: ByteBuffer): java.util.concurrent.Future[Integer]
  def write[A](src: ByteBuffer, attachment: A, handler: CompletionHandler[Integer, _ >: A]): Unit
}
