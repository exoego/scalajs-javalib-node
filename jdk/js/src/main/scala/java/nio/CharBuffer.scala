package java.nio

import java.util.stream.IntStream

abstract class CharBuffer
    extends Appendable
    with Readable
    with CharSequence
    with Comparable[CharBuffer] {
  def asReadOnlyBuffer(): CharBuffer = ???

  def compact(): CharBuffer = ???

  def duplicate(): CharBuffer = ???

  def get(): Char = ???

  def get(index: Int): Char = ???

  def isDirect(): Boolean = ???

  def order(): ByteOrder = ???

  def put(c: Char): CharBuffer = ???

  def put(index: Int, c: Char): CharBuffer = ???

  def slice(): CharBuffer = ???

  def subSequence(start: Int, end: Int): CharBuffer = ???

  def append(c: Char): CharBuffer = ???

  def append(csq: CharSequence): CharBuffer = ???

  def append(csq: CharSequence, start: Int, end: Int): CharBuffer = ???

  def array(): Array[Char] = ???

  def arrayOffset(): Int = ???

  def charAt(index: Int): Char = ???

  def get(dst: Array[Char]): CharBuffer = ???

  def get(dst: Array[Char], offset: Int, length: Int): CharBuffer = ???

  def hasArray(): Boolean = ???

  def length(): Int = ???

  def put(src: Array[Char]): CharBuffer = ???

  def put(src: Array[Char], offset: Int, length: Int): CharBuffer = ???

  def put(src: CharBuffer): CharBuffer = ???

  def put(src: String): CharBuffer = ???

  def put(src: String, offset: Int, length: Int): CharBuffer = ???

  def read(target: CharBuffer): Int = ???

  // Buffer
  def capacity(): Int                        = ???
  def clear(): CharBuffer                    = ???
  def flip(): CharBuffer                     = ???
  def hasRemaining(): Boolean                = ???
  def limit(): Int                           = ???
  def limit(newLimit: Int): CharBuffer       = ???
  def mark(): CharBuffer                     = ???
  def position(): Int                        = ???
  def position(newPosition: Int): CharBuffer = ???
  def remaining(): Int                       = ???
  def reset(): CharBuffer                    = ???
  def rewind(): Buffer                       = ???
}

object CharBuffer {
  def allocate(capacity: Int): CharBuffer                             = ???
  def wrap(array: Array[Char]): CharBuffer                            = ???
  def wrap(array: Array[Char], offset: Int, length: Int): CharBuffer  = ???
  def wrap(array: CharSequence): CharBuffer                           = ???
  def wrap(array: CharSequence, offset: Int, length: Int): CharBuffer = ???

}
