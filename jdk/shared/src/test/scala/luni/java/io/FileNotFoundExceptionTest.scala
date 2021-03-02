package luni.java.io

import java.io.{FileNotFoundException, IOException}

import org.scalatest.freespec.AnyFreeSpec

class FileNotFoundExceptionTest extends AnyFreeSpec {

  "Constructor" in {
    val ex = intercept[FileNotFoundException] {
      throw new FileNotFoundException()
    }
    assert(ex.getMessage === null)
  }

  "ConstructorLjava_lang_String" in {
    val message = "Cannot found file: 9://0//l"
    val ex = intercept[FileNotFoundException] {
      throw new FileNotFoundException(message)
    }
    assert(ex.getMessage === message)
  }

}
