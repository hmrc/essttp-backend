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

trait TdJourneySaPta {
  dependencies: TdBase with TdSa =>

  object SaPta extends TdJourneyStructure {

    def sjRequest: Sa.Simple = SjRequest.Sa.Simple(
      dependencies.returnUrl,
      dependencies.backUrl
    )

    def sjResponse: SjResponse = SjResponse(
      nextUrl   = NextUrl(s"http://localhost:9215/set-up-a-payment-plan/sa-payment-plan"),
      journeyId = dependencies.journeyId
    )

    def postPath: String = "/sa/pta/journey/start"

    def journeyAfterStarted: Journey.Sa.Started = Journey.Sa.Started(
      _id                  = dependencies.journeyId,
      origin               = Origins.Sa.Pta,
      createdOn            = dependencies.createdOn,
      sjRequest            = sjRequest,
      sessionId            = dependencies.sessionId,
      stage                = Stage.AfterStarted.Started,
      affordabilityEnabled = Some(false),
      correlationId        = dependencies.correlationId,
    )

    def updateTaxIdRequest(): TaxId = saUtr

    def journeyAfterDetermineTaxIds: Journey.Sa.ComputedTaxId = Journey.Sa.ComputedTaxId(
      _id                  = dependencies.journeyId,
      origin               = Origins.Sa.Pta,
      createdOn            = dependencies.createdOn,
      sjRequest            = sjRequest,
      sessionId            = dependencies.sessionId,
      stage                = Stage.AfterComputedTaxId.ComputedTaxId,
      affordabilityEnabled = Some(false),
      correlationId        = dependencies.correlationId,
      taxId                = saUtr
    )

    def updateEligibilityCheckRequest(): EligibilityCheckResult = eligibleEligibilityCheckResultSa

    def journeyAfterEligibilityCheckEligible: Journey.Sa.EligibilityChecked = Journey.Sa.EligibilityChecked(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Sa.Pta,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterEligibilityCheck.Eligible,
      affordabilityEnabled   = Some(false),
      correlationId          = dependencies.correlationId,
      taxId                  = saUtr,
      eligibilityCheckResult = eligibleEligibilityCheckResultSa
    )

    def journeyAfterEligibilityCheckNotEligible: Journey.Sa.EligibilityChecked = Journey.Sa.EligibilityChecked(
      _id                    = dependencies.journeyId,
      origin                 = Origins.Sa.Pta,
      createdOn              = dependencies.createdOn,
      sjRequest              = sjRequest,
      sessionId              = dependencies.sessionId,
      stage                  = Stage.AfterEligibilityCheck.Ineligible,
      affordabilityEnabled   = Some(false),
      correlationId          = dependencies.correlationId,
      taxId                  = saUtr,
      eligibilityCheckResult = ineligibleEligibilityCheckResultSa
    )

    def journeyAfterWhyCannotPayInFullNotRequired: Journey.Sa.ObtainedWhyCannotPayInFullAnswers = Journey.Sa.ObtainedWhyCannotPayInFullAnswers(
      _id                       = dependencies.journeyId,
      origin                    = Origins.Sa.Pta,
      createdOn                 = dependencies.createdOn,
      sjRequest                 = sjRequest,
      sessionId                 = dependencies.sessionId,
      stage                     = Stage.AfterWhyCannotPayInFullAnswers.AnswerNotRequired,
      affordabilityEnabled      = Some(false),
      correlationId             = dependencies.correlationId,
      taxId                     = saUtr,
      eligibilityCheckResult    = eligibleEligibilityCheckResultSa,
      whyCannotPayInFullAnswers = WhyCannotPayInFullAnswers.AnswerNotRequired
    )

    def updateCanPayUpfrontYesRequest(): CanPayUpfront = canPayUpfrontYes

    def updateCanPayUpfrontNoRequest(): CanPayUpfront = canPayUpfrontNo

