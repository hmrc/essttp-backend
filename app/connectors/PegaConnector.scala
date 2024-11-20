/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package connectors

import cats.syntax.either._
import com.google.inject.{Inject, Singleton}
import essttp.rootmodel.pega.PegaCaseId
import models.pega._
import play.api.Logging
import play.api.http.{HeaderNames, MimeTypes}
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.net.URL
import java.util.Base64
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PegaConnector @Inject() (
    httpClient: HttpClientV2,
    config:     ServicesConfig
)(implicit ec: ExecutionContext) extends Logging {

  private val startCaseUrl: URL = url"${config.baseUrl("pega")}/prweb/api/payments/v1/aa/createorupdatecase"

  private val getCaseUrl: URL = url"${config.baseUrl("pega")}/prweb/api/payments/v1/cases"

  private val oauthUrl: URL = url"${config.baseUrl("pega")}/prweb/PRRestService/oauth2/v1/token"

  private val oauthAuthorizationHeaderValue: String = {
    val oauthUserName: String = config.getString("pega.oauth.username")
    val oauthPassword: String = config.getString("pega.oauth.password")
    val toEncode = s"$oauthUserName:$oauthPassword"
    val encoded = Base64.getEncoder.encodeToString(toEncode.getBytes("UTF-8"))

    s"Basic $encoded"
  }

  private val oauthRequestBody: Map[String, String] =
    Map("grant_type" -> "client_credentials")

  private val correlationIdHeaderName: String = "correlationid"

  def getToken()(implicit hc: HeaderCarrier): Future[PegaOauthToken] =
    httpClient
      .post(oauthUrl)
      .withProxy
      .withBody(oauthRequestBody)
      .setHeader(HeaderNames.AUTHORIZATION -> oauthAuthorizationHeaderValue)
      .setHeader(HeaderNames.CONTENT_TYPE -> MimeTypes.FORM)
      .execute[Either[UpstreamErrorResponse, PegaOauthToken]]
      .map(_.leftMap(throw _).merge)

  def startCase(startCaseRequest: PegaStartCaseRequest, pegaToken: String, correlationId: String)(implicit hc: HeaderCarrier): Future[PegaStartCaseResponse] = {
    logger.info(s"Calling PEGA start case with correlation id $correlationId")

    httpClient
      .post(startCaseUrl)
      .withProxy
      .withBody(Json.toJson(startCaseRequest))
      .setHeader(HeaderNames.AUTHORIZATION -> s"Bearer $pegaToken")
      .setHeader(correlationIdHeaderName -> correlationId)
      .execute[Either[UpstreamErrorResponse, PegaStartCaseResponse]]
      .map(_.leftMap(throw _).merge)
  }

  def getCase(caseId: PegaCaseId, pegaToken: String, correlationId: String)(implicit hc: HeaderCarrier): Future[PegaGetCaseResponse] = {
    logger.info(s"Calling PEGA get case with correlation id $correlationId")

    httpClient
      .get(url"$getCaseUrl/${caseId.value}?viewType=none&pageName=GetCaseDetailsWrapper&getBusinessDataOnly=true")
      .withProxy
      .setHeader(correlationIdHeaderName -> correlationId)
      .setHeader(HeaderNames.AUTHORIZATION -> s"Bearer $pegaToken")
      .execute[Either[UpstreamErrorResponse, PegaGetCaseResponse]]
      .map(_.leftMap(throw _).merge)
  }

}
