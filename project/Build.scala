import sbt._
import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._

object Build extends Build {
  lazy val frontierVersion = "0.1-SNAPSHOT"

  val commons_cli = "commons-cli" % "commons-cli" % "1.2" withSources()
  val logback = "ch.qos.logback" % "logback-classic" % "1.0.11" withSources()

  /**
   * the interaction between sbt and the assembly plugin is very fragile and requires us to define the project here
   * rather than in a build.sbt file
   */
  lazy val appSettings = Defaults.defaultSettings ++ Seq(
    version := frontierVersion,
    organization := "ws.frontier",
    name := "frontier-app",
    scalaVersion := "2.10.1",
    libraryDependencies ++= Seq(commons_cli, logback)
  )

  lazy val all = Project(id = "all",
    base = file(".")) aggregate(core, app, velocity)

  lazy val app = Project(id = "app",
    base = file("frontier-app"),
    settings = appSettings ++ assemblySettings ++ Seq(
      mergeStrategy in assembly := {
        /**
         * need to explicitly handle how content gets merged together to avoid entry duplication
         */
        case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
        case _ => MergeStrategy.first
      },
      /**
       * nasty hack to force slf4j-jdk14 to be excluded from the packing.  we need to do this because one of our
       * projects includes slf4j already
       */
      excludedJars in assembly <<= (fullClasspath in assembly) map { cp =>
        cp filter {_.data.getName == "slf4j-jdk14-1.6.1.jar"}
      },
      jarName in assembly := "frontier-%s.jar".format(frontierVersion),
      mainClass in assembly := Some("ws.frontier.FrontierApp")
    )
  ) dependsOn(core, velocity, test % "compile->test")

  lazy val core = Project(id = "core",
    base = file("frontier-core")) dependsOn (test % "compile->test")

  lazy val velocity = Project(id = "velocity",
    base = file("frontier-velocity")) dependsOn (core, test % "compile->test")

  lazy val test = Project(id = "test",
    base = file("frontier-test"))
}
