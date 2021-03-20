package java.nio.file.spi

import java.io.{InputStream, OutputStream}
import java.util.{List => JavaList, Map => JavaMap, Set => JavaSet}
import java.net.URI
import java.nio.channels.{AsynchronousFileChannel, FileChannel, SeekableByteChannel}
import java.nio.file._
import java.nio.file.attribute.{BasicFileAttributes, FileAttribute, FileAttributeView}
import java.util.concurrent.ExecutorService
import scala.annotation.varargs

abstract class FileSystemProvider protected () {

  @varargs def checkAccess(path: Path, modes: AccessMode*): Unit

  @varargs def copy(source: Path, target: Path, options: CopyOption*): Unit

  @varargs def createDirectory(dir: Path, attrs: FileAttribute[_]*): Unit

  def createLink(link: Path, existing: Path): Unit =
    throw new UnsupportedOperationException("createLink not implemented")

  def createSymbolicLink(path: Path, target: Path, attrs: FileAttribute[_]*): Unit =
    throw new UnsupportedOperationException("createSymbolicLink not implemented")

  def delete(path: Path): Unit

  def deleteIfExists(path: Path): Boolean =
    throw new UnsupportedOperationException("deleteIfExists not implemented")

  @varargs def getFileAttributeView[V <: FileAttributeView](
      path: Path,
      clazz: Class[V],
      options: LinkOption*
  ): V

  def getFileStore(path: Path): FileStore

  def getFileSystem(uri: URI): FileSystem

  def getPath(uri: URI): Path

  def getScheme(): String

  def isHidden(path: Path): Boolean

  def isSameFile(path1: Path, path2: Path): Boolean

  @varargs def move(source: Path, target: Path, options: CopyOption*): Unit

  @varargs def newAsynchronousFileChannel(
      path: Path,
      options: JavaSet[_ <: OpenOption],
      executor: ExecutorService,
      attrs: FileAttribute[_]*
  ): AsynchronousFileChannel =
    throw new UnsupportedOperationException("newAsynchronousFileChannel not implemented")

  @varargs def newByteChannel(
      path: Path,
      options: JavaSet[_ <: OpenOption],
      attrs: FileAttribute[_]*
  ): SeekableByteChannel

  def newDirectoryStream(
      dir: Path,
      filter: DirectoryStream.Filter[_ >: Path]
  ): DirectoryStream[Path]

  @varargs def newFileChannel(
      path: Path,
      options: JavaSet[_ <: OpenOption],
      attrs: FileAttribute[_]*
  ): FileChannel = throw new UnsupportedOperationException("newFileChannel not implemented")

  def newFileSystem(uri: URI, env: JavaMap[String, _]): FileSystem

  def newFileSystem(path: Path, env: JavaMap[String, _]): FileSystem =
    newFileSystem(path.toUri, env)

  @varargs def newInputStream(path: Path, options: OpenOption*): InputStream

  @varargs def newOutputStream(path: Path, options: OpenOption*): OutputStream

  @varargs def readAttributes[A <: BasicFileAttributes](
      path: Path,
      clazz: Class[A],
      options: LinkOption*
  ): A

  @varargs def readAttributes(
      path: Path,
      attributes: String,
      linkOptions: LinkOption*
  ): JavaMap[String, AnyRef]

  def readSymbolicLink(link: Path): Path =
    throw new UnsupportedOperationException("readSymbolicLink not implemented")

  @varargs def setAttribute(path: Path, attruvyte: String, value: Any, options: LinkOption*): Unit
}

object FileSystemProvider {
  def installedProviders(): JavaList[FileSystemProvider] =
    throw new UnsupportedOperationException("installedProviders not implemented")
}
