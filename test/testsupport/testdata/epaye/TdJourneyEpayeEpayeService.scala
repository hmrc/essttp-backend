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

package testsupport.testdata.epaye

import essttp.journey.model.SjRequest.Epaye
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

trait TdJourneyEpayeEpayeService {
  dependencies: TdBase with TdEpaye =>

  object EpayeEpayeService extends TdJourneyStructure {

    def sjRequest: Epaye.Simple = SjRequest.Epaye.Simple(
      dependencies.returnUrl,
      dependencies.backUrl
    )

    def sjResponse: SjResponse = SjResponse(
      nextUrl   = NextUrl(s"http://localhost:9215/set-up-a-payment-plan/epaye-payment-plan"),
      journeyId = dependencies.journeyId
    )

    def postPath: String = "/epaye/epaye-service/journey/start"

    def journeyAfterStarted: Journey.Epaye.Started = Journey.Epaye.Started(
      _id                  = dependencies.journeyId,
      origin               = Origins.Epaye.EpayeService,
      createdOn            = dependencies.createdOn,
      sjRequest            = sjRequest,
      sessionId            = dependencies.sessionId,
      stage                = Stage.AfterStarted.Started,
      affordabilityEnabled = Some(false),
      correlationId        = dependencies.correlationId,
      pegaCaseId           = None
    )

    def updateTaxIdRequest(): TaxId = empRef

    def journeyAfterDetermineTaxIds: Journey.Epaye.ComputedTaxId = Journey.Epaye.ComputedTaxId(
      _id                  = dependencies.journeyId,
      origin               = Origins.Epaye.EpayeService,
      createdOn            = dependencies.createdOn,
      sjRequest            = sjRequest,
      sessionId            = dependencies.sessionId,
      stage                = Stage.AfterComputedTaxId.ComputedTaxId,
      affordabilityEnabled = Some(false),
      correlationId        = dependencies.correlationId,
      taxId                = empRef,
      pegaCaseId           = None
    )

    def updateEligibilityCheckRequest(): EligibilityCheckResult = eligibleEligibilityCheckResultEpaye

    def journeyAfterEligibilityCheckEligible: Journey.Epaye.EligibilityChecked = Journey.Epaye.EligibilityChecked(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.EpayeService,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterEligibilityCheck.Eligible,
      affordabilityEnabled   = Some(false),
      correlationId          = dependencies.correlationId,
      taxId                  = empRef,
      eligibilityCheckResult = eligibleEligibilityCheckResultEpaye,
      pegaCaseId             = None
    )

    def journeyAfterEligibilityCheckNotEligible: Journey.Epaye.EligibilityChecked = Journey.Epaye.EligibilityChecked(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.EpayeService,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterEligibilityCheck.Ineligible,
      affordabilityEnabled   = Some(false),
      correlationId          = dependencies.correlationId,
      taxId                  = empRef,
      eligibilityCheckResult = ineligibleEligibilityCheckResultEpaye,
      pegaCaseId             = None
    )
    def journeyAfterWhyCannotPayInFullNotRequired: Journey.Epaye.ObtainedWhyCannotPayInFullAnswers = Journey.Epaye.ObtainedWhyCannotPayInFullAnswers(
      _id                       = dependencies.journeyId,
      origin                    = Origins.Epaye.EpayeService,
      createdOn                 = dependencies.createdOn,
      sjRequest                 = sjRequest,
      sessionId                 = dependencies.sessionId,
      stage                     = Stage.AfterWhyCannotPayInFullAnswers.AnswerNotRequired,
      affordabilityEnabled      = Some(false),
      correlationId             = dependencies.correlationId,
      taxId                     = empRef,
      eligibilityCheckResult    = eligibleEligibilityCheckResultEpaye,
      whyCannotPayInFullAnswers = WhyCannotPayInFullAnswers.AnswerNotRequired,
      pegaCaseId                = None
    )

    def updateCanPayUpfrontYesRequest(): CanPayUpfront = canPayUpfrontYes

