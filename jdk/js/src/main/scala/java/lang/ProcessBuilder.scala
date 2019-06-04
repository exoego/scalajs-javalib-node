package java.lang

import java.io.{File, FileOutputStream, IOException, InputStream, OutputStream}
import java.util.{Arrays => JArrays, List => JList, Map => JMap}

import io.scalajs.nodejs
import io.scalajs.nodejs.buffer.Buffer
import io.scalajs.nodejs.fs.Fs
import net.exoego.nodejs.child_process.SpawnOptions

import scala.scalajs.js
import scala.collection.JavaConverters._

object ProcessBuilder {

  abstract class Redirect {
    def file(): File
    def `type`(): Redirect.Type
    override def toString: String = `type`.toString

    override def equals(obj: Any): scala.Boolean = obj match {
      case other: Redirect => this.`type` == other.`type` && this.file() == other.file()
      case _               => false
    }

    override def hashCode(): Int = {
      this.`type`.hashCode() * 41 + this.file.hashCode()
    }
  }

  object Redirect {

    final class Type protected[lang] (name: String, ordinal: Int)
        extends Enum[Type](name, ordinal) {}
    object Type {
      val APPEND  = new Type("APPEND", 0)
      val INHERIT = new Type("INHERIT", 1)
      val PIPE    = new Type("PIPE", 2)
      val READ    = new Type("READ", 3)
      val WRITE   = new Type("WRITE", 4)
    }

    private class RedirectImpl(val file: File, val `type`: Type) extends Redirect
    val PIPE: Redirect    = new RedirectImpl(null, Type.PIPE)
    val DISCARD: Redirect = new RedirectImpl(new File("/dev", "null"), Type.WRITE)
    val INHERIT: Redirect = new RedirectImpl(null, Type.INHERIT)

    private class PrintableFileRedirect(val file: File, val `type`: Type, to: scala.Boolean)
        extends Redirect {
      override def toString: String =
        s"""redirect to ${`type`.toString
          .toLowerCase()} ${if (to) "to" else "from"} file "${file.getName}""""
    }
    def appendTo(file: File): Redirect = new PrintableFileRedirect(file, Type.APPEND, to = true)
    def from(file: File): Redirect     = new PrintableFileRedirect(file, Type.READ, to = false)
    def to(file: File): Redirect       = new PrintableFileRedirect(file, Type.WRITE, to = true)
  }
}

class ProcessBuilder(private[this] var args: JList[String]) {
  import ProcessBuilder._

  private var workingDir: File = _

  private var env: JMap[String, String] = _

  private var isRedirectError: scala.Boolean = false

  private var error: Redirect  = Redirect.PIPE
  private var input: Redirect  = Redirect.PIPE
  private var output: Redirect = Redirect.PIPE

  def this(args: Array[String]) {
    this(JArrays.asList(args: _*))
  }

  def this(args: String*) {
    this(JArrays.asList(args: _*))
  }

  def inheritIO(): ProcessBuilder = {
    this
      .redirectOutput(Redirect.INHERIT)
      .redirectInput(Redirect.INHERIT)
      .redirectError(Redirect.INHERIT)
  }

  def command(): JList[String] = {
    this.args
  }

  def command(commands: Array[String]): ProcessBuilder = {
    this.args = JArrays.asList(commands: _*)
    this
  }

  def command(commands: JList[String]): ProcessBuilder = {
    this.args = commands
    this
  }

  def directory(): File = {
    this.workingDir
  }

  def directory(directory: File): ProcessBuilder = {
    this.workingDir = directory
    this
  }

  def environment(): JMap[String, String] = {
    this.env
  }

  def redirectError(): Redirect =
    if (redirectErrorStream()) {
      this.output
    } else {
      this.error
    }
  def redirectError(file: File): ProcessBuilder = {
    if (file == null) throw new NullPointerException
    this.redirectError(Redirect.to(file))
  }
  def redirectError(destination: Redirect): ProcessBuilder = {
    if (destination == null) throw new NullPointerException
    destination.`type`() match {
      case Redirect.Type.READ =>
        throw new IllegalArgumentException("Type of redirectInput destination should not be READ")
      case _ => // ok
    }
    this.error = destination
    this
  }

  def redirectInput(): Redirect = this.input
  def redirectInput(file: File): ProcessBuilder = {
    if (file == null) throw new NullPointerException
    this.redirectInput(Redirect.from(file))
  }
  def redirectInput(destination: Redirect): ProcessBuilder = {
    if (destination == null) throw new NullPointerException
    destination.`type`() match {
      case Redirect.Type.WRITE | Redirect.Type.APPEND =>
        throw new IllegalArgumentException(
          "Type of redirectInput destination should not be WRITE nor APPEND"
        )
      case _ => // ok
    }
    this.input = destination
    this
  }

