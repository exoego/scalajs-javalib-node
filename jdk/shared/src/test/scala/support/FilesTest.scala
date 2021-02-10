package support

import org.scalatest.funsuite.AnyFunSuite

import java.nio.file.{Files, Paths}

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

  ignore("getPosixFilePermissions(Path, LinkOption*)") {}

  ignore("isDirectory(Path, LinkOption*)") {}

  ignore("isExecutable(Path)") {}

  ignore("isHidden(Path)") {}

  test("isReadable(Path)") {
    assert(Files.isReadable(Paths.get("README.md")))
    assert(Files.isReadable(Paths.get("./README.md")))
    assert(!Files.isReadable(Paths.get("no-such-file")))

    assert(Files.isReadable(Paths.get("project/build.properties")))
    assert(Files.isReadable(Paths.get("project/build.properties")))
    assert(Files.isReadable(Paths.get("project", "build.properties")))
    assert(!Files.isReadable(Paths.get("project", "no-such-file")))
  }

  ignore("isRegularFile(Path, LinkOption*)") {}

  ignore("isSameFile(Path, Path)") {}

  ignore("isSymbolicLink(Path)") {}

  ignore("isWritable(Path)") {}

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

  ignore("size(Path)") {}

  ignore("walk(Path, FileVisitOption*)") {}
  ignore("walk(Path, Int, FileVisitOption*)") {}

  ignore("walkFileTree(Path, FileVisitor[_ >: Path])") {}
  ignore("walkFileTree(Path, JavaSet[FileVisitOption], maxDepth:Int, FileVisitor[_ >: Path])") {}

  ignore("write(Path, Array[Byte], OpenOption*)") {}
  ignore("write(Path, JavaIterable[_ <: CharSequence], Charset, OpenOption*)") {}
  ignore("write(Path, JavaIterable[_ <: CharSequence], OpenOption*)") {}

}
