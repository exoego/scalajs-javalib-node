package luni.java.nio.file

import org.scalatest.freespec.AnyFreeSpec

import java.nio.file.Paths

class PathTest extends AnyFreeSpec {
  "compareTo(Path)" ignore {}
  "endsWith(Path)" ignore {}
  "equals(Path)" ignore {}
  "getFileName()" in {
    assert(Paths.get("foo.md").getFileName === Paths.get("foo.md"))
    assert(Paths.get("bar", "foo.md").getFileName === Paths.get("foo.md"))
    assert(Paths.get("bar/foo.md").getFileName === Paths.get("foo.md"))
    assert(Paths.get("bar/../foo.md").getFileName === Paths.get("foo.md"))
    assert(Paths.get("bar/../").getFileName === Paths.get(".."))
    assert(Paths.get("").getFileName === Paths.get(""))
  }
  "getFileSystem()" ignore {}
  "getName(index)" ignore {}
  "getNameCount()" ignore {
    assert(Paths.get("/").getNameCount === 0)
    assert(Paths.get("///").getNameCount === 0)

    assert(Paths.get("foo.md").getNameCount === 1)
    assert(Paths.get("bar", "foo.md").getNameCount === 2)
    assert(Paths.get("bar/foo.md").getNameCount === 2)
    assert(Paths.get("bar/../foo.md").getNameCount === 3)
    assert(Paths.get("bar/../").getNameCount === 2)
    assert(Paths.get("").getNameCount === 1)
    assert(Paths.get("./").getNameCount === 1)
  }
  "getParent()" ignore {}
  "getRoot()" ignore {}
  "hashCode()" ignore {}
  "isAbsolute()" ignore {}
  "normalize()" ignore {}
  "register()" ignore {}
  "relativize(Path)" ignore {}
  "resolve(Path)" ignore {}
  "startsWith(Path)" ignore {}
  "subPath(begin,end)" ignore {}
  "toAbsolutePath()" ignore {}
  "toRealPath(options)" ignore {}
  "toString()" ignore {}
  "toURI()" ignore {}
}
