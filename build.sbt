lazy val root = project
  .in(file("."))
  .settings(
    name := "nose-implant",
    version := "0.1.0",
    scalaVersion := "3.2.0",
    resolvers ++= Seq(
      Resolver.sonatypeRepo("snapshots")
    ),
    libraryDependencies ++= Seq(
      "ch.unibas.cs.gravis" %% "scalismo-ui" % "0.91.1",
      "ch.unibas.cs.gravis" %% "scalismo-plot" % "0.1-SNAPSHOT",
      "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.4",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",
      "com.typesafe" % "config" % "1.4.1",
      "ch.qos.logback" % "logback-classic" % "1.2.3"
    ),
    excludeDependencies ++= Seq(
     "org.scala-lang.modules" % "scala-collection-compat_2.13"
    ),
    scalacOptions ++= Seq("-encoding", "UTF-8", "-Xlint", "-deprecation", "-unchecked", "-feature", "-rewrite", "-indent"),
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),

  )