    def updateCanPayUpfrontNoRequest(): CanPayUpfront = canPayUpfrontNo

    def journeyAfterCanPayUpfrontYes: Journey.Epaye.AnsweredCanPayUpfront = Journey.Epaye.AnsweredCanPayUpfront(
      _id                       = dependencies.journeyId,
      origin                    = Origins.Epaye.EpayeService,
      createdOn                 = dependencies.createdOn,
      sjRequest                 = sjRequest,
      sessionId                 = dependencies.sessionId,
      stage                     = Stage.AfterCanPayUpfront.Yes,
      affordabilityEnabled      = Some(false),
      correlationId             = dependencies.correlationId,
      taxId                     = empRef,
      eligibilityCheckResult    = eligibleEligibilityCheckResultEpaye,
      whyCannotPayInFullAnswers = WhyCannotPayInFullAnswers.AnswerNotRequired,
      canPayUpfront             = canPayUpfrontYes,
      pegaCaseId                = None
    )

    def journeyAfterCanPayUpfrontNo: Journey.Epaye.AnsweredCanPayUpfront = Journey.Epaye.AnsweredCanPayUpfront(
      _id                       = dependencies.journeyId,
      origin                    = Origins.Epaye.EpayeService,
      createdOn                 = dependencies.createdOn,
      sjRequest                 = sjRequest,
      sessionId                 = dependencies.sessionId,
      stage                     = Stage.AfterCanPayUpfront.No,
      affordabilityEnabled      = Some(false),
      correlationId             = dependencies.correlationId,
      taxId                     = empRef,
      eligibilityCheckResult    = eligibleEligibilityCheckResultEpaye,
      whyCannotPayInFullAnswers = WhyCannotPayInFullAnswers.AnswerNotRequired,
      canPayUpfront             = canPayUpfrontNo,
      pegaCaseId                = None
    )

    def updateUpfrontPaymentAmountRequest(): UpfrontPaymentAmount = dependencies.upfrontPaymentAmount

    def journeyAfterUpfrontPaymentAmount: Journey.Epaye.EnteredUpfrontPaymentAmount = Journey.Epaye.EnteredUpfrontPaymentAmount(
      _id                       = dependencies.journeyId,
      origin                    = Origins.Epaye.EpayeService,
      createdOn                 = dependencies.createdOn,
      sjRequest                 = sjRequest,
      sessionId                 = dependencies.sessionId,
      stage                     = Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount,
      affordabilityEnabled      = Some(false),
      correlationId             = dependencies.correlationId,
      taxId                     = empRef,
      eligibilityCheckResult    = eligibleEligibilityCheckResultEpaye,
      whyCannotPayInFullAnswers = WhyCannotPayInFullAnswers.AnswerNotRequired,
      canPayUpfront             = canPayUpfrontYes,
      upfrontPaymentAmount      = dependencies.upfrontPaymentAmount,
      pegaCaseId                = None
    )

    def updateExtremeDatesRequest(): ExtremeDatesResponse = dependencies.extremeDatesWithUpfrontPayment

    def journeyAfterExtremeDates: Journey.Epaye.RetrievedExtremeDates = Journey.Epaye.RetrievedExtremeDates(
      _id                       = dependencies.journeyId,
      origin                    = Origins.Epaye.EpayeService,
      createdOn                 = dependencies.createdOn,
      sjRequest                 = sjRequest,
      sessionId                 = dependencies.sessionId,
      stage                     = Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved,
      affordabilityEnabled      = Some(false),
      correlationId             = dependencies.correlationId,
      taxId                     = empRef,
      eligibilityCheckResult    = eligibleEligibilityCheckResultEpaye,
      whyCannotPayInFullAnswers = WhyCannotPayInFullAnswers.AnswerNotRequired,
      upfrontPaymentAnswers     = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse      = dependencies.extremeDatesWithUpfrontPayment,
      pegaCaseId                = None
    )

    def updateInstalmentAmountsRequest(): InstalmentAmounts = dependencies.instalmentAmounts

