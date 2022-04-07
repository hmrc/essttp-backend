/*
 * Copyright 2022 HM Revenue & Customs
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

import cats.data.EitherT
import config.AppConfig
import connectors.EligibilityStubConnector.{ServerError, ServiceError}
import essttp.rootmodel.{TaxId, TaxRegime}
import model.OverduePayments
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, UpstreamErrorResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EligibilityStubConnector @Inject() (httpClient: HttpClient, appConfig: AppConfig) {

  def handleUpstreamError(error: UpstreamErrorResponse): ServiceError = error match {
    case UpstreamErrorResponse(msg, code, _, _) => ServerError(msg, code)
  }

  def eligibilityData(regime: TaxRegime, id: TaxId)
    (implicit hc: HeaderCarrier, ec: ExecutionContext): EitherT[Future, ServiceError, OverduePayments] = {

    val response = httpClient
      .GET[Either[UpstreamErrorResponse, OverduePayments]](
        url = url(regime, id)
      )

    EitherT(response).leftMap(handleUpstreamError)
  }

  def url(regime: TaxRegime, id: TaxId): String =
    s"${appConfig.ttpBaseUrl}/essttp-stubs/eligibility/${regime.entryName}/${id.value}/financials"

}

object EligibilityStubConnector {
  sealed trait ServiceError {
    def message: String
  }
  case class ServerError(message: String, code: Int) extends ServiceError

}

