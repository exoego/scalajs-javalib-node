package java.nio.file

import java.io.{BufferedReader, BufferedWriter, IOException, InputStream, OutputStream}
import java.nio.channels.SeekableByteChannel
import java.nio.charset.Charset
import java.nio.file.attribute._
import java.lang.{Iterable => JavaIterable}
import java.util.{List => JavaList, Map => JavaMap, Set => JavaSet}
import java.util.function.BiPredicate
import java.util.stream.{Stream => JavaStream}
import io.scalajs.nodejs.{FileDescriptor, fs, os, path}

import java.util
import scala.annotation.varargs
import scala.collection.mutable
import scala.jdk.CollectionConverters._
import scala.reflect.ClassTag
import scala.scalajs.js
import scala.util.Random

object Files {
  @varargs def copy(in: InputStream, target: Path, options: CopyOption*): Long = {
    if (Files.exists(target) && !options.contains(StandardCopyOption.REPLACE_EXISTING)) {
      throw new FileAlreadyExistsException(target.toString)
    }

    val bytes = new Array[Byte](1024)
    val fd    = fs.Fs.openSync(target.toString, "w")

    var bytesTotal: Int = 0
    var bytesRead: Int  = 0
    while ({
      bytesRead = in.read(bytes, bytesTotal, bytes.length)
      bytesRead != -1
    }) {
      val jsBuffer = js.typedarray.byteArray2Int8Array(bytes)
      fs.Fs.writeSync(fd, jsBuffer, offset = 0, length = bytesRead)
      bytesTotal += bytesRead
    }
    bytesTotal
  }

  def copy(source: Path, out: OutputStream): Long = {
    if (Files.notExists(source)) {
      throw new FileAlreadyExistsException(source.toString)
    }

    val bufferLength = 1024
    val buffer       = new js.typedarray.Int8Array(bufferLength)
    val readFd       = fs.Fs.openSync(source.toString, "r")

    var bytesTotal: Int = 0
    var bytesRead: Int  = 0
    while ({
      bytesRead =
        fs.Fs.readSync(readFd, buffer, offset = 0, length = bufferLength, position = bytesTotal)
      bytesRead > 0
    }) {
      val bytes = js.typedarray.int8Array2ByteArray(buffer.subarray(0, bytesRead))
      out.write(bytes)
      bytesTotal += bytesRead
    }
    bytesTotal
  }

