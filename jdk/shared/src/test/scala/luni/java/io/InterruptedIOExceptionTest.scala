package luni.java.io

import org.scalatest.funsuite.AnyFunSuite

import java.io.InterruptedIOException

class InterruptedIOExceptionTest extends AnyFunSuite {

  test("Constructor") {
    val ex = intercept[InterruptedIOException] {
      throw new InterruptedIOException()
    }
    assert(ex.getMessage == null)
  }

  test("ConstructorLjava_lang_String") {
    val ex = intercept[InterruptedIOException] {
      throw new InterruptedIOException("Some error message")
    }
    assert(ex.getMessage == "Some error message")
  }

}
