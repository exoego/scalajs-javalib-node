package luni.java.net

import org.scalatest.funsuite.AnyFunSuite

import java.net.URISyntaxException

class URISyntaxExceptionTest extends AnyFunSuite {
  test("ConstructorLjava_lang_StringLjava_lang_StringI") {
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

  test("ConstructorLjava_lang_StringLjava_lang_String") {
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

  test("getIndex") {
    // see constructor tests
  }

  test("getReason") {
    // see constructor tests
  }

  test("getInput") {
    // see constructor tests
  }

  test("getMessage") {
    var e = new URISyntaxException("str", "problem", 3)
    assert("problem at index 3: str" === e.getMessage)
    e = new URISyntaxException("str", "problem")
    assert("problem: str" === e.getMessage)
  }
}
