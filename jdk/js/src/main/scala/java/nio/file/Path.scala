package java.nio.file

import java.io.File
import java.net.URI
import java.nio.file
import io.scalajs.nodejs.path.{Path => NodeJsPath}
import io.scalajs.nodejs.fs.{Fs => NodeJsFs}

import scala.annotation.varargs
import scala.jdk.CollectionConverters._

trait Path extends Comparable[Path] with java.lang.Iterable[Path] {
  def compareTo(path: Path): Int

  def iterator(): java.util.Iterator[Path]

  def toFile(): File

  def getFileSystem: file.FileSystem = ???

  def isAbsolute: Boolean

  def getRoot: Path = ???

  def getFileName: Path = ???

  def getParent: Path = ???

  def getNameCount: Int

  def getName(index: Int): Path

  def subpath(beginIndex: Int, endIndex: Int): Path = ???

  def startsWith(path: Path): Boolean
  def startsWith(path: String): Boolean = startsWith(Paths.get(path))

  def endsWith(path: Path): Boolean
  def endsWith(path: String): Boolean = endsWith(Paths.get(path))

  def normalize(): Path

  def resolve(other: Path): Path
  def resolve(other: String): Path = resolve(Paths.get(other))

  def resolveSibling(other: Path): Path = {
    if (other == null) throw new NullPointerException
    this.getParent match {
      case null   => other
      case parent => parent.resolve(other)
    }
  }
  def resolveSibling(other: String): Path = this.resolveSibling(Paths.get(other))

  def relativize(other: Path): Path

  def toUri: URI = ???

  def toAbsolutePath: Path

  @varargs def toRealPath(linkOptions: LinkOption*): Path

  def register(
      watchService: WatchService,
      kinds: Array[WatchEvent.Kind[_]],
      modifiers: WatchEvent.Modifier*
  ): WatchKey = ???
}

