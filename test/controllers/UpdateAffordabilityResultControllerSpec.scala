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
import essttp.rootmodel.AmountInPence
import essttp.testdata.TdAll
import testsupport.ItSpec

class UpdateAffordabilityResultControllerSpec extends ItSpec {

  def journeyConnector: JourneyConnector = app.injector.instanceOf[JourneyConnector]

  "POST /journey/:journeyId/update-affordability-result" - {
    "should throw Bad Request when Journey is in a stage [BeforeExtremeDatesResponse]" in new JourneyItTest {
      stubCommonActions()

      journeyConnector.Epaye.startJourneyBta(TdAll.EpayeBta.sjRequest).futureValue
      val result: Throwable = journeyConnector.updateAffordabilityResult(tdAll.journeyId, TdAll.EpayeBta.updateInstalmentAmountsRequest()).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"UpdateAffordabilityResult update is not possible in that state: [Started]"}""")

      verifyCommonActions(numberOfAuthCalls = 2)
    }

    "should not update the journey when Instalment amounts response hasn't changed" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterExtremeDates.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      journeyConnector.updateAffordabilityResult(tdAll.journeyId, TdAll.EpayeBta.updateInstalmentAmountsRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterInstalmentAmounts
      journeyConnector.updateAffordabilityResult(tdAll.journeyId, TdAll.EpayeBta.updateInstalmentAmountsRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterInstalmentAmounts

      verifyCommonActions(numberOfAuthCalls = 4)
    }
    "should update the journey when Instalment amounts response has changed" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterExtremeDates.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      journeyConnector.updateAffordabilityResult(tdAll.journeyId, TdAll.EpayeBta.updateInstalmentAmountsRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterInstalmentAmounts
      journeyConnector.updateAffordabilityResult(tdAll.journeyId, TdAll.EpayeBta.updateInstalmentAmountsRequest().copy(minimumInstalmentAmount = AmountInPence(999))).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterInstalmentAmounts.copy(instalmentAmounts = this.tdAll.instalmentAmounts.copy(minimumInstalmentAmount = AmountInPence(999)))

      verifyCommonActions(numberOfAuthCalls = 4)
    }
    "should throw a Bad Request when journey is in stage SubmittedArrangement" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterSubmittedArrangement.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      val result: Throwable = journeyConnector.updateAffordabilityResult(tdAll.journeyId, tdAll.EpayeBta.updateInstalmentAmountsRequest()).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"Cannot update AffordabilityResult when journey is in completed state"}""")

      verifyCommonActions(numberOfAuthCalls = 1)
    }
  }
}