    def journeyAfterCanPayUpfrontYes: Journey.Sa.AnsweredCanPayUpfront = Journey.Sa.AnsweredCanPayUpfront(
      _id                       = dependencies.journeyId,
      origin                    = Origins.Sa.Pta,
      createdOn                 = dependencies.createdOn,
      sjRequest                 = sjRequest,
      sessionId                 = dependencies.sessionId,
      stage                     = Stage.AfterCanPayUpfront.Yes,
      affordabilityEnabled      = Some(false),
      correlationId             = dependencies.correlationId,
      taxId                     = saUtr,
      eligibilityCheckResult    = eligibleEligibilityCheckResultSa,
      whyCannotPayInFullAnswers = WhyCannotPayInFullAnswers.AnswerNotRequired,
      canPayUpfront             = canPayUpfrontYes
    )

    def journeyAfterCanPayUpfrontNo: Journey.Sa.AnsweredCanPayUpfront = Journey.Sa.AnsweredCanPayUpfront(
      _id                       = dependencies.journeyId,
      origin                    = Origins.Sa.Pta,
      createdOn                 = dependencies.createdOn,
      sjRequest                 = sjRequest,
      sessionId                 = dependencies.sessionId,
      stage                     = Stage.AfterCanPayUpfront.No,
      affordabilityEnabled      = Some(false),
      correlationId             = dependencies.correlationId,
      taxId                     = saUtr,
      eligibilityCheckResult    = eligibleEligibilityCheckResultSa,
      whyCannotPayInFullAnswers = WhyCannotPayInFullAnswers.AnswerNotRequired,
      canPayUpfront             = canPayUpfrontNo
    )

    def updateUpfrontPaymentAmountRequest(): UpfrontPaymentAmount = dependencies.upfrontPaymentAmount

    // used in specific test for changing upfront payment amount, no need to copy to other TdJourneys
    def anotherUpdateUpfrontPaymentAmountRequest(): UpfrontPaymentAmount = dependencies.anotherUpfrontPaymentAmount

    def journeyAfterUpfrontPaymentAmount: Journey.Sa.EnteredUpfrontPaymentAmount = Journey.Sa.EnteredUpfrontPaymentAmount(
      _id                       = dependencies.journeyId,
      origin                    = Origins.Sa.Pta,
      createdOn                 = dependencies.createdOn,
      sjRequest                 = sjRequest,
      sessionId                 = dependencies.sessionId,
      stage                     = Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount,
      affordabilityEnabled      = Some(false),
      correlationId             = dependencies.correlationId,
      taxId                     = saUtr,
      eligibilityCheckResult    = eligibleEligibilityCheckResultSa,
      whyCannotPayInFullAnswers = WhyCannotPayInFullAnswers.AnswerNotRequired,
      canPayUpfront             = canPayUpfrontYes,
      upfrontPaymentAmount      = dependencies.upfrontPaymentAmount
    )

    def updateExtremeDatesRequest(): ExtremeDatesResponse = dependencies.extremeDatesWithUpfrontPayment

    def journeyAfterExtremeDates: Journey.Sa.RetrievedExtremeDates = Journey.Sa.RetrievedExtremeDates(
      _id                       = dependencies.journeyId,
      origin                    = Origins.Sa.Pta,
      createdOn                 = dependencies.createdOn,
      sjRequest                 = sjRequest,
      sessionId                 = dependencies.sessionId,
      stage                     = Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved,
      affordabilityEnabled      = Some(false),
      correlationId             = dependencies.correlationId,
      taxId                     = saUtr,
      eligibilityCheckResult    = eligibleEligibilityCheckResultSa,
      whyCannotPayInFullAnswers = WhyCannotPayInFullAnswers.AnswerNotRequired,
      upfrontPaymentAnswers     = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse      = dependencies.extremeDatesWithUpfrontPayment
    )

    def updateInstalmentAmountsRequest(): InstalmentAmounts = dependencies.instalmentAmounts

