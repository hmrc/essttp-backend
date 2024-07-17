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
import essttp.rootmodel.bank.{BankDetails, DetailsAboutBankAccount}
import essttp.rootmodel.dates.extremedates.ExtremeDatesResponse
import essttp.rootmodel.dates.startdates.StartDatesResponse
import essttp.rootmodel.ttp.affordability.InstalmentAmounts
import essttp.rootmodel.ttp.affordablequotes.{AffordableQuotesResponse, PaymentPlan}
import essttp.rootmodel.ttp.arrangement.ArrangementResponse
import essttp.rootmodel.ttp.eligibility.EligibilityCheckResult
import paymentsEmailVerification.models.EmailVerificationResult
import play.api.libs.json.JsNull
import testsupport.testdata.{TdBase, TdJourneyStructure}

trait TdJourneyEpayeDetachedUrl {
  dependencies: TdBase with TdEpaye =>

  object EpayeDetachedUrl extends TdJourneyStructure {

    def sjRequest: Epaye.Empty = SjRequest.Epaye.Empty()

    def sjResponse: SjResponse = SjResponse(
      nextUrl   = NextUrl(s"http://localhost:9215/set-up-a-payment-plan/epaye-payment-plan"),
      journeyId = dependencies.journeyId
    )

    def postPath: String = "/epaye/detached-url/journey/start"

    def journeyAfterStarted: Journey.Epaye.Started = Journey.Epaye.Started(
      _id                   = dependencies.journeyId,
      origin                = Origins.Epaye.DetachedUrl,
      createdOn             = dependencies.createdOn,
      sjRequest             = sjRequest,
      sessionId             = dependencies.sessionId,
      stage                 = Stage.AfterStarted.Started,
      affordabilityRequired = Some(false),
      correlationId         = dependencies.correlationId
    )

    def updateTaxIdRequest(): TaxId = empRef

    def journeyAfterDetermineTaxIds: Journey.Epaye.ComputedTaxId = Journey.Epaye.ComputedTaxId(
      _id                   = dependencies.journeyId,
      origin                = Origins.Epaye.DetachedUrl,
      createdOn             = dependencies.createdOn,
      sjRequest             = sjRequest,
      sessionId             = dependencies.sessionId,
      stage                 = Stage.AfterComputedTaxId.ComputedTaxId,
      affordabilityRequired = Some(false),
      correlationId         = dependencies.correlationId,
      taxId                 = empRef
    )

    def updateEligibilityCheckRequest(): EligibilityCheckResult = eligibleEligibilityCheckResultEpaye

    def journeyAfterEligibilityCheckEligible: Journey.Epaye.EligibilityChecked = Journey.Epaye.EligibilityChecked(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.DetachedUrl,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterEligibilityCheck.Eligible,
      affordabilityRequired  = Some(false),
      correlationId          = dependencies.correlationId,
      taxId                  = empRef,
      eligibilityCheckResult = eligibleEligibilityCheckResultEpaye
    )

    def journeyAfterEligibilityCheckNotEligible: Journey.Epaye.EligibilityChecked = Journey.Epaye.EligibilityChecked(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.DetachedUrl,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterEligibilityCheck.Ineligible,
      affordabilityRequired  = Some(false),
      correlationId          = dependencies.correlationId,
      taxId                  = empRef,
      eligibilityCheckResult = ineligibleEligibilityCheckResultEpaye
    )

    def updateCanPayUpfrontYesRequest(): CanPayUpfront = canPayUpfrontYes

    def updateCanPayUpfrontNoRequest(): CanPayUpfront = canPayUpfrontNo

    def journeyAfterCanPayUpfrontYes: Journey.Epaye.AnsweredCanPayUpfront = Journey.Epaye.AnsweredCanPayUpfront(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.DetachedUrl,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterCanPayUpfront.Yes,
      affordabilityRequired  = Some(false),
      correlationId          = dependencies.correlationId,
      taxId                  = empRef,
      eligibilityCheckResult = eligibleEligibilityCheckResultEpaye,
      canPayUpfront          = canPayUpfrontYes
    )

