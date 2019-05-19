package net.exoego.nodejs.child_process

import io.scalajs.{nodejs => njs}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js.|

@js.native
trait ChildProcess extends js.Object with njs.events.IEventEmitter {
  def kill(signal: js.UndefOr[Int | String] = js.native): Unit = js.native
}

@JSImport("child_process", JSImport.Namespace)
@js.native
object ChildProcess extends scala.scalajs.js.Object with ChildProcess {
  def execSync(
      command: String,
      options: njs.child_process.ExecOptions | io.scalajs.RawOptions = js.native
  ): njs.buffer.Buffer | String = js.native

  def exec(
      command: String,
      options: njs.child_process.ExecOptions | io.scalajs.RawOptions = js.native,
      callback: js.Function3[
        njs.Error,
        Output,
        Output,
        Any
      ] = js.native
  ): this.type = js.native

  def fork(
      modulePath: String,
      args: js.Array[String] = js.native,
      options: njs.child_process.ForkOptions | io.scalajs.RawOptions = js.native
  ): ChildProcess.this.type = js.native

  def spawn(
      command: String,
      args: js.Array[String] = js.native,
      options: SpawnOptions | io.scalajs.RawOptions = js.native
  ): ChildProcess.this.type = js.native

  def spawnSync(command: String): SpawnResult = js.native

  def spawnSync(
      command: String,
      args: js.Array[String],
      options: njs.child_process.SpawnOptions | io.scalajs.RawOptions = js.native
  ): SpawnResult = js.native

  def spawnSync(
      command: String,
      options: njs.child_process.SpawnOptions | io.scalajs.RawOptions
  ): SpawnResult = js.native
}

class SpawnOptions(
    val cwd: js.UndefOr[String] = js.undefined,
    val env: js.Any = js.undefined,
    val argv0: js.UndefOr[String] = js.undefined,
    val stdio: js.UndefOr[js.Array[String | io.scalajs.nodejs.FileDescriptor] | js.Array[
      io.scalajs.nodejs.FileDescriptor
    ] | js.Array[String] | String] = js.undefined,
    val detached: js.UndefOr[Boolean] = js.undefined,
    val uid: js.UndefOr[Int] = js.undefined,
    val gid: js.UndefOr[Int] = js.undefined,
    val shell: js.UndefOr[Boolean | String] = js.undefined
) extends js.Object

class SpawnResult(
    val pid: Int,
    val output: js.Array[Output],
    val stdout: Output,
    val stderr: Output,
    val status: js.UndefOr[Int],
    val signal: js.UndefOr[String],
    val error: js.UndefOr[js.Error]
) extends js.Object
