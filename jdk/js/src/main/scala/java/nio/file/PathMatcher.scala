package java.nio.file

@FunctionalInterface
trait PathMatcher {
  def matches(path: Path): Boolean
}
