import sbt.Def
import scoverage.ScoverageKeys

object ScoverageSettings {


  private val excludedPackages: Seq[String] = Seq(
    "uk\\.gov\\.hmrc\\.BuildInfo.*",
    ".*Routes.*",
    ".*Reverse.*",
    "testonly.*",
    "testOnlyDoNotUseInAppConf.*",
    ".*Module.*",
    ".*.modules",
    ".*\\.utils",
    ".*\\$anon.*",
    "<empty>",
    ".*javascript"
  )

  lazy val scoverageSettings: Seq[Def.SettingsDefinition] =  Seq(
    ScoverageKeys.coverageExcludedPackages :=  excludedPackages.mkString(";"),
    ScoverageKeys.coverageMinimumStmtTotal := 90.00,
 //   ScoverageKeys.coverageExcludedFiles := "<empty>;.*javascript.*;.*models.*;.*Routes.*;.*Reverse.*;.*testonly.*;.*DatesTdAll;.*JourneyLogger;.*TdJourney.*;.*Module.*",
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )

}
