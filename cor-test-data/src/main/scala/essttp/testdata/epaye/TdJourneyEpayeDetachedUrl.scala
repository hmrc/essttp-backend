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

import essttp.journey.model.SjRequest.Epaye
import essttp.journey.model._
import essttp.rootmodel.bank.{BankDetails, DetailsAboutBankAccount}
import essttp.rootmodel.dates.extremedates.ExtremeDatesResponse
import essttp.rootmodel.dates.startdates.StartDatesResponse
import essttp.rootmodel.ttp.EligibilityCheckResult
import essttp.rootmodel.ttp.affordability.InstalmentAmounts
import essttp.rootmodel.ttp.affordablequotes.{AffordableQuotesResponse, PaymentPlan}
import essttp.rootmodel.ttp.arrangement.ArrangementResponse
import essttp.rootmodel._
import essttp.testdata.{TdBase, TdJourneyStructure}
import essttp.utils.ResourceReader.read
import essttp.utils.JsonSyntax._
import play.api.libs.json.{JsNull, JsObject}

import scala.language.reflectiveCalls

trait TdJourneyEpayeDetachedUrl {
  dependencies: TdBase with TdEpaye =>

  object EpayeDetachedUrl extends TdJourneyStructure {

    def sjRequest: Epaye.Empty = SjRequest.Epaye.Empty()

    def sjResponse: SjResponse = SjResponse(
      nextUrl   = NextUrl(s"http://localhost:9215/set-up-a-payment-plan?traceId=${dependencies.traceId.value}"),
      journeyId = dependencies.journeyId
    )

    def postPath: String = "/epaye/detached-url/journey/start"

    def sjRequestJson: JsObject = read("testdata/epaye/detachedurl/SjRequest.json").asJson

    def journeyAfterStarted: Journey.Epaye.Started = Journey.Epaye.Started(
      _id           = dependencies.journeyId,
      origin        = Origins.Epaye.DetachedUrl,
      createdOn     = dependencies.createdOn,
      sjRequest     = sjRequest,
      sessionId     = dependencies.sessionId,
      stage         = Stage.AfterStarted.Started,
      correlationId = dependencies.correlationId
    )

    def journeyAfterStartedJson: JsObject = read("testdata/epaye/detachedurl/JourneyAfterStarted.json").asJson

    def updateTaxIdRequest(): TaxId = empRef

    def updateTaxIdRequestJson(): JsObject = read("testdata/epaye/detachedurl/UpdateTaxIdRequest.json").asJson

    def journeyAfterDetermineTaxIds: Journey.Epaye.ComputedTaxId = Journey.Epaye.ComputedTaxId(
      _id           = dependencies.journeyId,
      origin        = Origins.Epaye.DetachedUrl,
      createdOn     = dependencies.createdOn,
      sjRequest     = sjRequest,
      sessionId     = dependencies.sessionId,
      stage         = Stage.AfterComputedTaxId.ComputedTaxId,
      correlationId = dependencies.correlationId,
      taxId         = empRef
    )

    def journeyAfterDetermineTaxIdsJson: JsObject = read("testdata/epaye/detachedurl/JourneyAfterComputedTaxIds.json").asJson

    def updateEligibilityCheckRequest(): EligibilityCheckResult = eligibleEligibilityCheckResult

    def updateEligibilityCheckRequestJson(): JsObject = read("testdata/epaye/detachedurl/UpdateEligibilityCheckRequest.json").asJson

    def journeyAfterEligibilityCheckEligible: Journey.Epaye.EligibilityChecked = Journey.Epaye.EligibilityChecked(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.DetachedUrl,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterEligibilityCheck.Eligible,
      correlationId          = dependencies.correlationId,
      taxId                  = empRef,
      eligibilityCheckResult = eligibleEligibilityCheckResult
    )

    def journeyAfterEligibilityCheckEligibleJson: JsObject = read("testdata/epaye/detachedurl/JourneyAfterEligibilityCheck.json").asJson

    def journeyAfterEligibilityCheckNotEligible: Journey.Epaye.EligibilityChecked = Journey.Epaye.EligibilityChecked(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.DetachedUrl,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterEligibilityCheck.Ineligible,
      correlationId          = dependencies.correlationId,
      taxId                  = empRef,
      eligibilityCheckResult = ineligibleEligibilityCheckResult
    )

    def journeyAfterEligibilityCheckNotEligibleJson: JsObject = read("testdata/epaye/detachedurl/JourneyAfterEligibilityCheckNotEligible.json").asJson

    def updateCanPayUpfrontYesRequest(): CanPayUpfront = canPayUpfrontYes

    def updateCanPayUpfrontNoRequest(): CanPayUpfront = canPayUpfrontNo

    def updateCanPayUpfrontYesRequestJson(): JsObject = read("/testdata/epaye/detachedurl/UpdateCanPayUpfrontYes.json").asJson

    def updateCanPayUpfrontNoRequestJson(): JsObject = read("/testdata/epaye/detachedurl/UpdateCanPayUpfrontNo.json").asJson

