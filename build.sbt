libraryDependencies ++= Seq(
  //"org.specs2" %% "specs2" % "2.2.2" % "test",
  "com.typesafe.akka" %% "akka-actor" % "2.4.17" % "test",
  "io.spray" %% "spray-json" % "1.3.3" % "test",
  "io.spray" % "spray-routing" % "1.2.3" % "test"
)

libraryDependencies := {
  val scalaV = scalaVersion.value

  libraryDependencies.value ++
  Seq(
    "org.scala-lang" % "scala-reflect" % scalaV % "provided",
    "org.scala-lang" % "scala-compiler" % scalaV % "provided"
  )
}

scalaVersion := "2.11.8"

ScalariformSupport.formatSettings

scalacOptions ++= Seq(
  "-encoding", "utf8", "-unchecked", "-deprecation", "-feature", "-language:_"
)
