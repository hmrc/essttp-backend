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

package controllers

import essttp.journey.JourneyConnector
import essttp.journey.model.Journey
import testsupport.ItSpec
import testsupport.testdata.TdAll

class UpdateHasConfirmedDirectDebitDetailsControllerSpec extends ItSpec {

  def journeyConnector: JourneyConnector = app.injector.instanceOf[JourneyConnector]

  "POST /journey/:journeyId/update-has-confirmed-direct-debit-details" - {
    "should throw Bad Request when Journey is in a stage [BeforeEnteredDirectDebitDetails]" in new JourneyItTest {
      stubCommonActions()

      journeyConnector.Epaye.startJourneyBta(TdAll.EpayeBta.sjRequest).futureValue
      val result: Throwable = journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"UpdateHasConfirmedDirectDebitDetails is not possible in that state: [Started]"}""")

      verifyCommonActions(numberOfAuthCalls = 2)
    }
    "should return an unchanged journey when Direct debit details have already been confirmed" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterEnteredDirectDebitDetailsNoAffordability().copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))

      val result1: Journey = journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
      result1 shouldBe tdAll.EpayeBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability

      val result2: Journey = journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
      result2 shouldBe tdAll.EpayeBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability

      verifyCommonActions(numberOfAuthCalls = 3)
    }
    "should throw a Bad Request when journey is in stage SubmittedArrangement" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterSubmittedArrangementNoAffordability().copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      val result: Throwable = journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"Cannot update ConfirmedDirectDebitDetails when journey is in completed state"}""")

      verifyCommonActions(numberOfAuthCalls = 1)
    }
  }
}