  def redirectOutput(): Redirect = this.output
  def redirectOutput(file: File): ProcessBuilder = {
    if (file == null) throw new NullPointerException
    this.redirectOutput(Redirect.to(file))
  }
  def redirectOutput(destination: Redirect): ProcessBuilder = {
    if (destination == null) throw new NullPointerException
    destination.`type`() match {
      case Redirect.Type.READ =>
        throw new IllegalArgumentException("Type of redirectInput destination should not be READ")
      case _ => // ok
    }
    this.output = destination
    this
  }

  def redirectErrorStream(): scala.Boolean = this.isRedirectError
  def redirectErrorStream(redirectErrorStream: scala.Boolean): ProcessBuilder = {
    this.isRedirectError = redirectErrorStream
    this
  }

  private def that(): ProcessBuilder = this

  private[this] final class FileDescriptorInputStream(val fileDescriptor: nodejs.FileDescriptor)
      extends InputStream {
    private[this] var closed = false

    override def available(): Int = {
      if (closed) throw new IOException("stream is closed")
      super.available()
    }

    override def close(): Unit = {
      this.closed = true
      Fs.closeSync(this.fileDescriptor)
    }

    override def read(): Int = {
      val buf = Buffer.alloc(1)
      val byteReads =
        Fs.asInstanceOf[js.Dynamic].readSync(fileDescriptor, buf, 0, 1, null).asInstanceOf[Int]
      if (byteReads > 0) {
        buf(0)
      } else {
        -1
      }
    }
  }

  private def toInputStream(redirect: Redirect, fd: nodejs.FileDescriptor): InputStream = {
    if (fd == 0) {
      new FileDescriptorInputStream(fd)
    } else {
      new FileDescriptorInputStream(fd)
    }
  }

  private def toOutputStream(redirect: Redirect, fd: nodejs.FileDescriptor): OutputStream = {
    if (redirect == Redirect.PIPE) {
      System.out
    } else {
      new FileOutputStream(redirect.file(), redirect.`type`() == Redirect.Type.APPEND)
    }
  }

  def start(): Process = {
    val input = redirectInput()
    if (input.`type` == Redirect.Type.READ && !input.file().exists())
      throw new IOException(s"redirectInput not exist: $input")

    val command = that().command().asScala.mkString(" ")

    sealed trait ProcessStatus
    case object Taxing  extends ProcessStatus
    case object Running extends ProcessStatus
    case object Closed  extends ProcessStatus

    class ProcessImpl(
        redirectInput: Redirect,
        redirectOutput: Redirect,
        redirectError: Redirect,
        redirectErrorStream: scala.Boolean
    ) extends Process {
      import net.exoego.nodejs.child_process.ChildProcess

      private val fdInput  = toJavaScript(redirectInput, 0)
      private val fdOutput = toJavaScript(redirectOutput, 1)
      private val fdError  = toJavaScript(redirectError, 2)

      override val getInputStream: InputStream   = toInputStream(redirectInput, fdInput)
      override val getOutputStream: OutputStream = toOutputStream(redirectOutput, fdOutput)
      override val getErrorStream: InputStream   = toInputStream(redirectError, fdError)

      private var cpExitValue           = -1
      private var status: ProcessStatus = Taxing

      private def toJavaScript(redirect: Redirect, fallback: Int): nodejs.FileDescriptor = {
        val path = Option(redirect.file).map(_.getPath).getOrElse("")
        redirect.`type` match {
          case Redirect.Type.INHERIT => fallback
          case Redirect.Type.PIPE    => fallback
          case Redirect.Type.APPEND  => Fs.openSync(path, "a")
          case Redirect.Type.READ    => Fs.openSync(path, "r")
          case Redirect.Type.WRITE   => Fs.openSync(path, "w")
        }
      }

      // todo: use unref for wait ?
      override def waitFor(): Int = {
        this.status match {
          case Taxing =>
            this.status = Running

            val stdio = js.Array(
              fdInput,
              fdOutput,
              if (redirectErrorStream) fdOutput else fdError
            )
            val options = new SpawnOptions(
              stdio = stdio
            )
            val result = ChildProcess.spawnSync(
              command,
              options
            )
            this.status = Closed
            cpExitValue = result.status.getOrElse(-1)
            stdio.distinct
              .asInstanceOf[js.Dynamic]
              .filter((fd: Any) => fd != "pipe" && fd != "inherit")
              .asInstanceOf[js.Array[nodejs.FileDescriptor]]
              .foreach { fd =>
                Fs.closeSync(fd)
              }
          case Running =>
            throw new IllegalStateException("waitFor is invoked while running")
          case Closed => // do nothing
        }
        cpExitValue
      }

      override def exitValue(): Int = {
        if (this.status != Closed) throw new IllegalThreadStateException()
        cpExitValue
      }

      override def destroy(): Unit = {
//        childProcess.kill()
      }
    }

    new ProcessImpl(this.input, this.output, this.error, this.redirectErrorStream())
  }
}
