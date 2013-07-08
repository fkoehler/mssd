import sbt._
import sbt.Keys._

object MssdBuild extends Build {

  lazy val mad = Project(
    id = "mssd",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "mssd",
      organization := "com.github.mssd",
      version := "0.1.7",
      scalaVersion := "2.10.1",
      libraryDependencies ++= Seq(
        "org.specs2" %% "specs2" % "1.14" % "test",
		    "org.mongodb" % "mongo-java-driver" % "2.11.1",
        "joda-time" % "joda-time" % "2.2",
        "org.joda" % "joda-convert" % "1.3.1"
      ),
      publishTo <<= version {
        (v: String) =>
          val base = "/Users/fab/dev/fkoehler-mvn-repo"
          if (v.trim.endsWith("SNAPSHOT"))
            Some(Resolver.file("file", new File(base + "/snapshots")))
          else
            Some(Resolver.file("file", new File(base + "/releases")))
      }
    )
  )

}
