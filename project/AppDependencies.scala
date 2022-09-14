import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val bootstrapVersion = "7.1.0"
  val hmrcMongoVersion = "0.71.0"
  val chimneyVersion = "0.6.2"
  val catsVersion = "2.8.0"
  val playJsonDerivedCodesVersion = "7.0.0"
  val enumeratumVersion = "1.7.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-28"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-28"         % hmrcMongoVersion,
    "com.beachape"            %% "enumeratum"                 % enumeratumVersion,
    "org.julienrf"            %% "play-json-derived-codecs"   % playJsonDerivedCodesVersion, //choose carefully
    "io.scalaland"            %% "chimney"                    % chimneyVersion,
    "org.typelevel"           %% "cats-core"                  % catsVersion,
    "uk.gov.hmrc"             %% "crypto-json-play-28"        % bootstrapVersion,
    "uk.gov.hmrc"             %% "crypto"                     % bootstrapVersion,
    "uk.gov.hmrc"             %% "json-encryption"            % "5.1.0-play-28"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"     % bootstrapVersion,
    "com.vladsch.flexmark"    %  "flexmark-all"               % "0.36.8",
    "org.scalatestplus.play"  %% "scalatestplus-play"         % "5.1.0" ,
    "org.pegdown"             %  "pegdown"                    % "1.6.0" ,
    "com.typesafe.play"       %% "play-test"                  % PlayVersion.current,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-28"    % hmrcMongoVersion,
  ).map(_ % Test)

  def apply(): Seq[sbt.ModuleID] = compile ++ test
}
