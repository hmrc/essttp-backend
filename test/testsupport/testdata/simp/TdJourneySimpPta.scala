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

package testsupport.testdata.simp

import essttp.journey.model.SjRequest.Simp
import essttp.journey.model._
import essttp.rootmodel._
import essttp.rootmodel.bank.{BankDetails, CanSetUpDirectDebit}
import essttp.rootmodel.dates.extremedates.ExtremeDatesResponse
import essttp.rootmodel.dates.startdates.StartDatesResponse
import essttp.rootmodel.ttp.affordability.InstalmentAmounts
import essttp.rootmodel.ttp.affordablequotes.{AffordableQuotesResponse, PaymentPlan}
import essttp.rootmodel.ttp.arrangement.ArrangementResponse
import essttp.rootmodel.ttp.eligibility.EligibilityCheckResult
import paymentsEmailVerification.models.EmailVerificationResult
import play.api.libs.json.JsNull
import testsupport.testdata.{TdBase, TdJourneyStructure}

trait TdJourneySimpPta {
  dependencies: TdBase with TdSimp =>

  object SimpPta extends TdJourneyStructure {

    def sjRequest: Simp.Simple = SjRequest.Simp.Simple(
      dependencies.returnUrl,
      dependencies.backUrl
    )

    def sjResponse: SjResponse = SjResponse(
      nextUrl = NextUrl(s"http://localhost:9215/set-up-a-payment-plan/simple-assessment-payment-plan"),
      journeyId = dependencies.journeyId
    )

    def postPath: String = "/simp/pta/journey/start"

    def journeyAfterStarted: Journey.Started = Journey.Started(
      _id = dependencies.journeyId,
      origin = Origins.Simp.Pta,
      createdOn = dependencies.createdOn,
      sjRequest = sjRequest,
      sessionId = dependencies.sessionId,
      affordabilityEnabled = Some(false),
      correlationId = dependencies.correlationId,
      pegaCaseId = None
    )

    def updateTaxIdRequest(): TaxId = nino

    def journeyAfterDetermineTaxIds: Journey.ComputedTaxId = Journey.ComputedTaxId(
      _id = dependencies.journeyId,
      origin = Origins.Simp.Pta,
      createdOn = dependencies.createdOn,
      sjRequest = sjRequest,
      sessionId = dependencies.sessionId,
      affordabilityEnabled = Some(false),
      correlationId = dependencies.correlationId,
      taxId = nino,
      pegaCaseId = None
    )

    def updateEligibilityCheckRequest(): EligibilityCheckResult = eligibleEligibilityCheckResultSimp

    def journeyAfterEligibilityCheckEligible: Journey.EligibilityChecked = Journey.EligibilityChecked(
      _id = dependencies.journeyId,
      origin = Origins.Simp.Pta,
      createdOn = dependencies.createdOn,
      sjRequest = sjRequest,
      sessionId = dependencies.sessionId,
      affordabilityEnabled = Some(false),
      correlationId = dependencies.correlationId,
      taxId = nino,
      eligibilityCheckResult = eligibleEligibilityCheckResultSimp,
      pegaCaseId = None
    )

    def journeyAfterEligibilityCheckNotEligible: Journey.EligibilityChecked = Journey.EligibilityChecked(
      _id = dependencies.journeyId,
      origin = Origins.Simp.Pta,
      createdOn = dependencies.createdOn,
      sjRequest = sjRequest,
      sessionId = dependencies.sessionId,
      affordabilityEnabled = Some(false),
      correlationId = dependencies.correlationId,
      taxId = nino,
      eligibilityCheckResult = ineligibleEligibilityCheckResultSimp,
      pegaCaseId = None
    )

