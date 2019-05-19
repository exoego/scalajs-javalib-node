package java.nio.file

import java.lang.{Iterable => JavaIterable}
import java.io.{Closeable, IOException}
import java.nio.file.attribute.UserPrincipalLookupService
import java.nio.file.spi.FileSystemProvider
import java.util.{Set => JavaSet}

abstract class FileSystem protected () extends Closeable {

  def provider(): FileSystemProvider

  @throws[IOException]
  override def close(): Unit

  def isOpen(): Boolean

  def isReadOnly(): Boolean

  def getSeparator(): String

  def getRootDirectories(): JavaIterable[Path]

  def getFileStores(): JavaIterable[FileStore]

  def supportedFileAttributeViews(): JavaSet[String]

  def getPath(first: String, more: Array[String]): Path

  def getPathMatcher(syntaxAndPattern: String): PathMatcher

  def getUserPrincipalLookupService(): UserPrincipalLookupService

  @throws[IOException]
  def newWatchService(): WatchService
}
