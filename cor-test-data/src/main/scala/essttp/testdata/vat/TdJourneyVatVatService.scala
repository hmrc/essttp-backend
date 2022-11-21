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

package essttp.testdata.vat

import essttp.journey.model.SjRequest.Vat
import essttp.journey.model.{Journey, NextUrl, Origins, SjRequest, SjResponse, Stage}
import essttp.rootmodel.bank.DetailsAboutBankAccount
import essttp.rootmodel.dates.extremedates.ExtremeDatesResponse
import essttp.rootmodel.dates.startdates.StartDatesResponse
import essttp.rootmodel.{CanPayUpfront, DayOfMonth, MonthlyPaymentAmount, TaxId, UpfrontPaymentAmount}
import essttp.rootmodel.ttp.EligibilityCheckResult
import essttp.rootmodel.ttp.affordability.InstalmentAmounts
import essttp.rootmodel.ttp.affordablequotes.{AffordableQuotesResponse, PaymentPlan}
import essttp.testdata.TdBase
import essttp.utils.ResourceReader.read
import play.api.libs.json.{JsNull, JsObject}
import essttp.utils.JsonSyntax._

import scala.language.reflectiveCalls

trait TdJourneyVatVatService {
  dependencies: TdBase with TdVat =>
  object VatVatService { // todo uncomment this when we are closer to finishing vat... //extends TdJourneyStructure {
    def sjRequest: Vat.Simple = SjRequest.Vat.Simple(
      dependencies.returnUrl,
      dependencies.backUrl
    )

    def sjResponse: SjResponse = SjResponse(
      nextUrl   = NextUrl(s"http://localhost:9215/set-up-a-payment-plan/vat-payment-plan"),
      journeyId = dependencies.journeyId
    )

    def postPath: String = "/vat/vatservice/journey/start"

    def sjRequestJson: JsObject = read("/testdata/vat/vatservice/SjRequest.json").asJson

    def journeyAfterStarted: Journey.Vat.Started = Journey.Vat.Started(
      _id           = dependencies.journeyId,
      origin        = Origins.Vat.VatService,
      createdOn     = dependencies.createdOn,
      sjRequest     = sjRequest,
      sessionId     = dependencies.sessionId,
      stage         = Stage.AfterStarted.Started,
      correlationId = dependencies.correlationId,
    )

    def journeyAfterStartedJson: JsObject = read("/testdata/vat/vatservice/JourneyAfterStarted.json").asJson

    def updateTaxIdRequest(): TaxId = vrn

    def updateTaxIdRequestJson(): JsObject = read("/testdata/vat/vatservice/UpdateTaxIdRequest.json").asJson

    def journeyAfterDetermineTaxIds: Journey.Vat.ComputedTaxId = Journey.Vat.ComputedTaxId(
      _id           = dependencies.journeyId,
      origin        = Origins.Vat.VatService,
      createdOn     = dependencies.createdOn,
      sjRequest     = sjRequest,
      sessionId     = dependencies.sessionId,
      stage         = Stage.AfterComputedTaxId.ComputedTaxId,
      correlationId = dependencies.correlationId,
      taxId         = vrn
    )

    def journeyAfterDetermineTaxIdsJson: JsObject = read("testdata/vat/vatservice/JourneyAfterComputedTaxIds.json").asJson

    def updateEligibilityCheckRequest(): EligibilityCheckResult = eligibleEligibilityCheckResult()

    def updateEligibilityCheckRequestJson(): JsObject = read("/testdata/vat/vatservice/UpdateEligibilityCheckRequest.json").asJson

    def journeyAfterEligibilityCheckEligible: Journey.Vat.EligibilityChecked = Journey.Vat.EligibilityChecked(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Vat.VatService,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterEligibilityCheck.Eligible,
      correlationId          = dependencies.correlationId,
      taxId                  = vrn,
      eligibilityCheckResult = eligibleEligibilityCheckResult()
    )

    def journeyAfterEligibilityCheckEligibleJson: JsObject = read("/testdata/vat/vatservice/JourneyAfterEligibilityCheck.json").asJson

    def journeyAfterEligibilityCheckNotEligible: Journey.Vat.EligibilityChecked = Journey.Vat.EligibilityChecked(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Vat.VatService,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterEligibilityCheck.Ineligible,
      correlationId          = dependencies.correlationId,
      taxId                  = vrn,
      eligibilityCheckResult = ineligibleEligibilityCheckResult(eligibleEligibilityCheckResult())
    )

