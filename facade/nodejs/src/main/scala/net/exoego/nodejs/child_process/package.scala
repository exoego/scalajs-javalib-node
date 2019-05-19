package net.exoego.nodejs

import scala.scalajs.js.|

import io.scalajs.{nodejs => njs}

package object child_process {

  type Output = njs.buffer.Buffer | String

}
