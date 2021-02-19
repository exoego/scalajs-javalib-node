package java.nio.file
import java.io.IOException
import java.nio.file.attribute.BasicFileAttributes

class SimpleFileVisitor[T] protected () extends FileVisitor[T] {
  override def preVisitDirectory(dir: T, attrs: BasicFileAttributes): FileVisitResult = {
    FileVisitResult.CONTINUE
  }

  override def visitFile(file: T, attrs: BasicFileAttributes): FileVisitResult = {
    FileVisitResult.CONTINUE
  }

  override def visitFileFailed(file: T, exc: IOException): FileVisitResult = {
    // exc is not null
    throw exc
  }

  override def postVisitDirectory(dir: T, exc: IOException): FileVisitResult = {
    // exc could be null if visited successfully
    if (exc != null) {
      throw exc
    } else {
      FileVisitResult.CONTINUE
    }
  }
}
