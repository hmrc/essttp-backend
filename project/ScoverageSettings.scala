import sbt.Def
import scoverage.ScoverageKeys

object ScoverageSettings {

  lazy val scoverageSettings: Seq[Def.SettingsDefinition] =  Seq(
    ScoverageKeys.coverageExcludedPackages := """uk\.gov\.hmrc\.BuildInfo;.*\.Routes;.*\.RoutesPrefix;.*\.Reverse[^.]*;testonly;.*essttp\.testdata""",
    ScoverageKeys.coverageMinimumStmtTotal := 90.00,
    ScoverageKeys.coverageExcludedFiles := "<empty>;.*javascript.*;.*models.*;.*Routes.*;.*testonly.*;.*DatesTdAll;.*JourneyLogger;.*TdJourney.*",
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )

}
