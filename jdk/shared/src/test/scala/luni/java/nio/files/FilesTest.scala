package luni.java.nio.files

import org.scalatest.funsuite.AnyFunSuite

import java.io._
import java.nio.charset.Charset
import java.nio.file._
import java.nio.file.attribute._
import scala.jdk.CollectionConverters._

class FilesTest extends AnyFunSuite {

  private def using[T <: AutoCloseable](resource: T)(block: T => Unit): Unit = {
    try {
      block(resource)
    } finally {
      resource.close()
    }
  }
  private val utf16leCharset: Charset = Charset.forName("UTF-16LE")

  test("copy(InputStream, Path, CopyOption*)") {
    val sourceFile = Files.createTempFile("foo", ".txt")
    val tmpDir     = Files.createTempDirectory("foo")
    using(new FileInputStream(sourceFile.toFile)) { in =>
      val targetFile = tmpDir.resolve("newFile1.txt")
      assert(Files.copy(in, targetFile) === 0)
    }

    Files.write(sourceFile, Seq("abc").asJava)
    using(new FileInputStream(sourceFile.toFile)) { in =>
      val targetFile = tmpDir.resolve("newFile2.txt")
      assert(Files.copy(in, targetFile) === 4)
      assert(Files.readAllLines(targetFile).asScala === Seq("abc"))
    }

    assertThrows[IOException] {
      val closedStream = new FileInputStream(sourceFile.toFile)
      closedStream.close()
      Files.copy(closedStream, tmpDir.resolve("empty.txt"))
    }

    using(new FileInputStream(sourceFile.toFile)) { in =>
      val targetFile = tmpDir.resolve("newFile1.txt")
      assertThrows[FileAlreadyExistsException] {
        Files.copy(in, targetFile)
      }
    }
  }

  test("copy(Path, OutputStream)") {
    val tmpFile = Files.createTempFile("foo", ".txt")
    using(new FileOutputStream(tmpFile.toFile)) { out =>
      assert(Files.copy(regularText, out) === 3)
      assert(Files.readAllLines(tmpFile).asScala.toSeq === Seq("txt"))
    }
    // overwrite
    using(new FileOutputStream(tmpFile.toFile)) { out =>
      assert(Files.copy(utf16leTxt, out) === 28)
      assert(
        Files.readAllLines(tmpFile, utf16leCharset).asScala.toSeq === Seq("\uFEFFUTF-16LE", "日本語")
      )
    }
    // missing source
    using(new FileOutputStream(tmpFile.toFile)) { out =>
      assertThrows[IOException] {
        Files.copy(noSuchFile, out)
      }
      assert(Files.size(tmpFile) === 0)
      assert(Files.readAllLines(tmpFile).asScala.toSeq === Seq())
    }
  }

  test("copy(Path, Path, CopyOption*)") {
    // If source is directory, create an empty dir
    val root        = Files.createTempDirectory("root")
    val nonEmptyDir = Files.createTempDirectory(root, "nonEmptyDir")
    Files.createTempFile(nonEmptyDir, "file", ".txt")

    val targetDir = root.resolve("targetDir")
    assert(Files.copy(nonEmptyDir, targetDir) === targetDir)
    assert(Files.exists(targetDir))
    assertThrows[FileAlreadyExistsException] {
      Files.copy(nonEmptyDir, targetDir)
    }

    // Copy file
    val sourceFile = Files.createFile(root.resolve("foo.txt"))
    Files.write(sourceFile, Seq("abc").asJava)
    val newFile = root.resolve("newFile.txt")
    assert(Files.copy(sourceFile, newFile) === newFile)
    assert(Files.readAllLines(newFile).asScala.toSeq === Seq("abc"))
    assert(!Files.isSameFile(sourceFile, newFile))
    assertThrows[FileAlreadyExistsException] {
      Files.copy(sourceFile, newFile)
    }

    // Source is symbolic link
    val symbolicSource = Files.createSymbolicLink(
      root.resolve("symbolic"),
      sourceFile
    )
    val newFile2 = root.resolve("newFile2.txt")
    assert(Files.copy(symbolicSource, newFile2) === newFile2)
    assert(Files.readAllLines(newFile).asScala.toSeq === Seq("abc"))
    assert(!Files.isSameFile(symbolicSource, newFile2))

    // Fail if source not exists
    assertThrows[IOException] {
      Files.copy(root.resolve("not-exist"), root)
    }

    // Do nothing if same file
    Seq(sourceFile, root).foreach { path =>
      Files.copy(path, path)
    }
  }

