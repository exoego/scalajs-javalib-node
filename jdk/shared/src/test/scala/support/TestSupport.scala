package support

trait TestSupport {
  private var platformId: String = _

  val isScalaJS: Boolean       = System.getProperty("java.vm.name") == "Scala.js"
  val isJDK11AndLater: Boolean = System.getProperty("java.vm.version").split('.').head.toInt >= 11

  def getNewPlatformFile(pre: String, post: String): String = {
    if (platformId == null) {
      platformId = "JDK" + System.getProperty("java.vm.version").replace('.', '-')
    }
    pre + platformId + post
  }
}
