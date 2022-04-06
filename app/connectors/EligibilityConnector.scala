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
import connectors.EligibilityConnector.{ServerError, ServiceError, url}
import model.{IdType, OverduePayments, TaxID, TaxRegime}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, UpstreamErrorResponse}
import uk.gov.hmrc.http.HttpReads.Implicits._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EligibilityConnector @Inject() (httpClient: HttpClient, appConfig: AppConfig) {

  def handleUpstreamError(error: UpstreamErrorResponse): ServiceError = error match {
    case UpstreamErrorResponse(msg, code, _, _) => ServerError(msg, code)
  }
  //    case UpstreamErrorResponse(_,400,_,_) => NoMatchingAccount
  //    case UpstreamErrorResponse(_,403,_,_) => AccessDenied
  //    case UpstreamErrorResponse(msg,code,_,_) => BarsResponseError(msg,code)
  //  }

  def eligibilityData(regime: TaxRegime, idType: IdType, id: TaxID)
    (implicit hc: HeaderCarrier, ec: ExecutionContext): EitherT[Future, ServiceError, OverduePayments] = {

    val response = httpClient
      .GET[Either[UpstreamErrorResponse, OverduePayments]](
        url = url(regime, idType, id)
      // url = appConfig.barsPersonalAssessUrl,
      )

    EitherT(response).leftMap(handleUpstreamError)
  }

}

object EligibilityConnector {
  sealed trait ServiceError {
    def message: String
  }

  case class ServerError(message: String, code: Int) extends ServiceError

  def url(regime: TaxRegime, idType: IdType, id: TaxID): String = s"http://localhost:9218/essttp-stubs/eligibility/${regime.name}/${idType.name}/${id.value}/financials"
}

