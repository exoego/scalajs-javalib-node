package java.nio.channels

import java.io.Closeable

trait Channel extends Closeable {
  def isOpen(): Boolean
}
