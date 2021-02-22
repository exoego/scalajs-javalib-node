package luni.java.io

import org.scalatest.freespec.AnyFreeSpec

import java.io.IOException

class IOExceptionTest extends AnyFreeSpec {

  "Constructor" in {
    val ex = intercept[IOException] {
      throw new IOException()
    }
    assert(ex.getMessage === null)
  }

  "ConstructorLjava_lang_String" in {
    val ex = intercept[IOException] {
      throw new IOException("Some error message")
    }
    assert(ex.getMessage === "Some error message")
  }

}
