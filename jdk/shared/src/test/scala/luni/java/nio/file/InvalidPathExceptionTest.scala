package luni.java.nio.file

import org.scalatest.freespec.AnyFreeSpec

import java.nio.file.InvalidPathException

class InvalidPathExceptionTest extends AnyFreeSpec {
  "no index in message when index is not specified or -1" in {
    val e = new InvalidPathException("i", "r")
    assert(
      e.getIndex === -1 && e.getInput === "i" && e.getReason === "r" && e.getMessage === "r: i"
    )

    val e2 = new InvalidPathException("i", "r", -1)
    assert(
      e2.getIndex === -1 && e2.getInput === "i" && e2.getReason === "r" && e2.getMessage === "r: i"
    )
  }

  "show index in message when index is >= 0" in {
    val e = new InvalidPathException("i", "r", 0)
    assert(
      e.getIndex === 0 && e.getInput === "i" && e.getReason === "r" && e.getMessage === "r at index 0: i"
    )
  }

  "throw NPE" in {
    assertThrows[NullPointerException](new InvalidPathException(null, "r"))
    assertThrows[NullPointerException](new InvalidPathException("i", null))
    assertThrows[NullPointerException](new InvalidPathException(null, "r", 1))
    assertThrows[NullPointerException](new InvalidPathException("i", null, 1))
    assertThrows[NullPointerException](new InvalidPathException(null, "r", -2))
    assertThrows[NullPointerException](new InvalidPathException("i", null, -2))
  }

  "throw IAE on index less than -1" in {
    assertThrows[IllegalArgumentException](new InvalidPathException("i", "r", -2))
    assertThrows[IllegalArgumentException](new InvalidPathException("i", "r", Int.MinValue))
  }
}
