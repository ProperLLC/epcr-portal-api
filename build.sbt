import play.Project._

name := "epcr-portal-api"

version := "1.1-SNAPSHOT"

resolvers += "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

resolvers += Resolver.file("Local repo", file("/Users/terry/.ivy2/local"))(Resolver.ivyStylePatterns)

libraryDependencies ++= Seq(
  cache,
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.0",
  "net.xymox" %% "play-hawk" % "1.0-SNAPSHOT"
)

playScalaSettings