    def journeyAfterWhyCannotPayInFullRequired: Journey.ObtainedWhyCannotPayInFullAnswers =
      Journey.ObtainedWhyCannotPayInFullAnswers(
        _id = dependencies.journeyId,
        origin = Origins.Simp.Pta,
        createdOn = dependencies.createdOn,
        sjRequest = sjRequest,
        sessionId = dependencies.sessionId,
        affordabilityEnabled = Some(false),
        correlationId = dependencies.correlationId,
        taxId = nino,
        eligibilityCheckResult = eligibleEligibilityCheckResultSimp,
        whyCannotPayInFullAnswers = whyCannotPayInFullRequired,
        pegaCaseId = None
      )

    def journeyAfterWhyCannotPayInFullNotRequired: Journey.ObtainedWhyCannotPayInFullAnswers =
      Journey.ObtainedWhyCannotPayInFullAnswers(
        _id = dependencies.journeyId,
        origin = Origins.Simp.Pta,
        createdOn = dependencies.createdOn,
        sjRequest = sjRequest,
        sessionId = dependencies.sessionId,
        affordabilityEnabled = Some(false),
        correlationId = dependencies.correlationId,
        taxId = nino,
        eligibilityCheckResult = eligibleEligibilityCheckResultSimp,
        whyCannotPayInFullAnswers = WhyCannotPayInFullAnswers.AnswerNotRequired,
        pegaCaseId = None
      )

    def updateCanPayUpfrontYesRequest(): CanPayUpfront = canPayUpfrontYes

    def updateCanPayUpfrontNoRequest(): CanPayUpfront = canPayUpfrontNo

    def journeyAfterCanPayUpfrontYes: Journey.AnsweredCanPayUpfront = Journey.AnsweredCanPayUpfront(
      _id = dependencies.journeyId,
      origin = Origins.Simp.Pta,
      createdOn = dependencies.createdOn,
      sjRequest = sjRequest,
      sessionId = dependencies.sessionId,
      affordabilityEnabled = Some(false),
      correlationId = dependencies.correlationId,
      taxId = nino,
      eligibilityCheckResult = eligibleEligibilityCheckResultSimp,
      whyCannotPayInFullAnswers = WhyCannotPayInFullAnswers.AnswerNotRequired,
      canPayUpfront = canPayUpfrontYes,
      pegaCaseId = None
    )

    def journeyAfterCanPayUpfrontNo: Journey.AnsweredCanPayUpfront = Journey.AnsweredCanPayUpfront(
      _id = dependencies.journeyId,
      origin = Origins.Simp.Pta,
      createdOn = dependencies.createdOn,
      sjRequest = sjRequest,
      sessionId = dependencies.sessionId,
      affordabilityEnabled = Some(false),
      correlationId = dependencies.correlationId,
      taxId = nino,
      eligibilityCheckResult = eligibleEligibilityCheckResultSimp,
      whyCannotPayInFullAnswers = WhyCannotPayInFullAnswers.AnswerNotRequired,
      canPayUpfront = canPayUpfrontNo,
      pegaCaseId = None
    )

    def updateUpfrontPaymentAmountRequest(): UpfrontPaymentAmount = dependencies.upfrontPaymentAmount

    // used in specific test for changing upfront payment amount, no need to copy to other TdJourneys
    def anotherUpdateUpfrontPaymentAmountRequest(): UpfrontPaymentAmount = dependencies.anotherUpfrontPaymentAmount

    def journeyAfterUpfrontPaymentAmount: Journey.EnteredUpfrontPaymentAmount =
      Journey.EnteredUpfrontPaymentAmount(
        _id = dependencies.journeyId,
        origin = Origins.Simp.Pta,
        createdOn = dependencies.createdOn,
        sjRequest = sjRequest,
        sessionId = dependencies.sessionId,
        affordabilityEnabled = Some(false),
        correlationId = dependencies.correlationId,
        taxId = nino,
        eligibilityCheckResult = eligibleEligibilityCheckResultSimp,
        whyCannotPayInFullAnswers = WhyCannotPayInFullAnswers.AnswerNotRequired,
        canPayUpfront = canPayUpfrontYes,
        upfrontPaymentAmount = dependencies.upfrontPaymentAmount,
        pegaCaseId = None
      )

