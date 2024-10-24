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
import essttp.rootmodel.DayOfMonth
import paymentsEmailVerification.models.EmailVerificationResult
import testsupport.ItSpec
import testsupport.testdata.TdAll

class UpdateDayOfMonthControllerSpec extends ItSpec with UpdateJourneyControllerSpec {

  "POST /journey/:journeyId/update-day-of-month" - {
    "should throw Bad Request when Journey is in a stage" - {
      "[BeforeRetrievedAffordabilityResult]" in new JourneyItTest {
        stubCommonActions()

        journeyConnector.Epaye.startJourneyBta(TdAll.EpayeBta.sjRequest).futureValue
        val result: Throwable = journeyConnector.updateDayOfMonth(tdAll.journeyId, TdAll.EpayeBta.updateDayOfMonthRequest()).failed.futureValue
        result.getMessage should include("""{"statusCode":400,"message":"UpdateDayOfMonth update is not possible in that state: [Started]"}""")

        verifyCommonActions(numberOfAuthCalls = 2)
      }

      "[AfterStartedPegaCase]" in new JourneyItTest {
        insertJourneyForTest(tdAll.EpayeBta.journeyAfterStartedPegaCase)
        stubCommonActions()

        val result: Throwable = journeyConnector.updateDayOfMonth(tdAll.journeyId, tdAll.EpayeBta.updateDayOfMonthRequest()).failed.futureValue
        result.getMessage should include("""{"statusCode":400,"message":"Not expecting to update DayOfMonth after starting PEGA case"}""")

        verifyCommonActions(numberOfAuthCalls = 1)
      }

      "[AfterCheckedPaymentPlan] when the user has gone through the affordability journey" in new JourneyItTest {
        insertJourneyForTest(tdAll.EpayeBta.journeyAfterCheckedPaymentPlanWithAffordability)
        stubCommonActions()

        val result: Throwable = journeyConnector.updateDayOfMonth(tdAll.journeyId, TdAll.EpayeBta.updateDayOfMonthRequest()).failed.futureValue
        result.getMessage should include("""{"statusCode":400,"message":"Cannot update DayOfMonth on affordability journey"}""")

        verifyCommonActions(numberOfAuthCalls = 1)
      }
    }

    "should update the journey when an existing isAccountHolder didn't exist before for" - {

      "Epaye" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.EpayeBta.journeyAfterMonthlyPaymentAmount,
          TdAll.EpayeBta.updateDayOfMonthRequest()
        )(
            journeyConnector.updateDayOfMonth,
            tdAll.EpayeBta.journeyAfterDayOfMonth
          )(this)
      }

