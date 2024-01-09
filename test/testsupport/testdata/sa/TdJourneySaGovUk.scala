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

package testsupport.testdata.sa

import essttp.journey.model.SjRequest.Sa
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

trait TdJourneySaGovUk {
  dependencies: TdBase with TdSa =>

  object SaGovUk extends TdJourneyStructure {

    def sjRequest: Sa.Empty = SjRequest.Sa.Empty()

    def sjResponse: SjResponse = SjResponse(
      nextUrl   = NextUrl(s"http://localhost:9215/set-up-a-payment-plan/sa-payment-plan"),
      journeyId = dependencies.journeyId
    )

    def postPath: String = "/sa/gov-uk/journey/start"

    def journeyAfterStarted: Journey.Sa.Started = Journey.Sa.Started(
      _id           = dependencies.journeyId,
      origin        = Origins.Sa.GovUk,
      createdOn     = dependencies.createdOn,
      sjRequest     = sjRequest,
      sessionId     = dependencies.sessionId,
      correlationId = dependencies.correlationId,
      stage         = Stage.AfterStarted.Started
    )

    def updateTaxIdRequest(): TaxId = saUtr

    def journeyAfterDetermineTaxIds: Journey.Sa.ComputedTaxId = Journey.Sa.ComputedTaxId(
      _id           = dependencies.journeyId,
      origin        = Origins.Sa.GovUk,
      createdOn     = dependencies.createdOn,
      sjRequest     = sjRequest,
      sessionId     = dependencies.sessionId,
      correlationId = dependencies.correlationId,
      stage         = Stage.AfterComputedTaxId.ComputedTaxId,
      taxId         = saUtr
    )

    def updateEligibilityCheckRequest(): EligibilityCheckResult = eligibleEligibilityCheckResultSa

    def journeyAfterEligibilityCheckEligible: Journey.Sa.EligibilityChecked = Journey.Sa.EligibilityChecked(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Sa.GovUk,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      correlationId          = dependencies.correlationId,
      stage                  = Stage.AfterEligibilityCheck.Eligible,
      taxId                  = saUtr,
      eligibilityCheckResult = eligibleEligibilityCheckResultSa
    )

    def journeyAfterEligibilityCheckNotEligible: Journey.Sa.EligibilityChecked = Journey.Sa.EligibilityChecked(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Sa.GovUk,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      correlationId          = dependencies.correlationId,
      stage                  = Stage.AfterEligibilityCheck.Ineligible,
      taxId                  = saUtr,
      eligibilityCheckResult = ineligibleEligibilityCheckResultSa
    )

    def updateCanPayUpfrontYesRequest(): CanPayUpfront = canPayUpfrontYes

    def updateCanPayUpfrontNoRequest(): CanPayUpfront = canPayUpfrontNo

    def journeyAfterCanPayUpfrontYes: Journey.Sa.AnsweredCanPayUpfront = Journey.Sa.AnsweredCanPayUpfront(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Sa.GovUk,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      correlationId          = dependencies.correlationId,
      stage                  = Stage.AfterCanPayUpfront.Yes,
      taxId                  = saUtr,
      eligibilityCheckResult = eligibleEligibilityCheckResultSa,
      canPayUpfront          = canPayUpfrontYes
    )

    def journeyAfterCanPayUpfrontNo: Journey.Sa.AnsweredCanPayUpfront = Journey.Sa.AnsweredCanPayUpfront(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Sa.GovUk,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      correlationId          = dependencies.correlationId,
      stage                  = Stage.AfterCanPayUpfront.No,
      taxId                  = saUtr,
      eligibilityCheckResult = eligibleEligibilityCheckResultSa,
      canPayUpfront          = canPayUpfrontNo
    )

    override def updateUpfrontPaymentAmountRequest(): UpfrontPaymentAmount = dependencies.upfrontPaymentAmount

    override def journeyAfterUpfrontPaymentAmount: Journey.Sa.EnteredUpfrontPaymentAmount = Journey.Sa.EnteredUpfrontPaymentAmount(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Sa.GovUk,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      correlationId          = dependencies.correlationId,
      stage                  = Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount,
      taxId                  = saUtr,
      eligibilityCheckResult = eligibleEligibilityCheckResultSa,
      canPayUpfront          = canPayUpfrontYes,
      upfrontPaymentAmount   = dependencies.upfrontPaymentAmount
    )

    def updateExtremeDatesRequest(): ExtremeDatesResponse = dependencies.extremeDatesWithUpfrontPayment

    def journeyAfterExtremeDates: Journey.Sa.RetrievedExtremeDates = Journey.Sa.RetrievedExtremeDates(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Sa.GovUk,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      correlationId          = dependencies.correlationId,
      stage                  = Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved,
      taxId                  = saUtr,
      eligibilityCheckResult = eligibleEligibilityCheckResultSa,
      upfrontPaymentAnswers  = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse   = dependencies.extremeDatesWithUpfrontPayment
    )