    def journeyAfterCanPayUpfrontNo: Journey.Epaye.AnsweredCanPayUpfront = Journey.Epaye.AnsweredCanPayUpfront(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.DetachedUrl,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterCanPayUpfront.No,
      affordabilityRequired  = Some(false),
      correlationId          = dependencies.correlationId,
      taxId                  = empRef,
      eligibilityCheckResult = eligibleEligibilityCheckResultEpaye,
      canPayUpfront          = canPayUpfrontNo
    )

    def updateUpfrontPaymentAmountRequest(): UpfrontPaymentAmount = dependencies.upfrontPaymentAmount

    def journeyAfterUpfrontPaymentAmount: Journey.Epaye.EnteredUpfrontPaymentAmount = Journey.Epaye.EnteredUpfrontPaymentAmount(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.DetachedUrl,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount,
      affordabilityRequired  = Some(false),
      correlationId          = dependencies.correlationId,
      taxId                  = empRef,
      eligibilityCheckResult = eligibleEligibilityCheckResultEpaye,
      canPayUpfront          = canPayUpfrontYes,
      upfrontPaymentAmount   = dependencies.upfrontPaymentAmount
    )

    def updateExtremeDatesRequest(): ExtremeDatesResponse = dependencies.extremeDatesWithUpfrontPayment

    def journeyAfterExtremeDates: Journey.Epaye.RetrievedExtremeDates = Journey.Epaye.RetrievedExtremeDates(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.DetachedUrl,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved,
      affordabilityRequired  = Some(false),
      correlationId          = dependencies.correlationId,
      taxId                  = empRef,
      eligibilityCheckResult = eligibleEligibilityCheckResultEpaye,
      upfrontPaymentAnswers  = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse   = dependencies.extremeDatesWithUpfrontPayment
    )

    def updateInstalmentAmountsRequest(): InstalmentAmounts = dependencies.instalmentAmounts

    def journeyAfterInstalmentAmounts: Journey.Epaye.RetrievedAffordabilityResult = Journey.Epaye.RetrievedAffordabilityResult(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.DetachedUrl,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterAffordabilityResult.RetrievedAffordabilityResult,
      affordabilityRequired  = Some(false),
      correlationId          = dependencies.correlationId,
      taxId                  = empRef,
      eligibilityCheckResult = eligibleEligibilityCheckResultEpaye,
      upfrontPaymentAnswers  = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse   = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts      = dependencies.instalmentAmounts
    )

    def updateMonthlyPaymentAmountRequest(): MonthlyPaymentAmount = dependencies.monthlyPaymentAmount

    def journeyAfterMonthlyPaymentAmount: Journey.Epaye.EnteredMonthlyPaymentAmount = Journey.Epaye.EnteredMonthlyPaymentAmount(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.DetachedUrl,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount,
      affordabilityRequired  = Some(false),
      correlationId          = dependencies.correlationId,
      taxId                  = empRef,
      eligibilityCheckResult = eligibleEligibilityCheckResultEpaye,
      upfrontPaymentAnswers  = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse   = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts      = dependencies.instalmentAmounts,
      monthlyPaymentAmount   = dependencies.monthlyPaymentAmount
    )

    def updateDayOfMonthRequest(): DayOfMonth = dependencies.dayOfMonth

    def journeyAfterDayOfMonth: Journey.Epaye.EnteredDayOfMonth = Journey.Epaye.EnteredDayOfMonth(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.DetachedUrl,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth,
      affordabilityRequired  = Some(false),
      correlationId          = dependencies.correlationId,
      taxId                  = empRef,
      eligibilityCheckResult = eligibleEligibilityCheckResultEpaye,
      upfrontPaymentAnswers  = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse   = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts      = dependencies.instalmentAmounts,
      monthlyPaymentAmount   = dependencies.monthlyPaymentAmount,
      dayOfMonth             = dependencies.dayOfMonth
    )

