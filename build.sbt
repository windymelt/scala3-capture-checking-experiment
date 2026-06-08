val scala3Version = "3.8.4"

lazy val root = project
  .in(file("."))
  .settings(
    name := "scala 3 capture checking experiment",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    scalacOptions ++= Seq(
      "-language:experimental.captureChecking",
      "-no-indent"
    ),

    libraryDependencies += "org.scalameta" %% "munit" % "1.3.2" % Test
  )
