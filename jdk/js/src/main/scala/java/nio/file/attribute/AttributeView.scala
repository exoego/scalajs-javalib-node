package java.nio.file.attribute

trait AttributeView {
  def name: String
}

trait FileAttributeView extends AttributeView

trait DosFileAttributeView extends FileAttributeView
