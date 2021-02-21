import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}
import sbt._

val selfPackageName = "scalajs-javalib-node"
val scala212Ver     = "2.12.13"
val scala213Ver     = "2.13.4"
val selfVersion     = "0.1-SNAPSHOT"

lazy val commonSettings = Seq(
  parallelExecution in Test := false,
  description := "Attempt to implement Java standard libary for Scala.js on top of Node.js",
  version := selfVersion,
  organization := "net.exoego",
  homepage := Some(url(s"https://github.com/exoego/${selfPackageName}")),
  licenses := Seq("Apache License 2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0")),
  scalaVersion := scala213Ver,
  crossScalaVersions := Seq(scala212Ver, scala212Ver),
  scalacOptions ++= Seq(
    "-deprecation",
    "-feature",
    "-Xfatal-warnings",
    "-encoding",
    "UTF-8"
  ),
  scalacOptions in Test --= Seq(
    "-Xfatal-warnings"
  ),
  libraryDependencies ++= Seq(
    "org.scala-lang.modules" %%% "scala-collection-compat" % "2.4.1",
    "org.scalatest" %%% "scalatest" % "3.1.4" % "test"
  )
)
lazy val commonJsSettings = Seq(
  scalacOptions ++= Seq(
    ),
  scalaJSLinkerConfig in Test ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
  libraryDependencies ++= Seq(
    "net.exoego" %%% "scala-js-nodejs-v14" % "0.13.0"
  )
)

lazy val root = project
  .in(file("."))
  .settings(
    name := s"${selfPackageName}"
  )
  .settings(commonSettings: _*)
  .aggregate(
    jdk.jvm,
    jdk.js
  )

lazy val jdk = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Full)
  .in(file("jdk"))
  .settings(commonSettings: _*)
  .settings(
    name := s"${selfPackageName}"
  )
  .jsSettings(commonJsSettings: _*)
  .jvmSettings()
