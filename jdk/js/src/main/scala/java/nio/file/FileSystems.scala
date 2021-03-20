package java.nio.file

import java.io.File
import java.lang.{Iterable => JavaIterable}
import java.net.URI
import java.nio.channels.SeekableByteChannel
import java.nio.file.attribute.{
  BasicFileAttributes,
  FileAttribute,
  FileAttributeView,
  UserPrincipalLookupService
}
import java.nio.file.spi.FileSystemProvider
import java.util.{Map => JavaMap, Set => JavaSet}
import scala.annotation.varargs
import scala.jdk.CollectionConverters._

object FileSystems {

  def getDefault(): FileSystem = DefaultFileSystem

  def getFileSystem(uri: URI): FileSystem = throw new UnsupportedOperationException("getFileSystem")

  def newFileSystem(uri: URI, env: JavaMap[String, _]): FileSystem =
    throw new UnsupportedOperationException("newFileSystem")

}

private[file] object DefaultFileSystemProvider extends FileSystemProvider {

  def getScheme(): String = "file"

  def newFileSystem(uri: URI, map: JavaMap[String, _]): FileSystem = ???

  def getFileSystem(uri: URI): FileSystem = ???

  def getPath(uri: URI): Path = ???

  def newByteChannel(
      path: Path,
      set: JavaSet[_ <: OpenOption],
      fileAttributes: FileAttribute[_]*
  ): SeekableByteChannel = ???

  def newDirectoryStream(
      path: Path,
      filter: DirectoryStream.Filter[_ >: Path]
  ): DirectoryStream[Path] = ???

  def createDirectory(path: Path, fileAttributes: FileAttribute[_]*): Unit = ???

  def delete(path: Path): Unit = ???

  def copy(path: Path, path1: Path, copyOptions: CopyOption*): Unit = ???

  def move(path: Path, path1: Path, copyOptions: CopyOption*): Unit = ???

  def isSameFile(path: Path, path1: Path): Boolean = ???

  def isHidden(path: Path): Boolean = ???

  def getFileStore(path: Path): FileStore = ???

  def checkAccess(path: Path, accessModes: AccessMode*): Unit = ???

  def getFileAttributeView[V <: FileAttributeView](
      path: Path,
      aClass: Class[V],
      linkOptions: LinkOption*
  ): V = ???

  def readAttributes[A <: BasicFileAttributes](
      path: Path,
      aClass: Class[A],
      linkOptions: LinkOption*
  ): A = ???

  def readAttributes(
      path: Path,
      s: String,
      linkOptions: LinkOption*
  ): JavaMap[String, AnyRef] = ???

  def setAttribute(path: Path, s: String, o: Any, linkOptions: LinkOption*): Unit = ???
}

private[file] object DefaultFileSystem extends FileSystem {
  override def provider(): FileSystemProvider = DefaultFileSystemProvider

  override def close(): Unit = {
    throw new UnsupportedOperationException("Default FileSystem does not support close")
  }

  override def isOpen(): Boolean = {
    // TODO: not implemented
    true
  }

  override def isReadOnly(): Boolean = {
    // TODO: not implemented
    false
  }

  override def getSeparator(): String = {
    // TODO: not implemented
    File.separator
  }

  override def getRootDirectories(): JavaIterable[Path] = null

  override def getFileStores(): JavaIterable[FileStore] = null

  override def supportedFileAttributeViews(): JavaSet[String] = {
    val os = System.getProperty("os.name", "Linux")
    os match {
      case "Linux"    => Set("basic", "posix", "unix", "owner", "dos", "user").asJava
      case "Windows"  => Set("basic", "owner", "dos", "acl", "user").asJava
      case "Mac OS X" => Set("basic", "posix", "unix", "owner").asJava
      case _          => Set("basic").asJava
    }
  }

  @varargs override def getPath(first: String, more: String*): Path = ???

  override def getPathMatcher(s: String): PathMatcher = ???

  override def getUserPrincipalLookupService(): UserPrincipalLookupService = ???

  override def newWatchService(): WatchService = ???
}
