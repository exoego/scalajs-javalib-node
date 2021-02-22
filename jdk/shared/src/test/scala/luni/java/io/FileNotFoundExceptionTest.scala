package luni.java.io

import java.io.{FileNotFoundException, IOException}

import org.scalatest.funsuite.AnyFunSuite

class FileNotFoundExceptionTest extends AnyFunSuite {

  test("Constructor") {
    val ex = intercept[FileNotFoundException] {
      throw new FileNotFoundException()
    }
    assert(ex.getMessage === null)
  }

  test("ConstructorLjava_lang_String") {
    val message = "Cannot found file: 9://0//l"
    val ex = intercept[FileNotFoundException] {
      throw new FileNotFoundException(message)
    }
    assert(ex.getMessage === message)
  }

}