    def updateStartDatesResponse(): StartDatesResponse = dependencies.startDatesResponseWithInitialPayment

    def journeyAfterStartDatesResponse: Journey.AfterStartDatesResponse = Journey.Epaye.RetrievedStartDates(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.DetachedUrl,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterStartDatesResponse.StartDatesResponseRetrieved,
      affordabilityRequired  = Some(false),
      correlationId          = dependencies.correlationId,
      taxId                  = empRef,
      eligibilityCheckResult = eligibleEligibilityCheckResultEpaye,
      upfrontPaymentAnswers  = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse   = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts      = dependencies.instalmentAmounts,
      monthlyPaymentAmount   = dependencies.monthlyPaymentAmount,
      dayOfMonth             = dependencies.dayOfMonth,
      startDatesResponse     = dependencies.startDatesResponseWithInitialPayment
    )

    def updateAffordableQuotesResponse(): AffordableQuotesResponse = dependencies.affordableQuotesResponse

    def journeyAfterAffordableQuotesResponse: Journey.AfterAffordableQuotesResponse = Journey.Epaye.RetrievedAffordableQuotes(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Epaye.DetachedUrl,
      createdOn                = dependencies.createdOn,
      sjRequest                = sjRequest,
      sessionId                = dependencies.sessionId,
      stage                    = Stage.AfterAffordableQuotesResponse.AffordableQuotesRetrieved,
      affordabilityRequired    = Some(false),
      correlationId            = dependencies.correlationId,
      taxId                    = empRef,
      eligibilityCheckResult   = eligibleEligibilityCheckResultEpaye,
      upfrontPaymentAnswers    = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse     = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts        = dependencies.instalmentAmounts,
      monthlyPaymentAmount     = dependencies.monthlyPaymentAmount,
      dayOfMonth               = dependencies.dayOfMonth,
      startDatesResponse       = dependencies.startDatesResponseWithInitialPayment,
      affordableQuotesResponse = dependencies.affordableQuotesResponse
    )

    def updateSelectedPaymentPlanRequest(): PaymentPlan = dependencies.paymentPlan(1)

    def journeyAfterSelectedPaymentPlan: Journey.AfterSelectedPaymentPlan = Journey.Epaye.ChosenPaymentPlan(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Epaye.DetachedUrl,
      createdOn                = dependencies.createdOn,
      sjRequest                = sjRequest,
      sessionId                = dependencies.sessionId,
      stage                    = Stage.AfterSelectedPlan.SelectedPlan,
      affordabilityRequired    = Some(false),
      correlationId            = dependencies.correlationId,
      taxId                    = empRef,
      eligibilityCheckResult   = eligibleEligibilityCheckResultEpaye,
      upfrontPaymentAnswers    = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse     = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts        = dependencies.instalmentAmounts,
      monthlyPaymentAmount     = dependencies.monthlyPaymentAmount,
      dayOfMonth               = dependencies.dayOfMonth,
      startDatesResponse       = dependencies.startDatesResponseWithInitialPayment,
      affordableQuotesResponse = dependencies.affordableQuotesResponse,
      selectedPaymentPlan      = dependencies.paymentPlan(1)
    )

    def updateCheckedPaymentPlanRequest(): JsNull.type = JsNull

