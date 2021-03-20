package luni.java.nio.file.attributes

import org.scalatest.freespec.AnyFreeSpec

import java.nio.file.attribute.FileTime
import java.util.concurrent.TimeUnit._

class FileTimeTest extends AnyFreeSpec {
  "from(Instant)" ignore {
    // need scala-java-time
  }

  "toInstant" ignore {
    // need scala-java-time
  }

  "from(Long,TimeUnit)" in {
    assert(FileTime.from(1, NANOSECONDS).to(NANOSECONDS) === 1L)
    assert(FileTime.from(1, NANOSECONDS).to(MICROSECONDS) === 0L)

    assert(FileTime.from(1, MICROSECONDS).to(NANOSECONDS) === 1000L)
    assert(FileTime.from(1, MICROSECONDS).to(MICROSECONDS) === 1L)
    assert(FileTime.from(1, MICROSECONDS).to(MILLISECONDS) === 0L)

    assert(FileTime.from(1, MILLISECONDS).to(NANOSECONDS) === 1000000L)
    assert(FileTime.from(1, MILLISECONDS).to(MICROSECONDS) === 1000L)
    assert(FileTime.from(1, MILLISECONDS).to(MILLISECONDS) === 1L)
    assert(FileTime.from(1, MILLISECONDS).to(SECONDS) === 0L)

    assert(FileTime.from(1, SECONDS).to(NANOSECONDS) === 1000000000L)
    assert(FileTime.from(1, SECONDS).to(MICROSECONDS) === 1000000L)
    assert(FileTime.from(1, SECONDS).to(MILLISECONDS) === 1000L)
    assert(FileTime.from(1, SECONDS).to(SECONDS) === 1L)
    assert(FileTime.from(1, SECONDS).to(MINUTES) === 0L)

    assert(FileTime.from(1, MINUTES).to(NANOSECONDS) === 60L * 1000000000L)
    assert(FileTime.from(1, MINUTES).to(MICROSECONDS) === 60L * 1000000L)
    assert(FileTime.from(1, MINUTES).to(MILLISECONDS) === 60L * 1000L)
    assert(FileTime.from(1, MINUTES).to(SECONDS) === 60L)
    assert(FileTime.from(1, MINUTES).to(MINUTES) === 1L)
    assert(FileTime.from(1, MINUTES).to(HOURS) === 0L)

    assert(FileTime.from(1, HOURS).to(NANOSECONDS) === 60L * 60L * 1000000000L)
    assert(FileTime.from(1, HOURS).to(MICROSECONDS) === 60L * 60L * 1000000L)
    assert(FileTime.from(1, HOURS).to(MILLISECONDS) === 60L * 60L * 1000L)
    assert(FileTime.from(1, HOURS).to(SECONDS) === 60L * 60L)
    assert(FileTime.from(1, HOURS).to(MINUTES) === 60L)
    assert(FileTime.from(1, HOURS).to(HOURS) === 1L)
    assert(FileTime.from(1, HOURS).to(DAYS) === 0L)

    assert(FileTime.from(1, DAYS).to(NANOSECONDS) === 24L * 60L * 60L * 1000000000L)
    assert(FileTime.from(1, DAYS).to(MICROSECONDS) === 24L * 60L * 60L * 1000000L)
    assert(FileTime.from(1, DAYS).to(MILLISECONDS) === 24L * 60L * 60L * 1000L)
    assert(FileTime.from(1, DAYS).to(SECONDS) === 24L * 60L * 60L)
    assert(FileTime.from(1, DAYS).to(MINUTES) === 24L * 60L)
    assert(FileTime.from(1, DAYS).to(HOURS) === 24L)
    assert(FileTime.from(1, DAYS).to(DAYS) === 1L)
    assert(FileTime.from(365, DAYS).to(DAYS) === 365L)
  }

  "fromMillis(Long)" in {
    assert(FileTime.fromMillis(1).toMillis === 1)
    assert(FileTime.fromMillis(0).toMillis === 0)
  }

  "compareTo" in {
    val zero1 = FileTime.fromMillis(0)
    val zero2 = FileTime.fromMillis(0)
    val one   = FileTime.fromMillis(1)
    val two   = FileTime.fromMillis(2)

    assert(zero1.compareTo(zero2) === 0)
    assert(zero2.compareTo(zero1) === 0)

    assert(zero1.compareTo(one) < 0)
    assert(one.compareTo(zero1) > 0)

    assert(zero1.compareTo(two) < 0)
    assert(two.compareTo(zero1) > 0)

    assert(one.compareTo(two) < 0)
    assert(two.compareTo(one) > 0)
  }

  "equals" in {
    val zero1 = FileTime.fromMillis(0)
    val zero2 = FileTime.fromMillis(0)
    val one   = FileTime.fromMillis(1)

    // Reflexive
    assert(zero1 === zero1)
    assert(zero2 === zero2)
    assert(one === one)

    // Symmetric
    assert(zero1 === zero2)
    assert(zero2 === zero1)
    assert(one !== zero1)
    assert(zero1 !== one)

    // Transitive
    val zero3 = FileTime.fromMillis(0)
    assert(zero1 === zero2 && zero2 === zero3 && zero3 === zero1)
  }

  "hashCode" in {
    val zero1 = FileTime.fromMillis(0)
    val zero2 = FileTime.fromMillis(0)
    val one   = FileTime.fromMillis(1)

    // Reflexive
    assert(zero1.hashCode() === zero1.hashCode())
    assert(zero2.hashCode() === zero2.hashCode())
    assert(one.hashCode() === one.hashCode())

    // Symmetric
    assert(zero1.hashCode() === zero2.hashCode())
    assert(one.hashCode() !== zero1.hashCode())

    // Transitive
    val zero3 = FileTime.fromMillis(0)
    assert(zero1 === zero2 && zero2 === zero3 && zero3 === zero1)
  }

  "toString" in {
    assert(FileTime.fromMillis(0).toString === "1970-01-01T00:00:00Z")
    assert(FileTime.fromMillis(1).toString === "1970-01-01T00:00:00.001Z")
    assert(FileTime.fromMillis(1234567890123L).toString === "2009-02-13T23:31:30.123Z")
  }

  "to(TimeUnit)" in {
    assert(FileTime.fromMillis(1234567890L).to(NANOSECONDS) === 1234567890000000L)
    assert(FileTime.fromMillis(1234567890L).to(MICROSECONDS) === 1234567890000L)
    assert(FileTime.fromMillis(1234567890L).to(MILLISECONDS) === 1234567890L)
    assert(FileTime.fromMillis(1234567890L).to(SECONDS) === 1234567L)
    assert(FileTime.fromMillis(1234567890L).to(MINUTES) === 20576L)
    assert(FileTime.fromMillis(1234567890L).to(HOURS) === 342)
    assert(FileTime.fromMillis(1234567890L).to(DAYS) === 14)
  }

  "toMillis" in {
    assert(FileTime.fromMillis(1000).toMillis === 1000L)
    assert(FileTime.fromMillis(1000000).toMillis === 1000000L)
  }
}
