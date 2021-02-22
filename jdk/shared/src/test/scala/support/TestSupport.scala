package support

trait TestSupport {
  private var platformId: String = _

  val isScalaJS: Boolean = System.getProperty("java.vm.name") == "Scala.js"

  def getNewPlatformFile(pre: String, post: String): String = {
    if (platformId == null) {
      platformId = "JDK" + System.getProperty("java.vm.version").replace('.', '-')
    }
    pre + platformId + post
  }
}
