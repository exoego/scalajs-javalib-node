package luni.java.net

import org.scalatest.funsuite.AnyFunSuite

import java.net.{MalformedURLException, URL}

class MalformedURLExceptionTest extends AnyFunSuite {

  test("Constructor") {
    assertThrows[MalformedURLException] {
      new URL("notAProtocol://www.ibm.com")
    }
  }

  test("ConstructorLjava_lang_String") {
    val myString = "Gawsh!"
    val ex = intercept[MalformedURLException] {
      throw new MalformedURLException(myString)
    }
    assert(ex.getMessage == myString)
  }

}