  @varargs def copy(source: Path, target: Path, options: CopyOption*): Path = {
    @inline def innerCopy(): Unit = {
      if (Files.isDirectory(source)) {
        Files.createDirectories(target)
      } else {
        fs.Fs.copyFileSync(source.toString, target.toString, 0)
        val sourcePermissions = getPosixFilePermissions(source)
        if (options.contains(StandardCopyOption.COPY_ATTRIBUTES)) {
          setPosixFilePermissions(target, sourcePermissions)
          setLastModifiedTime(target, getLastModifiedTime(source))
        } else {
          val securedPermissions = (sourcePermissions.asScala.toSet diff Set(
            PosixFilePermission.GROUP_WRITE,
            PosixFilePermission.OTHERS_WRITE
          )).asJava
          setPosixFilePermissions(target, securedPermissions)
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
    target
  }

  @varargs def createDirectories(dir: Path, attrs: FileAttribute[_]*): Path = {
    validateInitialFileAttributes(attrs)
    val dirStr = dir.toString
    createDirectoryImpl(dirStr, attrs, recursive = true)
    dir
  }

  private def createDirectoryImpl(
      dir: String,
      attrs: Seq[FileAttribute[_]],
      recursive: Boolean
  ): Unit =
    fs.Fs.mkdirSync(
      dir,
      fs.MkdirOptions(
        recursive = recursive,
        mode = toNodejsFileMode(
          attrs,
          fs.Fs.constants.S_IRUSR | fs.Fs.constants.S_IWUSR | fs.Fs.constants.S_IXUSR |
            fs.Fs.constants.S_IRGRP | fs.Fs.constants.S_IXGRP |
            fs.Fs.constants.S_IROTH | fs.Fs.constants.S_IXOTH
        )
      )
    )

  @varargs def createDirectory(dir: Path, attrs: FileAttribute[_]*): Path = {
    validateInitialFileAttributes(attrs)
    val dirStr = dir.toString
    if (Files.exists(dir)) {
      throw new FileAlreadyExistsException(dirStr)
    }
    try {
      createDirectoryImpl(dirStr, attrs, recursive = false)
    } catch {
      case _: Throwable => throw new NoSuchFileException(dirStr)
    }
    dir
  }

  @varargs def createFile(path: Path, attrs: FileAttribute[_]*): Path = {
    validateInitialFileAttributes(attrs)
    try {
      fs.Fs.writeFileSync(
        path.toString,
        "",
        fs.FileAppendOptions(
          flag = "wx",
          mode = toNodejsFileMode(
            attrs,
            fs.Fs.constants.S_IRUSR | fs.Fs.constants.S_IWUSR | fs.Fs.constants.S_IRGRP | fs.Fs.constants.S_IROTH
          )
        )
      )
    } catch {
      case _: Throwable =>
        if (Files.exists(path.getParent)) {
          throw new FileAlreadyExistsException(path.toString)
        } else {
          throw new IOException()
        }
    }
    path
  }

  def createLink(link: Path, existing: Path): Path = {
    if (Files.exists(link)) {
      throw new FileAlreadyExistsException(link.toString)
    }
    try {
      fs.Fs.linkSync(existing.toString, link.toString)
    } catch {
      case e: Throwable => throw new IOException(e.getMessage)
    }
    link
  }

  @varargs def createSymbolicLink(link: Path, target: Path, attrs: FileAttribute[_]*): Path = {
    val newPath = link.toString
    if (Files.exists(link)) {
      throw new FileAlreadyExistsException(newPath)
    }
    if (attrs.nonEmpty) {
      throw new UnsupportedOperationException(
        s"`${attrs.head.name()}` not supported as initial attribute"
      )
    }
    val existingPath = target.toString
    fs.Fs.symlinkSync(existingPath, newPath)
    link
  }

  @varargs def createTempDirectory(dir: Path, prefix: String, attrs: FileAttribute[_]*): Path = {
    createTempDirectoryInternal(dir.toString, prefix, attrs)
  }

  @varargs def createTempDirectory(prefix: String, attrs: FileAttribute[_]*): Path = {
    createTempDirectoryInternal(defaultTempDir(), prefix, attrs)
  }

  private def createTempDirectoryInternal(
      dir: String,
      prefix: String,
      attrs: Seq[FileAttribute[_]]
  ): Path = {
    validateInitialFileAttributes(attrs)
    val joined = path.Path.join(dir, getRandomId(prefix, ""))
    val mode = toNodejsFileMode(
      attrs,
      fs.Fs.constants.S_IRUSR | fs.Fs.constants.S_IWUSR | fs.Fs.constants.S_IXUSR
    )
    fs.Fs.mkdirSync(
      joined,
      fs.MkdirOptions(
        mode = mode
      )
    )
    val normalized = path.Path.resolve(joined)
    Paths.get(normalized)
  }

  @varargs def createTempFile(
      dir: Path,
      prefix: String,
      suffix: String,
      attrs: FileAttribute[_]*
  ): Path = createTempFileInternal(dir.toString, prefix, suffix, attrs)

  @varargs def createTempFile(prefix: String, suffix: String, attrs: FileAttribute[_]*): Path =
    createTempFileInternal(defaultTempDir(), prefix, suffix, attrs)

  private def getRandomId(prefix: String, suffix: String): String = {
    val random  = Random.between(1000000000000000000L, Long.MaxValue)
    val suffix2 = if (suffix == null) ".tmp" else suffix
    s"${prefix}${random}${suffix2}"
  }

  private def validateInitialFileAttributes(attrs: Seq[FileAttribute[_]]): Unit = {
    attrs.find(_.name() != "posix:permissions").foreach { attr =>
      throw new UnsupportedOperationException(
        s"`${attr.name()}` not supported as initial attribute"
      )
    }
  }

  private def toNodejsFileMode(attrs: Seq[FileAttribute[_]], default: Int): Int = {
    attrs.collectFirst {
      case attr if attr.name() == "posix:permissions" =>
        attr.value().asInstanceOf[JavaSet[PosixFilePermission]]
    } match {
      case Some(permissions) =>
        var mode: Int = 0
        if (permissions.contains(PosixFilePermission.OWNER_READ)) mode |= fs.Fs.constants.S_IRUSR
        if (permissions.contains(PosixFilePermission.OWNER_WRITE)) mode |= fs.Fs.constants.S_IWUSR
        if (permissions.contains(PosixFilePermission.OWNER_EXECUTE)) mode |= fs.Fs.constants.S_IXUSR
        if (permissions.contains(PosixFilePermission.GROUP_READ)) mode |= fs.Fs.constants.S_IRGRP
        if (permissions.contains(PosixFilePermission.GROUP_EXECUTE)) mode |= fs.Fs.constants.S_IXGRP
        if (permissions.contains(PosixFilePermission.OTHERS_READ)) mode |= fs.Fs.constants.S_IROTH
        if (permissions.contains(PosixFilePermission.OTHERS_EXECUTE))
          mode |= fs.Fs.constants.S_IXOTH

        // JDK does not support the below permissions
        // if (permissions.contains(PosixFilePermission.GROUP_WRITE)) mode |= fs.Fs.constants.S_IWGRP
        // if (permissions.contains(PosixFilePermission.OTHERS_WRITE)) mode |= fs.Fs.constants.S_IWOTH
        mode
      case None =>
        default
    }
  }

  private def createTempFileInternal(
      dir: String,
      prefix: String,
      suffix: String,
      attrs: Seq[FileAttribute[_]]
  ): Path = {
    validateInitialFileAttributes(attrs)
    val fileName = getRandomId(prefix, suffix)
    val joined   = path.Path.join(dir, fileName)
    val fileMode = toNodejsFileMode(attrs, fs.Fs.constants.S_IRUSR | fs.Fs.constants.S_IWUSR)

    fs.Fs.writeFileSync(
      joined,
      "",
      fs.FileAppendOptions(
        mode = fileMode
      )
    )
    Paths.get(joined)
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

  def deleteIfExists(path: Path): Boolean = {
    if (notExists(path)) {
      false
    } else {
      delete(path)
      true
    }
  }

  @varargs def exists(path: Path, options: LinkOption*): Boolean =
    transformStats(path, options)(false)(_ => true)

  @varargs def find(
      start: Path,
      maxDepth: Int,
      matcher: BiPredicate[Path, BasicFileAttributes],
      options: FileVisitOption*
  ): JavaStream[Path] =
    throw new UnsupportedOperationException(
      "find. Since Scala-js does not support java.util.stream"
    )

  @varargs def getAttribute(path: Path, attribute: String, options: LinkOption*): AnyRef = {
    val typeName      = attributeFormatCheck(attribute)
    val attrs         = readAttributes(path, classOf[PosixFileAttributes], options: _*)
    val attributeName = attribute.substring(attribute.indexOf(':') + 1)
    (attributeName match {
      case "isDirectory"                        => attrs.isDirectory()
      case "isOther"                            => attrs.isOther()
      case "isRegularFile"                      => attrs.isRegularFile()
      case "isSymbolicLink"                     => attrs.isSymbolicLink()
      case "size"                               => attrs.size()
      case "fileKey"                            => attrs.fileKey()
      case "creationTime"                       => attrs.creationTime()
      case "lastAccessTime"                     => attrs.lastAccessTime()
      case "lastModifiedTime"                   => attrs.lastModifiedTime()
      case "permissions" if typeName == "posix" => attrs.permissions()
      case _                                    => throw new IllegalArgumentException(s"`${attribute}` not recognized")
    }).asInstanceOf[AnyRef]
  }

  @varargs def getFileAttributeView[V <: FileAttributeView](
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

  def getFileStore(path: Path): FileStore = throw new UnsupportedOperationException("getFileStore")

  @varargs def getOwner(path: Path, options: LinkOption*): UserPrincipal =
    throw new UnsupportedOperationException("getOwner")

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

  private def transformStatsOrThrow[T](path: Path, options: Seq[LinkOption])(
      transformer: fs.Stats => T
  ): T = {
    transformStats(path, options)(throw new NoSuchFileException(path.toString))(transformer)
  }

  @varargs def getPosixFilePermissions(
      path: Path,
      options: LinkOption*
  ): JavaSet[PosixFilePermission] = {
    transformStatsOrThrow(path, options)(PosixFilePermissionsHelper.fromJsStats)
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

  def lines(path: Path): JavaStream[String] =
    throw new UnsupportedOperationException(
      "lines. Since Scala-js does not support java.util.stream"
    )

  def lines(path: Path, cs: Charset): JavaStream[String] =
    throw new UnsupportedOperationException(
      "lines. Since Scala-js does not support java.util.stream"
    )

  def list(dir: Path): JavaStream[String] =
    throw new UnsupportedOperationException(
      "lines. Since Scala-js does not support java.util.stream"
    )

  @varargs def move(source: Path, target: Path, options: CopyOption*): Path = {
    if (options.contains(StandardCopyOption.COPY_ATTRIBUTES)) {
      throw new UnsupportedOperationException()
    } else if (notExists(source)) {
      throw new NoSuchFileException(source.toString)
    } else if (source != target) {
      if (options.contains(StandardCopyOption.REPLACE_EXISTING)) {
        try {
          Files.delete(target)
        } catch {
          case _: Throwable => throw new DirectoryNotEmptyException(target.toString)
        }
      } else if (exists(target)) {
        throw new FileAlreadyExistsException(target.toString)
      }
      fs.Fs.renameSync(source.toString, target.toString)
    }
    target
  }

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
  ): A = {
    if (`type` == classOf[BasicFileAttributes] || `type` == classOf[PosixFileAttributes]) {
      val attrs = transformStatsOrThrow(path, options) { stats =>
        new NodeJsPosixFileAttributes(stats)
      }
      attrs.asInstanceOf[A]
    } else {
      throw new UnsupportedOperationException(s"Unsupported class ${`type`}")
    }
  }

  private lazy val basicFileAttributesKeys = Set(
    "isDirectory",
    "isOther",
    "isRegularFile",
    "isSymbolicLink",
    "size",
    "fileKey",
    "creationTime",
    "lastAccessTime",
    "lastModifiedTime"
  )
  private lazy val posixFileAttributesKeys = basicFileAttributesKeys ++ Set("permissions")

  private def attributeFormatCheck(attributes: String): String = {
    val typeName = attributes.slice(0, attributes.indexOf(':'))
    if (attributes.contains(":") && !attributes.startsWith("basic:") && !attributes.startsWith(
          "posix:"
        )) {
      throw new UnsupportedOperationException(s"View '${typeName}' not available")
    }
    typeName
  }

  @varargs def readAttributes(
      path: Path,
      attributes: String,
      options: LinkOption*
  ): JavaMap[String, Any] = {
    val typeName = attributeFormatCheck(attributes)
    val (attrs, attrKeys) = typeName match {
      case "" | "basic" =>
        (readAttributes(path, classOf[BasicFileAttributes], options: _*), basicFileAttributesKeys)
      case "posix" =>
        (readAttributes(path, classOf[PosixFileAttributes], options: _*), posixFileAttributesKeys)
    }
    val keys = {
      val keySet  = attributes.substring(attributes.indexOf(':') + 1).split(",").toSet
      val keyDiff = keySet.diff(attrKeys) - "*"
      if (keyDiff.nonEmpty) {
        throw new IllegalArgumentException(s"Unknown attributes `${keyDiff.mkString(",")}`")
      } else if (keySet.contains("*")) {
        basicFileAttributesKeys
      } else {
        keySet
      }
    }
    val mapBuilder = mutable.Map[String, Any]()
    if (keys("isDirectory")) mapBuilder.put("isDirectory", attrs.isDirectory())
    if (keys("isOther")) mapBuilder.put("isOther", attrs.isOther())
    if (keys("isRegularFile")) mapBuilder.put("isRegularFile", attrs.isRegularFile())
    if (keys("isSymbolicLink")) mapBuilder.put("isSymbolicLink", attrs.isSymbolicLink())
    if (keys("size")) mapBuilder.put("size", attrs.size())
    if (keys("fileKey")) mapBuilder.put("fileKey", attrs.fileKey())
    if (keys("creationTime")) mapBuilder.put("creationTime", attrs.creationTime())
    if (keys("lastAccessTime")) mapBuilder.put("lastAccessTime", attrs.lastAccessTime())
    if (keys("lastModifiedTime")) mapBuilder.put("lastModifiedTime", attrs.lastModifiedTime())
    if (keys("permissions"))
      mapBuilder.put("permissions", attrs.asInstanceOf[PosixFileAttributes].permissions())
    mapBuilder.asJava
  }

  def readSymbolicLink(link: Path): Path = {
    if (!isSymbolicLink(link)) {
      throw new NotLinkException(link.toString)
    }
    val linkPath = fs.Fs.readlinkSync(link.toString)
    Paths.get(linkPath)
  }

  @varargs def setAttribute(
      path: Path,
      attribute: String,
      value: AnyRef,
      options: LinkOption*
  ): Path = {
    val typeName = attributeFormatCheck(attribute)

    val attributeName = attribute.substring(attribute.indexOf(':') + 1)
    def transformValue[V](setter: V => Unit)(implicit classtag: ClassTag[V]): Unit = value match {
      case expected: V => setter(expected)
      case _           => throw new ClassCastException(s"${value.getClass} cannot be cast to class $classtag")
    }

    attributeName match {
      case "lastAccessTime" =>
        transformValue { time: FileTime =>
          transformStatsOrThrow(path, options) { stats =>
            fs.Fs.utimesSync(
              path.toString,
              atime = (time.toMillis() / 1000).toString,
              mtime = stats.mtime
            )
          }
        }
      case "lastModifiedTime" =>
        setLastModifiedTime(path, value.asInstanceOf[FileTime])
      case "creationTime" =>
      // do nothing
      case "permissions" if typeName == "posix" =>
        Files.setPosixFilePermissions(path, value.asInstanceOf[java.util.Set[PosixFilePermission]])
      case _ => throw new IllegalArgumentException(s"`${attribute}` not recognized")
    }
    path
  }

  @varargs def getLastModifiedTime(path: Path, options: LinkOption*): FileTime = {
    transformStatsOrThrow(path, options) { stats =>
      FileTime.fromMillis(stats.mtimeMs.toLong)
    }
  }

  def setLastModifiedTime(path: Path, time: FileTime): Path = {
    if (time == null) {
      throw new NullPointerException()
    }
    transformStatsOrThrow(path, Seq.empty) { stats =>
      fs.Fs.utimesSync(
        path.toString,
        atime = stats.atime,
        mtime = (time.toMillis() / 1000).toString
      )
    }
    path
  }

  def setOwner(path: Path, owner: UserPrincipal): Path =
    throw new UnsupportedOperationException("setOwner")

  def setPosixFilePermissions(path: Path, perms: JavaSet[PosixFilePermission]): Path = {
    if (notExists(path)) {
      throw new NoSuchFileException(path.toString)
    }
    val fileMode = perms.asScala.map {
      case PosixFilePermission.OWNER_READ     => fs.Fs.constants.S_IRUSR
      case PosixFilePermission.OWNER_WRITE    => fs.Fs.constants.S_IWUSR
      case PosixFilePermission.OWNER_EXECUTE  => fs.Fs.constants.S_IXUSR
      case PosixFilePermission.GROUP_READ     => fs.Fs.constants.S_IRGRP
      case PosixFilePermission.GROUP_WRITE    => fs.Fs.constants.S_IWGRP
      case PosixFilePermission.GROUP_EXECUTE  => fs.Fs.constants.S_IXGRP
      case PosixFilePermission.OTHERS_READ    => fs.Fs.constants.S_IROTH
      case PosixFilePermission.OTHERS_WRITE   => fs.Fs.constants.S_IWOTH
      case PosixFilePermission.OTHERS_EXECUTE => fs.Fs.constants.S_IXOTH
    }.sum
    fs.Fs.chmodSync(path.toString, fileMode)
    path
  }

  def size(path: Path): Long = transformStatsOrThrow(path, Seq.empty)(_.size.toLong)

  @varargs def walk(start: Path, options: FileVisitOption*): JavaStream[Path] =
    throw new UnsupportedOperationException(
      "walk. Since Scala-js does not support java.util.stream"
    )
  @varargs def walk(start: Path, maxDepth: Int, options: FileVisitOption*): JavaStream[Path] =
    throw new UnsupportedOperationException(
      "walk. Since Scala-js does not support java.util.stream"
    )

  def walkFileTree(start: Path, visitor: FileVisitor[_ >: Path]): Path = {
    walkFileTree(start, util.Collections.emptySet(), Int.MaxValue, visitor)
  }
  def walkFileTree(
      start: Path,
      options: JavaSet[FileVisitOption],
      maxDepth: Int,
      visitor: FileVisitor[_ >: Path]
  ): Path = {
    import FileVisitResult._
    if (maxDepth < 0) {
      throw new IllegalArgumentException
    }

    val followLinks = options.contains(FileVisitOption.FOLLOW_LINKS)

    val linkOptions = if (followLinks) {
      Seq.empty
    } else {
      Seq(LinkOption.NOFOLLOW_LINKS)
    }

    def readAttr(path: Path): BasicFileAttributes =
      Files.readAttributes(path, classOf[BasicFileAttributes], linkOptions: _*)

    def visit(start: Path, depth: Int): FileVisitResult = {
      if (isRegularFile(start)) {
        visitFile(start)
      } else if (isSymbolicLink(start)) {
        if (depth > 0 && followLinks) {
          visitDirectory(start, depth)
        } else {
          visitFile(start)
        }
      } else if (isDirectory(start)) {
        if (depth > 0) {
          visitDirectory(start, depth)
        } else {
          visitFile(start)
        }
      } else {
        // TODO: non-regular file
        CONTINUE
      }
    }

    def visitFile(start: Path): FileVisitResult = {
      val result = try {
        visitor.visitFile(start, readAttr(start))
      } catch {
        case ioe: IOException =>
          visitor.visitFileFailed(start, ioe)
      }
      result match {
        case SKIP_SUBTREE => CONTINUE
        case otherwise    => otherwise
      }
    }

    def visitDirectory(start: Path, depth: Int): FileVisitResult = {
      visitor.preVisitDirectory(start, readAttr(start)) match {
        case TERMINATE     => TERMINATE
        case SKIP_SUBTREE  => CONTINUE
        case SKIP_SIBLINGS => SKIP_SIBLINGS
        case CONTINUE =>
          val startString = start.toString
          val startPath   = Paths.get(startString)
          var active      = true
          for (entryName <- fs.Fs.readdirSync(startString) if active) {
            val nioPath = startPath.resolve(entryName)
            visit(nioPath, depth - 1) match {
              case TERMINATE               => return TERMINATE
              case SKIP_SIBLINGS           => active = false
              case SKIP_SUBTREE | CONTINUE =>
            }
          }
          visitor.postVisitDirectory(start, null) match {
            case SKIP_SIBLINGS => CONTINUE
            case otherwise     => otherwise
          }
      }
    }

    visit(start, maxDepth)
    start
  }

  @varargs def write(path: Path, bytes: Array[Byte], options: OpenOption*): Path = {
    val validatedOptions = validateWriteOptions(path, options)
    val nodejsFlags =
      if (validatedOptions.contains(StandardOpenOption.APPEND)) {
        "a"
      } else {
        "w"
      }
    val fd       = openFileErrorHandling(path, nodejsFlags)
    val jsBuffer = js.typedarray.byteArray2Int8Array(bytes)
    fs.Fs.writeSync(fd, jsBuffer)
    path
  }
  private def openFileErrorHandling(path: Path, flags: String): FileDescriptor =
    try {
      fs.Fs.openSync(path.toString, flags)
    } catch {
      case jse: js.JavaScriptException if jse.getMessage().contains("EISDIR") =>
        throw new IOException(s"${path} is a directory")
    }

  @varargs def write(
      path: Path,
      lines: JavaIterable[_ <: CharSequence],
      cs: Charset,
      options: OpenOption*
  ): Path = {
    writeInternal(path, lines, cs.displayName(), options)
  }
  @varargs def write(
      path: Path,
      lines: JavaIterable[_ <: CharSequence],
      options: OpenOption*
  ): Path = {
    writeInternal(path, lines, "utf8", options)
  }
  private def validateWriteOptions(path: Path, rawOptions: Seq[OpenOption]): Set[OpenOption] = {
    val options: Set[OpenOption] = if (rawOptions.isEmpty) {
      Set(
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING,
        StandardOpenOption.WRITE
      )
    } else if (rawOptions.contains(StandardOpenOption.READ)) {
      throw new IllegalArgumentException()
    } else {
      rawOptions.toSet
    }

    if (Files.exists(path)) {
      if (options.contains(StandardOpenOption.CREATE_NEW)) {
        throw new FileAlreadyExistsException(path.toString)
      }
    } else {
      if (!options.contains(StandardOpenOption.CREATE) && !options.contains(
            StandardOpenOption.CREATE_NEW
          )) {
        throw new NoSuchFileException(path.toString)
      }
    }

    options
  }

  private def writeInternal(
      path: Path,
      lines: JavaIterable[_ <: CharSequence],
      encoding: String,
      rawOptions: Seq[OpenOption]
  ): Path = {
    val validatedOptions = validateWriteOptions(path, rawOptions)
    val nodejsFlags =
      if (validatedOptions.contains(StandardOpenOption.APPEND)) {
        "a"
      } else {
        "w"
      }
    val fd         = openFileErrorHandling(path, nodejsFlags)
    val dataString = lines.asScala.mkString(start = "", sep = os.OS.EOL, end = os.OS.EOL)
    fs.Fs.writeFileSync(
      fd,
      dataString,
      fs.FileAppendOptions(
        encoding = encoding
      )
    )
    path
  }

  private def defaultTempDir(): String = os.OS.tmpdir()
}
