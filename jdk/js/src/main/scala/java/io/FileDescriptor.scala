package java.io

import io.scalajs.nodejs.fs.Fs

final class FileDescriptor {

  private[io] var descriptor = -1

  private[io] var readOnly = false

  def valid: Boolean = this.descriptor != -1

}

object FileDescriptor {
  final val in: FileDescriptor  = FileDescriptorFactory.createInternal(0, true)
  final val out: FileDescriptor = FileDescriptorFactory.createInternal(1)
  final val err: FileDescriptor = FileDescriptorFactory.createInternal(2)
}

private[io] object FileDescriptorFactory {
  def createInternal(descriptor: Int, readOnly: Boolean = false): FileDescriptor = {
    val instance = new FileDescriptor
    instance.descriptor = descriptor
    instance.readOnly = readOnly
    instance
  }
}
