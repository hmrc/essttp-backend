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

trait TdJourneySimpGovUk {
  dependencies: TdBase with TdSimp =>

  object SimpGovUk extends TdJourneyStructure {

    def sjRequest: Simp.Empty = SjRequest.Simp.Empty()

    def sjResponse: SjResponse = SjResponse(
      nextUrl   = NextUrl(s"http://localhost:9215/set-up-a-payment-plan/simple-assessment-payment-plan"),
      journeyId = dependencies.journeyId
    )

    def postPath: String = "/simp/gov-uk/journey/start"

    def journeyAfterStarted: Journey.Simp.Started = Journey.Simp.Started(
      _id                  = dependencies.journeyId,
      origin               = Origins.Simp.GovUk,
      createdOn            = dependencies.createdOn,
      sjRequest            = sjRequest,
      sessionId            = dependencies.sessionId,
      correlationId        = dependencies.correlationId,
      stage                = Stage.AfterStarted.Started,
      affordabilityEnabled = Some(false),
      pegaCaseId           = None
    )

    def updateTaxIdRequest(): TaxId = nino

    def journeyAfterDetermineTaxIds: Journey.Simp.ComputedTaxId = Journey.Simp.ComputedTaxId(
      _id                  = dependencies.journeyId,
      origin               = Origins.Simp.GovUk,
      createdOn            = dependencies.createdOn,
      sjRequest            = sjRequest,
      sessionId            = dependencies.sessionId,
      correlationId        = dependencies.correlationId,
      stage                = Stage.AfterComputedTaxId.ComputedTaxId,
      affordabilityEnabled = Some(false),
      taxId                = nino,
      pegaCaseId           = None
    )

    def updateEligibilityCheckRequest(): EligibilityCheckResult = eligibleEligibilityCheckResultSimp

    def journeyAfterEligibilityCheckEligible: Journey.Simp.EligibilityChecked = Journey.Simp.EligibilityChecked(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Simp.GovUk,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      correlationId          = dependencies.correlationId,
      stage                  = Stage.AfterEligibilityCheck.Eligible,
      affordabilityEnabled   = Some(false),
      taxId                  = nino,
      eligibilityCheckResult = eligibleEligibilityCheckResultSimp,
      pegaCaseId             = None
    )

    def journeyAfterEligibilityCheckNotEligible: Journey.Simp.EligibilityChecked = Journey.Simp.EligibilityChecked(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Simp.GovUk,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      correlationId          = dependencies.correlationId,
      stage                  = Stage.AfterEligibilityCheck.Ineligible,
      affordabilityEnabled   = Some(false),
      taxId                  = nino,
      eligibilityCheckResult = ineligibleEligibilityCheckResultSimp,
      pegaCaseId             = None
    )

    def journeyAfterWhyCannotPayInFullNotRequired: Journey.Simp.ObtainedWhyCannotPayInFullAnswers = Journey.Simp.ObtainedWhyCannotPayInFullAnswers(
      _id                       = dependencies.journeyId,
      origin                    = Origins.Simp.GovUk,
      createdOn                 = dependencies.createdOn,
      sjRequest                 = sjRequest,
      sessionId                 = dependencies.sessionId,
      stage                     = Stage.AfterWhyCannotPayInFullAnswers.AnswerNotRequired,
      affordabilityEnabled      = Some(false),
      correlationId             = dependencies.correlationId,
      taxId                     = nino,
      eligibilityCheckResult    = eligibleEligibilityCheckResultSimp,
      whyCannotPayInFullAnswers = WhyCannotPayInFullAnswers.AnswerNotRequired,
      pegaCaseId                = None
    )

    def updateCanPayUpfrontYesRequest(): CanPayUpfront = canPayUpfrontYes

    def updateCanPayUpfrontNoRequest(): CanPayUpfront = canPayUpfrontNo

