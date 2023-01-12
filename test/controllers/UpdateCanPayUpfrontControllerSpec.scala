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

class UpdateCanPayUpfrontControllerSpec extends ItSpec {

  def journeyConnector: JourneyConnector = app.injector.instanceOf[JourneyConnector]

  "POST /journey/:journeyId/update-can-pay-upfront" - {
    "should throw Bad Request when Journey is in a stage [BeforeEligibilityChecked]" in new JourneyItTest {
      stubCommonActions()

      journeyConnector.Epaye.startJourneyBta(TdAll.EpayeBta.sjRequest).futureValue
      val result: Throwable = journeyConnector.updateCanPayUpfront(tdAll.journeyId, tdAll.EpayeBta.updateCanPayUpfrontYesRequest()).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"UpdateCanPayUpfront is not possible in that state."}""")

      verifyCommonActions(numberOfAuthCalls = 2)
    }
    "should not update the journey when can pay upfront choice hasn't changed" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterEligibilityCheckEligible.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      val result1 = journeyConnector.updateCanPayUpfront(tdAll.journeyId, tdAll.EpayeBta.updateCanPayUpfrontYesRequest()).futureValue
      result1 shouldBe tdAll.EpayeBta.journeyAfterCanPayUpfrontYes
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterCanPayUpfrontYes

      val result2 = journeyConnector.updateCanPayUpfront(tdAll.journeyId, tdAll.EpayeBta.updateCanPayUpfrontYesRequest()).futureValue
      result2 shouldBe tdAll.EpayeBta.journeyAfterCanPayUpfrontYes
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterCanPayUpfrontYes

      verifyCommonActions(numberOfAuthCalls = 4)
    }
    "should update the journey when Can Pay Upfront result has changed" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterEligibilityCheckEligible.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      val result1 = journeyConnector.updateCanPayUpfront(tdAll.journeyId, tdAll.EpayeBta.updateCanPayUpfrontYesRequest()).futureValue
      result1 shouldBe tdAll.EpayeBta.journeyAfterCanPayUpfrontYes
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterCanPayUpfrontYes

      val result2 = journeyConnector.updateCanPayUpfront(tdAll.journeyId, tdAll.EpayeBta.updateCanPayUpfrontNoRequest()).futureValue
      result2 shouldBe tdAll.EpayeBta.journeyAfterCanPayUpfrontNo
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterCanPayUpfrontNo

      val result3 = journeyConnector.updateCanPayUpfront(tdAll.journeyId, tdAll.EpayeBta.updateCanPayUpfrontYesRequest()).futureValue
      result3 shouldBe tdAll.EpayeBta.journeyAfterCanPayUpfrontYes
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterCanPayUpfrontYes

      verifyCommonActions(numberOfAuthCalls = 6)
    }
    "should throw a Bad Request when journey is in stage SubmittedArrangement" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterSubmittedArrangement().copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      val result: Throwable = journeyConnector.updateCanPayUpfront(tdAll.journeyId, tdAll.EpayeBta.updateCanPayUpfrontYesRequest()).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"Cannot update AnsweredCanPayUpFront when journey is in completed state"}""")

      verifyCommonActions(numberOfAuthCalls = 1)
    }
  }

}
