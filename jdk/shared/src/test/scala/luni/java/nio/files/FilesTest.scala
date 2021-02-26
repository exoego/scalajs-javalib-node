package luni.java.nio.files

import org.scalatest.freespec.AnyFreeSpec
import support.TestSupport

import java.io._
import java.nio.charset.Charset
import java.nio.file._
import java.nio.file.attribute._
import java.util.{Set => JavaSet}
import java.util.concurrent.TimeUnit._
import scala.jdk.CollectionConverters._
import scala.collection.mutable.ListBuffer

class FilesTest extends AnyFreeSpec with TestSupport {

  private def using[T <: AutoCloseable](resource: T)(block: T => Unit): Unit = {
    try {
      block(resource)
    } finally {
      resource.close()
    }
  }
  private val utf16leCharset: Charset = Charset.forName("UTF-16LE")

  private val directory        = Paths.get("project")
  private val resourceRoot     = Paths.get("jdk/shared/src/test/resources")
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

  "copy(InputStream, Path, CopyOption*)" in {
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

  "copy(Path, OutputStream)" in {
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

  "copy(Path, Path, CopyOption*)" in {
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

  "createDirectories(Path, FileAttribute[_]*)" in {
    val tmpDir = Files.createTempDirectory("createDirectories")
    // No throw
    Files.createDirectories(tmpDir)

    val created = Files.createDirectories(tmpDir.resolve("sub"))
    assert(Files.getPosixFilePermissions(created) === PosixFilePermissions.fromString("rwxr-xr-x"))
    assert(Files.exists(created))
    assert(Files.isDirectory(created))
    assert(created.getFileName.toString === "sub")

    val nestedPath = tmpDir.resolve("1").resolve("2").resolve("3")
    assert(Files.notExists(nestedPath))
    val createdDeep = Files.createDirectories(nestedPath)
    assert(Files.exists(createdDeep))
    assert(Files.isDirectory(createdDeep))
  }

  "createDirectory(Path, FileAttribute[_]*)" in {
    val tmpDir = Files.createTempDirectory("createDirectory")
    assertThrows[FileAlreadyExistsException] {
      Files.createDirectory(tmpDir)
    }

    val created = Files.createDirectory(tmpDir.resolve("sub"))
    assert(Files.getPosixFilePermissions(created) === PosixFilePermissions.fromString("rwxr-xr-x"))
    assert(Files.exists(created))
    assert(Files.isDirectory(created))
    assert(created.getFileName.toString === "sub")

    val nestedPath = tmpDir.resolve("1").resolve("2").resolve("3")
    assert(Files.notExists(nestedPath))
    assertThrows[NoSuchFileException] {
      Files.createDirectory(nestedPath)
    }
  }

  "createFile(Path, FileAttribute[_]*)" in {
    val dir  = Files.createTempDirectory("createFile")
    val file = dir.resolve("foo.txt")
    assert(Files.notExists(file))
    assert(Files.createFile(file) === file)
    assert(Files.getPosixFilePermissions(file) === PosixFilePermissions.fromString("rw-r--r--"))
    assert(Files.exists(file))
    assertThrows[FileAlreadyExistsException] {
      Files.createFile(file)
    }

    val nonExistSubDir = dir.resolve("sub")
    assertThrows[IOException] {
      Files.createFile(nonExistSubDir.resolve("bar.txt"))
    }
  }

  "createLink(Path, Path)" in {
    val baseDir = Files.createTempDirectory("tmp")

    val link1 = baseDir.resolve("link1")
    assert(Files.createLink(link1, regularText) === link1)
    assert(Files.getPosixFilePermissions(link1) === PosixFilePermissions.fromString("rw-r--r--"))
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

  "createSymbolicLink(Path, Path, FileAttribute[_])" in {
    val sourceDir = Files.createTempDirectory("source")
    val targetDir = Files.createTempDirectory("source").resolve("tmp-symlink")
    val created   = Files.createSymbolicLink(targetDir, sourceDir)
    assert(Files.getPosixFilePermissions(created) === PosixFilePermissions.fromString("rwx------"))

    assert(Files.isSymbolicLink(created))
    assert(Files.exists(created))

    assertThrows[FileAlreadyExistsException] {
      Files.createSymbolicLink(sourceDir, targetDir)
    }
  }

  "createTempDirectory(Path, String, FileAttribute[_])" in {
    val base    = Files.createTempDirectory("more")
    val tempDir = Files.createTempDirectory(base, "foobar")
    assert("/more[^/]+/foobar[^/]+$".r.findFirstIn(tempDir.toString).isDefined)
    assert(Files.exists(tempDir))
    assert(Files.isDirectory(tempDir))
    assert(Files.getPosixFilePermissions(tempDir) === PosixFilePermissions.fromString("rwx------"))

    // TODO: attrs
  }

  "createTempDirectory(String, FileAttribute[_])" in {
    val tempDir = Files.createTempDirectory("foobar")
    assert(tempDir.toString.contains("/foobar"))
    assert(Files.exists(tempDir))
    assert(Files.isDirectory(tempDir))
    assert(Files.getPosixFilePermissions(tempDir) === PosixFilePermissions.fromString("rwx------"))

    // TODO: attrs
  }

  "createTempFile(Path, String, String, FileAttribute[_])" in {
    val base    = Files.createTempDirectory("more")
    val tmpFile = Files.createTempFile(base, "foobar", ".txt")
    assert("/more[^/]+/foobar[^/]+\\.txt$".r.findFirstIn(tmpFile.toString).isDefined)
    assert(Files.exists(tmpFile))
    assert(Files.isRegularFile(tmpFile))
    assert(Files.getPosixFilePermissions(tmpFile) === PosixFilePermissions.fromString("rw-------"))

    val tmpFile2 = Files.createTempFile(
      base,
      "foobar",
      ".md",
      PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxrwxrwx"))
    )
    assert(Files.exists(tmpFile2))
    assert(Files.isRegularFile(tmpFile2))
    assert(Files.getPosixFilePermissions(tmpFile2) === PosixFilePermissions.fromString("rwxr-xr-x"))
  }

  "createTempFile(String, String, FileAttribute[_])" in {
    val tmpFile = Files.createTempFile("foobar", ".md")
    assert("/foobar[^/]+\\.md$".r.findFirstIn(tmpFile.toString).isDefined)
    assert(Files.exists(tmpFile))
    assert(Files.isRegularFile(tmpFile))
    assert(Files.getPosixFilePermissions(tmpFile) === PosixFilePermissions.fromString("rw-------"))

    val tmpFile2 = Files.createTempFile(
      "foobar",
      ".md",
      PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxrwxrwx"))
    )
    assert(Files.exists(tmpFile2))
    assert(Files.isRegularFile(tmpFile2))
    assert(Files.getPosixFilePermissions(tmpFile2) === PosixFilePermissions.fromString("rwxr-xr-x"))
  }

  "delete(Path)" in {
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

  "deleteIfExists(Path)" in {
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

  "exists(Path, LinkOption*)" in {
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

  "find(Path, Int, BiPredicate[Path, BasicFileAttributes], FileVisitOption*)" ignore {
    // Pending due to missing java.util.stream.Stream
  }

  "getAttribute(Path, String, LinkOption*)" - {
    val file = Files.createTempFile("file", ".txt")
    Files.setAttribute(file, "creationTime", FileTime.from(100, DAYS))
    Files.setAttribute(file, "lastModifiedTime", FileTime.from(200, DAYS))
    Files.setAttribute(file, "lastAccessTime", FileTime.from(300, DAYS))

    "supported attribute" in {
      Seq("", "basic:", "posix:").foreach { prefix =>
        assert(Files.getAttribute(file, s"${prefix}creationTime") !== FileTime.from(100, DAYS))
        assert(Files.getAttribute(file, s"${prefix}creationTime") !== FileTime.from(100, DAYS))
        assert(Files.getAttribute(file, s"${prefix}lastModifiedTime") === FileTime.from(200, DAYS))
        assert(Files.getAttribute(file, s"${prefix}lastAccessTime") === FileTime.from(300, DAYS))
        assert(Files.getAttribute(file, s"${prefix}size") === 0)
        assert(Files.getAttribute(file, s"${prefix}isRegularFile") === true)
        assert(Files.getAttribute(file, s"${prefix}isDirectory") === false)
        assert(Files.getAttribute(file, s"${prefix}isOther") === false)
        assert(Files.getAttribute(file, s"${prefix}isSymbolicLink") === false)
        assert(Files.getAttribute(file, s"${prefix}fileKey") !== null)
      }
    }

    "invalid attribute" in {
      assertThrows[UnsupportedOperationException] {
        Files.getAttribute(file, "unknown:lastModifiedTime")
      }
      assertThrows[IllegalArgumentException] {
        Files.getAttribute(file, "basic:typo")
      }
    }
    // todo: linkoption
  }

  "getFileAttributeView[V <: FileAttributeView](Path, Class[V], LinkOption*)" in {
    val tmpDir = Files.createTempFile("tmp", ".txt")

    val view: PosixFileAttributeView =
      Files.getFileAttributeView(tmpDir, classOf[PosixFileAttributeView])
    assert(view.name() === "posix")

    val oldAttr = view.readAttributes()
    assert(oldAttr.permissions() === PosixFilePermissions.fromString("rw-------"))

    // oldAttr never affected by setter
    view.setPermissions(PosixFilePermissions.fromString("rwxr-xr-x"))
    assert(oldAttr.permissions() === PosixFilePermissions.fromString("rw-------"))

    val newAttr = view.readAttributes()
    assert(newAttr.permissions() === PosixFilePermissions.fromString("rwxr-xr-x"))

    val noSuchFileView = Files.getFileAttributeView(noSuchFile, classOf[PosixFileAttributeView])
    assert(noSuchFileView.name() === "posix")
    assertThrows[IOException] {
      noSuchFileView.readAttributes()
    }

    // creation time can not be changed on JDK
    view.setTimes(null, null, FileTime.from(1, DAYS))
    assert(oldAttr.creationTime() !== FileTime.from(1, DAYS))
    assert(view.readAttributes().lastModifiedTime() !== FileTime.from(3, DAYS))
    assert(view.readAttributes().lastAccessTime() !== FileTime.from(2, DAYS))
    assert(view.readAttributes().creationTime() !== FileTime.from(1, DAYS))

    view.setTimes(null, FileTime.from(2, DAYS), null)
    assert(oldAttr.lastAccessTime() !== FileTime.from(2, DAYS))
    assert(view.readAttributes().lastModifiedTime() !== FileTime.from(3, DAYS))
    assert(view.readAttributes().lastAccessTime() === FileTime.from(2, DAYS))
    assert(view.readAttributes().creationTime() !== FileTime.from(1, DAYS))

    view.setTimes(FileTime.from(3, DAYS), null, null)
    assert(oldAttr.lastModifiedTime() !== FileTime.from(3, DAYS))
    assert(view.readAttributes().lastModifiedTime() === FileTime.from(3, DAYS))
    assert(view.readAttributes().lastAccessTime() === FileTime.from(2, DAYS))
    assert(view.readAttributes().creationTime() !== FileTime.from(1, DAYS))

    view.setTimes(FileTime.from(4, DAYS), FileTime.from(5, DAYS), FileTime.from(6, DAYS))
    assert(oldAttr.lastModifiedTime() !== FileTime.from(4, DAYS))
    assert(view.readAttributes().lastModifiedTime() === FileTime.from(4, DAYS))
    assert(view.readAttributes().lastAccessTime() === FileTime.from(5, DAYS))
    assert(view.readAttributes().creationTime() !== FileTime.from(6, DAYS))

    // TODO: node.js can not support these
    // view.getOwner()
    // newAttr.group()
    // newAttr.owner()

    // todo: linkoption
  }

  "getFileStore(Path)" ignore {
    // Node.js have no corresponding API
  }

  "getOwner(Path, LinkOption*)" ignore {
    // Node.js have no API to get user name associated with a uid
  }

  "getPosixFilePermissions(Path, LinkOption*)" in {
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

  "isDirectory(Path)" in {
    assert(Files.isDirectory(directory))
    assert(Files.isDirectory(subDirectory))
    assert(Files.isDirectory(directorySource))
    assert(Files.isDirectory(directorySymlink))

    assert(!Files.isDirectory(fileInSource))
    assert(!Files.isDirectory(noSuchFileInDir))
    assert(!Files.isDirectory(noSuchSubDir))
  }

  "isDirectory(Path, LinkOption*)" in {
    val option = LinkOption.NOFOLLOW_LINKS
    assert(Files.isDirectory(directory, option))
    assert(Files.isDirectory(directorySource, option))
    assert(!Files.isDirectory(directorySymlink, option))

    assert(!Files.isDirectory(regularText, option))
  }

  "isExecutable(Path)" in {
    assert(!Files.isExecutable(Paths.get("build.sbt")))
    assert(!Files.isExecutable(noSuchFile))
    assert(Files.isExecutable(rwxrwxrwx))
    assert(!Files.isExecutable(rrr))

    // directory is executable
    assert(Files.isExecutable(directory))
    assert(Files.isExecutable(Paths.get("jdk/shared")))
  }

  "isHidden(Path)" in {
    assert(!Files.isHidden(rwxrwxrwx))
    assert(Files.isHidden(hiddenDirectory))
    assert(!Files.isHidden(fileInHidden))
  }

  "isReadable(Path)" in {
    assert(Files.isReadable(regularText))
    assert(!Files.isReadable(noSuchFile))

    assert(Files.isReadable(regularText))
    assert(!Files.isReadable(noSuchFileInDir))
  }

  "isRegularFile(Path, LinkOption*)" in {
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

  "isSameFile(Path, Path)" in {
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

  "isSymbolicLink(Path)" in {
    assert(!Files.isSymbolicLink(directorySource))
    assert(!Files.isSymbolicLink(fileInSource))
    assert(Files.isSymbolicLink(directorySymlink))
    assert(Files.isSymbolicLink(deletedSymlinkFile))
    assert(!Files.isSymbolicLink(fileInSymlink))
    assert(!Files.isSymbolicLink(noSuchFile))
  }

  "isWritable(Path)" in {
    assert(Files.isWritable(directory))
    assert(Files.isWritable(fileInSource))
    assert(Files.isWritable(fileInHidden))
    assert(Files.isWritable(fileInSymlink))
    assert(!Files.isWritable(noSuchFile))
    assert(!Files.isWritable(rrr))
  }

  "lines(Path)" ignore {}

  "lines(Path, Charset)" ignore {}

  "list(Path)" ignore {}

  "move(Path, Path, CopyOption*)" - {
    "same existing path do nothing" in {
      Seq(fileInSource, directory).foreach { path =>
        assert(Files.move(path, path) === path)
        assert(Files.move(path, path, StandardCopyOption.ATOMIC_MOVE) === path)
        assert(Files.move(path, path, StandardCopyOption.REPLACE_EXISTING) === path)

        assertThrows[UnsupportedOperationException] {
          Files.move(path, path, StandardCopyOption.COPY_ATTRIBUTES)
        }
      }
    }

    "same no existing path throws IO" in {
      Seq(noSuchFile, noSuchFileInDir).foreach { path =>
        assertThrows[IOException] {
          Files.move(path, path)
        }
        assertThrows[IOException] {
          Files.move(path, path, StandardCopyOption.ATOMIC_MOVE)
        }
        assertThrows[IOException] {
          Files.move(path, path, StandardCopyOption.REPLACE_EXISTING)
        }
        assertThrows[UnsupportedOperationException] {
          Files.move(path, path, StandardCopyOption.COPY_ATTRIBUTES)
        }
      }
    }

    "When target does not exist" - {
      "source is a file" - {
        "moved, time preserved" in {
          val tmpDir   = Files.createTempDirectory("1234")
          val src      = Files.createFile(tmpDir.resolve("src.txt"))
          val srcAttrs = Files.readAttributes(src, classOf[BasicFileAttributes])

          val target = tmpDir.resolve("target.txt")
          assert(Files.move(src, target) === target)
          val targetAttrs = Files.readAttributes(target, classOf[BasicFileAttributes])
          assert(srcAttrs.creationTime() === targetAttrs.creationTime())
          assert(srcAttrs.lastAccessTime() === targetAttrs.lastAccessTime())
          assert(srcAttrs.lastModifiedTime() === targetAttrs.lastModifiedTime())
        }
      }

      "source is a directory" - {
        "moved, time preserved" in {
          val tmpDir   = Files.createTempDirectory("1234")
          val src      = Files.createDirectory(tmpDir.resolve("srcDir"))
          val srcAttrs = Files.readAttributes(src, classOf[BasicFileAttributes])
          val target   = tmpDir.resolve("target")

          assert(Files.move(src, target) === target)
          val targetAttrs = Files.readAttributes(target, classOf[BasicFileAttributes])
          assert(srcAttrs.creationTime() === targetAttrs.creationTime())
          assert(srcAttrs.lastAccessTime() === targetAttrs.lastAccessTime())
          assert(srcAttrs.lastModifiedTime() === targetAttrs.lastModifiedTime())
        }
      }
    }

    "When target exists" - {
      "and source is a file" - {
        "throws if NO REPLACE_EXISTING" in {
          val src    = Files.createTempFile("source", ".txt")
          val target = Files.createTempFile("target", ".txt")
          assertThrows[FileAlreadyExistsException] {
            Files.move(src, target)
          }
        }

        "replaces existing file if REPLACE_EXISTING" in {
          val src = Files.createTempFile("source", ".txt")
          Files.write(src, Array[Byte](1, 2, 3))
          val target = Files.createTempFile("target", ".txt")
          assert(Files.size(src) === 3)
          assert(Files.size(target) === 0)
          assert(Files.move(src, target, StandardCopyOption.REPLACE_EXISTING) === target)
          assert(Files.notExists(src))
          assert(Files.size(target) === 3)
        }

        "target is an empty directory" in {
          val src    = Files.createTempFile("source", ".txt")
          val target = Files.createTempDirectory("target")
          assert(Files.exists(src))
          assert(Files.move(src, target, StandardCopyOption.REPLACE_EXISTING) === target)
          assert(Files.notExists(src))
          assert(Files.isRegularFile(target))
        }

        "target is a non-empty dir" in {
          val src    = Files.createTempFile("source", ".txt")
          val target = Files.createTempDirectory("target")
          Files.createFile(target.resolve("file.txt"))

          assertThrows[DirectoryNotEmptyException] {
            Files.move(src, target, StandardCopyOption.REPLACE_EXISTING)
          }
        }
      }

      "and source is directory" - {
        "throws if NO REPLACE_EXISTING" in {
          val src    = Files.createTempDirectory("source")
          val target = Files.createTempDirectory("target")
          assertThrows[FileAlreadyExistsException] {
            Files.move(src, target)
          }
        }

        "replaces existing if REPLACE_EXISTING" in {
          val src  = Files.createTempDirectory("source")
          val file = Files.createTempFile(src, "foo", ".txt")

          val target = Files.createTempDirectory("target")
          assert(Files.exists(src))
          assert(Files.notExists(target.resolve(file.getFileName)))
          assert(Files.move(src, target, StandardCopyOption.REPLACE_EXISTING) === target)
          assert(Files.notExists(src))
          assert(Files.exists(target.resolve(file.getFileName)))
        }

        "target is a non-empty dir" in {
          val src    = Files.createTempDirectory("source")
          val target = Files.createTempDirectory("sotargeturce")
          Files.createFile(target.resolve("file.txt"))

          assertThrows[DirectoryNotEmptyException] {
            Files.move(src, target, StandardCopyOption.REPLACE_EXISTING)
          }
        }

        "target is an existing file" in {
          val src    = Files.createTempDirectory("source")
          val target = Files.createTempFile("target", ".txt")
          assert(Files.exists(src))
          assert(Files.move(src, target, StandardCopyOption.REPLACE_EXISTING) === target)
          assert(Files.notExists(src))
          assert(Files.isDirectory(target))
        }
      }
    }
  }

  "newBufferedReader(Path)" ignore {}

  "newBufferedReader(Path, Charset)" ignore {}

  "newBufferedWriter(Path, Charset, OpenOption*)" ignore {}

  "newBufferedWriter(Path, OpenOption*)" ignore {}

  "newByteChannel(Path, OpenOption*)" ignore {}

  "newByteChannel(Path, JavaSet[_ <: OpenOption], FileAttribute[_]*)" ignore {}

  "newDirectoryStream(Path)" ignore {}

  "newDirectoryStream(Path, DirectoryStream.Filter[_ >: Path])" ignore {}

  "newDirectoryStream(Path, String)" ignore {}

  "newInputStream(Path, OpenOption*)" ignore {}

  "newOutputStream(Path, OpenOption*)" ignore {}

  "notExists(Path, LinkOption*)" in {
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

  "probeContentType(Path)" in {
    assume(isJDK11AndLater, "JDK8 returns null on mac. Node.js does not have corresponding API .")
    assert(Files.probeContentType(noSuchSubDir) === null)
    assert(Files.probeContentType(noSuchFile) === null)
    assert(Files.probeContentType(directory) === null)
    assert(Files.probeContentType(fileInHidden) === "text/plain")
    assert(Files.probeContentType(fileInSource) === "text/plain")
  }

  "readAllBytes(Path)" in {
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

  "readAllLines(Path)" in {
    assert(Files.readAllLines(fileInSource).asScala === Seq("foo", "bar", "日本語"))
    assert(Files.readAllLines(fileInSymlink).asScala === Seq("foo", "bar", "日本語"))
    assertThrows[IOException] {
      Files.readAllLines(directory)
    }
    assertThrows[IOException] {
      Files.readAllLines(noSuchFile)
    }
  }

  "readAllLines(Path, Charset)" in {
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

  "readAttributes[A <: BasicFileAttributes](Path, Class[A], LinkOption*)" in {
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

    if (isJDK11AndLater) {
      assert(symAttr.lastAccessTime().compareTo(symAttr.creationTime()) > 0L)
      assert(symAttr.lastModifiedTime().compareTo(symAttr.creationTime()) > 0L)
    } else {
      // TODO: Node.js impl should support nano-seconds comparison, using BigIntStats
      assert(symAttr.lastAccessTime().compareTo(symAttr.creationTime()) === 0L)
      assert(symAttr.lastModifiedTime().compareTo(symAttr.creationTime()) === 0L)
    }

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
    Seq(noSuchFile, noSuchFile, fileInDeletedSymlink).foreach { nonExist =>
      assertThrows[IOException] {
        Files.readAttributes(nonExist, classOf[BasicFileAttributes])
      }
    }
  }

  "readAttributes(Path, String, LinkOption*)" in {
    // unavailabel attrs
    assertThrows[IllegalArgumentException] {
      Files.readAttributes(directory, "").asScala
    }
    assertThrows[IllegalArgumentException] {
      Files.readAttributes(directory, "basic:").asScala
    }
    assertThrows[IllegalArgumentException] {
      Files.readAttributes(directory, "no-such-attribute").asScala
    }
    assertThrows[IllegalArgumentException] {
      Files.readAttributes(directory, "*,no-such-attribute").asScala
    }

    // unsupported type
    assertThrows[UnsupportedOperationException] {
      Files.readAttributes(directory, "unknowntype:size").asScala
    }

    // directory and symlink without NOFOLLOW_LINK
    Seq(directorySource, directorySymlink).foreach { dir =>
      Seq("*", "*,*", "*,size", "basic:*").foreach { attrs =>
        val dirAttr = Files.readAttributes(dir, attrs).asScala
        assert(dirAttr("isDirectory") === true)
        assert(dirAttr("isOther") === false)
        assert(dirAttr("isRegularFile") === false)
        assert(dirAttr("isSymbolicLink") === false)
        assert(dirAttr("size").asInstanceOf[Long] > 0L)
        assert(dirAttr("fileKey") !== null)
        assert(dirAttr("creationTime").asInstanceOf[FileTime].toMillis > 0L)
        assert(dirAttr("lastAccessTime").asInstanceOf[FileTime].toMillis > 0L)
        assert(dirAttr("lastModifiedTime").asInstanceOf[FileTime].toMillis > 0L)
      }

      Seq("isDirectory", "basic:isDirectory").foreach { attrs =>
        val dirAttr = Files.readAttributes(dir, attrs).asScala
        assert(dirAttr("isDirectory") === true)
        assert(dirAttr.size === 1)
      }

      Seq("creationTime,size", "basic:size,creationTime", "basic:size,creationTime").foreach {
        attrs =>
          val dirAttr = Files.readAttributes(dir, attrs).asScala
          assert(dirAttr("size").asInstanceOf[Long] > 0L)
          assert(dirAttr("creationTime").asInstanceOf[FileTime].toMillis > 0L)
          assert(dirAttr.size === 2)
      }
    }

    // symbolic link with NOFOLLOW_LINK
    val symAttr = Files
      .readAttributes(
        directorySymlink,
        "*",
        LinkOption.NOFOLLOW_LINKS
      )
      .asScala
    assert(symAttr("isDirectory") === false)

    // non-exist
    Seq(noSuchFile, noSuchFile, fileInDeletedSymlink).foreach { nonExist =>
      assertThrows[IOException] {
        Files.readAttributes(nonExist, "*")
      }
    }
  }

  "readSymbolicLink(Path)" in {
    val read = Files.readSymbolicLink(directorySymlink)
    assert(read === directorySource.getFileName)
    assert(read.getParent === null)

    assertThrows[NotLinkException] {
      Files.readSymbolicLink(directorySource)
    }
    assertThrows[NotLinkException] {
      Files.readSymbolicLink(symlinkText)
    }
    assertThrows[NotLinkException] {
      Files.readSymbolicLink(fileInSource)
    }
    assertThrows[NotLinkException] {
      Files.readSymbolicLink(fileInSymlink)
    }

    assertThrows[IOException] {
      Files.readSymbolicLink(noSuchSubDir)
    }
  }

  "setAttribute(Path, String, AnyRef, LinkOption*)" - {
    val file = Files.createTempFile("file", ".txt")

    "supported attribute" in {
      Files.setAttribute(file, "lastModifiedTime", FileTime.from(11, DAYS))
      assert(Files.getLastModifiedTime(file) === FileTime.from(11, DAYS))
      Files.setAttribute(file, "posix:lastModifiedTime", FileTime.from(22, DAYS))
      assert(Files.getLastModifiedTime(file) === FileTime.from(22, DAYS))
      Files.setAttribute(file, "basic:lastModifiedTime", FileTime.from(33, DAYS))
      assert(Files.getLastModifiedTime(file) === FileTime.from(33, DAYS))

      Files.setAttribute(file, "lastAccessTime", FileTime.from(11, DAYS))
      assert(Files.getAttribute(file, "lastAccessTime") === FileTime.from(11, DAYS))
      Files.setAttribute(file, "posix:lastAccessTime", FileTime.from(22, DAYS))
      assert(Files.getAttribute(file, "posix:lastAccessTime") === FileTime.from(22, DAYS))
      Files.setAttribute(file, "basic:lastAccessTime", FileTime.from(33, DAYS))
      assert(Files.getAttribute(file, "basic:lastAccessTime") === FileTime.from(33, DAYS))
    }

    "creationTime is recognized, but no effect" in {
      val file = Files.createTempFile("file", ".txt")

      assert(Files.getAttribute(file, "creationTime") !== FileTime.from(11, DAYS))
      Files.setAttribute(file, "posix:creationTime", FileTime.from(22, DAYS))
      assert(Files.getAttribute(file, "posix:creationTime") !== FileTime.from(22, DAYS))
      Files.setAttribute(file, "basic:creationTime", FileTime.from(33, DAYS))
      assert(Files.getAttribute(file, "basic:creationTime") !== FileTime.from(33, DAYS))
    }

    "unsupported attributes" in {
      assertThrows[IllegalArgumentException] {
        Files.setAttribute(file, "size", true)
      }
      assertThrows[IllegalArgumentException] {
        Files.setAttribute(file, "isDirectory", true)
      }
      assertThrows[IllegalArgumentException] {
        Files.setAttribute(file, "isOther", true)
      }
      assertThrows[IllegalArgumentException] {
        Files.setAttribute(file, "isRegularFile", true)
      }
      assertThrows[IllegalArgumentException] {
        Files.setAttribute(file, "isSymbolicLink", true)
      }
      assertThrows[IllegalArgumentException] {
        Files.setAttribute(file, "fileKey", "fobar")
      }
    }

    "invalid attribute" in {
      assertThrows[UnsupportedOperationException] {
        Files.setAttribute(file, "unknown:lastModifiedTime", FileTime.from(999, DAYS))
      }
      assertThrows[IllegalArgumentException] {
        Files.setAttribute(file, "basic:typo", FileTime.from(999, DAYS))
      }
    }

    "invalid value" in {
      assertThrows[ClassCastException] {
        Files.setAttribute(file, "basic:lastAccessTime", "invalid value")
      }
    }
    // todo: linkoption
  }

  "setLastModifiedTime(Path, FileTime)" in {
    val file = Files.createTempFile("lastmodified", ".txt")

    assert(Files.setLastModifiedTime(file, FileTime.from(100, DAYS)) === file)
    assert(Files.getLastModifiedTime(file) === FileTime.from(100, DAYS))

    assert(Files.setLastModifiedTime(file, FileTime.from(200, DAYS)) === file)
    assert(Files.getLastModifiedTime(file) === FileTime.from(200, DAYS))

    assertThrows[NullPointerException] {
      Files.setLastModifiedTime(file, null)
    }
    assertThrows[IOException] {
      Files.setLastModifiedTime(noSuchFile, FileTime.from(200, DAYS))
    }
  }

  "setOwner(Path, UserPrincipal)" ignore {
    // Node.js have no API to get user name associated with a uid
  }

  "setPosixFilePermissions(Path, JavaSet[PosixFilePermission])" in {
    val file1 = Files.createTempFile("foo", ".txt")
    assert(
      Files.setPosixFilePermissions(file1, PosixFilePermissions.fromString("rwxrwxrwx")) === file1
    )
    assert(Files.getPosixFilePermissions(file1) === PosixFilePermissions.fromString("rwxrwxrwx"))

    val file2 = Files.createFile(Files.createTempDirectory("dir").resolve("file"))
    assert(
      Files.setPosixFilePermissions(file2, PosixFilePermissions.fromString("rwxrwxrwx")) === file2
    )
    assert(Files.getPosixFilePermissions(file2) === PosixFilePermissions.fromString("rwxrwxrwx"))

    assertThrows[IOException] {
      Files.setPosixFilePermissions(noSuchFile, PosixFilePermissions.fromString("rwxrwxrwx"))
    }
  }

  "size(Path)" in {
    assert(Files.size(directorySource) === 96)
    assert(Files.size(fileInSource) === 18)
    assert(Files.size(directorySymlink) === 96)

    assertThrows[IOException] {
      Files.size(noSuchFile)
    }
  }

  "walk(Path, FileVisitOption*)" ignore {}
  "walk(Path, Int, FileVisitOption*)" ignore {}

  "walkFileTree(Path, FileVisitor[_ >: Path])" in {
    val fileCollector = new FileCollector()
    assert(Files.walkFileTree(fileInSource, fileCollector) === fileInSource)
    assert(fileCollector.collectFiles() === Seq(fileInSource))

    val fileCollector2 = new FileCollector()
    assert(Files.walkFileTree(rrr.getParent, fileCollector2) === rrr.getParent)
    assert(fileCollector2.collectFiles() === Seq(rrr, rwxrwxrwx))

    val fileCollector3 = new FileCollector()
    assert(Files.walkFileTree(resourceRoot, fileCollector3) === resourceRoot)
    // order not guaranteed
    assert(
      fileCollector3.collectFiles().map(_.toString).toSet === Set(
        fileInHidden,
        directorySymlink,
        regularText,
        fileInSource,
        deletedSymlinkFile,
        utf16leTxt,
        symlinkText,
        rrr,
        rwxrwxrwx
      ).map(_.toString)
    )

    // todo: file TERMINATE
    // todo: file SKIP_SUBTREE
    // todo: file SKIP_SIBLINGS
    // todo: dir TERMINATE
    // todo: dir SKIP_SUBTREE
    // todo: dir SKIP_SIBLINGS
  }
  "walkFileTree(Path, JavaSet[FileVisitOption], maxDepth:Int, FileVisitor[_ >: Path])" ignore {}

  "write(Path, Array[Byte], OpenOption*)" in {
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
  "write(Path, JavaIterable[_ <: CharSequence], Charset, OpenOption*)" in {
    val tmpFile = Files.createTempFile("foo", ".txt")
    assert(Files.size(tmpFile) === 0)

    val utf16le = utf16leCharset
    val written = Files.write(tmpFile, Seq("abc").asJava, utf16le)
    assert(written === tmpFile)
    assert(Files.readAllLines(written, utf16le).asScala.toSeq === Seq("abc"))
    assert(Files.size(tmpFile) === 8)
  }
  "write(Path, JavaIterable[_ <: CharSequence], OpenOption*)" in {
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

  "getLastModifiedTime" in {
    assertThrows[IOException] {
      Files.getLastModifiedTime(noSuchFile)
    }
    assert(Files.getLastModifiedTime(fileInSource).toMillis > 0)
  }
}

private class FileCollector extends SimpleFileVisitor[Path] {
  private val buffer: ListBuffer[Path] = ListBuffer.empty

  override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
    buffer.addOne(file)
    FileVisitResult.CONTINUE
  }

  override def visitFileFailed(file: Path, exc: IOException): FileVisitResult = {
    println(s"failed: ${file}")
    super.visitFileFailed(file, exc)
  }

  def collectFiles(): Seq[Path] = buffer.toSeq
}
