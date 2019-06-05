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

class CharArrayReader(protected var buf: Array[Char], offset: Int, length: Int) extends Reader {
  if (offset < 0 || offset > buf.length || length < 0 || offset + length < 0)
    throw new IllegalArgumentException

  protected var pos: Int       = offset
  protected var markedPos: Int = offset
  protected var count: Int     = if (offset + length < buf.length) length else buf.length

  def this(buf: Array[Char]) {
    this(buf, 0, buf.length)
  }

  @Override def close(): Unit = {
    if (!isClosed) buf = null
  }

  @inline private def isClosed = buf == null

  override def mark(readLimit: Int): Unit = {
    ensureOpen()
    markedPos = pos
  }

  override val markSupported = true

  override def read: Int = {
    ensureOpen()
    if (pos == count) -1
    else {
      pos += 1
      buf(pos - 1)
    }
  }

  def read(buffer: Array[Char], offset: Int, len: Int): Int = {
    if (offset < 0 || offset > buffer.length) throw new IndexOutOfBoundsException()
    if (len < 0 || len > buffer.length - offset) throw new IndexOutOfBoundsException()
    ensureOpen()
    if (pos < this.count) {
      val bytesRead =
        if (pos + len > this.count) this.count - pos
        else len
      System.arraycopy(this.buf, pos, buffer, offset, bytesRead)
      pos += bytesRead
      bytesRead
    } else {
      -1
    }
  }

  override def ready: Boolean = {
    ensureOpen()
    pos != count
  }

  override def reset(): Unit = {
    ensureOpen()
    pos = if (markedPos != -1) markedPos else 0
  }

  override def skip(n: Long): Long = {
    ensureOpen()
    if (n <= 0) return 0
    var skipped = 0L
    if (n < this.count - pos) {
      pos = pos + n.toInt
      skipped = n
    } else {
      skipped = this.count - pos
      pos = this.count
    }
    skipped
  }

  private def ensureOpen(): Unit = {
    if (isClosed) throw new IOException()
  }
}
