package java.nio.channels.spi

import java.nio.channels.{Channel, InterruptibleChannel}

abstract class AbstractInterruptibleChannel extends Channel with InterruptibleChannel {

  protected def begin(): Unit = ()

  protected def end(completed: Boolean): Unit = ()

  protected def implCloeChannel(): Unit
}
