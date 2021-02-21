package support

trait Support_PlatformFile {
  private var platformId: String = _

  val isScalaJS = System.getProperty("java.vm.name") == "Scala.js"

  def getNewPlatformFile(pre: String, post: String): String = {
    if (platformId == null) {
      var property = System.getProperty("com.ibm.oti.configuration")
      if (property == null) property = "JDK"
      platformId = property + System.getProperty("java.vm.version").replace('.', '-')
    }
    pre + platformId + post
  }

  def onNodeJS(): Boolean = {
    System.getProperty("java.vm.name") == "Scala.js"
  }
}
