package luni.java.io

import java.io.CharConversionException

import org.scalatest.funsuite.AnyFunSuite

class CharConversionExceptionTest extends AnyFunSuite {

  test("Constructor") {
    val ex = intercept[CharConversionException] {
      throw new CharConversionException()
    }
    assert(ex.getMessage === null)
  }

  test("ConstructorLjava_lang_String") {
    val message = "Blah"
    val ex = intercept[CharConversionException] {
      throw new CharConversionException(message)
    }
    assert(ex.getMessage === message)
  }

}
