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

package essttp.testdata

import essttp.journey.model.SjRequest.Epaye
import essttp.journey.model._
import essttp.rootmodel.bank.{BankDetails, DetailsAboutBankAccount}
import essttp.rootmodel.dates.extremedates.ExtremeDatesResponse
import essttp.rootmodel.dates.startdates.StartDatesResponse
import essttp.rootmodel.ttp.EligibilityCheckResult
import essttp.rootmodel.ttp.affordability.InstalmentAmounts
import essttp.rootmodel.ttp.affordablequotes.{AffordableQuotesResponse, PaymentPlan}
import essttp.rootmodel.ttp.arrangement.ArrangementResponse
import essttp.rootmodel.{CanPayUpfront, DayOfMonth, MonthlyPaymentAmount, TaxId, UpfrontPaymentAmount}
import essttp.utils.JsonSyntax._
import essttp.utils.ResourceReader._
import play.api.libs.json.{JsNull, JsObject}

import scala.language.reflectiveCalls

trait TdJourneyEpayeGovUk { dependencies: TdBase with TdEpaye =>

  object EpayeGovUk extends TdJourneyStructure {

    def sjRequest: Epaye.Empty = SjRequest.Epaye.Empty()

    def sjResponse: SjResponse = SjResponse(
      nextUrl   = NextUrl(s"http://localhost:9215/set-up-a-payment-plan?traceId=${dependencies.traceId.value}"),
      journeyId = dependencies.journeyId
    )

    def postPath: String = "/epaye/gov-uk/journey/start"
    def sjRequestJson: JsObject = read("testdata/epaye/govuk/SjRequest.json").asJson

    def journeyAfterStarted: Journey.Epaye.Started = Journey.Epaye.Started(
      _id           = dependencies.journeyId,
      origin        = Origins.Epaye.GovUk,
      createdOn     = dependencies.createdOn,
      sjRequest     = sjRequest,
      sessionId     = dependencies.sessionId,
      correlationId = dependencies.correlationId,
      stage         = Stage.AfterStarted.Started
    )

    def journeyAfterStartedJson: JsObject = read("testdata/epaye/govuk/JourneyAfterStarted.json").asJson

    def updateTaxIdRequest(): TaxId = empRef

    def updateTaxIdRequestJson(): JsObject = read("testdata/epaye/govuk/UpdateTaxIdRequest.json").asJson

    def journeyAfterDetermineTaxIds: Journey.Epaye.ComputedTaxId = Journey.Epaye.ComputedTaxId(
      _id           = dependencies.journeyId,
      origin        = Origins.Epaye.GovUk,
      createdOn     = dependencies.createdOn,
      sjRequest     = sjRequest,
      sessionId     = dependencies.sessionId,
      correlationId = dependencies.correlationId,
      stage         = Stage.AfterComputedTaxId.ComputedTaxId,
      taxId         = empRef
    )

    def journeyAfterDetermineTaxIdsJson: JsObject = read("testdata/epaye/govuk/JourneyAfterComputedTaxIds.json").asJson

    def updateEligibilityCheckRequest(): EligibilityCheckResult = eligibleEligibilityCheckResult

    def updateEligibilityCheckRequestJson(): JsObject = read("testdata/epaye/govuk/UpdateEligibilityCheckRequest.json").asJson

    def journeyAfterEligibilityCheckEligible: Journey.Epaye.EligibilityChecked = Journey.Epaye.EligibilityChecked(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.GovUk,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      correlationId          = dependencies.correlationId,
      stage                  = Stage.AfterEligibilityCheck.Eligible,
      taxId                  = empRef,
      eligibilityCheckResult = eligibleEligibilityCheckResult
    )

    def journeyAfterEligibilityCheckEligibleJson: JsObject = read("testdata/epaye/govuk/JourneyAfterEligibilityCheck.json").asJson