    def journeyAfterEligibilityCheckNotEligibleJson: JsObject = read("/testdata/vat/vatservice/JourneyAfterEligibilityCheckNotEligible.json").asJson

    def updateCanPayUpfrontYesRequest(): CanPayUpfront = canPayUpfrontYes

    def updateCanPayUpfrontNoRequest(): CanPayUpfront = canPayUpfrontNo

    def updateCanPayUpfrontYesRequestJson(): JsObject = read("/testdata/vat/vatservice/UpdateCanPayUpfrontYes.json").asJson

    def updateCanPayUpfrontNoRequestJson(): JsObject = read("/testdata/vat/vatservice/UpdateCanPayUpfrontNo.json").asJson

    def journeyAfterCanPayUpfrontYes: Journey.Vat.AnsweredCanPayUpfront = Journey.Vat.AnsweredCanPayUpfront(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Vat.VatService,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterCanPayUpfront.Yes,
      correlationId          = dependencies.correlationId,
      taxId                  = vrn,
      eligibilityCheckResult = eligibleEligibilityCheckResult(),
      canPayUpfront          = canPayUpfrontYes
    )

    def journeyAfterCanPayUpfrontYesJson: JsObject = read("/testdata/vat/vatservice/JourneyAfterCanPayUpfrontYes.json").asJson

    def journeyAfterCanPayUpfrontNo: Journey.Vat.AnsweredCanPayUpfront = Journey.Vat.AnsweredCanPayUpfront(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Vat.VatService,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterCanPayUpfront.No,
      correlationId          = dependencies.correlationId,
      taxId                  = vrn,
      eligibilityCheckResult = eligibleEligibilityCheckResult(),
      canPayUpfront          = canPayUpfrontNo
    )

    def journeyAfterCanPayUpfrontNoJson: JsObject = read("/testdata/vat/vatservice/JourneyAfterCanPayUpfrontNo.json").asJson

    def updateUpfrontPaymentAmountRequest(): UpfrontPaymentAmount = dependencies.upfrontPaymentAmount

    def updateUpfrontPaymentAmountRequestJson(): JsObject = read("/testdata/vat/vatservice/UpdateUpfrontPaymentAmountRequest.json").asJson

    def journeyAfterUpfrontPaymentAmount: Journey.Vat.EnteredUpfrontPaymentAmount = Journey.Vat.EnteredUpfrontPaymentAmount(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Vat.VatService,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount,
      correlationId          = dependencies.correlationId,
      taxId                  = vrn,
      eligibilityCheckResult = eligibleEligibilityCheckResult(),
      canPayUpfront          = canPayUpfrontYes,
      upfrontPaymentAmount   = dependencies.upfrontPaymentAmount
    )

    def journeyAfterUpfrontPaymentAmountJson: JsObject = read("/testdata/vat/vatservice/JourneyAfterUpdateUpfrontPaymentAmount.json").asJson

    def updateExtremeDatesRequest(): ExtremeDatesResponse = dependencies.extremeDatesWithUpfrontPayment

    def updateExtremeDatesRequestJson(): JsObject = read("/testdata/vat/vatservice/UpdateExtremeDatesRequest.json").asJson

    def journeyAfterExtremeDates: Journey.Vat.RetrievedExtremeDates = Journey.Vat.RetrievedExtremeDates(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Vat.VatService,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved,
      correlationId          = dependencies.correlationId,
      taxId                  = vrn,
      eligibilityCheckResult = eligibleEligibilityCheckResult(),
      upfrontPaymentAnswers  = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse   = dependencies.extremeDatesWithUpfrontPayment
    )

    def journeyAfterExtremeDatesJson: JsObject = read("/testdata/vat/vatservice/JourneyAfterUpdateExtremeDates.json").asJson

    def updateInstalmentAmountsRequest(): InstalmentAmounts = dependencies.instalmentAmounts

    def updateInstalmentAmountsRequestJson(): JsObject = read("/testdata/vat/vatservice/UpdateInstalmentAmountsRequest.json").asJson

    def journeyAfterInstalmentAmounts: Journey.Vat.RetrievedAffordabilityResult = Journey.Vat.RetrievedAffordabilityResult(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Vat.VatService,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterAffordabilityResult.RetrievedAffordabilityResult,
      correlationId          = dependencies.correlationId,
      taxId                  = vrn,
      eligibilityCheckResult = eligibleEligibilityCheckResult(),
      upfrontPaymentAnswers  = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse   = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts      = dependencies.instalmentAmounts
    )

