package java.nio.file

import java.io.{BufferedWriter, File, IOException}
import java.nio.channels.SeekableByteChannel
import java.nio.charset.Charset
import java.nio.file.attribute._
import java.util

import helper.FileHelper
import io.scalajs.nodejs.fs.Fs

import scala.scalajs.js

object Files {

  def getFileAttributeView[V <: FileAttributeView](
      path: Path,
      tpe: Class[V],
      options: Array[LinkOption]
  ): V = {
    getFileAttributeView(path, tpe, options.toArray)
  }

  def getFileAttributeView[V <: FileAttributeView](
      path: Path,
      tpe: Class[V],
      options: LinkOption*
  ): V = {
    throw new UnsupportedOperationException("getFileAttributeView")
  }

  def readAttributes[A <: BasicFileAttributes](
      path: Path,
      tpe: Class[A],
      options: Array[LinkOption]
  ): A = {
    readAttributes(path, tpe, options)
  }

  private def dateToFileTime(date: js.Date): FileTime = ???

  private final class PosixFileAttributesImpl(
      basic: BasicFileAttributes,
      val owner: UserPrincipal,
      val group: GroupPrincipal,
      val permissions: java.util.Set[PosixFilePermission]
  ) extends PosixFileAttributes {
    override def lastModifiedTime = basic.lastModifiedTime
    override def lastAccessTime   = basic.lastAccessTime
    override def creationTime     = basic.creationTime
    override def isRegularFile    = basic.isRegularFile
    override def isDirectory      = basic.isDirectory
    override def isSymbolicLink   = basic.isSymbolicLink
    override def isOther          = basic.isOther
    override def size             = basic.size
    override def fileKey          = basic.fileKey
  }

  def readAttributes[A <: BasicFileAttributes](
      path: Path,
      tpe: Class[A],
      options: LinkOption*
  ): A = {
    val stat = Fs.statSync(path.toString)

    val basicAttributes = new BasicFileAttributes {
      override def lastModifiedTime = dateToFileTime(stat.mtime)
      override def lastAccessTime   = dateToFileTime(stat.atime)
      override def creationTime     = dateToFileTime(stat.ctime)
      override def isRegularFile =
        stat.isFile() || stat.isBlockDevice() || stat.isSymbolicLink() && !options.contains(
          LinkOption.NOFOLLOW_LINKS
        )
      override def isDirectory    = stat.isDirectory()
      override def isSymbolicLink = stat.isSymbolicLink()
      override def isOther        = !stat.isFile() && !stat.isDirectory() && !stat.isSymbolicLink()
      override def size           = stat.size.toLong
      override def fileKey        = throw new UnsupportedOperationException("fileKey")
    }

    val attribute = if (tpe == classOf[BasicFileAttributes]) {
      basicAttributes
    } else if (tpe == classOf[PosixFileAttributes]) {
      val owner = new UserPrincipal {
        override def getName: String =
          throw new UnsupportedOperationException("UserPrincipal.getName")
      }
      val group = new GroupPrincipal {
        override def getName: String =
          throw new UnsupportedOperationException("GroupPrincipal.getName")
      }

      val permissions: util.Set[PosixFilePermission] =
        FileHelper.getPermissions(path)
      new PosixFileAttributesImpl(basicAttributes, owner, group, permissions)
    } else {
      throw new UnsupportedOperationException(s"Unsupported file attributes ${tpe}")
    }
    attribute.asInstanceOf[A]
  }

  def newBufferedWriter(
      path: Path,
      charset: Charset,
      options: Array[OpenOption]
  ): BufferedWriter = {
    throw new UnsupportedOperationException("newBufferedWriter")
  }

  private def nullCheck(a: Array[java.nio.file.attribute.FileAttribute[_]]): Unit = {
    if (a == null || a.contains(null)) {
      throw new NullPointerException
    }
  }

  def createDirectories(path: Path, attributes: Array[FileAttribute[_]]): Path = {
    nullCheck(attributes)
    if (path == null) throw new NullPointerException
    throw new UnsupportedOperationException("createDirectories")
  }

  def createTempDirectory(dir: Path, prefix: String, attributes: Array[FileAttribute[_]]): Path = {
    nullCheck(attributes)
    if (dir == null) throw new NullPointerException
    val dirFile = dir.toFile()
    if (!dirFile.isDirectory()) throw new IOException()
    File.createTempFile(prefix, "", dirFile).toPath()
  }

  def createTempDirectory(prefix: String, attributes: Array[FileAttribute[_]]): Path = {
    nullCheck(attributes)
    createTempDirectory(FileHelper.getDefaultTempDirectory().toPath(), prefix, attributes)
  }

  def createTempFile(prefix: String, suffix: String, attributes: Array[FileAttribute[_]]): Path = {
    nullCheck(attributes)
    val tmpdir = FileHelper.getDefaultTempDirectory()
    createTempFile(tmpdir.toPath(), prefix, suffix, attributes)
  }

  def createTempFile(
      dir: Path,
      prefix: String,
      suffix: String,
      attributes: Array[FileAttribute[_]]
  ): Path = {
    nullCheck(attributes)
    if (dir == null) throw new NullPointerException
    val dirFile = dir.toFile()
    if (!dirFile.isDirectory()) throw new IOException()
    File.createTempFile(prefix, suffix, dirFile).toPath()
  }

  def createFile(path: Path, attributes: Array[FileAttribute[_]]): Path = {
    nullCheck(attributes)
    if (path.toFile().createNewFile()) {
      // TODO: atomic update here
      path
    } else {
      throw new IllegalArgumentException
    }
  }

  def newByteChannel(path: Path, options: Array[OpenOption]): SeekableByteChannel = {
    throw new UnsupportedOperationException("newByteChannel")
  }

  def newByteChannel(
      path: Path,
      options: java.util.Set[OpenOption],
      attrs: Array[FileAttribute[_]]
  ): SeekableByteChannel = {
    throw new UnsupportedOperationException("newByteChannel")
  }
  def newDirectoryStream(path: Path): DirectoryStream[Path] = {
    throw new UnsupportedOperationException("newDirectoryStream")
  }

  def getFileStore(path: Path): FileStore = {
    throw new UnsupportedOperationException("getFileStore")
  }

  def delete(path: Path): Unit = {
    path.toFile().delete()
  }

  def deleteIfExists(path: Path): Boolean = {
    if (path.toFile().exists()) {
      path.toFile().delete()
    } else {
      false
    }
  }

  def exists(path: Path, options: Array[LinkOption]): Boolean = {
    throw new UnsupportedOperationException("exists")
  }

  def getPosixFilePermissions(
      path: Path,
      options: Array[LinkOption]
  ): java.util.Set[PosixFilePermission] = {
    throw new UnsupportedOperationException("getPosixFilePermissions")
  }

}