    def journeyAfterInstalmentAmounts: Journey.Sa.RetrievedAffordabilityResult = Journey.Sa.RetrievedAffordabilityResult(
      _id                       = dependencies.journeyId,
      origin                    = Origins.Sa.Pta,
      createdOn                 = dependencies.createdOn,
      sjRequest                 = sjRequest,
      sessionId                 = dependencies.sessionId,
      stage                     = Stage.AfterAffordabilityResult.RetrievedAffordabilityResult,
      affordabilityEnabled      = Some(false),
      correlationId             = dependencies.correlationId,
      taxId                     = saUtr,
      eligibilityCheckResult    = eligibleEligibilityCheckResultSa,
      whyCannotPayInFullAnswers = WhyCannotPayInFullAnswers.AnswerNotRequired,
      upfrontPaymentAnswers     = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse      = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts         = dependencies.instalmentAmounts
    )

    def journeyAfterCanPayWithinSixMonths: Journey.Sa.ObtainedCanPayWithinSixMonthsAnswers = Journey.Sa.ObtainedCanPayWithinSixMonthsAnswers(
      _id                          = dependencies.journeyId,
      origin                       = Origins.Sa.Pta,
      createdOn                    = dependencies.createdOn,
      sjRequest                    = sjRequest,
      sessionId                    = dependencies.sessionId,
      stage                        = Stage.AfterCanPayWithinSixMonthsAnswers.AnswerNotRequired,
      affordabilityEnabled         = Some(false),
      correlationId                = dependencies.correlationId,
      taxId                        = saUtr,
      eligibilityCheckResult       = eligibleEligibilityCheckResultSa,
      whyCannotPayInFullAnswers    = WhyCannotPayInFullAnswers.AnswerNotRequired,
      upfrontPaymentAnswers        = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse         = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts            = dependencies.instalmentAmounts,
      canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired
    )

    def updateMonthlyPaymentAmountRequest(): MonthlyPaymentAmount = dependencies.monthlyPaymentAmount

    def journeyAfterMonthlyPaymentAmount: Journey.Sa.EnteredMonthlyPaymentAmount = Journey.Sa.EnteredMonthlyPaymentAmount(
      _id                          = dependencies.journeyId,
      origin                       = Origins.Sa.Pta,
      createdOn                    = dependencies.createdOn,
      sjRequest                    = sjRequest,
      sessionId                    = dependencies.sessionId,
      stage                        = Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount,
      affordabilityEnabled         = Some(false),
      correlationId                = dependencies.correlationId,
      taxId                        = saUtr,
      eligibilityCheckResult       = eligibleEligibilityCheckResultSa,
      whyCannotPayInFullAnswers    = WhyCannotPayInFullAnswers.AnswerNotRequired,
      upfrontPaymentAnswers        = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse         = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts            = dependencies.instalmentAmounts,
      canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
      monthlyPaymentAmount         = dependencies.monthlyPaymentAmount
    )

    def updateDayOfMonthRequest(): DayOfMonth = dependencies.dayOfMonth

    def journeyAfterDayOfMonth: Journey.Sa.EnteredDayOfMonth = Journey.Sa.EnteredDayOfMonth(
      _id                          = dependencies.journeyId,
      origin                       = Origins.Sa.Pta,
      createdOn                    = dependencies.createdOn,
      sjRequest                    = sjRequest,
      sessionId                    = dependencies.sessionId,
      stage                        = Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth,
      affordabilityEnabled         = Some(false),
      correlationId                = dependencies.correlationId,
      taxId                        = saUtr,
      eligibilityCheckResult       = eligibleEligibilityCheckResultSa,
      whyCannotPayInFullAnswers    = WhyCannotPayInFullAnswers.AnswerNotRequired,
      upfrontPaymentAnswers        = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse         = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts            = dependencies.instalmentAmounts,
      canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
      monthlyPaymentAmount         = dependencies.monthlyPaymentAmount,
      dayOfMonth                   = dependencies.dayOfMonth
    )

