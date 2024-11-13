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
import essttp.rootmodel.AmountInPence
import essttp.rootmodel.pega.PegaCaseId
import essttp.rootmodel.ttp.affordability.InstalmentAmounts
import paymentsEmailVerification.models.EmailVerificationResult
import testsupport.ItSpec
import testsupport.testdata.TdAll

class UpdateAffordabilityResultControllerSpec extends ItSpec with UpdateJourneyControllerSpec {

  "POST /journey/:journeyId/update-affordability-result" - {
    "should throw Bad Request when Journey is in a stage [BeforeExtremeDatesResponse]" in new JourneyItTest {
      stubCommonActions()

      journeyConnector.Epaye.startJourneyBta(TdAll.EpayeBta.sjRequest).futureValue
      val result: Throwable = journeyConnector.updateAffordabilityResult(tdAll.journeyId, TdAll.EpayeBta.updateInstalmentAmountsRequest()).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"UpdateAffordabilityResult update is not possible in that state: [Started]"}""")

      verifyCommonActions(numberOfAuthCalls = 2)
    }

    "should update the journey when an existing value didn't exist before for" - {

      "Epaye" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.EpayeBta.journeyAfterExtremeDates,
          TdAll.EpayeBta.updateInstalmentAmountsRequest()
        )(
            journeyConnector.updateAffordabilityResult,
            tdAll.EpayeBta.journeyAfterInstalmentAmounts
          )(this)
      }

      "Vat" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.VatBta.journeyAfterExtremeDates,
          TdAll.VatBta.updateInstalmentAmountsRequest()
        )(
            journeyConnector.updateAffordabilityResult,
            tdAll.VatBta.journeyAfterInstalmentAmounts
          )(this)
      }

      "Sa" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.SaBta.journeyAfterExtremeDates,
          TdAll.SaBta.updateInstalmentAmountsRequest()
        )(
            journeyConnector.updateAffordabilityResult,
            tdAll.SaBta.journeyAfterInstalmentAmounts
          )(this)
      }

      "Sia" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.SiaPta.journeyAfterExtremeDates,
          TdAll.SiaPta.updateInstalmentAmountsRequest()
        )(
            journeyConnector.updateAffordabilityResult,
            tdAll.SiaPta.journeyAfterInstalmentAmounts
          )(this)
      }
    }

    "should update the journey when a value already existed" - {

      val differentInstalmentAmount = TdAll.instalmentAmounts.copy(minimumInstalmentAmount = AmountInPence(67897))

      "Epaye when the current stage is" - {

          def testEpayeBta[J <: Journey](initialJourney: J)(existingValue: J => InstalmentAmounts)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentInstalmentAmount,
                journeyConnector.updateAffordabilityResult(_, _)(context.request),
                context.tdAll.EpayeBta.journeyAfterInstalmentAmounts.copy(instalmentAmounts = differentInstalmentAmount)
              )(context)

          def testEpayeBtaWithCaseId[J <: Journey](initialJourney: J)(existingValue: J => InstalmentAmounts)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentInstalmentAmount,
                journeyConnector.updateAffordabilityResult(_, _)(context.request),
                context.tdAll.EpayeBta.journeyAfterInstalmentAmounts.copy(instalmentAmounts = differentInstalmentAmount, pegaCaseId = Some(PegaCaseId("case-id")))
              )(context)

        "RetrievedAffordabilityResult" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterInstalmentAmounts)(_.instalmentAmounts)(this)
        }

        "ObtainedCanPayWithinSixMonthsAnswers" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterCanPayWithinSixMonthsNotRequired)(_.instalmentAmounts)(this)
        }

        "StartedPegaCase" in new JourneyItTest {
          testEpayeBtaWithCaseId(tdAll.EpayeBta.journeyAfterStartedPegaCase)(_.instalmentAmounts)(this)
        }

        "EnteredMonthlyPaymentAmount" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterMonthlyPaymentAmount)(_.instalmentAmounts)(this)
        }

        "EnteredDayOfMonth" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterDayOfMonth)(_.instalmentAmounts)(this)
        }

        "RetrievedStartDates" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterStartDatesResponse)(_.instalmentAmounts)(this)
        }

        "RetrievedAffordableQuotes" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterAffordableQuotesResponse)(_.instalmentAmounts)(this)
        }

        "ChosenPaymentPlan" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterSelectedPaymentPlan)(_.instalmentAmounts)(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterCheckedPaymentPlanNonAffordability)(_.instalmentAmounts)(this)
        }

        "EnteredCanYouSetUpDirectDebit" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true))(_.instalmentAmounts)(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(_.instalmentAmounts)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(_.instalmentAmounts)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(_.instalmentAmounts)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterSelectedEmailNoAffordability)(_.instalmentAmounts)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(_.instalmentAmounts)(this)
        }

      }

      "Vat when the current stage is" - {

          def testVatBta[J <: Journey](initialJourney: J)(existingValue: J => InstalmentAmounts)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentInstalmentAmount,
                journeyConnector.updateAffordabilityResult(_, _)(context.request),
                context.tdAll.VatBta.journeyAfterInstalmentAmounts.copy(instalmentAmounts = differentInstalmentAmount)
              )(context)

          def testVatBtaWithCaseId[J <: Journey](initialJourney: J)(existingValue: J => InstalmentAmounts)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentInstalmentAmount,
                journeyConnector.updateAffordabilityResult(_, _)(context.request),
                context.tdAll.VatBta.journeyAfterInstalmentAmounts.copy(instalmentAmounts = differentInstalmentAmount, pegaCaseId = Some(PegaCaseId("case-id")))
              )(context)

        "RetrievedAffordabilityResult" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterInstalmentAmounts)(_.instalmentAmounts)(this)
        }

        "ObtainedCanPayWithinSixMonthsAnswers" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterCanPayWithinSixMonthsNotRequired)(_.instalmentAmounts)(this)
        }

        "StartedPegaCase" in new JourneyItTest {
          testVatBtaWithCaseId(tdAll.VatBta.journeyAfterStartedPegaCase)(_.instalmentAmounts)(this)
        }

        "EnteredMonthlyPaymentAmount" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterMonthlyPaymentAmount)(_.instalmentAmounts)(this)
        }

        "EnteredDayOfMonth" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterDayOfMonth)(_.instalmentAmounts)(this)
        }

        "RetrievedStartDates" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterStartDatesResponse)(_.instalmentAmounts)(this)
        }

        "RetrievedAffordableQuotes" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterAffordableQuotesResponse)(_.instalmentAmounts)(this)
        }

        "ChosenPaymentPlan" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterSelectedPaymentPlan)(_.instalmentAmounts)(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterCheckedPaymentPlanNonAffordability)(_.instalmentAmounts)(this)
        }

        "EnteredCanYouSetUpDirectDebit" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true))(_.instalmentAmounts)(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(_.instalmentAmounts)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(_.instalmentAmounts)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(_.instalmentAmounts)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterSelectedEmailNoAffordability)(_.instalmentAmounts)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(_.instalmentAmounts)(this)
        }

      }

      "Sa when the current stage is" - {

          def testSaBta[J <: Journey](initialJourney: J)(existingValue: J => InstalmentAmounts)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentInstalmentAmount,
                journeyConnector.updateAffordabilityResult(_, _)(context.request),
                context.tdAll.SaBta.journeyAfterInstalmentAmounts.copy(instalmentAmounts = differentInstalmentAmount)
              )(context)

          def testSaBtaWithCaseId[J <: Journey](initialJourney: J)(existingValue: J => InstalmentAmounts)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentInstalmentAmount,
                journeyConnector.updateAffordabilityResult(_, _)(context.request),
                context.tdAll.SaBta.journeyAfterInstalmentAmounts.copy(instalmentAmounts = differentInstalmentAmount, pegaCaseId = Some(PegaCaseId("case-id")))
              )(context)

        "RetrievedAffordabilityResult" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterInstalmentAmounts)(_.instalmentAmounts)(this)
        }

        "ObtainedCanPayWithinSixMonthsAnswers" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterCanPayWithinSixMonths)(_.instalmentAmounts)(this)
        }

        "StartedPegaCase" in new JourneyItTest {
          testSaBtaWithCaseId(tdAll.SaBta.journeyAfterStartedPegaCase)(_.instalmentAmounts)(this)
        }

        "EnteredMonthlyPaymentAmount" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterMonthlyPaymentAmount)(_.instalmentAmounts)(this)
        }

        "EnteredDayOfMonth" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterDayOfMonth)(_.instalmentAmounts)(this)
        }

        "RetrievedStartDates" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterStartDatesResponse)(_.instalmentAmounts)(this)
        }

        "RetrievedAffordableQuotes" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterAffordableQuotesResponse)(_.instalmentAmounts)(this)
        }

        "ChosenPaymentPlan" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterSelectedPaymentPlan)(_.instalmentAmounts)(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterCheckedPaymentPlanNonAffordability)(_.instalmentAmounts)(this)
        }

        "EnteredCanYouSetUpDirectDebit" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true))(_.instalmentAmounts)(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(_.instalmentAmounts)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(_.instalmentAmounts)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(_.instalmentAmounts)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterSelectedEmailNoAffordability)(_.instalmentAmounts)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(_.instalmentAmounts)(this)
        }

      }

      "Sia when the current stage is" - {

          def testSiaPta[J <: Journey](initialJourney: J)(existingValue: J => InstalmentAmounts)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentInstalmentAmount,
                journeyConnector.updateAffordabilityResult(_, _)(context.request),
                context.tdAll.SiaPta.journeyAfterInstalmentAmounts.copy(instalmentAmounts = differentInstalmentAmount)
              )(context)

          def testSiaPtaWithCaseId[J <: Journey](initialJourney: J)(existingValue: J => InstalmentAmounts)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentInstalmentAmount,
                journeyConnector.updateAffordabilityResult(_, _)(context.request),
                context.tdAll.SiaPta.journeyAfterInstalmentAmounts.copy(instalmentAmounts = differentInstalmentAmount, pegaCaseId = Some(PegaCaseId("case-id")))
              )(context)

        "RetrievedAffordabilityResult" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterInstalmentAmounts)(_.instalmentAmounts)(this)
        }

        "ObtainedCanPayWithinSixMonthsAnswers" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterCanPayWithinSixMonths)(_.instalmentAmounts)(this)
        }

        "StartedPegaCase" in new JourneyItTest {
          testSiaPtaWithCaseId(tdAll.SiaPta.journeyAfterStartedPegaCase)(_.instalmentAmounts)(this)
        }

        "EnteredMonthlyPaymentAmount" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterMonthlyPaymentAmount)(_.instalmentAmounts)(this)
        }

        "EnteredDayOfMonth" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterDayOfMonth)(_.instalmentAmounts)(this)
        }

        "RetrievedStartDates" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterStartDatesResponse)(_.instalmentAmounts)(this)
        }

        "RetrievedAffordableQuotes" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterAffordableQuotesResponse)(_.instalmentAmounts)(this)
        }

        "ChosenPaymentPlan" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterSelectedPaymentPlan)(_.instalmentAmounts)(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterCheckedPaymentPlanNonAffordability)(_.instalmentAmounts)(this)
        }

        "EnteredCanYouSetUpDirectDebit" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true))(_.instalmentAmounts)(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(_.instalmentAmounts)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(_.instalmentAmounts)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(_.instalmentAmounts)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterSelectedEmail)(_.instalmentAmounts)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified))(_.instalmentAmounts)(this)
        }

      }

    }

    "should throw a Bad Request when journey is in stage SubmittedArrangement" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(tdAll.EpayeBta.journeyAfterSubmittedArrangementNoAffordability())
      val result: Throwable = journeyConnector.updateAffordabilityResult(tdAll.journeyId, tdAll.EpayeBta.updateInstalmentAmountsRequest()).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"Cannot update AffordabilityResult when journey is in completed state"}""")

      verifyCommonActions(numberOfAuthCalls = 1)
    }
  }
}
