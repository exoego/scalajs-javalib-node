package java.nio.file

final class FileVisitOption protected[file] (name: String, ordinal: Int)
    extends Enum[FileVisitOption](name, ordinal)
object FileVisitOption {
  val FOLLOW_LINKS = new FileVisitOption("FOLLOW_LINKS", 0)
}
