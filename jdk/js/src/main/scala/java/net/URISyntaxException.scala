package java.net

class URISyntaxException(
    private val input: String,
    private val reason: String,
    private val index: Int
) extends Exception({
      if (index < 0) s"$reason: $input" else s"$reason at index $index: $input"
    }) {
  if (input == null || reason == null) throw new NullPointerException
  if (index < -1) throw new IllegalArgumentException

  def this(input: String, reason: String) = this(input, reason, -1)

  def getIndex(): Int     = index
  def getInput(): String  = input
  def getReason(): String = reason

}
