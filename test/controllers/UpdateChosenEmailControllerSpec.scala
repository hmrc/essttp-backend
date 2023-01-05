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

import essttp.emailverification.EmailVerificationResult
import essttp.journey.JourneyConnector
import essttp.rootmodel.{Email, IsEmailAddressRequired}
import essttp.testdata.TdAll
import testsupport.ItSpec
import uk.gov.hmrc.crypto.Sensitive.SensitiveString

class UpdateChosenEmailControllerSpec extends ItSpec {

  def journeyConnector: JourneyConnector = app.injector.instanceOf[JourneyConnector]

  "POST /journey/:journeyId/update-chosen-email" - {
    "should throw Bad Request when Journey is in a stage [BeforeAgreedTermsAndConditions]" in new JourneyItTest {
      stubCommonActions()

      journeyConnector.Epaye.startJourneyBta(TdAll.EpayeBta.sjRequest).futureValue
      val result: Throwable = journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.EpayeBta.updateSelectedEmailRequest()).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"UpdateChosenEmail is not possible in that state: [Started]"}""")

      verifyCommonActions(numberOfAuthCalls = 2)
    }

    "should update the journey with users selected email address" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(
        TdAll.EpayeBta.journeyAfterAgreedTermsAndConditions(true)
          .copy(_id = tdAll.journeyId)
          .copy(correlationId = tdAll.correlationId)
      )

      val result1 = journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.EpayeBta.updateSelectedEmailRequest()).futureValue
      result1 shouldBe tdAll.EpayeBta.journeyAfterSelectedEmail
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterSelectedEmail

      val result2 = journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.EpayeBta.updateSelectedEmailRequest().copy(SensitiveString("billyJoel@pianoman.com"))).futureValue
      val expectedUpdatedJourney2 = tdAll.EpayeBta.journeyAfterSelectedEmail.copy(emailToBeVerified = Email(SensitiveString("billyJoel@pianoman.com")))
      result2 shouldBe expectedUpdatedJourney2
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe expectedUpdatedJourney2

      verifyCommonActions(numberOfAuthCalls = 4)
    }

    "change the journey state even if the email in the journey is the same" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(
        TdAll.EpayeBta.journeyAfterEmailVerificationResult(EmailVerificationResult.Locked)
          .copy(_id = tdAll.journeyId)
          .copy(correlationId = tdAll.correlationId)
      )

      val result = journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.EpayeBta.updateSelectedEmailRequest()).futureValue
      result shouldBe tdAll.EpayeBta.journeyAfterSelectedEmail
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterSelectedEmail

      verifyCommonActions(numberOfAuthCalls = 2)
    }

    "should throw a Bad Request when isEmailAddressRequired in journey is false" in new JourneyItTest {
      stubCommonActions()
      insertJourneyForTest(TdAll.EpayeBta.journeyAfterAgreedTermsAndConditions(false)
        .copy(_id = tdAll.journeyId)
        .copy(correlationId = tdAll.correlationId))
      val result: Throwable = journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.EpayeBta.updateSelectedEmailRequest()).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"Cannot update selected email address when isEmailAddressRequired is false for journey."}""")
      verifyCommonActions(numberOfAuthCalls = 1)
    }

    "should throw a Bad Request when journey is in stage SubmittedArrangement" in new JourneyItTest {
      stubCommonActions()
      insertJourneyForTest(
        TdAll.EpayeBta.journeyAfterSubmittedArrangement()
          .copy(_id = tdAll.journeyId)
          .copy(correlationId = tdAll.correlationId)
          .copy(isEmailAddressRequired = IsEmailAddressRequired(true))
      )
      val result: Throwable = journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.EpayeBta.updateSelectedEmailRequest()).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"Cannot update ChosenEmail when journey is in completed state."}""")
      verifyCommonActions(numberOfAuthCalls = 1)
    }
  }

}
