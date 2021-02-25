package java.nio.file.attribute

import java.nio.file.{Files, LinkOption, Path}
import java.util.{Set => JavaSet}

trait PosixFileAttributeView extends BasicFileAttributeView with FileOwnerAttributeView {
  def name(): String = ""
  def readAttributes(): PosixFileAttributes
  def setGroup(group: GroupPrincipal): Unit
  def setPermissions(perms: JavaSet[PosixFilePermission]): Unit
}

private[file] class NodeJsPosixFileAttributeView(sourcePath: Path, options: Seq[LinkOption])
    extends PosixFileAttributeView {
  override val name: String = "posix"

  override def readAttributes(): PosixFileAttributes =
    Files.readAttributes(sourcePath, classOf[PosixFileAttributes], options: _*)

  override def setPermissions(perms: JavaSet[PosixFilePermission]): Unit =
    Files.setPosixFilePermissions(sourcePath, perms)

  override def setTimes(
      lastModifiedTime: FileTime,
      lastAccessTime: FileTime,
      createTime: FileTime
  ): Unit = {
    if (lastModifiedTime != null) {
      Files.setLastModifiedTime(sourcePath, lastModifiedTime)
    }
    if (lastAccessTime != null) {
      Files.setAttribute(sourcePath, "lastAccessTime", lastAccessTime)
    }
    // createTime can not be set on JDK
  }

  override def setGroup(group: GroupPrincipal): Unit =
    throw new UnsupportedOperationException("setGroup not supported")

  override def getOwner(): UserPrincipal =
    throw new UnsupportedOperationException("getOwner not supported")

  override def setOwner(owner: UserPrincipal): Unit =
    throw new UnsupportedOperationException("setOwner not supported")
}