    def updateInstalmentAmountsRequest(): InstalmentAmounts = dependencies.instalmentAmounts

    def journeyAfterInstalmentAmounts: Journey.Sa.RetrievedAffordabilityResult = Journey.Sa.RetrievedAffordabilityResult(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Sa.GovUk,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      correlationId          = dependencies.correlationId,
      stage                  = Stage.AfterAffordabilityResult.RetrievedAffordabilityResult,
      taxId                  = saUtr,
      eligibilityCheckResult = eligibleEligibilityCheckResultSa,
      upfrontPaymentAnswers  = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse   = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts      = dependencies.instalmentAmounts
    )

    def updateMonthlyPaymentAmountRequest(): MonthlyPaymentAmount = dependencies.monthlyPaymentAmount

    def journeyAfterMonthlyPaymentAmount: Journey.Sa.EnteredMonthlyPaymentAmount = Journey.Sa.EnteredMonthlyPaymentAmount(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Sa.GovUk,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      correlationId          = dependencies.correlationId,
      stage                  = Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount,
      taxId                  = saUtr,
      eligibilityCheckResult = eligibleEligibilityCheckResultSa,
      upfrontPaymentAnswers  = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse   = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts      = dependencies.instalmentAmounts,
      monthlyPaymentAmount   = dependencies.monthlyPaymentAmount
    )

    def updateDayOfMonthRequest(): DayOfMonth = dependencies.dayOfMonth

    def journeyAfterDayOfMonth: Journey.Sa.EnteredDayOfMonth = Journey.Sa.EnteredDayOfMonth(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Sa.GovUk,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      correlationId          = dependencies.correlationId,
      stage                  = Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth,
      taxId                  = saUtr,
      eligibilityCheckResult = eligibleEligibilityCheckResultSa,
      upfrontPaymentAnswers  = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse   = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts      = dependencies.instalmentAmounts,
      monthlyPaymentAmount   = dependencies.monthlyPaymentAmount,
      dayOfMonth             = dependencies.dayOfMonth
    )

    def updateStartDatesResponse(): StartDatesResponse = dependencies.startDatesResponseWithInitialPayment

    def journeyAfterStartDatesResponse: Journey.AfterStartDatesResponse = Journey.Sa.RetrievedStartDates(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Sa.GovUk,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      correlationId          = dependencies.correlationId,
      stage                  = Stage.AfterStartDatesResponse.StartDatesResponseRetrieved,
      taxId                  = saUtr,
      eligibilityCheckResult = eligibleEligibilityCheckResultSa,
      upfrontPaymentAnswers  = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse   = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts      = dependencies.instalmentAmounts,
      monthlyPaymentAmount   = dependencies.monthlyPaymentAmount,
      dayOfMonth             = dependencies.dayOfMonth,
      startDatesResponse     = dependencies.startDatesResponseWithInitialPayment
    )

    def updateAffordableQuotesResponse(): AffordableQuotesResponse = dependencies.affordableQuotesResponse

    def journeyAfterAffordableQuotesResponse: Journey.AfterAffordableQuotesResponse = Journey.Sa.RetrievedAffordableQuotes(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Sa.GovUk,
      createdOn                = dependencies.createdOn,
      sjRequest                = sjRequest,
      sessionId                = dependencies.sessionId,
      correlationId            = dependencies.correlationId,
      stage                    = Stage.AfterAffordableQuotesResponse.AffordableQuotesRetrieved,
      taxId                    = saUtr,
      eligibilityCheckResult   = eligibleEligibilityCheckResultSa,
      upfrontPaymentAnswers    = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse     = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts        = dependencies.instalmentAmounts,
      monthlyPaymentAmount     = dependencies.monthlyPaymentAmount,
      dayOfMonth               = dependencies.dayOfMonth,
      startDatesResponse       = dependencies.startDatesResponseWithInitialPayment,
      affordableQuotesResponse = dependencies.affordableQuotesResponse
    )

    def updateSelectedPaymentPlanRequest(): PaymentPlan = dependencies.paymentPlan(1)

