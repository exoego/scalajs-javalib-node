package support

import org.scalatest.funsuite.AnyFunSuite

import java.nio.file.attribute.{PosixFilePermission, PosixFilePermissions}
import java.nio.file.{Files, LinkOption, NoSuchFileException, Paths}
import scala.jdk.CollectionConverters._

class FilesTest extends AnyFunSuite {
  ignore("copy(InputStream, Path, CopyOption*)") {}

  ignore("copy(Path, OutputStream)") {}

  ignore("copy(Path, Path, CopyOption*)") {}

  ignore("createDirectories(Path, FileAttribute[_]*)") {}

  ignore("createDirectory(Path, FileAttribute[_]*)") {}

  ignore("createFile(Path, FileAttribute[_]*)") {}

  ignore("createLink(Path, Path)") {}

  ignore("createSymbolicLink(Path, Path, FileAttribute[_])") {}

  ignore("createTempDirectory(Path, String, FileAttribute[_])") {}

  ignore("createTempDirectory(String, FileAttribute[_])") {}

  ignore("createTempFile(Path, String, String, FileAttribute[_])") {}

  ignore("createTempFile(String, String, FileAttribute[_])") {}

  ignore("delete(Path)") {}

  ignore("deleteIfExists(Path)") {}

  ignore("exists(Path, LinkOption*)") {}

  ignore("find(Path, Int, BiPredicate[Path, BasicFileAttributes], FileVisitOption*)") {}

  ignore("getAttribute(Path, String, LinkOption*)") {}

  ignore("getFileAttributeView[V <: FileAttributeView](Path, Class[V], LinkOption*)") {}

  ignore("getFileStore(Path)") {}

  ignore("getOwner(Path, LinkOption*)") {}

  test("getPosixFilePermissions(Path, LinkOption*)") {
    import PosixFilePermission._
    assert(Files.getPosixFilePermissions(rrr).asScala === Set(GROUP_READ, OWNER_READ, OTHERS_READ))
    assert(
      Files.getPosixFilePermissions(rwxrwxrwx).asScala === Set(
        GROUP_READ,
        OWNER_READ,
        OTHERS_READ,
        GROUP_EXECUTE,
        OWNER_EXECUTE,
        OTHERS_EXECUTE,
        GROUP_WRITE,
        OWNER_WRITE,
        OTHERS_WRITE
      )
    )

    assertThrows[NoSuchFileException] {
      Files.getPosixFilePermissions(noSuchFile).asScala === Set()
    }

    val mutable = Files.getPosixFilePermissions(rrr)
    mutable.clear()
    mutable.add(GROUP_WRITE)
    assert(mutable.asScala === Set(GROUP_WRITE))
    assert(mutable.asScala === Set(GROUP_WRITE))
  }

  private val directory        = Paths.get("project")
  private val directorySource  = Paths.get("jdk/shared/src/test/resources/source")
  private val directorySymlink = Paths.get("jdk/shared/src/test/resources/symlink")
  private val subDirectory     = Paths.get("project/target")

  private val fileInSource  = Paths.get("jdk/shared/src/test/resources/source/hello.txt")
  private val fileInSymlink = Paths.get("jdk/shared/src/test/resources/symlink/hello.txt")

  private val regularText = Paths.get("jdk/shared/src/test/resources/regular.txt")
  private val symlinkText = Paths.get("jdk/shared/src/test/resources/symbolic.txt")

  private val rrr       = Paths.get("jdk/shared/src/test/resources/permissions/rrr.txt")
  private val rwxrwxrwx = Paths.get("jdk/shared/src/test/resources/permissions/rwxrwxrwx.txt")

  private val hiddenDirectory = Paths.get("jdk/shared/src/test/resources/.hidden")
  private val fileInHidden    = Paths.get("jdk/shared/src/test/resources/.hidden/foo.txt")

  private val noSuchFileInDir = Paths.get("project", "no-such-file")
  private val noSuchFile      = Paths.get("no-such-file")
  private val noSuchSubDir    = Paths.get("no-such-dir/no-such-sub")

  test("isDirectory(Path)") {
    assert(Files.isDirectory(directory))
    assert(Files.isDirectory(subDirectory))
    assert(Files.isDirectory(directorySource))
    assert(Files.isDirectory(directorySymlink))

    assert(!Files.isDirectory(fileInSource))
    assert(!Files.isDirectory(noSuchFileInDir))
    assert(!Files.isDirectory(noSuchSubDir))
  }

  test("isDirectory(Path, LinkOption*)") {
    val option = LinkOption.NOFOLLOW_LINKS
    assert(Files.isDirectory(directory, option))
    assert(Files.isDirectory(directorySource, option))
    assert(!Files.isDirectory(directorySymlink, option))

    assert(!Files.isDirectory(regularText, option))
  }

  test("isExecutable(Path)") {
    assert(!Files.isExecutable(Paths.get("build.sbt")))
    assert(!Files.isExecutable(noSuchFile))
    assert(Files.isExecutable(rwxrwxrwx))
    assert(!Files.isExecutable(rrr))

    // directory is executable
    assert(Files.isExecutable(directory))
    assert(Files.isExecutable(Paths.get("jdk/shared")))
  }

  test("isHidden(Path)") {
    assert(!Files.isHidden(rwxrwxrwx))
    assert(Files.isHidden(hiddenDirectory))
    assert(!Files.isHidden(fileInHidden))
  }

