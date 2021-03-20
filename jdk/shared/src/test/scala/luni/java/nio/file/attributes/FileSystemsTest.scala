package luni.java.nio.file.attributes

import org.scalatest.freespec.AnyFreeSpec

import java.net.URI
import java.nio.file.FileSystems

class FileSystemsTest extends AnyFreeSpec {

  "getDefault" in {
    assert(FileSystems.getDefault !== null)
    assert(FileSystems.getDefault.isOpen, "always open")
    assert(FileSystems.getDefault.isReadOnly === false, "always writable")
    assert(FileSystems.getDefault.provider().getScheme === "file")
  }

  "getFileSystem(URI)" in {
    assert(FileSystems.getFileSystem(new URI("file:/")) === FileSystems.getDefault)
    assert(FileSystems.getFileSystem(new URI("file:/")).isOpen)
    assert(FileSystems.getFileSystem(new URI("file:/")).provider().getScheme === "file")
  }

  "newFileSystem(URI)" ignore {}

  "newFileSystem(Path)" ignore {}
}
