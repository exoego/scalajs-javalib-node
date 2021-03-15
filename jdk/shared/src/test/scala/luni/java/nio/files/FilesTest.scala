package luni.java.nio.files

import org.scalatest.freespec.AnyFreeSpec
import support.TestSupport

import java.util.{Set => JavaSet}
import java.io._
import java.nio.charset.Charset
import java.nio.file._
import java.nio.file.attribute._
import java.nio.file.LinkOption.NOFOLLOW_LINKS
import java.util.concurrent.TimeUnit._
import scala.jdk.CollectionConverters._
import scala.collection.mutable.ListBuffer
import scala.language.reflectiveCalls

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

  private val unsupportedInitialAttributes = Seq(
    "nosuchattr",
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

  "copy(InputStream, Path, CopyOption*)" - {
    "default options" in {
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

    "REPLACE_EXISTING" in {
      val sourceFile = Files.createTempFile("foo", ".txt")
      Files.write(sourceFile, Seq("abc").asJava)
      val targetFile = Files.createTempFile("target", ".txt")
      using(new FileInputStream(sourceFile.toFile)) { in =>
        assert(Files.copy(in, targetFile, StandardCopyOption.REPLACE_EXISTING) === 4)
        assert(Files.readAllLines(targetFile).asScala === Seq("abc"))
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

  "copy(Path, Path, CopyOption*)" - {
    val root = Files.createTempDirectory("root")

    "default options" - {
      "If source is directory, create an empty dir" in {
        val nonEmptyDir = Files.createTempDirectory(root, "nonEmptyDir")
        Files.createTempFile(nonEmptyDir, "file", ".txt")
        val targetDir = root.resolve("targetDir")
        assert(Files.copy(nonEmptyDir, targetDir) === targetDir)
        assert(Files.exists(targetDir))
        assertThrows[FileAlreadyExistsException] {
          Files.copy(nonEmptyDir, targetDir)
        }
      }

      "copy file" in {
        val sourceFile = Files.createFile(root.resolve("foo.txt"))
        Files.write(sourceFile, Seq("abc").asJava)
        Files.setPosixFilePermissions(sourceFile, PosixFilePermissions.fromString("rwxrwxrwx"))
        val newFile = root.resolve("newFile.txt")
        assert(Files.copy(sourceFile, newFile) === newFile)
        assert(Files.readAllLines(newFile).asScala.toSeq === Seq("abc"))
        assert(!Files.isSameFile(sourceFile, newFile))
        assert(
          Files.getPosixFilePermissions(newFile) === PosixFilePermissions.fromString("rwxr-xr-x")
        )
        assert(Files.getPosixFilePermissions(newFile) !== Files.getPosixFilePermissions(sourceFile))
        assertThrows[FileAlreadyExistsException] {
          Files.copy(sourceFile, newFile)
        }
      }

      "copy symbolic link" in {
        val sourceFile     = Files.createFile(root.resolve("source.txt"))
        val symbolicSource = Files.createSymbolicLink(root.resolve("symbolic"), sourceFile)
        val newFile2       = root.resolve("newFile2.txt")
        Files.write(sourceFile, Seq("abc").asJava)
        assert(Files.copy(symbolicSource, newFile2) === newFile2)
        assert(Files.readAllLines(newFile2).asScala.toSeq === Seq("abc"))
        assert(!Files.isSameFile(symbolicSource, newFile2))
      }
    }

    "fail if source not exists" in {
      val notExist = root.resolve("not-exist")
      assertThrows[IOException] {
        Files.copy(notExist, root)
      }
      assertThrows[IOException] {
        Files.copy(notExist, notExist)
      }
    }

    "no effect if same file" in {
      Seq(Files.createTempFile("file", ".d"), root).foreach { path =>
        Files.copy(path, path)
      }
    }

    "REPLACE_EXISTING" - {
      "source is file" - {
        "If the target file exists, then the target file is replaced if it is not a non-empty directory" in {
          val source     = Files.write(Files.createTempFile("source", ".md"), Seq("source").asJava)
          val targetFile = Files.write(Files.createTempFile("target", ".md"), Seq("target").asJava)
          assert(Files.copy(source, targetFile, StandardCopyOption.REPLACE_EXISTING) === targetFile)
          assert(Files.readAllLines(targetFile).asScala === Seq("source"))

          val targetEmptyDir = Files.createTempDirectory("emptyDir")
          assert(
            Files
              .copy(source, targetEmptyDir, StandardCopyOption.REPLACE_EXISTING) === targetEmptyDir
          )
          assert(Files.readAllLines(targetEmptyDir).asScala === Seq("source"))

          val targetNonEmptyDir = Files.createTempDirectory("nonEmptyDir")
          Files.createFile(targetNonEmptyDir.resolve("file"))
          assertThrows[DirectoryNotEmptyException] {
            Files.copy(source, targetNonEmptyDir, StandardCopyOption.REPLACE_EXISTING)
          }
        }

        "If the target file exists and is a symbolic link, then the symbolic link itself, not the target of the link, is replaced." in {
          val source = Files.write(Files.createTempFile("source", ".md"), Seq("source").asJava)

          val tmpDir  = Files.createTempDirectory("tmp")
          val file    = Files.createTempFile("temp", "file")
          val symlink = Files.createSymbolicLink(tmpDir.resolve("symlink"), file)

          assertThrows[FileAlreadyExistsException] {
            Files.copy(source, symlink)
          }
          assert(Files.copy(source, symlink, StandardCopyOption.REPLACE_EXISTING) === symlink)
          assert(Files.readAllLines(symlink).asScala === Seq("source"))
        }
      }

      "source is directory" - {
        "If the target file exists, then the target file is replaced if it is not a non-empty directory" in {
          val source = Files.createTempDirectory("tmpdir")
          Files.createFile(source.resolve("file"))

          val targetFile = Files.write(Files.createTempFile("target", ".md"), Seq("target").asJava)
          assert(Files.copy(source, targetFile, StandardCopyOption.REPLACE_EXISTING) === targetFile)
          assert(Files.isDirectory(targetFile))
          assert(Files.notExists(targetFile.resolve("file")))

          val targetEmptyDir = Files.createTempDirectory("emptyDir")
          assert(
            Files
              .copy(source, targetEmptyDir, StandardCopyOption.REPLACE_EXISTING) === targetEmptyDir
          )
          assert(Files.isDirectory(targetFile))
          assert(Files.notExists(targetFile.resolve("file")))

          val targetNonEmptyDir = Files.createTempDirectory("nonEmptyDir")
          Files.createFile(targetNonEmptyDir.resolve("file"))
          assertThrows[DirectoryNotEmptyException] {
            Files.copy(source, targetNonEmptyDir, StandardCopyOption.REPLACE_EXISTING)
          }
        }

        "If the target file exists and is a symbolic link, then the symbolic link itself, not the target of the link, is replaced." in {
          val source = Files.createTempDirectory("tmpdir")
          Files.createFile(source.resolve("file"))

          val tmpDir  = Files.createTempDirectory("tmp")
          val file    = Files.createTempFile("temp", "file")
          val symlink = Files.createSymbolicLink(tmpDir.resolve("symlink"), file)

          assertThrows[FileAlreadyExistsException] {
            Files.copy(source, symlink)
          }
          assert(Files.copy(source, symlink, StandardCopyOption.REPLACE_EXISTING) === symlink)
          assert(Files.isDirectory(symlink))
          assert(Files.notExists(symlink.resolve("file")))
        }
      }
    }

    "COPY_ATTRIBUTES" - {
      "Minimally, the last-modified-time is copied to the target file if supported by both the source and target file stores." in {
        val sourceFile = Files.createFile(root.resolve("copyAttributes.txt"))
        Files.write(sourceFile, Seq("abc").asJava)
        Files.setPosixFilePermissions(sourceFile, PosixFilePermissions.fromString("rwxrwxrwx"))
        val newFile = root.resolve("copyAttributes_new.txt")
        assert(Files.copy(sourceFile, newFile, StandardCopyOption.COPY_ATTRIBUTES) === newFile)
        assert(Files.readAllLines(newFile).asScala.toSeq === Seq("abc"))
        assert(
          Files
            .getLastModifiedTime(newFile)
            .toMillis / 1000 === Files.getLastModifiedTime(sourceFile).toMillis / 1000,
          "precision loss"
        )
        assert(Files.getPosixFilePermissions(newFile) === Files.getPosixFilePermissions(sourceFile))
      }
    }

    "NOFOLLOW_LINKS" - {
      "If the file is a symbolic link, then the symbolic link itself, not the target of the link, is copied." ignore {
        // TODO
        fail()
      }
    }

    "unsupported options" in {
      assertThrows[UnsupportedOperationException] {
        Files.copy(
          Files.createTempDirectory("dir"),
          root.resolve("fail"),
          StandardCopyOption.ATOMIC_MOVE
        )
      }
      assertThrows[UnsupportedOperationException] {
        Files.copy(
          Files.createTempFile("file", ".md"),
          root.resolve("fail"),
          StandardCopyOption.ATOMIC_MOVE
        )
      }
    }
  }

  "createDirectories(Path, FileAttribute[_]*)" - {
    "no attributes" in {
      val tmpDir = Files.createTempDirectory("createDirectories")
      // No throw
      Files.createDirectories(tmpDir)

      val created = Files.createDirectories(tmpDir.resolve("sub"))
      assert(
        Files.getPosixFilePermissions(created) === PosixFilePermissions.fromString("rwxr-xr-x")
      )
      assert(Files.exists(created))
      assert(Files.isDirectory(created))
      assert(created.getFileName.toString === "sub")

      val nestedPath = tmpDir.resolve("1").resolve("2").resolve("3")
      assert(Files.notExists(nestedPath))
      val createdDeep = Files.createDirectories(nestedPath)
      assert(Files.exists(createdDeep))
      assert(Files.isDirectory(createdDeep))
    }

    "file-attributes to set atomically when creating the directory" in {
      val tmpDir = Files.createTempDirectory("createDirectory")
      val created =
        Files.createDirectories(tmpDir.resolve("x").resolve("y"), new FilePermissions("rwx------"))
      assert(
        Files.getPosixFilePermissions(created) === PosixFilePermissions.fromString("rwx------")
      )
      assert(Files.exists(created))
    }

    "unsupported attributes" in {
      val tmpDir = Files.createTempDirectory("createDirectory")

      unsupportedInitialAttributes.foreach { key =>
        assertThrows[UnsupportedOperationException] {
          Files.createDirectories(
            tmpDir.resolve("x").resolve("y").resolve("z"),
            new ConstantFileAttributes(key)
          )
        }
      }
    }
  }

  "createDirectory(Path, FileAttribute[_]*)" - {
    "no attributes" in {
      val tmpDir = Files.createTempDirectory("createDirectory")
      assertThrows[FileAlreadyExistsException] {
        Files.createDirectory(tmpDir)
      }

      val created = Files.createDirectory(tmpDir.resolve("sub"))
      assert(
        Files.getPosixFilePermissions(created) === PosixFilePermissions.fromString("rwxr-xr-x")
      )
      assert(Files.exists(created))
      assert(Files.isDirectory(created))
      assert(created.getFileName.toString === "sub")

      val nestedPath = tmpDir.resolve("1").resolve("2").resolve("3")
      assert(Files.notExists(nestedPath))
      assertThrows[NoSuchFileException] {
        Files.createDirectory(nestedPath)
      }
    }

    "file-attributes to set atomically when creating the directory" in {
      val tmpDir  = Files.createTempDirectory("createDirectory")
      val created = Files.createDirectory(tmpDir.resolve("x"), new FilePermissions("rwx------"))
      assert(
        Files.getPosixFilePermissions(created) === PosixFilePermissions.fromString("rwx------")
      )
      assert(Files.exists(created))
    }

    "unsupported attributes" in {
      val tmpDir = Files.createTempDirectory("createDirectory")
      unsupportedInitialAttributes.foreach { key =>
        assertThrows[UnsupportedOperationException] {
          Files.createDirectory(tmpDir.resolve("x"), new ConstantFileAttributes(key))
        }
      }
    }
  }

  "createFile(Path, FileAttribute[_]*)" - {
    "no attributes" in {
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

    "permissions" in {
      val dir  = Files.createTempDirectory("createFile")
      val file = dir.resolve("foo.txt")
      assert(Files.createFile(file, new FilePermissions("rwxrwxrwx")) === file)
      assert(Files.getPosixFilePermissions(file) === PosixFilePermissions.fromString("rwxr-xr-x"))
    }

    "unsupported attributes" in {
      val tmpDir = Files.createTempDirectory("createDirectory")
      unsupportedInitialAttributes.foreach { key =>
        assertThrows[UnsupportedOperationException] {
          Files.createFile(tmpDir.resolve("x"), new ConstantFileAttributes(key))
        }
      }
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

  "createSymbolicLink(Path, Path, FileAttribute[_])" - {
    "no attributes" in {
      val sourceDir = Files.createTempDirectory("source")
      val targetDir = Files.createTempDirectory("source").resolve("tmp-symlink")
      val created   = Files.createSymbolicLink(targetDir, sourceDir)
      assert(
        Files.getPosixFilePermissions(created) === PosixFilePermissions.fromString("rwx------")
      )

      assert(Files.isSymbolicLink(created))
      assert(Files.exists(created))

      assertThrows[FileAlreadyExistsException] {
        Files.createSymbolicLink(sourceDir, targetDir)
      }
    }

    "permissions not supported on symbolic link" in {
      val sourceDir = Files.createTempDirectory("source")
      val targetDir = Files.createTempDirectory("source").resolve("tmp-symlink")
      assertThrows[UnsupportedOperationException] {
        Files.createSymbolicLink(targetDir, sourceDir, new FilePermissions("rwxrwxrwx"))
      }
    }

    "unsupported attributes" in {
      unsupportedInitialAttributes.foreach { key =>
        val sourceDir = Files.createTempDirectory("source")
        val targetDir = Files.createTempDirectory("source").resolve("tmp-symlink")
        assertThrows[UnsupportedOperationException] {
          Files.createSymbolicLink(targetDir, sourceDir, new ConstantFileAttributes(key))
        }
      }
    }
  }

  "createTempDirectory(Path, String, FileAttribute[_])" - {
    "no attributes" in {
      val base    = Files.createTempDirectory("more")
      val tempDir = Files.createTempDirectory(base, "foobar")
      assert("/more[^/]+/foobar[^/]+$".r.findFirstIn(tempDir.toString).isDefined)
      assert(Files.exists(tempDir))
      assert(Files.isDirectory(tempDir))
      assert(
        Files.getPosixFilePermissions(tempDir) === PosixFilePermissions.fromString("rwx------")
      )
    }

    "permissions" in {
      val base = Files.createTempDirectory("more")
      val created =
        Files.createTempDirectory(base, "createDirectory", new FilePermissions("rwxrwxrwx"))
      assert(
        Files.getPosixFilePermissions(created) === PosixFilePermissions.fromString("rwxr-xr-x")
      )
      assert(Files.isDirectory(created))
    }

    "unsupported attributes" in {
      val base = Files.createTempDirectory("more")
      unsupportedInitialAttributes.foreach { key =>
        assertThrows[UnsupportedOperationException] {
          Files.createTempDirectory(base, "x", new ConstantFileAttributes(key))
        }
      }
    }
  }

  "createTempDirectory(String, FileAttribute[_])" - {
    "no attributes" in {
      val tempDir = Files.createTempDirectory("foobar")
      assert(tempDir.toString.contains("/foobar"))
      assert(Files.exists(tempDir))
      assert(Files.isDirectory(tempDir))
      assert(
        Files.getPosixFilePermissions(tempDir) === PosixFilePermissions.fromString("rwx------")
      )
    }

    "permissions" in {
      val created = Files.createTempDirectory("createDirectory", new FilePermissions("rwxrwxrwx"))
      assert(
        Files.getPosixFilePermissions(created) === PosixFilePermissions.fromString("rwxr-xr-x")
      )
      assert(Files.isDirectory(created))
    }

    "unsupported attributes" in {
      unsupportedInitialAttributes.foreach { key =>
        assertThrows[UnsupportedOperationException] {
          Files.createTempDirectory("x", new ConstantFileAttributes(key))
        }
      }
    }
  }

  "createTempFile(Path, String, String, FileAttribute[_])" - {
    "no attributes" in {
      val base    = Files.createTempDirectory("more")
      val tmpFile = Files.createTempFile(base, "foobar", ".txt")
      assert("/more[^/]+/foobar[^/]+\\.txt$".r.findFirstIn(tmpFile.toString).isDefined)
      assert(Files.exists(tmpFile))
      assert(Files.isRegularFile(tmpFile))
      assert(
        Files.getPosixFilePermissions(tmpFile) === PosixFilePermissions.fromString("rw-------")
      )
    }

    "permissions" in {
      val base     = Files.createTempDirectory("more")
      val tmpFile2 = Files.createTempFile(base, "foobar", ".md", new FilePermissions("rwxrwxrwx"))
      assert(Files.exists(tmpFile2))
      assert(Files.isRegularFile(tmpFile2))
      assert(
        Files.getPosixFilePermissions(tmpFile2) === PosixFilePermissions.fromString("rwxr-xr-x")
      )
    }

    "unsupported attributes" in {
      val base = Files.createTempDirectory("more")
      unsupportedInitialAttributes.foreach { key =>
        assertThrows[UnsupportedOperationException] {
          Files.createTempFile(base, "x", "y", new ConstantFileAttributes(key))
        }
      }
    }
  }

  "createTempFile(String, String, FileAttribute[_])" - {
    "no attributes" in {
      val tmpFile = Files.createTempFile("foobar", ".md")
      assert("/foobar[^/]+\\.md$".r.findFirstIn(tmpFile.toString).isDefined)
      assert(Files.exists(tmpFile))
      assert(Files.isRegularFile(tmpFile))
      assert(
        Files.getPosixFilePermissions(tmpFile) === PosixFilePermissions.fromString("rw-------")
      )
    }

    "permissions" in {
      val tmpFile2 = Files.createTempFile("foobar", ".md", new FilePermissions("rwxrwxrwx"))
      assert(Files.exists(tmpFile2))
      assert(Files.isRegularFile(tmpFile2))
      assert(
        Files.getPosixFilePermissions(tmpFile2) === PosixFilePermissions.fromString("rwxr-xr-x")
      )
    }

    "unsupported attributes" in {
      val base = Files.createTempDirectory("more")
      unsupportedInitialAttributes.foreach { key =>
        assertThrows[UnsupportedOperationException] {
          Files.createTempFile("x", "y", new ConstantFileAttributes(key))
        }
      }
    }
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

    val file2 = Files.createTempFile("open", ".txt")
    Files.write(file2, Seq("foo", "bar", "buz").asJava)
    using(new FileInputStream(file2.toFile)) { in =>
      in.read(new Array[Byte](10))
      Files.delete(file2)
      assert(Files.notExists(file2))
    }
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
    assert(Files.exists(fileInSymlink, NOFOLLOW_LINKS))

    assert(Files.exists(fileInSymlink))
    assert(Files.exists(fileInSymlink, NOFOLLOW_LINKS))

    assert(!Files.exists(fileInDeletedSymlink))
    assert(!Files.exists(fileInDeletedSymlink, NOFOLLOW_LINKS))

    assert(!Files.exists(deletedSymlinkFile))
    assert(Files.exists(deletedSymlinkFile, NOFOLLOW_LINKS))
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

      assert(Files.getAttribute(file, "posix:permissions").isInstanceOf[java.util.Set[_]])
    }

    "invalid attribute" in {
      assertThrows[IllegalArgumentException] {
        Files.getAttribute(file, "permissions")
      }
      assertThrows[IllegalArgumentException] {
        Files.getAttribute(file, "basic:permissions")
      }
      assertThrows[UnsupportedOperationException] {
        Files.getAttribute(file, "unknown:lastModifiedTime")
      }
      assertThrows[IllegalArgumentException] {
        Files.getAttribute(file, "basic:typo")
      }
    }

    "linkOption" in {
      assert(Files.getAttribute(fileInSymlink, "isRegularFile") === true)
      assert(Files.getAttribute(fileInSymlink, "isRegularFile", NOFOLLOW_LINKS) === true)
      assert(Files.getAttribute(symlinkText, "isRegularFile") === true)
      assert(Files.getAttribute(symlinkText, "isRegularFile", NOFOLLOW_LINKS) === true)

      assert(Files.getAttribute(directorySymlink, "isDirectory") === true)
      assert(Files.getAttribute(directorySymlink, "isDirectory", NOFOLLOW_LINKS) === false)
      assert(Files.getAttribute(directorySymlink, "isSymbolicLink") === false)
      assert(Files.getAttribute(directorySymlink, "isSymbolicLink", NOFOLLOW_LINKS) === true)
    }
  }

  "getFileAttributeView[V <: FileAttributeView](Path, Class[V], LinkOption*)" - {
    val posix = classOf[PosixFileAttributeView]

    "default options" in {
      val tmpDir = Files.createTempFile("tmp", ".txt")

      val view: PosixFileAttributeView =
        Files.getFileAttributeView(tmpDir, posix)
      assert(view.name() === "posix")

      val oldAttr = view.readAttributes()
      assert(oldAttr.permissions() === PosixFilePermissions.fromString("rw-------"))

      // oldAttr never affected by setter
      view.setPermissions(PosixFilePermissions.fromString("rwxr-xr-x"))
      assert(oldAttr.permissions() === PosixFilePermissions.fromString("rw-------"))

      val newAttr = view.readAttributes()
      assert(newAttr.permissions() === PosixFilePermissions.fromString("rwxr-xr-x"))

      val noSuchFileView = Files.getFileAttributeView(noSuchFile, posix)
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
    }
    // TODO: node.js can not support these
    // view.getOwner()
    // newAttr.group()
    // newAttr.owner()

    "linkoption" in {
      assert(Files.getFileAttributeView(fileInSymlink, posix).readAttributes().isRegularFile)
      assert(
        Files
          .getFileAttributeView(fileInSymlink, posix, NOFOLLOW_LINKS)
          .readAttributes()
          .isRegularFile
      )
      assert(Files.getFileAttributeView(symlinkText, posix).readAttributes().isRegularFile)
      assert(
        Files
          .getFileAttributeView(symlinkText, posix, NOFOLLOW_LINKS)
          .readAttributes()
          .isRegularFile
      )

      val symlinkAttrs = Files.getFileAttributeView(directorySymlink, posix).readAttributes()
      assert(symlinkAttrs.isDirectory)
      assert(!symlinkAttrs.isSymbolicLink)
      val symlinkNofollowAttrs = Files
        .getFileAttributeView(directorySymlink, posix, NOFOLLOW_LINKS)
        .readAttributes()
      assert(!symlinkNofollowAttrs.isDirectory)
      assert(symlinkNofollowAttrs.isSymbolicLink)
    }
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
    assert(Files.isDirectory(directory, NOFOLLOW_LINKS))
    assert(Files.isDirectory(directorySource, NOFOLLOW_LINKS))
    assert(!Files.isDirectory(directorySymlink, NOFOLLOW_LINKS))

    assert(!Files.isDirectory(regularText, NOFOLLOW_LINKS))
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
    assert(!Files.isRegularFile(directorySymlink, NOFOLLOW_LINKS))
    assert(Files.isRegularFile(fileInSource, NOFOLLOW_LINKS))

    assert(!Files.isRegularFile(directorySymlink))
    assert(!Files.isRegularFile(directorySymlink, NOFOLLOW_LINKS))
    assert(Files.isRegularFile(fileInSymlink))
    assert(Files.isRegularFile(fileInSymlink, NOFOLLOW_LINKS))

    assert(Files.isRegularFile(regularText))
    assert(Files.isRegularFile(regularText, NOFOLLOW_LINKS))
    assert(Files.isRegularFile(symlinkText))
    assert(Files.isRegularFile(symlinkText, NOFOLLOW_LINKS))

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
    assert(!Files.notExists(fileInSymlink, NOFOLLOW_LINKS))

    assert(!Files.notExists(fileInSymlink))
    assert(!Files.notExists(fileInSymlink, NOFOLLOW_LINKS))

    assert(Files.notExists(fileInDeletedSymlink))
    assert(Files.notExists(fileInDeletedSymlink, NOFOLLOW_LINKS))

    assert(Files.notExists(deletedSymlinkFile))
    assert(!Files.notExists(deletedSymlinkFile, NOFOLLOW_LINKS))
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
      assert(dirAttr.lastAccessTime().compareTo(dirAttr.creationTime()) >= 0)
      assert(dirAttr.lastModifiedTime().compareTo(dirAttr.creationTime()) >= 0)
    }

    // symbolic link with NOFOLLOW_LINK
    val symAttr: BasicFileAttributes = Files.readAttributes(
      directorySymlink,
      classOf[BasicFileAttributes],
      NOFOLLOW_LINKS
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
      Seq(Seq.empty[LinkOption], Seq(NOFOLLOW_LINKS)).foreach { options =>
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
        assert(fileAttr.lastAccessTime().compareTo(fileAttr.creationTime()) >= 0)
        assert(fileAttr.lastModifiedTime().compareTo(fileAttr.creationTime()) >= 0)
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
    // unavailable attrs
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
        assert(dirAttr.get("permissions") === None)
      }

      assert(Files.readAttributes(dir, "posix:permissions").asScala.apply("permissions") !== null)

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
    val symAttr = Files.readAttributes(directorySymlink, "*", NOFOLLOW_LINKS).asScala
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
      Files.setAttribute(file, "basic:lastModifiedTime", FileTime.fromMillis(1615679182864L))
      assert(
        Files.getLastModifiedTime(file) === FileTime.fromMillis(1615679182000L),
        "precision loss"
      )

      Files.setAttribute(file, "lastAccessTime", FileTime.from(11, DAYS))
      assert(Files.getAttribute(file, "lastAccessTime") === FileTime.from(11, DAYS))
      Files.setAttribute(file, "posix:lastAccessTime", FileTime.from(22, DAYS))
      assert(Files.getAttribute(file, "posix:lastAccessTime") === FileTime.from(22, DAYS))
      Files.setAttribute(file, "basic:lastAccessTime", FileTime.from(33, DAYS))
      assert(Files.getAttribute(file, "basic:lastAccessTime") === FileTime.from(33, DAYS))
      Files.setAttribute(file, "basic:lastModifiedTime", FileTime.fromMillis(1615679182864L))
      assert(
        Files.getAttribute(file, "basic:lastModifiedTime") === FileTime.fromMillis(1615679182000L),
        "precision loss"
      )

      Files.setAttribute(file, "posix:permissions", PosixFilePermissions.fromString("rwxrwxrwx"))
      assert(
        Files.getAttribute(file, "posix:permissions") === PosixFilePermissions
          .fromString("rwxrwxrwx")
      )
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
      assertThrows[IllegalArgumentException] {
        Files.setAttribute(file, "permissions", true)
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

  "setLastModifiedTime(Path, FileTime)" - {
    "can work with an existing file" in {
      val file = Files.createTempFile("lastmodified", ".txt")

      assert(Files.setLastModifiedTime(file, FileTime.from(100, DAYS)) === file)
      assert(Files.getLastModifiedTime(file) === FileTime.from(100, DAYS))

      assert(Files.setLastModifiedTime(file, FileTime.from(200, DAYS)) === file)
      assert(Files.getLastModifiedTime(file) === FileTime.from(200, DAYS))
    }
    "can work with an existing directory" in {
      val file = Files.createTempDirectory("lastmodified")

      assert(Files.setLastModifiedTime(file, FileTime.from(100, DAYS)) === file)
      assert(Files.getLastModifiedTime(file) === FileTime.from(100, DAYS))

      assert(Files.setLastModifiedTime(file, FileTime.from(200, DAYS)) === file)
      assert(Files.getLastModifiedTime(file) === FileTime.from(200, DAYS))
    }
    "reject null time" in {
      assume(isScalaJS || isJDK11AndLater, "Java 8 does not throw NPE")
      val file = Files.createTempFile("lastmodified", ".txt")
      assertThrows[NullPointerException] {
        Files.setLastModifiedTime(file, null)
      }
    }
    "reject non-existent file" in {
      assertThrows[IOException] {
        Files.setLastModifiedTime(noSuchFile, FileTime.from(200, DAYS))
      }
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

  "walkFileTree(Path, FileVisitor[_ >: Path])" - {
    "depth-first, but iteration order not guaranteed" in {
      val root = Files.createTempDirectory("top")
      val dirA = Files.createDirectory(root.resolve("dirA"))
      val dirB = Files.createDirectory(dirA.resolve("dirB"))
      val dir1 = Files.createDirectory(root.resolve("dir1"))
      val dir2 = Files.createDirectory(dir1.resolve("dir2"))

      val collector = new BaseCountingPathCollector()
      assert(Files.walkFileTree(root, collector) === root)
      assert(collector.countPreVisitDirectory() === 5)
      assert(collector.countVisitFile() === 0)
      assert(collector.countVisitFileFailed() === 0)
      assert(collector.countPostVisitDirectory() === 5)

      val alphaThenNum = List(root, dirA, dirB, dirB, dirA, dir1, dir2, dir2, dir1, root)
      val numThenAlpha = List(root, dir1, dir2, dir2, dir1, dirA, dirB, dirB, dirA, root)
      assert(collector.visited === alphaThenNum || collector.visited === numThenAlpha)
    }

    "TERMINATE" - {
      "terminate when preVisitDirectory return TERMINATE" in {
        val root = Files.createTempDirectory("top")
        val dirA = Files.createDirectory(root.resolve("dirA"))
        val dirB = Files.createDirectory(dirA.resolve("terminate"))

        val collector = new BaseCountingPathCollector {
          override def preVisitDirectoryImpl(
              dir: Path,
              attrs: BasicFileAttributes
          ): FileVisitResult = {
            if (dir == dirB) {
              FileVisitResult.TERMINATE
            } else {
              FileVisitResult.CONTINUE
            }
          }
        }
        assert(Files.walkFileTree(root, collector) === root)
        assert(collector.countPreVisitDirectory() === 3)
        assert(collector.countVisitFile() === 0)
        assert(collector.countVisitFileFailed() === 0)
        assert(collector.countPostVisitDirectory() === 0)
        assert(collector.visited === List(root, dirA, dirB))
      }

      "terminate when visitFile return TERMINATE" in {
        val root  = Files.createTempDirectory("top")
        val dirA  = Files.createDirectory(root.resolve("dirA"))
        val dirB  = Files.createDirectory(dirA.resolve("dirB"))
        val fileC = Files.createFile(dirB.resolve(".hidden"))
        val fileD = Files.createFile(dirB.resolve("terminate"))

        val collector = new BaseCountingPathCollector {
          override protected def visitFileImpl(
              file: Path,
              attrs: BasicFileAttributes
          ): FileVisitResult = {
            if (file == fileD) {
              FileVisitResult.TERMINATE
            } else {
              FileVisitResult.CONTINUE
            }
          }
        }
        assert(Files.walkFileTree(root, collector) === root)
        assert(collector.countPreVisitDirectory() === 3)
        assert(collector.countVisitFile() === 2)
        assert(collector.countVisitFileFailed() === 0)
        assert(collector.countPostVisitDirectory() === 0)
        assert(collector.visited === List(root, dirA, dirB, fileC, fileD))
      }

      "terminate when visitFileFailed return TERMINATE" ignore {
        // This method is invoked if the file's attributes could not be read,
        // the file is a directory that could not be opened, and other reasons.
      }

      "terminate when postVisitDirectory return TERMINATE" in {
        val root = Files.createTempDirectory("top")
        val dirA = Files.createDirectory(root.resolve("dirA"))
        val dirB = Files.createDirectory(dirA.resolve("terminate"))

        val collector = new BaseCountingPathCollector {
          override protected def postVisitDirectoryImpl(
              dir: Path,
              exc: IOException
          ): FileVisitResult = {
            if (dir == dirB) {
              FileVisitResult.TERMINATE
            } else {
              FileVisitResult.CONTINUE
            }
          }
        }
        assert(Files.walkFileTree(root, collector) === root)
        assert(collector.countPreVisitDirectory() === 3)
        assert(collector.countVisitFile() === 0)
        assert(collector.countVisitFileFailed() === 0)
        assert(collector.countPostVisitDirectory() === 1)
        assert(collector.visited === List(root, dirA, dirB, dirB))
      }
    }

    "SKIP_SUBTREE" - {
      "skip sub tree when preVisitDirectory return SKIP_SUBTREE" in {
        val root  = Files.createTempDirectory("top")
        val dirA  = Files.createDirectory(root.resolve(".dirA"))
        val fileB = Files.createFile(dirA.resolve("fileB"))
        val dir1  = Files.createDirectory(root.resolve("dir1"))
        val file2 = Files.createFile(dir1.resolve("file2"))

        val collector = new BaseCountingPathCollector {
          override protected def preVisitDirectoryImpl(
              dir: Path,
              attrs: BasicFileAttributes
          ): FileVisitResult = {
            if (dir == dirA) {
              FileVisitResult.SKIP_SUBTREE
            } else {
              FileVisitResult.CONTINUE
            }
          }
        }
        assert(Files.walkFileTree(root, collector) === root)
        assert(collector.countPreVisitDirectory() === 3)
        assert(collector.countVisitFile() === 1)
        assert(collector.countVisitFileFailed() === 0)
        assert(collector.countPostVisitDirectory() === 2)
        assert(collector.visited === List(root, dirA, dir1, file2, dir1, root))
      }

      "SKIP_SUBTREE on postVisitDirectory is identical to CONTINUE" in {
        val root  = Files.createTempDirectory("top")
        val dirA  = Files.createDirectory(root.resolve(".dirA"))
        val fileB = Files.createFile(dirA.resolve("fileB"))
        val dir1  = Files.createDirectory(root.resolve("dir1"))
        val file2 = Files.createFile(dir1.resolve("file2"))

        Seq(
          new BaseCountingPathCollector(),
          new BaseCountingPathCollector {
            override protected def postVisitDirectoryImpl(
                dir: Path,
                exc: IOException
            ): FileVisitResult =
              if (dir == dirA) {
                FileVisitResult.SKIP_SUBTREE
              } else {
                FileVisitResult.CONTINUE
              }
          }
        ).foreach { collector =>
          assert(Files.walkFileTree(root, collector) === root)
          assert(collector.countPreVisitDirectory() === 3)
          assert(collector.countVisitFile() === 2)
          assert(collector.countVisitFileFailed() === 0)
          assert(collector.countPostVisitDirectory() === 3)
          assert(collector.visited === List(root, dirA, fileB, dirA, dir1, file2, dir1, root))
        }
      }

      "SKIP_SUBTREE on visitFile is identical to CONTINUE" in {
        val root    = Files.createTempDirectory("top")
        val dirA    = Files.createDirectory(root.resolve(".dirA"))
        val fileB   = Files.createFile(dirA.resolve("fileB"))
        val fileBBB = Files.createFile(dirA.resolve("fileBBB"))
        val dir1    = Files.createDirectory(root.resolve("dir1"))
        val file2   = Files.createFile(dir1.resolve("file2"))

        Seq(
          new BaseCountingPathCollector(),
          new BaseCountingPathCollector {
            override protected def visitFileImpl(
                file: Path,
                attrs: BasicFileAttributes
            ): FileVisitResult =
              if (file == fileB) {
                FileVisitResult.SKIP_SUBTREE
              } else {
                FileVisitResult.CONTINUE
              }
          }
        ).foreach { collector =>
          assert(Files.walkFileTree(root, collector) === root)
          assert(collector.countPreVisitDirectory() === 3)
          assert(collector.countVisitFile() === 3)
          assert(collector.countVisitFileFailed() === 0)
          assert(collector.countPostVisitDirectory() === 3)
          assert(
            collector.visited === List(root, dirA, fileB, fileBBB, dirA, dir1, file2, dir1, root)
          )
        }
      }
    }

    "SKIP_SIBLINGS" - {
      "skip siblings when preVisitDirectory return SKIP_SUBTREE" in {
        val root  = Files.createTempDirectory("top")
        val dirA  = Files.createDirectory(root.resolve(".dirA"))
        val fileB = Files.createFile(dirA.resolve("fileB"))
        val dir1  = Files.createDirectory(root.resolve("dir1"))
        val file2 = Files.createFile(dir1.resolve("file2"))

        val collector = new BaseCountingPathCollector {
          override protected def preVisitDirectoryImpl(
              dir: Path,
              attrs: BasicFileAttributes
          ): FileVisitResult = {
            if (dir == dirA) {
              FileVisitResult.SKIP_SIBLINGS
            } else {
              FileVisitResult.CONTINUE
            }
          }
        }
        assert(Files.walkFileTree(root, collector) === root)
        assert(collector.countPreVisitDirectory() === 2)
        assert(collector.countVisitFile() === 0)
        assert(collector.countVisitFileFailed() === 0)
        assert(collector.countPostVisitDirectory() === 1)
        assert(collector.visited === List(root, dirA, /* fileB, dirA, *dir1, file2, dir1, */ root))
      }

      "SKIP_SUBTREE on postVisitDirectory is identical to CONTINUE" in {
        val root  = Files.createTempDirectory("top")
        val dirA  = Files.createDirectory(root.resolve(".dirA"))
        val fileB = Files.createFile(dirA.resolve("fileB"))
        val dir1  = Files.createDirectory(root.resolve("dir1"))
        val file2 = Files.createFile(dir1.resolve("file2"))

        Seq(
          new BaseCountingPathCollector(),
          new BaseCountingPathCollector {
            override protected def postVisitDirectoryImpl(
                dir: Path,
                exc: IOException
            ): FileVisitResult =
              if (dir == dirA) {
                FileVisitResult.SKIP_SIBLINGS
              } else {
                FileVisitResult.CONTINUE
              }
          }
        ).foreach { collector =>
          assert(Files.walkFileTree(root, collector) === root)
          assert(collector.countPreVisitDirectory() === 3)
          assert(collector.countVisitFile() === 2)
          assert(collector.countVisitFileFailed() === 0)
          assert(collector.countPostVisitDirectory() === 3)
          assert(collector.visited === List(root, dirA, fileB, dirA, dir1, file2, dir1, root))
        }
      }

      "skip siblings when visitFile return SKIP_SIBLINGS" in {
        val root    = Files.createTempDirectory("top")
        val dirA    = Files.createDirectory(root.resolve(".dirA"))
        val fileB   = Files.createFile(dirA.resolve("fileB"))
        val fileBBB = Files.createFile(dirA.resolve("fileBBB"))

        val collector = new BaseCountingPathCollector {
          override protected def visitFileImpl(
              file: Path,
              attrs: BasicFileAttributes
          ): FileVisitResult =
            if (file == fileB) {
              FileVisitResult.SKIP_SIBLINGS
            } else {
              FileVisitResult.CONTINUE
            }
        }
        assert(Files.walkFileTree(root, collector) === root)
        assert(collector.countPreVisitDirectory() === 2)
        assert(collector.countVisitFile() === 1)
        assert(collector.countVisitFileFailed() === 0)
        assert(collector.countPostVisitDirectory() === 2)
        assert(collector.visited === List(root, dirA, fileB, dirA, root))
      }
    }
  }
  "walkFileTree(Path, JavaSet[FileVisitOption], maxDepth:Int, FileVisitor[_ >: Path])" - {
    "depth" - {
      "file" in {
        val root  = Files.createTempDirectory("top")
        val file1 = Files.createFile(root.resolve("file"))

        val collector0 = new BaseCountingPathCollector()
        assert(Files.walkFileTree(root, Set.empty[FileVisitOption].asJava, 0, collector0) === root)
        assert(collector0.countPreVisitDirectory() === 0)
        assert(
          collector0.countVisitFile() === 1,
          "directory at final depth should be treated as file"
        )
        assert(collector0.countVisitFileFailed() === 0)
        assert(collector0.countPostVisitDirectory() === 0)
        assert(collector0.visited === List(root))

        val collector1 = new BaseCountingPathCollector()
        assert(Files.walkFileTree(root, Set.empty[FileVisitOption].asJava, 1, collector1) === root)
        assert(collector1.countPreVisitDirectory() === 1)
        assert(collector1.countVisitFile() === 1)
        assert(collector1.countVisitFileFailed() === 0)
        assert(collector1.countPostVisitDirectory() === 1)
        assert(collector1.visited === List(root, file1, root))

        val collector2 = new BaseCountingPathCollector()
        assert(Files.walkFileTree(root, Set.empty[FileVisitOption].asJava, 2, collector2) === root)
        assert(collector2.countPreVisitDirectory() === 1)
        assert(collector2.countVisitFile() === 1)
        assert(collector2.countVisitFileFailed() === 0)
        assert(collector2.countPostVisitDirectory() === 1)
        assert(collector2.visited === List(root, file1, root))
      }

      "directory at final depth is treated as file" in {
        val root = Files.createTempDirectory("top")
        val dir1 = Files.createDirectory(root.resolve("dir1"))
        val dir2 = Files.createDirectory(dir1.resolve("dir2"))
        val dir3 = Files.createDirectory(dir2.resolve("dir3"))

        val collector0 = new BaseCountingPathCollector()
        assert(Files.walkFileTree(root, Set.empty[FileVisitOption].asJava, 0, collector0) === root)
        assert(collector0.countPreVisitDirectory() === 0)
        assert(
          collector0.countVisitFile() === 1,
          "directory at final depth should be treated as file"
        )
        assert(collector0.countVisitFileFailed() === 0)
        assert(collector0.countPostVisitDirectory() === 0)
        assert(collector0.visited === List(root))

        val collector1 = new BaseCountingPathCollector()
        assert(Files.walkFileTree(root, Set.empty[FileVisitOption].asJava, 1, collector1) === root)
        assert(collector1.countPreVisitDirectory() === 1)
        assert(
          collector1.countVisitFile() === 1,
          "directory at final depth should be treated as file"
        )
        assert(collector1.countVisitFileFailed() === 0)
        assert(collector1.countPostVisitDirectory() === 1)
        assert(collector1.visited === List(root, dir1, root))

        val collector2 = new BaseCountingPathCollector()
        assert(Files.walkFileTree(root, Set.empty[FileVisitOption].asJava, 2, collector2) === root)
        assert(collector2.countPreVisitDirectory() === 2)
        assert(
          collector2.countVisitFile() === 1,
          "directory at final depth should be treated as file"
        )
        assert(collector2.countVisitFileFailed() === 0)
        assert(collector2.countPostVisitDirectory() === 2)
        assert(collector2.visited === List(root, dir1, dir2, dir1, root))

        val collector3 = new BaseCountingPathCollector()
        assert(Files.walkFileTree(root, Set.empty[FileVisitOption].asJava, 3, collector3) === root)
        assert(collector3.countPreVisitDirectory() === 3)
        assert(
          collector3.countVisitFile() === 1,
          "directory at final depth should be treated as file"
        )
        assert(collector3.countVisitFileFailed() === 0)
        assert(collector3.countPostVisitDirectory() === 3)
        assert(collector3.visited === List(root, dir1, dir2, dir3, dir2, dir1, root))

        val collector4 = new BaseCountingPathCollector()
        assert(Files.walkFileTree(root, Set.empty[FileVisitOption].asJava, 4, collector4) === root)
        assert(collector4.countPreVisitDirectory() === 4)
        assert(
          collector4.countVisitFile() === 0,
          "directory at final depth should be treated as file"
        )
        assert(collector4.countVisitFileFailed() === 0)
        assert(collector4.countPostVisitDirectory() === 4)
        assert(collector4.visited === List(root, dir1, dir2, dir3, dir3, dir2, dir1, root))
      }
    }

    "LinkOptions" - {
      "links not followed by default" in {
        val noFollow = Set.empty[FileVisitOption].asJava

        val col0 = new BaseCountingPathCollector()
        assert(Files.walkFileTree(directorySymlink, noFollow, 0, col0) === directorySymlink)
        assert(col0.countPreVisitDirectory() === 0)
        assert(col0.countVisitFile() === 1)
        assert(col0.countVisitFileFailed() === 0)
        assert(col0.countPostVisitDirectory() === 0)
        assert(col0.visited === List(directorySymlink))

        val col1 = new BaseCountingPathCollector()
        assert(Files.walkFileTree(directorySymlink, noFollow, 1, col1) === directorySymlink)
        assert(col1.countPreVisitDirectory() === 0)
        assert(col1.countVisitFile() === 1)
        assert(col1.countVisitFileFailed() === 0)
        assert(col1.countPostVisitDirectory() === 0)
        assert(col1.visited === List(directorySymlink))
      }

      "follow links" in {
        val follow = Set(FileVisitOption.FOLLOW_LINKS).asJava

        val col0 = new BaseCountingPathCollector()
        assert(Files.walkFileTree(directorySymlink, follow, 0, col0) === directorySymlink)
        assert(col0.countPreVisitDirectory() === 0)
        assert(col0.countVisitFile() === 1)
        assert(col0.countVisitFileFailed() === 0)
        assert(col0.countPostVisitDirectory() === 0)
        assert(col0.visited === List(directorySymlink))

        val col1 = new BaseCountingPathCollector()
        assert(Files.walkFileTree(directorySymlink, follow, 1, col1) === directorySymlink)
        assert(col1.countPreVisitDirectory() === 1)
        assert(col1.countVisitFile() === 1)
        assert(col1.countVisitFileFailed() === 0)
        assert(col1.countPostVisitDirectory() === 1)
        assert(col1.visited === List(directorySymlink, fileInSymlink, directorySymlink))
        assert(col1.visited !== List(directorySymlink, fileInSource, directorySymlink))
      }
    }
  }

  "write(Path, Array[Byte], OpenOption*)" - {
    "no option" in {
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

//      assertThrows[IOException] {
//        Files.write(directory, Array[Byte](97, 98, 99))
//      }
    }

    "options" - {
      "NOFOLLOW_LINKS" ignore {}

      "StandardOpenOption.READ" in {
        val tmpFile = Files.createTempFile("1", "")
        assertThrows[IllegalArgumentException] {
          Files.write(tmpFile, Array.empty[Byte], StandardOpenOption.READ)
        }
      }

      "StandardOpenOption.WRITE" in {
        val exists = Files.createTempFile("2", "")
        Files.write(exists, Array[Byte](97, 98, 99), StandardOpenOption.WRITE)
        assert(Files.readAllLines(exists).asScala === List("abc"))
        Files.write(exists, Array[Byte](100, 101, 102), StandardOpenOption.WRITE)
        assert(Files.readAllLines(exists).asScala === List("def"))

        val nonExists = Files.createTempDirectory("dir").resolve("file")
        assertThrows[NoSuchFileException] {
          Files.write(nonExists, Array[Byte](97, 98, 99), StandardOpenOption.WRITE)
        }
      }

      "StandardOpenOption.APPEND" in {
        val tmp = Files.createTempFile("2", "")
        Files.write(tmp, Array[Byte](97, 98, 99), StandardOpenOption.APPEND)
        assert(Files.readAllLines(tmp).asScala === List("abc"))
        Files.write(tmp, Array[Byte](100, 101, 102), StandardOpenOption.APPEND)
        assert(Files.readAllLines(tmp).asScala === List("abcdef"))

        val nonExists = Files.createTempDirectory("dir").resolve("file")
        assertThrows[NoSuchFileException] {
          Files.write(nonExists, Array[Byte](97, 98, 99), StandardOpenOption.APPEND)
        }
      }

      "StandardOpenOption.TRUNCATE_EXISTING" in {
        val tmp = Files.createTempFile("2", "")
        Files.write(tmp, Array[Byte](97, 98, 99), StandardOpenOption.TRUNCATE_EXISTING)
        assert(Files.readAllLines(tmp).asScala === List("abc"))
        Files.write(tmp, Array[Byte](100, 101, 102), StandardOpenOption.TRUNCATE_EXISTING)
        assert(Files.readAllLines(tmp).asScala === List("def"))

        val nonExists = Files.createTempDirectory("dir").resolve("file")
        assertThrows[NoSuchFileException] {
          Files.write(nonExists, Array[Byte](97, 98, 99), StandardOpenOption.TRUNCATE_EXISTING)
        }
      }

      "StandardOpenOption.CREATE" in {
        val exists = Files.createTempFile("2", "")
        Files.write(exists, Array[Byte](97, 98, 99), StandardOpenOption.CREATE)
        assert(Files.readAllLines(exists).asScala === List("abc"))
        Files.write(exists, Array[Byte](100, 101, 102), StandardOpenOption.CREATE)
        assert(Files.readAllLines(exists).asScala === List("def"))

        val nonExists = Files.createTempDirectory("dir").resolve("file")
        Files.write(nonExists, Array[Byte](97, 98, 99), StandardOpenOption.CREATE)
        assert(Files.readAllLines(nonExists).asScala === List("abc"))

        // This option is ignored if the CREATE_NEW option is also set
        assertThrows[FileAlreadyExistsException] {
          Files.write(
            nonExists,
            Array[Byte](97, 98, 99),
            StandardOpenOption.CREATE,
            StandardOpenOption.CREATE_NEW
          )
        }
      }

      "StandardOpenOption.CREATE_NEW" in {
        val tmpDir    = Files.createTempDirectory("dir")
        val nonExists = tmpDir.resolve("file")
        Files.write(nonExists, Array[Byte](97, 98, 99), StandardOpenOption.CREATE_NEW)
        assert(Files.readAllLines(nonExists).asScala === List("abc"))

        val exists = Files.createTempFile("2", "")
        assertThrows[FileAlreadyExistsException] {
          Files.write(exists, Array.empty[Byte], StandardOpenOption.CREATE_NEW)
        }
      }

      "StandardOpenOption.DELETE_ON_CLOSE" in {
        val exists = Files.createTempFile("2", "")
        Files.write(exists, Array[Byte](97, 98, 99), StandardOpenOption.DELETE_ON_CLOSE)
        Files.notExists(exists)

        val nonExists = Files.createTempDirectory("dir").resolve("file")
        assertThrows[NoSuchFileException] {
          Files.write(nonExists, Array[Byte](97, 98, 99), StandardOpenOption.DELETE_ON_CLOSE)
        }
      }

      "StandardOpenOption.SPARSE" ignore {
        // MacOS HFS+ dos not support sparse file
      }

      "StandardOpenOption.SYNC" in {
        // same as write
        val exists = Files.createTempFile("2", "")
        Files.write(exists, Array[Byte](97, 98, 99), StandardOpenOption.SYNC)
        assert(Files.readAllLines(exists).asScala === List("abc"))
        Files.write(exists, Array[Byte](100, 101, 102), StandardOpenOption.SYNC)
        assert(Files.readAllLines(exists).asScala === List("def"))

        val nonExists = Files.createTempDirectory("dir").resolve("file")
        assertThrows[NoSuchFileException] {
          Files.write(nonExists, Array[Byte](97, 98, 99), StandardOpenOption.SYNC)
        }

        // todo: specific for sync
      }

      "StandardOpenOption.DSYNC" in {
        // same as write
        val exists = Files.createTempFile("2", "")
        Files.write(exists, Array[Byte](97, 98, 99), StandardOpenOption.DSYNC)
        assert(Files.readAllLines(exists).asScala === List("abc"))
        Files.write(exists, Array[Byte](100, 101, 102), StandardOpenOption.DSYNC)
        assert(Files.readAllLines(exists).asScala === List("def"))

        val nonExists = Files.createTempDirectory("dir").resolve("file")
        assertThrows[NoSuchFileException] {
          Files.write(nonExists, Array[Byte](97, 98, 99), StandardOpenOption.DSYNC)
        }
        // todo: specific for sync
      }
    }
  }
  "write(Path, JavaIterable[_ <: CharSequence], Charset, OpenOption*)" - {
    "default options" in {
      val tmpFile = Files.createTempFile("foo", ".txt")
      assert(Files.size(tmpFile) === 0)

      val written = Files.write(tmpFile, Seq("abc").asJava, utf16leCharset)
      assert(written === tmpFile)
      assert(Files.readAllLines(written, utf16leCharset).asScala.toSeq === Seq("abc"))
      assert(Files.size(tmpFile) === 8)
    }

    "StandardOpenOption.APPEND" in {
      val tmp = Files.createTempFile("2", "")
      assert(Files.write(tmp, Seq("abc").asJava, utf16leCharset, StandardOpenOption.APPEND) === tmp)
      assert(Files.readAllLines(tmp, utf16leCharset).asScala === List("abc"))
      assert(Files.write(tmp, Seq("def").asJava, utf16leCharset, StandardOpenOption.APPEND) === tmp)
      assert(Files.readAllLines(tmp, utf16leCharset).asScala === List("abc", "def"))

      val nonExists = Files.createTempDirectory("dir").resolve("file")
      assertThrows[NoSuchFileException] {
        Files.write(nonExists, Seq("a").asJava, utf16leCharset, StandardOpenOption.APPEND)
      }
    }
  }

  "write(Path, JavaIterable[_ <: CharSequence], OpenOption*)" - {
    "default options" in {
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

    "StandardOpenOption.APPEND" in {
      val tmp = Files.createTempFile("2", "")
      assert(Files.write(tmp, Seq("abc").asJava, StandardOpenOption.APPEND) === tmp)
      assert(Files.readAllLines(tmp).asScala === List("abc"))
      assert(Files.write(tmp, Seq("def").asJava, StandardOpenOption.APPEND) === tmp)
      assert(Files.readAllLines(tmp).asScala === List("abc", "def"))

      val nonExists = Files.createTempDirectory("dir").resolve("file")
      assertThrows[NoSuchFileException] {
        Files.write(nonExists, Seq("a").asJava, StandardOpenOption.APPEND)
      }
    }
  }

  "getLastModifiedTime" in {
    assertThrows[IOException] {
      Files.getLastModifiedTime(noSuchFile)
    }
    assert(Files.getLastModifiedTime(fileInSource).toMillis > 0)
  }
}

class BaseCountingPathCollector extends FileVisitor[Path] {
  val visited: ListBuffer[Path] = ListBuffer.empty

  private var visitFile: Int      = 0
  final def countVisitFile(): Int = visitFile

  private var visitFileFailed: Int      = 0
  final def countVisitFileFailed(): Int = visitFileFailed

  private var preVisitDirectory: Int      = 0
  final def countPreVisitDirectory(): Int = preVisitDirectory

  private var postVisitDirectory: Int      = 0
  final def countPostVisitDirectory(): Int = postVisitDirectory

  final override def preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult = {
    try {
      visited.addOne(dir)
      preVisitDirectoryImpl(dir, attrs)
    } finally {
      preVisitDirectory += 1
    }
  }

  protected def preVisitDirectoryImpl(dir: Path, attrs: BasicFileAttributes): FileVisitResult =
    FileVisitResult.CONTINUE

  final override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
    try {
      visited.addOne(file)
      visitFileImpl(file, attrs)
    } finally {
      visitFile += 1
    }
  }

  protected def visitFileImpl(file: Path, attrs: BasicFileAttributes): FileVisitResult =
    FileVisitResult.CONTINUE

  final override def visitFileFailed(file: Path, exc: IOException): FileVisitResult = {
    try {
      visited.addOne(file)
      FileVisitResult.CONTINUE
    } finally {
      visitFileFailed += 1
    }
  }

  protected def visitFileFailedImpl(file: Path, exc: IOException): FileVisitResult =
    FileVisitResult.CONTINUE

  final override def postVisitDirectory(dir: Path, exc: IOException): FileVisitResult = {
    try {
      visited.addOne(dir)
      postVisitDirectoryImpl(dir, exc)
    } finally {
      postVisitDirectory += 1
    }
  }

  protected def postVisitDirectoryImpl(dir: Path, exc: IOException): FileVisitResult =
    FileVisitResult.CONTINUE
}

class ConstantFileAttributes(val name: String) extends FileAttribute[Boolean] {
  override def value(): Boolean = true
}

class FilePermissions(perms: String) extends FileAttribute[JavaSet[PosixFilePermission]] {
  override def name(): String = "posix:permissions"

  override def value(): JavaSet[PosixFilePermission] = PosixFilePermissions.fromString(perms)
}