    def journeyAfterEligibilityCheckNotEligible: Journey.Epaye.EligibilityChecked = Journey.Epaye.EligibilityChecked(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.GovUk,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      correlationId          = dependencies.correlationId,
      stage                  = Stage.AfterEligibilityCheck.Ineligible,
      taxId                  = empRef,
      eligibilityCheckResult = ineligibleEligibilityCheckResult
    )

    def journeyAfterEligibilityCheckNotEligibleJson: JsObject = read("testdata/epaye/govuk/JourneyAfterEligibilityCheckNotEligible.json").asJson

    def updateCanPayUpfrontYesRequest(): CanPayUpfront = canPayUpfrontYes

    def updateCanPayUpfrontNoRequest(): CanPayUpfront = canPayUpfrontNo

    def updateCanPayUpfrontYesRequestJson(): JsObject = read("/testdata/epaye/govuk/UpdateCanPayUpfrontYes.json").asJson

    def updateCanPayUpfrontNoRequestJson(): JsObject = read("/testdata/epaye/govuk/UpdateCanPayUpfrontNo.json").asJson

    def journeyAfterCanPayUpfrontYes: Journey.Epaye.AnsweredCanPayUpfront = Journey.Epaye.AnsweredCanPayUpfront(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.GovUk,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      correlationId          = dependencies.correlationId,
      stage                  = Stage.AfterCanPayUpfront.Yes,
      taxId                  = empRef,
      eligibilityCheckResult = eligibleEligibilityCheckResult,
      canPayUpfront          = canPayUpfrontYes
    )

    def journeyAfterCanPayUpfrontNo: Journey.Epaye.AnsweredCanPayUpfront = Journey.Epaye.AnsweredCanPayUpfront(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.GovUk,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      correlationId          = dependencies.correlationId,
      stage                  = Stage.AfterCanPayUpfront.No,
      taxId                  = empRef,
      eligibilityCheckResult = eligibleEligibilityCheckResult,
      canPayUpfront          = canPayUpfrontNo
    )

    override def journeyAfterCanPayUpfrontYesJson: JsObject = read("/testdata/epaye/govuk/JourneyAfterCanPayUpfrontYes.json").asJson

    override def journeyAfterCanPayUpfrontNoJson: JsObject = read("/testdata/epaye/govuk/JourneyAfterCanPayUpfrontNo.json").asJson

    override def updateUpfrontPaymentAmountRequest(): UpfrontPaymentAmount = dependencies.upfrontPaymentAmount

    override def updateUpfrontPaymentAmountRequestJson(): JsObject = read("/testdata/epaye/govuk/UpdateUpfrontPaymentAmountRequest.json").asJson

    override def journeyAfterUpfrontPaymentAmount: Journey.Epaye.EnteredUpfrontPaymentAmount = Journey.Epaye.EnteredUpfrontPaymentAmount(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.GovUk,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      correlationId          = dependencies.correlationId,
      stage                  = Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount,
      taxId                  = empRef,
      eligibilityCheckResult = eligibleEligibilityCheckResult,
      canPayUpfront          = canPayUpfrontYes,
      upfrontPaymentAmount   = dependencies.upfrontPaymentAmount
    )

    override def journeyAfterUpfrontPaymentAmountJson: JsObject = read("/testdata/epaye/govuk/JourneyAfterUpdateUpfrontPaymentAmount.json").asJson

    def updateExtremeDatesRequest(): ExtremeDatesResponse = dependencies.extremeDatesWithUpfrontPayment

    def updateExtremeDatesRequestJson(): JsObject = read("/testdata/epaye/govuk/UpdateExtremeDatesRequest.json").asJson

    def journeyAfterExtremeDates: Journey.Epaye.RetrievedExtremeDates = Journey.Epaye.RetrievedExtremeDates(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.GovUk,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      correlationId          = dependencies.correlationId,
      stage                  = Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved,
      taxId                  = empRef,
      eligibilityCheckResult = eligibleEligibilityCheckResult,
      upfrontPaymentAnswers  = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse   = dependencies.extremeDatesWithUpfrontPayment
    )

