package luni.java.io

import org.scalatest.funsuite.AnyFunSuite

import java.io.IOException

class IOExceptionTest extends AnyFunSuite {

  test("Constructor") {
    val ex = intercept[IOException] {
      throw new IOException()
    }
    assert(ex.getMessage === null)
  }

  test("ConstructorLjava_lang_String") {
    val ex = intercept[IOException] {
      throw new IOException("Some error message")
    }
    assert(ex.getMessage === "Some error message")
  }

}
