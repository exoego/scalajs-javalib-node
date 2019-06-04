package java.lang

/* Modified from Scala.js javalib implementation to add missing methods
 */
class Thread private (dummy: Unit) extends Runnable {
  private var interruptedState   = false
  private[this] var name: String = "main" // default name of the main thread

  def run(): Unit = ()

  def interrupt(): Unit =
    interruptedState = true

  def isInterrupted(): scala.Boolean =
    interruptedState

  final def setName(name: String): Unit =
    this.name = name

  final def getName(): String =
    this.name

  def getStackTrace(): Array[StackTraceElement] =
    scala.scalajs.runtime.StackTrace.getCurrentStackTrace()

  def getId(): scala.Long = 1

  def start(): Unit = {}

  def join(): Unit = {
    // TODO:
  }
  final def join(milis: scala.Long): Unit = {
    // TODO:
  }
  final def join(milis: scala.Long, nanos: scala.Int): Unit = {
    // TODO:
  }

}

object Thread {
  private[this] val SingleThread = new Thread(())

  def currentThread(): Thread = SingleThread

  def interrupted(): scala.Boolean = {
    val ret = currentThread.isInterrupted
    currentThread.interruptedState = false
    ret
  }
}
