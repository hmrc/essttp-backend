import play.core.PlayVersion
import sbt._

object AppDependencies {

  val bootstrapVersion = "7.23.0"
  val hmrcMongoVersion = "1.3.0"
  val chimneyVersion = "0.8.2"
  val catsVersion = "2.10.0"
  val playJsonDerivedCodesVersion = "7.0.0"
  val enumeratumVersion = "1.7.0" // breaks with later version
  val cryptoVersion = "7.5.0"
  val hmrcJsonEncryptionVersion = "5.2.0-play-28"

  /**
   * Note we pull in various libraries from corJourneyDependencies such as:
   * [hmrc-mongo-play-28, crypto-json-play-28, crypto, json-encryption]
   * due to dependsOn and aggregate in build.sbt
   */
  lazy val microserviceDependencies: Seq[ModuleID] = {
    val compile: Seq[ModuleID] = Seq(
      "uk.gov.hmrc"             %% "bootstrap-backend-play-28"  % bootstrapVersion,
      "com.beachape"            %% "enumeratum"                 % enumeratumVersion,
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
      "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-28"    % hmrcMongoVersion
    ).map(_ % Test)

    compile ++ test
  }

  lazy val corJourneyDependencies: Seq[ModuleID] = Seq(
    //WARN! - The version of `auth-client` has to be exact!
    //make sure it's version is the same as version in bootstrap (`uk.gov.hmrc:bootstrap-backend-play-xx_x.xx:xxx`)
    //run `essttp-backend/dependencyTree::toFile deps.txt -f` and look for that line:
    // +-uk.gov.hmrc:auth-client_2.12:3.0.0-play-27 (evicted by: 5.1.0-play-27)
    //the correct version in this time was `3.0.0`
    "uk.gov.hmrc"           %% "auth-client"                     % "6.2.0-play-28",
    "uk.gov.hmrc"           %% "bootstrap-common-play-28"        % AppDependencies.bootstrapVersion % Provided,
    "org.julienrf"          %% "play-json-derived-codecs"        % AppDependencies.playJsonDerivedCodesVersion, //choose carefully
    "com.github.kxbmap"     %% "configs"                         % "0.6.1" exclude("org.apache.commons", "commons-text"),
    "com.github.pureconfig" %% "pureconfig"                      % "0.17.4",
    "com.beachape"          %% "enumeratum-play"                 % AppDependencies.enumeratumVersion,
    "com.typesafe.play"     %% "play"                            % play.core.PlayVersion.current % Provided,
    "io.scalaland"          %% "chimney"                         % AppDependencies.chimneyVersion,
    "org.typelevel"         %% "cats-core"                       % AppDependencies.catsVersion,
    "uk.gov.hmrc.mongo"     %% "hmrc-mongo-play-28"              % AppDependencies.hmrcMongoVersion,
    "uk.gov.hmrc"           %% "crypto-json-play-28"             % AppDependencies.cryptoVersion,
    "uk.gov.hmrc"           %% "json-encryption"                 % hmrcJsonEncryptionVersion,
    "uk.gov.hmrc"           %% "payments-email-verification-cor" % "1.0.0" excludeAll (ExclusionRule(organization = "uk.gov.hmrc"), ExclusionRule(organization = "uk.gov.hmrc.mongo"))
  )

}
