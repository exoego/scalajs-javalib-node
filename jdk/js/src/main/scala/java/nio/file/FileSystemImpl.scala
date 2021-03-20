package java.nio.file

import java.io.File
import java.lang.{Iterable => JavaIterable}
import java.nio.file.attribute.UserPrincipalLookupService
import java.nio.file.spi.FileSystemProvider
import java.util.{Set => JavaSet}
import scala.annotation.varargs
import scala.jdk.CollectionConverters._

private[file] object MacOsXFileSystem extends FileSystem {
  override def provider(): FileSystemProvider = MacOsXFileSystemProvider

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
