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

package journey

import essttp.journey.JourneyConnector
import essttp.journey.model.{Journey, JourneyId}
import essttp.testdata.TdAll
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import testsupport.ItSpec
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse, SessionId}
import uk.gov.hmrc.http.HttpReads.Implicits._

import java.time.Clock

class FindLatestJourneyBySessionIdSpec extends ItSpec {

  override val clock: Clock = Clock.systemDefaultZone()

  def journeyConnector: JourneyConnector = app.injector.instanceOf[JourneyConnector]

  def makeHeaderCarrier(sessionId: SessionId): HeaderCarrier =
    HeaderCarrier(sessionId = Some(SessionId(sessionId.value)))

  "return a 500 when no sessionId provided" in {
    val httpClient = app.injector.instanceOf[HttpClient]
    val response = httpClient.GET[HttpResponse](s"$baseUrl/essttp-backend/journey/find-latest-by-session-id").futureValue
    response.status shouldBe 500
    response.body shouldBe """{"statusCode":500,"message":"Missing required 'SessionId'"}"""
  }

  "connector fails to make request if no sessionId provided" in {
    val thrown = journeyConnector.findLatestJourneyBySessionId().failed.futureValue
    thrown.getMessage should include("Missing required 'SessionId'")
  }

  "find a single journey" in {

    def startJourney(sessionId: SessionId): JourneyId = {
      implicit val hc: HeaderCarrier = makeHeaderCarrier(sessionId)
      implicit val request: FakeRequest[AnyContentAsEmpty.type] = TdAll.request
      val sjRequest = TdAll.EpayeBta.sjRequest
      val journeyId = journeyConnector.Epaye.startJourneyBta(sjRequest).futureValue.journeyId
      journeyId
    }

    val sessionId = SessionId("session-2082fcd4-70f6-49cc-a4bf-845917981cd7")
    val previousJourneyId = startJourney(sessionId) //there is only 1 journey in mongo with the sessionId
    Thread.sleep(100)
    val latterJourneyId = startJourney(sessionId) //now there are 2 journeys with the same sessionId
    //end of test setup

    implicit val hc: HeaderCarrier = makeHeaderCarrier(sessionId)

    Thread.sleep(1000)
    val journey = journeyConnector.findLatestJourneyBySessionId().futureValue.value
    val journeyId = journey._id
    journeyId shouldBe latterJourneyId
    journeyId should not be previousJourneyId
  }

  "find a single journey - Not Found" in {
    val sessionId = SessionId("i-have-no-session-id")
    implicit val hc: HeaderCarrier = makeHeaderCarrier(sessionId)
    val journey: Option[Journey] = journeyConnector.findLatestJourneyBySessionId().futureValue
    journey shouldBe None
  }

}