    def updateExtremeDatesRequest(): ExtremeDatesResponse = dependencies.extremeDatesWithUpfrontPayment

    def journeyAfterExtremeDates: Journey.RetrievedExtremeDates = Journey.RetrievedExtremeDates(
      _id = dependencies.journeyId,
      origin = Origins.Simp.Pta,
      createdOn = dependencies.createdOn,
      sjRequest = sjRequest,
      sessionId = dependencies.sessionId,
      affordabilityEnabled = Some(false),
      correlationId = dependencies.correlationId,
      taxId = nino,
      eligibilityCheckResult = eligibleEligibilityCheckResultSimp,
      whyCannotPayInFullAnswers = WhyCannotPayInFullAnswers.AnswerNotRequired,
      upfrontPaymentAnswers = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse = dependencies.extremeDatesWithUpfrontPayment,
      pegaCaseId = None
    )

    def updateInstalmentAmountsRequest(): InstalmentAmounts = dependencies.instalmentAmounts

    def journeyAfterInstalmentAmounts: Journey.RetrievedAffordabilityResult =
      Journey.RetrievedAffordabilityResult(
        _id = dependencies.journeyId,
        origin = Origins.Simp.Pta,
        createdOn = dependencies.createdOn,
        sjRequest = sjRequest,
        sessionId = dependencies.sessionId,
        affordabilityEnabled = Some(false),
        correlationId = dependencies.correlationId,
        taxId = nino,
        eligibilityCheckResult = eligibleEligibilityCheckResultSimp,
        whyCannotPayInFullAnswers = WhyCannotPayInFullAnswers.AnswerNotRequired,
        upfrontPaymentAnswers = dependencies.upfrontPaymentAnswersDeclared,
        extremeDatesResponse = dependencies.extremeDatesWithUpfrontPayment,
        instalmentAmounts = dependencies.instalmentAmounts,
        pegaCaseId = None
      )

    def journeyAfterCanPayWithinSixMonths: Journey.ObtainedCanPayWithinSixMonthsAnswers =
      Journey.ObtainedCanPayWithinSixMonthsAnswers(
        _id = dependencies.journeyId,
        origin = Origins.Simp.Pta,
        createdOn = dependencies.createdOn,
        sjRequest = sjRequest,
        sessionId = dependencies.sessionId,
        affordabilityEnabled = Some(false),
        correlationId = dependencies.correlationId,
        taxId = nino,
        eligibilityCheckResult = eligibleEligibilityCheckResultSimp,
        whyCannotPayInFullAnswers = WhyCannotPayInFullAnswers.AnswerNotRequired,
        upfrontPaymentAnswers = dependencies.upfrontPaymentAnswersDeclared,
        extremeDatesResponse = dependencies.extremeDatesWithUpfrontPayment,
        instalmentAmounts = dependencies.instalmentAmounts,
        canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
        pegaCaseId = None
      )

    def journeyAfterStartedPegaCase: Journey.StartedPegaCase = Journey.StartedPegaCase(
      _id = dependencies.journeyId,
      origin = Origins.Simp.Pta,
      createdOn = dependencies.createdOn,
      sjRequest = sjRequest,
      sessionId = dependencies.sessionId,
      affordabilityEnabled = Some(false),
      correlationId = dependencies.correlationId,
      taxId = nino,
      eligibilityCheckResult = eligibleEligibilityCheckResultSimp,
      whyCannotPayInFullAnswers = WhyCannotPayInFullAnswers.AnswerNotRequired,
      upfrontPaymentAnswers = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts = dependencies.instalmentAmounts,
      canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
      startCaseResponse = dependencies.startCaseResponse,
      pegaCaseId = Some(dependencies.pegaCaseId)
    )

    def updateMonthlyPaymentAmountRequest(): MonthlyPaymentAmount = dependencies.monthlyPaymentAmount

