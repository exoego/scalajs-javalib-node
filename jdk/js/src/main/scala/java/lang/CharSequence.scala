package java.lang

trait CharSequence {
  def length(): scala.Int
  def charAt(index: scala.Int): scala.Char
  def subSequence(start: scala.Int, end: scala.Int): CharSequence
  def toString(): String
}

object CharSequence {
  def compare(left: CharSequence, right: CharSequence): Int = {
    left.toString.compareTo(right.toString)
  }
}