    def updateStartDatesResponse(): StartDatesResponse = dependencies.startDatesResponseWithInitialPayment

    def journeyAfterStartDatesResponse: Journey.Sa.RetrievedStartDates = Journey.Sa.RetrievedStartDates(
      _id                          = dependencies.journeyId,
      origin                       = Origins.Sa.Pta,
      createdOn                    = dependencies.createdOn,
      sjRequest                    = sjRequest,
      sessionId                    = dependencies.sessionId,
      stage                        = Stage.AfterStartDatesResponse.StartDatesResponseRetrieved,
      affordabilityEnabled         = Some(false),
      correlationId                = dependencies.correlationId,
      taxId                        = saUtr,
      eligibilityCheckResult       = eligibleEligibilityCheckResultSa,
      whyCannotPayInFullAnswers    = WhyCannotPayInFullAnswers.AnswerNotRequired,
      upfrontPaymentAnswers        = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse         = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts            = dependencies.instalmentAmounts,
      canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
      monthlyPaymentAmount         = dependencies.monthlyPaymentAmount,
      dayOfMonth                   = dependencies.dayOfMonth,
      startDatesResponse           = dependencies.startDatesResponseWithInitialPayment
    )

    def updateAffordableQuotesResponse(): AffordableQuotesResponse = dependencies.affordableQuotesResponse

    def journeyAfterAffordableQuotesResponse: Journey.Sa.RetrievedAffordableQuotes = Journey.Sa.RetrievedAffordableQuotes(
      _id                          = dependencies.journeyId,
      origin                       = Origins.Sa.Pta,
      createdOn                    = dependencies.createdOn,
      sjRequest                    = sjRequest,
      sessionId                    = dependencies.sessionId,
      stage                        = Stage.AfterAffordableQuotesResponse.AffordableQuotesRetrieved,
      affordabilityEnabled         = Some(false),
      correlationId                = dependencies.correlationId,
      taxId                        = saUtr,
      eligibilityCheckResult       = eligibleEligibilityCheckResultSa,
      whyCannotPayInFullAnswers    = WhyCannotPayInFullAnswers.AnswerNotRequired,
      upfrontPaymentAnswers        = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse         = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts            = dependencies.instalmentAmounts,
      canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
      monthlyPaymentAmount         = dependencies.monthlyPaymentAmount,
      dayOfMonth                   = dependencies.dayOfMonth,
      startDatesResponse           = dependencies.startDatesResponseWithInitialPayment,
      affordableQuotesResponse     = dependencies.affordableQuotesResponse
    )

    def updateSelectedPaymentPlanRequest(): PaymentPlan = dependencies.paymentPlan(1)

    def journeyAfterSelectedPaymentPlan: Journey.Sa.ChosenPaymentPlan = Journey.Sa.ChosenPaymentPlan(
      _id                          = dependencies.journeyId,
      origin                       = Origins.Sa.Pta,
      createdOn                    = dependencies.createdOn,
      sjRequest                    = sjRequest,
      sessionId                    = dependencies.sessionId,
      stage                        = Stage.AfterSelectedPlan.SelectedPlan,
      affordabilityEnabled         = Some(false),
      correlationId                = dependencies.correlationId,
      taxId                        = saUtr,
      eligibilityCheckResult       = eligibleEligibilityCheckResultSa,
      whyCannotPayInFullAnswers    = WhyCannotPayInFullAnswers.AnswerNotRequired,
      upfrontPaymentAnswers        = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse         = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts            = dependencies.instalmentAmounts,
      canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
      monthlyPaymentAmount         = dependencies.monthlyPaymentAmount,
      dayOfMonth                   = dependencies.dayOfMonth,
      startDatesResponse           = dependencies.startDatesResponseWithInitialPayment,
      affordableQuotesResponse     = dependencies.affordableQuotesResponse,
      selectedPaymentPlan          = dependencies.paymentPlan(1)
    )

