package java.nio.file

import java.lang.{Iterable => JavaIterable}
import java.io.{Closeable, IOException}
import java.nio.file.attribute.UserPrincipalLookupService
import java.nio.file.spi.FileSystemProvider
import java.util.{Set => JavaSet}
import scala.annotation.varargs

abstract class FileSystem protected () extends Closeable with AutoCloseable {

  @throws[IOException]
  override def close(): Unit

  def getFileStores(): JavaIterable[FileStore]

  @varargs def getPath(first: String, more: String*): Path

  def getPathMatcher(syntaxAndPattern: String): PathMatcher

  def getRootDirectories(): JavaIterable[Path]

  def getSeparator(): String

  def getUserPrincipalLookupService(): UserPrincipalLookupService

  def isOpen(): Boolean

  def isReadOnly(): Boolean

  @throws[IOException]
  def newWatchService(): WatchService

  def provider(): FileSystemProvider

  def supportedFileAttributeViews(): JavaSet[String]
}
