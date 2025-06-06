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
import essttp.rootmodel.pega.PegaCaseId
import essttp.rootmodel.ttp.eligibility.{EligibilityCheckResult, ProcessingDateTime}
import paymentsEmailVerification.models.EmailVerificationResult
import testsupport.ItSpec
import testsupport.testdata.TdAll

import java.time.Instant

class UpdateEligibilityCheckResultControllerSpec extends ItSpec, UpdateJourneyControllerSpec {

  "POST /journey/:journeyId/update-eligibility-result" - {
    "should throw Bad Request when Journey is in a stage [BeforeComputedTaxId]" in new JourneyItTest {
      stubCommonActions()

      journeyConnector.Epaye.startJourneyBta(TdAll.EpayeBta.sjRequest).futureValue
      val result: Throwable = journeyConnector
        .updateEligibilityCheckResult(tdAll.journeyId, tdAll.EpayeBta.updateEligibilityCheckRequest())
        .failed
        .futureValue
      result.getMessage should include(
        """{"statusCode":400,"message":"EligibilityCheckResult update is not possible in that state."}"""
      )

      verifyCommonActions(numberOfAuthCalls = 2)
    }

    "should update the journey when an existing value didn't exist before for" - {

      "Epaye" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.EpayeBta.journeyAfterDetermineTaxIds,
          TdAll.EpayeBta.updateEligibilityCheckRequest()
        )(
          journeyConnector.updateEligibilityCheckResult,
          tdAll.EpayeBta.journeyAfterEligibilityCheckEligible
        )(this)
      }