    def updateCheckedPaymentPlanRequest(): JsNull.type = JsNull

    def journeyAfterCheckedPaymentPlanNonAffordability: Journey.Sa.CheckedPaymentPlan = Journey.Sa.CheckedPaymentPlan(
      _id                          = dependencies.journeyId,
      origin                       = Origins.Sa.Pta,
      createdOn                    = dependencies.createdOn,
      sjRequest                    = sjRequest,
      sessionId                    = dependencies.sessionId,
      stage                        = Stage.AfterCheckedPlan.AcceptedPlan,
      affordabilityEnabled         = Some(false),
      correlationId                = dependencies.correlationId,
      taxId                        = saUtr,
      eligibilityCheckResult       = eligibleEligibilityCheckResultSa,
      whyCannotPayInFullAnswers    = WhyCannotPayInFullAnswers.AnswerNotRequired,
      upfrontPaymentAnswers        = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse         = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts            = dependencies.instalmentAmounts,
      canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
      paymentPlanAnswers           = dependencies.paymentPlanAnswersNoAffordability,
    )

    def updateDetailsAboutBankAccountRequest(isAccountHolder: Boolean): DetailsAboutBankAccount =
      DetailsAboutBankAccount(isAccountHolder)

    def journeyAfterEnteredDetailsAboutBankAccountNoAffordability(isAccountHolder: Boolean): Journey.Sa.EnteredDetailsAboutBankAccount = Journey.Sa.EnteredDetailsAboutBankAccount(
      _id                          = dependencies.journeyId,
      origin                       = Origins.Sa.Pta,
      createdOn                    = dependencies.createdOn,
      sjRequest                    = sjRequest,
      sessionId                    = dependencies.sessionId,
      stage                        = if (isAccountHolder) Stage.AfterEnteredDetailsAboutBankAccount.IsAccountHolder else Stage.AfterEnteredDetailsAboutBankAccount.IsNotAccountHolder,
      affordabilityEnabled         = Some(false),
      correlationId                = dependencies.correlationId,
      taxId                        = saUtr,
      eligibilityCheckResult       = eligibleEligibilityCheckResultSa,
      whyCannotPayInFullAnswers    = WhyCannotPayInFullAnswers.AnswerNotRequired,
      upfrontPaymentAnswers        = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse         = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts            = dependencies.instalmentAmounts,
      canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
      paymentPlanAnswers           = dependencies.paymentPlanAnswersNoAffordability,
      detailsAboutBankAccount      = DetailsAboutBankAccount(isAccountHolder)
    )

    val updateDirectDebitDetailsRequest: BankDetails = dependencies.directDebitDetails

    def journeyAfterEnteredDirectDebitDetailsNoAffordability(): Journey.Sa.EnteredDirectDebitDetails = Journey.Sa.EnteredDirectDebitDetails(
      _id                          = dependencies.journeyId,
      origin                       = Origins.Sa.Pta,
      createdOn                    = dependencies.createdOn,
      sjRequest                    = sjRequest,
      sessionId                    = dependencies.sessionId,
      stage                        = Stage.AfterEnteredDirectDebitDetails.EnteredDirectDebitDetails,
      affordabilityEnabled         = Some(false),
      correlationId                = dependencies.correlationId,
      taxId                        = saUtr,
      eligibilityCheckResult       = eligibleEligibilityCheckResultSa,
      whyCannotPayInFullAnswers    = WhyCannotPayInFullAnswers.AnswerNotRequired,
      upfrontPaymentAnswers        = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse         = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts            = dependencies.instalmentAmounts,
      canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
      paymentPlanAnswers           = dependencies.paymentPlanAnswersNoAffordability,
      detailsAboutBankAccount      = DetailsAboutBankAccount(isAccountHolder = true),
      directDebitDetails           = directDebitDetails
    )

