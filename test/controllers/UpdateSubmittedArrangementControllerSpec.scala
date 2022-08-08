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

package controllers

import essttp.journey.JourneyConnector
import essttp.testdata.TdAll
import testsupport.ItSpec

class UpdateSubmittedArrangementControllerSpec extends ItSpec {

  def journeyConnector: JourneyConnector = app.injector.instanceOf[JourneyConnector]

  "POST /journey/:journeyId/update-arrangement" - {
    "should throw Bad Request when Journey is in a stage [BeforeAgreedTermsAndConditions]" in new JourneyItTest {
      journeyConnector.Epaye.startJourneyBta(TdAll.EpayeBta.sjRequest).futureValue
      val result: Throwable = journeyConnector.updateArrangement(tdAll.journeyId, TdAll.EpayeBta.updateArrangementRequest()).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"UpdateArrangement is not possible if the user hasn't agreed to the terms and conditions, state: [Started]"}""")
    }
    "should throw a Bad Request when journey is already in stage SubmittedArrangement" in new JourneyItTest {
      insertJourneyForTest(TdAll.EpayeBta.journeyAfterSubmittedArrangement.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      val result: Throwable = journeyConnector.updateArrangement(tdAll.journeyId, tdAll.EpayeBta.updateArrangementRequest()).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"Cannot update SubmittedArrangement when journey is in completed state"}""")
    }
  }
}
