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

import essttp.journey.model.ttp.EligibilityCheckResult
import essttp.journey.model.ttp.affordability.InstalmentAmounts
import essttp.journey.model.ttp.affordablequotes.{AffordableQuotesResponse, PaymentPlan}
import essttp.journey.model.ttp.arrangement.ArrangementResponse
import essttp.journey.model.{Journey, SjRequest, SjResponse}
import essttp.rootmodel.bank.{DirectDebitDetails, TypeOfBankAccount}
import essttp.rootmodel.dates.extremedates.ExtremeDatesResponse
import essttp.rootmodel.dates.startdates.StartDatesResponse
import essttp.rootmodel.{CanPayUpfront, DayOfMonth, MonthlyPaymentAmount, TaxId, UpfrontPaymentAmount}
import play.api.libs.json.{JsNull, JsObject}

trait TdJourneyStructure {
  /**
   * Defining all td requirements for each journey
   */
  def sjRequest: SjRequest
  def sjResponse: SjResponse
  def postPath: String
  def sjRequestJson: JsObject

  def journeyAfterStarted: Journey
  def journeyAfterStartedJson: JsObject

  def updateTaxIdRequest(): TaxId
  def updateTaxIdRequestJson(): JsObject

  def journeyAfterDetermineTaxIds: Journey
  def journeyAfterDetermineTaxIdsJson: JsObject

  def updateEligibilityCheckRequest(): EligibilityCheckResult
  def updateEligibilityCheckRequestJson(): JsObject

  def journeyAfterEligibilityCheckEligible: Journey
  def journeyAfterEligibilityCheckEligibleJson: JsObject

  def journeyAfterEligibilityCheckNotEligible: Journey
  def journeyAfterEligibilityCheckNotEligibleJson: JsObject

  def updateCanPayUpfrontYesRequest(): CanPayUpfront
  def updateCanPayUpfrontNoRequest(): CanPayUpfront
  def updateCanPayUpfrontYesRequestJson(): JsObject
  def updateCanPayUpfrontNoRequestJson(): JsObject

  def journeyAfterCanPayUpfrontYes: Journey
  def journeyAfterCanPayUpfrontYesJson: JsObject

  def journeyAfterCanPayUpfrontNo: Journey
  def journeyAfterCanPayUpfrontNoJson: JsObject

  def updateUpfrontPaymentAmountRequest(): UpfrontPaymentAmount
  def updateUpfrontPaymentAmountRequestJson(): JsObject

  def journeyAfterUpfrontPaymentAmount: Journey
  def journeyAfterUpfrontPaymentAmountJson: JsObject

  def updateExtremeDatesRequest(): ExtremeDatesResponse
  def updateExtremeDatesRequestJson(): JsObject

  def journeyAfterExtremeDates: Journey
  def journeyAfterExtremeDatesJson: JsObject

  def updateInstalmentAmountsRequest(): InstalmentAmounts
  def updateInstalmentAmountsRequestJson(): JsObject

  def journeyAfterInstalmentAmounts: Journey
  def journeyAfterInstalmentAmountsJson: JsObject

  def updateMonthlyPaymentAmountRequest(): MonthlyPaymentAmount
  def updateMonthlyPaymentAmountRequestJson(): JsObject

  def journeyAfterMonthlyPaymentAmount: Journey
  def journeyAfterMonthlyPaymentAmountJson: JsObject

  def updateDayOfMonthRequest(): DayOfMonth
  def updateDayOfMonthRequestJson(): JsObject

  def journeyAfterDayOfMonth: Journey
  def journeyAfterDayOfMonthJson: JsObject

  def updateStartDatesResponse(): StartDatesResponse
  def updateStartDatesResponseJson(): JsObject

  def journeyAfterStartDatesResponse: Journey.AfterStartDatesResponse
  def journeyAfterStartDatesResponseJson: JsObject

  def updateAffordableQuotesResponse(): AffordableQuotesResponse
  def updateAffordableQuotesResponseJson(): JsObject

  def journeyAfterAffordableQuotesResponse: Journey.AfterAffordableQuotesResponse
  def journeyAfterAffordableQuotesResponseJson: JsObject

  def updateSelectedPaymentPlanRequest(): PaymentPlan
  def updateSelectedPaymentPlanRequestJson(): JsObject

  def journeyAfterSelectedPaymentPlan: Journey.AfterSelectedPaymentPlan
  def journeyAfterSelectedPaymentPlanJson: JsObject

  def updateCheckedPaymentPlanRequest(): JsNull.type
  def updateCheckedPaymentPlanRequestJson(): JsObject

  def journeyAfterCheckedPaymentPlan: Journey.AfterCheckedPaymentPlan
  def journeyAfterCheckedPaymentPlanJson: JsObject

  def updateChosenTypeOfBankAccountRequest(): TypeOfBankAccount
  def updateChosenTypeOfBankAccountRequestJson(): JsObject

  def journeyAfterChosenTypeOfBankAccount: Journey.AfterChosenTypeOfBankAccount
  def journeyAfterChosenTypeOfBankAccountJson: JsObject

  def updateDirectDebitDetailsRequest(isAccountHolder: Boolean): DirectDebitDetails
  def updateDirectDebitDetailsRequestJson(): JsObject

  def journeyAfterEnteredDirectDebitDetails(isAccountHolder: Boolean): Journey.AfterEnteredDirectDebitDetails
  def journeyAfterEnteredDirectDebitDetailsJson: JsObject

  def updateConfirmedDirectDebitDetailsRequest(): JsNull.type
  def updateConfirmedDirectDebitDetailsJson(): JsObject

  def journeyAfterConfirmedDirectDebitDetails: Journey.AfterConfirmedDirectDebitDetails
  def journeyAfterConfirmedDirectDebitDetailsJson: JsObject

  def updateAgreedTermsAndConditionsRequest(): JsNull.type
  def updateAgreedTermsAndConditionsJson(): JsObject

  def journeyAfterAgreedTermsAndConditions: Journey.AfterAgreedTermsAndConditions
  def journeyAfterAgreedTermsAndConditionsJson: JsObject

  def updateArrangementRequest(): ArrangementResponse
  def updateArrangementRequestJson(): JsObject

  def journeyAfterSubmittedArrangement: Journey.AfterArrangementSubmitted
  def journeyAfterSubmittedArrangementJson: JsObject

}
