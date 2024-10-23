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
import essttp.rootmodel.bank.DetailsAboutBankAccount
import paymentsEmailVerification.models.EmailVerificationResult
import testsupport.ItSpec
import testsupport.testdata.TdAll

class UpdateCheckYouCanSetupDDControllerSpec extends ItSpec with UpdateJourneyControllerSpec {

  "POST /journey/:journeyId/update-details-about-bank-account" - {
    "should throw Bad Request when Journey is in a stage [BeforeCheckedPaymentPlan]" in new JourneyItTest {
      stubCommonActions()

      journeyConnector.Epaye.startJourneyBta(TdAll.EpayeBta.sjRequest).futureValue

      val requestBody = TdAll.EpayeBta.updateDetailsAboutBankAccountRequest(isAccountHolder = true)
      val result: Throwable = journeyConnector.updateDetailsAboutBankAccount(tdAll.journeyId, requestBody).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"UpdateDetailsAboutBankAccount is not possible in that state: [Started]"}""")

      verifyCommonActions(numberOfAuthCalls = 2)
    }

    "should update the journey when an existing value didn't exist before for" - {

      "Epaye" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.EpayeBta.journeyAfterCheckedPaymentPlanNonAffordability,
          TdAll.EpayeBta.updateDetailsAboutBankAccountRequest(isAccountHolder = true)
        )(
            journeyConnector.updateDetailsAboutBankAccount,
            tdAll.EpayeBta.journeyAfterEnteredDetailsAboutBankAccountNoAffordability(isAccountHolder = true)
          )(this)
      }

      "Vat" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.VatBta.journeyAfterCheckedPaymentPlanNonAffordability,
          TdAll.VatBta.updateDetailsAboutBankAccountRequest(isAccountHolder = true)
        )(
            journeyConnector.updateDetailsAboutBankAccount,
            tdAll.VatBta.journeyAfterEnteredDetailsAboutBankAccountNoAffordability(isAccountHolder = true)
          )(this)
      }

      "Sa" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.SaBta.journeyAfterCheckedPaymentPlanNonAffordability,
          TdAll.SaBta.updateDetailsAboutBankAccountRequest(isAccountHolder = true)
        )(
            journeyConnector.updateDetailsAboutBankAccount,
            tdAll.SaBta.journeyAfterEnteredDetailsAboutBankAccountNoAffordability(isAccountHolder = true)
          )(this)
      }

      "Sia" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.SiaPta.journeyAfterCheckedPaymentPlanNonAffordability,
          TdAll.SiaPta.updateDetailsAboutBankAccountRequest(isAccountHolder = true)
        )(
            journeyConnector.updateDetailsAboutBankAccount,
            tdAll.SiaPta.journeyAfterEnteredDetailsAboutBankAccountNoAffordability(isAccountHolder = true)
          )(this)
      }
    }

    "should update the journey when a value already existed" - {

      val differentDetailsAboutBankAccount = DetailsAboutBankAccount(isAccountHolder = false)

      "Epaye when the current stage is" - {

          def testEpayeBta[J <: Journey](initialJourney: J)(existingValue: J => DetailsAboutBankAccount)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentDetailsAboutBankAccount,
                journeyConnector.updateDetailsAboutBankAccount(_, _)(context.request),
                context.tdAll.EpayeBta.journeyAfterEnteredDetailsAboutBankAccountNoAffordability(isAccountHolder = false).copy(detailsAboutBankAccount = differentDetailsAboutBankAccount)
              )(context)

        "EnteredDetailsAboutBankAccount" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEnteredDetailsAboutBankAccountNoAffordability(isAccountHolder = true))(_.detailsAboutBankAccount)(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(_.detailsAboutBankAccount)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(_.detailsAboutBankAccount)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(_.detailsAboutBankAccount)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterSelectedEmailNoAffordability)(_.detailsAboutBankAccount)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(_.detailsAboutBankAccount)(this)
        }

      }

      "Vat when the current stage is" - {

          def testVatBta[J <: Journey](initialJourney: J)(existingValue: J => DetailsAboutBankAccount)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentDetailsAboutBankAccount,
                journeyConnector.updateDetailsAboutBankAccount(_, _)(context.request),
                context.tdAll.VatBta.journeyAfterEnteredDetailsAboutBankAccountNoAffordability(isAccountHolder = false).copy(detailsAboutBankAccount = differentDetailsAboutBankAccount)
              )(context)

        "EnteredDetailsAboutBankAccount" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEnteredDetailsAboutBankAccountNoAffordability(isAccountHolder = true))(_.detailsAboutBankAccount)(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(_.detailsAboutBankAccount)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(_.detailsAboutBankAccount)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(_.detailsAboutBankAccount)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterSelectedEmailNoAffordability)(_.detailsAboutBankAccount)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(_.detailsAboutBankAccount)(this)
        }

      }

      "Sa when the current stage is" - {

          def testSaBta[J <: Journey](initialJourney: J)(existingValue: J => DetailsAboutBankAccount)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentDetailsAboutBankAccount,
                journeyConnector.updateDetailsAboutBankAccount(_, _)(context.request),
                context.tdAll.SaBta.journeyAfterEnteredDetailsAboutBankAccountNoAffordability(isAccountHolder = false).copy(detailsAboutBankAccount = differentDetailsAboutBankAccount)
              )(context)

        "EnteredDetailsAboutBankAccount" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEnteredDetailsAboutBankAccountNoAffordability(isAccountHolder = true))(_.detailsAboutBankAccount)(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(_.detailsAboutBankAccount)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(_.detailsAboutBankAccount)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(_.detailsAboutBankAccount)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterSelectedEmailNoAffordability)(_.detailsAboutBankAccount)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(_.detailsAboutBankAccount)(this)
        }

      }

      "Sia when the current stage is" - {

          def testSiaPta[J <: Journey](initialJourney: J)(existingValue: J => DetailsAboutBankAccount)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentDetailsAboutBankAccount,
                journeyConnector.updateDetailsAboutBankAccount(_, _)(context.request),
                context.tdAll.SiaPta.journeyAfterEnteredDetailsAboutBankAccountNoAffordability(isAccountHolder = false).copy(detailsAboutBankAccount = differentDetailsAboutBankAccount)
              )(context)

        "EnteredDetailsAboutBankAccount" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterEnteredDetailsAboutBankAccountNoAffordability(isAccountHolder = true))(_.detailsAboutBankAccount)(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(_.detailsAboutBankAccount)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(_.detailsAboutBankAccount)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(_.detailsAboutBankAccount)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterSelectedEmail)(_.detailsAboutBankAccount)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified))(_.detailsAboutBankAccount)(this)
        }

      }

    }

    "should throw a Bad Request when journey is in stage SubmittedArrangement" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterSubmittedArrangementNoAffordability().copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))

      val requestBody = TdAll.EpayeBta.updateDetailsAboutBankAccountRequest(isAccountHolder = true)
      val result: Throwable = journeyConnector.updateDetailsAboutBankAccount(tdAll.journeyId, requestBody).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"Cannot update DetailsAboutBankAccount when journey is in completed state"}""")

      verifyCommonActions(numberOfAuthCalls = 1)
    }
  }
}
