package helper

import java.io.File
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermission

import io.scalajs.nodejs.fs.Fs
import io.scalajs.nodejs.os.OS
import io.scalajs.nodejs.process.Process

import scala.collection.mutable
import scala.util.matching.Regex

object FileHelper {
  val deleteQueue: mutable.Queue[String] = collection.mutable.Queue.empty[String]
  Process.onBeforeExit((exitCode) => {
    FileHelper.deleteQueue.foreach { file =>
      try {
        val result = new File(file).delete()
      } catch {
        case e: Throwable => // ignore
      }
    }
  })

  val READ_BY_OWNER: Int     = Integer.parseInt("400", 8)
  val WRITE_BY_OWNER: Int    = Integer.parseInt("200", 8)
  val EXECUTE_BY_OWNER: Int  = Integer.parseInt("100", 8)
  val READ_BY_GROUP: Int     = Integer.parseInt("40", 8)
  val WRITE_BY_GROUP: Int    = Integer.parseInt("20", 8)
  val EXECUTE_BY_GROUP: Int  = Integer.parseInt("10", 8)
  val READ_BY_OTHERS: Int    = Integer.parseInt("4", 8)
  val WRITE_BY_OTHERS: Int   = Integer.parseInt("2", 8)
  val EXECUTE_BY_OTHERS: Int = Integer.parseInt("1", 8)

  System.setProperty("java.io.tmpdir", OS.tmpdir())

  val unixHiddenFile: Regex = "(^|\\/)\\.[^\\/]*".r

  def getDefaultTempDirectory(): File = {
    val tmpdir = System.getProperty("java.io.tmpdir")
    if (tmpdir == null) throw new NullPointerException("java.io.tmpdir")
    new File(tmpdir)
  }

  def getPermissionCode(file: File): Int = {
    Fs.statSync(file.getPath()).mode & Integer.parseInt("777", 8)
  }

  def getPermissions(path: Path): java.util.Set[PosixFilePermission] = {
    val code = getPermissionCode(path.toFile())
    val set  = new java.util.HashSet[PosixFilePermission]()
    if ((code & READ_BY_OWNER) > 0) {
      set.add(PosixFilePermission.OWNER_READ)
    }
    if ((code & WRITE_BY_OWNER) > 0) {
      set.add(PosixFilePermission.OWNER_WRITE)
    }
    if ((code & EXECUTE_BY_OWNER) > 0) {
      set.add(PosixFilePermission.OWNER_EXECUTE)
    }
    if ((code & READ_BY_GROUP) > 0) {
      set.add(PosixFilePermission.GROUP_READ)
    }
    if ((code & WRITE_BY_GROUP) > 0) {
      set.add(PosixFilePermission.GROUP_WRITE)
    }
    if ((code & EXECUTE_BY_GROUP) > 0) {
      set.add(PosixFilePermission.GROUP_EXECUTE)
    }
    if ((code & READ_BY_OTHERS) > 0) {
      set.add(PosixFilePermission.OTHERS_READ)
    }
    if ((code & WRITE_BY_OTHERS) > 0) {
      set.add(PosixFilePermission.OTHERS_WRITE)
    }
    if ((code & EXECUTE_BY_OTHERS) > 0) {
      set.add(PosixFilePermission.OTHERS_EXECUTE)
    }
    java.util.Collections.unmodifiableSet(set)
  }
}
