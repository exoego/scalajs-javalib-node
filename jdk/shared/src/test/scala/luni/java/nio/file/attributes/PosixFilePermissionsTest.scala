package luni.java.nio.file.attributes

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.prop.TableDrivenPropertyChecks._

import java.nio.file.attribute.PosixFilePermission._
import java.nio.file.attribute.{PosixFilePermission, PosixFilePermissions}
import scala.jdk.CollectionConverters._

class PosixFilePermissionsTest extends AnyFreeSpec {
  private val allPermissions = Set(
    OWNER_READ,
    OWNER_WRITE,
    OWNER_EXECUTE,
    GROUP_READ,
    GROUP_WRITE,
    GROUP_EXECUTE,
    OTHERS_READ,
    OTHERS_WRITE,
    OTHERS_EXECUTE
  )

  "toString" in {
    assert("---------" === PosixFilePermissions.toString(Set.empty[PosixFilePermission].asJava))
    assert("r--------" === PosixFilePermissions.toString(Set(OWNER_READ).asJava))
    assert("-w-------" === PosixFilePermissions.toString(Set(OWNER_WRITE).asJava))
    assert("--x------" === PosixFilePermissions.toString(Set(OWNER_EXECUTE).asJava))
    assert("---r-----" === PosixFilePermissions.toString(Set(GROUP_READ).asJava))
    assert("----w----" === PosixFilePermissions.toString(Set(GROUP_WRITE).asJava))
    assert("-----x---" === PosixFilePermissions.toString(Set(GROUP_EXECUTE).asJava))
    assert("------r--" === PosixFilePermissions.toString(Set(OTHERS_READ).asJava))
    assert("-------w-" === PosixFilePermissions.toString(Set(OTHERS_WRITE).asJava))
    assert("--------x" === PosixFilePermissions.toString(Set(OTHERS_EXECUTE).asJava))
    assert("rwxrwxrwx" === PosixFilePermissions.toString(allPermissions.asJava))
  }

  "fromString" - {
    "Convert to a set of permissions" in {
      assert(PosixFilePermissions.fromString("---------").asScala === Set())
      assert(PosixFilePermissions.fromString("r--------").asScala === Set(OWNER_READ))
      assert(PosixFilePermissions.fromString("-w-------").asScala === Set(OWNER_WRITE))
      assert(PosixFilePermissions.fromString("--x------").asScala === Set(OWNER_EXECUTE))
      assert(PosixFilePermissions.fromString("---r-----").asScala === Set(GROUP_READ))
      assert(PosixFilePermissions.fromString("----w----").asScala === Set(GROUP_WRITE))
      assert(PosixFilePermissions.fromString("-----x---").asScala === Set(GROUP_EXECUTE))
      assert(PosixFilePermissions.fromString("------r--").asScala === Set(OTHERS_READ))
      assert(PosixFilePermissions.fromString("-------w-").asScala === Set(OTHERS_WRITE))
      assert(PosixFilePermissions.fromString("--------x").asScala === Set(OTHERS_EXECUTE))
      assert(PosixFilePermissions.fromString("rwxrwxrwx").asScala === allPermissions)
    }

    "throws if perms can not be converted" in {
      assertThrows[IllegalArgumentException] {
        PosixFilePermissions.fromString("")
      }
      assertThrows[IllegalArgumentException] {
        PosixFilePermissions.fromString("RWXRWXRWX")
      }
      assertThrows[IllegalArgumentException] {
        PosixFilePermissions.fromString("rwxrwxrwxrwx")
      }
      assertThrows[IllegalArgumentException] {
        PosixFilePermissions.fromString("rwxrwxrwx---")
      }
    }
  }

  "asFileAttribute" in {
    forAll(
      Table(
        "perms",
        "---------",
        "r--------",
        "-w-------",
        "--x------",
        "---r-----",
        "----w----",
        "-----x---",
        "------r--",
        "-------w-",
        "--------x",
        "rwxrwxrwx"
      )
    ) { perms: String =>
      val attr = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString(perms))
      assert(attr.name() === "posix:permissions")
      assert(attr.value() === PosixFilePermissions.fromString(perms))
    }
  }
}
