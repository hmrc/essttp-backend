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

import essttp.journey.model.{Journey, WhyCannotPayInFullAnswers}
import essttp.rootmodel.CannotPayReason
import paymentsEmailVerification.models.EmailVerificationResult
import testsupport.ItSpec
import testsupport.testdata.TdAll

class UpdateWhyCannotPayInFullControllerSpec extends ItSpec, UpdateJourneyControllerSpec {

  "POST /journey/:journeyId/update-why-cannot-pay-in-full" - {

    "should throw Bad Request when Journey is in a stage [BeforeEligibilityCheck]" in new JourneyItTest {
      stubCommonActions()

      journeyConnector.Epaye.startJourneyBta(TdAll.EpayeBta.sjRequest).futureValue
      journeyConnector.updateTaxId(tdAll.journeyId, TdAll.empRef).futureValue

      val result: Throwable = journeyConnector
        .updateWhyCannotPayInFullAnswers(tdAll.journeyId, tdAll.whyCannotPayInFullNotRequired)
        .failed
        .futureValue
      result.getMessage should include(
        """{"statusCode":400,"message":"WhyCannotPayInFullAnswers update is not possible in that state."}"""
      )

      verifyCommonActions(numberOfAuthCalls = 3)
    }

    "should update the journey when an existing value didn't exist before for" - {

      "Epaye" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.EpayeBta.journeyAfterEligibilityCheckEligible,
          TdAll.whyCannotPayInFullNotRequired
        )(
          journeyConnector.updateWhyCannotPayInFullAnswers,
          tdAll.EpayeBta.journeyAfterWhyCannotPayInFullNotRequired
        )(this)
      }

