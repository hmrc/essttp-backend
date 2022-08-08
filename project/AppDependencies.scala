import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val bootstrapVersion = "6.3.0"
  val hmrcMongoVersion = "0.68.0"
  val chimneyVersion = "0.6.1"
  val catsVersion = "2.7.0"
  val playJsonDerivedCodesVersion = "7.0.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-28"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-28"         % hmrcMongoVersion,
    "com.beachape"            %% "enumeratum"                 % "1.6.1",
    "org.julienrf"            %% "play-json-derived-codecs"   % playJsonDerivedCodesVersion, //choose carefully
    "io.scalaland"            %% "chimney"                    % chimneyVersion,
    "org.typelevel"           %% "cats-core"                  % catsVersion
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"     % bootstrapVersion,
    "com.vladsch.flexmark"    %  "flexmark-all"               % "0.36.8",
    "org.scalatestplus.play"  %% "scalatestplus-play"         % "5.1.0" ,
    "org.pegdown"             %  "pegdown"                    % "1.6.0" ,
    "com.typesafe.play"       %% "play-test"                  % PlayVersion.current,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-28"    % "0.58.0",
    "com.vladsch.flexmark"    %  "flexmark-all"               % "0.35.10", // Required to stay at this version - see https://github.com/scalatest/scalatest/issues/1736
  ).map(_ % Test)

  def apply() = compile ++ test
}