    def journeyAfterCanPayUpfrontYes: Journey.Simp.AnsweredCanPayUpfront = Journey.Simp.AnsweredCanPayUpfront(
      _id                       = dependencies.journeyId,
      origin                    = Origins.Simp.GovUk,
      createdOn                 = dependencies.createdOn,
      sjRequest                 = sjRequest,
      sessionId                 = dependencies.sessionId,
      correlationId             = dependencies.correlationId,
      stage                     = Stage.AfterCanPayUpfront.Yes,
      affordabilityEnabled      = Some(false),
      taxId                     = nino,
      eligibilityCheckResult    = eligibleEligibilityCheckResultSimp,
      whyCannotPayInFullAnswers = WhyCannotPayInFullAnswers.AnswerNotRequired,
      canPayUpfront             = canPayUpfrontYes,
      pegaCaseId                = None
    )

    def journeyAfterCanPayUpfrontNo: Journey.Simp.AnsweredCanPayUpfront = Journey.Simp.AnsweredCanPayUpfront(
      _id                       = dependencies.journeyId,
      origin                    = Origins.Simp.GovUk,
      createdOn                 = dependencies.createdOn,
      sjRequest                 = sjRequest,
      sessionId                 = dependencies.sessionId,
      correlationId             = dependencies.correlationId,
      stage                     = Stage.AfterCanPayUpfront.No,
      affordabilityEnabled      = Some(false),
      taxId                     = nino,
      eligibilityCheckResult    = eligibleEligibilityCheckResultSimp,
      whyCannotPayInFullAnswers = WhyCannotPayInFullAnswers.AnswerNotRequired,
      canPayUpfront             = canPayUpfrontNo,
      pegaCaseId                = None
    )

    override def updateUpfrontPaymentAmountRequest(): UpfrontPaymentAmount = dependencies.upfrontPaymentAmount

    override def journeyAfterUpfrontPaymentAmount: Journey.Simp.EnteredUpfrontPaymentAmount = Journey.Simp.EnteredUpfrontPaymentAmount(
      _id                       = dependencies.journeyId,
      origin                    = Origins.Simp.GovUk,
      createdOn                 = dependencies.createdOn,
      sjRequest                 = sjRequest,
      sessionId                 = dependencies.sessionId,
      correlationId             = dependencies.correlationId,
      stage                     = Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount,
      affordabilityEnabled      = Some(false),
      taxId                     = nino,
      eligibilityCheckResult    = eligibleEligibilityCheckResultSimp,
      whyCannotPayInFullAnswers = WhyCannotPayInFullAnswers.AnswerNotRequired,
      canPayUpfront             = canPayUpfrontYes,
      upfrontPaymentAmount      = dependencies.upfrontPaymentAmount,
      pegaCaseId                = None
    )

    def updateExtremeDatesRequest(): ExtremeDatesResponse = dependencies.extremeDatesWithUpfrontPayment

    def journeyAfterExtremeDates: Journey.Simp.RetrievedExtremeDates = Journey.Simp.RetrievedExtremeDates(
      _id                       = dependencies.journeyId,
      origin                    = Origins.Simp.GovUk,
      createdOn                 = dependencies.createdOn,
      sjRequest                 = sjRequest,
      sessionId                 = dependencies.sessionId,
      correlationId             = dependencies.correlationId,
      stage                     = Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved,
      affordabilityEnabled      = Some(false),
      taxId                     = nino,
      eligibilityCheckResult    = eligibleEligibilityCheckResultSimp,
      whyCannotPayInFullAnswers = WhyCannotPayInFullAnswers.AnswerNotRequired,
      upfrontPaymentAnswers     = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse      = dependencies.extremeDatesWithUpfrontPayment,
      pegaCaseId                = None
    )

    def updateInstalmentAmountsRequest(): InstalmentAmounts = dependencies.instalmentAmounts

