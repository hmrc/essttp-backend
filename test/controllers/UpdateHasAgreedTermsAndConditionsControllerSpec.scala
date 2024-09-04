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
import essttp.journey.model.Journey
import paymentsEmailVerification.models.EmailVerificationResult
import testsupport.ItSpec
import testsupport.testdata.TdAll

class UpdateHasAgreedTermsAndConditionsControllerSpec extends ItSpec with UpdateJourneyControllerSpec {

  "POST /journey/:journeyId/update-has-agreed-terms-and-conditions" - {

    "should throw Bad Request when Journey is in a stage [BeforeConfirmedDirectDebitDetails]" in new JourneyItTest {
      stubCommonActions()

      journeyConnector.Epaye.startJourneyBta(TdAll.EpayeBta.sjRequest).futureValue
      val result: Throwable = journeyConnector.updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(value = true)).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"UpdateAgreedTermsAndConditions is not possible in that state: [Started]"}""")

      verifyCommonActions(numberOfAuthCalls = 2)
    }

    "should update the journey when an existing value didn't exist before for" - {

      "Epaye" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.EpayeBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability,
          IsEmailAddressRequired(value = true)
        )(
            journeyConnector.updateHasAgreedTermsAndConditions,
            tdAll.EpayeBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true)
          )(this)
      }

      "Vat" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.VatBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability,
          IsEmailAddressRequired(value = true)
        )(
            journeyConnector.updateHasAgreedTermsAndConditions,
            tdAll.VatBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true)
          )(this)
      }

      "Sa" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.SaBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability,
          IsEmailAddressRequired(value = true)
        )(
            journeyConnector.updateHasAgreedTermsAndConditions,
            tdAll.SaBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true)
          )(this)
      }

      "Sia" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.SiaPta.journeyAfterConfirmedDirectDebitDetailsNoAffordability,
          IsEmailAddressRequired(value = true)
        )(
            journeyConnector.updateHasAgreedTermsAndConditions,
            tdAll.SiaPta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true)
          )(this)
      }
    }

    "should update the journey when a value already existed" - {

      "Epaye when the current stage is" - {

          def testEpayeBta[J <: Journey](initialJourney: J)(existingValue: J => IsEmailAddressRequired)(context: JourneyItTest): Unit = {
            val differentValue = IsEmailAddressRequired(!existingValue(initialJourney).value)

            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentValue,
                journeyConnector.updateHasAgreedTermsAndConditions(_, _)(context.request),
                context.tdAll.EpayeBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = differentValue.value)
              )(context)
          }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = false))(_.isEmailAddressRequired)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterSelectedEmailNoAffordability)(_.isEmailAddressRequired)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(_.isEmailAddressRequired)(this)
        }

      }

    }

    "should throw a Bad Request when journey is in stage SubmittedArrangement" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterSubmittedArrangementNoAffordability().copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      val result: Throwable = journeyConnector.updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(value = true)).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"Cannot update AgreedTermsAndConditions when journey is in completed state"}""")

      verifyCommonActions(numberOfAuthCalls = 1)
    }
  }
}
