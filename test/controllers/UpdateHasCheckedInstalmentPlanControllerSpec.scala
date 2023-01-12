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

class UpdateHasCheckedInstalmentPlanControllerSpec extends ItSpec {

  def journeyConnector: JourneyConnector = app.injector.instanceOf[JourneyConnector]

  "POST /journey/:journeyId/update-has-checked-plan" - {
    "should throw Bad Request when Journey is in a stage [BeforeSelectedPaymentPlan]" in new JourneyItTest {
      stubCommonActions()

      journeyConnector.Epaye.startJourneyBta(TdAll.EpayeBta.sjRequest).futureValue
      val result: Throwable = journeyConnector.updateHasCheckedPaymentPlan(tdAll.journeyId).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"UpdateHasCheckedInstalmentPlan is not possible in that state: [Started]"}""")

      verifyCommonActions(numberOfAuthCalls = 2)
    }
    "should return an unchanged journey whenSelected Plan has already been confirmed but not submitted to ttp" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterSelectedPaymentPlan.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))

      val result1 = journeyConnector.updateHasCheckedPaymentPlan(tdAll.journeyId).futureValue
      result1 shouldBe tdAll.EpayeBta.journeyAfterCheckedPaymentPlan
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterCheckedPaymentPlan

      val result2 = journeyConnector.updateHasCheckedPaymentPlan(tdAll.journeyId).futureValue
      result2 shouldBe tdAll.EpayeBta.journeyAfterCheckedPaymentPlan

      verifyCommonActions(numberOfAuthCalls = 3)
    }
    "should throw a Bad Request when journey is in stage SubmittedArrangement" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterSubmittedArrangement().copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      val result: Throwable = journeyConnector.updateHasCheckedPaymentPlan(tdAll.journeyId).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"Cannot update HasCheckedPaymentPlan when journey is in completed state"}""")

      verifyCommonActions(numberOfAuthCalls = 1)
    }
  }
}
