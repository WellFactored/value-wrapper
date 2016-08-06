publishMavenStyle in ThisBuild := true

useGpg := true

usePgpKeyHex("46C41F3C")

publishTo in ThisBuild := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

pomIncludeRepository in ThisBuild := { _ => false }

licenses in ThisBuild := Seq("MIT" -> url("https://opensource.org/licenses/MIT"))

homepage in ThisBuild := Some(url(s"https://github.com/wellfactored/$name"))

scmInfo in ThisBuild := Some(ScmInfo(url(s"http://github.com/wellfactored/$name"), s"scm:git@github.com:wellfactored/$name.git"))

pomExtra in ThisBuild :=
  <developers>
    <developer>
      <id>dclinton</id>
      <name>Doug Clinton</name>
    </developer>
  </developers>
