package java.nio.file

import java.net.URI
import java.util.{Map => JavaMap}

object FileSystems {

  private lazy val defaultFileSystem: FileSystem = System.getProperty("os.name") match {
    case "Mac OS X" => MacOsXFileSystem
    case "Linux"    => MacOsXFileSystem
    case otherwise =>
      throw new UnsupportedOperationException(s"FileSystem for '${otherwise}' is not implemented")
  }

  @inline def getDefault(): FileSystem = defaultFileSystem

  def getFileSystem(uri: URI): FileSystem = {
    if (uri.getScheme() == "file") {
      getDefault()
    } else {
      throw new UnsupportedOperationException("getFileSystem")
    }
  }

  def newFileSystem(uri: URI, env: JavaMap[String, _]): FileSystem =
    throw new UnsupportedOperationException("newFileSystem")

  def newFileSystem(path: Path, env: JavaMap[String, _]): FileSystem =
    throw new UnsupportedOperationException("newFileSystem")
}
