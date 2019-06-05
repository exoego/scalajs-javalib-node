package luni.java.net

import java.net.{MalformedURLException, URL}

import org.scalatest.FunSuite

class MalformedURLExceptionTest extends FunSuite {

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
