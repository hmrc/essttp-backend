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
import essttp.rootmodel.bank.{DetailsAboutBankAccount, TypesOfBankAccount}
import paymentsEmailVerification.models.EmailVerificationResult
import testsupport.ItSpec
import testsupport.testdata.TdAll

class UpdateDetailsAboutBankAccountControllerSpec extends ItSpec with UpdateJourneyControllerSpec {

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
          tdAll.EpayeBta.journeyAfterCheckedPaymentPlan,
          TdAll.EpayeBta.updateDetailsAboutBankAccountRequest(isAccountHolder = true)
        )(
            journeyConnector.updateDetailsAboutBankAccount,
            tdAll.EpayeBta.journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder = true)
          )(this)
      }

      "Vat" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.VatBta.journeyAfterCheckedPaymentPlan,
          TdAll.VatBta.updateDetailsAboutBankAccountRequest(isAccountHolder = true)
        )(
            journeyConnector.updateDetailsAboutBankAccount,
            tdAll.VatBta.journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder = true)
          )(this)
      }

      "Sa" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.SaBta.journeyAfterCheckedPaymentPlan,
          TdAll.SaBta.updateDetailsAboutBankAccountRequest(isAccountHolder = true)
        )(
            journeyConnector.updateDetailsAboutBankAccount,
            tdAll.SaBta.journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder = true)
          )(this)
      }
    }

    "should update the journey when a value already existed" - {

      val differentDetailsAboutBankAccount = DetailsAboutBankAccount(TypesOfBankAccount.Business, isAccountHolder = false)

      "Epaye when the current stage is" - {

          def testEpayeBta[J <: Journey](initialJourney: J)(existingValue: J => DetailsAboutBankAccount)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentDetailsAboutBankAccount,
                journeyConnector.updateDetailsAboutBankAccount(_, _)(context.request),
                context.tdAll.EpayeBta.journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder = false).copy(detailsAboutBankAccount = differentDetailsAboutBankAccount)
              )(context)

        "EnteredDetailsAboutBankAccount" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder = true))(_.detailsAboutBankAccount)(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEnteredDirectDebitDetails())(_.detailsAboutBankAccount)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterConfirmedDirectDebitDetails)(_.detailsAboutBankAccount)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterAgreedTermsAndConditions(isEmailAddressRequired = true))(_.detailsAboutBankAccount)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterSelectedEmail)(_.detailsAboutBankAccount)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified))(_.detailsAboutBankAccount)(this)
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
                context.tdAll.VatBta.journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder = false).copy(detailsAboutBankAccount = differentDetailsAboutBankAccount)
              )(context)

        "EnteredDetailsAboutBankAccount" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder = true))(_.detailsAboutBankAccount)(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEnteredDirectDebitDetails())(_.detailsAboutBankAccount)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterConfirmedDirectDebitDetails)(_.detailsAboutBankAccount)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterAgreedTermsAndConditions(isEmailAddressRequired = true))(_.detailsAboutBankAccount)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterSelectedEmail)(_.detailsAboutBankAccount)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified))(_.detailsAboutBankAccount)(this)
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
                context.tdAll.SaBta.journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder = false).copy(detailsAboutBankAccount = differentDetailsAboutBankAccount)
              )(context)

        "EnteredDetailsAboutBankAccount" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder = true))(_.detailsAboutBankAccount)(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEnteredDirectDebitDetails())(_.detailsAboutBankAccount)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterConfirmedDirectDebitDetails)(_.detailsAboutBankAccount)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterAgreedTermsAndConditions(isEmailAddressRequired = true))(_.detailsAboutBankAccount)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterSelectedEmail)(_.detailsAboutBankAccount)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified))(_.detailsAboutBankAccount)(this)
        }

      }

    }

    "should throw a Bad Request when journey is in stage SubmittedArrangement" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterSubmittedArrangement().copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))

      val requestBody = TdAll.EpayeBta.updateDetailsAboutBankAccountRequest(isAccountHolder = true)
      val result: Throwable = journeyConnector.updateDetailsAboutBankAccount(tdAll.journeyId, requestBody).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"Cannot update DetailsAboutBankAccount when journey is in completed state"}""")

      verifyCommonActions(numberOfAuthCalls = 1)
    }
  }
}
