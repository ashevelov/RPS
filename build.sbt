ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.5.1"

val http4sVersion = "1.0.0-M44"
val circeVersion = "0.15.0-M1"

lazy val server = Project(
  id = "rsp-server",
  base = file("rsp-server")
).settings(
  organization := "ru.shevel",
  version := "0.1",
  scalacOptions ++= Seq("-Xmax-inlines", "50"),
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-effect" % "3.5.4",
    "org.typelevel" %% "cats-effect-kernel" % "3.5.4",
    "org.typelevel" %% "cats-core" % "2.12.0",
    "org.scala-lang.modules" %% "scala-java8-compat" % "1.0.2",
    "org.typelevel" %% "log4cats-slf4j" % "2.7.0",
    "org.http4s" %% "http4s-ember-client" % http4sVersion,
    "org.http4s" %% "http4s-ember-server" % http4sVersion,
    "org.http4s" %% "http4s-dsl" % http4sVersion,
    "org.slf4j" % "slf4j-simple" % "2.0.16",
    "org.http4s" %% "http4s-circe" % http4sVersion,
    "io.circe" %% "circe-generic" % circeVersion,
    "io.circe" %% "circe-parser" % circeVersion
  )
)