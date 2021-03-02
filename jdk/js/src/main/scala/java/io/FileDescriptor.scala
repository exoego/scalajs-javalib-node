package java.io

import io.scalajs.nodejs.fs.Fs

import java.io.FileDescriptor.in

final class FileDescriptor {

  private[io] var internal = -1

  private[io] var readOnly = false

  def valid(): Boolean = this.internal != -1

  private[io] def invalidate(): Unit = {
    internal = -1
  }

  def sync(): Unit = {
    if (!readOnly && valid()) {
      Fs.fsyncSync(this.internal)
    }
  }
}

object FileDescriptor {
  final val in: FileDescriptor  = FileDescriptorFactory.createInternal(0, true)
  final val out: FileDescriptor = FileDescriptorFactory.createInternal(1)
  final val err: FileDescriptor = FileDescriptorFactory.createInternal(2)
}

private[io] object FileDescriptorFactory {
  def openWrite(filepath: String, append: Boolean): FileDescriptor = {
    val nodeFD = Fs.openSync(filepath, if (append) "a" else "w")
    FileDescriptorFactory.createInternal(nodeFD, readOnly = false)
  }

  def openRead(filepath: String): FileDescriptor = {
    val nodeFD = Fs.openSync(filepath, "r")
    FileDescriptorFactory.createInternal(nodeFD, readOnly = true)
  }

  def createInternal(descriptor: Int, readOnly: Boolean = false): FileDescriptor = {
    val instance = new FileDescriptor
    instance.internal = descriptor
    instance.readOnly = readOnly
    instance
  }
}
