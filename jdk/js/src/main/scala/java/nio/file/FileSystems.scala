package java.nio.file

import java.net.URI
import java.util.{Map => JavaMap}

object FileSystems {

  def getDefault(): FileSystem = FileSystemImpl

  def getFileSystem(uri: URI): FileSystem = throw new UnsupportedOperationException("getFileSystem")

  def newFileSystem(uri: URI, env: JavaMap[String, _]): FileSystem =
    throw new UnsupportedOperationException("newFileSystem")

  def newFileSystem(path: Path, env: JavaMap[String, _]): FileSystem =
    throw new UnsupportedOperationException("newFileSystem")
}
