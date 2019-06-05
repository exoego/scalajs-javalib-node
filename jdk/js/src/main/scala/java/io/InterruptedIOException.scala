package java.io

class InterruptedIOException(message: String) extends IOException(message) {

  def this() {
    this(null)
  }

}
