package java.security

object AccessController {

  def doPrivileged[T](action: PrivilegedAction[T]): T = {
    // TODO: check privilege
    action.run()
  }
}