    def journeyAfterInstalmentAmounts: Journey.Simp.RetrievedAffordabilityResult = Journey.Simp.RetrievedAffordabilityResult(
      _id                       = dependencies.journeyId,
      origin                    = Origins.Simp.GovUk,
      createdOn                 = dependencies.createdOn,
      sjRequest                 = sjRequest,
      sessionId                 = dependencies.sessionId,
      correlationId             = dependencies.correlationId,
      stage                     = Stage.AfterAffordabilityResult.RetrievedAffordabilityResult,
      affordabilityEnabled      = Some(false),
      taxId                     = nino,
      eligibilityCheckResult    = eligibleEligibilityCheckResultSimp,
      whyCannotPayInFullAnswers = WhyCannotPayInFullAnswers.AnswerNotRequired,
      upfrontPaymentAnswers     = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse      = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts         = dependencies.instalmentAmounts,
      pegaCaseId                = None
    )

    def journeyAfterCanPayWithinSixMonths: Journey.Simp.ObtainedCanPayWithinSixMonthsAnswers = Journey.Simp.ObtainedCanPayWithinSixMonthsAnswers(
      _id                          = dependencies.journeyId,
      origin                       = Origins.Simp.GovUk,
      createdOn                    = dependencies.createdOn,
      sjRequest                    = sjRequest,
      sessionId                    = dependencies.sessionId,
      stage                        = Stage.AfterCanPayWithinSixMonthsAnswers.AnswerNotRequired,
      affordabilityEnabled         = Some(false),
      correlationId                = dependencies.correlationId,
      taxId                        = nino,
      eligibilityCheckResult       = eligibleEligibilityCheckResultSimp,
      whyCannotPayInFullAnswers    = WhyCannotPayInFullAnswers.AnswerNotRequired,
      upfrontPaymentAnswers        = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse         = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts            = dependencies.instalmentAmounts,
      canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
      pegaCaseId                   = None
    )

    def updateMonthlyPaymentAmountRequest(): MonthlyPaymentAmount = dependencies.monthlyPaymentAmount

    def journeyAfterMonthlyPaymentAmount: Journey.Simp.EnteredMonthlyPaymentAmount = Journey.Simp.EnteredMonthlyPaymentAmount(
      _id                          = dependencies.journeyId,
      origin                       = Origins.Simp.GovUk,
      createdOn                    = dependencies.createdOn,
      sjRequest                    = sjRequest,
      sessionId                    = dependencies.sessionId,
      correlationId                = dependencies.correlationId,
      stage                        = Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount,
      affordabilityEnabled         = Some(false),
      taxId                        = nino,
      eligibilityCheckResult       = eligibleEligibilityCheckResultSimp,
      whyCannotPayInFullAnswers    = WhyCannotPayInFullAnswers.AnswerNotRequired,
      upfrontPaymentAnswers        = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse         = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts            = dependencies.instalmentAmounts,
      canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
      monthlyPaymentAmount         = dependencies.monthlyPaymentAmount,
      pegaCaseId                   = None
    )

    def updateDayOfMonthRequest(): DayOfMonth = dependencies.dayOfMonth

    def journeyAfterDayOfMonth: Journey.Simp.EnteredDayOfMonth = Journey.Simp.EnteredDayOfMonth(
      _id                          = dependencies.journeyId,
      origin                       = Origins.Simp.GovUk,
      createdOn                    = dependencies.createdOn,
      sjRequest                    = sjRequest,
      sessionId                    = dependencies.sessionId,
      correlationId                = dependencies.correlationId,
      stage                        = Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth,
      affordabilityEnabled         = Some(false),
      taxId                        = nino,
      eligibilityCheckResult       = eligibleEligibilityCheckResultSimp,
      whyCannotPayInFullAnswers    = WhyCannotPayInFullAnswers.AnswerNotRequired,
      upfrontPaymentAnswers        = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse         = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts            = dependencies.instalmentAmounts,
      canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
      monthlyPaymentAmount         = dependencies.monthlyPaymentAmount,
      dayOfMonth                   = dependencies.dayOfMonth,
      pegaCaseId                   = None
    )

