/*
 * Copyright 2023 HM Revenue & Customs
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

package dates

import essttp.rootmodel.dates.extremedates.{ExtremeDatesRequest, ExtremeDatesResponse}
import essttp.rootmodel.dates.startdates.{StartDatesRequest, StartDatesResponse}
import play.api.libs.ws.writeableOf_JsValue
import play.api.libs.json.Json
import testsupport.ItSpec
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpReadsInstances, HttpResponse, StringContextOps}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TestDatesConnector @Inject() (httpClient: HttpClientV2)(implicit executionContext: ExecutionContext) {

  private val essttpBackendBaseUrl = s"http://localhost:${ItSpec.testServerPort.toString}/essttp-backend"

  given HttpReads[HttpResponse] =
    HttpReadsInstances.throwOnFailure(HttpReadsInstances.readEitherOf(using HttpReadsInstances.readRaw))

  def startDates(startDatesRequest: StartDatesRequest)(using HeaderCarrier): Future[StartDatesResponse] =
    httpClient
      .post(url"$essttpBackendBaseUrl/start-dates")
      .withBody(Json.toJson(startDatesRequest))
      .execute[StartDatesResponse]

  def extremeDates(extremeDatesRequest: ExtremeDatesRequest)(using HeaderCarrier): Future[ExtremeDatesResponse] =
    httpClient
      .post(url"$essttpBackendBaseUrl/extreme-dates")
      .withBody(Json.toJson(extremeDatesRequest))
      .execute[ExtremeDatesResponse]

}