    def journeyAfterCheckedPaymentPlan: Journey.AfterCheckedPaymentPlan = Journey.Epaye.CheckedPaymentPlan(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Epaye.DetachedUrl,
      createdOn                = dependencies.createdOn,
      sjRequest                = sjRequest,
      sessionId                = dependencies.sessionId,
      stage                    = Stage.AfterCheckedPlan.AcceptedPlan,
      affordabilityRequired    = Some(false),
      correlationId            = dependencies.correlationId,
      taxId                    = empRef,
      eligibilityCheckResult   = eligibleEligibilityCheckResultEpaye,
      upfrontPaymentAnswers    = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse     = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts        = dependencies.instalmentAmounts,
      monthlyPaymentAmount     = dependencies.monthlyPaymentAmount,
      dayOfMonth               = dependencies.dayOfMonth,
      startDatesResponse       = dependencies.startDatesResponseWithInitialPayment,
      affordableQuotesResponse = dependencies.affordableQuotesResponse,
      selectedPaymentPlan      = dependencies.paymentPlan(1)
    )

    def updateDetailsAboutBankAccountRequest(isAccountHolder: Boolean): DetailsAboutBankAccount =
      DetailsAboutBankAccount(dependencies.businessBankAccount, isAccountHolder)

    def journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder: Boolean): Journey.AfterEnteredDetailsAboutBankAccount = Journey.Epaye.EnteredDetailsAboutBankAccount(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Epaye.DetachedUrl,
      createdOn                = dependencies.createdOn,
      sjRequest                = sjRequest,
      sessionId                = dependencies.sessionId,
      stage                    = if (isAccountHolder) Stage.AfterEnteredDetailsAboutBankAccount.Business else Stage.AfterEnteredDetailsAboutBankAccount.IsNotAccountHolder,
      affordabilityRequired    = Some(false),
      correlationId            = dependencies.correlationId,
      taxId                    = empRef,
      eligibilityCheckResult   = eligibleEligibilityCheckResultEpaye,
      upfrontPaymentAnswers    = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse     = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts        = dependencies.instalmentAmounts,
      monthlyPaymentAmount     = dependencies.monthlyPaymentAmount,
      dayOfMonth               = dependencies.dayOfMonth,
      startDatesResponse       = dependencies.startDatesResponseWithInitialPayment,
      affordableQuotesResponse = dependencies.affordableQuotesResponse,
      selectedPaymentPlan      = dependencies.paymentPlan(1),
      detailsAboutBankAccount  = DetailsAboutBankAccount(dependencies.businessBankAccount, isAccountHolder)
    )

    def updateDirectDebitDetailsRequest(): BankDetails = dependencies.directDebitDetails

    def journeyAfterEnteredDirectDebitDetails(): Journey.AfterEnteredDirectDebitDetails = Journey.Epaye.EnteredDirectDebitDetails(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Epaye.DetachedUrl,
      createdOn                = dependencies.createdOn,
      sjRequest                = sjRequest,
      sessionId                = dependencies.sessionId,
      stage                    = Stage.AfterEnteredDirectDebitDetails.EnteredDirectDebitDetails,
      affordabilityRequired    = Some(false),
      correlationId            = dependencies.correlationId,
      taxId                    = empRef,
      eligibilityCheckResult   = eligibleEligibilityCheckResultEpaye,
      upfrontPaymentAnswers    = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse     = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts        = dependencies.instalmentAmounts,
      monthlyPaymentAmount     = dependencies.monthlyPaymentAmount,
      dayOfMonth               = dependencies.dayOfMonth,
      startDatesResponse       = dependencies.startDatesResponseWithInitialPayment,
      affordableQuotesResponse = dependencies.affordableQuotesResponse,
      selectedPaymentPlan      = dependencies.paymentPlan(1),
      detailsAboutBankAccount  = DetailsAboutBankAccount(dependencies.businessBankAccount, isAccountHolder = true),
      directDebitDetails       = directDebitDetails
    )

    def updateConfirmedDirectDebitDetailsRequest(): JsNull.type = JsNull