  test("isReadable(Path)") {
    assert(Files.isReadable(regularText))
    assert(!Files.isReadable(noSuchFile))

    assert(Files.isReadable(regularText))
    assert(!Files.isReadable(noSuchFileInDir))
  }

  test("isRegularFile(Path, LinkOption*)") {
    assert(!Files.isRegularFile(directorySource))
    assert(Files.isRegularFile(fileInSource))
    assert(!Files.isRegularFile(directorySymlink, LinkOption.NOFOLLOW_LINKS))
    assert(Files.isRegularFile(fileInSource, LinkOption.NOFOLLOW_LINKS))

    assert(!Files.isRegularFile(directorySymlink))
    assert(!Files.isRegularFile(directorySymlink, LinkOption.NOFOLLOW_LINKS))
    assert(Files.isRegularFile(fileInSymlink))
    assert(Files.isRegularFile(fileInSymlink, LinkOption.NOFOLLOW_LINKS))

    assert(Files.isRegularFile(regularText))
    assert(Files.isRegularFile(regularText, LinkOption.NOFOLLOW_LINKS))
    assert(Files.isRegularFile(symlinkText))
    assert(Files.isRegularFile(symlinkText, LinkOption.NOFOLLOW_LINKS))

    assert(!Files.isRegularFile(noSuchFile))
  }

  test("isSameFile(Path, Path)") {
    assert(Files.isSameFile(regularText, regularText))
    assert(Files.isSameFile(symlinkText, symlinkText))
    assert(!Files.isSameFile(regularText, symlinkText))

    assert(Files.isSameFile(directorySource, directorySource))
    assert(Files.isSameFile(directorySymlink, directorySymlink))
    assert(Files.isSameFile(directorySymlink, directorySymlink))

    assert(Files.isSameFile(Paths.get("README.md"), Paths.get("./project/../README.md")))
    assert(Files.isSameFile(noSuchFile, noSuchFile))
    assert(Files.isSameFile(noSuchSubDir, noSuchSubDir))

    assertThrows[NoSuchFileException] {
      Files.isSameFile(noSuchSubDir, noSuchFile)
    }
  }

  test("isSymbolicLink(Path)") {
    assert(!Files.isSymbolicLink(directorySource))
    assert(!Files.isSymbolicLink(fileInSource))
    assert(Files.isSymbolicLink(directorySymlink))
    assert(!Files.isSymbolicLink(fileInSymlink))
    assert(!Files.isSymbolicLink(noSuchFile))
  }

  test("isWritable(Path)") {
    assert(Files.isWritable(directory))
    assert(Files.isWritable(fileInSource))
    assert(Files.isWritable(fileInHidden))
    assert(Files.isWritable(fileInSymlink))
    assert(!Files.isWritable(noSuchFile))
    assert(!Files.isWritable(rrr))
  }

  ignore("lines(Path)") {}

  ignore("lines(Path, Charset)") {}

  ignore("list(Path)") {}

  ignore("move(Path, Path, CopyOption*)") {}

  ignore("newBufferedReader(Path)") {}

  ignore("newBufferedReader(Path, Charset)") {}

  ignore("newBufferedWriter(Path, Charset, OpenOption*)") {}

  ignore("newBufferedWriter(Path, OpenOption*)") {}

  ignore("newByteChannel(Path, OpenOption*)") {}

  ignore("newByteChannel(Path, JavaSet[_ <: OpenOption], FileAttribute[_]*)") {}

  ignore("newDirectoryStream(Path)") {}

  ignore("newDirectoryStream(Path, DirectoryStream.Filter[_ >: Path])") {}

  ignore("newDirectoryStream(Path, String)") {}

  ignore("newInputStream(Path, OpenOption*)") {}

  ignore("newOutputStream(Path, OpenOption*)") {}

  ignore("notExists(Path, LinkOption*)") {}

  ignore("probeContentType(Path)") {}

  ignore("readAllBytes(Path)") {}

  ignore("readAllLines(Path)") {}

  ignore("readAllLines(Path, Charset)") {}

  ignore("readAttributes[A <: BasicFileAttributes](Path, Class[A], LinkOption*)") {}

  ignore("readAttributes[A <: BasicFileAttributes](Path, String, LinkOption*)") {}

  ignore("readSymbolicLink(Path)") {}

  ignore("setAttribute(Path, String, AnyRef, LinkOption*)") {}

  ignore("setLastModifiedTime(Path, FileTime)") {}

  ignore("setOwner(Path, UserPrincipal)") {}

  ignore("setPosixFilePermissions(Path, JavaSet[PosixFilePermission])") {}

  test("size(Path)") {
    // directory
    assert(Files.size(directorySource) === 96)

    // file
    assert(Files.size(fileInSource) === 0)

    // symlink
    assert(Files.size(directorySymlink) === 96)
  }

  ignore("walk(Path, FileVisitOption*)") {}
  ignore("walk(Path, Int, FileVisitOption*)") {}

  ignore("walkFileTree(Path, FileVisitor[_ >: Path])") {}
  ignore("walkFileTree(Path, JavaSet[FileVisitOption], maxDepth:Int, FileVisitor[_ >: Path])") {}

  ignore("write(Path, Array[Byte], OpenOption*)") {}
  ignore("write(Path, JavaIterable[_ <: CharSequence], Charset, OpenOption*)") {}
  ignore("write(Path, JavaIterable[_ <: CharSequence], OpenOption*)") {}

}