    def journeyAfterInstalmentAmounts: Journey.Epaye.RetrievedAffordabilityResult = Journey.Epaye.RetrievedAffordabilityResult(
      _id                       = dependencies.journeyId,
      origin                    = Origins.Epaye.EpayeService,
      createdOn                 = dependencies.createdOn,
      sjRequest                 = sjRequest,
      sessionId                 = dependencies.sessionId,
      stage                     = Stage.AfterAffordabilityResult.RetrievedAffordabilityResult,
      affordabilityEnabled      = Some(false),
      correlationId             = dependencies.correlationId,
      taxId                     = empRef,
      eligibilityCheckResult    = eligibleEligibilityCheckResultEpaye,
      whyCannotPayInFullAnswers = WhyCannotPayInFullAnswers.AnswerNotRequired,
      upfrontPaymentAnswers     = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse      = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts         = dependencies.instalmentAmounts,
      pegaCaseId                = None
    )

    def journeyAfterCanPayWithinSixMonths: Journey.Epaye.ObtainedCanPayWithinSixMonthsAnswers = Journey.Epaye.ObtainedCanPayWithinSixMonthsAnswers(
      _id                          = dependencies.journeyId,
      origin                       = Origins.Epaye.EpayeService,
      createdOn                    = dependencies.createdOn,
      sjRequest                    = sjRequest,
      sessionId                    = dependencies.sessionId,
      stage                        = Stage.AfterCanPayWithinSixMonthsAnswers.AnswerNotRequired,
      affordabilityEnabled         = Some(false),
      correlationId                = dependencies.correlationId,
      taxId                        = empRef,
      eligibilityCheckResult       = eligibleEligibilityCheckResultEpaye,
      whyCannotPayInFullAnswers    = WhyCannotPayInFullAnswers.AnswerNotRequired,
      upfrontPaymentAnswers        = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse         = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts            = dependencies.instalmentAmounts,
      canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
      pegaCaseId                   = None
    )

    def updateMonthlyPaymentAmountRequest(): MonthlyPaymentAmount = dependencies.monthlyPaymentAmount

    def journeyAfterMonthlyPaymentAmount: Journey.Epaye.EnteredMonthlyPaymentAmount = Journey.Epaye.EnteredMonthlyPaymentAmount(
      _id                          = dependencies.journeyId,
      origin                       = Origins.Epaye.EpayeService,
      createdOn                    = dependencies.createdOn,
      sjRequest                    = sjRequest,
      sessionId                    = dependencies.sessionId,
      stage                        = Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount,
      affordabilityEnabled         = Some(false),
      correlationId                = dependencies.correlationId,
      taxId                        = empRef,
      eligibilityCheckResult       = eligibleEligibilityCheckResultEpaye,
      whyCannotPayInFullAnswers    = WhyCannotPayInFullAnswers.AnswerNotRequired,
      upfrontPaymentAnswers        = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse         = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts            = dependencies.instalmentAmounts,
      canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
      monthlyPaymentAmount         = dependencies.monthlyPaymentAmount,
      pegaCaseId                   = None
    )

    def updateDayOfMonthRequest(): DayOfMonth = dependencies.dayOfMonth

