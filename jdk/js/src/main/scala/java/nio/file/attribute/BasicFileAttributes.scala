package java.nio.file.attribute

import java.util
import io.scalajs.nodejs.fs.Stats

trait BasicFileAttributes {
  def lastModifiedTime(): FileTime

  def lastAccessTime(): FileTime

  def creationTime(): FileTime

  def isRegularFile(): Boolean

  def isDirectory(): Boolean

  def isSymbolicLink(): Boolean

  def isOther(): Boolean

  def size(): Long

  def fileKey(): AnyRef
}

trait PosixFileAttributes extends BasicFileAttributes {
  def owner(): UserPrincipal

  def group(): GroupPrincipal

  def permissions(): util.Set[PosixFilePermission]
}

private[file] class NodeJsPosixFileAttributes(stats: Stats) extends PosixFileAttributes {
  @inline private def toLong(d: Double): Long = d.toLong

  override def lastModifiedTime(): FileTime = FileTime.fromMillis(toLong(stats.mtimeMs))

  override def lastAccessTime(): FileTime = FileTime.fromMillis(toLong(stats.atimeMs))

  override def creationTime(): FileTime = FileTime.fromMillis(toLong(stats.birthtimeMs))

  override def isRegularFile(): Boolean = stats.isFile()

  override def isDirectory(): Boolean = stats.isDirectory()

  override def isSymbolicLink(): Boolean = stats.isSymbolicLink()

  override def isOther(): Boolean = !isRegularFile() && !isDirectory() && !isSymbolicLink()

  override def size(): Long = toLong(stats.size)

  override def fileKey(): String = s"(dev=${stats.dev},ino=${stats.ino})"

  override def owner(): UserPrincipal = ???

  override def group(): GroupPrincipal = ???

  override def permissions(): util.Set[PosixFilePermission] =
    PosixFilePermissionsHelper.fromJsStats(this.stats)
}
