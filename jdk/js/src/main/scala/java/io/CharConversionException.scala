package java.io

class CharConversionException(message: String) extends IOException(message) {

  def this() {
    this(null)
  }

}