    def journeyAfterInstalmentAmountsJson: JsObject = read("/testdata/vat/vatservice/JourneyAfterUpdateInstalmentAmounts.json").asJson

    def updateMonthlyPaymentAmountRequest(): MonthlyPaymentAmount = dependencies.monthlyPaymentAmount

    def updateMonthlyPaymentAmountRequestJson(): JsObject = read("/testdata/vat/vatservice/UpdateMonthlyPaymentAmountRequest.json").asJson

    def journeyAfterMonthlyPaymentAmount: Journey.Vat.EnteredMonthlyPaymentAmount = Journey.Vat.EnteredMonthlyPaymentAmount(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Vat.VatService,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount,
      correlationId          = dependencies.correlationId,
      taxId                  = vrn,
      eligibilityCheckResult = eligibleEligibilityCheckResult(),
      upfrontPaymentAnswers  = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse   = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts      = dependencies.instalmentAmounts,
      monthlyPaymentAmount   = dependencies.monthlyPaymentAmount
    )

    def journeyAfterMonthlyPaymentAmountJson: JsObject = read("/testdata/vat/vatservice/JourneyAfterUpdateMonthlyPaymentAmount.json").asJson

    def updateDayOfMonthRequest(): DayOfMonth = dependencies.dayOfMonth

    def updateDayOfMonthRequestJson(): JsObject = read("/testdata/vat/vatservice/UpdateDayOfMonthRequest.json").asJson

    def journeyAfterDayOfMonth: Journey.Vat.EnteredDayOfMonth = Journey.Vat.EnteredDayOfMonth(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Vat.VatService,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth,
      correlationId          = dependencies.correlationId,
      taxId                  = vrn,
      eligibilityCheckResult = eligibleEligibilityCheckResult(),
      upfrontPaymentAnswers  = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse   = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts      = dependencies.instalmentAmounts,
      monthlyPaymentAmount   = dependencies.monthlyPaymentAmount,
      dayOfMonth             = dependencies.dayOfMonth
    )

    def journeyAfterDayOfMonthJson: JsObject = read("/testdata/vat/vatservice/JourneyAfterUpdateDayOfMonth.json").asJson

    def updateStartDatesResponse(): StartDatesResponse = dependencies.startDatesResponseWithInitialPayment

    def updateStartDatesResponseJson(): JsObject = read("/testdata/vat/vatservice/UpdateStartDatesResponse.json").asJson

    def journeyAfterStartDatesResponse: Journey.Vat.RetrievedStartDates = Journey.Vat.RetrievedStartDates(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Vat.VatService,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterStartDatesResponse.StartDatesResponseRetrieved,
      correlationId          = dependencies.correlationId,
      taxId                  = vrn,
      eligibilityCheckResult = eligibleEligibilityCheckResult(),
      upfrontPaymentAnswers  = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse   = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts      = dependencies.instalmentAmounts,
      monthlyPaymentAmount   = dependencies.monthlyPaymentAmount,
      dayOfMonth             = dependencies.dayOfMonth,
      startDatesResponse     = dependencies.startDatesResponseWithInitialPayment
    )

    def journeyAfterStartDatesResponseJson: JsObject = read("/testdata/vat/vatservice/JourneyAfterUpdateStartDatesResponse.json").asJson

    def updateAffordableQuotesResponse(): AffordableQuotesResponse = dependencies.affordableQuotesResponse

    def updateAffordableQuotesResponseJson(): JsObject = read("/testdata/vat/vatservice/UpdateAffordableQuotesRequest.json").asJson

    def journeyAfterAffordableQuotesResponse: Journey.Vat.RetrievedAffordableQuotes = Journey.Vat.RetrievedAffordableQuotes(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Vat.VatService,
      createdOn                = dependencies.createdOn,
      sjRequest                = sjRequest,
      sessionId                = dependencies.sessionId,
      stage                    = Stage.AfterAffordableQuotesResponse.AffordableQuotesRetrieved,
      correlationId            = dependencies.correlationId,
      taxId                    = vrn,
      eligibilityCheckResult   = eligibleEligibilityCheckResult(),
      upfrontPaymentAnswers    = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse     = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts        = dependencies.instalmentAmounts,
      monthlyPaymentAmount     = dependencies.monthlyPaymentAmount,
      dayOfMonth               = dependencies.dayOfMonth,
      startDatesResponse       = dependencies.startDatesResponseWithInitialPayment,
      affordableQuotesResponse = dependencies.affordableQuotesResponse
    )

