package luni.java.io

import java.io.{ByteArrayOutputStream, OutputStreamWriter, UnsupportedEncodingException}

import org.scalatest.freespec.AnyFreeSpec

class UnsupportedEncodingExceptionTest extends AnyFreeSpec {

  "Constructor" in {
    val ex = intercept[UnsupportedEncodingException] {
      new OutputStreamWriter(new ByteArrayOutputStream(), "BogusEncoding")
    }
    assert(ex.getMessage === "BogusEncoding")
  }

  "ConstructorLjava_lang_String" in {
    val message = null
    val ex = intercept[UnsupportedEncodingException] {
      throw new UnsupportedEncodingException(message)
    }
    assert(ex.getMessage === message)
  }

}
