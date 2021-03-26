package java.nio.channels.spi

import java.nio.channels.{Channel, InterruptibleChannel}

abstract class AbstractInterruptibleChannel extends Channel with InterruptibleChannel {

  private var open: Boolean = true

  protected def begin(): Unit = ()

  final def close(): Unit = {
    this.open = false
    this.implCloseChannel()
  }

  final override def isOpen(): Boolean = this.open

  protected def end(completed: Boolean): Unit = ()

  protected def implCloseChannel(): Unit
}
