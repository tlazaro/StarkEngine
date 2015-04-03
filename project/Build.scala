import sbt._, Keys._, Path._

//import eu.diversit.sbt.plugin.WebDavPlugin._

object ProjectDefinition extends Build {
  val gdxVersion = "1.5.5"

  lazy val root = Project("StarkEngine", file(".")) settings (Seq(
    organization := "com.starkengine",
    name := "starkengine",
    version := "0.2.0",
    resolvers += Resolver.sonatypeRepo("releases"),
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.3",
      "com.badlogicgames.gdx" % "gdx" % gdxVersion,
      "com.badlogicgames.gdx" % "gdx-backend-lwjgl" % gdxVersion,
      "com.badlogicgames.gdx" % "gdx-platform" % gdxVersion % "test" classifier "natives-desktop",
      "com.badlogicgames.gdx" % "gdx-freetype" % gdxVersion,
      "com.badlogicgames.gdx" % "gdx-freetype-platform" % gdxVersion % "test" classifier "natives-desktop",
      "org.scalatest" %% "scalatest" % "2.2.4" % "test"
    ),
    fork in test := true,
    scalaVersion := "2.11.6"
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