    def updateConfirmedDirectDebitDetailsRequest(): JsNull.type = JsNull

    def journeyAfterConfirmedDirectDebitDetailsNoAffordability: Journey.Sa.ConfirmedDirectDebitDetails = Journey.Sa.ConfirmedDirectDebitDetails(
      _id                          = dependencies.journeyId,
      origin                       = Origins.Sa.Pta,
      createdOn                    = dependencies.createdOn,
      sjRequest                    = sjRequest,
      sessionId                    = dependencies.sessionId,
      stage                        = Stage.AfterConfirmedDirectDebitDetails.ConfirmedDetails,
      affordabilityEnabled         = Some(false),
      correlationId                = dependencies.correlationId,
      taxId                        = saUtr,
      eligibilityCheckResult       = eligibleEligibilityCheckResultSa,
      whyCannotPayInFullAnswers    = WhyCannotPayInFullAnswers.AnswerNotRequired,
      upfrontPaymentAnswers        = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse         = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts            = dependencies.instalmentAmounts,
      canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
      paymentPlanAnswers           = dependencies.paymentPlanAnswersNoAffordability,
      detailsAboutBankAccount      = DetailsAboutBankAccount(isAccountHolder = true),
      directDebitDetails           = directDebitDetails
    )

    def updateAgreedTermsAndConditionsRequest(isEmailAddressRequired: Boolean): IsEmailAddressRequired = IsEmailAddressRequired(isEmailAddressRequired)

    def journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired: Boolean): Journey.Sa.AgreedTermsAndConditions = {
      val stage =
        if (isEmailAddressRequired) Stage.AfterAgreedTermsAndConditions.EmailAddressRequired
        else Stage.AfterAgreedTermsAndConditions.EmailAddressNotRequired

      Journey.Sa.AgreedTermsAndConditions(
        _id                          = dependencies.journeyId,
        origin                       = Origins.Sa.Pta,
        createdOn                    = dependencies.createdOn,
        sjRequest                    = sjRequest,
        sessionId                    = dependencies.sessionId,
        stage                        = stage,
        affordabilityEnabled         = Some(false),
        correlationId                = dependencies.correlationId,
        taxId                        = saUtr,
        eligibilityCheckResult       = eligibleEligibilityCheckResultSa,
        whyCannotPayInFullAnswers    = WhyCannotPayInFullAnswers.AnswerNotRequired,
        upfrontPaymentAnswers        = dependencies.upfrontPaymentAnswersDeclared,
        extremeDatesResponse         = dependencies.extremeDatesWithUpfrontPayment,
        instalmentAmounts            = dependencies.instalmentAmounts,
        canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
        paymentPlanAnswers           = dependencies.paymentPlanAnswersNoAffordability,
        detailsAboutBankAccount      = DetailsAboutBankAccount(isAccountHolder = true),
        directDebitDetails           = directDebitDetails,
        isEmailAddressRequired       = IsEmailAddressRequired(isEmailAddressRequired)
      )
    }

    def updateSelectedEmailRequest(): Email = dependencies.email

    def journeyAfterSelectedEmail: Journey.Sa.SelectedEmailToBeVerified = Journey.Sa.SelectedEmailToBeVerified(
      _id                          = dependencies.journeyId,
      origin                       = Origins.Sa.Pta,
      createdOn                    = dependencies.createdOn,
      sjRequest                    = sjRequest,
      sessionId                    = dependencies.sessionId,
      stage                        = Stage.AfterSelectedAnEmailToBeVerified.EmailChosen,
      affordabilityEnabled         = Some(false),
      correlationId                = dependencies.correlationId,
      taxId                        = saUtr,
      eligibilityCheckResult       = eligibleEligibilityCheckResultSa,
      whyCannotPayInFullAnswers    = WhyCannotPayInFullAnswers.AnswerNotRequired,
      upfrontPaymentAnswers        = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse         = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts            = dependencies.instalmentAmounts,
      canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
      paymentPlanAnswers           = dependencies.paymentPlanAnswersNoAffordability,
      detailsAboutBankAccount      = DetailsAboutBankAccount(isAccountHolder = true),
      directDebitDetails           = directDebitDetails,
      isEmailAddressRequired       = IsEmailAddressRequired(value = true),
      emailToBeVerified            = dependencies.email
    )

