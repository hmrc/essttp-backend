import play.core.PlayVersion
import sbt.*

object AppDependencies {

  val bootstrapVersion = "8.4.0"
  val hmrcMongoVersion = "1.7.0"
  val chimneyVersion = "0.8.5"
  val catsVersion = "2.10.0"
  val playJsonDerivedCodesVersion = "10.1.0"
  val enumeratumVersion = "1.7.0" // breaks with later version
  val enumeratumPlayVersion = "1.8.0"
  val cryptoVersion = "7.6.0"

  /**
   * Note we pull in various libraries from corJourneyDependencies such as:
   * [hmrc-mongo-play-28, crypto-json-play-28, crypto, json-encryption]
   * due to dependsOn and aggregate in build.sbt
   */
  lazy val microserviceDependencies: Seq[ModuleID] = {
    val compile: Seq[ModuleID] = Seq(
      "uk.gov.hmrc"             %% "bootstrap-backend-play-30"  % bootstrapVersion,
      "com.beachape"            %% "enumeratum"                 % enumeratumVersion,
      "org.julienrf"            %% "play-json-derived-codecs"   % playJsonDerivedCodesVersion,
      "io.scalaland"            %% "chimney"                    % chimneyVersion,
      "org.typelevel"           %% "cats-core"                  % catsVersion
    )

    val test: Seq[ModuleID] = Seq(
      "uk.gov.hmrc"             %% "bootstrap-test-play-30"     % bootstrapVersion,
      "org.scalatestplus.play"  %% "scalatestplus-play"         % "7.0.1" ,
      "org.playframework"       %% "play-test"                  % PlayVersion.current,
      "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-30"    % hmrcMongoVersion
    ).map(_ % Test)

    compile ++ test
  }

  lazy val corJourneyDependencies: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"           %% "bootstrap-common-play-30"        % bootstrapVersion % Provided,
    "org.julienrf"          %% "play-json-derived-codecs"        % playJsonDerivedCodesVersion, //choose carefully
    "com.github.kxbmap"     %% "configs"                         % "0.6.1" exclude("org.apache.commons", "commons-text"),
    "com.github.pureconfig" %% "pureconfig"                      % "0.17.4",
    "com.beachape"          %% "enumeratum-play"                 % enumeratumPlayVersion,
    "org.playframework"     %% "play"                            % play.core.PlayVersion.current % Provided,
    "io.scalaland"          %% "chimney"                         % chimneyVersion,
    "org.typelevel"         %% "cats-core"                       % catsVersion,
    "uk.gov.hmrc.mongo"     %% "hmrc-mongo-play-30"              % hmrcMongoVersion,
    "uk.gov.hmrc"           %% "crypto-json-play-30"             % cryptoVersion,
    "uk.gov.hmrc"           %% "payments-email-verification-cor-play-30" % "2.0.0" excludeAll (ExclusionRule(organization = "uk.gov.hmrc"), ExclusionRule(organization = "uk.gov.hmrc.mongo"))
  )

}
