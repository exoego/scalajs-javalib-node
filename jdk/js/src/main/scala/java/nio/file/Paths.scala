package java.nio.file

import java.io.File

object Paths {
  def get(first: String, more: String*): Path = {
    new File((first +: more).mkString(File.separator)).toPath()
  }
}
