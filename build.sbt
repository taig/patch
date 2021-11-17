val Version = new {
  val Circe = "0.14.1"
  val EmbeddedPostgres = "1.3.1"
  val EmbeddedPostgresBinary = "13.4.0"
  val Munit = "0.7.29"
  val MunitCatsEffect = "1.0.6"
  val Scala213 = "2.13.7"
  val Shapeless = "2.3.7"
  val Slf4j = "1.7.32"
  val Skunk = "0.2.2"
}

ThisBuild / developers := List(Developer("taig", "Niklas Klein", "mail@taig.io", url("https://taig.io/")))
ThisBuild / dynverVTagPrefix := false
ThisBuild / homepage := Some(url("https://github.com/taig/patch/"))
ThisBuild / licenses := List("MIT" -> url("https://raw.githubusercontent.com/taig/patch/main/LICENSE"))
ThisBuild / scalaVersion := Version.Scala213
ThisBuild / versionScheme := Some("early-semver")

noPublishSettings
name := "patch"

lazy val circe = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("modules/circe"))
  .settings(
    libraryDependencies ++=
      "io.circe" %%% "circe-core" % Version.Circe ::
        "org.scalameta" %%% "munit" % Version.Munit % "test" ::
        Nil,
    name := "patch-circe"
  )

lazy val circeGeneric = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("modules/circe-generic"))
  .settings(
    libraryDependencies ++=
      "com.chuusai" %%% "shapeless" % Version.Shapeless ::
        Nil,
    name := "patch-circe-generic"
  )
  .dependsOn(circe % "compile->compile;test->test")

lazy val skunk = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Full)
  .in(file("modules/skunk"))
  .settings(
    libraryDependencies ++=
      "org.tpolecat" %%% "skunk-core" % Version.Skunk ::
        Nil,
    name := "patch-skunk"
  )
  .jvmSettings(
    libraryDependencies ++=
      "io.zonky.test" % "embedded-postgres" % Version.EmbeddedPostgres % "it" ::
        "org.slf4j" % "slf4j-nop" % Version.Slf4j % "it" ::
        "org.typelevel" %% "munit-cats-effect-3" % Version.MunitCatsEffect % "it" ::
        Nil,
    libraryDependencies += {
      val platform = Option(System.getProperty("os.name")).map(_.toLowerCase) match {
        case Some(os) if os.contains("mac") => "darwin"
        case Some(os) if os.contains("win") => "windows"
        case Some(_) | None                 => "linux"
      }

      "io.zonky.test.postgres" % s"embedded-postgres-binaries-$platform-amd64" % Version.EmbeddedPostgresBinary % "it"
    },
  )
