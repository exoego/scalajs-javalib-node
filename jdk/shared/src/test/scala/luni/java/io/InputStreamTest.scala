package luni.java.io

import java.io.InputStream

import org.scalatest.FunSuite

class InputStreamTest extends FunSuite {

  private val isScalaJS = System.getProperty("java.vm.name") == "Scala.js"

  test("Regression for HARMONY-4337") {
    val in = new MockInputStream
    if (isScalaJS) {
      assertThrows[IndexOutOfBoundsException] {
        in.read(null, -1, 1)
      }
    } else {
      assertThrows[NullPointerException] {
        in.read(null, -1, 1)
      }
    }
  }

  private[io] class MockInputStream extends InputStream {
    def read = 0
  }
}
