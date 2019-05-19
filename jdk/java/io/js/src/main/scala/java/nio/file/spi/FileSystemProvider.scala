package java.nio.file.spi

import java.util.{Map => JavaMap, Set => JavaSet}
import java.net.URI
import java.nio.channels.SeekableByteChannel
import java.nio.file.{
  AccessMode,
  CopyOption,
  DirectoryStream,
  FileStore,
  FileSystem,
  LinkOption,
  OpenOption,
  Path
}
import java.nio.file.attribute.{BasicFileAttributes, FileAttribute, FileAttributeView}

abstract class FileSystemProvider {

  def getScheme(): String

  def newFileSystem(uri: URI, map: JavaMap[String, _]): FileSystem

  def getFileSystem(uri: URI): FileSystem

  def getPath(uri: URI): Path

  def newByteChannel(
      path: Path,
      set: JavaSet[_ <: OpenOption],
      fileAttributes: FileAttribute[_]*
  ): SeekableByteChannel

  def newDirectoryStream(
      path: Path,
      filter: DirectoryStream.Filter[_ >: Path]
  ): DirectoryStream[Path]

  def createDirectory(path: Path, fileAttributes: FileAttribute[_]*): Unit

  def delete(path: Path): Unit

  def copy(path: Path, path1: Path, copyOptions: CopyOption*): Unit

  def move(path: Path, path1: Path, copyOptions: CopyOption*): Unit

  def isSameFile(path: Path, path1: Path): Boolean

  def isHidden(path: Path): Boolean

  def getFileStore(path: Path): FileStore

  def checkAccess(path: Path, accessModes: AccessMode*): Unit

  def getFileAttributeView[V <: FileAttributeView](
      path: Path,
      aClass: Class[V],
      linkOptions: LinkOption*
  ): V

  def readAttributes[A <: BasicFileAttributes](
      path: Path,
      aClass: Class[A],
      linkOptions: LinkOption*
  ): A

  def readAttributes(
      path: Path,
      s: String,
      linkOptions: LinkOption*
  ): JavaMap[String, AnyRef]

  def setAttribute(path: Path, s: String, o: Any, linkOptions: LinkOption*): Unit
}
