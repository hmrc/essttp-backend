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
import essttp.rootmodel.bank.{AccountName, BankDetails}
import paymentsEmailVerification.models.EmailVerificationResult
import testsupport.ItSpec
import testsupport.testdata.TdAll
import uk.gov.hmrc.crypto.Sensitive.SensitiveString

class UpdateDirectDebitDetailsControllerSpec extends ItSpec with UpdateJourneyControllerSpec {

  "POST /journey/:journeyId/update-direct-debit-details" - {
    "should throw Bad Request when Journey is in a stage [BeforeChosenTypeOfBankAccount]" in new JourneyItTest {
      stubCommonActions()

      journeyConnector.Epaye.startJourneyBta(TdAll.EpayeBta.sjRequest).futureValue

      val result: Throwable = journeyConnector.updateDirectDebitDetails(tdAll.journeyId, TdAll.EpayeBta.updateDirectDebitDetailsRequest).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"UpdateDirectDebitDetails is not possible in that state: [Started]"}""")

      verifyCommonActions(numberOfAuthCalls = 2)
    }

    "should update the journey when an existing value didn't exist before for" - {

      "Epaye" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.EpayeBta.journeyAfterEnteredDetailsAboutBankAccountNoAffordability(isAccountHolder = true),
          TdAll.EpayeBta.updateDirectDebitDetailsRequest
        )(
            journeyConnector.updateDirectDebitDetails,
            tdAll.EpayeBta.journeyAfterEnteredDirectDebitDetailsNoAffordability()
          )(this)
      }

      "Vat" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.VatBta.journeyAfterEnteredDetailsAboutBankAccountNoAffordability(isAccountHolder = true),
          TdAll.VatBta.updateDirectDebitDetailsRequest
        )(
            journeyConnector.updateDirectDebitDetails,
            tdAll.VatBta.journeyAfterEnteredDirectDebitDetailsNoAffordability()
          )(this)
      }

      "Sa" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.SaBta.journeyAfterEnteredDetailsAboutBankAccountNoAffordability(isAccountHolder = true),
          TdAll.SaBta.updateDirectDebitDetailsRequest
        )(
            journeyConnector.updateDirectDebitDetails,
            tdAll.SaBta.journeyAfterEnteredDirectDebitDetailsNoAffordability()
          )(this)
      }

      "Sia" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.SiaPta.journeyAfterEnteredDetailsAboutBankAccountNoAffordability(isAccountHolder = true),
          TdAll.SiaPta.updateDirectDebitDetailsRequest
        )(
            journeyConnector.updateDirectDebitDetails,
            tdAll.SiaPta.journeyAfterEnteredDirectDebitDetailsNoAffordability()
          )(this)
      }
    }

    "should update the journey when a value already existed" - {

      val differentBankDetails = TdAll.directDebitDetails.copy(name = AccountName(SensitiveString("Afdgusi Ydfgysu")))

      "Epaye when the current stage is" - {

          def testEpayeBta[J <: Journey](initialJourney: J)(existingValue: J => BankDetails)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentBankDetails,
                journeyConnector.updateDirectDebitDetails(_, _)(context.request),
                context.tdAll.EpayeBta.journeyAfterEnteredDirectDebitDetailsNoAffordability().copy(directDebitDetails = differentBankDetails)
              )(context)

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(_.directDebitDetails)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(_.directDebitDetails)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(_.directDebitDetails)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterSelectedEmailNoAffordability)(_.directDebitDetails)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(_.directDebitDetails)(this)
        }

      }

      "Vat when the current stage is" - {

          def testVatBta[J <: Journey](initialJourney: J)(existingValue: J => BankDetails)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentBankDetails,
                journeyConnector.updateDirectDebitDetails(_, _)(context.request),
                context.tdAll.VatBta.journeyAfterEnteredDirectDebitDetailsNoAffordability().copy(directDebitDetails = differentBankDetails)
              )(context)

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(_.directDebitDetails)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(_.directDebitDetails)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(_.directDebitDetails)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterSelectedEmailNoAffordability)(_.directDebitDetails)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(_.directDebitDetails)(this)
        }

      }

      "Sa when the current stage is" - {

          def testSaBta[J <: Journey](initialJourney: J)(existingValue: J => BankDetails)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentBankDetails,
                journeyConnector.updateDirectDebitDetails(_, _)(context.request),
                context.tdAll.SaBta.journeyAfterEnteredDirectDebitDetailsNoAffordability().copy(directDebitDetails = differentBankDetails)
              )(context)

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(_.directDebitDetails)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(_.directDebitDetails)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(_.directDebitDetails)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterSelectedEmailNoAffordability)(_.directDebitDetails)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(_.directDebitDetails)(this)
        }

      }

      "Sia when the current stage is" - {

          def testSiaPta[J <: Journey](initialJourney: J)(existingValue: J => BankDetails)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentBankDetails,
                journeyConnector.updateDirectDebitDetails(_, _)(context.request),
                context.tdAll.SiaPta.journeyAfterEnteredDirectDebitDetailsNoAffordability().copy(directDebitDetails = differentBankDetails)
              )(context)

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(_.directDebitDetails)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(_.directDebitDetails)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(_.directDebitDetails)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterSelectedEmail)(_.directDebitDetails)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified))(_.directDebitDetails)(this)
        }

      }

    }

    "should throw a Bad Request when journey is in stage SubmittedArrangement" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterSubmittedArrangementNoAffordability().copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      val result: Throwable = journeyConnector.updateDirectDebitDetails(tdAll.journeyId, tdAll.EpayeBta.updateDirectDebitDetailsRequest).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"Cannot update DirectDebitDetails when journey is in completed state"}""")

      verifyCommonActions(numberOfAuthCalls = 1)
    }
  }
}