    def journeyAfterCanPayUpfrontYes: Journey.Epaye.AnsweredCanPayUpfront = Journey.Epaye.AnsweredCanPayUpfront(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.DetachedUrl,
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
      origin                 = Origins.Epaye.DetachedUrl,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterCanPayUpfront.No,
      correlationId          = dependencies.correlationId,
      taxId                  = empRef,
      eligibilityCheckResult = eligibleEligibilityCheckResult,
      canPayUpfront          = canPayUpfrontNo
    )

    def journeyAfterCanPayUpfrontYesJson: JsObject = read("/testdata/epaye/detachedurl/JourneyAfterCanPayUpfrontYes.json").asJson

    def journeyAfterCanPayUpfrontNoJson: JsObject = read("/testdata/epaye/detachedurl/JourneyAfterCanPayUpfrontNo.json").asJson

    def updateUpfrontPaymentAmountRequest(): UpfrontPaymentAmount = dependencies.upfrontPaymentAmount

    def updateUpfrontPaymentAmountRequestJson(): JsObject = read("/testdata/epaye/detachedurl/UpdateUpfrontPaymentAmountRequest.json").asJson

    def journeyAfterUpfrontPaymentAmount: Journey.Epaye.EnteredUpfrontPaymentAmount = Journey.Epaye.EnteredUpfrontPaymentAmount(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.DetachedUrl,
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

    def journeyAfterUpfrontPaymentAmountJson: JsObject = read("/testdata/epaye/detachedurl/JourneyAfterUpdateUpfrontPaymentAmount.json").asJson

    def updateExtremeDatesRequest(): ExtremeDatesResponse = dependencies.extremeDatesWithUpfrontPayment

    def updateExtremeDatesRequestJson(): JsObject = read("/testdata/epaye/detachedurl/UpdateExtremeDatesRequest.json").asJson

    def journeyAfterExtremeDates: Journey.Epaye.RetrievedExtremeDates = Journey.Epaye.RetrievedExtremeDates(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.DetachedUrl,
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

    def journeyAfterExtremeDatesJson: JsObject = read("/testdata/epaye/detachedurl/JourneyAfterUpdateExtremeDates.json").asJson

    def updateInstalmentAmountsRequest(): InstalmentAmounts = dependencies.instalmentAmounts

    def updateInstalmentAmountsRequestJson(): JsObject = read("/testdata/epaye/detachedurl/UpdateInstalmentAmountsRequest.json").asJson

    def journeyAfterInstalmentAmounts: Journey.Epaye.RetrievedAffordabilityResult = Journey.Epaye.RetrievedAffordabilityResult(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.DetachedUrl,
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

    def journeyAfterInstalmentAmountsJson: JsObject = read("/testdata/epaye/detachedurl/JourneyAfterUpdateInstalmentAmounts.json").asJson

    def updateMonthlyPaymentAmountRequest(): MonthlyPaymentAmount = dependencies.monthlyPaymentAmount

    def updateMonthlyPaymentAmountRequestJson(): JsObject = read("/testdata/epaye/detachedurl/UpdateMonthlyPaymentAmountRequest.json").asJson

    def journeyAfterMonthlyPaymentAmount: Journey.Epaye.EnteredMonthlyPaymentAmount = Journey.Epaye.EnteredMonthlyPaymentAmount(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.DetachedUrl,
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

    def journeyAfterMonthlyPaymentAmountJson: JsObject = read("/testdata/epaye/detachedurl/JourneyAfterUpdateMonthlyPaymentAmount.json").asJson

    def updateDayOfMonthRequest(): DayOfMonth = dependencies.dayOfMonth

    def updateDayOfMonthRequestJson(): JsObject = read("/testdata/epaye/detachedurl/UpdateDayOfMonthRequest.json").asJson

    def journeyAfterDayOfMonth: Journey.Epaye.EnteredDayOfMonth = Journey.Epaye.EnteredDayOfMonth(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.DetachedUrl,
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

    def journeyAfterDayOfMonthJson: JsObject = read("/testdata/epaye/detachedurl/JourneyAfterUpdateDayOfMonth.json").asJson

    def updateStartDatesResponse(): StartDatesResponse = dependencies.startDatesResponseWithInitialPayment

    def updateStartDatesResponseJson(): JsObject = read("/testdata/epaye/detachedurl/UpdateStartDatesResponse.json").asJson

    def journeyAfterStartDatesResponse: Journey.AfterStartDatesResponse = Journey.Epaye.RetrievedStartDates(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Epaye.DetachedUrl,
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

    def journeyAfterStartDatesResponseJson: JsObject = read("/testdata/epaye/detachedurl/JourneyAfterUpdateStartDatesResponse.json").asJson

    def updateAffordableQuotesResponse(): AffordableQuotesResponse = dependencies.affordableQuotesResponse

    def updateAffordableQuotesResponseJson(): JsObject = read("/testdata/epaye/detachedurl/UpdateAffordableQuotesRequest.json").asJson

    def journeyAfterAffordableQuotesResponse: Journey.AfterAffordableQuotesResponse = Journey.Epaye.RetrievedAffordableQuotes(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Epaye.DetachedUrl,
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

    def journeyAfterAffordableQuotesResponseJson: JsObject = read("/testdata/epaye/detachedurl/JourneyAfterUpdateAffordableQuotesResponse.json").asJson

    def updateSelectedPaymentPlanRequest(): PaymentPlan = dependencies.paymentPlan(1)

    def updateSelectedPaymentPlanRequestJson(): JsObject = read("/testdata/epaye/detachedurl/UpdateSelectedPaymentPlanRequest.json").asJson

    def journeyAfterSelectedPaymentPlan: Journey.AfterSelectedPaymentPlan = Journey.Epaye.ChosenPaymentPlan(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Epaye.DetachedUrl,
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

    def journeyAfterSelectedPaymentPlanJson: JsObject = read("/testdata/epaye/detachedurl/JourneyAfterSelectedPaymentPlan.json").asJson

    def updateCheckedPaymentPlanRequest(): JsNull.type = JsNull

    def updateCheckedPaymentPlanRequestJson(): JsObject = read("/testdata/epaye/detachedurl/UpdateCheckedPaymentPlanRequest.json").asJson

    def journeyAfterCheckedPaymentPlan: Journey.AfterCheckedPaymentPlan = Journey.Epaye.CheckedPaymentPlan(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Epaye.DetachedUrl,
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

    def journeyAfterCheckedPaymentPlanJson: JsObject = read("/testdata/epaye/detachedurl/JourneyAfterCheckedPaymentPlan.json").asJson

    def updateDetailsAboutBankAccountRequest(isAccountHolder: Boolean): DetailsAboutBankAccount =
      DetailsAboutBankAccount(dependencies.businessBankAccount, isAccountHolder)

    def updateDetailsAboutBankAccountRequestJson(): JsObject = read("/testdata/epaye/detachedurl/JourneyAfterEnteredDetailsAboutBankAccount.json").asJson

    def journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder: Boolean): Journey.AfterEnteredDetailsAboutBankAccount = Journey.Epaye.EnteredDetailsAboutBankAccount(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Epaye.DetachedUrl,
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

    def journeyAfterEnteredDetailsAboutBankAccountJson: JsObject = read("/testdata/epaye/detachedurl/JourneyAfterUpdateDetailsAboutBankAccountRequest.json").asJson

    def updateDirectDebitDetailsRequest(): BankDetails = dependencies.directDebitDetails

    def updateDirectDebitDetailsRequestJson(): JsObject = read("/testdata/epaye/detachedurl/UpdateDirectDebitDetailsRequest.json").asJson

    def journeyAfterEnteredDirectDebitDetails(): Journey.AfterEnteredDirectDebitDetails = Journey.Epaye.EnteredDirectDebitDetails(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Epaye.DetachedUrl,
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

    def journeyAfterEnteredDirectDebitDetailsJson: JsObject = read("/testdata/epaye/detachedurl/JourneyAfterUpdateDirectDebitDetails.json").asJson

    def updateConfirmedDirectDebitDetailsRequest(): JsNull.type = JsNull

    def updateConfirmedDirectDebitDetailsJson(): JsObject = read("/testdata/epaye/detachedurl/UpdateConfirmedDirectDebitDetailsRequest.json").asJson

    def journeyAfterConfirmedDirectDebitDetails: Journey.AfterConfirmedDirectDebitDetails = Journey.Epaye.ConfirmedDirectDebitDetails(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Epaye.DetachedUrl,
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

    def journeyAfterConfirmedDirectDebitDetailsJson: JsObject = read("/testdata/epaye/detachedurl/JourneyAfterUpdateConfirmedDirectDebitDetails.json").asJson

    def updateAgreedTermsAndConditionsRequest(isEmailAddressRequired: Boolean): IsEmailAddressRequired = IsEmailAddressRequired(isEmailAddressRequired)

    def updateAgreedTermsAndConditionsJson(): JsObject = read("/testdata/epaye/detachedurl/UpdateAgreedTermsAndConditions.json").asJson

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

    def journeyAfterAgreedTermsAndConditionsJson: JsObject = read("/testdata/epaye/detachedurl/JourneyAfterUpdateAgreedTermsAndConditions.json").asJson

    def updateArrangementRequest(): ArrangementResponse = dependencies.arrangementResponse

    def updateArrangementRequestJson(): JsObject = read("/testdata/epaye/detachedurl/UpdateSubmittedArrangementRequest.json").asJson

    def journeyAfterSubmittedArrangement: Journey.AfterArrangementSubmitted = Journey.Epaye.SubmittedArrangement(
      _id                      = dependencies.journeyId,
      origin                   = Origins.Epaye.DetachedUrl,
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
      isEmailAddressRequired   = IsEmailAddressRequired(false),
      emailVerificationAnswers = EmailVerificationAnswers.NoEmailJourney,
      arrangementResponse      = dependencies.arrangementResponse
    )

    def journeyAfterSubmittedArrangementJson: JsObject = read("/testdata/epaye/detachedurl/JourneyAfterUpdateSubmittedArrangement.json").asJson
  }
}