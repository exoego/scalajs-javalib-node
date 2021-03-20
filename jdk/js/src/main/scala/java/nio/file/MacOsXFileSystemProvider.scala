package java.nio.file

import io.scalajs.nodejs.fs

import java.io.{InputStream, OutputStream}
import java.net.URI
import java.nio.channels.SeekableByteChannel
import java.nio.file.attribute._
import java.nio.file.spi.FileSystemProvider
import java.util.{Map => JavaMap, Set => JavaSet}
import scala.annotation.varargs
import scala.collection.mutable
import scala.jdk.CollectionConverters._
import scala.reflect.ClassTag

// TODO: DO NOT DEPEND ON FILES
private[file] object MacOsXFileSystemProvider extends FileSystemProvider {
  override def getScheme(): String = "file"

  override def newFileSystem(uri: URI, env: JavaMap[String, _]): FileSystem = ???

  override def getFileSystem(uri: URI): FileSystem = ???

  override def getPath(uri: URI): Path = ???

  @varargs override def newByteChannel(
      path: Path,
      options: JavaSet[_ <: OpenOption],
      attrs: FileAttribute[_]*
  ): SeekableByteChannel = ???

  override def newDirectoryStream(
      dir: Path,
      filter: DirectoryStream.Filter[_ >: Path]
  ): DirectoryStream[Path] = ???

  override def createDirectory(dir: Path, attrs: FileAttribute[_]*): Unit = {
    FileSystemProviderHelper.validateInitialFileAttributes(attrs)
    val dirStr = dir.toString
    if (Files.exists(dir)) {
      throw new FileAlreadyExistsException(dirStr)
    }
    try {
      FileSystemProviderHelper.createDirectoryImpl(dirStr, attrs, recursive = false)
    } catch {
      case _: Throwable => throw new NoSuchFileException(dirStr)
    }
  }

  override def delete(path: Path): Unit = {
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

  @varargs override def newInputStream(path: Path, options: OpenOption*): InputStream = ???

  @varargs override def newOutputStream(path: Path, options: OpenOption*): OutputStream = ???

  override def copy(source: Path, target: Path, options: CopyOption*): Unit = {
    @inline def innerCopy(): Unit = {
      if (Files.isDirectory(source)) {
        Files.createDirectories(target)
      } else {
        fs.Fs.copyFileSync(source.toString, target.toString, 0)
        val sourcePermissions = Files.getPosixFilePermissions(source)
        if (options.contains(StandardCopyOption.COPY_ATTRIBUTES)) {
          Files.setPosixFilePermissions(target, sourcePermissions)
          Files.setLastModifiedTime(target, Files.getLastModifiedTime(source))
        } else {
          val securedPermissions = (sourcePermissions.asScala.toSet diff Set(
            PosixFilePermission.GROUP_WRITE,
            PosixFilePermission.OTHERS_WRITE
          )).asJava
          Files.setPosixFilePermissions(target, securedPermissions)
        }
      }
    }

    if (options.contains(StandardCopyOption.ATOMIC_MOVE)) {
      throw new UnsupportedOperationException("StandardCopyOption.ATOMIC_MOVE")
    } else if (Files.exists(target)) {
      if (Files.isSameFile(source, target)) {
        // do nothing
      } else if (options.contains(StandardCopyOption.REPLACE_EXISTING)) {
        Files.delete(target)
        innerCopy()
      } else {
        throw new FileAlreadyExistsException(target.toString)
      }
    } else if (Files.notExists(source)) {
      throw new NoSuchFileException(source.toString)
    } else {
      innerCopy()
    }
  }

  override def move(source: Path, target: Path, options: CopyOption*): Unit = {
    if (options.contains(StandardCopyOption.COPY_ATTRIBUTES)) {
      throw new UnsupportedOperationException()
    } else if (Files.notExists(source)) {
      throw new NoSuchFileException(source.toString)
    } else if (source != target) {
      if (options.contains(StandardCopyOption.REPLACE_EXISTING)) {
        try {
          Files.delete(target)
        } catch {
          case _: Throwable => throw new DirectoryNotEmptyException(target.toString)
        }
      } else if (Files.exists(target)) {
        throw new FileAlreadyExistsException(target.toString)
      }
      fs.Fs.renameSync(source.toString, target.toString)
    }
  }

  override def isSameFile(path: Path, path2: Path): Boolean = {
    if (path == path2) {
      true
    } else if (path.getFileSystem() == path2.getFileSystem()) {
      try {
        fs.Fs.statSync(path.toString).ino == fs.Fs.statSync(path2.toString).ino
      } catch {
        case _: Throwable =>
          throw new NoSuchFileException(path.toString)
      }
    } else {
      false
    }
  }

  override def isHidden(path: Path): Boolean = {
    path.getFileName().toString.startsWith(".")
  }

  override def getFileStore(path: Path): FileStore =
    throw new UnsupportedOperationException("getFileStore")

  // TODO: this may be implementable
  override def checkAccess(path: Path, modes: AccessMode*): Unit = ???

  override def getFileAttributeView[V <: FileAttributeView](
      path: Path,
      `type`: Class[V],
      options: LinkOption*
  ): V = {
    if (`type` != classOf[PosixFileAttributeView]) {
      null.asInstanceOf[V]
    } else {
      new NodeJsPosixFileAttributeView(path, options).asInstanceOf[V]
    }
  }

  override def readAttributes[A <: BasicFileAttributes](
      path: Path,
      `type`: Class[A],
      options: LinkOption*
  ): A = {
    if (`type` == classOf[BasicFileAttributes] || `type` == classOf[PosixFileAttributes]) {
      val attrs = FileSystemProviderHelper.transformStatsOrThrow(path, options) { stats =>
        new NodeJsPosixFileAttributes(stats)
      }
      attrs.asInstanceOf[A]
    } else {
      throw new UnsupportedOperationException(s"Unsupported class ${`type`}")
    }
  }

  override def readAttributes(
      path: Path,
      attributes: String,
      options: LinkOption*
  ): JavaMap[String, AnyRef] = {
    val typeName = FileSystemProviderHelper.attributeFormatCheck(attributes)
    val clazz    = FileSystemProviderHelper.toAttributeClass(typeName)
    val attrs    = readAttributes(path, clazz, options: _*)
    val keys     = FileSystemProviderHelper.keys(attributes)

    val mapBuilder = mutable.Map[String, AnyRef]()
    if (keys("isDirectory")) mapBuilder.put("isDirectory", attrs.isDirectory().asInstanceOf[AnyRef])
    if (keys("isOther")) mapBuilder.put("isOther", attrs.isOther().asInstanceOf[AnyRef])
    if (keys("isRegularFile"))
      mapBuilder.put("isRegularFile", attrs.isRegularFile().asInstanceOf[AnyRef])
    if (keys("isSymbolicLink"))
      mapBuilder.put("isSymbolicLink", attrs.isSymbolicLink().asInstanceOf[AnyRef])
    if (keys("size")) mapBuilder.put("size", attrs.size().asInstanceOf[AnyRef])
    if (keys("fileKey")) mapBuilder.put("fileKey", attrs.fileKey())
    if (keys("creationTime")) mapBuilder.put("creationTime", attrs.creationTime())
    if (keys("lastAccessTime")) mapBuilder.put("lastAccessTime", attrs.lastAccessTime())
    if (keys("lastModifiedTime")) mapBuilder.put("lastModifiedTime", attrs.lastModifiedTime())
    if (keys("permissions"))
      mapBuilder.put("permissions", attrs.asInstanceOf[PosixFileAttributes].permissions())
    mapBuilder.asJava
  }

  override def setAttribute(
      path: Path,
      attribute: String,
      value: Any,
      options: LinkOption*
  ): Unit = {
    val typeName = FileSystemProviderHelper.attributeFormatCheck(attribute)

    val attributeName = attribute.substring(attribute.indexOf(':') + 1)
    def transformValue[V](setter: V => Unit)(implicit classtag: ClassTag[V]): Unit = value match {
      case expected: V => setter(expected)
      case _           => throw new ClassCastException(s"${value.getClass} cannot be cast to class $classtag")
    }

    attributeName match {
      case "lastAccessTime" =>
        transformValue { time: FileTime =>
          FileSystemProviderHelper.transformStatsOrThrow(path, options) { stats =>
            fs.Fs.utimesSync(
              path.toString,
              atime = (time.toMillis() / 1000).toString,
              mtime = stats.mtime
            )
          }
        }
      case "lastModifiedTime" =>
        Files.setLastModifiedTime(path, value.asInstanceOf[FileTime])
      case "creationTime" =>
      // do nothing
      case "permissions" if typeName == "posix" =>
        Files.setPosixFilePermissions(path, value.asInstanceOf[java.util.Set[PosixFilePermission]])
      case _ => throw new IllegalArgumentException(s"`${attribute}` not recognized")
    }
  }

  override def readSymbolicLink(link: Path): Path = {
    if (!Files.isSymbolicLink(link)) {
      throw new NotLinkException(link.toString)
    }
    val linkPath = fs.Fs.readlinkSync(link.toString)
    Paths.get(linkPath)
  }
}
