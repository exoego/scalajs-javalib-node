package luni.java.net

import org.scalatest.freespec.AnyFreeSpec

import java.net.{MalformedURLException, URL}

class MalformedURLExceptionTest extends AnyFreeSpec {

  "Constructor" in {
    assertThrows[MalformedURLException] {
      new URL("notAProtocol://www.ibm.com")
    }
  }

  "ConstructorLjava_lang_String" in {
    val myString = "Gawsh!"
    val ex = intercept[MalformedURLException] {
      throw new MalformedURLException(myString)
    }
    assert(ex.getMessage === myString)
  }

}