    def updateStartDatesResponse(): StartDatesResponse = dependencies.startDatesResponseWithInitialPayment

    def journeyAfterStartDatesResponse: Journey.AfterStartDatesResponse = Journey.Simp.RetrievedStartDates(
      _id                          = dependencies.journeyId,
      origin                       = Origins.Simp.GovUk,
      createdOn                    = dependencies.createdOn,
      sjRequest                    = sjRequest,
      sessionId                    = dependencies.sessionId,
      correlationId                = dependencies.correlationId,
      stage                        = Stage.AfterStartDatesResponse.StartDatesResponseRetrieved,
      affordabilityEnabled         = Some(false),
      taxId                        = nino,
      eligibilityCheckResult       = eligibleEligibilityCheckResultSimp,
      whyCannotPayInFullAnswers    = WhyCannotPayInFullAnswers.AnswerNotRequired,
      upfrontPaymentAnswers        = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse         = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts            = dependencies.instalmentAmounts,
      canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
      monthlyPaymentAmount         = dependencies.monthlyPaymentAmount,
      dayOfMonth                   = dependencies.dayOfMonth,
      startDatesResponse           = dependencies.startDatesResponseWithInitialPayment,
      pegaCaseId                   = None
    )

    def updateAffordableQuotesResponse(): AffordableQuotesResponse = dependencies.affordableQuotesResponse

    def journeyAfterAffordableQuotesResponse: Journey.AfterAffordableQuotesResponse = Journey.Simp.RetrievedAffordableQuotes(
      _id                          = dependencies.journeyId,
      origin                       = Origins.Simp.GovUk,
      createdOn                    = dependencies.createdOn,
      sjRequest                    = sjRequest,
      sessionId                    = dependencies.sessionId,
      correlationId                = dependencies.correlationId,
      stage                        = Stage.AfterAffordableQuotesResponse.AffordableQuotesRetrieved,
      affordabilityEnabled         = Some(false),
      taxId                        = nino,
      eligibilityCheckResult       = eligibleEligibilityCheckResultSimp,
      whyCannotPayInFullAnswers    = WhyCannotPayInFullAnswers.AnswerNotRequired,
      upfrontPaymentAnswers        = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse         = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts            = dependencies.instalmentAmounts,
      canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
      monthlyPaymentAmount         = dependencies.monthlyPaymentAmount,
      dayOfMonth                   = dependencies.dayOfMonth,
      startDatesResponse           = dependencies.startDatesResponseWithInitialPayment,
      affordableQuotesResponse     = dependencies.affordableQuotesResponse,
      pegaCaseId                   = None
    )

    def updateSelectedPaymentPlanRequest(): PaymentPlan = dependencies.paymentPlan(1)

    def journeyAfterSelectedPaymentPlan: Journey.AfterSelectedPaymentPlan = Journey.Simp.ChosenPaymentPlan(
      _id                          = dependencies.journeyId,
      origin                       = Origins.Simp.GovUk,
      createdOn                    = dependencies.createdOn,
      sjRequest                    = sjRequest,
      sessionId                    = dependencies.sessionId,
      correlationId                = dependencies.correlationId,
      stage                        = Stage.AfterSelectedPlan.SelectedPlan,
      affordabilityEnabled         = Some(false),
      taxId                        = nino,
      eligibilityCheckResult       = eligibleEligibilityCheckResultSimp,
      whyCannotPayInFullAnswers    = WhyCannotPayInFullAnswers.AnswerNotRequired,
      upfrontPaymentAnswers        = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse         = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts            = dependencies.instalmentAmounts,
      canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
      monthlyPaymentAmount         = dependencies.monthlyPaymentAmount,
      dayOfMonth                   = dependencies.dayOfMonth,
      startDatesResponse           = dependencies.startDatesResponseWithInitialPayment,
      affordableQuotesResponse     = dependencies.affordableQuotesResponse,
      selectedPaymentPlan          = dependencies.paymentPlan(1),
      pegaCaseId                   = None
    )

