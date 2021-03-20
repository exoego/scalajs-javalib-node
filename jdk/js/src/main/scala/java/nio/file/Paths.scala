package java.nio.file

import java.io.File
import scala.annotation.varargs

object Paths {
  @varargs def get(first: String, more: String*): Path = {
    val joined = if (more.isEmpty) {
      first
    } else {
      val elements = first +: more
      if (elements.contains(null)) {
        throw new NullPointerException
      }
      elements.filter(_.nonEmpty).mkString(File.separator)
    }
    if (joined.contains('\u0000')) {
      throw new InvalidPathException("", "Nul character not allowed")
    }
    PathHelper.fromString(joined)
  }
}