  test("createDirectories(Path, FileAttribute[_]*)") {
    val tmpDir = Files.createTempDirectory("createDirectories")
    // No throw
    Files.createDirectories(tmpDir)

    val created = Files.createDirectories(tmpDir.resolve("sub"))
    assert(Files.exists(created))
    assert(Files.isDirectory(created))
    assert(created.getFileName.toString == "sub")

    val nestedPath = tmpDir.resolve("1").resolve("2").resolve("3")
    assert(Files.notExists(nestedPath))
    val createdDeep = Files.createDirectories(nestedPath)
    assert(Files.exists(createdDeep))
    assert(Files.isDirectory(createdDeep))
  }

  test("createDirectory(Path, FileAttribute[_]*)") {
    val tmpDir = Files.createTempDirectory("createDirectory")
    assertThrows[FileAlreadyExistsException] {
      Files.createDirectory(tmpDir)
    }

    val created = Files.createDirectory(tmpDir.resolve("sub"))
    assert(Files.exists(created))
    assert(Files.isDirectory(created))
    assert(created.getFileName.toString == "sub")

    val nestedPath = tmpDir.resolve("1").resolve("2").resolve("3")
    assert(Files.notExists(nestedPath))
    assertThrows[NoSuchFileException] {
      Files.createDirectory(nestedPath)
    }
  }

  test("createFile(Path, FileAttribute[_]*)") {
    val dir  = Files.createTempDirectory("createFile")
    val file = dir.resolve("foo.txt")
    assert(Files.notExists(file))
    assert(Files.createFile(file) === file)
    assert(Files.exists(file))
    assertThrows[FileAlreadyExistsException] {
      Files.createFile(file)
    }

    val nonExistSubDir = dir.resolve("sub")
    assertThrows[IOException] {
      Files.createFile(nonExistSubDir.resolve("bar.txt"))
    }
  }

  test("createLink(Path, Path)") {
    val baseDir = Files.createTempDirectory("tmp")

    val link1 = baseDir.resolve("link1")
    assert(Files.createLink(link1, regularText) === link1)
    assert(Files.exists(link1))
    assert(Files.isRegularFile(link1))
    assert(Files.isSymbolicLink(link1) === false)
    assertThrows[FileAlreadyExistsException] {
      Files.createLink(link1, regularText)
    }
    // TODO: check node number is equal

    // directory
    val link2 = baseDir.resolve("link2")
    assertThrows[IOException] {
      Files.createLink(link2, directorySource)
    }
    assert(Files.notExists(link2))
  }

  test("createSymbolicLink(Path, Path, FileAttribute[_])") {
    val sourceDir = Files.createTempDirectory("source")
    val targetDir = Files.createTempDirectory("source").resolve("tmp-symlink")
    val created   = Files.createSymbolicLink(targetDir, sourceDir)

    assert(Files.isSymbolicLink(created))
    assert(Files.exists(created))

    assertThrows[FileAlreadyExistsException] {
      Files.createSymbolicLink(sourceDir, targetDir)
    }
  }

  test("createTempDirectory(Path, String, FileAttribute[_])") {
    val base    = Files.createTempDirectory("more")
    val tempDir = Files.createTempDirectory(base, "foobar")
    assert("/more[^/]+/foobar[^/]+$".r.findFirstIn(tempDir.toString).isDefined)
    assert(Files.exists(tempDir))
    assert(Files.isDirectory(tempDir))

    // TODO: attrs
  }

  test("createTempDirectory(String, FileAttribute[_])") {
    val tempDir = Files.createTempDirectory("foobar")
    assert(tempDir.toString.contains("/foobar"))
    assert(Files.exists(tempDir))
    assert(Files.isDirectory(tempDir))

    // TODO: attrs
  }

  test("createTempFile(Path, String, String, FileAttribute[_])") {
    val base    = Files.createTempDirectory("more")
    val tmpFile = Files.createTempFile(base, "foobar", ".txt")
    assert("/more[^/]+/foobar[^/]+\\.txt$".r.findFirstIn(tmpFile.toString).isDefined)
    assert(Files.exists(tmpFile))
    assert(Files.isRegularFile(tmpFile))

    // TODO: attrs
  }

  test("createTempFile(String, String, FileAttribute[_])") {
    val tmpFile = Files.createTempFile("foobar", ".md")
    assert("/foobar[^/]+\\.md$".r.findFirstIn(tmpFile.toString).isDefined)
    assert(Files.exists(tmpFile))
    assert(Files.isRegularFile(tmpFile))

    // TODO: attrs
  }

