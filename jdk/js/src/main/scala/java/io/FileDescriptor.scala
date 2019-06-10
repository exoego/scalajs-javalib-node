package java.io

import io.scalajs.nodejs.fs.Fs

final class FileDescriptor {

  private[io] var internal = -1

  private[io] var readOnly = false

  def valid(): Boolean = this.internal != -1

  private[io] def invalidate(): Unit = {
    internal = -1
  }
}

object FileDescriptor {
  final val in: FileDescriptor  = FileDescriptorFactory.createInternal(0, true)
  final val out: FileDescriptor = FileDescriptorFactory.createInternal(1)
  final val err: FileDescriptor = FileDescriptorFactory.createInternal(2)
}

private[io] object FileDescriptorFactory {
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