      "Vat" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.VatBta.journeyAfterMonthlyPaymentAmount,
          TdAll.VatBta.updateDayOfMonthRequest()
        )(
            journeyConnector.updateDayOfMonth,
            tdAll.VatBta.journeyAfterDayOfMonth
          )(this)
      }

      "Sa" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.SaBta.journeyAfterMonthlyPaymentAmount,
          TdAll.SaBta.updateDayOfMonthRequest()
        )(
            journeyConnector.updateDayOfMonth,
            tdAll.SaBta.journeyAfterDayOfMonth
          )(this)
      }

      "Sia" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.SiaPta.journeyAfterMonthlyPaymentAmount,
          TdAll.SiaPta.updateDayOfMonthRequest()
        )(
            journeyConnector.updateDayOfMonth,
            tdAll.SiaPta.journeyAfterDayOfMonth
          )(this)
      }
    }

    "should update the journey when a isAccountHolder already existed" - {

      val differentDayOfMonth = TdAll.dayOfMonth.copy(value = 2)

      "Epaye when the current stage is" - {

          def testEpayeBta[J <: Journey](initialJourney: J)(existingValue: J => DayOfMonth)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentDayOfMonth,
                journeyConnector.updateDayOfMonth(_, _)(context.request),
                context.tdAll.EpayeBta.journeyAfterDayOfMonth.copy(dayOfMonth = differentDayOfMonth)
              )(context)

        "RetrievedStartDates" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterStartDatesResponse)(_.dayOfMonth)(this)
        }

        "RetrievedAffordableQuotes" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterAffordableQuotesResponse)(_.dayOfMonth)(this)
        }

        "ChosenPaymentPlan" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterSelectedPaymentPlan)(_.dayOfMonth)(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterCheckedPaymentPlanNonAffordability)(_.paymentPlanAnswers.nonAffordabilityAnswers.dayOfMonth)(this)
        }

        "EnteredCanYouSetUpDirectDebit" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true))(_.paymentPlanAnswers.nonAffordabilityAnswers.dayOfMonth)(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(_.paymentPlanAnswers.nonAffordabilityAnswers.dayOfMonth)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(_.paymentPlanAnswers.nonAffordabilityAnswers.dayOfMonth)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(_.paymentPlanAnswers.nonAffordabilityAnswers.dayOfMonth)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterSelectedEmailNoAffordability)(_.paymentPlanAnswers.nonAffordabilityAnswers.dayOfMonth)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(_.paymentPlanAnswers.nonAffordabilityAnswers.dayOfMonth)(this)
        }

      }

      "Vat when the current stage is" - {

          def testVatBta[J <: Journey](initialJourney: J)(existingValue: J => DayOfMonth)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentDayOfMonth,
                journeyConnector.updateDayOfMonth(_, _)(context.request),
                context.tdAll.VatBta.journeyAfterDayOfMonth.copy(dayOfMonth = differentDayOfMonth)
              )(context)

        "RetrievedStartDates" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterStartDatesResponse)(_.dayOfMonth)(this)
        }

        "RetrievedAffordableQuotes" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterAffordableQuotesResponse)(_.dayOfMonth)(this)
        }

        "ChosenPaymentPlan" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterSelectedPaymentPlan)(_.dayOfMonth)(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterCheckedPaymentPlanNonAffordability)(_.paymentPlanAnswers.nonAffordabilityAnswers.dayOfMonth)(this)
        }

        "EnteredCanYouSetUpDirectDebit" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true))(_.paymentPlanAnswers.nonAffordabilityAnswers.dayOfMonth)(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(_.paymentPlanAnswers.nonAffordabilityAnswers.dayOfMonth)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(_.paymentPlanAnswers.nonAffordabilityAnswers.dayOfMonth)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(_.paymentPlanAnswers.nonAffordabilityAnswers.dayOfMonth)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterSelectedEmailNoAffordability)(_.paymentPlanAnswers.nonAffordabilityAnswers.dayOfMonth)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(_.paymentPlanAnswers.nonAffordabilityAnswers.dayOfMonth)(this)
        }

      }

      "Sa when the current stage is" - {

          def testSaBta[J <: Journey](initialJourney: J)(existingValue: J => DayOfMonth)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentDayOfMonth,
                journeyConnector.updateDayOfMonth(_, _)(context.request),
                context.tdAll.SaBta.journeyAfterDayOfMonth.copy(dayOfMonth = differentDayOfMonth)
              )(context)

        "RetrievedStartDates" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterStartDatesResponse)(_.dayOfMonth)(this)
        }

        "RetrievedAffordableQuotes" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterAffordableQuotesResponse)(_.dayOfMonth)(this)
        }

        "ChosenPaymentPlan" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterSelectedPaymentPlan)(_.dayOfMonth)(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterCheckedPaymentPlanNonAffordability)(_.paymentPlanAnswers.nonAffordabilityAnswers.dayOfMonth)(this)
        }

        "EnteredCanYouSetUpDirectDebit" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true))(_.paymentPlanAnswers.nonAffordabilityAnswers.dayOfMonth)(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(_.paymentPlanAnswers.nonAffordabilityAnswers.dayOfMonth)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(_.paymentPlanAnswers.nonAffordabilityAnswers.dayOfMonth)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(_.paymentPlanAnswers.nonAffordabilityAnswers.dayOfMonth)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterSelectedEmailNoAffordability)(_.paymentPlanAnswers.nonAffordabilityAnswers.dayOfMonth)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(_.paymentPlanAnswers.nonAffordabilityAnswers.dayOfMonth)(this)
        }

      }

      "Sia when the current stage is" - {

          def testSiaPta[J <: Journey](initialJourney: J)(existingValue: J => DayOfMonth)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentDayOfMonth,
                journeyConnector.updateDayOfMonth(_, _)(context.request),
                context.tdAll.SiaPta.journeyAfterDayOfMonth.copy(dayOfMonth = differentDayOfMonth)
              )(context)

        "RetrievedStartDates" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterStartDatesResponse)(_.dayOfMonth)(this)
        }

        "RetrievedAffordableQuotes" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterAffordableQuotesResponse)(_.dayOfMonth)(this)
        }

        "ChosenPaymentPlan" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterSelectedPaymentPlan)(_.dayOfMonth)(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterCheckedPaymentPlanNonAffordability)(_.paymentPlanAnswers.nonAffordabilityAnswers.dayOfMonth)(this)
        }

        "EnteredCanYouSetUpDirectDebit" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true))(_.paymentPlanAnswers.nonAffordabilityAnswers.dayOfMonth)(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(_.paymentPlanAnswers.nonAffordabilityAnswers.dayOfMonth)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(_.paymentPlanAnswers.nonAffordabilityAnswers.dayOfMonth)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(_.paymentPlanAnswers.nonAffordabilityAnswers.dayOfMonth)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterSelectedEmail)(_.paymentPlanAnswers.nonAffordabilityAnswers.dayOfMonth)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified))(_.paymentPlanAnswers.nonAffordabilityAnswers.dayOfMonth)(this)
        }

      }

    }

    "should throw a Bad Request when journey is in stage SubmittedArrangement" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterSubmittedArrangementNoAffordability().copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      val result: Throwable = journeyConnector.updateDayOfMonth(tdAll.journeyId, tdAll.EpayeBta.updateDayOfMonthRequest()).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"Cannot update DayOfMonth when journey is in completed state"}""")

      verifyCommonActions(numberOfAuthCalls = 1)
    }
  }
}
