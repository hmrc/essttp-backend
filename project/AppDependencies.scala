import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  //https://github.com/hmrc/bootstrap-play
  val bootstrapVersion = "5.20.0"

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-28"  % bootstrapVersion,
    "uk.gov.hmrc"             %% "simple-reactivemongo"       % "8.0.0-play-28",
    "com.beachape"            %% "enumeratum"                 % "1.6.1",
//    "org.julienrf"            %% "play-json-derived-codecs" % "6.0.0", //choose carefully
    "io.scalaland"            %% "chimney" % "0.6.1",
    "org.typelevel"              %% "cats-core"                  % "2.7.0"
  )

  val test = Seq(
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
