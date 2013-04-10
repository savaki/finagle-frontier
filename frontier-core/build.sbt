name := "frontier-core"

organization := "ws.frontier"

version := "0.1-SNAPSHOT"

scalaVersion := "2.10.1"

resolvers += "Twitter Repo" at "http://maven.twttr.com"

{
    val finagleVersion = "6.2.0"
    libraryDependencies ++= Seq(
        "com.twitter" %% "finagle-core" % finagleVersion withSources(),
        "com.twitter" %% "finagle-native" % finagleVersion withSources(),
        "com.twitter" %% "finagle-redis" % finagleVersion withSources(),
        "com.twitter" %% "finagle-serversets" % finagleVersion withSources(),
        "com.twitter" %% "finagle-http" % finagleVersion withSources()
    )
}

{
  val jacksonVersion = "2.1.4"
  libraryDependencies ++= Seq(
    "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion withSources()
  )
}