    def journeyAfterDayOfMonth: Journey.Epaye.EnteredDayOfMonth = Journey.Epaye.EnteredDayOfMonth(
      _id                          = dependencies.journeyId,
      origin                       = Origins.Epaye.EpayeService,
      createdOn                    = dependencies.createdOn,
      sjRequest                    = sjRequest,
      sessionId                    = dependencies.sessionId,
      stage                        = Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth,
      affordabilityEnabled         = Some(false),
      correlationId                = dependencies.correlationId,
      taxId                        = empRef,
      eligibilityCheckResult       = eligibleEligibilityCheckResultEpaye,
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

    def journeyAfterStartDatesResponse: Journey.AfterStartDatesResponse = Journey.Epaye.RetrievedStartDates(
      _id                          = dependencies.journeyId,
      origin                       = Origins.Epaye.EpayeService,
      createdOn                    = dependencies.createdOn,
      sjRequest                    = sjRequest,
      sessionId                    = dependencies.sessionId,
      stage                        = Stage.AfterStartDatesResponse.StartDatesResponseRetrieved,
      affordabilityEnabled         = Some(false),
      correlationId                = dependencies.correlationId,
      taxId                        = empRef,
      eligibilityCheckResult       = eligibleEligibilityCheckResultEpaye,
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

    def journeyAfterAffordableQuotesResponse: Journey.AfterAffordableQuotesResponse = Journey.Epaye.RetrievedAffordableQuotes(
      _id                          = dependencies.journeyId,
      origin                       = Origins.Epaye.EpayeService,
      createdOn                    = dependencies.createdOn,
      sjRequest                    = sjRequest,
      sessionId                    = dependencies.sessionId,
      stage                        = Stage.AfterAffordableQuotesResponse.AffordableQuotesRetrieved,
      affordabilityEnabled         = Some(false),
      correlationId                = dependencies.correlationId,
      taxId                        = empRef,
      eligibilityCheckResult       = eligibleEligibilityCheckResultEpaye,
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

    def journeyAfterSelectedPaymentPlan: Journey.AfterSelectedPaymentPlan = Journey.Epaye.ChosenPaymentPlan(
      _id                          = dependencies.journeyId,
      origin                       = Origins.Epaye.EpayeService,
      createdOn                    = dependencies.createdOn,
      sjRequest                    = sjRequest,
      sessionId                    = dependencies.sessionId,
      stage                        = Stage.AfterSelectedPlan.SelectedPlan,
      affordabilityEnabled         = Some(false),
      correlationId                = dependencies.correlationId,
      taxId                        = empRef,
      eligibilityCheckResult       = eligibleEligibilityCheckResultEpaye,
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

    def journeyAfterCheckedPaymentPlanNonAffordability: Journey.AfterCheckedPaymentPlan = Journey.Epaye.CheckedPaymentPlan(
      _id                          = dependencies.journeyId,
      origin                       = Origins.Epaye.EpayeService,
      createdOn                    = dependencies.createdOn,
      sjRequest                    = sjRequest,
      sessionId                    = dependencies.sessionId,
      stage                        = Stage.AfterCheckedPlan.AcceptedPlan,
      affordabilityEnabled         = Some(false),
      correlationId                = dependencies.correlationId,
      taxId                        = empRef,
      eligibilityCheckResult       = eligibleEligibilityCheckResultEpaye,
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

    def journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder: Boolean): Journey.AfterEnteredCanYouSetUpDirectDebit = Journey.Epaye.EnteredCanYouSetUpDirectDebit(
      _id                          = dependencies.journeyId,
      origin                       = Origins.Epaye.EpayeService,
      createdOn                    = dependencies.createdOn,
      sjRequest                    = sjRequest,
      sessionId                    = dependencies.sessionId,
      stage                        = if (isAccountHolder) Stage.AfterEnteredCanYouSetUpDirectDebit.CanSetUpDirectDebit else Stage.AfterEnteredCanYouSetUpDirectDebit.CannotSetUpDirectDebit,
      affordabilityEnabled         = Some(false),
      correlationId                = dependencies.correlationId,
      taxId                        = empRef,
      eligibilityCheckResult       = eligibleEligibilityCheckResultEpaye,
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

    def journeyAfterEnteredDirectDebitDetailsNoAffordability(): Journey.AfterEnteredDirectDebitDetails = Journey.Epaye.EnteredDirectDebitDetails(
      _id                          = dependencies.journeyId,
      origin                       = Origins.Epaye.EpayeService,
      createdOn                    = dependencies.createdOn,
      sjRequest                    = sjRequest,
      sessionId                    = dependencies.sessionId,
      stage                        = Stage.AfterEnteredDirectDebitDetails.EnteredDirectDebitDetails,
      affordabilityEnabled         = Some(false),
      correlationId                = dependencies.correlationId,
      taxId                        = empRef,
      eligibilityCheckResult       = eligibleEligibilityCheckResultEpaye,
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

    def updateConfirmedDirectDebitDetailsRequest(): JsNull.type = JsNull

    def journeyAfterConfirmedDirectDebitDetailsNoAffordability: Journey.AfterConfirmedDirectDebitDetails = Journey.Epaye.ConfirmedDirectDebitDetails(
      _id                          = dependencies.journeyId,
      origin                       = Origins.Epaye.EpayeService,
      createdOn                    = dependencies.createdOn,
      sjRequest                    = sjRequest,
      sessionId                    = dependencies.sessionId,
      stage                        = Stage.AfterConfirmedDirectDebitDetails.ConfirmedDetails,
      affordabilityEnabled         = Some(false),
      correlationId                = dependencies.correlationId,
      taxId                        = empRef,
      eligibilityCheckResult       = eligibleEligibilityCheckResultEpaye,
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

      Journey.Epaye.AgreedTermsAndConditions(
        _id                          = dependencies.journeyId,
        origin                       = Origins.Epaye.EpayeService,
        createdOn                    = dependencies.createdOn,
        sjRequest                    = sjRequest,
        sessionId                    = dependencies.sessionId,
        stage                        = stage,
        affordabilityEnabled         = Some(false),
        correlationId                = dependencies.correlationId,
        taxId                        = empRef,
        eligibilityCheckResult       = eligibleEligibilityCheckResultEpaye,
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

    def journeyAfterSelectedEmail: Journey.Epaye.SelectedEmailToBeVerified = Journey.Epaye.SelectedEmailToBeVerified(
      _id                          = dependencies.journeyId,
      origin                       = Origins.Epaye.EpayeService,
      createdOn                    = dependencies.createdOn,
      sjRequest                    = sjRequest,
      sessionId                    = dependencies.sessionId,
      stage                        = Stage.AfterSelectedAnEmailToBeVerified.EmailChosen,
      affordabilityEnabled         = Some(false),
      correlationId                = dependencies.correlationId,
      taxId                        = empRef,
      eligibilityCheckResult       = eligibleEligibilityCheckResultEpaye,
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

    def journeyAfterEmailVerificationResult(result: EmailVerificationResult): Journey.Epaye.EmailVerificationComplete = Journey.Epaye.EmailVerificationComplete(
      _id                          = dependencies.journeyId,
      origin                       = Origins.Epaye.EpayeService,
      createdOn                    = dependencies.createdOn,
      sjRequest                    = sjRequest,
      sessionId                    = dependencies.sessionId,
      stage                        = result match {
        case EmailVerificationResult.Verified => Stage.AfterEmailVerificationPhase.VerificationSuccess
        case EmailVerificationResult.Locked   => Stage.AfterEmailVerificationPhase.Locked
      },
      affordabilityEnabled         = Some(false),
      correlationId                = dependencies.correlationId,
      taxId                        = empRef,
      eligibilityCheckResult       = eligibleEligibilityCheckResultEpaye,
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

    def updateArrangementRequest(): ArrangementResponse = dependencies.arrangementResponseEpaye

    def journeyAfterSubmittedArrangementNoAffordability(isEmailAddressRequired: Boolean): Journey.AfterArrangementSubmitted = Journey.Epaye.SubmittedArrangement(
      _id                          = dependencies.journeyId,
      origin                       = Origins.Epaye.EpayeService,
      createdOn                    = dependencies.createdOn,
      sjRequest                    = sjRequest,
      sessionId                    = dependencies.sessionId,
      stage                        = Stage.AfterSubmittedArrangement.Submitted,
      affordabilityEnabled         = Some(false),
      correlationId                = dependencies.correlationId,
      taxId                        = empRef,
      eligibilityCheckResult       = eligibleEligibilityCheckResultEpaye,
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
      arrangementResponse          = dependencies.arrangementResponseEpaye,
      pegaCaseId                   = None
    )

  }
}