private[java] object PathHelper {

  private val compactContinuingSeparator = s"${File.separatorChar}+".r
  private val dropLastSeparator          = s"(.+)${File.separatorChar}$$".r

  def fromString(rawPath: String): Path = {
    val x = compactContinuingSeparator.replaceAllIn(rawPath, File.separator)
    val y = dropLastSeparator.replaceFirstIn(x, "$1")
    new PathImpl(y, FileSystems.getDefault())
  }

  private final class PathImpl(val rawPath: String, override val getFileSystem: FileSystem)
      extends Path {
    private val names: Array[String] =
      rawPath.dropWhile(_ == File.separatorChar).split(File.separatorChar)

    private def newInstance(s: String): Path = new PathImpl(s, getFileSystem)

    override def compareTo(path: Path): Int =
      if (this == path) {
        0
      } else {
        this.toString.compareTo(path.toString)
      }

    override def toFile(): File = new File(rawPath)

    override def isAbsolute: Boolean = NodeJsPath.isAbsolute(rawPath)

    override def getRoot: Path = if (rawPath.startsWith("/")) Paths.get("/") else null

    override def getFileName: Path = newInstance(NodeJsPath.basename(rawPath))

    override def getParent: Path = {
      NodeJsPath
        .parse(rawPath)
        .dir
        .filter(_.nonEmpty)
        .map(parent => newInstance(parent))
        .getOrElse(null)
    }

    override def getNameCount: Int = {
      if (rawPath == "/") {
        0
      } else {
        names.length
      }
    }

    override def getName(index: Int): Path = {
      if (index < 0) {
        throw new IllegalArgumentException("'i' should be 0 or positive")
      }
      if (names.lengthIs > index) {
        Paths.get(names(index))
      } else {
        throw new IllegalArgumentException(s"${rawPath}: invalid 'i' <${index}>")
      }
    }

    override def subpath(beginIndex: Int, endIndex: Int): Path = {
      if (beginIndex < 0) {
        throw new IllegalArgumentException("beginIndex should be equal to or greater than 0")
      }
      if (beginIndex >= getNameCount) {
        throw new IllegalArgumentException("beginIndex should be less than the number of element")
      }
      if (endIndex <= beginIndex) {
        throw new IllegalArgumentException("endIndex should be greater than beginIndex")
      }
      if (endIndex > getNameCount) {
        throw new IllegalArgumentException(
          "endIndex should be equal to or less than the number of element"
        )
      }
      Paths.get(names.slice(beginIndex, endIndex).mkString(File.separator))
    }

    override def startsWith(path: Path): Boolean = {
      val thisCount     = this.getNameCount
      val pathCount     = path.getNameCount
      val isRoot        = thisCount == 0
      val pathIsRoot    = pathCount == 0
      val otherIsLonger = thisCount - pathCount < 0
      if (otherIsLonger || isRoot != pathIsRoot || isAbsolute != path.isAbsolute) {
        false
      } else {
        (0 until pathCount).forall { i =>
          this.getName(i) == path.getName(i)
        }
      }
    }

    override def endsWith(path: Path): Boolean = {
      val thisCount = this.getNameCount
      val pathCount = path.getNameCount
      val diffCount = thisCount - pathCount

      val isRoot     = thisCount == 0
      val pathIsRoot = pathCount == 0
      if (diffCount < 0 || isRoot != pathIsRoot) {
        false
      } else {
        ((pathCount - 1) to 0 by -1).forall { i =>
          this.getName(i + diffCount) == path.getName(i)
        }
      }
    }

    override def normalize(): Path = {
      NodeJsPath.normalize(rawPath) match {
        case "."       => newInstance("")
        case otherwise => newInstance(otherwise)
      }
    }

    override def resolve(other: Path): Path = {
      if (other.isAbsolute) {
        other
      } else if (other.toString == "") {
        this
      } else if (rawPath == "") {
        other
      } else {
        newInstance(s"${rawPath}${File.separatorChar}${other.toString}")
      }
    }

    override def relativize(other: Path): Path = {
      if (this.isAbsolute != other.isAbsolute) {
        throw new IllegalArgumentException(
          "A relative path cannot be constructed if only one of the paths have a root component"
        )
      }
      // Avoid .. or . resolution in Node.js's relative
      val escaped = names.map(s => if (s == ".." || s == ".") "_" else s).mkString(File.separator)
      val o       = if (isAbsolute) s"${File.separator}${escaped}" else escaped
      newInstance(NodeJsPath.relative(o, other.toString))
    }

    override def toUri: URI = {
      throw new UnsupportedOperationException
    }

    override def toAbsolutePath: Path = {
      if (isAbsolute) {
        this
      } else {
        newInstance(
          io.scalajs.nodejs.process.Process.env.PWD.getOrElse("") + NodeJsPath.sep + rawPath
        )
      }
    }

    @varargs override def toRealPath(linkOptions: LinkOption*): Path = {
      if (linkOptions.contains(LinkOption.NOFOLLOW_LINKS)) {
        toAbsolutePath
      } else if (NodeJsFs.existsSync(rawPath)) {
        newInstance(NodeJsFs.realpathSync(rawPath))
      } else {
        throw new NoSuchFileException(rawPath)
      }
    }

    override def register(
        watchService: WatchService,
        kinds: Array[WatchEvent.Kind[_]],
        modifiers: WatchEvent.Modifier*
    ): WatchKey = {
      throw new UnsupportedOperationException
    }

    override def toString: String = rawPath

    override def equals(obj: Any): Boolean = {
      obj match {
        case other: PathImpl => this.rawPath == other.rawPath
        case other: Path     => toString == other.toString && this.getFileSystem == other.getFileSystem
        case _               => false
      }
    }

    override def hashCode(): Int = rawPath.hashCode

    override def iterator(): java.util.Iterator[Path] = {
      if (rawPath == "/") {
        java.util.Collections.emptyIterator()
      } else {
        names.iterator
          .map(Paths.get(_))
          .asJava
      }
    }
  }

}
