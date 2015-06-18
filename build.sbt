libraryDependencies ++= Seq(
  "org.specs2" %% "specs2" % "2.2.2" % "test",
  "org.scala-lang" % "scala-reflect" % "2.10.5" % "provided",
  "org.scala-lang" % "scala-compiler" % "2.10.5" % "provided",
  "com.typesafe.akka" %% "akka-actor" % "2.2.3" % "test",
  "io.spray" %% "spray-json" % "1.2.5" % "test",
  "io.spray" % "spray-routing" % "1.2.0" % "test"
)

scalaVersion := "2.10.5"

ScalariformSupport.formatSettings

scalacOptions ++= Seq(
  "-encoding", "utf8", "-unchecked", "-deprecation", "-feature", "-language:_"
)
