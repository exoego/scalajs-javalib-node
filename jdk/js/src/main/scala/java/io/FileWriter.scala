package java.io

import java.nio.charset.Charset

class FileWriter(file: File, charset: Charset, append: Boolean)
    extends OutputStreamWriter(new FileOutputStream(file, append), charset) {

  def this(file: File) = {
    this(file, Charset.defaultCharset(), false)
  }

  def this(file: File, append: Boolean) = {
    this(file, Charset.defaultCharset(), append)
  }

  def this(file: File, charset: Charset) = {
    this(file, charset, false)
  }

  def this(fileName: String) = {
    this(new File(fileName), Charset.defaultCharset(), false)
  }

  def this(fileName: String, charset: Charset) = {
    this(new File(fileName), charset, false)
  }

  def this(fileName: String, append: Boolean) = {
    this(new File(fileName), Charset.defaultCharset, append)
  }

  def this(fileName: String, charset: Charset, append: Boolean) = {
    this(new File(fileName), charset, append)
  }
}
