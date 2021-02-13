package support

import org.scalatest.funsuite.AnyFunSuite

import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.attribute.{FileAttribute, PosixFilePermission, PosixFilePermissions}
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

  test("createTempDirectory(Path, String, FileAttribute[_])") {
    val base    = Paths.get("./")
    val tempDir = Files.createTempDirectory(base, "foobar")
    assert(tempDir.toString.contains("/foobar"))
    assert(Files.exists(tempDir))

    // TODO: attrs
  }

  test("createTempDirectory(String, FileAttribute[_])") {
    val tempDir = Files.createTempDirectory("foobar")
    assert(tempDir.toString.contains("/foobar"))
    assert(Files.exists(tempDir))

    // TODO: attrs
  }

  ignore("createTempFile(Path, String, String, FileAttribute[_])") {}

  ignore("createTempFile(String, String, FileAttribute[_])") {}

  ignore("delete(Path)") {}

  ignore("deleteIfExists(Path)") {}

  test("exists(Path, LinkOption*)") {
    assert(!Files.exists(noSuchFile))
    assert(!Files.exists(noSuchFileInDir))

    assert(Files.exists(fileInSymlink))
    assert(Files.exists(fileInSymlink, LinkOption.NOFOLLOW_LINKS))

    assert(Files.exists(fileInSymlink))
    assert(Files.exists(fileInSymlink, LinkOption.NOFOLLOW_LINKS))

    assert(!Files.exists(fileInDeletedSymlink))
    assert(!Files.exists(fileInDeletedSymlink, LinkOption.NOFOLLOW_LINKS))

    assert(!Files.exists(deletedSymlinkFile))
    assert(Files.exists(deletedSymlinkFile, LinkOption.NOFOLLOW_LINKS))
  }

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

  private val sjisTxt = Paths.get("jdk/shared/src/test/resources/utf16be.txt")

  private val fileInSource  = Paths.get("jdk/shared/src/test/resources/source/hello.txt")
  private val fileInSymlink = Paths.get("jdk/shared/src/test/resources/symlink/hello.txt")
  private val fileInDeletedSymlink =
    Paths.get("jdk/shared/src/test/resources/deleted-symlink/hello.txt")
  private val deletedSymlinkFile = Paths.get("jdk/shared/src/test/resources/deleted-symlink.txt")

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
    assert(Files.isSameFile(deletedSymlinkFile, deletedSymlinkFile))

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
    assert(Files.isSymbolicLink(deletedSymlinkFile))
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

  test("notExists(Path, LinkOption*)") {
    assert(Files.notExists(noSuchFile))
    assert(Files.notExists(noSuchFileInDir))

    assert(!Files.notExists(fileInSymlink))
    assert(!Files.notExists(fileInSymlink, LinkOption.NOFOLLOW_LINKS))

    assert(!Files.notExists(fileInSymlink))
    assert(!Files.notExists(fileInSymlink, LinkOption.NOFOLLOW_LINKS))

    assert(Files.notExists(fileInDeletedSymlink))
    assert(Files.notExists(fileInDeletedSymlink, LinkOption.NOFOLLOW_LINKS))

    assert(Files.notExists(deletedSymlinkFile))
    assert(!Files.notExists(deletedSymlinkFile, LinkOption.NOFOLLOW_LINKS))
  }

  test("probeContentType(Path)") {
    // Return null on any files by default ?
    assert(Files.probeContentType(noSuchSubDir) === null)
    assert(Files.probeContentType(noSuchFile) === null)
    assert(Files.probeContentType(directory) === null)
    assert(Files.probeContentType(fileInHidden) === null)
    assert(Files.probeContentType(fileInSource) === null)
  }

  test("readAllBytes(Path)") {
    assert(
      Files.readAllBytes(fileInSource) === Array[Byte](
        102, 111, 111, 10, 98, 97, 114, 10, -26, -105, -91, -26, -100, -84, -24, -86, -98, 10
      )
    )
    assert(
      Files.readAllBytes(fileInSymlink) === Array[Byte](
        102, 111, 111, 10, 98, 97, 114, 10, -26, -105, -91, -26, -100, -84, -24, -86, -98, 10
      )
    )
    assertThrows[IOException] {
      Files.readAllBytes(directory)
    }
    assertThrows[IOException] {
      Files.readAllBytes(noSuchFile)
    }
  }

  test("readAllLines(Path)") {
    assert(Files.readAllLines(fileInSource).asScala === Seq("foo", "bar", "日本語"))
    assert(Files.readAllLines(fileInSymlink).asScala === Seq("foo", "bar", "日本語"))
    assertThrows[IOException] {
      Files.readAllLines(directory)
    }
    assertThrows[IOException] {
      Files.readAllLines(noSuchFile)
    }
  }

  test("readAllLines(Path, Charset)") {
    val utf16le = Charset.forName("UTF-16LE")
    assert(Files.readAllLines(sjisTxt, utf16le).asScala === Seq("\uFEFFUTF-16LE", "日本語"))
    assert(Files.readAllLines(fileInSymlink, utf16le).asScala !== Seq("foo", "bar", "buz"))
    assertThrows[IOException] {
      Files.readAllLines(directory, utf16le)
    }
    assertThrows[IOException] {
      Files.readAllLines(noSuchFile, utf16le)
    }
  }

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
    assert(Files.size(fileInSource) === 18)

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