  test("delete(Path)") {
    val tmpFile = Files.createTempFile("deleteme", ".txt")
    assert(Files.exists(tmpFile))
    Files.delete(tmpFile)
    assert(Files.notExists(tmpFile))
    assertThrows[NoSuchFileException] {
      Files.delete(tmpFile)
    }

    val emptyDir = Files.createTempDirectory("deleteme")
    assert(Files.exists(emptyDir))
    Files.delete(emptyDir)
    assert(Files.notExists(emptyDir))

    val nonEmptyDir = Files.createTempDirectory("nonEmpty")
    Files.createTempFile(nonEmptyDir, "foo", ".txt")
    assert(Files.exists(nonEmptyDir))
    assertThrows[DirectoryNotEmptyException] {
      Files.delete(nonEmptyDir)
    }
    assert(Files.exists(nonEmptyDir))

    val symbolicLink = Files.createSymbolicLink(
      Files.createTempDirectory("deleteme").resolve("sym"),
      Files.createTempDirectory("empty")
    )
    assert(Files.exists(symbolicLink))
    Files.delete(symbolicLink)
    assert(Files.notExists(symbolicLink))

    // todo: opened by other process
  }

  test("deleteIfExists(Path)") {
    val tmpDir = Files.createTempDirectory("d-i-e")

    // non-exist
    assert(Files.deleteIfExists(noSuchFile) === false)

    // file
    val file = Files.createFile(tmpDir.resolve("file.txt"))
    assert(Files.deleteIfExists(file))
    assert(Files.notExists(file))

    // empty directory
    val emptyDir = Files.createDirectory(tmpDir.resolve("empty-dir"))
    assert(Files.deleteIfExists(emptyDir))
    assert(Files.notExists(emptyDir))

    // non-empty dir
    assertThrows[DirectoryNotEmptyException] {
      Files.deleteIfExists(directorySource)
    }
    assert(Files.exists(directorySource))

    // symbolic file
    val file2        = Files.createFile(tmpDir.resolve("2.txt"))
    val symbolicFile = Files.createSymbolicLink(tmpDir.resolve("3.txt"), file2)
    assert(Files.deleteIfExists(symbolicFile))
    assert(Files.notExists(symbolicFile))

    // symbolic dir
    val dir1        = Files.createDirectory(tmpDir.resolve("sub1"))
    val symbolicDir = Files.createSymbolicLink(tmpDir.resolve("3sub2"), dir1)
    assert(Files.deleteIfExists(symbolicDir))
    assert(Files.notExists(symbolicFile))
  }

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

  ignore("find(Path, Int, BiPredicate[Path, BasicFileAttributes], FileVisitOption*)") {
    // Pending due to missing java.util.stream.Stream
  }

  ignore("getAttribute(Path, String, LinkOption*)") {
    // Node.js have no corresponding API
  }

  ignore("getFileAttributeView[V <: FileAttributeView](Path, Class[V], LinkOption*)") {
    // Node.js have no corresponding API
  }

  ignore("getFileStore(Path)") {
    // Node.js have no corresponding API
  }

  ignore("getOwner(Path, LinkOption*)") {
    // Node.js have no API to get user name associated with a uid
  }

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

  private val utf16leTxt = Paths.get("jdk/shared/src/test/resources/utf16be.txt")

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

    Seq(noSuchFile, noSuchSubDir).foreach { notExist =>
      assertThrows[NoSuchFileException] {
        Files.isSameFile(notExist, directory)
      }
      assertThrows[NoSuchFileException] {
        Files.isSameFile(directory, notExist)
      }
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
    val utf16le = utf16leCharset
    assert(Files.readAllLines(utf16leTxt, utf16le).asScala === Seq("\uFEFFUTF-16LE", "日本語"))
    assert(Files.readAllLines(fileInSymlink, utf16le).asScala !== Seq("foo", "bar", "buz"))
    assertThrows[IOException] {
      Files.readAllLines(directory, utf16le)
    }
    assertThrows[IOException] {
      Files.readAllLines(noSuchFile, utf16le)
    }
  }

