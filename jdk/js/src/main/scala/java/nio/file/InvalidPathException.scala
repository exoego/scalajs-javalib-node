package java.nio.file

class InvalidPathException(input: String, reason: String, index: Int)
    extends IllegalArgumentException {
  if (input == null || reason == null) throw new NullPointerException
  if (index < -1) throw new IllegalArgumentException

  def this(input: String, reason: String) = this(input, reason, -1)

  def getIndex(): Int     = this.index
  def getInput(): String  = this.input
  def getReason(): String = this.reason

  override def getMessage: String = {
    val at = index match {
      case -1 => ""
      case _  => s" at index $index"
    }
    s"$reason$at: $input"
  }
}
