package java.nio.file

import java.io.{BufferedReader, BufferedWriter, InputStream, OutputStream}
import java.nio.channels.SeekableByteChannel
import java.nio.charset.Charset
import java.nio.file.attribute._
import java.lang.{Iterable => JavaIterable}
import java.util.{List => JavaList, Map => JavaMap, Set => JavaSet}
import java.util.function.BiPredicate
import java.util.stream.{Stream => JavaStream}

import io.scalajs.nodejs.fs

object Files {
  def copy(in: InputStream, target: Path, options: CopyOption*): Long = ???

  def copy(source: Path, out: OutputStream): Long = ???

  def copy(source: Path, tareget: Path, options: CopyOption*): Path = ???

  def createDirectories(dir: Path, attrs: FileAttribute[_]*): Path = ???

  def createDirectory(dir: Path, attrs: FileAttribute[_]*): Path = ???

  def createFile(path: Path, attrs: FileAttribute[_]*): Path = ???

  def createLink(link: Path, existing: Path): Path = ???

  def createSymbolicLink(link: Path, target: Path, attrs: FileAttribute[_]): Path = ???

  def createTempDirectory(dir: Path, prefix: String, attrs: FileAttribute[_]): Path = ???

  def createTempDirectory(prefix: String, attrs: FileAttribute[_]): Path = ???

  def createTempFile(dir: Path, prefix: String, suffix: String, attrs: FileAttribute[_]): Path = ???

  def createTempFile(prefix: String, suffix: String, attrs: FileAttribute[_]): Path = ???

  def delete(path: Path): Unit = ???

  def deleteIfExists(path: Path): Boolean = ???

  def exists(path: Path, options: LinkOption*): Boolean = ???

  def find(
      start: Path,
      maxDepth: Int,
      matcher: BiPredicate[Path, BasicFileAttributes],
      options: FileVisitOption*
  ): JavaStream[Path] = ???

  def getAttribute(path: Path, attribute: String, options: LinkOption*): AnyRef = ???

  def getFileAttributeView[V <: FileAttributeView](
      path: Path,
      `type`: Class[V],
      options: LinkOption*
  ): V = ???

  def getFileStore(path: Path): FileStore = ???

  def getOwner(path: Path, options: LinkOption*): UserPrincipal = ???

  def getPosixFilePermissions(path: Path, options: LinkOption*): JavaSet[PosixFilePermission] = ???

  def isDirectory(path: Path, options: Array[LinkOption]): Boolean = {
    if (options.contains(LinkOption.NOFOLLOW_LINKS)) {
      try {
        fs.Fs.lstatSync(path.toString).isDirectory()
      } catch {
        case _: Throwable => false
      }
    } else {
      try {
        fs.Fs.statSync(path.toString).isDirectory()
      } catch {
        case _: Throwable => false
      }
    }
  }
  def isDirectory(path: Path): Boolean = isDirectory(path, Array.empty)

  def isExecutable(path: Path): Boolean = ???

  def isHidden(path: Path): Boolean = ???

  def isReadable(path: Path): Boolean = {
    try {
      fs.Fs.accessSync(path.toString, fs.Fs.constants.R_OK)
      true
    } catch {
      case _: Throwable => false
    }
  }

  def isRegularFile(path: Path, options: LinkOption*): Boolean = ???

  def isSameFile(path: Path, path2: Path): Boolean = ???

  def isSymbolicLink(path: Path): Boolean = ???

  def isWritable(path: Path): Boolean = ???

  def lines(path: Path): JavaStream[String] = ???

  def lines(path: Path, cs: Charset): JavaStream[String] = ???

  def list(dir: Path): JavaStream[String] = ???

  def move(source: Path, target: Path, options: CopyOption*): Path = ???

  def newBufferedReader(path: Path): BufferedReader = ???

  def newBufferedReader(path: Path, cs: Charset): BufferedReader = ???

  def newBufferedWriter(path: Path, cs: Charset, options: OpenOption*): BufferedWriter = ???

  def newBufferedWriter(path: Path, options: OpenOption*): BufferedWriter = ???

  def newByteChannel(path: Path, options: OpenOption*): SeekableByteChannel = ???

  def newByteChannel(
      path: Path,
      options: JavaSet[_ <: OpenOption],
      attrs: FileAttribute[_]*
  ): SeekableByteChannel = ???

  def newDirectoryStream(dir: Path): DirectoryStream[Path] = ???

  def newDirectoryStream(
      dir: Path,
      filter: DirectoryStream.Filter[_ >: Path]
  ): DirectoryStream[Path] = ???

  def newDirectoryStream(dir: Path, glob: String): DirectoryStream[Path] = ???

  def newInputStream(path: Path, options: OpenOption*): InputStream = ???

  def newOutputStream(path: Path, options: OpenOption*): OutputStream = ???

  def notExists(path: Path, options: LinkOption*): Boolean = ???

  def probeContentType(path: Path): String = ???

  def readAllBytes(path: Path): Array[Byte] = ???

  def readAllLines(path: Path): JavaList[String] = ???

  def readAllLines(path: Path, cs: Charset): JavaList[String] = ???

  def readAttributes[A <: BasicFileAttributes](
      path: Path,
      `type`: Class[A],
      options: LinkOption*
  ): A = ???

  def readAttributes[A <: BasicFileAttributes](
      path: Path,
      Sattributes: String,
      options: LinkOption*
  ): JavaMap[String, AnyRef] = ???

  def readSymbolicLink(link: Path): Path = ???

  def setAttribute(path: Path, attribute: String, value: AnyRef, options: LinkOption*): Path = ???

  def setLastModifiedTime(path: Path, time: FileTime): Path = ???

  def setOwner(path: Path, owner: UserPrincipal): Path = ???

  def setPosixFilePermissions(path: Path, perms: JavaSet[PosixFilePermission]): Path = ???

  def size(path: Path): Long = ???

  def walk(start: Path, options: FileVisitOption*): JavaStream[Path]                = ???
  def walk(start: Path, maxDepth: Int, options: FileVisitOption*): JavaStream[Path] = ???

  def walkFileTree(start: Path, visitor: FileVisitor[_ >: Path]): Path = ???
  def walkFileTree(
      start: Path,
      options: JavaSet[FileVisitOption],
      maxDepth: Int,
      visitor: FileVisitor[_ >: Path]
  ): Path = ???

  def write(path: Path, bytes: Array[Byte], options: OpenOption*): Path = ???
  def write(
      path: Path,
      lines: JavaIterable[_ <: CharSequence],
      cs: Charset,
      options: OpenOption*
  ): Path                                                                                   = ???
  def write(path: Path, lines: JavaIterable[_ <: CharSequence], options: OpenOption*): Path = ???
}
