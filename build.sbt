organization := "org.sosa"
name := "simpleapi"
version := "0.0.1-SNAPSHOT"
scalaVersion := "2.11.0"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-blaze-server" % "0.13.2a",
  "org.http4s" %% "http4s-dsl"          % "0.13.2a",
  "org.http4s" %% "http4s-argonaut"     % "0.13.2a",
  "com.typesafe.slick" %% "slick" % "3.1.1",
  "com.h2database" % "h2" % "1.3.170",
  "org.slf4j" % "slf4j-nop" % "1.6.4"
)

