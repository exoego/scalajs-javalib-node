package java.io

import java.net.URL
import java.nio.file._

import helper.FileHelper
import io.scalajs.nodejs.fs.Fs
import io.scalajs.nodejs.path.{Path => NodePath}

import scala.scalajs.js
import scala.scalajs.js.JavaScriptException
import scala.util.Random

object File {
  val separator: String       = System.getProperty("file.separator")
  val separatorChar: Char     = separator.charAt(0)
  val pathSeparator: String   = System.getProperty("path.separator")
  val pathSeparatorChar: Char = System.getProperty("path.separator").charAt(0)

  def listRoots(): Array[File] = {
    val path     = new File(".").getCanonicalPath()
    val rootPath = NodePath.parse(path).root
    rootPath.map(p => Array(new File(p))).getOrElse(Array.empty)
  }

  private val rnd = new Random()

  private def randomBetween(start: Int, end: Int): Int = {
    start + rnd.nextInt((end - start) + 1)
  }

  def createTempFile(prefix: String, suffix: String, directory: File): File = {
    if (prefix == null) {
      throw new NullPointerException("prefix must be non-null")
    }
    if (prefix.length < 3) {
      throw new IllegalArgumentException("prefix must has 3 character or more")
    }
    val start = 100000000
    val end   = 999999999

    val suffixComplemented = if (suffix == null) {
      ".tmp"
    } else if (suffix.endsWith(File.separator)) {
      suffix.dropRight(File.separator.length)
    } else {
      suffix
    }

    val randomMiddle =
      s"${randomBetween(1, 99)}${randomBetween(start, end)}${randomBetween(start, end)}"
    val tempName = s"$prefix$randomMiddle$suffixComplemented"
    val file     = new File(directory, tempName)
    file.createNewFile()
    file
  }

  def createTempFile(prefix: String, suffix: String): File = {
    if (prefix == null) {
      throw new NullPointerException()
    }
    // TODO: not implemented
    null
  }
}

class File private (dirname: Option[String], basename: String) extends Comparable[File] {
  def this(pathname: String) = {
    this({
      if (pathname == null) {
        throw new NullPointerException
      }
      Option(pathname)
        .filter(_.nonEmpty)
        .map(_.replaceAll("/+", "/"))
        .map(NodePath.dirname)
        .filter(
          dir => dir != pathname && (pathname.startsWith("." + File.separatorChar) || dir != ".")
        )
    }, if (pathname == "/") pathname else NodePath.basename(pathname))

  }

  def this(parent: String, child: String) = {
    this({
      if (child == null) {
        throw new NullPointerException()
      }
      Option(parent)
    }, child)
  }

  def this(parent: File, child: String) = {
    this({
      if (child == null) {
        throw new NullPointerException()
      }
      Option(parent).map(_.getPath())
    }, child)
  }

  private def file(): String = {
    val c = dirname match {
      case None => basename
      case Some(value) => {
        val parent_ = if (value == "") "/" else value
        if (basename.nonEmpty) {
          s"${parent_}${File.separatorChar}$basename"
        } else {
          parent_
        }
      }
    }
    c.trim()
  }

  def lastModified(): Long = {
    // TODO
    0L
  }
  def setLastModified(modified: Long): Boolean = {
    // TODO: not implemented
    false
  }

  def getParentFile(): File = {
    Option(getParent()).map(new File(_)).orNull
  }

  def getAbsoluteFile(): File = {
    // TODO: not implemented
    this
  }

  def getCanonicalFile(): File = {
    // TODO: not implemented
    this
  }

  def getAbsolutePath(): String = {
    // TODO: not implemented
    NodePath.resolve(this.file())
  }

  def getCanonicalPath(): String = {
    // TODO: not implemented
    NodePath.resolve(this.file())
  }

  def isDirectory(): Boolean = {
    try {
      Fs.statSync(this.file()).isDirectory()
    } catch {
      case _: JavaScriptException => false
    }
  }

  def isFile(): Boolean = {
    try {
      Fs.statSync(this.file()).isFile()
    } catch {
      case _: JavaScriptException => false
    }
  }

  def isAbsolute(): Boolean = {
    NodePath.isAbsolute(file())
  }

  def toPath(): Path = {
    PathHelper.fromFile(this)
  }

  def exists(): Boolean = {
    try {
      Fs.existsSync(file())
    } catch {
      case ex: JavaScriptException =>
        println(ex.getMessage())
        false
    }
  }

  def delete(): Boolean = {
    try {
      if (exists()) {
        if (isDirectory()) {
          Fs.rmdirSync(this.file())
        } else {
          Fs.unlinkSync(this.file())
        }
        true
      } else {
        false
      }
    } catch {
      case _: Throwable => false
    }
  }

  def isHidden(): Boolean = {
    // TODO: Windows
    FileHelper.unixHiddenFile.findFirstIn(basename).isDefined
  }

  def deleteOnExit(): Unit = {
    FileHelper.deleteQueue.enqueue(this.file())
  }

  def setReadOnly(): Boolean = {
    setWritable(false) && setExecutable(false)
  }

  def createNewFile(): Boolean = {
    if (file() == "") {
      throw new IOException()
    }
    if (exists()) {
      false
    } else {
      try {
        Fs.writeFileSync(file = this.file(), data = "")
        true
      } catch {
        case jse: js.JavaScriptException => false
        //throw new IOException(jse.getMessage())
      }
    }
  }