    def journeyAfterAffordableQuotesResponseJson: JsObject = read("/testdata/vat/vatservice/JourneyAfterUpdateAffordableQuotesResponse.json").asJson

    def updateSelectedPaymentPlanRequest(): PaymentPlan = dependencies.paymentPlan(1)

    def updateSelectedPaymentPlanRequestJson(): JsObject = read("/testdata/vat/vatservice/UpdateSelectedPaymentPlanRequest.json").asJson

    def journeyAfterSelectedPaymentPlan: Journey.Vat.ChosenPaymentPlan = Journey.Vat.ChosenPaymentPlan(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Vat.VatService,
      createdOn                = dependencies.createdOn,
      sjRequest                = sjRequest,
      sessionId                = dependencies.sessionId,
      stage                    = Stage.AfterSelectedPlan.SelectedPlan,
      correlationId            = dependencies.correlationId,
      taxId                    = vrn,
      eligibilityCheckResult   = eligibleEligibilityCheckResult(),
      upfrontPaymentAnswers    = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse     = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts        = dependencies.instalmentAmounts,
      monthlyPaymentAmount     = dependencies.monthlyPaymentAmount,
      dayOfMonth               = dependencies.dayOfMonth,
      startDatesResponse       = dependencies.startDatesResponseWithInitialPayment,
      affordableQuotesResponse = dependencies.affordableQuotesResponse,
      selectedPaymentPlan      = dependencies.paymentPlan(1)
    )

    def journeyAfterSelectedPaymentPlanJson: JsObject = read("/testdata/vat/vatservice/JourneyAfterSelectedPaymentPlan.json").asJson

    def updateCheckedPaymentPlanRequest(): JsNull.type = JsNull

    def updateCheckedPaymentPlanRequestJson(): JsObject = read("/testdata/vat/vatservice/UpdateCheckedPaymentPlanRequest.json").asJson

    def journeyAfterCheckedPaymentPlan: Journey.Vat.CheckedPaymentPlan = Journey.Vat.CheckedPaymentPlan(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Vat.VatService,
      createdOn                = dependencies.createdOn,
      sjRequest                = sjRequest,
      sessionId                = dependencies.sessionId,
      stage                    = Stage.AfterCheckedPlan.AcceptedPlan,
      correlationId            = dependencies.correlationId,
      taxId                    = vrn,
      eligibilityCheckResult   = eligibleEligibilityCheckResult(),
      upfrontPaymentAnswers    = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse     = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts        = dependencies.instalmentAmounts,
      monthlyPaymentAmount     = dependencies.monthlyPaymentAmount,
      dayOfMonth               = dependencies.dayOfMonth,
      startDatesResponse       = dependencies.startDatesResponseWithInitialPayment,
      affordableQuotesResponse = dependencies.affordableQuotesResponse,
      selectedPaymentPlan      = dependencies.paymentPlan(1)
    )

    def journeyAfterCheckedPaymentPlanJson: JsObject = read("/testdata/vat/vatservice/JourneyAfterCheckedPaymentPlan.json").asJson

    def updateDetailsAboutBankAccountRequest(isAccountHolder: Boolean): DetailsAboutBankAccount =
      DetailsAboutBankAccount(dependencies.businessBankAccount, isAccountHolder)

    def updateDetailsAboutBankAccountRequestJson(): JsObject = read("/testdata/vat/vatservice/JourneyAfterEnteredDetailsAboutBankAccount.json").asJson

    def journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder: Boolean): Journey.AfterEnteredDetailsAboutBankAccount = Journey.Vat.EnteredDetailsAboutBankAccount(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Vat.VatService,
      createdOn                = dependencies.createdOn,
      sjRequest                = sjRequest,
      sessionId                = dependencies.sessionId,
      correlationId            = dependencies.correlationId,
      stage                    = if (isAccountHolder) Stage.AfterEnteredDetailsAboutBankAccount.Business else Stage.AfterEnteredDetailsAboutBankAccount.IsNotAccountHolder,
      taxId                    = vrn,
      eligibilityCheckResult   = eligibleEligibilityCheckResult(),
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

    def journeyAfterEnteredDetailsAboutBankAccountJson: JsObject = read("/testdata/vat/vatservice/JourneyAfterUpdateDetailsAboutBankAccountRequest.json").asJson
  }
}
