name := "frontier-core"

organization := "ws.frontier"

version := "0.1-SNAPSHOT"

scalaVersion := "2.10.0"

resolvers += "Twitter Repo" at "http://maven.twttr.com"

{
    val finagleVersion = "6.2.0"
    libraryDependencies ++= Seq(
        "com.twitter" % "finagle-core_2.10" % finagleVersion % "provided" withSources(),
        "com.twitter" % "finagle-native_2.10" % finagleVersion % "provided" withSources(),
        "com.twitter" % "finagle-redis_2.10" % finagleVersion % "provided" withSources(),
        "com.twitter" % "finagle-serversets_2.10" % finagleVersion % "provided" withSources(),
        "com.twitter" % "finagle-http_2.10" % finagleVersion % "provided" withSources()
    )
}

{
  val jacksonVersion = "2.1.4"
  libraryDependencies ++= Seq(
    "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion withSources()
  )
}
