package luni.java.io

import org.scalatest.freespec.AnyFreeSpec

import java.io.InterruptedIOException

class InterruptedIOExceptionTest extends AnyFreeSpec {

  "Constructor" in {
    val ex = intercept[InterruptedIOException] {
      throw new InterruptedIOException()
    }
    assert(ex.getMessage === null)
  }

  "ConstructorLjava_lang_String" in {
    val ex = intercept[InterruptedIOException] {
      throw new InterruptedIOException("Some error message")
    }
    assert(ex.getMessage === "Some error message")
  }

}
