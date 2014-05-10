name := "Cqrs"

organization := "org.higherState"

version := "0.1"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-compiler" % "2.10.4",
  "org.scalaz" %% "scalaz-core" % "7.0.6",
  "com.typesafe.akka" %% "akka-actor" % "2.3.2",
  "org.scalatest" % "scalatest_2.10" % "2.1.5" % "test",
  "com.chuusai" % "shapeless_2.10.4" % "2.0.0"
)

resolvers ++= Seq (
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
)
