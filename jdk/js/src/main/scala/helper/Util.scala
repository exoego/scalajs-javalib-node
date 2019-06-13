package helper

import java.io.UTFDataFormatException

object Util {
  def convertFromUTF8(buf: Array[Byte], offset: Int, utfSize: Int): String =
    convertUTF8WithBuf(buf, new Array[Char](utfSize), offset, utfSize)

  def convertUTF8WithBuf(buf: Array[Byte], out: Array[Char], offset: Int, utfSize: Int): String = {
    var count = 0
    var s     = 0
    var a     = 0
    while (count < utfSize) {
      if ({
        out(s) = buf(offset + {
          count += 1; count - 1
        }).toChar
        out(s) < '\u0080'
      }) {
        s += 1; s - 1
      } else if ({
        a = out(s)
        (a & 0xe0) == 0xc0
      }) {
        if (count >= utfSize) throw new UTFDataFormatException()
        val b = buf({
          count += 1; count - 1
        })
        if ((b & 0xC0) != 0x80) throw new UTFDataFormatException()
        out({
          s += 1; s - 1
        }) = (((a & 0x1F) << 6) | (b & 0x3F)).toChar
      } else if ((a & 0xf0) == 0xe0) {
        if (count + 1 >= utfSize) throw new UTFDataFormatException()
        val b = buf({
          count += 1; count - 1
        })
        val c = buf({
          count += 1; count - 1
        })
        if (((b & 0xC0) != 0x80) || ((c & 0xC0) != 0x80)) throw new UTFDataFormatException()
        out({
          s += 1; s - 1
        }) = (((a & 0x0F) << 12) | ((b & 0x3F) << 6) | (c & 0x3F)).toChar
      } else throw new UTFDataFormatException()
    }
    new String(out, 0, s)
  }
}