      "Vat" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.VatBta.journeyAfterEligibilityCheckEligible,
          TdAll.whyCannotPayInFullNotRequired
        )(
          journeyConnector.updateWhyCannotPayInFullAnswers,
          tdAll.VatBta.journeyAfterWhyCannotPayInFullNotRequired
        )(this)
      }

      "Sa" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.SaBta.journeyAfterEligibilityCheckEligible,
          TdAll.whyCannotPayInFullRequired
        )(
          journeyConnector.updateWhyCannotPayInFullAnswers,
          tdAll.SaBta.journeyAfterWhyCannotPayInFullRequired
        )(this)
      }

      "Simp" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.SimpPta.journeyAfterEligibilityCheckEligible,
          TdAll.whyCannotPayInFullRequired
        )(
          journeyConnector.updateWhyCannotPayInFullAnswers,
          tdAll.SimpPta.journeyAfterWhyCannotPayInFullRequired
        )(this)
      }
    }

    "should update the journey when a value already existed" - {

      "Epaye when the current stage is" - {

        val differentWhyCannotPayInFullReasons =
          WhyCannotPayInFullAnswers.WhyCannotPayInFull(
            Set(
              CannotPayReason.NoMoneySetAside,
              CannotPayReason.LostOrReducedAbilityToEarnOrTrade,
              CannotPayReason.NationalOrLocalDisaster
            )
          )

        def testEpayeBta[J <: Journey](initialJourney: J)(
          existingValue:                              J => WhyCannotPayInFullAnswers,
          expectedUpdateInitialJourneyTransformation: J => J
        )(context: JourneyItTest): Unit =
          testUpdateWithExistingValue(initialJourney)(
            _.journeyId,
            existingValue(initialJourney)
          )(
            differentWhyCannotPayInFullReasons,
            journeyConnector.updateWhyCannotPayInFullAnswers(_, _)(using context.request),
            expectedUpdateInitialJourneyTransformation(initialJourney)
          )(context)

        "ObtainedWhyCannotPayInFullAnswers" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterWhyCannotPayInFullNotRequired)(
            _.whyCannotPayInFullAnswers,
            _.copy(
              whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons
            )
          )(this)
        }

        "AnsweredCanPayUpfront" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterCanPayUpfrontNo)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "EnteredUpfrontPaymentAmount" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterUpfrontPaymentAmount)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "RetrievedExtremeDates" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterExtremeDates)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "RetrievedAffordabilityResult" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterInstalmentAmounts)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "ObtainedCanPayWithinSixMonthsAnswers" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterCanPayWithinSixMonthsNotRequired)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "StartedPegaCase" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterStartedPegaCase)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "EnteredMonthlyPaymentAmount" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterMonthlyPaymentAmount)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "EnteredDayOfMonth" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterDayOfMonth)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "RetrievedStartDates" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterStartDatesResponse)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "RetrievedAffordableQuotes" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterAffordableQuotesResponse)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "ChosenPaymentPlan" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterSelectedPaymentPlan)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterCheckedPaymentPlanNonAffordability)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "EnteredCanYouSetUpDirectDebit" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true))(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testEpayeBta(
            tdAll.EpayeBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true)
          )(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterSelectedEmailNoAffordability)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testEpayeBta(
            tdAll.EpayeBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified)
          )(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

      }

      "Vat when the current stage is" - {

        val differentWhyCannotPayInFullReasons =
          WhyCannotPayInFullAnswers.WhyCannotPayInFull(
            Set(
              CannotPayReason.NoMoneySetAside,
              CannotPayReason.LostOrReducedAbilityToEarnOrTrade,
              CannotPayReason.NationalOrLocalDisaster
            )
          )

        def testVatBta[J <: Journey](initialJourney: J)(
          existingValue:                              J => WhyCannotPayInFullAnswers,
          expectedUpdateInitialJourneyTransformation: J => J
        )(context: JourneyItTest): Unit =
          testUpdateWithExistingValue(initialJourney)(
            _.journeyId,
            existingValue(initialJourney)
          )(
            differentWhyCannotPayInFullReasons,
            journeyConnector.updateWhyCannotPayInFullAnswers(_, _)(using context.request),
            expectedUpdateInitialJourneyTransformation(initialJourney)
          )(context)

        "ObtainedWhyCannotPayInFullAnswers" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterWhyCannotPayInFullNotRequired)(
            _.whyCannotPayInFullAnswers,
            _.copy(
              whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons
            )
          )(this)
        }

        "AnsweredCanPayUpfront" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterCanPayUpfrontNo)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "EnteredUpfrontPaymentAmount" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterUpfrontPaymentAmount)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "RetrievedExtremeDates" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterExtremeDates)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "RetrievedAffordabilityResult" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterInstalmentAmounts)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "ObtainedCanPayWithinSixMonthsAnswers" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterCanPayWithinSixMonthsNotRequired)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "StartedPegaCase" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterStartedPegaCase)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "EnteredMonthlyPaymentAmount" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterMonthlyPaymentAmount)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "EnteredDayOfMonth" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterDayOfMonth)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "RetrievedStartDates" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterStartDatesResponse)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "RetrievedAffordableQuotes" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterAffordableQuotesResponse)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "ChosenPaymentPlan" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterSelectedPaymentPlan)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterCheckedPaymentPlanNonAffordability)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "EnteredCanYouSetUpDirectDebit" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true))(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterSelectedEmailNoAffordability)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

      }

      "Sa when the current stage is" - {

        val differentWhyCannotPayInFullReasons =
          WhyCannotPayInFullAnswers.WhyCannotPayInFull(
            Set(
              CannotPayReason.NoMoneySetAside,
              CannotPayReason.LostOrReducedAbilityToEarnOrTrade,
              CannotPayReason.NationalOrLocalDisaster
            )
          )

        def testSaBta[J <: Journey](initialJourney: J)(
          existingValue:                              J => WhyCannotPayInFullAnswers,
          expectedUpdateInitialJourneyTransformation: J => J
        )(context: JourneyItTest): Unit =
          testUpdateWithExistingValue(initialJourney)(
            _.journeyId,
            existingValue(initialJourney)
          )(
            differentWhyCannotPayInFullReasons,
            journeyConnector.updateWhyCannotPayInFullAnswers(_, _)(using context.request),
            expectedUpdateInitialJourneyTransformation(initialJourney)
          )(context)

        "ObtainedWhyCannotPayInFullAnswers" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterWhyCannotPayInFullNotRequired)(
            _.whyCannotPayInFullAnswers,
            _.copy(
              whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons
            )
          )(this)
        }

        "AnsweredCanPayUpfront" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterCanPayUpfrontNo)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "EnteredUpfrontPaymentAmount" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterUpfrontPaymentAmount)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "RetrievedExtremeDates" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterExtremeDates)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "RetrievedAffordabilityResult" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterInstalmentAmounts)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "ObtainedCanPayWithinSixMonthsAnswers" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterCanPayWithinSixMonths)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "StartedPegaCase" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterStartedPegaCase)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "EnteredMonthlyPaymentAmount" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterMonthlyPaymentAmount)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "EnteredDayOfMonth" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterDayOfMonth)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "RetrievedStartDates" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterStartDatesResponse)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "RetrievedAffordableQuotes" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterAffordableQuotesResponse)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "ChosenPaymentPlan" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterSelectedPaymentPlan)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterCheckedPaymentPlanNonAffordability)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "EnteredCanYouSetUpDirectDebit" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true))(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterSelectedEmailNoAffordability)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

      }

      "Simp when the current stage is" - {

        val differentWhyCannotPayInFullReasons =
          WhyCannotPayInFullAnswers.WhyCannotPayInFull(
            Set(
              CannotPayReason.NoMoneySetAside,
              CannotPayReason.LostOrReducedAbilityToEarnOrTrade,
              CannotPayReason.NationalOrLocalDisaster
            )
          )

        def testSimpPta[J <: Journey](initialJourney: J)(
          existingValue:                              J => WhyCannotPayInFullAnswers,
          expectedUpdateInitialJourneyTransformation: J => J
        )(context: JourneyItTest): Unit =
          testUpdateWithExistingValue(initialJourney)(
            _.journeyId,
            existingValue(initialJourney)
          )(
            differentWhyCannotPayInFullReasons,
            journeyConnector.updateWhyCannotPayInFullAnswers(_, _)(using context.request),
            expectedUpdateInitialJourneyTransformation(initialJourney)
          )(context)

        "ObtainedWhyCannotPayInFullAnswers" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterWhyCannotPayInFullNotRequired)(
            _.whyCannotPayInFullAnswers,
            _.copy(
              whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons
            )
          )(this)
        }

        "AnsweredCanPayUpfront" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterCanPayUpfrontNo)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "EnteredUpfrontPaymentAmount" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterUpfrontPaymentAmount)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "RetrievedExtremeDates" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterExtremeDates)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "RetrievedAffordabilityResult" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterInstalmentAmounts)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "ObtainedCanPayWithinSixMonthsAnswers" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterCanPayWithinSixMonths)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "StartedPegaCase" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterStartedPegaCase)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "EnteredMonthlyPaymentAmount" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterMonthlyPaymentAmount)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "EnteredDayOfMonth" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterDayOfMonth)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "RetrievedStartDates" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterStartDatesResponse)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "RetrievedAffordableQuotes" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterAffordableQuotesResponse)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "ChosenPaymentPlan" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterSelectedPaymentPlan)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterCheckedPaymentPlanNonAffordability)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "EnteredCanYouSetUpDirectDebit" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true))(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterSelectedEmail)(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
          )(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified))(
            _.whyCannotPayInFullAnswers,
            _.copy(whyCannotPayInFullAnswers = differentWhyCannotPayInFullReasons)
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
        .updateWhyCannotPayInFullAnswers(tdAll.journeyId, tdAll.whyCannotPayInFullRequired)
        .failed
        .futureValue
      result.getMessage should include(
        """{"statusCode":400,"message":"Cannot update WhyCannotPayInFullAnswers when journey is in completed state"}"""
      )

      verifyCommonActions(numberOfAuthCalls = 1)
    }
  }

}
