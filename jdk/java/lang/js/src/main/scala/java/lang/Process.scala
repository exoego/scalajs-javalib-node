package java.lang

import java.io.{InputStream, OutputStream}
import java.util.concurrent.{CompletableFuture, TimeUnit}
import java.util.stream.Stream

abstract class Process {

  def destroy(): Unit
  def exitValue(): Int
  def getErrorStream(): InputStream
  def getInputStream(): InputStream
  def getOutputStream(): OutputStream
  def waitFor(): Int

  def children(): Stream[ProcessHandle] = throw new UnsupportedOperationException("children")

  def descendants(): Stream[ProcessHandle] = throw new UnsupportedOperationException("descendants")

  def destroyForcibly(): Process = throw new UnsupportedOperationException("destroyForcibly")

  def info(): ProcessHandle.Info = throw new UnsupportedOperationException("info")

  def isAlive(): scala.Boolean = throw new UnsupportedOperationException("isAlive")

  def onExit(): CompletableFuture[Process] = throw new UnsupportedOperationException("onExit")

  def pid(): scala.Long = throw new UnsupportedOperationException("pid")

  def supportsNormalTermination(): scala.Boolean =
    throw new UnsupportedOperationException("supportsNormalTermination")

  def toHandle(): ProcessHandle = throw new UnsupportedOperationException("toHandle")

  @throws[InterruptedException]
  def waitFor(timeout: scala.Long, unit: TimeUnit): scala.Boolean =
    throw new UnsupportedOperationException("waitFor(timeout,unit)")

}
