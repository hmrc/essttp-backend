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
import essttp.rootmodel.{AmountInPence, MonthlyPaymentAmount}
import paymentsEmailVerification.models.EmailVerificationResult
import testsupport.ItSpec
import testsupport.testdata.TdAll

class UpdateMonthlyPaymentAmountControllerSpec extends ItSpec with UpdateJourneyControllerSpec {

  "POST /journey/:journeyId/update-monthly-payment-amount" - {
    "should throw Bad Request when Journey is in a stage" - {
      "[BeforeObtainedCanPayWithinSixMonths]" in new JourneyItTest {
        stubCommonActions()

        journeyConnector.Epaye.startJourneyBta(TdAll.EpayeBta.sjRequest).futureValue
        val result: Throwable = journeyConnector.updateMonthlyPaymentAmount(tdAll.journeyId, TdAll.EpayeBta.updateMonthlyPaymentAmountRequest()).failed.futureValue
        result.getMessage should include("""{"statusCode":400,"message":"UpdateMonthlyPaymentAmount update is not possible in that state: [Started]"}""")

        verifyCommonActions(numberOfAuthCalls = 2)
      }

      "[StartedPegaJourney]" in new JourneyItTest {
        insertJourneyForTest(tdAll.EpayeBta.journeyAfterStartedPegaCase)
        stubCommonActions()

        val result: Throwable = journeyConnector.updateMonthlyPaymentAmount(tdAll.journeyId, TdAll.EpayeBta.updateMonthlyPaymentAmountRequest()).failed.futureValue
        result.getMessage should include("""{"statusCode":400,"message":"Not expecting monthly payment amount to be updated after PEGA case started"}""")

        verifyCommonActions(numberOfAuthCalls = 1)
      }
    }

    "should update the journey when an existing value didn't exist before for" - {

      "Epaye" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.EpayeBta.journeyAfterCanPayWithinSixMonthsNotRequired,
          TdAll.EpayeBta.updateMonthlyPaymentAmountRequest()
        )(
            journeyConnector.updateMonthlyPaymentAmount,
            tdAll.EpayeBta.journeyAfterMonthlyPaymentAmount
          )(this)
      }

