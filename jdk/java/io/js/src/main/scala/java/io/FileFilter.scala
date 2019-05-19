package java.io

@FunctionalInterface
trait FileFilter {
  def accept(file: File): Boolean
}
