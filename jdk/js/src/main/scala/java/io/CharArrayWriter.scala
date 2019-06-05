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

class CharArrayWriter(initialSize: Int) extends Writer {
  if (initialSize < 0) throw new IllegalArgumentException()

  private final val TOKEN_NULL: String = "null"

  protected var buf: Array[Char] = new Array[Char](initialSize)

  protected var count = 0

  def this() {
    this(32)
  }

  override def close(): Unit = {}

  private def expand(i: Int): Unit = {
    val newLen = Math.max(2 * buf.length, count + i)
    val newbuf = new Array[Char](newLen)
    System.arraycopy(buf, 0, newbuf, 0, count)
    buf = newbuf
  }

  @Override def flush(): Unit = {}
  def reset(): Unit           = { count = 0 }

  def size: Int = count

  def toCharArray: Array[Char] = {
    val result = new Array[Char](count)
    System.arraycopy(buf, 0, result, 0, count)
    result
  }

  override def toString: String = new String(buf, 0, count)

  override def write(c: Array[Char], offset: Int, len: Int): Unit = {
    // avoid int overflow
    if (offset < 0 || offset > c.length || len < 0 || len > c.length - offset)
      throw new IndexOutOfBoundsException
    expand(len)
    System.arraycopy(c, offset, this.buf, this.count, len)
    this.count += len
  }

  override def write(oneChar: Int): Unit = {
    expand(1)
    buf({ count += 1; count - 1 }) = oneChar.toChar
  }

  override def write(str: String, offset: Int, len: Int): Unit = {
    if (str == null) throw new NullPointerException()
    if (offset < 0 || offset > str.length || len < 0 || len > str.length - offset)
      throw new StringIndexOutOfBoundsException
    expand(len)
    str.getChars(offset, offset + len, buf, this.count)
    this.count += len

  }

  def writeTo(out: Writer): Unit = {
    out.write(buf, 0, count)
  }

  override def append(c: Char): CharArrayWriter = {
    write(c)
    this
  }

  override def append(csq: CharSequence): CharArrayWriter = {
    if (null == csq) append(TOKEN_NULL, 0, TOKEN_NULL.length)
    else append(csq, 0, csq.length)
    this
  }
  override def append(csq: CharSequence, start: Int, end: Int): CharArrayWriter = {
    val nonNullCsq = if (null == csq) TOKEN_NULL else csq
    val output     = nonNullCsq.subSequence(start, end).toString
    write(output, 0, output.length)
    this
  }
}
