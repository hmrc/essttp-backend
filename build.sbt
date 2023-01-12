import sbt.Tests.{Group, SubProcess}
import uk.gov.hmrc.DefaultBuildSettings.{scalaSettings, targetJvm}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import wartremover.WartRemover.autoImport.wartremoverExcluded
import uk.gov.hmrc.ShellPrompt
import sbt.Resolver

val appName = "essttp-backend"
val majorVer = 1

Global / onChangedBuildSource := ReloadOnSourceChanges

val appScalaVersion = "2.13.8"

lazy val playSettings : Seq[Setting[_]] = Seq.empty

lazy val scalaCompilerOptions = Seq(
  "-Xfatal-warnings",
  "-Xlint:-missing-interpolator,_",
  "-Xlint:adapted-args",
  "-Xlint:-byname-implicit",
  "-Ywarn-unused:implicits",
  "-Ywarn-unused:imports",
  "-Ywarn-unused:locals",
  "-Ywarn-unused:params",
  "-Ywarn-unused:patvars",
  "-Ywarn-unused:privates",
  "-Ywarn-value-discard",
  "-Ywarn-dead-code",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-language:implicitConversions",
  "-Wconf:cat=unused-imports&src=html/.*:s",
  "-Wconf:src=routes/.*:s"
)

def oneForkedJvmPerTest(tests: Seq[TestDefinition]): Seq[Group] = {
  tests.map { test =>
    new Group(test.name, Seq(test), SubProcess(ForkOptions().withRunJVMOptions(Vector(s"-Dtest.name=${test.name}"))))
  }
}

lazy val projectResolvers: Seq[MavenRepository] = Seq(
  Resolver.sonatypeOssRepos("releases"),
  Seq(Resolver.jcenterRepo)
).flatten

lazy val commonSettings = Seq(
  majorVersion := majorVer,
  scalacOptions ++= scalaCompilerOptions,
  update / evictionWarningOptions := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
  ScalariformSettings.scalariformSettings,
  shellPrompt := ShellPrompt(version.value),
  buildInfoPackage := name.value.toLowerCase().replaceAllLiterally("-", ""),
  targetJvm := "jvm-1.8",
  Compile / doc / scalacOptions := Seq(), //this will allow to have warnings in `doc` task and not fail the build
  resolvers ++= projectResolvers
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
  .dependsOn(corJourney)
  .aggregate(corJourney)
  .settings(publishingSettings: _*)


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
