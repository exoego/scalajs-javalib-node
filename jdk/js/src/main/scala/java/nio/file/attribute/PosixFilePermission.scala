package java.nio.file.attribute

final class PosixFilePermission private (name: String, ordinal: Int)
    extends java.lang.Enum[PosixFilePermission](name, ordinal) {}

object PosixFilePermission {
  val OWNER_READ     = new PosixFilePermission("OWNER_READ", 1)
  val OWNER_WRITE    = new PosixFilePermission("OWNER_WRITE", 2)
  val OWNER_EXECUTE  = new PosixFilePermission("OWNER_EXECUTE", 3)
  val GROUP_READ     = new PosixFilePermission("GROUP_READ", 4)
  val GROUP_WRITE    = new PosixFilePermission("GROUP_WRITE", 5)
  val GROUP_EXECUTE  = new PosixFilePermission("GROUP_EXECUTE", 6)
  val OTHERS_READ    = new PosixFilePermission("OTHERS_READ", 7)
  val OTHERS_WRITE   = new PosixFilePermission("OTHERS_WRITE", 8)
  val OTHERS_EXECUTE = new PosixFilePermission("OTHERS_EXECUTE", 9)
}
