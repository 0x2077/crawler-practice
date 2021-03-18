name := "crawler-practice"
scalaVersion := "2.13.4"

val scalaScraperVersion = "2.2.0"
val catsVersion = "2.4.0"
val catsEffectVersion = "2.3.3"
val scalaTestVersion = "3.2.6"
val http4sVersion = "0.21.20"
val circeVersion = "0.13.0"

lazy val crawler = (project in file("crawler"))
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest) (Defaults.itSettings) : _*)
  .settings(
    scalaVersion := "2.13.4",
    sources in(Compile, doc) := Seq.empty,
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-core" % http4sVersion,
      "org.http4s" %% "http4s-circe" % http4sVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % http4sVersion,
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "io.circe" %% "circe-literal" % circeVersion, // string interpolation to JSON model
      "net.ruippeixotog" %% "scala-scraper" % scalaScraperVersion, // DSL for loading and extracting content from HTML pages
      "org.typelevel" %% "cats-core" % catsVersion,
      "org.typelevel" %% "cats-effect" % catsEffectVersion,
      "org.scalactic" %% "scalactic" % scalaTestVersion % "it, test",
      "org.scalatest" %% "scalatest" % scalaTestVersion % "it, test"
    )
  )


lazy val root = (project in file("."))
  .settings(scalaVersion := "2.13.4")
  .aggregate(crawler)
