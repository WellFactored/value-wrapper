name := "value-wrapper"

organization := "com.wellfactored"

scalaVersion := "2.11.8"

lazy val `value-wrapper` =
  (project in file("."))
    .enablePlugins(GitVersioning)
    .enablePlugins(GitBranchPrompt)

git.useGitDescribe in ThisBuild := true

libraryDependencies ++= Seq(
  "com.chuusai" %% "shapeless" % "2.3.1",
  "org.typelevel" %% "cats-core" % "0.7.0",

  "org.scalatest" %% "scalatest" % "3.0.0" % Test
)
