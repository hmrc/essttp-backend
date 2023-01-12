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

import essttp.rootmodel.IsEmailAddressRequired
import essttp.journey.JourneyConnector
import testsupport.ItSpec
import testsupport.testdata.TdAll

class UpdateHasAgreedTermsAndConditionsControllerSpec extends ItSpec {

  def journeyConnector: JourneyConnector = app.injector.instanceOf[JourneyConnector]

  "POST /journey/:journeyId/update-has-agreed-terms-and-conditions" - {

    "should throw Bad Request when Journey is in a stage [BeforeConfirmedDirectDebitDetails]" in new JourneyItTest {
      stubCommonActions()

      journeyConnector.Epaye.startJourneyBta(TdAll.EpayeBta.sjRequest).futureValue
      val result: Throwable = journeyConnector.updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(true)).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"UpdateAgreedTermsAndConditions is not possible in that state: [Started]"}""")

      verifyCommonActions(numberOfAuthCalls = 2)
    }

    "should return Unit when terms and conditions have already been confirmed and the value for isEmailAddressRequired has not changed" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterConfirmedDirectDebitDetails.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))

      val expectedUpdatedJourney = tdAll.EpayeBta.journeyAfterAgreedTermsAndConditions(isEmailAddressRequired = false)

      val result1 = journeyConnector.updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(false)).futureValue
      result1 shouldBe expectedUpdatedJourney
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe expectedUpdatedJourney

      val result2 = journeyConnector.updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(false)).futureValue
      result2 shouldBe expectedUpdatedJourney
      // check value hasn't actually changed
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe expectedUpdatedJourney

      verifyCommonActions(numberOfAuthCalls = 4)
    }

    "should return Unit when terms and conditions have already been confirmed and the value for isEmailAddressRequired has changed" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterConfirmedDirectDebitDetails.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))

      val result1 = journeyConnector.updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(false)).futureValue
      val expectedUpdatedJourney1 = tdAll.EpayeBta.journeyAfterAgreedTermsAndConditions(isEmailAddressRequired = false)
      result1 shouldBe expectedUpdatedJourney1
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe expectedUpdatedJourney1

      val result2 = journeyConnector.updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(true)).futureValue
      val expectedUpdatedJourney2 = tdAll.EpayeBta.journeyAfterAgreedTermsAndConditions(isEmailAddressRequired = true)
      result2 shouldBe expectedUpdatedJourney2
      // check value has changed
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe expectedUpdatedJourney2

      verifyCommonActions(numberOfAuthCalls = 4)
    }

    "should throw a Bad Request when journey is in stage SubmittedArrangement" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterSubmittedArrangement().copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      val result: Throwable = journeyConnector.updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(true)).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"Cannot update AgreedTermsAndConditions when journey is in completed state"}""")

      verifyCommonActions(numberOfAuthCalls = 1)
    }
  }
}
