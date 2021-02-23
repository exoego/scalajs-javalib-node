package java.nio.file.attribute

import scala.scalajs.js
import java.util.{Set => JavaSet, HashSet => JavaHashSet}

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

private[attribute] final class PosixFilePermissionFileAttribute(
    val value: JavaSet[PosixFilePermission]
) extends FileAttribute[JavaSet[PosixFilePermission]] {
  override def name(): String = "posix:permissions"
}
