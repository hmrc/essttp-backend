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

import scala.language.reflectiveCalls
import essttp.rootmodel._
import essttp.rootmodel.bank.{DirectDebitDetails, TypeOfBankAccount}
import essttp.rootmodel.dates.extremedates.ExtremeDatesResponse
import essttp.rootmodel.dates.startdates.StartDatesResponse
import essttp.rootmodel.ttp.EligibilityCheckResult
import essttp.rootmodel.ttp.affordability.InstalmentAmounts
import essttp.rootmodel.ttp.affordablequotes.{AffordableQuotesResponse, PaymentPlan}
import essttp.rootmodel.ttp.arrangement.ArrangementResponse
import essttp.utils.JsonSyntax._
import essttp.utils.ResourceReader._
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
      nextUrl   = NextUrl(s"http://localhost:9215/set-up-a-payment-plan?traceId=${dependencies.traceId.value}"),
      journeyId = dependencies.journeyId
    )

    def postPath: String = "/epaye/bta/journey/start"

    def sjRequestJson: JsObject = read("/testdata/epaye/bta/SjRequest.json").asJson

    def journeyAfterStarted: Journey.Epaye.Started = Journey.Epaye.Started(
      _id           = dependencies.journeyId,
      origin        = Origins.Epaye.Bta,
      createdOn     = dependencies.createdOn,
      sjRequest     = sjRequest,
      sessionId     = dependencies.sessionId,
      stage         = Stage.AfterStarted.Started,
      correlationId = dependencies.correlationId,
    )

    def journeyAfterStartedJson: JsObject = read("/testdata/epaye/bta/JourneyAfterStarted.json").asJson

    def updateTaxIdRequest(): TaxId = empRef

    def updateTaxIdRequestJson(): JsObject = read("/testdata/epaye/bta/UpdateTaxIdRequest.json").asJson

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

    def journeyAfterDetermineTaxIdsJson: JsObject = read("testdata/epaye/bta/JourneyAfterComputedTaxIds.json").asJson

    def updateEligibilityCheckRequest(): EligibilityCheckResult = eligibleEligibilityCheckResult

    def updateEligibilityCheckRequestJson(): JsObject = read("/testdata/epaye/bta/UpdateEligibilityCheckRequest.json").asJson

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

    def journeyAfterEligibilityCheckNotEligibleJson: JsObject = read("/testdata/epaye/bta/JourneyAfterEligibilityCheckNotEligible.json").asJson

    def updateCanPayUpfrontYesRequest(): CanPayUpfront = canPayUpfrontYes

    def updateCanPayUpfrontNoRequest(): CanPayUpfront = canPayUpfrontNo

    def updateCanPayUpfrontYesRequestJson(): JsObject = read("/testdata/epaye/bta/UpdateCanPayUpfrontYes.json").asJson

    def updateCanPayUpfrontNoRequestJson(): JsObject = read("/testdata/epaye/bta/UpdateCanPayUpfrontNo.json").asJson

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

    def journeyAfterCanPayUpfrontYesJson: JsObject = read("/testdata/epaye/bta/JourneyAfterCanPayUpfrontYes.json").asJson

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

    def journeyAfterCanPayUpfrontNoJson: JsObject = read("/testdata/epaye/bta/JourneyAfterCanPayUpfrontNo.json").asJson

    def updateUpfrontPaymentAmountRequest(): UpfrontPaymentAmount = dependencies.upfrontPaymentAmount

    // used in specific test for changing upfront payment amount, no need to copy to other TdJourneys
    def anotherUpdateUpfrontPaymentAmountRequest(): UpfrontPaymentAmount = dependencies.anotherUpfrontPaymentAmount

    def updateUpfrontPaymentAmountRequestJson(): JsObject = read("/testdata/epaye/bta/UpdateUpfrontPaymentAmountRequest.json").asJson

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

    def journeyAfterUpfrontPaymentAmountJson: JsObject = read("/testdata/epaye/bta/JourneyAfterUpdateUpfrontPaymentAmount.json").asJson

    def updateExtremeDatesRequest(): ExtremeDatesResponse = dependencies.extremeDatesWithUpfrontPayment

    def updateExtremeDatesRequestJson(): JsObject = read("/testdata/epaye/bta/UpdateExtremeDatesRequest.json").asJson

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

    def journeyAfterExtremeDatesJson: JsObject = read("/testdata/epaye/bta/JourneyAfterUpdateExtremeDates.json").asJson

    def updateInstalmentAmountsRequest(): InstalmentAmounts = dependencies.instalmentAmounts

    def updateInstalmentAmountsRequestJson(): JsObject = read("/testdata/epaye/bta/UpdateInstalmentAmountsRequest.json").asJson

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

    def journeyAfterInstalmentAmountsJson: JsObject = read("/testdata/epaye/bta/JourneyAfterUpdateInstalmentAmounts.json").asJson

    def updateMonthlyPaymentAmountRequest(): MonthlyPaymentAmount = dependencies.monthlyPaymentAmount

    def updateMonthlyPaymentAmountRequestJson(): JsObject = read("/testdata/epaye/bta/UpdateMonthlyPaymentAmountRequest.json").asJson

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

    def journeyAfterMonthlyPaymentAmountJson: JsObject = read("/testdata/epaye/bta/JourneyAfterUpdateMonthlyPaymentAmount.json").asJson

    def updateDayOfMonthRequest(): DayOfMonth = dependencies.dayOfMonth

    def updateDayOfMonthRequestJson(): JsObject = read("/testdata/epaye/bta/UpdateDayOfMonthRequest.json").asJson

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

    def journeyAfterDayOfMonthJson: JsObject = read("/testdata/epaye/bta/JourneyAfterUpdateDayOfMonth.json").asJson

    def updateStartDatesResponse(): StartDatesResponse = dependencies.startDatesResponseWithInitialPayment

    def updateStartDatesResponseJson(): JsObject = read("/testdata/epaye/bta/UpdateStartDatesResponse.json").asJson

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

    def journeyAfterStartDatesResponseJson: JsObject = read("/testdata/epaye/bta/JourneyAfterUpdateStartDatesResponse.json").asJson

    def updateAffordableQuotesResponse(): AffordableQuotesResponse = dependencies.affordableQuotesResponse

    def updateAffordableQuotesResponseJson(): JsObject = read("/testdata/epaye/bta/UpdateAffordableQuotesRequest.json").asJson

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

    def journeyAfterAffordableQuotesResponseJson: JsObject = read("/testdata/epaye/bta/JourneyAfterUpdateAffordableQuotesResponse.json").asJson

    def updateSelectedPaymentPlanRequest(): PaymentPlan = dependencies.paymentPlan(1)

    def updateSelectedPaymentPlanRequestJson(): JsObject = read("/testdata/epaye/bta/UpdateSelectedPaymentPlanRequest.json").asJson

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

    def journeyAfterSelectedPaymentPlanJson: JsObject = read("/testdata/epaye/bta/JourneyAfterSelectedPaymentPlan.json").asJson

    def updateCheckedPaymentPlanRequest(): JsNull.type = JsNull

    def updateCheckedPaymentPlanRequestJson(): JsObject = read("/testdata/epaye/bta/UpdateCheckedPaymentPlanRequest.json").asJson

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

    def journeyAfterCheckedPaymentPlanJson: JsObject = read("/testdata/epaye/bta/JourneyAfterCheckedPaymentPlan.json").asJson

    def updateChosenTypeOfBankAccountRequest(): TypeOfBankAccount = dependencies.businessBankAccount

    def updateChosenTypeOfBankAccountRequestJson(): JsObject = read("/testdata/epaye/bta/JourneyAfterChosenTypeOfBankAccount.json").asJson

    def journeyAfterChosenTypeOfBankAccount: Journey.Epaye.ChosenTypeOfBankAccount = Journey.Epaye.ChosenTypeOfBankAccount(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Epaye.Bta,
      createdOn                = dependencies.createdOn,
      sjRequest                = sjRequest,
      sessionId                = dependencies.sessionId,
      stage                    = Stage.AfterChosenTypeOfBankAccount.Business,
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
      typeOfBankAccount        = dependencies.businessBankAccount
    )

    def journeyAfterChosenTypeOfBankAccountJson: JsObject = read("/testdata/epaye/bta/UpdateTypeOfBankAccountRequest.json").asJson

    def updateDirectDebitDetailsRequest(isAccountHolder: Boolean): DirectDebitDetails = dependencies.directDebitDetails(isAccountHolder)

    def updateDirectDebitDetailsRequestJson(): JsObject = read("/testdata/epaye/bta/UpdateDirectDebitDetailsRequest.json").asJson

    def journeyAfterEnteredDirectDebitDetails(isAccountHolder: Boolean): Journey.Epaye.EnteredDirectDebitDetails = Journey.Epaye.EnteredDirectDebitDetails(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Epaye.Bta,
      createdOn                = dependencies.createdOn,
      sjRequest                = sjRequest,
      sessionId                = dependencies.sessionId,
      stage                    = if (isAccountHolder) Stage.AfterEnteredDirectDebitDetails.IsAccountHolder else Stage.AfterEnteredDirectDebitDetails.IsNotAccountHolder,
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
      typeOfBankAccount        = dependencies.businessBankAccount,
      directDebitDetails       = directDebitDetails(isAccountHolder)
    )

    def journeyAfterEnteredDirectDebitDetailsJson: JsObject = read("/testdata/epaye/bta/JourneyAfterUpdateDirectDebitDetails.json").asJson

    def updateConfirmedDirectDebitDetailsRequest(): JsNull.type = JsNull

    def updateConfirmedDirectDebitDetailsJson(): JsObject = read("/testdata/epaye/bta/UpdateConfirmedDirectDebitDetailsRequest.json").asJson

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
      typeOfBankAccount        = dependencies.businessBankAccount,
      directDebitDetails       = directDebitDetails(true)
    )

    def journeyAfterConfirmedDirectDebitDetailsJson: JsObject = read("/testdata/epaye/bta/JourneyAfterUpdateConfirmedDirectDebitDetails.json").asJson

    def updateAgreedTermsAndConditionsRequest(): JsNull.type = JsNull

    def updateAgreedTermsAndConditionsJson(): JsObject = read("/testdata/epaye/bta/UpdateAgreedTermsAndConditions.json").asJson

    def journeyAfterAgreedTermsAndConditions: Journey.Epaye.AgreedTermsAndConditions = Journey.Epaye.AgreedTermsAndConditions(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Epaye.Bta,
      createdOn                = dependencies.createdOn,
      sjRequest                = sjRequest,
      sessionId                = dependencies.sessionId,
      stage                    = Stage.AfterAgreedTermsAndConditions.Agreed,
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
      typeOfBankAccount        = dependencies.businessBankAccount,
      directDebitDetails       = directDebitDetails(true)
    )

    def journeyAfterAgreedTermsAndConditionsJson: JsObject = read("/testdata/epaye/bta/JourneyAfterUpdateAgreedTermsAndConditions.json").asJson

    def updateArrangementRequest(): ArrangementResponse = dependencies.arrangementResponse

    def updateArrangementRequestJson(): JsObject = read("/testdata/epaye/bta/UpdateSubmittedArrangementRequest.json").asJson

    def journeyAfterSubmittedArrangement: Journey.Epaye.SubmittedArrangement = Journey.Epaye.SubmittedArrangement(
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
      typeOfBankAccount        = dependencies.businessBankAccount,
      directDebitDetails       = directDebitDetails(true),
      arrangementResponse      = dependencies.arrangementResponse
    )
    def journeyAfterSubmittedArrangementJson: JsObject = read("/testdata/epaye/bta/JourneyAfterUpdateSubmittedArrangement.json").asJson
  }
}
