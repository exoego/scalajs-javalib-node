package luni.java.io

import java.io.CharConversionException

import org.scalatest.freespec.AnyFreeSpec

class CharConversionExceptionTest extends AnyFreeSpec {

  "Constructor" in {
    val ex = intercept[CharConversionException] {
      throw new CharConversionException()
    }
    assert(ex.getMessage === null)
  }

  "ConstructorLjava_lang_String" in {
    val message = "Blah"
    val ex = intercept[CharConversionException] {
      throw new CharConversionException(message)
    }
    assert(ex.getMessage === message)
  }

}
