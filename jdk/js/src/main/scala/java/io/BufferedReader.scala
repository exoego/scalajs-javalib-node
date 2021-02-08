/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.io
import java.lang.{StringBuilder => JStringBuilder}

import scala.annotation.tailrec
class BufferedReader(in: Reader, size: Int) extends Reader {
  private var buf       = new Array[Char](size)
  private var pos       = 0
  private var end       = 0
  private var mark      = -1
  private var markLimit = -1

  @inline private def isClosed = buf == null

  def this(in: Reader) = {
    this(in, 8192)
  }

  override def close(): Unit = {
    if (!isClosed) {
      in.close()
      buf = null
    }
  }

  private def fillBuf: Int = {
    if (mark == -1 || (pos - mark >= markLimit)) {
      /* mark isn't set or has exceeded its limit. use the whole buffer */
      val result = in.read(buf, 0, buf.length)
      if (result > 0) {
        mark = -1
        pos = 0
        end = result
      }
      return result
    }
    if (mark == 0 && markLimit > buf.length) {
      /* the only way to make room when mark=0 is by growing the buffer */
      var newLength = buf.length * 2
      if (newLength > markLimit) newLength = markLimit
      val newbuf = new Array[Char](newLength)
      System.arraycopy(buf, 0, newbuf, 0, buf.length)
      buf = newbuf
    } else if (mark > 0) {
      /* make room by shifting the buffered data to left mark positions */
      System.arraycopy(buf, mark, buf, 0, buf.length - mark)
      pos -= mark
      end -= mark
      mark = 0
    }
    /* Set the new position and mark position */
    val count = in.read(buf, pos, buf.length - pos)
    if (count != -1) end += count
    count
  }

  override val markSupported = true

  override def mark(markLimit: Int): Unit = {
    if (markLimit < 0) throw new IllegalArgumentException
    ensureOpen()
    this.markLimit = markLimit
    mark = pos
  }

  override def read(): Int = {
    ensureOpen()
    val isBufferedCharsAvailable = pos < end || fillBuf != -1
    if (isBufferedCharsAvailable)
      buf({ pos += 1; pos - 1 })
    else
      -1
  }

  def read(buffer: Array[Char], _offset: Int, length: Int): Int = {
    var offset = _offset
    ensureOpen()
    if (offset < 0 || {
          if (buffer == null) throw new NullPointerException
          offset > buffer.length - length || length < 0
        }) throw new IndexOutOfBoundsException

    @tailrec
    def calcOutstanding(outstanding_ : Int): Int = {
      if (outstanding_ <= 0) outstanding_
      else {
        var outstanding = outstanding_

        // If there are bytes in the buffer, grab those first.
        val available = end - pos
        if (available > 0) {
          val count =
            if (available >= outstanding) outstanding
            else available
          System.arraycopy(buf, pos, buffer, offset, count)
          pos += count
          offset += count
          outstanding -= count
        }

        /*
         * Before attempting to read from the underlying stream, make
         * sure we really, really want to. We won't bother if we're
         * done, or if we've already got some bytes and reading from the
         * underlying stream would block.
         */
        if (outstanding == 0 || (outstanding < length && !in.ready)) outstanding
        /*
         * If we're unmarked and the requested size is greater than our
         * buffer, read the bytes directly into the caller's buffer. We
         * don't read into smaller buffers because that could result in
         * a many reads.
         */
        else if ((mark == -1 || (pos - mark >= markLimit)) && outstanding >= buf.length) {
          val count = in.read(buffer, offset, outstanding)
          if (count > 0) {
            offset += count
            outstanding -= count
            mark = -1
          }
          outstanding
        } else if (fillBuf == -1) outstanding
        else calcOutstanding(outstanding)
      }
    }

    val outstanding = calcOutstanding(length)
    val count       = length - outstanding
    if (count > 0 || count == length) count
    else -1
  }

  final private[io] def chompNewline(): Unit = {
    if ((pos != end || fillBuf != -1) && buf(pos) == '\n')
      pos += 1
  }

  def readLine: String = {
    ensureOpen()
    /* has the underlying stream been exhausted? */
    if (pos == end && fillBuf == -1) return null
    var charPos = pos
    while (charPos < end) {
      val ch = buf(charPos)
      if (ch > '\r') {
        // continue
      } else {
        if (ch == '\n') {
          val res = new String(buf, pos, charPos - pos)
          pos = charPos + 1
          return res
        } else if (ch == '\r') {
          val res = new String(buf, pos, charPos - pos)
          pos = charPos + 1
          if (((pos < end) || (fillBuf != -1)) && (buf(pos) == '\n')) pos += 1
          return res
        }
      }
      charPos += 1
    }
    var eol    = '\u0000'
    val result = new JStringBuilder(80)
    /* Typical Line Length */
    result.append(buf, pos, end - pos)

    @tailrec
    def recRead(): String = {
      pos = end
      if (eol == '\n') return result.toString
      // attempt to fill buffer
      if (fillBuf == -1) {
        return if (result.length > 0 || eol != '\u0000') result.toString else null
      }
      var charPos = pos
      while (charPos < end) {
        val c = buf(charPos)
        if (eol == '\u0000') {
          if (c == '\n' || c == '\r') eol = c
          else if (eol == '\r' && c == '\n') {
            if (charPos > pos) result.append(buf, pos, charPos - pos - 1)
            pos = charPos + 1
            return result.toString
          } else {
            if (charPos > pos) result.append(buf, pos, charPos - pos - 1)
            pos = charPos
            return result.toString
          }
        }
        charPos += 1
      }
      if (eol == '\u0000') result.append(buf, pos, end - pos)
      else result.append(buf, pos, end - pos - 1)
      recRead()
    }
    recRead()
  }

  override def ready: Boolean = {
    ensureOpen()
    ((end - pos) > 0) || in.ready
  }

  override def reset(): Unit = {
    ensureOpen()
    if (mark == -1) throw new IOException()
    pos = mark
  }

  override def skip(amount: Long): Long = {
    if (amount < 0) throw new IllegalArgumentException()
    ensureOpen()
    if (amount < 1) return 0
    if (end - pos >= amount) {
      pos += amount.toInt
      return amount
    }
    var read = end - pos
    pos = end
    while (read < amount) {
      if (fillBuf == -1) return read
      if (end - pos >= amount - read) {
        pos += (amount - read).toInt
        return amount
      }
      // Couldn't get all the characters, skip what we read
      read += (end - pos)
      pos = end
    }
    amount
  }

  private def ensureOpen(): Unit = {
    if (isClosed) throw new IOException()
  }
}
