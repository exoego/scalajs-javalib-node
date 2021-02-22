package luni.java.net

import org.scalatest.freespec.AnyFreeSpec

import java.net.URISyntaxException

class URISyntaxExceptionTest extends AnyFreeSpec {
  "ConstructorLjava_lang_StringLjava_lang_StringI" in {
    assertThrows[NullPointerException] {
      new URISyntaxException(null, "problem", 2)
    }
    assertThrows[NullPointerException] {
      new URISyntaxException("str", null, 2)
    }
    assertThrows[IllegalArgumentException] {
      new URISyntaxException("str", "problem", -2)
    }
    val e = new URISyntaxException("str", "problem", 2)
    assert("problem" === e.getReason)
    assert("str" === e.getInput)
    assert(2 === e.getIndex)
  }

  "ConstructorLjava_lang_StringLjava_lang_String" in {
    assertThrows[NullPointerException] {
      new URISyntaxException(null, "problem")
    }
    assertThrows[NullPointerException] {
      new URISyntaxException("str", null)
    }
    val e = new URISyntaxException("str", "problem")
    assert("problem" === e.getReason)
    assert("str" === e.getInput)
    assert(-1 === e.getIndex)
  }

  "getIndex" in {
    // see constructor tests
  }

  "getReason" in {
    // see constructor tests
  }

  "getInput" in {
    // see constructor tests
  }

  "getMessage" in {
    var e = new URISyntaxException("str", "problem", 3)
    assert("problem at index 3: str" === e.getMessage)
    e = new URISyntaxException("str", "problem")
    assert("problem: str" === e.getMessage)
  }
}
