import sbt.Tests.{Group, SubProcess}
import uk.gov.hmrc.DefaultBuildSettings.{scalaSettings, targetJvm}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import wartremover.WartRemover.autoImport.wartremoverExcluded
import uk.gov.hmrc.ShellPrompt

val appName = "essttp-backend"
val majorVer = 1

Global / onChangedBuildSource := ReloadOnSourceChanges

val appScalaVersion = "2.12.15"

lazy val playSettings : Seq[Setting[_]] = Seq.empty

lazy val scalaCompilerOptions = Seq(
  "-Xfatal-warnings",
  "-Xlint:-missing-interpolator,_",
  "-Yno-adapted-args",
  "-Ywarn-value-discard",
  "-Ywarn-dead-code",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-language:implicitConversions",
  "-Ypartial-unification", //required by cats
  "-Wconf:cat=unused-imports&src=html/.*:s",
  "-Wconf:src=routes/.*:s"
)

def oneForkedJvmPerTest(tests: Seq[TestDefinition]): Seq[Group] = {
  tests.map { test =>
    new Group(test.name, Seq(test), SubProcess(ForkOptions().withRunJVMOptions(Vector(s"-Dtest.name=${test.name}"))))
  }
}

lazy val commonSettings = Seq(
  majorVersion := majorVer,
  scalacOptions ++= scalaCompilerOptions,
  update / evictionWarningOptions := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
  ScalariformSettings.scalariformSettings,
  shellPrompt := ShellPrompt(version.value),
  buildInfoPackage := name.value.toLowerCase().replaceAllLiterally("-", ""),
  targetJvm := "jvm-1.8",
  Compile / doc / scalacOptions := Seq(), //this will allow to have warnings in `doc` task and not fail the build
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.jcenterRepo
  )
).++(WartRemoverSettings.wartRemoverSettings)
  .++(ScoverageSettings.scoverageSettings)
  .++(scalaSettings)
  .++(uk.gov.hmrc.DefaultBuildSettings.defaultSettings())
  .++(SbtUpdatesSettings.sbtUpdatesSettings)

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin, SbtAutoBuildPlugin, SbtGitVersioning)
  .settings(commonSettings: _*)
  .settings(playSettings : _*)
  .settings(PlayKeys.playDefaultPort := 9216)
  .settings(
    scalaVersion := appScalaVersion,
    libraryDependencies ++= AppDependencies.microserviceDependencies,
    Test / parallelExecution := false,
    Test / fork := false,
    routesImport ++= Seq("essttp.journey.model._"),
    wartremoverExcluded ++= (Compile / routes).value
  )
  .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)
  .settings(
    commands += Command.command("runTestOnly") { state =>
      state.globalLogging.full.info("running play using 'testOnlyDoNotUseInAppConf' routes...")
      s"""set javaOptions += "-Dplay.http.router=testOnlyDoNotUseInAppConf.Routes"""" ::
        "run" ::
        s"""set javaOptions -= "-Dplay.http.router=testOnlyDoNotUseInAppConf.Routes"""" ::
        state
    }
  )
  .dependsOn(corJourney, corTestData)
  .aggregate(corJourney, corTestData)
  .settings(publishingSettings: _*)
  .settings(resolvers += Resolver.jcenterRepo)


/**
 * Collection Of Routines - the common journey
 */
lazy val corJourney = Project(appName + "-cor-journey", file("cor-journey"))
  .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)
  .settings(commonSettings: _*)
  .settings(
    scalaVersion := appScalaVersion,
    majorVersion := majorVer,
    libraryDependencies ++= AppDependencies.corJourneyDependencies
  )

/**
 * Collection Of Routines - test data
 */
lazy val corTestData = Project(appName + "-cor-test-data", file("cor-test-data"))
  .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)
  .settings(commonSettings: _*)
  .settings(
    scalaVersion := appScalaVersion,
    majorVersion := majorVer,
    libraryDependencies ++= AppDependencies.corTestDataDependencies
  )
  .dependsOn(corJourney)
  .aggregate(corJourney)
