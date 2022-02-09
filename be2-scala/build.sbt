import scala.util.{Try, Success, Failure}
import sbtsonar.SonarPlugin.autoImport.sonarProperties

name := "pop"

version := "0.1"

scalaVersion := "2.13.7"

// Recommended 2.13 Scala flags (https://nathankleyn.com/2019/05/13/recommended-scalac-flags-for-2-13) slightly adapted for PoP
scalacOptions ++= Seq(
    "-deprecation", // Emit warning and location for usages of deprecated APIs.
    "-explaintypes", // Explain type errors in more detail.
    "-feature", // Emit warning and location for usages of features that should be imported explicitly.
    "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
    "-language:experimental.macros", // Allow macro definition (besides implementation and application)
    "-language:higherKinds", // Allow higher-kinded types
    "-language:implicitConversions", // Allow definition of implicit functions called views
    "-unchecked", // Enable additional warnings where generated code depends on assumptions.
    "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
    "-Xfatal-warnings", // Fail the compilation if there are any warnings.
    "-Xlint:adapted-args", // Warn if an argument list is modified to match the receiver.
    "-Xlint:constant", // Evaluation of a constant arithmetic expression results in an error.
    "-Xlint:delayedinit-select", // Selecting member of DelayedInit.
    "-Xlint:doc-detached", // A Scaladoc comment appears to be detached from its element.
    "-Xlint:inaccessible", // Warn about inaccessible types in method signatures.
    "-Xlint:infer-any", // Warn when a type argument is inferred to be `Any`.
    "-Xlint:missing-interpolator", // A string literal appears to be missing an interpolator id.
    "-Xlint:nullary-unit", // Warn when nullary methods return Unit.
    "-Xlint:option-implicit", // Option.apply used implicit view.
    "-Xlint:package-object-classes", // Class or object defined in package object.
    "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
    "-Xlint:private-shadow", // A private field (or class parameter) shadows a superclass field.
    "-Xlint:stars-align", // Pattern sequence wildcard must align with sequence component.
    "-Xlint:type-parameter-shadow", // A local type parameter shadows a type already in scope.
    "-Ywarn-extra-implicit", // Warn when more than one implicit parameter section is defined.
    "-Ywarn-numeric-widen", // Warn when numerics are widened.
    "-Ycache-plugin-class-loader:last-modified", // Enables caching of classloaders for compiler plugins
    "-Ycache-macro-class-loader:last-modified", // and macro definitions. This can lead to performance improvements.
)


//Reload changes automatically
Global / onChangedBuildSource := ReloadOnSourceChanges
Global / cancelable := true

//Fork run task in compile scope
Compile/ run / fork := true
Compile/ run / connectInput := true
Compile/ run / javaOptions += "-Dscala.config=src/main/scala/ch/epfl/pop/config"

//Make test execution synchronized
Test/ test/ parallelExecution := false

//Create task to copy the protocol folder to resources
lazy val copyProtocolTask = taskKey[Unit]("Copy protocol to resources")
copyProtocolTask := {
    val log = streams.value.log
    log.info("Executing Protocol folder copy...")
    val scalaDest = "be2-scala"
    baseDirectory.value.name
    if(! baseDirectory.value.name.equals(scalaDest)){
        log.error(s"Please make sure you working dir is $scalaDest !")
    }else{
        val source = new File("../protocol")
        val dest   = new File("./src/main/resources/protocol")
        Try(IO.copyDirectory(source, dest, overwrite = true)) match {
            case Success(_) => log.info("Copied !!")
            case Failure(exception) =>
                log.error("Could not copy protocol to resource folder")
                exception.printStackTrace()
        }
    }
}
//Add the copyProtocolTask to compile and test scopes
(Compile/ compile) := ((Compile/ compile) dependsOn copyProtocolTask).value
(Test/ test) := ((Test/ test) dependsOn copyProtocolTask).value

//Setup resource directory for jar assembly
(Compile /packageBin / resourceDirectory) := file(".") / "./src/main/resources"

//Make resourceDirectory setting global to remove sbt warning
(Global / excludeLintKeys) += resourceDirectory

//Setup main calass task context/confiuration
Compile/ run/ mainClass := Some("ch.epfl.pop.Server")
Compile/ packageBin/ mainClass := Some("ch.epfl.pop.Server")

lazy val scoverageSettings = Seq(
  Compile/ coverageEnabled  := true,
  Test/ coverageEnabled  := true,
  packageBin/ coverageEnabled  := false,
)

ThisBuild/ scapegoatVersion := "1.4.11"

scapegoatReports := Seq("xml")

// temporarily report scapegoat errors as warnings, to avoid broken builds
Scapegoat/ scalacOptions += "-P:scapegoat:overrideLevels:all=Warning"

// Configure Sonar
sonarProperties := Map(
  "sonar.organization" -> "dedis",
  "sonar.projectKey" -> "dedis_popstellar_be2",

  "sonar.sources" -> "src/main/scala",
  "sonar.tests" -> "src/test/scala",

  "sonar.sourceEncoding" -> "UTF-8",
  "sonar.scala.version" -> "2.13.7",
  // Paths to the test and coverage reports
  "sonar.scala.coverage.reportPaths" -> "./target/scala-2.13/scoverage-report/scoverage.xml",
  "sonar.scala.scapegoat.reportPaths" -> "./target/scala-2.13/scapegoat-report/scapegoat.xml"
)

assembly/ assemblyMergeStrategy  := {
    case PathList("module-info.class") => MergeStrategy.discard
    case PathList("reference.conf") => MergeStrategy.concat
    case PathList("META-INF","MANIFEST.MF") => MergeStrategy.discard
    case _ => MergeStrategy.defaultMergeStrategy("")
}

// For websockets
val AkkaVersion = "2.6.8"
val AkkaHttpVersion = "10.2.0"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion)

// Logging for akka
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.3" % Runtime

// distributed pub sub cluster
libraryDependencies += "com.typesafe.akka" %% "akka-cluster-tools" % AkkaVersion

// Akka actor test kit
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % AkkaVersion % Test

// For LevelDB database
// https://mvnrepository.com/artifact/org.iq80.leveldb/leveldb
libraryDependencies += "org.iq80.leveldb" % "leveldb" % "0.12"
libraryDependencies += "org.xerial.snappy" % "snappy-java" % "1.1.7.3"
// missing binary dependency, leveldbjni
//libraryDependencies += "com.typesafe.akka" %% "akka-persistence" % AkkaVersion


// Json Parser (https://github.com/spray/spray-json)
libraryDependencies += "io.spray" %%  "spray-json" % "1.3.5"

// Encryption
libraryDependencies += "com.google.crypto.tink" % "tink" % "1.5.0"

// Scala unit tests
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.9" % Test

// Jackson Databind (for Json Schema Validation)
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.0.0-RC3"

// Json Schema Validator
libraryDependencies += "com.networknt" % "json-schema-validator" % "1.0.60"

conflictManager := ConflictManager.latestCompatible