    def journeyAfterMonthlyPaymentAmount: Journey.EnteredMonthlyPaymentAmount =
      Journey.EnteredMonthlyPaymentAmount(
        _id = dependencies.journeyId,
        origin = Origins.Simp.Pta,
        createdOn = dependencies.createdOn,
        sjRequest = sjRequest,
        sessionId = dependencies.sessionId,
        affordabilityEnabled = Some(false),
        correlationId = dependencies.correlationId,
        taxId = nino,
        eligibilityCheckResult = eligibleEligibilityCheckResultSimp,
        whyCannotPayInFullAnswers = WhyCannotPayInFullAnswers.AnswerNotRequired,
        upfrontPaymentAnswers = dependencies.upfrontPaymentAnswersDeclared,
        extremeDatesResponse = dependencies.extremeDatesWithUpfrontPayment,
        instalmentAmounts = dependencies.instalmentAmounts,
        canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
        monthlyPaymentAmount = dependencies.monthlyPaymentAmount,
        pegaCaseId = None
      )

    def updateDayOfMonthRequest(): DayOfMonth = dependencies.dayOfMonth

    def journeyAfterDayOfMonth: Journey.EnteredDayOfMonth = Journey.EnteredDayOfMonth(
      _id = dependencies.journeyId,
      origin = Origins.Simp.Pta,
      createdOn = dependencies.createdOn,
      sjRequest = sjRequest,
      sessionId = dependencies.sessionId,
      affordabilityEnabled = Some(false),
      correlationId = dependencies.correlationId,
      taxId = nino,
      eligibilityCheckResult = eligibleEligibilityCheckResultSimp,
      whyCannotPayInFullAnswers = WhyCannotPayInFullAnswers.AnswerNotRequired,
      upfrontPaymentAnswers = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts = dependencies.instalmentAmounts,
      canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
      monthlyPaymentAmount = dependencies.monthlyPaymentAmount,
      dayOfMonth = dependencies.dayOfMonth,
      pegaCaseId = None
    )

    def updateStartDatesResponse(): StartDatesResponse = dependencies.startDatesResponseWithInitialPayment

    def journeyAfterStartDatesResponse: Journey.RetrievedStartDates = Journey.RetrievedStartDates(
      _id = dependencies.journeyId,
      origin = Origins.Simp.Pta,
      createdOn = dependencies.createdOn,
      sjRequest = sjRequest,
      sessionId = dependencies.sessionId,
      affordabilityEnabled = Some(false),
      correlationId = dependencies.correlationId,
      taxId = nino,
      eligibilityCheckResult = eligibleEligibilityCheckResultSimp,
      whyCannotPayInFullAnswers = WhyCannotPayInFullAnswers.AnswerNotRequired,
      upfrontPaymentAnswers = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts = dependencies.instalmentAmounts,
      canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
      monthlyPaymentAmount = dependencies.monthlyPaymentAmount,
      dayOfMonth = dependencies.dayOfMonth,
      startDatesResponse = dependencies.startDatesResponseWithInitialPayment,
      pegaCaseId = None
    )

    def updateAffordableQuotesResponse(): AffordableQuotesResponse = dependencies.affordableQuotesResponse

    def journeyAfterAffordableQuotesResponse: Journey.RetrievedAffordableQuotes =
      Journey.RetrievedAffordableQuotes(
        _id = dependencies.journeyId,
        origin = Origins.Simp.Pta,
        createdOn = dependencies.createdOn,
        sjRequest = sjRequest,
        sessionId = dependencies.sessionId,
        affordabilityEnabled = Some(false),
        correlationId = dependencies.correlationId,
        taxId = nino,
        eligibilityCheckResult = eligibleEligibilityCheckResultSimp,
        whyCannotPayInFullAnswers = WhyCannotPayInFullAnswers.AnswerNotRequired,
        upfrontPaymentAnswers = dependencies.upfrontPaymentAnswersDeclared,
        extremeDatesResponse = dependencies.extremeDatesWithUpfrontPayment,
        instalmentAmounts = dependencies.instalmentAmounts,
        canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
        monthlyPaymentAmount = dependencies.monthlyPaymentAmount,
        dayOfMonth = dependencies.dayOfMonth,
        startDatesResponse = dependencies.startDatesResponseWithInitialPayment,
        affordableQuotesResponse = dependencies.affordableQuotesResponse,
        pegaCaseId = None
      )

