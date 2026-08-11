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

import essttp.journey.model.Journey.AssessmentCategoryDetermined
import essttp.journey.model.{Journey, WhyCannotPayInFullAnswers}
import essttp.rootmodel.bank.TypesOfBankAccount
import essttp.rootmodel.ttp.eligibility.AssessmentCategory
import paymentsEmailVerification.models.EmailVerificationResult
import testsupport.ItSpec
import testsupport.testdata.TdAll

class UpdateAssessmentCategoryControllerSpec extends ItSpec, UpdateJourneyControllerSpec {

  "POST /journey/:journeyId/update-assessment-category" - {

    "should throw Bad Request when Journey is in a stage [BeforeEligibilityChecked]" in new JourneyItTest {
      stubCommonActions()

      journeyConnector.Epaye.startJourneyBta(TdAll.EpayeBta.sjRequest).futureValue
      journeyConnector.updateTaxId(tdAll.journeyId, TdAll.empRef).futureValue

      val result: Throwable = journeyConnector
        .updateAssessmentCategory(tdAll.journeyId, AssessmentCategory.Standard)
        .failed
        .futureValue
      result.getMessage should include(
        """{"statusCode":400,"message":"AssessmentCategory update is not possible in that state."}"""
      )

      verifyCommonActions(numberOfAuthCalls = 3)
    }

    "should update the journey when an existing value didn't exist before for" - {

      "Epaye" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.EpayeBta.journeyAfterEligibilityCheckEligible,
          AssessmentCategory.Standard
        )(
          journeyConnector.updateAssessmentCategory,
          tdAll.EpayeBta.journeyAfterAssessmentCategoryDetermined()
        )(this)
      }