    def updateCheckedPaymentPlanRequest(): JsNull.type = JsNull

    def journeyAfterCheckedPaymentPlanNonAffordability: Journey.AfterCheckedPaymentPlan = Journey.Simp.CheckedPaymentPlan(
      _id                          = dependencies.journeyId,
      origin                       = Origins.Simp.GovUk,
      createdOn                    = dependencies.createdOn,
      sjRequest                    = sjRequest,
      sessionId                    = dependencies.sessionId,
      correlationId                = dependencies.correlationId,
      stage                        = Stage.AfterCheckedPlan.AcceptedPlan,
      affordabilityEnabled         = Some(false),
      taxId                        = nino,
      eligibilityCheckResult       = eligibleEligibilityCheckResultSimp,
      whyCannotPayInFullAnswers    = WhyCannotPayInFullAnswers.AnswerNotRequired,
      upfrontPaymentAnswers        = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse         = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts            = dependencies.instalmentAmounts,
      canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
      paymentPlanAnswers           = dependencies.paymentPlanAnswersNoAffordability,
      pegaCaseId                   = None
    )

    def updateCanSetUpDirectDebitRequest(isAccountHolder: Boolean): CanSetUpDirectDebit =
      CanSetUpDirectDebit(isAccountHolder)

    def journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder: Boolean): Journey.AfterEnteredCanYouSetUpDirectDebit = Journey.Simp.EnteredCanYouSetUpDirectDebit(
      _id                          = dependencies.journeyId,
      origin                       = Origins.Simp.GovUk,
      createdOn                    = dependencies.createdOn,
      sjRequest                    = sjRequest,
      sessionId                    = dependencies.sessionId,
      correlationId                = dependencies.correlationId,
      stage                        = if (isAccountHolder) Stage.AfterEnteredCanYouSetUpDirectDebit.CanSetUpDirectDebit else Stage.AfterEnteredCanYouSetUpDirectDebit.CannotSetUpDirectDebit,
      affordabilityEnabled         = Some(false),
      taxId                        = nino,
      eligibilityCheckResult       = eligibleEligibilityCheckResultSimp,
      whyCannotPayInFullAnswers    = WhyCannotPayInFullAnswers.AnswerNotRequired,
      upfrontPaymentAnswers        = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse         = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts            = dependencies.instalmentAmounts,
      canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
      paymentPlanAnswers           = dependencies.paymentPlanAnswersNoAffordability,
      canSetUpDirectDebitAnswer    = CanSetUpDirectDebit(isAccountHolder),
      pegaCaseId                   = None
    )

    def updateDirectDebitDetailsRequest(): BankDetails = dependencies.directDebitDetails

    def journeyAfterEnteredDirectDebitDetailsNoAffordability(): Journey.AfterEnteredDirectDebitDetails = Journey.Simp.EnteredDirectDebitDetails(
      _id                          = dependencies.journeyId,
      origin                       = Origins.Simp.GovUk,
      createdOn                    = dependencies.createdOn,
      sjRequest                    = sjRequest,
      sessionId                    = dependencies.sessionId,
      correlationId                = dependencies.correlationId,
      stage                        = Stage.AfterEnteredDirectDebitDetails.EnteredDirectDebitDetails,
      affordabilityEnabled         = Some(false),
      taxId                        = nino,
      eligibilityCheckResult       = eligibleEligibilityCheckResultSimp,
      whyCannotPayInFullAnswers    = WhyCannotPayInFullAnswers.AnswerNotRequired,
      upfrontPaymentAnswers        = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse         = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts            = dependencies.instalmentAmounts,
      canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
      paymentPlanAnswers           = dependencies.paymentPlanAnswersNoAffordability,
      canSetUpDirectDebitAnswer    = CanSetUpDirectDebit(isAccountHolder = true),
      directDebitDetails           = directDebitDetails,
      pegaCaseId                   = None
    )