    def journeyAfterConfirmedDirectDebitDetails: Journey.AfterConfirmedDirectDebitDetails = Journey.Epaye.ConfirmedDirectDebitDetails(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Epaye.DetachedUrl,
      createdOn                = dependencies.createdOn,
      sjRequest                = sjRequest,
      sessionId                = dependencies.sessionId,
      stage                    = Stage.AfterConfirmedDirectDebitDetails.ConfirmedDetails,
      affordabilityRequired    = Some(false),
      correlationId            = dependencies.correlationId,
      taxId                    = empRef,
      eligibilityCheckResult   = eligibleEligibilityCheckResultEpaye,
      upfrontPaymentAnswers    = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse     = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts        = dependencies.instalmentAmounts,
      monthlyPaymentAmount     = dependencies.monthlyPaymentAmount,
      dayOfMonth               = dependencies.dayOfMonth,
      startDatesResponse       = dependencies.startDatesResponseWithInitialPayment,
      affordableQuotesResponse = dependencies.affordableQuotesResponse,
      selectedPaymentPlan      = dependencies.paymentPlan(1),
      detailsAboutBankAccount  = DetailsAboutBankAccount(dependencies.businessBankAccount, isAccountHolder = true),
      directDebitDetails       = directDebitDetails
    )

    def updateAgreedTermsAndConditionsRequest(isEmailAddressRequired: Boolean): IsEmailAddressRequired = IsEmailAddressRequired(isEmailAddressRequired)

    def journeyAfterAgreedTermsAndConditions(isEmailAddressRequired: Boolean): Journey.AfterAgreedTermsAndConditions = {
      val stage =
        if (isEmailAddressRequired) Stage.AfterAgreedTermsAndConditions.EmailAddressRequired
        else Stage.AfterAgreedTermsAndConditions.EmailAddressNotRequired

      Journey.Epaye.AgreedTermsAndConditions(
        _id                      = dependencies.journeyId,
        origin                   = Origins.Epaye.DetachedUrl,
        createdOn                = dependencies.createdOn,
        sjRequest                = sjRequest,
        sessionId                = dependencies.sessionId,
        stage                    = stage,
        affordabilityRequired    = Some(false),
        correlationId            = dependencies.correlationId,
        taxId                    = empRef,
        eligibilityCheckResult   = eligibleEligibilityCheckResultEpaye,
        upfrontPaymentAnswers    = dependencies.upfrontPaymentAnswersDeclared,
        extremeDatesResponse     = dependencies.extremeDatesWithUpfrontPayment,
        instalmentAmounts        = dependencies.instalmentAmounts,
        monthlyPaymentAmount     = dependencies.monthlyPaymentAmount,
        dayOfMonth               = dependencies.dayOfMonth,
        startDatesResponse       = dependencies.startDatesResponseWithInitialPayment,
        affordableQuotesResponse = dependencies.affordableQuotesResponse,
        selectedPaymentPlan      = dependencies.paymentPlan(1),
        detailsAboutBankAccount  = DetailsAboutBankAccount(dependencies.businessBankAccount, isAccountHolder = true),
        directDebitDetails       = directDebitDetails,
        isEmailAddressRequired   = IsEmailAddressRequired(isEmailAddressRequired)
      )
    }

    def updateSelectedEmailRequest(): Email = dependencies.email

    def journeyAfterSelectedEmail: Journey.Epaye.SelectedEmailToBeVerified = Journey.Epaye.SelectedEmailToBeVerified(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Epaye.DetachedUrl,
      createdOn                = dependencies.createdOn,
      sjRequest                = sjRequest,
      sessionId                = dependencies.sessionId,
      stage                    = Stage.AfterSelectedAnEmailToBeVerified.EmailChosen,
      affordabilityRequired    = Some(false),
      correlationId            = dependencies.correlationId,
      taxId                    = empRef,
      eligibilityCheckResult   = eligibleEligibilityCheckResultEpaye,
      upfrontPaymentAnswers    = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse     = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts        = dependencies.instalmentAmounts,
      monthlyPaymentAmount     = dependencies.monthlyPaymentAmount,
      dayOfMonth               = dependencies.dayOfMonth,
      startDatesResponse       = dependencies.startDatesResponseWithInitialPayment,
      affordableQuotesResponse = dependencies.affordableQuotesResponse,
      selectedPaymentPlan      = dependencies.paymentPlan(1),
      detailsAboutBankAccount  = DetailsAboutBankAccount(dependencies.businessBankAccount, isAccountHolder = true),
      directDebitDetails       = directDebitDetails,
      isEmailAddressRequired   = IsEmailAddressRequired(value = true),
      emailToBeVerified        = dependencies.email
    )

