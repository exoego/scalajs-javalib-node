package luni.java.io

import java.io.{ ByteArrayOutputStream, OutputStreamWriter, UnsupportedEncodingException }

import org.scalatest.FunSuite

class UnsupportedEncodingExceptionTest extends FunSuite {

  test("Constructor") {
    val ex = intercept[UnsupportedEncodingException] {
      new OutputStreamWriter(new ByteArrayOutputStream(), "BogusEncoding")
    }
    assert(ex.getMessage == "BogusEncoding")
  }

  test("ConstructorLjava_lang_String") {
    val message = null
    val ex = intercept[UnsupportedEncodingException] {
      throw new UnsupportedEncodingException(message)
    }
    assert(ex.getMessage == message)
  }

}