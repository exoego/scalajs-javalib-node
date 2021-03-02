package java.nio.file

import java.nio.file.attribute.{FileAttributeView, FileStoreAttributeView}

trait FileStore {
  def getAttribute(attribute: String): AnyRef
  def getFileStoreAttributeView[V <: FileStoreAttributeView](tpe: Class[V]): V
  def getTotalSpace(): Long
  def getUnallocatedSpace(): Long
  def getUsableSpace(): Long
  def isReadOnly(): Boolean
  def name(): String
  def supportsFileAttributeView(tpe: Class[_ <: FileAttributeView]): Boolean
  def supportsFileAttributeView(name: String): Boolean
  def `type`(): String
}
