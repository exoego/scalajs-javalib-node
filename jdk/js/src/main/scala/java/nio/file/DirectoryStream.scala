package java.nio.file

import java.io.Closeable
import java.lang.{Iterable => JavaIterable}

trait DirectoryStream[T] extends Closeable with AutoCloseable with JavaIterable[T]

object DirectoryStream {
  @FunctionalInterface
  trait Filter[T] {
    def accept(entry: T): Boolean
  }
}