    def journeyAfterSelectedPaymentPlan: Journey.AfterSelectedPaymentPlan = Journey.Sa.ChosenPaymentPlan(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Sa.GovUk,
      createdOn                = dependencies.createdOn,
      sjRequest                = sjRequest,
      sessionId                = dependencies.sessionId,
      correlationId            = dependencies.correlationId,
      stage                    = Stage.AfterSelectedPlan.SelectedPlan,
      taxId                    = saUtr,
      eligibilityCheckResult   = eligibleEligibilityCheckResultSa,
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

    def journeyAfterCheckedPaymentPlan: Journey.AfterCheckedPaymentPlan = Journey.Sa.CheckedPaymentPlan(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Sa.GovUk,
      createdOn                = dependencies.createdOn,
      sjRequest                = sjRequest,
      sessionId                = dependencies.sessionId,
      correlationId            = dependencies.correlationId,
      stage                    = Stage.AfterCheckedPlan.AcceptedPlan,
      taxId                    = saUtr,
      eligibilityCheckResult   = eligibleEligibilityCheckResultSa,
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

    def journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder: Boolean): Journey.AfterEnteredDetailsAboutBankAccount = Journey.Sa.EnteredDetailsAboutBankAccount(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Sa.GovUk,
      createdOn                = dependencies.createdOn,
      sjRequest                = sjRequest,
      sessionId                = dependencies.sessionId,
      correlationId            = dependencies.correlationId,
      stage                    = if (isAccountHolder) Stage.AfterEnteredDetailsAboutBankAccount.Business else Stage.AfterEnteredDetailsAboutBankAccount.IsNotAccountHolder,
      taxId                    = saUtr,
      eligibilityCheckResult   = eligibleEligibilityCheckResultSa,
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

    def journeyAfterEnteredDirectDebitDetails(): Journey.AfterEnteredDirectDebitDetails = Journey.Sa.EnteredDirectDebitDetails(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Sa.GovUk,
      createdOn                = dependencies.createdOn,
      sjRequest                = sjRequest,
      sessionId                = dependencies.sessionId,
      correlationId            = dependencies.correlationId,
      stage                    = Stage.AfterEnteredDirectDebitDetails.EnteredDirectDebitDetails,
      taxId                    = saUtr,
      eligibilityCheckResult   = eligibleEligibilityCheckResultSa,
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

    override def updateConfirmedDirectDebitDetailsRequest(): JsNull.type = JsNull

    override def journeyAfterConfirmedDirectDebitDetails: Journey.AfterConfirmedDirectDebitDetails = Journey.Sa.ConfirmedDirectDebitDetails(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Sa.GovUk,
      createdOn                = dependencies.createdOn,
      sjRequest                = sjRequest,
      sessionId                = dependencies.sessionId,
      correlationId            = dependencies.correlationId,
      stage                    = Stage.AfterConfirmedDirectDebitDetails.ConfirmedDetails,
      taxId                    = saUtr,
      eligibilityCheckResult   = eligibleEligibilityCheckResultSa,
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

      Journey.Sa.AgreedTermsAndConditions(
        _id                      = dependencies.journeyId,
        origin                   = Origins.Sa.GovUk,
        createdOn                = dependencies.createdOn,
        sjRequest                = sjRequest,
        sessionId                = dependencies.sessionId,
        correlationId            = dependencies.correlationId,
        stage                    = stage,
        taxId                    = saUtr,
        eligibilityCheckResult   = eligibleEligibilityCheckResultSa,
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

    def journeyAfterSelectedEmail: Journey.Sa.SelectedEmailToBeVerified = Journey.Sa.SelectedEmailToBeVerified(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Sa.GovUk,
      createdOn                = dependencies.createdOn,
      sjRequest                = sjRequest,
      sessionId                = dependencies.sessionId,
      stage                    = Stage.AfterSelectedAnEmailToBeVerified.EmailChosen,
      correlationId            = dependencies.correlationId,
      taxId                    = saUtr,
      eligibilityCheckResult   = eligibleEligibilityCheckResultSa,
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
      isEmailAddressRequired   = IsEmailAddressRequired(true),
      emailToBeVerified        = dependencies.email
    )

    def journeyAfterEmailVerificationResult(result: EmailVerificationResult): Journey.Sa.EmailVerificationComplete = Journey.Sa.EmailVerificationComplete(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Sa.GovUk,
      createdOn                = dependencies.createdOn,
      sjRequest                = sjRequest,
      sessionId                = dependencies.sessionId,
      stage                    = result match {
        case EmailVerificationResult.Verified => Stage.AfterEmailVerificationPhase.VerificationSuccess
        case EmailVerificationResult.Locked   => Stage.AfterEmailVerificationPhase.Locked
      },
      correlationId            = dependencies.correlationId,
      taxId                    = saUtr,
      eligibilityCheckResult   = eligibleEligibilityCheckResultSa,
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
      isEmailAddressRequired   = IsEmailAddressRequired(true),
      emailToBeVerified        = dependencies.email,
      emailVerificationResult  = result,
      emailVerificationAnswers = emailVerificationAnswers(Some(result))
    )

    def updateArrangementRequest(): ArrangementResponse = dependencies.arrangementResponseSa

    def journeyAfterSubmittedArrangement(isEmailAddressRequired: Boolean): Journey.AfterArrangementSubmitted = Journey.Sa.SubmittedArrangement(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Sa.GovUk,
      createdOn                = dependencies.createdOn,
      sjRequest                = sjRequest,
      sessionId                = dependencies.sessionId,
      correlationId            = dependencies.correlationId,
      stage                    = Stage.AfterSubmittedArrangement.Submitted,
      taxId                    = saUtr,
      eligibilityCheckResult   = eligibleEligibilityCheckResultSa,
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
      arrangementResponse      = dependencies.arrangementResponseSa
    )

  }
}
