name := "frontier-handlebars"

organization := "ws.frontier"

version := "0.1-SNAPSHOT"

scalaVersion := "2.10.1"

{
  val handlebarsVersion = "0.11.0"
  libraryDependencies ++= Seq(
    "com.github.jknack" % "handlebars" % handlebarsVersion withSources()
  )
}


