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

package journey

import essttp.journey.JourneyConnector
import essttp.journey.model.{Journey, JourneyId}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import testsupport.ItSpec
import testsupport.testdata.TdAll
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, SessionId, SessionKeys, StringContextOps}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2

import java.time.Clock
import java.util.UUID

class FindLatestJourneyBySessionIdSpec extends ItSpec {

  override val overrideClock: Option[Clock] = Some(Clock.systemDefaultZone())

  def journeyConnector: JourneyConnector = app.injector.instanceOf[JourneyConnector]

  def makeHeaderCarrier(sessionId: SessionId): HeaderCarrier =
    HeaderCarrier(sessionId = Some(sessionId), authorization = Some(TdAll.authorization))

  "return a 500 when no sessionId provided" in {
    stubCommonActions()

    val httpClient = app.injector.instanceOf[HttpClientV2]
    val response   = httpClient
      .get(url"$baseUrl/essttp-backend/journey/find-latest-by-session-id")
      .setHeader("Authorization" -> TdAll.authorization.value)
      .execute[HttpResponse]
      .futureValue
    response.status shouldBe 500
    response.body shouldBe """{"statusCode":500,"message":"Missing required 'SessionId'"}"""

    verifyCommonActions(numberOfAuthCalls = 1)
  }

  "connector fails to make request if no sessionId provided" in {
    val thrown = journeyConnector.findLatestJourneyBySessionId().failed.futureValue
    thrown.getMessage should include("Missing required 'SessionId'")

    verifyCommonActions(numberOfAuthCalls = 0)
  }

  "find a single journey" in {
    def startJourney(sessionId: SessionId): JourneyId = {
      given FakeRequest[AnyContentAsEmpty.type] =
        TdAll.request.withSession(SessionKeys.sessionId -> sessionId.value)
      val sjRequest                             = TdAll.EpayeBta.sjRequest
      val journeyId                             = journeyConnector.Epaye.startJourneyBta(sjRequest).futureValue.journeyId
      journeyId
    }

    stubCommonActions()

    val sessionId       = SessionId(s"session-${UUID.randomUUID().toString}")
    given HeaderCarrier = makeHeaderCarrier(sessionId)

    val previousJourneyId = startJourney(sessionId) // there is only 1 journey in mongo with the sessionId
    val result1           = journeyConnector.findLatestJourneyBySessionId().futureValue.value
    result1.journeyId shouldBe previousJourneyId

    val latterJourneyId = startJourney(sessionId) // now there are 2 journeys with the same sessionId
    val result2         = journeyConnector.findLatestJourneyBySessionId().futureValue.value
    result2.journeyId shouldBe latterJourneyId
    result2.journeyId shouldNot be(previousJourneyId)

    verifyCommonActions(numberOfAuthCalls = 4)
  }

  "find a single journey - Not Found" in {
    stubCommonActions()

    val sessionId                = SessionId("i-have-no-session-id")
    given HeaderCarrier          = makeHeaderCarrier(sessionId)
    val journey: Option[Journey] = journeyConnector.findLatestJourneyBySessionId().futureValue
    journey shouldBe None

    verifyCommonActions(numberOfAuthCalls = 1)
  }

}
