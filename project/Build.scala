import sbt._
import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._

object Build extends Build {
  lazy val frontierVersion = "0.1-SNAPSHOT"

  val commons_cli = "commons-cli" % "commons-cli" % "1.2" withSources()

  /**
   * the interaction between sbt and the assembly plugin is very fragile and requires us to define the project here
   * rather than in a build.sbt file
   */
  lazy val appSettings = Defaults.defaultSettings ++ Seq(
    version := frontierVersion,
    organization := "ws.frontier",
    name := "frontier-app",
    scalaVersion := "2.10.1",
    libraryDependencies ++= Seq(commons_cli)
  )

  lazy val all = Project(id = "all",
    base = file(".")) aggregate(core, app)

  lazy val app = Project(id = "app",
    base = file("frontier-app"),
    settings = appSettings ++ assemblySettings ++ Seq(
      mergeStrategy in assembly := {
        case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
        case _ => MergeStrategy.first
      },
      jarName in assembly := "frontier-%s.jar".format(frontierVersion),
      mainClass in assembly := Some("ws.frontier.FrontierApp")
    )
  ) dependsOn(core, test % "compile->test")

  lazy val core = Project(id = "core",
    base = file("frontier-core")) dependsOn (test % "compile->test")

  lazy val test = Project(id = "test",
    base = file("frontier-test"))
}
