package java.nio.file

import java.io.File
import java.net.URI
import java.nio.file

trait Path {

  def toFile(): File

  override def toString: String = toFile().getPath()

  def getFileSystem: file.FileSystem = ???

  def isAbsolute: Boolean = ???

  def getRoot: Path = ???

  def getFileName: Path = ???

  def getParent: Path = ???

  def getNameCount: Int = ???

  def getName(i: Int): Path = ???

  def subpath(i: Int, i1: Int): Path = ???

  def startsWith(path: Path): Boolean   = ???
  def startsWith(path: String): Boolean = ???

  def endsWith(path: Path): Boolean   = ???
  def endsWith(path: String): Boolean = ???

  def normalize(): Path = ???

  def resolve(other: String): Path = {
    this.resolve(this.getFileSystem.getPath(other))
  }
  def resolveSibling(other: Path): Path = {
    if (other == null) throw new NullPointerException
    this.getParent match {
      case null   => other
      case parent => parent.resolve(other)
    }
  }
  def resolveSibling(other: String): Path = {
    this.resolveSibling(this.getFileSystem.getPath(other))
  }
  def resolve(other: Path): Path = ???

  def relativize(path: Path): Path = ???

  def toUri: URI = ???

  def toAbsolutePath: Path = ???

  def toRealPath(linkOptions: LinkOption*): Path = ???

  def register(
      watchService: WatchService,
      kinds: Array[WatchEvent.Kind[_]],
      modifiers: WatchEvent.Modifier*
  ): WatchKey = ???

  def compareTo(path: Path): Int = ???

}

private[java] object PathHelper {

  private final class PathImpl(file: File) extends Path {
    override def toFile(): File = file

    override def getFileSystem: FileSystem = {
      throw new UnsupportedOperationException
    }
    override def isAbsolute: Boolean = {
      throw new UnsupportedOperationException
    }
    override def getRoot: Path = {
      throw new UnsupportedOperationException
    }
    override def getFileName: Path = {
      Paths.get(file.getName())
    }
    override def getParent: Path = {
      file.getParentFile().toPath()
    }
    override def getNameCount: Int = {
      throw new UnsupportedOperationException
    }
    override def getName(i: Int): Path = {
      throw new UnsupportedOperationException
    }
    override def subpath(i: Int, i1: Int): Path = {
      throw new UnsupportedOperationException
    }
    override def startsWith(path: Path): Boolean = ???
    override def startsWith(path: String): Boolean = {
      file.getName().startsWith(path)
    }
    override def endsWith(path: Path): Boolean = {
      throw new UnsupportedOperationException
    }
    override def endsWith(path: String): Boolean = {
      throw new UnsupportedOperationException
    }
    override def normalize(): Path = {
      throw new UnsupportedOperationException
    }
    override def resolve(path: Path): Path = {
      throw new UnsupportedOperationException
    }
    override def relativize(path: Path): Path = {
      throw new UnsupportedOperationException
    }
    override def toUri: URI = {
      throw new UnsupportedOperationException
    }
    override def toAbsolutePath: Path = {
      throw new UnsupportedOperationException
    }
    override def toRealPath(linkOptions: LinkOption*): Path = {
      throw new UnsupportedOperationException
    }
    override def register(
        watchService: WatchService,
        kinds: Array[WatchEvent.Kind[_]],
        modifiers: WatchEvent.Modifier*
    ): WatchKey = {
      throw new UnsupportedOperationException
    }
    override def compareTo(path: Path): Int = {
      throw new UnsupportedOperationException
    }

    override def equals(obj: Any): Boolean = {
      obj match {
        case path: Path => this.toFile() == path.toFile()
        case _          => false
      }
    }
  }

  def fromFile(file: File): Path = new PathImpl(file)

}