    def journeyAfterEmailVerificationResult(result: EmailVerificationResult): Journey.Sa.EmailVerificationComplete = Journey.Sa.EmailVerificationComplete(
      _id                          = dependencies.journeyId,
      origin                       = Origins.Sa.Pta,
      createdOn                    = dependencies.createdOn,
      sjRequest                    = sjRequest,
      sessionId                    = dependencies.sessionId,
      stage                        = result match {
        case EmailVerificationResult.Verified => Stage.AfterEmailVerificationPhase.VerificationSuccess
        case EmailVerificationResult.Locked   => Stage.AfterEmailVerificationPhase.Locked
      },
      affordabilityEnabled         = Some(false),
      correlationId                = dependencies.correlationId,
      taxId                        = saUtr,
      eligibilityCheckResult       = eligibleEligibilityCheckResultSa,
      whyCannotPayInFullAnswers    = WhyCannotPayInFullAnswers.AnswerNotRequired,
      upfrontPaymentAnswers        = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse         = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts            = dependencies.instalmentAmounts,
      canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
      paymentPlanAnswers           = dependencies.paymentPlanAnswersNoAffordability,
      detailsAboutBankAccount      = DetailsAboutBankAccount(isAccountHolder = true),
      directDebitDetails           = directDebitDetails,
      isEmailAddressRequired       = IsEmailAddressRequired(value = true),
      emailToBeVerified            = dependencies.email,
      emailVerificationResult      = result,
      emailVerificationAnswers     = emailVerificationAnswers(Some(result))
    )

    def updateArrangementRequest(): ArrangementResponse = dependencies.arrangementResponseSa

    def journeyAfterSubmittedArrangementNoAffordability(isEmailAddressRequired: Boolean): Journey.Sa.SubmittedArrangement = Journey.Sa.SubmittedArrangement(
      _id                          = dependencies.journeyId,
      origin                       = Origins.Sa.Pta,
      createdOn                    = dependencies.createdOn,
      sjRequest                    = sjRequest,
      sessionId                    = dependencies.sessionId,
      stage                        = Stage.AfterSubmittedArrangement.Submitted,
      affordabilityEnabled         = Some(false),
      correlationId                = dependencies.correlationId,
      taxId                        = saUtr,
      eligibilityCheckResult       = eligibleEligibilityCheckResultSa,
      whyCannotPayInFullAnswers    = WhyCannotPayInFullAnswers.AnswerNotRequired,
      upfrontPaymentAnswers        = dependencies.upfrontPaymentAnswersDeclared,
      extremeDatesResponse         = dependencies.extremeDatesWithUpfrontPayment,
      instalmentAmounts            = dependencies.instalmentAmounts,
      canPayWithinSixMonthsAnswers = dependencies.canPayWithinSixMonthsNotRequired,
      paymentPlanAnswers           = dependencies.paymentPlanAnswersNoAffordability,
      detailsAboutBankAccount      = DetailsAboutBankAccount(isAccountHolder = true),
      directDebitDetails           = directDebitDetails,
      isEmailAddressRequired       = IsEmailAddressRequired(isEmailAddressRequired),
      emailVerificationAnswers     = if (isEmailAddressRequired) {
        EmailVerificationAnswers.EmailVerified(dependencies.email, EmailVerificationResult.Verified)
      } else {
        EmailVerificationAnswers.NoEmailJourney
      },
      arrangementResponse          = dependencies.arrangementResponseSa
    )

  }
}
