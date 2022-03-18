import _root_.play.sbt.routes.RoutesKeys._
import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import play.core.PlayVersion
import play.sbt.PlayImport.PlayKeys
import sbt.Keys._
import sbt.Tests.{Group, SubProcess}
import scalariform.formatter.preferences._
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings, scalaSettings, targetJvm}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import uk.gov.hmrc.versioning.SbtGitVersioning
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion
import wartremover.Wart
import wartremover.WartRemover.autoImport.{wartremoverErrors, wartremoverExcluded, wartremoverWarnings}
import uk.gov.hmrc.ShellPrompt

val appName = "essttp-backend"


Global / onChangedBuildSource := ReloadOnSourceChanges

val appScalaVersion = "2.12.12"

lazy val appDependencies : Seq[ModuleID] = AppDependencies()
lazy val playSettings : Seq[Setting[_]] = Seq.empty

lazy val commonSettings = Seq(
  majorVersion := 1,
  scalacOptions ++= scalaCompilerOptions,
  resolvers ++= Seq(Resolver.bintrayRepo("hmrc", "releases"), Resolver.jcenterRepo),
  update / evictionWarningOptions := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
  scalariformSettings,
  shellPrompt := ShellPrompt(version.value),
  buildInfoPackage := name.value.toLowerCase().replaceAllLiterally("-", ""),
  targetJvm := "jvm-1.8",
  Compile / doc / scalacOptions := Seq(), //this will allow to have warnings in `doc` task and not fail the build
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.bintrayRepo("hmrc", "releases"),
    Resolver.jcenterRepo,
    "third-party-maven-releases" at "https://artefacts.tax.service.gov.uk/artifactory/third-party-maven-releases/",
    "hmrc-releases-local" at "https://artefacts.tax.service.gov.uk/artifactory/hmrc-releases-local"
  )
).++(wartRemoverSettings)
  .++(scoverageSettings)
  .++(scalaSettings)
  .++(uk.gov.hmrc.DefaultBuildSettings.defaultSettings())

lazy val scalariformSettings =
// description of options found here -> https://github.com/scala-ide/scalariform
  ScalariformKeys.preferences := ScalariformKeys.preferences.value
    .setPreference(AlignArguments, true)
    .setPreference(AlignParameters, true)
    .setPreference(AlignSingleLineCaseStatements, true)
    .setPreference(AllowParamGroupsOnNewlines, true)
    .setPreference(CompactControlReadability, false)
    .setPreference(CompactStringConcatenation, false)
    .setPreference(DanglingCloseParenthesis, Force)
    .setPreference(DoubleIndentConstructorArguments, true)
    .setPreference(DoubleIndentMethodDeclaration, true)
    .setPreference(FirstArgumentOnNewline, Force)
    .setPreference(FirstParameterOnNewline, Force)
    .setPreference(FormatXml, true)
    .setPreference(IndentLocalDefs, true)
    .setPreference(IndentPackageBlocks, true)
    .setPreference(IndentSpaces, 2)
    .setPreference(IndentWithTabs, false)
    .setPreference(MultilineScaladocCommentsStartOnFirstLine, false)
    .setPreference(NewlineAtEndOfFile, true)
    .setPreference(PlaceScaladocAsterisksBeneathSecondAsterisk, false)
    .setPreference(PreserveSpaceBeforeArguments, true)
    .setPreference(RewriteArrowSymbols, false)
    .setPreference(SpaceBeforeColon, false)
    .setPreference(SpaceBeforeContextColon, false)
    .setPreference(SpaceInsideBrackets, false)
    .setPreference(SpaceInsideParentheses, false)
    .setPreference(SpacesAroundMultiImports, false)
    .setPreference(SpacesWithinPatternBinders, true)

lazy val scoverageSettings =
  Seq(
    ScoverageKeys.coverageExcludedPackages := """uk\.gov\.hmrc\.BuildInfo;.*\.Routes;.*\.RoutesPrefix;.*\.Reverse[^.]*;testonly""",
    ScoverageKeys.coverageMinimum := 70.00,
    ScoverageKeys.coverageExcludedFiles := "<empty>;.*javascript.*;.*models.*;.*Routes.*;.*testonly.*",
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )

