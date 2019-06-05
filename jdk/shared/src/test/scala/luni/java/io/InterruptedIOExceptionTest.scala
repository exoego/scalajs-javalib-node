package luni.java.io

import java.io.{ IOException, InterruptedIOException }

import org.scalatest.FunSuite

class InterruptedIOExceptionTest extends FunSuite {

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
