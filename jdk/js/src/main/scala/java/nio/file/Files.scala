package java.nio.file

import java.io.{BufferedReader, BufferedWriter, IOException, InputStream, OutputStream}
import java.nio.channels.SeekableByteChannel
import java.nio.charset.Charset
import java.nio.file.attribute._
import java.lang.{Iterable => JavaIterable}
import java.util.{List => JavaList, Map => JavaMap, Set => JavaSet}
import java.util.function.BiPredicate
import java.util.stream.{Stream => JavaStream}
import io.scalajs.nodejs.fs
import io.scalajs.nodejs.path
import io.scalajs.nodejs.os

import java.util
import scala.annotation.varargs
import scala.jdk.CollectionConverters._
import scala.util.Random

object Files {
  @varargs def copy(in: InputStream, target: Path, options: CopyOption*): Long = ???

  def copy(source: Path, out: OutputStream): Long = ???

  @varargs def copy(source: Path, target: Path, options: CopyOption*): Path = ???

  @varargs def createDirectories(dir: Path, attrs: FileAttribute[_]*): Path = {
    val dirStr = dir.toString
    fs.Fs.mkdirSync(dirStr, fs.MkdirOptions(recursive = true))
    // TODO: attrs
    dir
  }

  @varargs def createDirectory(dir: Path, attrs: FileAttribute[_]*): Path = {
    val dirStr = dir.toString
    if (Files.exists(dir)) {
      throw new FileAlreadyExistsException(dirStr)
    }

    try {
      fs.Fs.mkdirSync(dirStr)
      // TODO: attrs
    } catch {
      case _: Throwable => throw new NoSuchFileException(dirStr)
    }
    dir
  }

  @varargs def createFile(path: Path, attrs: FileAttribute[_]*): Path = ???

  def createLink(link: Path, existing: Path): Path = ???

  @varargs def createSymbolicLink(link: Path, target: Path, attrs: FileAttribute[_]*): Path = {
    val newPath = link.toString
    if (Files.exists(link)) {
      throw new FileAlreadyExistsException(newPath)
    }
    val existingPath = target.toString
    fs.Fs.symlinkSync(existingPath, newPath)
    // TODO: attrs
    link
  }

  @varargs def createTempDirectory(dir: Path, prefix: String, attrs: FileAttribute[_]*): Path = {
    createTempDirectoryInternal(dir.toString, prefix, attrs: _*)
  }

  @varargs def createTempDirectory(prefix: String, attrs: FileAttribute[_]*): Path = {
    createTempDirectoryInternal(defaultTempDir(), prefix, attrs: _*)
  }

  private def createTempDirectoryInternal(
      dir: String,
      prefix: String,
      attrs: FileAttribute[_]*
  ): Path = {
    val joined     = path.Path.join(dir, prefix)
    val created    = fs.Fs.mkdtempSync(joined)
    val normalized = path.Path.resolve(created)
    Paths.get(normalized)
  }

  @varargs def createTempFile(
      dir: Path,
      prefix: String,
      suffix: String,
      attrs: FileAttribute[_]*
  ): Path = createTempFileInternal(dir.toString, prefix, suffix, attrs: _*)

  @varargs def createTempFile(prefix: String, suffix: String, attrs: FileAttribute[_]*): Path =
    createTempFileInternal(defaultTempDir(), prefix, suffix, attrs: _*)

  private def createTempFileInternal(
      dir: String,
      prefix: String,
      suffix: String,
      attrs: FileAttribute[_]*
  ): Path = {
    val random   = Random.between(1000000000000000000L, Long.MaxValue)
    val suffix2  = if (suffix == null) ".tmp" else suffix
    val fileName = s"${prefix}${random}${suffix2}"
    val joined   = path.Path.join(dir, fileName)
    fs.Fs.writeFileSync(joined, "")
    Paths.get(joined)

    // TODO: attrs
  }

  def delete(path: Path): Unit = {
    if (Files.isRegularFile(path) || Files.isSymbolicLink(path)) {
      fs.Fs.unlinkSync(path.toString)
    } else if (Files.isDirectory(path)) {
      try {
        fs.Fs.rmdirSync(path.toString)
      } catch {
        case _: Throwable => throw new DirectoryNotEmptyException(path.toString)
      }
    } else if (Files.notExists(path)) {
      throw new NoSuchFileException(path.toString)
    }
  }

  def deleteIfExists(path: Path): Boolean = ???

  @varargs def exists(path: Path, options: LinkOption*): Boolean =
    transformStats(path, options)(false)(_ => true)

  @varargs def find(
      start: Path,
      maxDepth: Int,
      matcher: BiPredicate[Path, BasicFileAttributes],
      options: FileVisitOption*
  ): JavaStream[Path] = ???

  @varargs def getAttribute(path: Path, attribute: String, options: LinkOption*): AnyRef = ???

  @varargs def getFileAttributeView[V <: FileAttributeView](
      path: Path,
      `type`: Class[V],
      options: LinkOption*
  ): V = ???

  def getFileStore(path: Path): FileStore = ???

  @varargs def getOwner(path: Path, options: LinkOption*): UserPrincipal = ???

  private def transformStats[T](path: Path, options: Seq[LinkOption])(
      fallback: => T
  )(transformer: fs.Stats => T): T = {
    try {
      val stat: fs.Stats =
        if (options.contains(LinkOption.NOFOLLOW_LINKS)) {
          fs.Fs.lstatSync(path.toString)
        } else {
          fs.Fs.statSync(path.toString)
        }
      transformer(stat)
    } catch {
      case _: Throwable => fallback
    }
  }

