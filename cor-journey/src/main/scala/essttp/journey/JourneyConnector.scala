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

package essttp.journey

import essttp.journey.model.{Journey, JourneyId, SjRequest, SjResponse}
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import essttp.utils.RequestSupport._
import essttp.utils.HttpReadsInstances._

@Singleton
class JourneyConnector(httpClient: HttpClient, baseUrl: String)(implicit ec: ExecutionContext) {

  def getJourney(journeyId: JourneyId)(implicit request: RequestHeader): Future[Journey] = {
    httpClient.GET[Journey](s"$baseUrl/essttp-backend/journey/${journeyId.value}")
  }

  object Epaye {

    def startJourneyBta(sjRequest: SjRequest.Epaye.Simple)(implicit request: RequestHeader): Future[SjResponse] = {
      httpClient.POST[SjRequest.Epaye.Simple, SjResponse](
        url  = s"$baseUrl/essttp-backend/epaye/bta/journey/start",
        body = sjRequest
      )
    }

    def startJourneyDetachedUrl(sjRequest: SjRequest.Epaye.Empty)(implicit request: RequestHeader): Future[SjResponse] = {
      httpClient.POST[SjRequest, SjResponse](
        url  = s"$baseUrl/essttp-backend/epaye/detached-url/journey/start",
        body = sjRequest
      )
    }

    def startJourneyGovUk(sjRequest: SjRequest.Epaye.Empty)(implicit request: RequestHeader): Future[SjResponse] = {
      httpClient.POST[SjRequest, SjResponse](
        url  = s"$baseUrl/essttp-backend/epaye/gov-uk/journey/start",
        body = sjRequest
      )
    }
  }

  @Inject()
  def this(httpClient: HttpClient, servicesConfig: ServicesConfig)(implicit ec: ExecutionContext) = this(
    httpClient,
    servicesConfig.baseUrl("essttp-backend")
  )
}
