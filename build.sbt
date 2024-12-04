ThisBuild / organization := "es.eriktorr"
ThisBuild / version := "1.0.0"
ThisBuild / idePackagePrefix := Some("es.eriktorr.langchain4s")
Global / excludeLintKeys += idePackagePrefix

ThisBuild / scalaVersion := "3.3.4"

ThisBuild / scalacOptions ++= Seq(
  "-Xfatal-warnings",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-source:future", // https://github.com/oleg-py/better-monadic-for
  "-Yexplicit-nulls", // https://docs.scala-lang.org/scala3/reference/other-new-features/explicit-nulls.html
  "-Ysafe-init", // https://docs.scala-lang.org/scala3/reference/other-new-features/safe-initialization.html
  "-Wnonunit-statement",
  "-Wunused:all",
)

Global / cancelable := true
Global / fork := true
Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / semanticdbEnabled := true
ThisBuild / javacOptions ++= Seq("-source", "21", "-target", "21")

lazy val MUnitFramework = new TestFramework("munit.Framework")
lazy val warts = Warts.unsafe.filter(_ != Wart.DefaultArguments)

Compile / doc / sources := Seq()
Compile / compile / wartremoverErrors ++= warts
Test / compile / wartremoverErrors ++= warts
Test / testFrameworks += MUnitFramework
Test / testOptions += Tests.Argument(MUnitFramework, "--exclude-tags=online")

addCommandAlias(
  "check",
  "; undeclaredCompileDependenciesTest; unusedCompileDependenciesTest; scalafixAll; scalafmtSbtCheck; scalafmtCheckAll",
)

Test / envVars := Map(
  "SBT_TEST_ENV_VARS" -> "true",
)

lazy val root = (project in file("."))
  .settings(
    name := "langchain4s-demo",
    libraryDependencies ++= Seq(
      "co.fs2" %% "fs2-core" % "3.11.0",
      "co.fs2" %% "fs2-io" % "3.11.0",
      "com.47deg" %% "scalacheck-toolbox-datetime" % "0.7.0" % Test,
      "com.comcast" %% "ip4s-core" % "3.6.0",
      "com.lihaoyi" %% "os-lib" % "0.11.3" % Test,
      "com.lmax" % "disruptor" % "3.4.4" % Runtime,
      "com.monovore" %% "decline" % "2.4.1",
      "com.monovore" %% "decline-effect" % "2.4.1",
      "dev.langchain4j" % "langchain4j" % "0.36.2",
      "dev.langchain4j" % "langchain4j-core" % "0.36.2",
      "dev.langchain4j" % "langchain4j-embeddings" % "0.36.2",
      "dev.langchain4j" % "langchain4j-embeddings-all-minilm-l6-v2" % "0.36.2",
      "dev.langchain4j" % "langchain4j-ollama" % "0.36.2",
      "io.chrisdavenport" %% "cats-scalacheck" % "0.3.2" % Test,
      "io.circe" %% "circe-core" % "0.14.10",
      "io.circe" %% "circe-parser" % "0.14.10",
      "io.github.iltotore" %% "iron" % "2.6.0",
      "io.github.iltotore" %% "iron-cats" % "2.6.0",
      "io.github.iltotore" %% "iron-circe" % "2.6.0",
      "org.apache.logging.log4j" % "log4j-core" % "2.24.2" % Runtime,
      "org.apache.logging.log4j" % "log4j-layout-template-json" % "2.24.2" % Runtime,
      "org.apache.logging.log4j" % "log4j-slf4j2-impl" % "2.24.2" % Runtime,
      "org.http4s" %% "http4s-circe" % "0.23.30",
      "org.http4s" %% "http4s-client" % "0.23.30",
      "org.http4s" %% "http4s-core" % "0.23.30",
      "org.http4s" %% "http4s-ember-client" % "0.23.30",
      "org.scalameta" %% "munit" % "1.0.3" % Test,
      "org.scalameta" %% "munit-scalacheck" % "1.0.0" % Test,
      "org.typelevel" %% "cats-core" % "2.12.0",
      "org.typelevel" %% "cats-effect-kernel" % "3.5.7",
      "org.typelevel" %% "cats-effect" % "3.5.7",
      "org.typelevel" %% "cats-effect-std" % "3.5.7",
      "org.typelevel" %% "cats-kernel" % "2.12.0",
      "org.typelevel" %% "case-insensitive" % "1.4.2",
      "org.typelevel" %% "log4cats-core" % "2.7.0",
      "org.typelevel" %% "log4cats-slf4j" % "2.7.0",
      "org.typelevel" %% "munit-cats-effect" % "2.0.0" % Test,
      "org.typelevel" %% "scalacheck-effect" % "1.0.4" % Test,
      "org.typelevel" %% "scalacheck-effect-munit" % "1.0.4" % Test,
      "org.typelevel" %% "vault" % "3.6.0",
    ),
  )
  .enablePlugins(JavaAppPackaging)
