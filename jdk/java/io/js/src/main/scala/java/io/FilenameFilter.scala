package java.io

@FunctionalInterface
trait FilenameFilter {
  def accept(dir: File, name: String): Boolean
}