    def updateSelectedPaymentPlanRequest(): PaymentPlan = dependencies.paymentPlan(1)

    def journeyAfterSelectedPaymentPlan: Journey.ChosenPaymentPlan = Journey.ChosenPaymentPlan(
      _id = dependencies.journeyId,
      origin = Origins.Simp.Pta,
      createdOn = dependencies.createdOn,
      sjRequest = sjRequest,
      sessionId = dependencies.sessionId,
      affordabilityEnabled = Some(false),
      correlationId = dependencies.correlationId,
      taxId = nino,
      eligibilityCheckResult = eligibleEligibilityCheckResultSimp,
      whyCannotPayInFullAnswers = WhyCannotPayInFullAnswers.AnswerNotRequired,
      upfrontPaymentAnswers = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts = dependencies.instalmentAmounts,
      canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
      monthlyPaymentAmount = dependencies.monthlyPaymentAmount,
      dayOfMonth = dependencies.dayOfMonth,
      startDatesResponse = dependencies.startDatesResponseWithInitialPayment,
      affordableQuotesResponse = dependencies.affordableQuotesResponse,
      selectedPaymentPlan = dependencies.paymentPlan(1),
      pegaCaseId = None
    )

    def updateCheckedPaymentPlanRequest(): JsNull.type = JsNull

    def journeyAfterCheckedPaymentPlanNonAffordability: Journey.CheckedPaymentPlan =
      Journey.CheckedPaymentPlan(
        _id = dependencies.journeyId,
        origin = Origins.Simp.Pta,
        createdOn = dependencies.createdOn,
        sjRequest = sjRequest,
        sessionId = dependencies.sessionId,
        affordabilityEnabled = Some(false),
        correlationId = dependencies.correlationId,
        taxId = nino,
        eligibilityCheckResult = eligibleEligibilityCheckResultSimp,
        whyCannotPayInFullAnswers = WhyCannotPayInFullAnswers.AnswerNotRequired,
        upfrontPaymentAnswers = dependencies.upfrontPaymentAnswersDeclared,
        extremeDatesResponse = dependencies.extremeDatesWithUpfrontPayment,
        instalmentAmounts = dependencies.instalmentAmounts,
        canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
        paymentPlanAnswers = dependencies.paymentPlanAnswersNoAffordability,
        pegaCaseId = None
      )

    def updateCanSetUpDirectDebitRequest(isAccountHolder: Boolean): CanSetUpDirectDebit =
      CanSetUpDirectDebit(isAccountHolder)

    def journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(
      isAccountHolder: Boolean
    ): Journey.EnteredCanYouSetUpDirectDebit = Journey.EnteredCanYouSetUpDirectDebit(
      _id = dependencies.journeyId,
      origin = Origins.Simp.Pta,
      createdOn = dependencies.createdOn,
      sjRequest = sjRequest,
      sessionId = dependencies.sessionId,
      affordabilityEnabled = Some(false),
      correlationId = dependencies.correlationId,
      taxId = nino,
      eligibilityCheckResult = eligibleEligibilityCheckResultSimp,
      whyCannotPayInFullAnswers = WhyCannotPayInFullAnswers.AnswerNotRequired,
      upfrontPaymentAnswers = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts = dependencies.instalmentAmounts,
      canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
      paymentPlanAnswers = dependencies.paymentPlanAnswersNoAffordability,
      canSetUpDirectDebitAnswer = CanSetUpDirectDebit(isAccountHolder),
      pegaCaseId = None
    )

    def updateDirectDebitDetailsRequest(): BankDetails = dependencies.directDebitDetails

