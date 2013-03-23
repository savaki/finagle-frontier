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
    libraryDependencies ++= Seq(
        "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test"
    )
}