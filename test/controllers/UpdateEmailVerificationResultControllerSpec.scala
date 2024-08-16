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

import essttp.journey.model.Journey
import essttp.rootmodel.IsEmailAddressRequired
import paymentsEmailVerification.models.EmailVerificationResult
import testsupport.ItSpec
import testsupport.testdata.TdAll

class UpdateEmailVerificationResultControllerSpec extends ItSpec with UpdateJourneyControllerSpec {

  "POST /journey/:journeyId/update-email-verification-status" - {

    "should throw Bad Request when Journey is in a stage [BeforeEmailAddressSelectedToBeVerified]" in new JourneyItTest {
      stubCommonActions()

      journeyConnector.Epaye.startJourneyBta(TdAll.EpayeBta.sjRequest).futureValue
      val result: Throwable = journeyConnector.updateEmailVerificationResult(tdAll.journeyId, EmailVerificationResult.Verified).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"UpdateEmailVerificationResult is not possible in that state: [Started]"}""")

      verifyCommonActions(numberOfAuthCalls = 2)
    }

    "should update the journey when an existing value didn't exist before for" - {

      "Epaye" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.EpayeBta.journeyAfterSelectedEmailNoAffordability,
          EmailVerificationResult.Verified
        )(
            journeyConnector.updateEmailVerificationResult,
            tdAll.EpayeBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified)
          )(this)
      }

      "Vat" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.VatBta.journeyAfterSelectedEmailNoAffordability,
          EmailVerificationResult.Verified
        )(
            journeyConnector.updateEmailVerificationResult,
            tdAll.VatBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified)
          )(this)
      }

      "Sa" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.SaBta.journeyAfterSelectedEmailNoAffordability,
          EmailVerificationResult.Verified
        )(
            journeyConnector.updateEmailVerificationResult,
            tdAll.SaBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified)
          )(this)
      }
    }

    "should update the journey when a value already existed" - {

      "Epaye when the current stage is" - {

          def testEpayeBta[J <: Journey](initialJourney: J)(existingValue: J => EmailVerificationResult)(context: JourneyItTest): Unit = {
            val differentVerificationResult: EmailVerificationResult = existingValue(initialJourney) match {
              case EmailVerificationResult.Verified => EmailVerificationResult.Locked
              case EmailVerificationResult.Locked   => EmailVerificationResult.Verified
            }

            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentVerificationResult,
                journeyConnector.updateEmailVerificationResult(_, _)(context.request),
                context.tdAll.EpayeBta.journeyAfterEmailVerificationResultNoAffordability(differentVerificationResult)
              )(context)
          }

        "EmailVerificationComplete" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(_.emailVerificationResult)(this)
        }

      }

      "Vat when the current stage is" - {

          def testVatBta[J <: Journey](initialJourney: J)(existingValue: J => EmailVerificationResult)(context: JourneyItTest): Unit = {
            val differentVerificationResult: EmailVerificationResult = existingValue(initialJourney) match {
              case EmailVerificationResult.Verified => EmailVerificationResult.Locked
              case EmailVerificationResult.Locked   => EmailVerificationResult.Verified
            }

            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentVerificationResult,
                journeyConnector.updateEmailVerificationResult(_, _)(context.request),
                context.tdAll.VatBta.journeyAfterEmailVerificationResultNoAffordability(differentVerificationResult)
              )(context)
          }

        "EmailVerificationComplete" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(_.emailVerificationResult)(this)
        }

      }

      "Sa when the current stage is" - {

          def testSaBta[J <: Journey](initialJourney: J)(existingValue: J => EmailVerificationResult)(context: JourneyItTest): Unit = {
            val differentVerificationResult: EmailVerificationResult = existingValue(initialJourney) match {
              case EmailVerificationResult.Verified => EmailVerificationResult.Locked
              case EmailVerificationResult.Locked   => EmailVerificationResult.Verified
            }

            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentVerificationResult,
                journeyConnector.updateEmailVerificationResult(_, _)(context.request),
                context.tdAll.SaBta.journeyAfterEmailVerificationResultNoAffordability(differentVerificationResult)
              )(context)
          }

        "EmailVerificationComplete" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(_.emailVerificationResult)(this)
        }

      }

    }

    "should throw a Bad Request when journey is in stage SubmittedArrangement" in new JourneyItTest {
      stubCommonActions()
      insertJourneyForTest(
        TdAll.EpayeBta.journeyAfterSubmittedArrangementNoAffordability()
          .copy(_id = tdAll.journeyId)
          .copy(correlationId = tdAll.correlationId)
          .copy(isEmailAddressRequired = IsEmailAddressRequired(value = true))
      )
      val result: Throwable = journeyConnector.updateEmailVerificationResult(tdAll.journeyId, EmailVerificationResult.Locked).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"Cannot update EmailVerificationResult when journey is in completed state."}""")
      verifyCommonActions(numberOfAuthCalls = 1)
    }

  }

}
