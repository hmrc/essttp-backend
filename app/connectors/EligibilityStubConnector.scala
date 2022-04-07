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
import connectors.EligibilityStubConnector.StubTaxRegime.EPaye
import connectors.EligibilityStubConnector.TaxID.EmpRef
import connectors.EligibilityStubConnector.{ServerError, ServiceError, asStubTaxRegime, url}
import essttp.rootmodel.TaxRegime._
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
        url = url(asStubTaxRegime(regime), id)
      // url = appConfig.barsPersonalAssessUrl,
      )

    EitherT(response).leftMap(handleUpstreamError)
  }

}

object EligibilityStubConnector {
  sealed trait ServiceError {
    def message: String
  }

  case class ServerError(message: String, code: Int) extends ServiceError

  def url(regime: StubTaxRegime, id: TaxId): String = s"http://localhost:9218/essttp-stubs/eligibility/${regime.name}/${id.value}/financials"

  sealed trait TaxID {
    def value: String
  }

  object TaxID {
    case class EmpRef(value: String) extends TaxID
  }

  sealed trait StubTaxRegime {
    def name: String

    def taxIdOf(value: String): TaxID
  }

  object StubTaxRegime {

    def regimeOf(name: String): StubTaxRegime = name.toLowerCase() match {
      case "epaye" => EPaye
      case n       => throw new IllegalArgumentException(s"$n is not the name of a tax regime")
    }

    object EPaye extends StubTaxRegime {
      override def name: String = "EPaye"

      def taxIdOf(value: String): TaxID = EmpRef(value)
    }

  }

  def asStubTaxRegime(regime: TaxRegime): StubTaxRegime = regime match {
    case Epaye => EPaye

    case Vat   => throw new IllegalArgumentException("not defined yet")
  }

}

