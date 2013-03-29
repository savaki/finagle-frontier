import sbt._

object Build extends Build {
  lazy val all = Project(id = "all",
    base = file(".")) aggregate (core)

  lazy val core = Project(id = "core",
    base = file("frontier-core")) dependsOn (test % "compile->test")

  lazy val test = Project(id = "test",
    base = file("frontier-test"))
}
