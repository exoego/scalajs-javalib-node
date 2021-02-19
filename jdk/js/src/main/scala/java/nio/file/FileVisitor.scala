package java.nio.file

import java.io.IOException
import java.nio.file.attribute.BasicFileAttributes

trait FileVisitor[T] {
  def postVisitDirectory(dir: T, exc: IOException): FileVisitResult
  def preVisitDirectory(dir: T, attrs: BasicFileAttributes): FileVisitResult
  def visitFile(file: T, attrs: BasicFileAttributes): FileVisitResult
  def visitFileFailed(file: T, exc: IOException): FileVisitResult
}

final class FileVisitResult protected[file] (name: String, ordinal: Int)
    extends Enum[FileVisitResult](name, ordinal)
object FileVisitResult {
  val CONTINUE      = new FileVisitResult("CONTINUE", 0)
  val SKIP_SIBLINGS = new FileVisitResult("SKIP_SIBLINGS", 1)
  val SKIP_SUBTREE  = new FileVisitResult("SKIP_SUBTREE", 2)
  val TERMINATE     = new FileVisitResult("TERMINATE", 3)
}
