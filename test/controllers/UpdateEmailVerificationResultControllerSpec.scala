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

import essttp.emailverification.EmailVerificationResult
import essttp.journey.JourneyConnector
import essttp.rootmodel.IsEmailAddressRequired
import essttp.testdata.TdAll
import testsupport.ItSpec

class UpdateEmailVerificationResultControllerSpec extends ItSpec {

  def journeyConnector: JourneyConnector = app.injector.instanceOf[JourneyConnector]

  "POST /journey/:journeyId/update-email-verification-status" - {

    "should throw Bad Request when Journey is in a stage [BeforeEmailAddressSelectedToBeVerified]" in new JourneyItTest {
      stubCommonActions()

      journeyConnector.Epaye.startJourneyBta(TdAll.EpayeBta.sjRequest).futureValue
      val result: Throwable = journeyConnector.updateEmailVerificationResult(tdAll.journeyId, EmailVerificationResult.Verified).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"UpdateEmailVerificationResult is not possible in that state: [Started]"}""")

      verifyCommonActions(numberOfAuthCalls = 2)
    }

    "should update the journey with the email verification status" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(
        TdAll.EpayeBta.journeyAfterSelectedEmail
          .copy(_id = tdAll.journeyId)
          .copy(correlationId = tdAll.correlationId)
      )

      val result1 = journeyConnector.updateEmailVerificationResult(tdAll.journeyId, EmailVerificationResult.Verified).futureValue
      val expectedUpdatedJourney1 = tdAll.EpayeBta.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified)
      result1 shouldBe expectedUpdatedJourney1
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe expectedUpdatedJourney1

      val result2 = journeyConnector.updateEmailVerificationResult(tdAll.journeyId, EmailVerificationResult.Locked).futureValue
      val expectedUpdatedJourney2 = tdAll.EpayeBta.journeyAfterEmailVerificationResult(EmailVerificationResult.Locked)
      result2 shouldBe expectedUpdatedJourney2
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe expectedUpdatedJourney2

      verifyCommonActions(numberOfAuthCalls = 4)
    }

    "should throw a Bad Request when journey is in stage SubmittedArrangement" in new JourneyItTest {
      stubCommonActions()
      insertJourneyForTest(
        TdAll.EpayeBta.journeyAfterSubmittedArrangement()
          .copy(_id = tdAll.journeyId)
          .copy(correlationId = tdAll.correlationId)
          .copy(isEmailAddressRequired = IsEmailAddressRequired(true))
      )
      val result: Throwable = journeyConnector.updateEmailVerificationResult(tdAll.journeyId, EmailVerificationResult.Locked).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"Cannot update EmailVerificationResult when journey is in completed state."}""")
      verifyCommonActions(numberOfAuthCalls = 1)
    }

  }

}
