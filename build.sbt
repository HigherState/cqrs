name := "Cqrs"

organization := "org.higherState"

version := "0.1"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-compiler" % "2.10.3",
  "org.scalaz" %% "scalaz-core" % "7.0.4",
  "com.typesafe.akka" %% "akka-actor" % "2.2.3",
  "org.scalatest" % "scalatest_2.10" % "2.1.0" % "test"
)

resolvers ++= Seq (
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
)
