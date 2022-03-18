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
import essttp.journey.model.JourneyId
import essttp.testdata.TdAll
import testsupport.ItSpec

class JourneyControllerSpec extends ItSpec {

  lazy val journeyConnector: JourneyConnector = app.injector.instanceOf[JourneyConnector]

  "start journey EpayeBta" in {
    val tdAll = new TdAll {
      override val journeyId: JourneyId = journeyIdGenerator.readNextJourneyId()
    }
    implicit val request = tdAll.request
    val response = journeyConnector.Epaye.startJourneyBta(tdAll.EpayeBta.sjRequest).futureValue

    response shouldBe tdAll.EpayeBta.sjResponse
    journeyConnector.getJourney(response.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterStarted
  }

  "start journey EpayeGovUk" in {
    val tdAll = new TdAll {
      override val journeyId: JourneyId = journeyIdGenerator.readNextJourneyId()
    }
    implicit val request = tdAll.request
    val response = journeyConnector.Epaye.startJourneyGovUk(tdAll.EpayeGovUk.sjRequest).futureValue

    response shouldBe tdAll.EpayeGovUk.sjResponse
    journeyConnector.getJourney(response.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterStarted
  }

  "start journey EpayeDetachedUrl" in {
    val tdAll = new TdAll {
      override val journeyId: JourneyId = journeyIdGenerator.readNextJourneyId()
    }
    implicit val request = tdAll.request
    val response = journeyConnector.Epaye.startJourneyDetachedUrl(tdAll.EpayeDetachedUrl.sjRequest).futureValue

    response shouldBe tdAll.EpayeDetachedUrl.sjResponse
    journeyConnector.getJourney(response.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterStarted
  }

}