    def journeyAfterEnteredDirectDebitDetailsNoAffordability(): Journey.EnteredDirectDebitDetails =
      Journey.EnteredDirectDebitDetails(
        _id = dependencies.journeyId,
        origin = Origins.Simp.Pta,
        createdOn = dependencies.createdOn,
        sjRequest = sjRequest,
        sessionId = dependencies.sessionId,
        affordabilityEnabled = Some(false),
        correlationId = dependencies.correlationId,
        taxId = nino,
        eligibilityCheckResult = eligibleEligibilityCheckResultSimp,
        whyCannotPayInFullAnswers = WhyCannotPayInFullAnswers.AnswerNotRequired,
        upfrontPaymentAnswers = dependencies.upfrontPaymentAnswersDeclared,
        extremeDatesResponse = dependencies.extremeDatesWithUpfrontPayment,
        instalmentAmounts = dependencies.instalmentAmounts,
        canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
        paymentPlanAnswers = dependencies.paymentPlanAnswersNoAffordability,
        canSetUpDirectDebitAnswer = CanSetUpDirectDebit(isAccountHolder = true),
        directDebitDetails = directDebitDetails,
        pegaCaseId = None
      )

    def updateConfirmedDirectDebitDetailsRequest(): JsNull.type = JsNull

    def journeyAfterConfirmedDirectDebitDetailsNoAffordability: Journey.ConfirmedDirectDebitDetails =
      Journey.ConfirmedDirectDebitDetails(
        _id = dependencies.journeyId,
        origin = Origins.Simp.Pta,
        createdOn = dependencies.createdOn,
        sjRequest = sjRequest,
        sessionId = dependencies.sessionId,
        affordabilityEnabled = Some(false),
        correlationId = dependencies.correlationId,
        taxId = nino,
        eligibilityCheckResult = eligibleEligibilityCheckResultSimp,
        whyCannotPayInFullAnswers = WhyCannotPayInFullAnswers.AnswerNotRequired,
        upfrontPaymentAnswers = dependencies.upfrontPaymentAnswersDeclared,
        extremeDatesResponse = dependencies.extremeDatesWithUpfrontPayment,
        instalmentAmounts = dependencies.instalmentAmounts,
        canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
        paymentPlanAnswers = dependencies.paymentPlanAnswersNoAffordability,
        canSetUpDirectDebitAnswer = CanSetUpDirectDebit(isAccountHolder = true),
        directDebitDetails = directDebitDetails,
        pegaCaseId = None
      )

    def updateAgreedTermsAndConditionsRequest(isEmailAddressRequired: Boolean): IsEmailAddressRequired =
      IsEmailAddressRequired(isEmailAddressRequired)

    def journeyAfterAgreedTermsAndConditionsNoAffordability(
      isEmailAddressRequired: Boolean
    ): Journey.AgreedTermsAndConditions =
      Journey.AgreedTermsAndConditions(
        _id = dependencies.journeyId,
        origin = Origins.Simp.Pta,
        createdOn = dependencies.createdOn,
        sjRequest = sjRequest,
        sessionId = dependencies.sessionId,
        affordabilityEnabled = Some(false),
        correlationId = dependencies.correlationId,
        taxId = nino,
        eligibilityCheckResult = eligibleEligibilityCheckResultSimp,
        whyCannotPayInFullAnswers = WhyCannotPayInFullAnswers.AnswerNotRequired,
        upfrontPaymentAnswers = dependencies.upfrontPaymentAnswersDeclared,
        extremeDatesResponse = dependencies.extremeDatesWithUpfrontPayment,
        instalmentAmounts = dependencies.instalmentAmounts,
        canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
        paymentPlanAnswers = dependencies.paymentPlanAnswersNoAffordability,
        canSetUpDirectDebitAnswer = CanSetUpDirectDebit(isAccountHolder = true),
        directDebitDetails = directDebitDetails,
        isEmailAddressRequired = IsEmailAddressRequired(isEmailAddressRequired),
        pegaCaseId = None
      )

    def updateSelectedEmailRequest(): Email = dependencies.email