    override def updateConfirmedDirectDebitDetailsRequest(): JsNull.type = JsNull

    override def journeyAfterConfirmedDirectDebitDetailsNoAffordability: Journey.AfterConfirmedDirectDebitDetails = Journey.Simp.ConfirmedDirectDebitDetails(
      _id                          = dependencies.journeyId,
      origin                       = Origins.Simp.GovUk,
      createdOn                    = dependencies.createdOn,
      sjRequest                    = sjRequest,
      sessionId                    = dependencies.sessionId,
      correlationId                = dependencies.correlationId,
      stage                        = Stage.AfterConfirmedDirectDebitDetails.ConfirmedDetails,
      affordabilityEnabled         = Some(false),
      taxId                        = nino,
      eligibilityCheckResult       = eligibleEligibilityCheckResultSimp,
      whyCannotPayInFullAnswers    = WhyCannotPayInFullAnswers.AnswerNotRequired,
      upfrontPaymentAnswers        = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse         = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts            = dependencies.instalmentAmounts,
      canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
      paymentPlanAnswers           = dependencies.paymentPlanAnswersNoAffordability,
      canSetUpDirectDebitAnswer    = CanSetUpDirectDebit(isAccountHolder = true),
      directDebitDetails           = directDebitDetails,
      pegaCaseId                   = None
    )

    def updateAgreedTermsAndConditionsRequest(isEmailAddressRequired: Boolean): IsEmailAddressRequired = IsEmailAddressRequired(isEmailAddressRequired)

