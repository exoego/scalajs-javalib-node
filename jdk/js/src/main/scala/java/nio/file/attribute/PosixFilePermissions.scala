package java.nio.file.attribute

import io.scalajs.nodejs.fs

import scala.scalajs.js
import scala.jdk.CollectionConverters._
import java.util.{HashSet => JavaHashSet, Set => JavaSet}

object PosixFilePermissions {
  def fromString(perms: String): JavaSet[PosixFilePermission] = {
    if (perms.length != 9) {
      throw new IllegalArgumentException()
    }
    val set = new JavaHashSet[PosixFilePermission]()
    @inline def parse(index: Int, expected: Char, permission: PosixFilePermission): Unit = {
      perms.charAt(index) match {
        case `expected` => set.add(permission)
        case '-'        =>
        case _          => throw new IllegalArgumentException()
      }
    }
    parse(0, 'r', PosixFilePermission.OWNER_READ)
    parse(1, 'w', PosixFilePermission.OWNER_WRITE)
    parse(2, 'x', PosixFilePermission.OWNER_EXECUTE)
    parse(3, 'r', PosixFilePermission.GROUP_READ)
    parse(4, 'w', PosixFilePermission.GROUP_WRITE)
    parse(5, 'x', PosixFilePermission.GROUP_EXECUTE)
    parse(6, 'r', PosixFilePermission.OTHERS_READ)
    parse(7, 'w', PosixFilePermission.OTHERS_WRITE)
    parse(8, 'x', PosixFilePermission.OTHERS_EXECUTE)
    set
  }

  def toString(set: JavaSet[PosixFilePermission]): String = {
    js.Array(
        if (set.contains(PosixFilePermission.OWNER_READ)) "r" else "-",
        if (set.contains(PosixFilePermission.OWNER_WRITE)) "w" else "-",
        if (set.contains(PosixFilePermission.OWNER_EXECUTE)) "x" else "-",
        if (set.contains(PosixFilePermission.GROUP_READ)) "r" else "-",
        if (set.contains(PosixFilePermission.GROUP_WRITE)) "w" else "-",
        if (set.contains(PosixFilePermission.GROUP_EXECUTE)) "x" else "-",
        if (set.contains(PosixFilePermission.OTHERS_READ)) "r" else "-",
        if (set.contains(PosixFilePermission.OTHERS_WRITE)) "w" else "-",
        if (set.contains(PosixFilePermission.OTHERS_EXECUTE)) "x" else "-"
      )
      .join("")
  }

  def asFileAttribute(
      perms: JavaSet[PosixFilePermission]
  ): FileAttribute[JavaSet[PosixFilePermission]] = new PosixFilePermissionFileAttribute(perms)

}

private[nio] object PosixFilePermissionsHelper {
  def fromJsStats(stat: fs.Stats): JavaSet[PosixFilePermission] = {
    val set = scala.collection.mutable.Set[PosixFilePermission]()
    if ((stat.mode & fs.Fs.constants.S_IRUSR) != 0) {
      set.add(PosixFilePermission.OWNER_READ)
    }
    if ((stat.mode & fs.Fs.constants.S_IWUSR) != 0) {
      set.add(PosixFilePermission.OWNER_WRITE)
    }
    if ((stat.mode & fs.Fs.constants.S_IXUSR) != 0) {
      set.add(PosixFilePermission.OWNER_EXECUTE)
    }
    if ((stat.mode & fs.Fs.constants.S_IRGRP) != 0) {
      set.add(PosixFilePermission.GROUP_READ)
    }
    if ((stat.mode & fs.Fs.constants.S_IWGRP) != 0) {
      set.add(PosixFilePermission.GROUP_WRITE)
    }
    if ((stat.mode & fs.Fs.constants.S_IXGRP) != 0) {
      set.add(PosixFilePermission.GROUP_EXECUTE)
    }
    if ((stat.mode & fs.Fs.constants.S_IROTH) != 0) {
      set.add(PosixFilePermission.OTHERS_READ)
    }
    if ((stat.mode & fs.Fs.constants.S_IWOTH) != 0) {
      set.add(PosixFilePermission.OTHERS_WRITE)
    }
    if ((stat.mode & fs.Fs.constants.S_IXOTH) != 0) {
      set.add(PosixFilePermission.OTHERS_EXECUTE)
    }
    set.asJava
  }
}

private[attribute] final class PosixFilePermissionFileAttribute(
    val value: JavaSet[PosixFilePermission]
) extends FileAttribute[JavaSet[PosixFilePermission]] {
  override val name: String = "posix:permissions"
}