    def journeyAfterExtremeDatesJson: JsObject = read("/testdata/epaye/govuk/JourneyAfterUpdateExtremeDates.json").asJson

    def updateInstalmentAmountsRequest(): InstalmentAmounts = dependencies.instalmentAmounts

    def updateInstalmentAmountsRequestJson(): JsObject = read("/testdata/epaye/govuk/UpdateInstalmentAmountsRequest.json").asJson

    def journeyAfterInstalmentAmounts: Journey.Epaye.RetrievedAffordabilityResult = Journey.Epaye.RetrievedAffordabilityResult(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.GovUk,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      correlationId          = dependencies.correlationId,
      stage                  = Stage.AfterAffordabilityResult.RetrievedAffordabilityResult,
      taxId                  = empRef,
      eligibilityCheckResult = eligibleEligibilityCheckResult,
      upfrontPaymentAnswers  = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse   = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts      = dependencies.instalmentAmounts
    )

    def journeyAfterInstalmentAmountsJson: JsObject = read("/testdata/epaye/govuk/JourneyAfterUpdateInstalmentAmounts.json").asJson

    def updateMonthlyPaymentAmountRequest(): MonthlyPaymentAmount = dependencies.monthlyPaymentAmount

    def updateMonthlyPaymentAmountRequestJson(): JsObject = read("/testdata/epaye/govuk/UpdateMonthlyPaymentAmountRequest.json").asJson

    def journeyAfterMonthlyPaymentAmount: Journey.Epaye.EnteredMonthlyPaymentAmount = Journey.Epaye.EnteredMonthlyPaymentAmount(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.GovUk,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      correlationId          = dependencies.correlationId,
      stage                  = Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount,
      taxId                  = empRef,
      eligibilityCheckResult = eligibleEligibilityCheckResult,
      upfrontPaymentAnswers  = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse   = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts      = dependencies.instalmentAmounts,
      monthlyPaymentAmount   = dependencies.monthlyPaymentAmount
    )

    def journeyAfterMonthlyPaymentAmountJson: JsObject = read("/testdata/epaye/govuk/JourneyAfterUpdateMonthlyPaymentAmount.json").asJson

    def updateDayOfMonthRequest(): DayOfMonth = dependencies.dayOfMonth

    def updateDayOfMonthRequestJson(): JsObject = read("/testdata/epaye/govuk/UpdateDayOfMonthRequest.json").asJson

    def journeyAfterDayOfMonth: Journey.Epaye.EnteredDayOfMonth = Journey.Epaye.EnteredDayOfMonth(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.GovUk,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      correlationId          = dependencies.correlationId,
      stage                  = Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth,
      taxId                  = empRef,
      eligibilityCheckResult = eligibleEligibilityCheckResult,
      upfrontPaymentAnswers  = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse   = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts      = dependencies.instalmentAmounts,
      monthlyPaymentAmount   = dependencies.monthlyPaymentAmount,
      dayOfMonth             = dependencies.dayOfMonth
    )

    def journeyAfterDayOfMonthJson: JsObject = read("/testdata/epaye/govuk/JourneyAfterUpdateDayOfMonth.json").asJson

    def updateStartDatesResponse(): StartDatesResponse = dependencies.startDatesResponseWithInitialPayment

    def updateStartDatesResponseJson(): JsObject = read("/testdata/epaye/govuk/UpdateStartDatesResponse.json").asJson

    def journeyAfterStartDatesResponse: Journey.AfterStartDatesResponse = Journey.Epaye.RetrievedStartDates(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.GovUk,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      correlationId          = dependencies.correlationId,
      stage                  = Stage.AfterStartDatesResponse.StartDatesResponseRetrieved,
      taxId                  = empRef,
      eligibilityCheckResult = eligibleEligibilityCheckResult,
      upfrontPaymentAnswers  = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse   = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts      = dependencies.instalmentAmounts,
      monthlyPaymentAmount   = dependencies.monthlyPaymentAmount,
      dayOfMonth             = dependencies.dayOfMonth,
      startDatesResponse     = dependencies.startDatesResponseWithInitialPayment
    )

