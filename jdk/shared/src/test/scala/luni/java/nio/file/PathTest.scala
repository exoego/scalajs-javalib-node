package luni.java.nio.file

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.prop.TableDrivenPropertyChecks._

import java.nio.file._
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

  "endsWith(Path | String)" in {
    val trueCombination = Table(
      ("left", "right"),
      (Path("/"), "/"),
      (Path("a"), "a"),
      (Path("a/b"), "b"),
      (Path("a/b/c"), "c"),
      (Path("a/b/c"), "b/c"),
      (Path("a/b/c"), "a/b/c"),
      (Path("/a/b/c/"), "c"),
      (Path("/a/b/../c/"), "b/../c")
    )
    forAll(trueCombination) { (left: Path, right: String) =>
      assert(left.endsWith(Path(right)))
      assert(left.endsWith(right))
    }

    val falseCombinations = Table(
      ("left", "right"),
      (Path("a"), "b"),
      (Path("a/b"), "a/x"),
      (Path("a/b"), "x/b"),
      (Path("a/b"), "x/a/b"),
      (Path("a/b/c"), "b"),
      (Path("a/b/c"), "a/b"),
      (Path("/a/b/c/"), "/"),
      (Path("/"), "/a")
    )
    forAll(falseCombinations) { (left: Path, right: String) =>
      assert(left.endsWith(Path(right)) === false)
      assert(left.endsWith(right) === false)
    }
  }

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
  "getName(index)" in {
    assert(Path("").getName(0) === Path(""))
    assert(Path("a").getName(0) === Path("a"))
    assert(Path("a/b").getName(0) === Path("a"))
    assert(Path("a/b").getName(1) === Path("b"))
    assert(Path("/a/b").getName(0) === Path("a"))
    assert(Path("/a/b").getName(1) === Path("b"))
    assertThrows[IllegalArgumentException] {
      Path("a/b").getName(2)
    }
    assertThrows[IllegalArgumentException] {
      Path("a/b").getName(-1)
    }
  }
  "getNameCount()" in {
    assert(Path("/").getNameCount === 0)
    assert(Path("///").getNameCount === 0)

    assert(Path("foo.md").getNameCount === 1)
    assert(Path("bar", "foo.md").getNameCount === 2)
    assert(Path("bar/foo.md").getNameCount === 2)
    assert(Path("bar/../foo.md").getNameCount === 3)
    assert(Path("bar/../").getNameCount === 2)
    assert(Path("/a").getNameCount === 1)
    assert(Path("/a/").getNameCount === 1)
    assert(Path("a/").getNameCount === 1)
    assert(Path("/a/b/c/").getNameCount === 3)
    assert(Path("a/b/c/").getNameCount === 3)
    assert(Path("a/b/c").getNameCount === 3)

    assert(Path("").getNameCount === 1)
    assert(Path("./").getNameCount === 1)
  }

  "getParent()" in {
    val table = Table(
      ("base", "parent"),
      (Path(""), null),
      (Path("a"), null),
      (Path("/a"), Path("/")),
      (Path("a/b"), Path("a")),
      (Path("a/b"), Path("a")),
      (Path("x/a/."), Path("x/a")),
      (Path("x/../b"), Path("x/..")),
      (Path("/a/b/c/"), Path("/a/b"))
    )
    forAll(table) { (base: Path, parent: Path) =>
      assert(base.getParent === parent)
    }
  }

  "getRoot()" in {
    assert(Path("").getRoot === null)
    assert(Path("a").getRoot === null)
    assert(Path("a/b/../c").getRoot === null)
    assert(Path("/a/b/../c").getRoot === Path("/"))
    assert(Path("/").getRoot === Path("/"))
  }

  "hashCode()" ignore {}

  "isAbsolute()" in {
    val absolutePaths = Table(
      "path",
      "/",
      "/a/b/c",
      "/a/../c",
      "/a/../."
    )
    forAll(absolutePaths) { path: String =>
      assert(Path(path).isAbsolute)
    }

    val notAbsolutPaths = Table(
      "path",
      "a/b/c",
      "a/../."
    )
    forAll(notAbsolutPaths) { path: String =>
      assert(Path(path).isAbsolute === false)
    }
  }

  "normalize()" ignore {}
  "register()" ignore {}
  "relativize(Path)" ignore {}
  "resolve(Path)" ignore {}

  "startsWith(Path | String)" in {
    val trueCombination = Table(
      ("left", "right"),
      (Path("/"), "/"),
      (Path("a"), "a"),
      (Path("a/b"), "a"),
      (Path("a/b/c"), "a/b"),
      (Path("a/b/c"), "a/b/c"),
      (Path("/a/b/c/"), "/a"),
      (Path("/a/b/../c/"), "/a/b/.."),
      (Path("/a/b/../c/"), "/a/b/../c")
    )
    forAll(trueCombination) { (left: Path, right: String) =>
      assert(left.startsWith(Path(right)))
      assert(left.startsWith(right))
    }

    val falseCombinations = Table(
      ("left", "right"),
      (Path("a"), "b"),
      (Path("a/b"), "a/b/x"),
      (Path("a/b"), "a/x"),
      (Path("a/b"), "x/b"),
      (Path("a/b/c"), "b"),
      (Path("a/b/c"), "b/c"),
      (Path("/a/b/c/"), "a"),
      (Path("/"), "/a")
    )
    forAll(falseCombinations) { (left: Path, right: String) =>
      assert(left.startsWith(Path(right)) === false)
      assert(left.startsWith(right) === false)
    }
  }

  "subath(begin,end)" - {
    "successful path" in {
      assert(Path("a/b/c").subpath(0, 1) === Path("a"))
      assert(Path("a/b/c").subpath(0, 2) === Path("a/b"))
      assert(Path("a/b/c").subpath(0, 3) === Path("a/b/c"))
      assert(Path("a/b/c").subpath(1, 2) === Path("b"))
      assert(Path("a/b/c").subpath(1, 3) === Path("b/c"))
      assert(Path("a/b/c").subpath(2, 3) === Path("c"))

      assert(Path("/a/b/c").subpath(0, 1) === Path("a"))
      assert(Path("/a/b/c").subpath(0, 2) === Path("a/b"))
      assert(Path("/a/b/c").subpath(0, 3) === Path("a/b/c"))
      assert(Path("/a/b/c").subpath(1, 2) === Path("b"))
      assert(Path("/a/b/c").subpath(1, 3) === Path("b/c"))
      assert(Path("/a/b/c").subpath(2, 3) === Path("c"))

      assert(Path("a/b/../c").subpath(0, 3) === Path("a/b/.."))
      assert(Path("a/b/../c").subpath(1, 4) === Path("b/../c"))
      assert(Path("a/b/.").subpath(0, 2) === Path("a/b"))
      assert(Path("a/b/.").subpath(1, 3) === Path("b/."))
    }

    "invalid beginIndex" in {
      assertThrows[IllegalArgumentException] {
        Path("a/b/c").subpath(-1, 0)
      }
      assertThrows[IllegalArgumentException] {
        Path("a/b/c").subpath(3, 3)
      }
      assertThrows[IllegalArgumentException] {
        Path("a/b/c").subpath(4, 4)
      }
    }

    "invalid endIndex" in {
      assertThrows[IllegalArgumentException] {
        Path("a/b/c").subpath(0, -2)
      }
      assertThrows[IllegalArgumentException] {
        Path("a/b/c").subpath(0, 0)
      }
      assertThrows[IllegalArgumentException] {
        Path("a/b/c").subpath(0, 4)
      }
    }
  }
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