  test("readAttributes[A <: BasicFileAttributes](Path, Class[A], LinkOption*)") {
    // directory and symlink without NOFOLLOW_LINK
    Seq(directorySource, directorySymlink).foreach { dir =>
      val dirAttr: BasicFileAttributes = Files.readAttributes(dir, classOf[BasicFileAttributes])
      assert(dirAttr.isDirectory)
      assert(!dirAttr.isOther)
      assert(!dirAttr.isRegularFile)
      assert(!dirAttr.isSymbolicLink)
      assert(dirAttr.size() > 0L)
      assert(dirAttr.fileKey() !== null)
      assert(dirAttr.creationTime().toMillis > 0L)
      assert(dirAttr.lastAccessTime().toMillis > 0L)
      assert(dirAttr.lastModifiedTime().toMillis > 0L)
      assert(dirAttr.lastAccessTime().compareTo(dirAttr.creationTime()) > 0)
      assert(dirAttr.lastModifiedTime().compareTo(dirAttr.creationTime()) > 0)
    }

    // symbolic link with NOFOLLOW_LINK
    val symAttr: BasicFileAttributes = Files.readAttributes(
      directorySymlink,
      classOf[BasicFileAttributes],
      LinkOption.NOFOLLOW_LINKS
    )
    assert(!symAttr.isDirectory)
    assert(!symAttr.isOther)
    assert(!symAttr.isRegularFile)
    assert(symAttr.isSymbolicLink)
    assert(symAttr.size() > 0L)
    assert(symAttr.fileKey() !== null)
    assert(symAttr.creationTime().toMillis > 0L)
    assert(symAttr.lastAccessTime().toMillis > 0L)
    assert(symAttr.lastModifiedTime().toMillis > 0L)
    assert(symAttr.lastAccessTime().compareTo(symAttr.creationTime()) === 0)
    assert(symAttr.lastModifiedTime().compareTo(symAttr.creationTime()) === 0)

    // files
    Seq(fileInSource, fileInHidden, fileInSymlink).foreach { file =>
      Seq(Seq.empty[LinkOption], Seq(LinkOption.NOFOLLOW_LINKS)).foreach { options =>
        val fileAttr: BasicFileAttributes =
          Files.readAttributes(file, classOf[BasicFileAttributes], options: _*)
        assert(!fileAttr.isDirectory)
        assert(!fileAttr.isOther)
        assert(fileAttr.isRegularFile)
        assert(!fileAttr.isSymbolicLink)
        assert(fileAttr.size() > 0L)
        assert(fileAttr.fileKey() !== null)
        assert(fileAttr.creationTime().toMillis > 0L)
        assert(fileAttr.lastAccessTime().toMillis > 0L)
        assert(fileAttr.lastModifiedTime().toMillis > 0L)
        assert(fileAttr.lastAccessTime().compareTo(fileAttr.creationTime()) > 0)
        assert(fileAttr.lastModifiedTime().compareTo(fileAttr.creationTime()) > 0)
      }
    }

    // non-exist
    assertThrows[IOException] {
      Files.readAttributes(noSuchFile, classOf[BasicFileAttributes])
    }
    assertThrows[IOException] {
      Files.readAttributes(deletedSymlinkFile, classOf[BasicFileAttributes])
    }
    assertThrows[IOException] {
      Files.readAttributes(fileInDeletedSymlink, classOf[BasicFileAttributes])
    }
  }

  ignore("readAttributes[A <: BasicFileAttributes](Path, String, LinkOption*)") {}

  ignore("readSymbolicLink(Path)") {}

  ignore("setAttribute(Path, String, AnyRef, LinkOption*)") {
    // Node.js have no API to get user name associated with a uid
  }

  ignore("setLastModifiedTime(Path, FileTime)") {}

  ignore("setOwner(Path, UserPrincipal)") {
    // Node.js have no API to get user name associated with a uid
  }

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

  test("write(Path, Array[Byte], OpenOption*)") {
    val tmpFile = Files.createTempFile("foo", ".txt")
    assert(Files.size(tmpFile) === 0)
    val written = Files.write(tmpFile, Array[Byte](97, 98, 99))
    assert(written === tmpFile)
    assert(Files.readAllLines(written).asScala.toSeq === Seq("abc"))
    Files.write(tmpFile, Array[Byte](100, 101, 102))
    assert(Files.size(tmpFile) === 3)
    // Overwrite, not append
    assert(Files.readAllLines(written).asScala.toSeq === Seq("def"))

    Files.delete(tmpFile)
    // Create if not exists
    Files.write(tmpFile, Array[Byte](97, 98, 99))

    assertThrows[IOException] {
      Files.write(directory, Array[Byte](97, 98, 99))
    }
  }
  test("write(Path, JavaIterable[_ <: CharSequence], Charset, OpenOption*)") {
    val tmpFile = Files.createTempFile("foo", ".txt")
    assert(Files.size(tmpFile) === 0)

    val utf16le = utf16leCharset
    val written = Files.write(tmpFile, Seq("abc").asJava, utf16le)
    assert(written === tmpFile)
    assert(Files.readAllLines(written, utf16le).asScala.toSeq === Seq("abc"))
    assert(Files.size(tmpFile) === 8)
  }
  test("write(Path, JavaIterable[_ <: CharSequence], OpenOption*)") {
    val tmpFile = Files.createTempFile("foo", ".txt")
    assert(Files.size(tmpFile) === 0)
    val written = Files.write(tmpFile, Seq("abc").asJava)
    assert(written === tmpFile)
    assert(Files.readAllLines(written).asScala.toSeq === Seq("abc"))
    assert(Files.size(tmpFile) === 4)
    Files.write(tmpFile, Seq("a", "b", "c").asJava)
    assert(Files.size(tmpFile) === 6)
    // Overwrite, not append
    assert(Files.readAllLines(written).asScala.toSeq === Seq("a", "b", "c"))

    Files.delete(tmpFile)
    // Create if not exists
    Files.write(tmpFile, Seq("abc").asJava)

    assertThrows[IOException] {
      Files.write(directory, Seq("abc").asJava)
    }
  }
}