      "Vat" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.VatBta.journeyAfterEligibilityCheckEligible,
          AssessmentCategory.Standard
        )(
          journeyConnector.updateAssessmentCategory,
          tdAll.VatBta.journeyAfterAssessmentCategoryDetermined()
        )(this)
      }

      "Sa" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.SaBta.journeyAfterEligibilityCheckEligible,
          AssessmentCategory.Standard
        )(
          journeyConnector.updateAssessmentCategory,
          tdAll.SaBta.journeyAfterAssessmentCategoryDetermined()
        )(this)
      }

      "Simp" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.SimpPta.journeyAfterEligibilityCheckEligible,
          AssessmentCategory.Standard
        )(
          journeyConnector.updateAssessmentCategory,
          tdAll.SimpPta.journeyAfterAssessmentCategoryDetermined()
        )(this)
      }
    }

    "should update the journey when a value already existed" - {

      "Epaye when the current stage is" - {

        val differentAssessmentCategory = AssessmentCategory.DebtsAndLiabilities

        def testEpayeBta[J <: Journey](initialJourney: J)(
          existingValue:          J => AssessmentCategory,
          expectedUpdatedJourney: AssessmentCategoryDetermined
        )(context: JourneyItTest): Unit =
          testUpdateWithExistingValue(initialJourney)(
            _.journeyId,
            existingValue(initialJourney)
          )(
            differentAssessmentCategory,
            journeyConnector.updateAssessmentCategory(_, _)(using context.request),
            expectedUpdatedJourney
          )(context)

        "AssessmentCategoryDetermined" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterAssessmentCategoryDetermined())(
            _.assessmentCategory,
            tdAll.EpayeBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "ObtainedWhyCannotPayInFullAnswers" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterWhyCannotPayInFullNotRequired)(
            _.assessmentCategory,
            tdAll.EpayeBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "AnsweredCanPayUpfront" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterCanPayUpfrontNo)(
            _.assessmentCategory,
            tdAll.EpayeBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "EnteredUpfrontPaymentAmount" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterUpfrontPaymentAmount)(
            _.assessmentCategory,
            tdAll.EpayeBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "RetrievedExtremeDates" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterExtremeDates)(
            _.assessmentCategory,
            tdAll.EpayeBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "RetrievedAffordabilityResult" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterInstalmentAmounts)(
            _.assessmentCategory,
            tdAll.EpayeBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "ObtainedCanPayWithinSixMonthsAnswers" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterCanPayWithinSixMonthsNotRequired)(
            _.assessmentCategory,
            tdAll.EpayeBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "StartedPegaCase" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterStartedPegaCase)(
            _.assessmentCategory,
            tdAll.EpayeBta
              .journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
              .copy(pegaCaseId = Some(tdAll.pegaCaseId))
          )(this)
        }

        "EnteredMonthlyPaymentAmount" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterMonthlyPaymentAmount)(
            _.assessmentCategory,
            tdAll.EpayeBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "EnteredDayOfMonth" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterDayOfMonth)(
            _.assessmentCategory,
            tdAll.EpayeBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "RetrievedStartDates" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterStartDatesResponse)(
            _.assessmentCategory,
            tdAll.EpayeBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "RetrievedAffordableQuotes" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterAffordableQuotesResponse)(
            _.assessmentCategory,
            tdAll.EpayeBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "ChosenPaymentPlan" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterSelectedPaymentPlan)(
            _.assessmentCategory,
            tdAll.EpayeBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterCheckedPaymentPlanNonAffordability)(
            _.assessmentCategory,
            tdAll.EpayeBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "EnteredCanYouSetUpDirectDebit" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true))(
            _.assessmentCategory,
            tdAll.EpayeBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "ChosenTypeOfBankAccount" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterChosenTypeOfBankAccount(TypesOfBankAccount.Business))(
            _.assessmentCategory,
            tdAll.EpayeBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(
            _.assessmentCategory,
            tdAll.EpayeBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(
            _.assessmentCategory,
            tdAll.EpayeBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testEpayeBta(
            tdAll.EpayeBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true)
          )(
            _.assessmentCategory,
            tdAll.EpayeBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterSelectedEmailNoAffordability)(
            _.assessmentCategory,
            tdAll.EpayeBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testEpayeBta(
            tdAll.EpayeBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified)
          )(
            _.assessmentCategory,
            tdAll.EpayeBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

      }

      "Vat when the current stage is" - {

        val differentAssessmentCategory = AssessmentCategory.Debts

        def testVatBta[J <: Journey](initialJourney: J)(
          existingValue:          J => AssessmentCategory,
          expectedUpdatedJourney: AssessmentCategoryDetermined
        )(context: JourneyItTest): Unit =
          testUpdateWithExistingValue(initialJourney)(
            _.journeyId,
            existingValue(initialJourney)
          )(
            differentAssessmentCategory,
            journeyConnector.updateAssessmentCategory(_, _)(using context.request),
            expectedUpdatedJourney
          )(context)

        "AssessmentCategoryDetermined" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterAssessmentCategoryDetermined())(
            _.assessmentCategory,
            tdAll.VatBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "ObtainedWhyCannotPayInFullAnswers" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterWhyCannotPayInFullNotRequired)(
            _.assessmentCategory,
            tdAll.VatBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "AnsweredCanPayUpfront" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterCanPayUpfrontNo)(
            _.assessmentCategory,
            tdAll.VatBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "EnteredUpfrontPaymentAmount" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterUpfrontPaymentAmount)(
            _.assessmentCategory,
            tdAll.VatBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "RetrievedExtremeDates" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterExtremeDates)(
            _.assessmentCategory,
            tdAll.VatBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "RetrievedAffordabilityResult" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterInstalmentAmounts)(
            _.assessmentCategory,
            tdAll.VatBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "ObtainedCanPayWithinSixMonthsAnswers" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterCanPayWithinSixMonthsNotRequired)(
            _.assessmentCategory,
            tdAll.VatBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "StartedPegaCase" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterStartedPegaCase)(
            _.assessmentCategory,
            tdAll.VatBta
              .journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
              .copy(pegaCaseId = Some(tdAll.pegaCaseId))
          )(this)
        }

        "EnteredMonthlyPaymentAmount" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterMonthlyPaymentAmount)(
            _.assessmentCategory,
            tdAll.VatBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "EnteredDayOfMonth" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterDayOfMonth)(
            _.assessmentCategory,
            tdAll.VatBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "RetrievedStartDates" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterStartDatesResponse)(
            _.assessmentCategory,
            tdAll.VatBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "RetrievedAffordableQuotes" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterAffordableQuotesResponse)(
            _.assessmentCategory,
            tdAll.VatBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "ChosenPaymentPlan" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterSelectedPaymentPlan)(
            _.assessmentCategory,
            tdAll.VatBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterCheckedPaymentPlanNonAffordability)(
            _.assessmentCategory,
            tdAll.VatBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "EnteredCanYouSetUpDirectDebit" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true))(
            _.assessmentCategory,
            tdAll.VatBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "ChosenTypeOfBankAccount" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterChosenTypeOfBankAccount(TypesOfBankAccount.Personal))(
            _.assessmentCategory,
            tdAll.VatBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(
            _.assessmentCategory,
            tdAll.VatBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(
            _.assessmentCategory,
            tdAll.VatBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(
            _.assessmentCategory,
            tdAll.VatBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterSelectedEmailNoAffordability)(
            _.assessmentCategory,
            tdAll.VatBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(
            _.assessmentCategory,
            tdAll.VatBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

      }

      "Sa when the current stage is" - {

        val differentAssessmentCategory = AssessmentCategory.Liabilities

        def testSaBta[J <: Journey](initialJourney: J)(
          existingValue:          J => AssessmentCategory,
          expectedUpdatedJourney: AssessmentCategoryDetermined
        )(context: JourneyItTest): Unit =
          testUpdateWithExistingValue(initialJourney)(
            _.journeyId,
            existingValue(initialJourney)
          )(
            differentAssessmentCategory,
            journeyConnector.updateAssessmentCategory(_, _)(using context.request),
            expectedUpdatedJourney
          )(context)

        "AssessmentCategoryDetermined" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterAssessmentCategoryDetermined())(
            _.assessmentCategory,
            tdAll.SaBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "ObtainedWhyCannotPayInFullAnswers" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterWhyCannotPayInFullNotRequired)(
            _.assessmentCategory,
            tdAll.SaBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "AnsweredCanPayUpfront" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterCanPayUpfrontNo)(
            _.assessmentCategory,
            tdAll.SaBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "EnteredUpfrontPaymentAmount" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterUpfrontPaymentAmount)(
            _.assessmentCategory,
            tdAll.SaBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "RetrievedExtremeDates" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterExtremeDates)(
            _.assessmentCategory,
            tdAll.SaBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "RetrievedAffordabilityResult" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterInstalmentAmounts)(
            _.assessmentCategory,
            tdAll.SaBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "ObtainedCanPayWithinSixMonthsAnswers" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterCanPayWithinSixMonths)(
            _.assessmentCategory,
            tdAll.SaBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "StartedPegaCase" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterStartedPegaCase)(
            _.assessmentCategory,
            tdAll.SaBta
              .journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
              .copy(pegaCaseId = Some(tdAll.pegaCaseId))
          )(this)
        }

        "EnteredMonthlyPaymentAmount" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterMonthlyPaymentAmount)(
            _.assessmentCategory,
            tdAll.SaBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "EnteredDayOfMonth" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterDayOfMonth)(
            _.assessmentCategory,
            tdAll.SaBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "RetrievedStartDates" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterStartDatesResponse)(
            _.assessmentCategory,
            tdAll.SaBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "RetrievedAffordableQuotes" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterAffordableQuotesResponse)(
            _.assessmentCategory,
            tdAll.SaBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "ChosenPaymentPlan" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterSelectedPaymentPlan)(
            _.assessmentCategory,
            tdAll.SaBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterCheckedPaymentPlanNonAffordability)(
            _.assessmentCategory,
            tdAll.SaBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "EnteredCanYouSetUpDirectDebit" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true))(
            _.assessmentCategory,
            tdAll.SaBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "ChosenTypeOfBankAccount" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterChosenTypeOfBankAccount(TypesOfBankAccount.Business))(
            _.assessmentCategory,
            tdAll.SaBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(
            _.assessmentCategory,
            tdAll.SaBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(
            _.assessmentCategory,
            tdAll.SaBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(
            _.assessmentCategory,
            tdAll.SaBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterSelectedEmailNoAffordability)(
            _.assessmentCategory,
            tdAll.SaBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(
            _.assessmentCategory,
            tdAll.SaBta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

      }

      "Simp when the current stage is" - {

        val differentAssessmentCategory = AssessmentCategory.DebtsAndLiabilities

        def testSimpPta[J <: Journey](initialJourney: J)(
          existingValue:          J => AssessmentCategory,
          expectedUpdatedJourney: AssessmentCategoryDetermined
        )(context: JourneyItTest): Unit =
          testUpdateWithExistingValue(initialJourney)(
            _.journeyId,
            existingValue(initialJourney)
          )(
            differentAssessmentCategory,
            journeyConnector.updateAssessmentCategory(_, _)(using context.request),
            expectedUpdatedJourney
          )(context)

        "AssessmentCategoryDetermined" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterAssessmentCategoryDetermined())(
            _.assessmentCategory,
            tdAll.SimpPta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "ObtainedWhyCannotPayInFullAnswers" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterAssessmentCategoryDetermined())(
            _.assessmentCategory,
            tdAll.SimpPta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "AnsweredCanPayUpfront" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterCanPayUpfrontNo)(
            _.assessmentCategory,
            tdAll.SimpPta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "EnteredUpfrontPaymentAmount" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterUpfrontPaymentAmount)(
            _.assessmentCategory,
            tdAll.SimpPta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "RetrievedExtremeDates" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterExtremeDates)(
            _.assessmentCategory,
            tdAll.SimpPta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "RetrievedAffordabilityResult" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterInstalmentAmounts)(
            _.assessmentCategory,
            tdAll.SimpPta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "ObtainedCanPayWithinSixMonthsAnswers" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterCanPayWithinSixMonths)(
            _.assessmentCategory,
            tdAll.SimpPta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "StartedPegaCase" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterStartedPegaCase)(
            _.assessmentCategory,
            tdAll.SimpPta
              .journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
              .copy(pegaCaseId = Some(tdAll.pegaCaseId))
          )(this)
        }

        "EnteredMonthlyPaymentAmount" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterMonthlyPaymentAmount)(
            _.assessmentCategory,
            tdAll.SimpPta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "EnteredDayOfMonth" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterDayOfMonth)(
            _.assessmentCategory,
            tdAll.SimpPta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "RetrievedStartDates" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterStartDatesResponse)(
            _.assessmentCategory,
            tdAll.SimpPta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "RetrievedAffordableQuotes" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterAffordableQuotesResponse)(
            _.assessmentCategory,
            tdAll.SimpPta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "ChosenPaymentPlan" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterSelectedPaymentPlan)(
            _.assessmentCategory,
            tdAll.SimpPta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterCheckedPaymentPlanNonAffordability)(
            _.assessmentCategory,
            tdAll.SimpPta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "EnteredCanYouSetUpDirectDebit" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true))(
            _.assessmentCategory,
            tdAll.SimpPta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "ChosenTypeOfBankAccount" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterChosenTypeOfBankAccount(TypesOfBankAccount.Personal))(
            _.assessmentCategory,
            tdAll.SimpPta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(
            _.assessmentCategory,
            tdAll.SimpPta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(
            _.assessmentCategory,
            tdAll.SimpPta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(
            _.assessmentCategory,
            tdAll.SimpPta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterSelectedEmail)(
            _.assessmentCategory,
            tdAll.SimpPta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
          )(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified))(
            _.assessmentCategory,
            tdAll.SimpPta.journeyAfterAssessmentCategoryDetermined(differentAssessmentCategory)
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
        .updateAssessmentCategory(tdAll.journeyId, AssessmentCategory.Standard)
        .failed
        .futureValue
      result.getMessage should include(
        """{"statusCode":400,"message":"Cannot update AssessmentCategory when journey is in completed state"}"""
      )

      verifyCommonActions(numberOfAuthCalls = 1)
    }
  }

}
