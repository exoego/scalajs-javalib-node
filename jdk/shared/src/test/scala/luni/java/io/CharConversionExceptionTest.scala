package luni.java.io

import java.io.CharConversionException

import org.scalatest.FunSuite

class CharConversionExceptionTest extends FunSuite {

  test("Constructor") {
    val ex = intercept[CharConversionException] {
      throw new CharConversionException()
    }
    assert(ex.getMessage == null)
  }

  test("ConstructorLjava_lang_String") {
    val message = "Blah"
    val ex = intercept[CharConversionException] {
      throw new CharConversionException(message)
    }
    assert(ex.getMessage == message)
  }

}
