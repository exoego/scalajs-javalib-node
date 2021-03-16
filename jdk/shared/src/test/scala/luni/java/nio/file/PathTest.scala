package luni.java.nio.file

import org.scalatest.freespec.AnyFreeSpec

import java.nio.file.{Path, Paths}
import scala.jdk.CollectionConverters._

class PathTest extends AnyFreeSpec {

  private def Path(first: String, more: String*): Path = Paths.get(first, more: _*)

  "compareTo(Path)" in {
    assert(Path("a").compareTo(Path("a")) === 0)
    assert(Path("a/b").compareTo(Path("a", "b")) === 0)

    assert(Path("a").compareTo(Path("b")) < 0)
    assert(Path("./a").compareTo(Path("a")) < 0)
    assert(Path("/a").compareTo(Path("a")) < 0)

    assert(Path("b").compareTo(Path("a")) > 0)
    assert(Path("a/b").compareTo(Path("a")) > 0)
  }

  "iterator" in {
    assert(Path("/").iterator().asScala.toSeq === Seq.empty)
    assert(Path("").iterator().asScala.toSeq === Seq("").map(Path(_)))
    assert(Path("a").iterator().asScala.toSeq === Seq("a").map(Path(_)))
    assert(Path("/a/b/c").iterator().asScala.toSeq === Seq("a", "b", "c").map(Path(_)))
    assert(Path("a/b/c").iterator().asScala.toSeq === Seq("a", "b", "c").map(Path(_)))
    assert(Path("a/b/c/").iterator().asScala.toSeq === Seq("a", "b", "c").map(Path(_)))
    assert(Path("a/b/../c").iterator().asScala.toSeq === Seq("a", "b", "..", "c").map(Path(_)))
    assert(Path("a/b/./c").iterator().asScala.toSeq === Seq("a", "b", ".", "c").map(Path(_)))
  }

  "endsWith(Path)" ignore {}
  "equals(Path)" ignore {}
  "getFileName()" in {
    assert(Path("foo.md").getFileName === Path("foo.md"))
    assert(Path("bar", "foo.md").getFileName === Path("foo.md"))
    assert(Path("bar/foo.md").getFileName === Path("foo.md"))
    assert(Path("bar/../foo.md").getFileName === Path("foo.md"))
    assert(Path("bar/../").getFileName === Path(".."))
    assert(Path("").getFileName === Path(""))
  }
  "getFileSystem()" ignore {}
  "getName(index)" ignore {}
  "getNameCount()" in {
    assert(Path("/").getNameCount === 0)
    assert(Path("///").getNameCount === 0)

    assert(Path("foo.md").getNameCount === 1)
    assert(Path("bar", "foo.md").getNameCount === 2)
    assert(Path("bar/foo.md").getNameCount === 2)
    assert(Path("bar/../foo.md").getNameCount === 3)
    assert(Path("bar/../").getNameCount === 2)
    assert(Path("").getNameCount === 1)
    assert(Path("./").getNameCount === 1)
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
  "toString()" in {
    assert(Path("").toString === "")
    assert(Path("/").toString === "/")
    assert(Path("/////").toString === "/")
    assert(Path("a").toString === "a")
    assert(Path("a/").toString === "a")
    assert(Path("a/").toString === "a")
    assert(Path("a/b").toString === "a/b")
    assert(Path("a///////b").toString === "a/b")
    assert(Path("a///////b///////").toString === "a/b")
    assert(Path("/////a///////b///////").toString === "/a/b")

    assert(Path("/////a", "///////b///////").toString === "/a/b")
    assert(Path("/////a", "///////b///////", "///").toString === "/a/b")

    assert(Path("a/b/c/../d").toString === "a/b/c/../d")
  }
  "toURI()" ignore {}
}
