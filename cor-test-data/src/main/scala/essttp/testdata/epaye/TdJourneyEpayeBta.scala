/*
 * Copyright 2022 HM Revenue & Customs
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

package essttp.testdata.epaye

import essttp.emailverification.EmailVerificationStatus
import essttp.journey.model.SjRequest.Epaye
import essttp.journey.model._
import essttp.rootmodel.bank.{BankDetails, DetailsAboutBankAccount}
import essttp.rootmodel.dates.extremedates.ExtremeDatesResponse
import essttp.rootmodel.dates.startdates.StartDatesResponse
import essttp.rootmodel.ttp.affordability.InstalmentAmounts
import essttp.rootmodel.ttp.affordablequotes.{AffordableQuotesResponse, PaymentPlan}
import essttp.rootmodel.ttp.arrangement.ArrangementResponse
import essttp.rootmodel._
import essttp.rootmodel.ttp.eligibility.EligibilityCheckResult
import essttp.testdata.{TdBase, TdJourneyStructure}
import essttp.utils.ResourceReader.read
import essttp.utils.JsonSyntax._
import play.api.libs.json.{JsNull, JsObject}

import scala.language.reflectiveCalls

trait TdJourneyEpayeBta {
  dependencies: TdBase with TdEpaye =>

  object EpayeBta extends TdJourneyStructure {

    def sjRequest: Epaye.Simple = SjRequest.Epaye.Simple(
      dependencies.returnUrl,
      dependencies.backUrl
    )

    def sjResponse: SjResponse = SjResponse(
      nextUrl   = NextUrl(s"http://localhost:9215/set-up-a-payment-plan/epaye-payment-plan"),
      journeyId = dependencies.journeyId
    )

    def postPath: String = "/epaye/bta/journey/start"

    def journeyAfterStarted: Journey.Epaye.Started = Journey.Epaye.Started(
      _id           = dependencies.journeyId,
      origin        = Origins.Epaye.Bta,
      createdOn     = dependencies.createdOn,
      sjRequest     = sjRequest,
      sessionId     = dependencies.sessionId,
      stage         = Stage.AfterStarted.Started,
      correlationId = dependencies.correlationId,
    )

    def updateTaxIdRequest(): TaxId = empRef

    def journeyAfterDetermineTaxIds: Journey.Epaye.ComputedTaxId = Journey.Epaye.ComputedTaxId(
      _id           = dependencies.journeyId,
      origin        = Origins.Epaye.Bta,
      createdOn     = dependencies.createdOn,
      sjRequest     = sjRequest,
      sessionId     = dependencies.sessionId,
      stage         = Stage.AfterComputedTaxId.ComputedTaxId,
      correlationId = dependencies.correlationId,
      taxId         = empRef
    )

    def updateEligibilityCheckRequest(): EligibilityCheckResult = eligibleEligibilityCheckResult

    def journeyAfterEligibilityCheckEligible: Journey.Epaye.EligibilityChecked = Journey.Epaye.EligibilityChecked(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.Bta,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterEligibilityCheck.Eligible,
      correlationId          = dependencies.correlationId,
      taxId                  = empRef,
      eligibilityCheckResult = eligibleEligibilityCheckResult
    )

    def journeyAfterEligibilityCheckEligibleJson: JsObject = read("/testdata/epaye/bta/JourneyAfterEligibilityCheck.json").asJson

    def journeyAfterEligibilityCheckNotEligible: Journey.Epaye.EligibilityChecked = Journey.Epaye.EligibilityChecked(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.Bta,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterEligibilityCheck.Ineligible,
      correlationId          = dependencies.correlationId,
      taxId                  = empRef,
      eligibilityCheckResult = ineligibleEligibilityCheckResult
    )

    def updateCanPayUpfrontYesRequest(): CanPayUpfront = canPayUpfrontYes

    def updateCanPayUpfrontNoRequest(): CanPayUpfront = canPayUpfrontNo

    def journeyAfterCanPayUpfrontYes: Journey.Epaye.AnsweredCanPayUpfront = Journey.Epaye.AnsweredCanPayUpfront(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.Bta,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterCanPayUpfront.Yes,
      correlationId          = dependencies.correlationId,
      taxId                  = empRef,
      eligibilityCheckResult = eligibleEligibilityCheckResult,
      canPayUpfront          = canPayUpfrontYes
    )

    def journeyAfterCanPayUpfrontNo: Journey.Epaye.AnsweredCanPayUpfront = Journey.Epaye.AnsweredCanPayUpfront(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.Bta,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterCanPayUpfront.No,
      correlationId          = dependencies.correlationId,
      taxId                  = empRef,
      eligibilityCheckResult = eligibleEligibilityCheckResult,
      canPayUpfront          = canPayUpfrontNo
    )

    def updateUpfrontPaymentAmountRequest(): UpfrontPaymentAmount = dependencies.upfrontPaymentAmount

    // used in specific test for changing upfront payment amount, no need to copy to other TdJourneys
    def anotherUpdateUpfrontPaymentAmountRequest(): UpfrontPaymentAmount = dependencies.anotherUpfrontPaymentAmount

    def journeyAfterUpfrontPaymentAmount: Journey.Epaye.EnteredUpfrontPaymentAmount = Journey.Epaye.EnteredUpfrontPaymentAmount(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.Bta,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount,
      correlationId          = dependencies.correlationId,
      taxId                  = empRef,
      eligibilityCheckResult = eligibleEligibilityCheckResult,
      canPayUpfront          = canPayUpfrontYes,
      upfrontPaymentAmount   = dependencies.upfrontPaymentAmount
    )

    def updateExtremeDatesRequest(): ExtremeDatesResponse = dependencies.extremeDatesWithUpfrontPayment

    def journeyAfterExtremeDates: Journey.Epaye.RetrievedExtremeDates = Journey.Epaye.RetrievedExtremeDates(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.Bta,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved,
      correlationId          = dependencies.correlationId,
      taxId                  = empRef,
      eligibilityCheckResult = eligibleEligibilityCheckResult,
      upfrontPaymentAnswers  = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse   = dependencies.extremeDatesWithUpfrontPayment
    )

    def updateInstalmentAmountsRequest(): InstalmentAmounts = dependencies.instalmentAmounts

    def journeyAfterInstalmentAmounts: Journey.Epaye.RetrievedAffordabilityResult = Journey.Epaye.RetrievedAffordabilityResult(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.Bta,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterAffordabilityResult.RetrievedAffordabilityResult,
      correlationId          = dependencies.correlationId,
      taxId                  = empRef,
      eligibilityCheckResult = eligibleEligibilityCheckResult,
      upfrontPaymentAnswers  = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse   = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts      = dependencies.instalmentAmounts
    )

    def updateMonthlyPaymentAmountRequest(): MonthlyPaymentAmount = dependencies.monthlyPaymentAmount

    def journeyAfterMonthlyPaymentAmount: Journey.Epaye.EnteredMonthlyPaymentAmount = Journey.Epaye.EnteredMonthlyPaymentAmount(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.Bta,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount,
      correlationId          = dependencies.correlationId,
      taxId                  = empRef,
      eligibilityCheckResult = eligibleEligibilityCheckResult,
      upfrontPaymentAnswers  = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse   = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts      = dependencies.instalmentAmounts,
      monthlyPaymentAmount   = dependencies.monthlyPaymentAmount
    )

    def updateDayOfMonthRequest(): DayOfMonth = dependencies.dayOfMonth

    def journeyAfterDayOfMonth: Journey.Epaye.EnteredDayOfMonth = Journey.Epaye.EnteredDayOfMonth(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.Bta,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth,
      correlationId          = dependencies.correlationId,
      taxId                  = empRef,
      eligibilityCheckResult = eligibleEligibilityCheckResult,
      upfrontPaymentAnswers  = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse   = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts      = dependencies.instalmentAmounts,
      monthlyPaymentAmount   = dependencies.monthlyPaymentAmount,
      dayOfMonth             = dependencies.dayOfMonth
    )

    def updateStartDatesResponse(): StartDatesResponse = dependencies.startDatesResponseWithInitialPayment

    def journeyAfterStartDatesResponse: Journey.Epaye.RetrievedStartDates = Journey.Epaye.RetrievedStartDates(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.Bta,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterStartDatesResponse.StartDatesResponseRetrieved,
      correlationId          = dependencies.correlationId,
      taxId                  = empRef,
      eligibilityCheckResult = eligibleEligibilityCheckResult,
      upfrontPaymentAnswers  = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse   = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts      = dependencies.instalmentAmounts,
      monthlyPaymentAmount   = dependencies.monthlyPaymentAmount,
      dayOfMonth             = dependencies.dayOfMonth,
      startDatesResponse     = dependencies.startDatesResponseWithInitialPayment
    )

    def updateAffordableQuotesResponse(): AffordableQuotesResponse = dependencies.affordableQuotesResponse

    def journeyAfterAffordableQuotesResponse: Journey.Epaye.RetrievedAffordableQuotes = Journey.Epaye.RetrievedAffordableQuotes(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Epaye.Bta,
      createdOn                = dependencies.createdOn,
      sjRequest                = sjRequest,
      sessionId                = dependencies.sessionId,
      stage                    = Stage.AfterAffordableQuotesResponse.AffordableQuotesRetrieved,
      correlationId            = dependencies.correlationId,
      taxId                    = empRef,
      eligibilityCheckResult   = eligibleEligibilityCheckResult,
      upfrontPaymentAnswers    = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse     = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts        = dependencies.instalmentAmounts,
      monthlyPaymentAmount     = dependencies.monthlyPaymentAmount,
      dayOfMonth               = dependencies.dayOfMonth,
      startDatesResponse       = dependencies.startDatesResponseWithInitialPayment,
      affordableQuotesResponse = dependencies.affordableQuotesResponse
    )

    def updateSelectedPaymentPlanRequest(): PaymentPlan = dependencies.paymentPlan(1)

    def journeyAfterSelectedPaymentPlan: Journey.Epaye.ChosenPaymentPlan = Journey.Epaye.ChosenPaymentPlan(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Epaye.Bta,
      createdOn                = dependencies.createdOn,
      sjRequest                = sjRequest,
      sessionId                = dependencies.sessionId,
      stage                    = Stage.AfterSelectedPlan.SelectedPlan,
      correlationId            = dependencies.correlationId,
      taxId                    = empRef,
      eligibilityCheckResult   = eligibleEligibilityCheckResult,
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

    def journeyAfterCheckedPaymentPlan: Journey.Epaye.CheckedPaymentPlan = Journey.Epaye.CheckedPaymentPlan(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Epaye.Bta,
      createdOn                = dependencies.createdOn,
      sjRequest                = sjRequest,
      sessionId                = dependencies.sessionId,
      stage                    = Stage.AfterCheckedPlan.AcceptedPlan,
      correlationId            = dependencies.correlationId,
      taxId                    = empRef,
      eligibilityCheckResult   = eligibleEligibilityCheckResult,
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

    def journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder: Boolean): Journey.Epaye.EnteredDetailsAboutBankAccount = Journey.Epaye.EnteredDetailsAboutBankAccount(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Epaye.Bta,
      createdOn                = dependencies.createdOn,
      sjRequest                = sjRequest,
      sessionId                = dependencies.sessionId,
      stage                    = if (isAccountHolder) Stage.AfterEnteredDetailsAboutBankAccount.Business else Stage.AfterEnteredDetailsAboutBankAccount.IsNotAccountHolder,
      correlationId            = dependencies.correlationId,
      taxId                    = empRef,
      eligibilityCheckResult   = eligibleEligibilityCheckResult,
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

    val updateDirectDebitDetailsRequest: BankDetails = dependencies.directDebitDetails

    def journeyAfterEnteredDirectDebitDetails(): Journey.Epaye.EnteredDirectDebitDetails = Journey.Epaye.EnteredDirectDebitDetails(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Epaye.Bta,
      createdOn                = dependencies.createdOn,
      sjRequest                = sjRequest,
      sessionId                = dependencies.sessionId,
      stage                    = Stage.AfterEnteredDirectDebitDetails.EnteredDirectDebitDetails,
      correlationId            = dependencies.correlationId,
      taxId                    = empRef,
      eligibilityCheckResult   = eligibleEligibilityCheckResult,
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

    def journeyAfterConfirmedDirectDebitDetails: Journey.Epaye.ConfirmedDirectDebitDetails = Journey.Epaye.ConfirmedDirectDebitDetails(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Epaye.Bta,
      createdOn                = dependencies.createdOn,
      sjRequest                = sjRequest,
      sessionId                = dependencies.sessionId,
      stage                    = Stage.AfterConfirmedDirectDebitDetails.ConfirmedDetails,
      correlationId            = dependencies.correlationId,
      taxId                    = empRef,
      eligibilityCheckResult   = eligibleEligibilityCheckResult,
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

    def journeyAfterAgreedTermsAndConditions(isEmailAddressRequired: Boolean): Journey.Epaye.AgreedTermsAndConditions = {
      val stage =
        if (isEmailAddressRequired) Stage.AfterAgreedTermsAndConditions.EmailAddressRequired
        else Stage.AfterAgreedTermsAndConditions.EmailAddressNotRequired

      Journey.Epaye.AgreedTermsAndConditions(
        _id                      = dependencies.journeyId,
        origin                   = Origins.Epaye.Bta,
        createdOn                = dependencies.createdOn,
        sjRequest                = sjRequest,
        sessionId                = dependencies.sessionId,
        stage                    = stage,
        correlationId            = dependencies.correlationId,
        taxId                    = empRef,
        eligibilityCheckResult   = eligibleEligibilityCheckResult,
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
      origin                   = Origins.Epaye.Bta,
      createdOn                = dependencies.createdOn,
      sjRequest                = sjRequest,
      sessionId                = dependencies.sessionId,
      stage                    = Stage.AfterSelectedAnEmailToBeVerified.EmailChosen,
      correlationId            = dependencies.correlationId,
      taxId                    = empRef,
      eligibilityCheckResult   = eligibleEligibilityCheckResult,
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

    def journeyAfterEmailVerificationStatus(status: EmailVerificationStatus): Journey.Epaye.EmailVerificationComplete = Journey.Epaye.EmailVerificationComplete(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Epaye.Bta,
      createdOn                = dependencies.createdOn,
      sjRequest                = sjRequest,
      sessionId                = dependencies.sessionId,
      stage                    = status match {
        case EmailVerificationStatus.Verified => Stage.AfterEmailVerificationPhase.VerificationSuccess
        case EmailVerificationStatus.Locked   => Stage.AfterEmailVerificationPhase.Locked
      },
      correlationId            = dependencies.correlationId,
      taxId                    = empRef,
      eligibilityCheckResult   = eligibleEligibilityCheckResult,
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
      emailVerificationStatus  = status,
      emailVerificationAnswers = emailVerificationAnswers(Some(status))
    )

    def updateArrangementRequest(): ArrangementResponse = dependencies.arrangementResponse

    def journeyAfterSubmittedArrangement(isEmailAddressRequired: Boolean): Journey.Epaye.SubmittedArrangement = Journey.Epaye.SubmittedArrangement(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Epaye.Bta,
      createdOn                = dependencies.createdOn,
      sjRequest                = sjRequest,
      sessionId                = dependencies.sessionId,
      stage                    = Stage.AfterSubmittedArrangement.Submitted,
      correlationId            = dependencies.correlationId,
      taxId                    = empRef,
      eligibilityCheckResult   = eligibleEligibilityCheckResult,
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
        EmailVerificationAnswers.EmailVerified(dependencies.email, EmailVerificationStatus.Verified)
      } else {
        EmailVerificationAnswers.NoEmailJourney
      },
      arrangementResponse      = dependencies.arrangementResponse
    )

  }
}