  @varargs def getPosixFilePermissions(
      path: Path,
      options: LinkOption*
  ): JavaSet[PosixFilePermission] = {
    transformStats(path, options)(throw new NoSuchFileException(path.toString)) { stat =>
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

  @varargs def isDirectory(path: Path, options: LinkOption*): Boolean = {
    transformStats(path, options)(false)(_.isDirectory())
  }

  def isExecutable(path: Path): Boolean = {
    try {
      fs.Fs.accessSync(path.toString, fs.Fs.constants.X_OK)
      true
    } catch {
      case _: Throwable => false
    }
  }

  def isHidden(path: Path): Boolean = {
    // unix
    path.startsWith(".")

    // TODO: read attribute on windows
  }

  def isReadable(path: Path): Boolean = {
    try {
      fs.Fs.accessSync(path.toString, fs.Fs.constants.R_OK)
      true
    } catch {
      case _: Throwable => false
    }
  }

  @varargs def isRegularFile(path: Path, options: LinkOption*): Boolean = {
    transformStats(path, options)(false)(_.isFile())
  }

  def isSameFile(path: Path, path2: Path): Boolean = {
    if (path == path2) {
      true
    } else {
      try {
        fs.Fs.statSync(path.toString).ino == fs.Fs.statSync(path2.toString).ino
      } catch {
        case _: Throwable =>
          throw new NoSuchFileException(path.toString)
      }
    }
  }

  def isSymbolicLink(path: Path): Boolean = {
    try {
      fs.Fs.lstatSync(path.toString).isSymbolicLink()
    } catch {
      case _: Throwable => false
    }
  }

  def isWritable(path: Path): Boolean = {
    try {
      fs.Fs.accessSync(path.toString, fs.Fs.constants.W_OK)
      true
    } catch {
      case _: Throwable => false
    }
  }

  def lines(path: Path): JavaStream[String] = ???

  def lines(path: Path, cs: Charset): JavaStream[String] = ???

  def list(dir: Path): JavaStream[String] = ???

  @varargs def move(source: Path, target: Path, options: CopyOption*): Path = ???

  def newBufferedReader(path: Path): BufferedReader = ???

  def newBufferedReader(path: Path, cs: Charset): BufferedReader = ???

  @varargs def newBufferedWriter(path: Path, cs: Charset, options: OpenOption*): BufferedWriter =
    ???

  @varargs def newBufferedWriter(path: Path, options: OpenOption*): BufferedWriter = ???

  @varargs def newByteChannel(path: Path, options: OpenOption*): SeekableByteChannel = ???

  @varargs def newByteChannel(
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

  @varargs def newInputStream(path: Path, options: OpenOption*): InputStream = ???

  @varargs def newOutputStream(path: Path, options: OpenOption*): OutputStream = ???

  @varargs def notExists(path: Path, options: LinkOption*): Boolean = !exists(path, options: _*)

  def probeContentType(path: Path): String = null

  def readAllBytes(path: Path): Array[Byte] = {
    try {
      fs.Fs.readFileSync(path.toString).map(_.toByte).toArray
    } catch {
      case ex: Throwable => throw new IOException(ex.getCause)
    }
  }

  def readAllLines(path: Path): JavaList[String] = readAllLinesInternal(path, "utf8")

  def readAllLines(path: Path, cs: Charset): JavaList[String] =
    readAllLinesInternal(path, cs.displayName())

  private def readAllLinesInternal(path: Path, cs: String): JavaList[String] = {
    try {
      val javaList = new util.ArrayList[String]()
      fs.Fs
        .readFileSync(path.toString, cs)
        .linesIterator
        .foreach(line => javaList.add(line))
      javaList
    } catch {
      case ex: Throwable => throw new IOException(ex.getCause)
    }
  }

  @varargs def readAttributes[A <: BasicFileAttributes](
      path: Path,
      `type`: Class[A],
      options: LinkOption*
  ): A = ???

  @varargs def readAttributes[A <: BasicFileAttributes](
      path: Path,
      Sattributes: String,
      options: LinkOption*
  ): JavaMap[String, AnyRef] = ???

  def readSymbolicLink(link: Path): Path = ???

  @varargs def setAttribute(
      path: Path,
      attribute: String,
      value: AnyRef,
      options: LinkOption*
  ): Path = ???

  def setLastModifiedTime(path: Path, time: FileTime): Path = ???

  def setOwner(path: Path, owner: UserPrincipal): Path = ???

  def setPosixFilePermissions(path: Path, perms: JavaSet[PosixFilePermission]): Path = ???

  def size(path: Path): Long = fs.Fs.statSync(path.toString).size.toLong

  @varargs def walk(start: Path, options: FileVisitOption*): JavaStream[Path]                = ???
  @varargs def walk(start: Path, maxDepth: Int, options: FileVisitOption*): JavaStream[Path] = ???

  def walkFileTree(start: Path, visitor: FileVisitor[_ >: Path]): Path = ???
  def walkFileTree(
      start: Path,
      options: JavaSet[FileVisitOption],
      maxDepth: Int,
      visitor: FileVisitor[_ >: Path]
  ): Path = ???

  @varargs def write(path: Path, bytes: Array[Byte], options: OpenOption*): Path = ???
  @varargs def write(
      path: Path,
      lines: JavaIterable[_ <: CharSequence],
      cs: Charset,
      options: OpenOption*
  ): Path = ???
  @varargs def write(
      path: Path,
      lines: JavaIterable[_ <: CharSequence],
      options: OpenOption*
  ): Path = ???

  private def defaultTempDir(): String = os.OS.tmpdir()
}