      "Vat" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.VatBta.journeyAfterCanPayWithinSixMonthsNotRequired,
          TdAll.VatBta.updateMonthlyPaymentAmountRequest()
        )(
            journeyConnector.updateMonthlyPaymentAmount,
            tdAll.VatBta.journeyAfterMonthlyPaymentAmount
          )(this)
      }

      "Sa" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.SaBta.journeyAfterCanPayWithinSixMonths,
          TdAll.SaBta.updateMonthlyPaymentAmountRequest()
        )(
            journeyConnector.updateMonthlyPaymentAmount,
            tdAll.SaBta.journeyAfterMonthlyPaymentAmount
          )(this)
      }
    }

    "should update the journey when a value already existed" - {

      "Epaye when the current stage is" - {

        val differentAmount = MonthlyPaymentAmount(AmountInPence(4583972))

          def testEpayeBta[J <: Journey](initialJourney: J)(existingValue: J => MonthlyPaymentAmount)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentAmount,
                journeyConnector.updateMonthlyPaymentAmount(_, _)(context.request),
                context.tdAll.EpayeBta.journeyAfterMonthlyPaymentAmount.copy(monthlyPaymentAmount = differentAmount)
              )(context)

        "EnteredMonthlyPaymentAmount" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterMonthlyPaymentAmount)(_.monthlyPaymentAmount)(this)
        }

        "EnteredDayOfMonth" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterDayOfMonth)(_.monthlyPaymentAmount)(this)
        }

        "RetrievedStartDates" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterStartDatesResponse)(_.monthlyPaymentAmount)(this)
        }

        "RetrievedAffordableQuotes" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterAffordableQuotesResponse)(_.monthlyPaymentAmount)(this)
        }

        "ChosenPaymentPlan" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterSelectedPaymentPlan)(_.monthlyPaymentAmount)(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterCheckedPaymentPlan)(_.monthlyPaymentAmount)(this)
        }

        "EnteredDetailsAboutBankAccount" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder = true))(_.monthlyPaymentAmount)(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEnteredDirectDebitDetails())(_.monthlyPaymentAmount)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterConfirmedDirectDebitDetails)(_.monthlyPaymentAmount)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterAgreedTermsAndConditions(isEmailAddressRequired = true))(_.monthlyPaymentAmount)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterSelectedEmail)(_.monthlyPaymentAmount)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified))(_.monthlyPaymentAmount)(this)
        }

      }

      "Vat when the current stage is" - {

        val differentAmount = MonthlyPaymentAmount(AmountInPence(4583972))

          def testVatBta[J <: Journey](initialJourney: J)(existingValue: J => MonthlyPaymentAmount)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentAmount,
                journeyConnector.updateMonthlyPaymentAmount(_, _)(context.request),
                context.tdAll.VatBta.journeyAfterMonthlyPaymentAmount.copy(monthlyPaymentAmount = differentAmount)
              )(context)

        "EnteredMonthlyPaymentAmount" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterMonthlyPaymentAmount)(_.monthlyPaymentAmount)(this)
        }

        "EnteredDayOfMonth" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterDayOfMonth)(_.monthlyPaymentAmount)(this)
        }

        "RetrievedStartDates" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterStartDatesResponse)(_.monthlyPaymentAmount)(this)
        }

        "RetrievedAffordableQuotes" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterAffordableQuotesResponse)(_.monthlyPaymentAmount)(this)
        }

        "ChosenPaymentPlan" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterSelectedPaymentPlan)(_.monthlyPaymentAmount)(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterCheckedPaymentPlan)(_.monthlyPaymentAmount)(this)
        }

        "EnteredDetailsAboutBankAccount" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder = true))(_.monthlyPaymentAmount)(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEnteredDirectDebitDetails())(_.monthlyPaymentAmount)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterConfirmedDirectDebitDetails)(_.monthlyPaymentAmount)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterAgreedTermsAndConditions(isEmailAddressRequired = true))(_.monthlyPaymentAmount)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterSelectedEmail)(_.monthlyPaymentAmount)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified))(_.monthlyPaymentAmount)(this)
        }

      }

      "Sa when the current stage is" - {

        val differentAmount = MonthlyPaymentAmount(AmountInPence(4583972))

          def testSaBta[J <: Journey](initialJourney: J)(existingValue: J => MonthlyPaymentAmount)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentAmount,
                journeyConnector.updateMonthlyPaymentAmount(_, _)(context.request),
                context.tdAll.SaBta.journeyAfterMonthlyPaymentAmount.copy(monthlyPaymentAmount = differentAmount)
              )(context)

        "EnteredMonthlyPaymentAmount" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterMonthlyPaymentAmount)(_.monthlyPaymentAmount)(this)
        }

        "EnteredDayOfMonth" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterDayOfMonth)(_.monthlyPaymentAmount)(this)
        }

        "RetrievedStartDates" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterStartDatesResponse)(_.monthlyPaymentAmount)(this)
        }

        "RetrievedAffordableQuotes" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterAffordableQuotesResponse)(_.monthlyPaymentAmount)(this)
        }

        "ChosenPaymentPlan" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterSelectedPaymentPlan)(_.monthlyPaymentAmount)(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterCheckedPaymentPlan)(_.monthlyPaymentAmount)(this)
        }

        "EnteredDetailsAboutBankAccount" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder = true))(_.monthlyPaymentAmount)(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEnteredDirectDebitDetails())(_.monthlyPaymentAmount)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterConfirmedDirectDebitDetails)(_.monthlyPaymentAmount)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterAgreedTermsAndConditions(isEmailAddressRequired = true))(_.monthlyPaymentAmount)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterSelectedEmail)(_.monthlyPaymentAmount)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified))(_.monthlyPaymentAmount)(this)
        }

      }

    }

    "should throw a Bad Request when journey is in stage SubmittedArrangement" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterSubmittedArrangement().copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      val result: Throwable = journeyConnector.updateMonthlyPaymentAmount(tdAll.journeyId, tdAll.EpayeBta.updateMonthlyPaymentAmountRequest()).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"Cannot update MonthlyAmount when journey is in completed state"}""")

      verifyCommonActions(numberOfAuthCalls = 1)
    }
  }
}