    def journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired: Boolean): Journey.AfterAgreedTermsAndConditions = {
      val stage =
        if (isEmailAddressRequired) Stage.AfterAgreedTermsAndConditions.EmailAddressRequired
        else Stage.AfterAgreedTermsAndConditions.EmailAddressNotRequired

      Journey.Simp.AgreedTermsAndConditions(
        _id                          = dependencies.journeyId,
        origin                       = Origins.Simp.GovUk,
        createdOn                    = dependencies.createdOn,
        sjRequest                    = sjRequest,
        sessionId                    = dependencies.sessionId,
        correlationId                = dependencies.correlationId,
        stage                        = stage,
        affordabilityEnabled         = Some(false),
        taxId                        = nino,
        eligibilityCheckResult       = eligibleEligibilityCheckResultSimp,
        whyCannotPayInFullAnswers    = WhyCannotPayInFullAnswers.AnswerNotRequired,
        upfrontPaymentAnswers        = dependencies.upfrontPaymentAnswersDeclared,
        extremeDatesResponse         = dependencies.extremeDatesWithUpfrontPayment,
        instalmentAmounts            = dependencies.instalmentAmounts,
        canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
        paymentPlanAnswers           = dependencies.paymentPlanAnswersNoAffordability,
        canSetUpDirectDebitAnswer    = CanSetUpDirectDebit(isAccountHolder = true),
        directDebitDetails           = directDebitDetails,
        isEmailAddressRequired       = IsEmailAddressRequired(isEmailAddressRequired),
        pegaCaseId                   = None
      )
    }

    def updateSelectedEmailRequest(): Email = dependencies.email

    def journeyAfterSelectedEmail: Journey.Simp.SelectedEmailToBeVerified = Journey.Simp.SelectedEmailToBeVerified(
      _id                          = dependencies.journeyId,
      origin                       = Origins.Simp.GovUk,
      createdOn                    = dependencies.createdOn,
      sjRequest                    = sjRequest,
      sessionId                    = dependencies.sessionId,
      stage                        = Stage.AfterSelectedAnEmailToBeVerified.EmailChosen,
      affordabilityEnabled         = Some(false),
      correlationId                = dependencies.correlationId,
      taxId                        = nino,
      eligibilityCheckResult       = eligibleEligibilityCheckResultSimp,
      whyCannotPayInFullAnswers    = WhyCannotPayInFullAnswers.AnswerNotRequired,
      upfrontPaymentAnswers        = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse         = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts            = dependencies.instalmentAmounts,
      canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
      paymentPlanAnswers           = dependencies.paymentPlanAnswersNoAffordability,
      canSetUpDirectDebitAnswer    = CanSetUpDirectDebit(isAccountHolder = true),
      directDebitDetails           = directDebitDetails,
      isEmailAddressRequired       = IsEmailAddressRequired(value = true),
      emailToBeVerified            = dependencies.email,
      pegaCaseId                   = None
    )

    def journeyAfterEmailVerificationResult(result: EmailVerificationResult): Journey.Simp.EmailVerificationComplete = Journey.Simp.EmailVerificationComplete(
      _id                          = dependencies.journeyId,
      origin                       = Origins.Simp.GovUk,
      createdOn                    = dependencies.createdOn,
      sjRequest                    = sjRequest,
      sessionId                    = dependencies.sessionId,
      stage                        = result match {
        case EmailVerificationResult.Verified => Stage.AfterEmailVerificationPhase.VerificationSuccess
        case EmailVerificationResult.Locked   => Stage.AfterEmailVerificationPhase.Locked
      },
      affordabilityEnabled         = Some(false),
      correlationId                = dependencies.correlationId,
      taxId                        = nino,
      eligibilityCheckResult       = eligibleEligibilityCheckResultSimp,
      whyCannotPayInFullAnswers    = WhyCannotPayInFullAnswers.AnswerNotRequired,
      upfrontPaymentAnswers        = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse         = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts            = dependencies.instalmentAmounts,
      canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
      paymentPlanAnswers           = dependencies.paymentPlanAnswersNoAffordability,
      canSetUpDirectDebitAnswer    = CanSetUpDirectDebit(isAccountHolder = true),
      directDebitDetails           = directDebitDetails,
      isEmailAddressRequired       = IsEmailAddressRequired(value = true),
      emailToBeVerified            = dependencies.email,
      emailVerificationResult      = result,
      emailVerificationAnswers     = emailVerificationAnswers(Some(result)),
      pegaCaseId                   = None
    )

    def updateArrangementRequest(): ArrangementResponse = dependencies.arrangementResponseSimp

    def journeyAfterSubmittedArrangementNoAffordability(isEmailAddressRequired: Boolean): Journey.AfterArrangementSubmitted = Journey.Simp.SubmittedArrangement(
      _id                          = dependencies.journeyId,
      origin                       = Origins.Simp.GovUk,
      createdOn                    = dependencies.createdOn,
      sjRequest                    = sjRequest,
      sessionId                    = dependencies.sessionId,
      correlationId                = dependencies.correlationId,
      stage                        = Stage.AfterSubmittedArrangement.Submitted,
      affordabilityEnabled         = Some(false),
      taxId                        = nino,
      eligibilityCheckResult       = eligibleEligibilityCheckResultSimp,
      whyCannotPayInFullAnswers    = WhyCannotPayInFullAnswers.AnswerNotRequired,
      upfrontPaymentAnswers        = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse         = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts            = dependencies.instalmentAmounts,
      canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
      paymentPlanAnswers           = dependencies.paymentPlanAnswersNoAffordability,
      canSetUpDirectDebitAnswer    = CanSetUpDirectDebit(isAccountHolder = true),
      directDebitDetails           = directDebitDetails,
      isEmailAddressRequired       = IsEmailAddressRequired(isEmailAddressRequired),
      emailVerificationAnswers     = if (isEmailAddressRequired) {
        EmailVerificationAnswers.EmailVerified(dependencies.email, EmailVerificationResult.Verified)
      } else {
        EmailVerificationAnswers.NoEmailJourney
      },
      arrangementResponse          = dependencies.arrangementResponseSimp,
      pegaCaseId                   = None
    )

  }
}