    def journeyAfterEmailVerificationResult(result: EmailVerificationResult): Journey.Epaye.EmailVerificationComplete = Journey.Epaye.EmailVerificationComplete(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Epaye.DetachedUrl,
      createdOn                = dependencies.createdOn,
      sjRequest                = sjRequest,
      sessionId                = dependencies.sessionId,
      stage                    = result match {
        case EmailVerificationResult.Verified => Stage.AfterEmailVerificationPhase.VerificationSuccess
        case EmailVerificationResult.Locked   => Stage.AfterEmailVerificationPhase.Locked
      },
      affordabilityRequired    = Some(false),
      correlationId            = dependencies.correlationId,
      taxId                    = empRef,
      eligibilityCheckResult   = eligibleEligibilityCheckResultEpaye,
      upfrontPaymentAnswers    = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse     = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts        = dependencies.instalmentAmounts,
      monthlyPaymentAmount     = dependencies.monthlyPaymentAmount,
      dayOfMonth               = dependencies.dayOfMonth,
      startDatesResponse       = dependencies.startDatesResponseWithInitialPayment,
      affordableQuotesResponse = dependencies.affordableQuotesResponse,
      selectedPaymentPlan      = dependencies.paymentPlan(1),
      detailsAboutBankAccount  = DetailsAboutBankAccount(dependencies.businessBankAccount, isAccountHolder = true),
      directDebitDetails       = directDebitDetails,
      isEmailAddressRequired   = IsEmailAddressRequired(value = true),
      emailToBeVerified        = dependencies.email,
      emailVerificationResult  = result,
      emailVerificationAnswers = emailVerificationAnswers(Some(result))
    )

    def updateArrangementRequest(): ArrangementResponse = dependencies.arrangementResponseEpaye

    def journeyAfterSubmittedArrangement(isEmailAddressRequired: Boolean): Journey.AfterArrangementSubmitted = Journey.Epaye.SubmittedArrangement(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Epaye.DetachedUrl,
      createdOn                = dependencies.createdOn,
      sjRequest                = sjRequest,
      sessionId                = dependencies.sessionId,
      stage                    = Stage.AfterSubmittedArrangement.Submitted,
      affordabilityRequired    = Some(false),
      correlationId            = dependencies.correlationId,
      taxId                    = empRef,
      eligibilityCheckResult   = eligibleEligibilityCheckResultEpaye,
      upfrontPaymentAnswers    = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse     = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts        = dependencies.instalmentAmounts,
      monthlyPaymentAmount     = dependencies.monthlyPaymentAmount,
      dayOfMonth               = dependencies.dayOfMonth,
      startDatesResponse       = dependencies.startDatesResponseWithInitialPayment,
      affordableQuotesResponse = dependencies.affordableQuotesResponse,
      selectedPaymentPlan      = dependencies.paymentPlan(1),
      detailsAboutBankAccount  = DetailsAboutBankAccount(dependencies.businessBankAccount, isAccountHolder = true),
      directDebitDetails       = directDebitDetails,
      isEmailAddressRequired   = IsEmailAddressRequired(isEmailAddressRequired),
      emailVerificationAnswers = if (isEmailAddressRequired) {
        EmailVerificationAnswers.EmailVerified(dependencies.email, EmailVerificationResult.Verified)
      } else {
        EmailVerificationAnswers.NoEmailJourney
      },
      arrangementResponse      = dependencies.arrangementResponseEpaye
    )

  }
}
