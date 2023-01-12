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
import testsupport.ItSpec
import testsupport.testdata.TdAll

class UpdateInstalmentPlanControllerSpec extends ItSpec {

  def journeyConnector: JourneyConnector = app.injector.instanceOf[JourneyConnector]

  "POST /journey/:journeyId/update-selected-plan" - {
    "should throw Bad Request when Journey is in a stage [BeforeAffordableQuotesResponse]" in new JourneyItTest {
      stubCommonActions()

      journeyConnector.Epaye.startJourneyBta(TdAll.EpayeBta.sjRequest).futureValue
      val result: Throwable = journeyConnector.updateChosenPaymentPlan(tdAll.journeyId, TdAll.EpayeBta.updateSelectedPaymentPlanRequest()).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"UpdateSelectedPaymentPlan is not possible in that state: [Started]"}""")

      verifyCommonActions(numberOfAuthCalls = 2)
    }
    "should not update the journey when Selected Plan hasn't changed" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterAffordableQuotesResponse.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))

      val result1 = journeyConnector.updateChosenPaymentPlan(tdAll.journeyId, TdAll.EpayeBta.updateSelectedPaymentPlanRequest()).futureValue
      result1 shouldBe tdAll.EpayeBta.journeyAfterSelectedPaymentPlan
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterSelectedPaymentPlan

      val result2 = journeyConnector.updateChosenPaymentPlan(tdAll.journeyId, TdAll.EpayeBta.updateSelectedPaymentPlanRequest()).futureValue
      result2 shouldBe tdAll.EpayeBta.journeyAfterSelectedPaymentPlan
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterSelectedPaymentPlan

      verifyCommonActions(numberOfAuthCalls = 4)
    }
    "should update the journey when Selected Plan has changed" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterAffordableQuotesResponse.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))

      val result1 = journeyConnector.updateChosenPaymentPlan(tdAll.journeyId, TdAll.EpayeBta.updateSelectedPaymentPlanRequest()).futureValue
      result1 shouldBe tdAll.EpayeBta.journeyAfterSelectedPaymentPlan
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterSelectedPaymentPlan

      val result2 = journeyConnector.updateChosenPaymentPlan(tdAll.journeyId, TdAll.paymentPlan(2)).futureValue
      val expectedUpdatedJourney2 = tdAll.EpayeBta.journeyAfterSelectedPaymentPlan.copy(selectedPaymentPlan = TdAll.paymentPlan(2))
      result2 shouldBe expectedUpdatedJourney2
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe expectedUpdatedJourney2

      verifyCommonActions(numberOfAuthCalls = 4)
    }
    "should throw a Bad Request when journey is in stage SubmittedArrangement" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterSubmittedArrangement().copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      val result: Throwable = journeyConnector.updateChosenPaymentPlan(tdAll.journeyId, tdAll.EpayeBta.updateSelectedPaymentPlanRequest()).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"Cannot update ChosenPlan when journey is in completed state"}""")

      verifyCommonActions(numberOfAuthCalls = 1)
    }
  }
}