    def journeyAfterSelectedEmail: Journey.SelectedEmailToBeVerified = Journey.SelectedEmailToBeVerified(
      _id = dependencies.journeyId,
      origin = Origins.Simp.Pta,
      createdOn = dependencies.createdOn,
      sjRequest = sjRequest,
      sessionId = dependencies.sessionId,
      affordabilityEnabled = Some(false),
      correlationId = dependencies.correlationId,
      taxId = nino,
      eligibilityCheckResult = eligibleEligibilityCheckResultSimp,
      whyCannotPayInFullAnswers = WhyCannotPayInFullAnswers.AnswerNotRequired,
      upfrontPaymentAnswers = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts = dependencies.instalmentAmounts,
      canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
      paymentPlanAnswers = dependencies.paymentPlanAnswersNoAffordability,
      canSetUpDirectDebitAnswer = CanSetUpDirectDebit(isAccountHolder = true),
      directDebitDetails = directDebitDetails,
      isEmailAddressRequired = IsEmailAddressRequired(value = true),
      emailToBeVerified = dependencies.email,
      pegaCaseId = None
    )

    def journeyAfterEmailVerificationResult(result: EmailVerificationResult): Journey.EmailVerificationComplete =
      Journey.EmailVerificationComplete(
        _id = dependencies.journeyId,
        origin = Origins.Simp.Pta,
        createdOn = dependencies.createdOn,
        sjRequest = sjRequest,
        sessionId = dependencies.sessionId,
        affordabilityEnabled = Some(false),
        correlationId = dependencies.correlationId,
        taxId = nino,
        eligibilityCheckResult = eligibleEligibilityCheckResultSimp,
        whyCannotPayInFullAnswers = WhyCannotPayInFullAnswers.AnswerNotRequired,
        upfrontPaymentAnswers = dependencies.upfrontPaymentAnswersDeclared,
        extremeDatesResponse = dependencies.extremeDatesWithUpfrontPayment,
        instalmentAmounts = dependencies.instalmentAmounts,
        canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
        paymentPlanAnswers = dependencies.paymentPlanAnswersNoAffordability,
        canSetUpDirectDebitAnswer = CanSetUpDirectDebit(isAccountHolder = true),
        directDebitDetails = directDebitDetails,
        isEmailAddressRequired = IsEmailAddressRequired(value = true),
        emailToBeVerified = dependencies.email,
        emailVerificationResult = result,
        emailVerificationAnswers = emailVerificationAnswers(Some(result)),
        pegaCaseId = None
      )

    def updateArrangementRequest(): ArrangementResponse = dependencies.arrangementResponseSimp

    def journeyAfterSubmittedArrangementNoAffordability(
      isEmailAddressRequired: Boolean
    ): Journey.SubmittedArrangement = Journey.SubmittedArrangement(
      _id = dependencies.journeyId,
      origin = Origins.Simp.Pta,
      createdOn = dependencies.createdOn,
      sjRequest = sjRequest,
      sessionId = dependencies.sessionId,
      affordabilityEnabled = Some(false),
      correlationId = dependencies.correlationId,
      taxId = nino,
      eligibilityCheckResult = eligibleEligibilityCheckResultSimp,
      whyCannotPayInFullAnswers = WhyCannotPayInFullAnswers.AnswerNotRequired,
      upfrontPaymentAnswers = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts = dependencies.instalmentAmounts,
      canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
      paymentPlanAnswers = dependencies.paymentPlanAnswersNoAffordability,
      canSetUpDirectDebitAnswer = CanSetUpDirectDebit(isAccountHolder = true),
      directDebitDetails = directDebitDetails,
      isEmailAddressRequired = IsEmailAddressRequired(isEmailAddressRequired),
      emailVerificationAnswers = if (isEmailAddressRequired) {
        EmailVerificationAnswers.EmailVerified(dependencies.email, EmailVerificationResult.Verified)
      } else {
        EmailVerificationAnswers.NoEmailJourney
      },
      arrangementResponse = dependencies.arrangementResponseSimp,
      pegaCaseId = None
    )

  }
}
