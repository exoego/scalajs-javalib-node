package java.nio.file.attribute

import java.time.Instant
import java.util.concurrent.TimeUnit
import scalajs.js

final class FileTime private (val nanos: Long) extends Comparable[FileTime] {
  private lazy val hash = nanos.hashCode()
  private lazy val stringRep =
    new js.Date((nanos / 1000000L).toDouble).toISOString().replace(".000", "")

  override def compareTo(o: FileTime): Int = {
    if (this.nanos == o.nanos) {
      0
    } else if (this.nanos > o.nanos) {
      1
    } else {
      -1
    }
  }

  override def equals(obj: Any): Boolean =
    obj match {
      case other: FileTime => this.nanos == other.nanos
      case _               => false
    }

  override def hashCode(): Int = hash

  override def toString: String = stringRep

  def to(unit: TimeUnit): Long = unit match {
    case TimeUnit.NANOSECONDS  => nanos
    case TimeUnit.MICROSECONDS => nanos / 1000L
    case TimeUnit.MILLISECONDS => nanos / 1000000L
    case TimeUnit.SECONDS      => nanos / 1000000000L
    case TimeUnit.MINUTES      => nanos / 60000000000L
    case TimeUnit.HOURS        => nanos / 3600000000000L
    case TimeUnit.DAYS         => nanos / 86400000000000L
  }

  def toMillis(): Long = nanos / 1000000L
}

object FileTime {
  def from(instant: Instant): FileTime = ???

  def from(value: Long, unit: TimeUnit): FileTime = unit match {
    case TimeUnit.NANOSECONDS  => new FileTime(value)
    case TimeUnit.MICROSECONDS => new FileTime(value * 1000L)
    case TimeUnit.MILLISECONDS => new FileTime(value * 1000000L)
    case TimeUnit.SECONDS      => new FileTime(value * 1000000000L)
    case TimeUnit.MINUTES      => new FileTime(value * 60000000000L)
    case TimeUnit.HOURS        => new FileTime(value * 3600000000000L)
    case TimeUnit.DAYS         => new FileTime(value * 86400000000000L)
  }

  def fromMillis(value: Long): FileTime = new FileTime(value * 1000000L)
}
