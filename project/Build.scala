import sbt._, Keys._, Path._

//import eu.diversit.sbt.plugin.WebDavPlugin._

object ProjectDefinition extends Build {
  lazy val root = Project("StarkEngine", file(".")) settings (Seq(
    organization := "com.starkengine",
    name := "starkengine",
    version := "0.1.1",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "1.9.1" % "test"
    ),
    fork in test := true,
    scalaVersion := "2.10.4",
    crossScalaVersions := Seq("2.9.3", "2.10.4")
  ): _*)

  /*lazy val publishSettings = aether.Aether.aetherSettings ++ WebDav.scopedSettings ++ Seq[Project.Setting[_]](
    organization := "com.starkengine",
    name := "starkengine",
    version := "0.1",
    publishMavenStyle := true,
    publishTo <<= (version) {
      version: String =>
      val cloudbees = "https://repository-belfry.forge.cloudbees.com/"
      if (version.trim.endsWith("SNAPSHOT")) Some("snapshot" at cloudbees + "snapshot/")
      else                                   Some("release"  at cloudbees + "release/")
    },
    credentials += {
      val credsFile = (Path.userHome / ".credentials")
      (if (credsFile.exists) Credentials(credsFile)
       else Credentials(file("/private/belfry/.credentials/.credentials")))
    }
  )*/
}
