import _root_.sbt.Keys._
import _root_.scala._
import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "epcr-portal-api"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "org.reactivemongo" %% "reactivemongo" % "0.9",
    "org.reactivemongo" %% "play2-reactivemongo" % "0.9"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers += "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
  )

}
