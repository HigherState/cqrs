name := "cqrs"

organization := "org.higherState"

version := "0.2.1"

scalaVersion := "2.11.5"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-compiler" % "2.11.5",
  "org.scala-lang" % "scala-reflect" % "2.11.5",
  "org.scalaz" %% "scalaz-core" % "7.1.0",
  "org.scalaz" %% "scalaz-concurrent" % "7.1.0",
  "com.typesafe.akka" %% "akka-actor" % "2.3.6",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test"
)

resolvers ++= Seq (
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
)