      "Vat" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.VatBta.journeyAfterDetermineTaxIds,
          TdAll.VatBta.updateEligibilityCheckRequest()
        )(
          journeyConnector.updateEligibilityCheckResult,
          tdAll.VatBta.journeyAfterEligibilityCheckEligible
        )(this)
      }

      "Sa" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.SaBta.journeyAfterDetermineTaxIds,
          TdAll.SaBta.updateEligibilityCheckRequest()
        )(
          journeyConnector.updateEligibilityCheckResult,
          tdAll.SaBta.journeyAfterEligibilityCheckEligible
        )(this)
      }

      "Simp" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.SimpPta.journeyAfterDetermineTaxIds,
          TdAll.SimpPta.updateEligibilityCheckRequest()
        )(
          journeyConnector.updateEligibilityCheckResult,
          tdAll.SimpPta.journeyAfterEligibilityCheckEligible
        )(this)
      }
    }

    "should update the journey when a value already existed" - {

      "Epaye when the current stage is" - {

        val differentEligibilityCheckResult =
          TdAll.eligibleEligibilityCheckResultEpaye.copy(processingDateTime =
            ProcessingDateTime(Instant.now().toString)
          )

        def testEpayeBta[J <: Journey](
          initialJourney: J
        )(existingValue: J => EligibilityCheckResult)(context: JourneyItTest): Unit =
          testUpdateWithExistingValue(initialJourney)(
            _.journeyId,
            existingValue(initialJourney)
          )(
            differentEligibilityCheckResult,
            journeyConnector.updateEligibilityCheckResult(_, _)(using context.request),
            context.tdAll.EpayeBta.journeyAfterEligibilityCheckEligible
              .copy(eligibilityCheckResult = differentEligibilityCheckResult)
          )(context)

        def testEpayeBtaWithCaseId[J <: Journey](
          initialJourney: J
        )(existingValue: J => EligibilityCheckResult)(context: JourneyItTest): Unit =
          testUpdateWithExistingValue(initialJourney)(
            _.journeyId,
            existingValue(initialJourney)
          )(
            differentEligibilityCheckResult,
            journeyConnector.updateEligibilityCheckResult(_, _)(using context.request),
            context.tdAll.EpayeBta.journeyAfterEligibilityCheckEligible.copy(
              eligibilityCheckResult = differentEligibilityCheckResult,
              pegaCaseId = Some(PegaCaseId("case-id"))
            )
          )(context)

        "EligibilityChecked" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEligibilityCheckEligible)(_.eligibilityCheckResult)(this)
        }

        "ObtainedWhyCannotPayInFullAnswers" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterWhyCannotPayInFullNotRequired)(_.eligibilityCheckResult)(this)
        }

        "AnsweredCanPayUpfront" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterCanPayUpfrontNo)(_.eligibilityCheckResult)(this)
        }

        "EnteredUpfrontPaymentAmount" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterUpfrontPaymentAmount)(_.eligibilityCheckResult)(this)
        }

        "RetrievedExtremeDates" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterExtremeDates)(_.eligibilityCheckResult)(this)
        }

        "RetrievedAffordabilityResult" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterInstalmentAmounts)(_.eligibilityCheckResult)(this)
        }

        "ObtainedCanPayWithinSixMonthsAnswers" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterCanPayWithinSixMonthsNotRequired)(_.eligibilityCheckResult)(this)
        }

        "StartedPegaCase" in new JourneyItTest {
          testEpayeBtaWithCaseId(tdAll.EpayeBta.journeyAfterStartedPegaCase)(_.eligibilityCheckResult)(this)
        }

        "EnteredMonthlyPaymentAmount" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterMonthlyPaymentAmount)(_.eligibilityCheckResult)(this)
        }

        "EnteredDayOfMonth" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterDayOfMonth)(_.eligibilityCheckResult)(this)
        }

        "RetrievedStartDates" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterStartDatesResponse)(_.eligibilityCheckResult)(this)
        }

        "RetrievedAffordableQuotes" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterAffordableQuotesResponse)(_.eligibilityCheckResult)(this)
        }

        "ChosenPaymentPlan" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterSelectedPaymentPlan)(_.eligibilityCheckResult)(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterCheckedPaymentPlanNonAffordability)(_.eligibilityCheckResult)(this)
        }

        "EnteredCanYouSetUpDirectDebit" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true))(
            _.eligibilityCheckResult
          )(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(_.eligibilityCheckResult)(
            this
          )
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(_.eligibilityCheckResult)(
            this
          )
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testEpayeBta(
            tdAll.EpayeBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true)
          )(_.eligibilityCheckResult)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterSelectedEmailNoAffordability)(_.eligibilityCheckResult)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testEpayeBta(
            tdAll.EpayeBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified)
          )(_.eligibilityCheckResult)(this)
        }

      }

      "Vat when the current stage is" - {

        val differentEligibilityCheckResult =
          TdAll
            .eligibleEligibilityCheckResultVat()
            .copy(processingDateTime = ProcessingDateTime(Instant.now().toString))

        def testVatBta[J <: Journey](
          initialJourney: J
        )(existingValue: J => EligibilityCheckResult)(context: JourneyItTest): Unit =
          testUpdateWithExistingValue(initialJourney)(
            _.journeyId,
            existingValue(initialJourney)
          )(
            differentEligibilityCheckResult,
            journeyConnector.updateEligibilityCheckResult(_, _)(using context.request),
            context.tdAll.VatBta.journeyAfterEligibilityCheckEligible
              .copy(eligibilityCheckResult = differentEligibilityCheckResult)
          )(context)

        def testVatBtaWithCaseId[J <: Journey](
          initialJourney: J
        )(existingValue: J => EligibilityCheckResult)(context: JourneyItTest): Unit =
          testUpdateWithExistingValue(initialJourney)(
            _.journeyId,
            existingValue(initialJourney)
          )(
            differentEligibilityCheckResult,
            journeyConnector.updateEligibilityCheckResult(_, _)(using context.request),
            context.tdAll.VatBta.journeyAfterEligibilityCheckEligible.copy(
              eligibilityCheckResult = differentEligibilityCheckResult,
              pegaCaseId = Some(PegaCaseId("case-id"))
            )
          )(context)

        "EligibilityChecked" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEligibilityCheckEligible)(_.eligibilityCheckResult)(this)
        }

        "ObtainedWhyCannotPayInFullAnswers" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterWhyCannotPayInFullNotRequired)(_.eligibilityCheckResult)(this)
        }

        "AnsweredCanPayUpfront" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterCanPayUpfrontNo)(_.eligibilityCheckResult)(this)
        }

        "EnteredUpfrontPaymentAmount" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterUpfrontPaymentAmount)(_.eligibilityCheckResult)(this)
        }

        "RetrievedExtremeDates" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterExtremeDates)(_.eligibilityCheckResult)(this)
        }

        "RetrievedAffordabilityResult" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterInstalmentAmounts)(_.eligibilityCheckResult)(this)
        }

        "ObtainedCanPayWithinSixMonthsAnswers" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterCanPayWithinSixMonthsNotRequired)(_.eligibilityCheckResult)(this)
        }

        "StartedPegaCase" in new JourneyItTest {
          testVatBtaWithCaseId(tdAll.VatBta.journeyAfterStartedPegaCase)(_.eligibilityCheckResult)(this)
        }

        "EnteredMonthlyPaymentAmount" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterMonthlyPaymentAmount)(_.eligibilityCheckResult)(this)
        }

        "EnteredDayOfMonth" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterDayOfMonth)(_.eligibilityCheckResult)(this)
        }

        "RetrievedStartDates" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterStartDatesResponse)(_.eligibilityCheckResult)(this)
        }

        "RetrievedAffordableQuotes" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterAffordableQuotesResponse)(_.eligibilityCheckResult)(this)
        }

        "ChosenPaymentPlan" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterSelectedPaymentPlan)(_.eligibilityCheckResult)(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterCheckedPaymentPlanNonAffordability)(_.eligibilityCheckResult)(this)
        }

        "EnteredCanYouSetUpDirectDebit" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true))(
            _.eligibilityCheckResult
          )(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(_.eligibilityCheckResult)(
            this
          )
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(_.eligibilityCheckResult)(
            this
          )
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(
            _.eligibilityCheckResult
          )(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterSelectedEmailNoAffordability)(_.eligibilityCheckResult)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(
            _.eligibilityCheckResult
          )(this)
        }

      }

      "Sa when the current stage is" - {

        val differentEligibilityCheckResult =
          TdAll.eligibleEligibilityCheckResultSa.copy(processingDateTime = ProcessingDateTime(Instant.now().toString))

        def testSaBta[J <: Journey](
          initialJourney: J
        )(existingValue: J => EligibilityCheckResult)(context: JourneyItTest): Unit =
          testUpdateWithExistingValue(initialJourney)(
            _.journeyId,
            existingValue(initialJourney)
          )(
            differentEligibilityCheckResult,
            journeyConnector.updateEligibilityCheckResult(_, _)(using context.request),
            context.tdAll.SaBta.journeyAfterEligibilityCheckEligible
              .copy(eligibilityCheckResult = differentEligibilityCheckResult)
          )(context)

        def testSaBtaWithcaseId[J <: Journey](
          initialJourney: J
        )(existingValue: J => EligibilityCheckResult)(context: JourneyItTest): Unit =
          testUpdateWithExistingValue(initialJourney)(
            _.journeyId,
            existingValue(initialJourney)
          )(
            differentEligibilityCheckResult,
            journeyConnector.updateEligibilityCheckResult(_, _)(using context.request),
            context.tdAll.SaBta.journeyAfterEligibilityCheckEligible.copy(
              eligibilityCheckResult = differentEligibilityCheckResult,
              pegaCaseId = Some(PegaCaseId("case-id"))
            )
          )(context)

        "EligibilityChecked" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEligibilityCheckEligible)(_.eligibilityCheckResult)(this)
        }

        "ObtainedWhyCannotPayInFullAnswers" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterWhyCannotPayInFullNotRequired)(_.eligibilityCheckResult)(this)
        }

        "AnsweredCanPayUpfront" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterCanPayUpfrontNo)(_.eligibilityCheckResult)(this)
        }

        "EnteredUpfrontPaymentAmount" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterUpfrontPaymentAmount)(_.eligibilityCheckResult)(this)
        }

        "RetrievedExtremeDates" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterExtremeDates)(_.eligibilityCheckResult)(this)
        }

        "RetrievedAffordabilityResult" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterInstalmentAmounts)(_.eligibilityCheckResult)(this)
        }

        "ObtainedCanPayWithinSixMonthsAnswers" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterCanPayWithinSixMonths)(_.eligibilityCheckResult)(this)
        }

        "StartedPegaCase" in new JourneyItTest {
          testSaBtaWithcaseId(tdAll.SaBta.journeyAfterStartedPegaCase)(_.eligibilityCheckResult)(this)
        }

        "EnteredMonthlyPaymentAmount" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterMonthlyPaymentAmount)(_.eligibilityCheckResult)(this)
        }

        "EnteredDayOfMonth" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterDayOfMonth)(_.eligibilityCheckResult)(this)
        }

        "RetrievedStartDates" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterStartDatesResponse)(_.eligibilityCheckResult)(this)
        }

        "RetrievedAffordableQuotes" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterAffordableQuotesResponse)(_.eligibilityCheckResult)(this)
        }

        "ChosenPaymentPlan" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterSelectedPaymentPlan)(_.eligibilityCheckResult)(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterCheckedPaymentPlanNonAffordability)(_.eligibilityCheckResult)(this)
        }

        "EnteredCanYouSetUpDirectDebit" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true))(
            _.eligibilityCheckResult
          )(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(_.eligibilityCheckResult)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(_.eligibilityCheckResult)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(
            _.eligibilityCheckResult
          )(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterSelectedEmailNoAffordability)(_.eligibilityCheckResult)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(
            _.eligibilityCheckResult
          )(this)
        }

      }

      "Simp when the current stage is" - {

        val differentEligibilityCheckResult =
          TdAll.eligibleEligibilityCheckResultSa.copy(processingDateTime = ProcessingDateTime(Instant.now().toString))

        def testSimpPta[J <: Journey](
          initialJourney: J
        )(existingValue: J => EligibilityCheckResult)(context: JourneyItTest): Unit =
          testUpdateWithExistingValue(initialJourney)(
            _.journeyId,
            existingValue(initialJourney)
          )(
            differentEligibilityCheckResult,
            journeyConnector.updateEligibilityCheckResult(_, _)(using context.request),
            context.tdAll.SimpPta.journeyAfterEligibilityCheckEligible
              .copy(eligibilityCheckResult = differentEligibilityCheckResult)
          )(context)

        def testSimpPtaWithCaseid[J <: Journey](
          initialJourney: J
        )(existingValue: J => EligibilityCheckResult)(context: JourneyItTest): Unit =
          testUpdateWithExistingValue(initialJourney)(
            _.journeyId,
            existingValue(initialJourney)
          )(
            differentEligibilityCheckResult,
            journeyConnector.updateEligibilityCheckResult(_, _)(using context.request),
            context.tdAll.SimpPta.journeyAfterEligibilityCheckEligible.copy(
              eligibilityCheckResult = differentEligibilityCheckResult,
              pegaCaseId = Some(PegaCaseId("case-id"))
            )
          )(context)

        "EligibilityChecked" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterEligibilityCheckEligible)(_.eligibilityCheckResult)(this)
        }

        "ObtainedWhyCannotPayInFullAnswers" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterWhyCannotPayInFullNotRequired)(_.eligibilityCheckResult)(this)
        }

        "AnsweredCanPayUpfront" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterCanPayUpfrontNo)(_.eligibilityCheckResult)(this)
        }

        "EnteredUpfrontPaymentAmount" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterUpfrontPaymentAmount)(_.eligibilityCheckResult)(this)
        }

        "RetrievedExtremeDates" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterExtremeDates)(_.eligibilityCheckResult)(this)
        }

        "RetrievedAffordabilityResult" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterInstalmentAmounts)(_.eligibilityCheckResult)(this)
        }

        "ObtainedCanPayWithinSixMonthsAnswers" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterCanPayWithinSixMonths)(_.eligibilityCheckResult)(this)
        }

        "StartedPegaCase" in new JourneyItTest {
          testSimpPtaWithCaseid(tdAll.SimpPta.journeyAfterStartedPegaCase)(_.eligibilityCheckResult)(this)
        }

        "EnteredMonthlyPaymentAmount" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterMonthlyPaymentAmount)(_.eligibilityCheckResult)(this)
        }

        "EnteredDayOfMonth" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterDayOfMonth)(_.eligibilityCheckResult)(this)
        }

        "RetrievedStartDates" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterStartDatesResponse)(_.eligibilityCheckResult)(this)
        }

        "RetrievedAffordableQuotes" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterAffordableQuotesResponse)(_.eligibilityCheckResult)(this)
        }

        "ChosenPaymentPlan" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterSelectedPaymentPlan)(_.eligibilityCheckResult)(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterCheckedPaymentPlanNonAffordability)(_.eligibilityCheckResult)(this)
        }

        "EnteredCanYouSetUpDirectDebit" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true))(
            _.eligibilityCheckResult
          )(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(_.eligibilityCheckResult)(
            this
          )
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(_.eligibilityCheckResult)(
            this
          )
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(
            _.eligibilityCheckResult
          )(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterSelectedEmail)(_.eligibilityCheckResult)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified))(
            _.eligibilityCheckResult
          )(this)
        }

      }

    }

    "should throw a Bad Request when journey is in stage SubmittedArrangement" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(
        TdAll.EpayeBta
          .journeyAfterSubmittedArrangementNoAffordability()
          .copy(_id = tdAll.journeyId)
          .copy(correlationId = tdAll.correlationId)
      )
      val result: Throwable = journeyConnector
        .updateEligibilityCheckResult(tdAll.journeyId, tdAll.EpayeBta.updateEligibilityCheckRequest())
        .failed
        .futureValue
      result.getMessage should include(
        """{"statusCode":400,"message":"Cannot update EligibilityCheckResult when journey is in completed state"}"""
      )

      verifyCommonActions(numberOfAuthCalls = 1)
    }
  }

}