  private def isValid(): Boolean = {
    this.file() != ""
  }

  def mkdir(): Boolean = {
    if (!isValid() || exists()) {
      false
    } else {
      Fs.mkdirSync(this.file())
      true
    }
  }

  def mkdirs(): Boolean = {
    if (!isValid() || exists()) {
      false
    } else {
      // TODO: scalajs-io does not support recursive yet
      Fs.asInstanceOf[js.Dynamic]
        .mkdirSync(
          this.file(),
          js.Dynamic.literal(
            recursive = true
          )
        )
      true
    }
  }

  def renameTo(dest: File): Boolean = {
    if (dest == null) {
      throw new NullPointerException
    }
    Fs.renameSync(oldPath = this.file(), newPath = dest.getPath())
    true
  }

  def list(): Array[String] = {
    if (isDirectory()) {
      Fs.readdirSync(this.file()).toArray
    } else {
      null
    }
  }

  def list(filter: FilenameFilter): Array[String] = {
    if (isDirectory()) {
      if (filter == null) {
        Fs.readdirSync(this.file()).toArray
      } else {
        Fs.readdirSync(this.file()).filter(name => filter.accept(this, name)).toArray
      }
    } else {
      null
    }
  }

  def listFiles(): Array[File] = {
    this.list().map(s => new File(this, s))
  }

  def listFiles(filter: FilenameFilter): Array[File] = {
    this.list(filter).map(s => new File(this, s))
  }

  def listFiles(filter: FileFilter): Array[File] = {
    if (filter == null) {
      Fs.readdirSync(this.file()).map(name => new File(this, name)).toArray
    } else {
      Fs.readdirSync(this.file())
        .flatMap(name => {
          val file = new File(this, name)
          if (filter.accept(file)) {
            Some(file)
          } else {
            None
          }
        })
        .toArray
    }
  }

  def length(): Long = {
    Fs.statSync(file()).size.toLong
  }

  override def hashCode(): Int = {
    this.getAbsolutePath().hashCode
  }

  override def equals(obj: Any): Boolean = {
    obj match {
      case null       => false
      case that: File => this.getAbsolutePath() == that.getAbsolutePath()
      case _          => false
    }
  }

  override def toString: String = {
    s"""new File(${dirname}, ${basename})"""
  }

  def getUsableSpace(): Long = {
    // TODO: not implemented
    0L
  }
  def getFreeSpace(): Long = {
    // TODO: not implemented
    0L
  }
  def getTotalSpace(): Long = {
    // TODO: not implemented
    0L
  }
  private def testPermission(flag: io.scalajs.nodejs.FileMode): Boolean = {
    try {
      Fs.accessSync(this.file(), flag)
      true
    } catch {
      case _: Throwable => false
    }
  }

  private def setPermission(flag: io.scalajs.nodejs.FileMode, enable: Boolean): Boolean = {
    try {
      val current = FileHelper.getPermissionCode(this)
      val updated = if (enable) {
        current | flag
      } else {
        current ^ flag
      }
      Fs.chmodSync(this.file(), updated)
      true
    } catch {
      case _: Exception => false
    }
  }

  def canExecute(): Boolean = {
    testPermission(Fs.constants.X_OK)
  }

  def setExecutable(executable: Boolean): Boolean = {
    setPermission(FileHelper.EXECUTE_BY_OWNER, executable)
  }

  def setExecutable(executable: Boolean, ownerOnly: Boolean): Boolean = {
    if (ownerOnly) {
      setExecutable(executable)
    } else {
      setPermission(
        FileHelper.EXECUTE_BY_OWNER | FileHelper.EXECUTE_BY_GROUP | FileHelper.EXECUTE_BY_OTHERS,
        executable
      )
    }
  }

  def canRead(): Boolean = {
    testPermission(Fs.constants.R_OK)
  }

  def setReadable(readable: Boolean): Boolean = {
    setPermission(FileHelper.READ_BY_OWNER, readable)
  }

  def setReadable(readable: Boolean, ownerOnly: Boolean): Boolean = {
    if (ownerOnly) {
      setReadable(readable)
    } else {
      setPermission(
        FileHelper.READ_BY_OWNER | FileHelper.READ_BY_GROUP | FileHelper.READ_BY_OTHERS,
        readable
      )
    }
  }

  def canWrite(): Boolean = {
    testPermission(Fs.constants.W_OK)
  }

  def setWritable(writable: Boolean): Boolean = {
    setPermission(FileHelper.WRITE_BY_OWNER, writable)
  }

  def setWritable(writable: Boolean, ownerOnly: Boolean): Boolean = {
    if (ownerOnly) {
      setWritable(writable)
    } else {
      setPermission(
        FileHelper.WRITE_BY_OWNER | FileHelper.WRITE_BY_GROUP | FileHelper.WRITE_BY_OTHERS,
        writable
      )
    }
  }

  def toURL: URL = {
    // TODO: not implemented
    null
  }

  def getName(): String = {
    // TODO: not implemented
    basename
  }

  def getParent(): String = {
    dirname.orNull
  }

  def getPath(): String = {
    this.file()
  }

  override def compareTo(that: File): Int = {
    if (that == null) {
      throw new NullPointerException()
    }
    this.getPath().compareTo(that.getPath())
  }
}
