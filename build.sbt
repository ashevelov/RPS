ThisBuild / scalaVersion := "3.6.4"

val http4sVersion = "1.0.0-M44"
val circeVersion = "0.15.0-M1"
fork := true

packageOptions += Package.ManifestAttributes(
  "Main-Class" -> "ru.shevel.rsp.RSPServer"
)

// Настройки запуска
Compile / run / mainClass := Some("ru.shevel.rsp.RSPServer")
assembly / mainClass := Some("ru.shevel.rsp.RSPServer")

// Настройки сборки для исключения конфликтов
assembly / assemblyMergeStrategy := {
  case "META-INF/versions/9/module-info.class" => MergeStrategy.discard
  case x =>
    val oldStrategy = (assembly / assemblyMergeStrategy).value
    oldStrategy(x)
}
// Общие настройки проекта
organization := "ru.shevel"
version := "0.1"
scalacOptions ++= Seq("-Xmax-inlines", "50")

// Зависимости проекта
libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "3.6.3",
  "org.typelevel" %% "cats-effect-kernel" % "3.6.3",
  "org.typelevel" %% "cats-core" % "2.13.0",
  "org.scala-lang.modules" %% "scala-java8-compat" % "1.0.2",
  "org.typelevel" %% "log4cats-slf4j" % "2.7.1",
  "org.http4s" %% "http4s-ember-client" % http4sVersion,
  "org.http4s" %% "http4s-ember-server" % http4sVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.slf4j" % "slf4j-simple" % "2.0.17",
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion
)

//lazy val server = Project(
//  id = "rsp-server",
//  base = file("rsp-server")
//).settings(
//  // Основные настройки упаковки
//
//)