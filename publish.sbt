publishTo <<= version { v: String =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
  else                             Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

pomExtra :=
  Helpers.generatePomExtra("git@github.com:jrudolph/rescue.git",
                           "scm:git:git@github.com:jrudolph/rescue.git",
                           "jrudolph", "Johannes Rudolph")

useGpg := true
