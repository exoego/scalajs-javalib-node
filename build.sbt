import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}
import sbt._

val selfPackageName = "scalajs-javalib-node"
val scalaVer        = "2.12.8"
val selfVersion     = "0.1-SNAPSHOT"

lazy val commonSettings = Seq(
  description := "Attempt to implement Java standard libary for Scala.js on top of Node.js",
  version := selfVersion,
  organization := "net.exoego",
  homepage := Some(url(s"https://github.com/exoego/${selfPackageName}")),
  licenses := Seq("Apache License 2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0")),
  scalaVersion := scalaVer,
  crossScalaVersions := Seq("2.12.8", "2.13.0-RC2"),
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
    "org.scalatest" %%% "scalatest" % "3.0.7" % "test"
  )
)
lazy val commonJsSettings = Seq(
  scalacOptions ++= Seq(
    "-P:scalajs:sjsDefinedByDefault"
  ),
  scalaJSModuleKind := ModuleKind.CommonJSModule,
  libraryDependencies ++= Seq(
    "io.scalajs" %%% "nodejs" % "0.4.2"
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
    jdk.js,
    nodejsFacade
  )

lazy val nodejsFacade = (project in file("facade/nodejs"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(commonJsSettings: _*)
  .settings(
    name := s"${selfPackageName}-nodejs-facade"
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
lazy val jdkJS = jdk.js.dependsOn(nodejsFacade)
