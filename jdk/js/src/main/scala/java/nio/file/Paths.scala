package java.nio.file

import java.io.File
import scala.annotation.varargs

object Paths {
  @varargs def get(first: String, more: String*): Path = {
    new File((first +: more).mkString(File.separator)).toPath()
  }
}
