package java.nio.file

trait OpenOption {}
trait CopyOption {}

final class StandardOpenOption protected[file] (name: String, ordinal: Int)
    extends Enum[StandardOpenOption](name, ordinal)
object StandardOpenOption {
  val APPEND            = new StandardOpenOption("APPEND", 0)
  val CREATE            = new StandardOpenOption("CREATE", 1)
  val CREATE_NEW        = new StandardOpenOption("CREATE_NEW", 2)
  val DELETE_ON_CLOSE   = new StandardOpenOption("DELETE_ON_CLOSE", 3)
  val DSYNC             = new StandardOpenOption("DSYNC", 4)
  val READ              = new StandardOpenOption("READ", 5)
  val SPARSE            = new StandardOpenOption("SPARSE", 6)
  val SYNC              = new StandardOpenOption("SYNC", 7)
  val TRUNCATE_EXISTING = new StandardOpenOption("TRUNCATE_EXISTINGS", 8)
  val WRITE             = new StandardOpenOption("WRITE", 9)
}

final class LinkOption protected[file] (name: String, ordinal: Int)
    extends Enum[LinkOption](name, ordinal)
    with OpenOption
    with CopyOption
object LinkOption {
  val NOFOLLOW_LINKS = new LinkOption("NOFOLLOW_LINKS", 0)
}

final class StandardCopyOption protected[file] (name: String, ordinal: Int)
    extends Enum[StandardOpenOption](name, ordinal)
    with CopyOption
object StandardCopyOption {
  val ATOMIC_MOVE      = new StandardCopyOption("ATOMIC_MOVE", 0)
  val COPY_ATTRIBUTES  = new StandardCopyOption("COPY_ATTRIBUTES", 1)
  val REPLACE_EXISTING = new StandardCopyOption("REPLACE_EXISTING", 2)
}
