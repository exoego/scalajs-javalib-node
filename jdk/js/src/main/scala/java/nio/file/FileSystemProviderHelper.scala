package java.nio.file

import io.scalajs.nodejs.fs

import java.util.{Set => JavaSet}
import java.nio.file.attribute._

private[file] object FileSystemProviderHelper {
  private val basicFileAttributesKeys = Set(
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
  private val posixFileAttributesKeys = basicFileAttributesKeys ++ Set("permissions")

  def toAttributeClass(typeName: String): (Class[_ <: BasicFileAttributes]) = {
    typeName match {
      case "" | "basic" => classOf[BasicFileAttributes]
      case "posix"      => classOf[PosixFileAttributes]
    }
  }

  def keys(attributes: String): Set[String] = {
    val typeName = attributeFormatCheck(attributes)
    val attrKeys = typeName match {
      case "" | "basic" =>
        basicFileAttributesKeys
      case "posix" =>
        posixFileAttributesKeys
    }

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

  def attributeFormatCheck(attributes: String): String = {
    val typeName = attributes.slice(0, attributes.indexOf(':'))
    if (attributes.contains(":") && !attributes.startsWith("basic:") && !attributes.startsWith(
          "posix:"
        )) {
      throw new UnsupportedOperationException(s"View '${typeName}' not available")
    }
    typeName
  }

  def validateInitialFileAttributes(attrs: Seq[FileAttribute[_]]): Unit = {
    attrs.find(_.name() != "posix:permissions").foreach { attr =>
      throw new UnsupportedOperationException(
        s"`${attr.name()}` not supported as initial attribute"
      )
    }
  }

  def toNodejsFileMode(attrs: Seq[FileAttribute[_]], default: Int): Int = {
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

  def createDirectoryImpl(
      dir: String,
      attrs: Seq[FileAttribute[_]],
      recursive: Boolean
  ): Unit =
    fs.Fs.mkdirSync(
      dir,
      fs.MkdirOptions(
        recursive = recursive,
        mode = FileSystemProviderHelper.toNodejsFileMode(
          attrs,
          fs.Fs.constants.S_IRUSR | fs.Fs.constants.S_IWUSR | fs.Fs.constants.S_IXUSR |
            fs.Fs.constants.S_IRGRP | fs.Fs.constants.S_IXGRP |
            fs.Fs.constants.S_IROTH | fs.Fs.constants.S_IXOTH
        )
      )
    )

  def transformStats[T](path: Path, options: Seq[LinkOption])(
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

  def transformStatsOrThrow[T](path: Path, options: Seq[LinkOption])(
      transformer: fs.Stats => T
  ): T = {
    transformStats(path, options)(throw new NoSuchFileException(path.toString))(transformer)
  }
}
