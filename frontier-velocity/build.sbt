name := "frontier-velocity"

organization := "ws.frontier"

version := "0.1-SNAPSHOT"

scalaVersion := "2.10.1"

{
  val velocityVersion = "1.7"
  libraryDependencies ++= Seq(
    "org.apache.velocity" % "velocity" % velocityVersion withSources()
  )
}