    def journeyAfterStartDatesResponseJson: JsObject = read("/testdata/epaye/govuk/JourneyAfterUpdateStartDatesResponse.json").asJson

    def updateAffordableQuotesResponse(): AffordableQuotesResponse = dependencies.affordableQuotesResponse

    def updateAffordableQuotesResponseJson(): JsObject = read("/testdata/epaye/govuk/UpdateAffordableQuotesRequest.json").asJson

    def journeyAfterAffordableQuotesResponse: Journey.AfterAffordableQuotesResponse = Journey.Epaye.RetrievedAffordableQuotes(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Epaye.GovUk,
      createdOn                = dependencies.createdOn,
      sjRequest                = sjRequest,
      sessionId                = dependencies.sessionId,
      correlationId            = dependencies.correlationId,
      stage                    = Stage.AfterAffordableQuotesResponse.AffordableQuotesRetrieved,
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

    def journeyAfterAffordableQuotesResponseJson: JsObject = read("/testdata/epaye/govuk/JourneyAfterUpdateAffordableQuotesResponse.json").asJson

    def updateSelectedPaymentPlanRequest(): PaymentPlan = dependencies.paymentPlan(1)

    def updateSelectedPaymentPlanRequestJson(): JsObject = read("/testdata/epaye/bta/UpdateSelectedPaymentPlanRequest.json").asJson

    def journeyAfterSelectedPaymentPlan: Journey.AfterSelectedPaymentPlan = Journey.Epaye.ChosenPaymentPlan(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Epaye.GovUk,
      createdOn                = dependencies.createdOn,
      sjRequest                = sjRequest,
      sessionId                = dependencies.sessionId,
      correlationId            = dependencies.correlationId,
      stage                    = Stage.AfterSelectedPlan.SelectedPlan,
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

    def journeyAfterSelectedPaymentPlanJson: JsObject = read("/testdata/epaye/govuk/JourneyAfterSelectedPaymentPlan.json").asJson

    def updateCheckedPaymentPlanRequest(): JsNull.type = JsNull

    def updateCheckedPaymentPlanRequestJson(): JsObject = read("/testdata/epaye/govuk/UpdateCheckedPaymentPlanRequest.json").asJson

    def journeyAfterCheckedPaymentPlan: Journey.AfterCheckedPaymentPlan = Journey.Epaye.CheckedPaymentPlan(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Epaye.GovUk,
      createdOn                = dependencies.createdOn,
      sjRequest                = sjRequest,
      sessionId                = dependencies.sessionId,
      correlationId            = dependencies.correlationId,
      stage                    = Stage.AfterCheckedPlan.AcceptedPlan,
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

    def journeyAfterCheckedPaymentPlanJson: JsObject = read("/testdata/epaye/govuk/JourneyAfterCheckedPaymentPlan.json").asJson

    def updateDetailsAboutBankAccountRequest(isAccountHolder: Boolean): DetailsAboutBankAccount =
      DetailsAboutBankAccount(dependencies.businessBankAccount, isAccountHolder)

    def updateDetailsAboutBankAccountRequestJson(): JsObject = read("/testdata/epaye/govuk/JourneyAfterEnteredDetailsAboutBankAccount.json").asJson

    def journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder: Boolean): Journey.AfterEnteredDetailsAboutBankAccount = Journey.Epaye.EnteredDetailsAboutBankAccount(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Epaye.GovUk,
      createdOn                = dependencies.createdOn,
      sjRequest                = sjRequest,
      sessionId                = dependencies.sessionId,
      correlationId            = dependencies.correlationId,
      stage                    = if (isAccountHolder) Stage.AfterEnteredDetailsAboutBankAccount.Business else Stage.AfterEnteredDetailsAboutBankAccount.IsNotAccountHolder,
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

    def journeyAfterEnteredDetailsAboutBankAccountJson: JsObject = read("/testdata/epaye/govuk/JourneyAfterUpdateDetailsAboutBankAccountRequest.json").asJson

    def updateDirectDebitDetailsRequest(): BankDetails = dependencies.directDebitDetails

    def updateDirectDebitDetailsRequestJson(): JsObject = read("/testdata/epaye/govuk/UpdateDirectDebitDetailsRequest.json").asJson

    def journeyAfterEnteredDirectDebitDetails(): Journey.AfterEnteredDirectDebitDetails = Journey.Epaye.EnteredDirectDebitDetails(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Epaye.GovUk,
      createdOn                = dependencies.createdOn,
      sjRequest                = sjRequest,
      sessionId                = dependencies.sessionId,
      correlationId            = dependencies.correlationId,
      stage                    = Stage.AfterEnteredDirectDebitDetails.EnteredDirectDebitDetails,
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

    override def journeyAfterEnteredDirectDebitDetailsJson: JsObject = read("/testdata/epaye/govuk/JourneyAfterUpdateDirectDebitDetails.json").asJson

    override def updateConfirmedDirectDebitDetailsRequest(): JsNull.type = JsNull

    override def updateConfirmedDirectDebitDetailsJson(): JsObject = read("/testdata/epaye/govuk/UpdateConfirmedDirectDebitDetailsRequest.json").asJson

    override def journeyAfterConfirmedDirectDebitDetails: Journey.AfterConfirmedDirectDebitDetails = Journey.Epaye.ConfirmedDirectDebitDetails(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Epaye.GovUk,
      createdOn                = dependencies.createdOn,
      sjRequest                = sjRequest,
      sessionId                = dependencies.sessionId,
      correlationId            = dependencies.correlationId,
      stage                    = Stage.AfterConfirmedDirectDebitDetails.ConfirmedDetails,
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

    override def journeyAfterConfirmedDirectDebitDetailsJson: JsObject = read("/testdata/epaye/govuk/JourneyAfterUpdateConfirmedDirectDebitDetails.json").asJson

    def updateAgreedTermsAndConditionsRequest(): JsNull.type = JsNull

    def updateAgreedTermsAndConditionsJson(): JsObject = read("/testdata/epaye/govuk/UpdateAgreedTermsAndConditions.json").asJson

    def journeyAfterAgreedTermsAndConditions: Journey.AfterAgreedTermsAndConditions = Journey.Epaye.AgreedTermsAndConditions(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Epaye.GovUk,
      createdOn                = dependencies.createdOn,
      sjRequest                = sjRequest,
      sessionId                = dependencies.sessionId,
      correlationId            = dependencies.correlationId,
      stage                    = Stage.AfterAgreedTermsAndConditions.Agreed,
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

    def journeyAfterAgreedTermsAndConditionsJson: JsObject = read("/testdata/epaye/govuk/JourneyAfterUpdateAgreedTermsAndConditions.json").asJson

    def updateArrangementRequest(): ArrangementResponse = dependencies.arrangementResponse

    def updateArrangementRequestJson(): JsObject = read("/testdata/epaye/govuk/UpdateSubmittedArrangementRequest.json").asJson

    def journeyAfterSubmittedArrangement: Journey.AfterArrangementSubmitted = Journey.Epaye.SubmittedArrangement(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Epaye.GovUk,
      createdOn                = dependencies.createdOn,
      sjRequest                = sjRequest,
      sessionId                = dependencies.sessionId,
      correlationId            = dependencies.correlationId,
      stage                    = Stage.AfterSubmittedArrangement.Submitted,
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
      arrangementResponse      = dependencies.arrangementResponse
    )
    def journeyAfterSubmittedArrangementJson: JsObject = read("/testdata/epaye/govuk/JourneyAfterUpdateSubmittedArrangement.json").asJson
  }
}
