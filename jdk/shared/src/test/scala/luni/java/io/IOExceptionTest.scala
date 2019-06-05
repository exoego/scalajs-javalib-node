package luni.java.io

import java.io.IOException

import org.scalatest.FunSuite

class IOExceptionTest extends FunSuite {

  test("Constructor") {
    val ex = intercept[IOException] {
      throw new IOException()
    }
    assert(ex.getMessage == null)
  }

  test("ConstructorLjava_lang_String") {
    val ex = intercept[IOException] {
      throw new IOException("Some error message")
    }
    assert(ex.getMessage == "Some error message")
  }

}
