package java.nio.file.attribute

import java.util

trait BasicFileAttributes {
  def lastModifiedTime: FileTime

  def lastAccessTime: FileTime

  def creationTime: FileTime

  def isRegularFile: Boolean

  def isDirectory: Boolean

  def isSymbolicLink: Boolean

  def isOther: Boolean

  def size: Long

  def fileKey: AnyRef
}

trait PosixFileAttributes extends BasicFileAttributes {
  def owner: UserPrincipal

  def group: GroupPrincipal

  def permissions: util.Set[PosixFilePermission]
}
