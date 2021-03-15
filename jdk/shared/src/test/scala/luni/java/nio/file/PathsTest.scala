package luni.java.nio.file

import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.freespec.AnyFreeSpec

import java.nio.file.{InvalidPathException, Paths}

class PathsTest extends AnyFreeSpec {

  "get(String, String...)" - {
    "converts first to path" in {
      assert(Paths.get("first") === Paths.get("first"))
      assert(Paths.get("first") !== Paths.get("other"))
      assert(Paths.get("/first") !== Paths.get("other"))
      assert(Paths.get("./first") !== Paths.get("other"))
    }

    "joins a sequence of path" in {
      assert(Paths.get("first", "second") === Paths.get("first", "second"))
      assert(Paths.get("first", "second") !== Paths.get("first"))
      assert(Paths.get("first", "second") !== Paths.get("second"))
      assert(Paths.get("first", "second") !== Paths.get("other", "second"))
      assert(Paths.get("first", "second") !== Paths.get("first", "other"))

      assert(Paths.get("first", "second", "third") === Paths.get("first", "second", "third"))
      assert(Paths.get("first", "second", "third") !== Paths.get("first"))
      assert(Paths.get("first", "second", "third") !== Paths.get("third"))
      assert(Paths.get("first", "second", "third") !== Paths.get("first", "second", "other"))
      assert(Paths.get("first", "second", "third") !== Paths.get("first", "other", "three"))
      assert(Paths.get("first", "second", "third") !== Paths.get("other", "second", "three"))
    }

    "each elements can contain separator" in {
      assert(Paths.get("first", "/second", "third") === Paths.get("first", "second", "third"))
      assert(Paths.get("first", "/second", "/third") === Paths.get("first", "second", "third"))
      assert(Paths.get("first", "second", "/third") === Paths.get("first/second/third"))
    }

    "If more is empty, then first is the path string to convert." in {
      assert(Paths.get("first", Seq.empty[String]: _*) === Paths.get("first"))
      assert(Paths.get("first/second", Seq.empty[String]: _*) === Paths.get("first/second"))
      assert(Paths.get("first/second", Seq.empty[String]: _*) === Paths.get("first", "second"))
    }

    "empty path is ignored" in {
      assert(Paths.get("", "second") === Paths.get("second"))
      assert(Paths.get("first", "") === Paths.get("first"))
      assert(Paths.get("first", "second", "") === Paths.get("first/second"))
      assert(Paths.get("first", "", "second") === Paths.get("first/second"))
      assert(Paths.get("first", "second", "", "third") === Paths.get("first/second/third"))
    }

    "sequence of slash are compacted" in {
      assert(Paths.get("//////") === Paths.get("/"))
      assert(Paths.get("///foo///bar///") === Paths.get("/foo/bar"))
      assert(Paths.get("///foo///", "///bar///") === Paths.get("/foo/bar"))
    }

    "null not allowed" in {
      assertThrows[Exception] {
        Paths.get(null.asInstanceOf[String])
      }
      assertThrows[Exception] {
        Paths.get("first", null)
      }
      assertThrows[Exception] {
        Paths.get("first", "second", null)
      }
    }

    "empty allowed" in {
      assert(Paths.get("") === Paths.get(""))
      assert(Paths.get("", "") === Paths.get(""))
      assert(Paths.get("", "", "") === Paths.get(""))
    }

    "throw InvalidPathException" in {
      val invalidPaths = Table(
        "invalid path",
        "\u0000",
        "foo\u0000bar"
      )
      forAll(invalidPaths) { (path: String) =>
        assertThrows[InvalidPathException](Paths.get(path))
      }
    }
  }

  "get(URL)" ignore {}
}