lazy val wartRemoverSettings = {
  val wartRemoverWarning = {
    val warningWarts = Seq(
      Wart.JavaSerializable,
      Wart.StringPlusAny,
      Wart.AsInstanceOf,
      Wart.IsInstanceOf
      //Wart.Any
    )
    Compile / compile / wartremoverWarnings ++= warningWarts
  }

  val wartRemoverError = {
    // Error
    val errorWarts = Seq(
      Wart.StringPlusAny,
      Wart.AsInstanceOf,
      Wart.IsInstanceOf,
      Wart.ArrayEquals,
      //      Wart.Any,
      Wart.AnyVal,
      Wart.EitherProjectionPartial,
      Wart.Enumeration,
      Wart.ExplicitImplicitTypes,
      Wart.FinalVal,
      Wart.JavaConversions,
      Wart.JavaSerializable,
      Wart.LeakingSealed,
      Wart.MutableDataStructures,
      Wart.Null,
      Wart.OptionPartial,
      Wart.Recursion,
      Wart.Return,
      Wart.TraversableOps,
      Wart.TryPartial,
      Wart.Var,
      Wart.While)

    Compile / compile / wartremoverErrors  ++= errorWarts
  }

  Seq(
    wartRemoverError,
    wartRemoverWarning,
    Test / compile / wartremoverErrors --= Seq(Wart.Any, Wart.Equals, Wart.Null, Wart.NonUnitStatements, Wart.PublicInference),
    wartremoverExcluded ++= (baseDirectory.value / "test").get
  )
  Seq()
}

lazy val scalaCompilerOptions = Seq(
  "-Xfatal-warnings",
  "-Ywarn-unused:-imports,-patvars,-privates,-locals,-explicits,-implicits,_",
  "-Xlint:-missing-interpolator,_",
  "-Yno-adapted-args",
  "-Ywarn-value-discard",
  "-Ywarn-dead-code",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-language:implicitConversions",
  "-Ypartial-unification" //required by cats
)

def oneForkedJvmPerTest(tests: Seq[TestDefinition]): Seq[Group] = {
  tests.map { test =>
    new Group(test.name, Seq(test), SubProcess(ForkOptions().withRunJVMOptions(Vector(s"-Dtest.name=${test.name}"))))
  }
}

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin, SbtAutoBuildPlugin, SbtGitVersioning)
  .settings(commonSettings)
  .settings(playSettings : _*)
  .settings(PlayKeys.playDefaultPort := 9066)
  .settings(
    scalaVersion := appScalaVersion,
    libraryDependencies ++= appDependencies,
    Test / parallelExecution := false,
    Test / fork := false,
    wartremoverExcluded ++= (Compile / routes).value,
    routesImport ++= Seq(
//      "essttp.corjourney.util.ValueClassBinder._",
      "essttp.journey.model._",
    )
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
  .settings(
    majorVersion                     := 0,
    scalaVersion                     := appScalaVersion,
    libraryDependencies              ++= AppDependencies.compile ++ AppDependencies.test
  )
  .settings(publishingSettings: _*)
  .settings(resolvers += Resolver.jcenterRepo)

/**
 * Collection Of Routines - the common journey
 */
lazy val corJourney = Project(appName + "-cor-journey", file("cor-journey"))
  .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)
  .settings(commonSettings)
  .settings(
    scalaVersion := appScalaVersion,
    libraryDependencies ++= List(

      //WARN! - The version of `auth-client` has to be exact!

      //make sure it's version is the same as version in bootstrap (`uk.gov.hmrc:bootstrap-backend-play-xx_x.xx:xxx`)
      //run `essttp-backend/dependencyTree::toFile deps.txt -f` and look for that line:
      // +-uk.gov.hmrc:auth-client_2.12:3.0.0-play-27 (evicted by: 5.1.0-play-27)
      //the correct version in this time was `3.0.0`
      "uk.gov.hmrc" %% "auth-client" % "5.10.0-play-28",
      "uk.gov.hmrc" %% "bootstrap-common-play-28" % AppDependencies.bootstrapVersion % Provided,

      "org.julienrf" %% "play-json-derived-codecs" % "6.0.0", //choose carefully
      "com.github.kxbmap" %% "configs" % "0.4.4",
      "com.github.pureconfig" %% "pureconfig" % "0.12.2",
      "com.beachape" %% "enumeratum-play" % "1.5.15",
      "com.typesafe.play" %% "play" % play.core.PlayVersion.current % Provided,
      "io.scalaland" %% "chimney" % "0.6.1"
    )
  )

/**
 * Collection Of Routines - test data
 */
lazy val corTestData = Project(appName + "-cor-test-data", file("cor-test-data"))
  .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)
  .settings(commonSettings)
  .settings(
    scalaVersion := appScalaVersion,
    libraryDependencies ++= List(
      "com.typesafe.play" %% "play"      % play.core.PlayVersion.current % Provided,
      "com.typesafe.play" %% "play-test" % play.core.PlayVersion.current % Provided,
    )
  )
  .dependsOn(corJourney)
  .aggregate(corJourney)
