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

package testsupport.testdata

import essttp.journey.model.Journey.ChosenTypeOfBankAccount
import essttp.journey.model.{Journey, JourneyStage, SjRequest, SjResponse}
import essttp.rootmodel.bank.{BankDetails, CanSetUpDirectDebit, TypeOfBankAccount}
import essttp.rootmodel.dates.extremedates.ExtremeDatesResponse
import essttp.rootmodel.dates.startdates.StartDatesResponse
import essttp.rootmodel.ttp.affordability.InstalmentAmounts
import essttp.rootmodel.ttp.affordablequotes.{AffordableQuotesResponse, PaymentPlan}
import essttp.rootmodel.ttp.arrangement.ArrangementResponse
import essttp.rootmodel.{CanPayUpfront, DayOfMonth, IsEmailAddressRequired, MonthlyPaymentAmount, TaxId, UpfrontPaymentAmount}
import essttp.rootmodel.ttp.eligibility.EligibilityCheckResult
import play.api.libs.json.JsNull

trait TdJourneyStructure {

  /** Defining all td requirements for each journey
    */
  def sjRequest: SjRequest

  def sjResponse: SjResponse

  def postPath: String

  def journeyAfterStarted: Journey

  def updateTaxIdRequest(): TaxId

  def journeyAfterDetermineTaxIds: Journey

  def updateEligibilityCheckRequest(): EligibilityCheckResult

  def journeyAfterEligibilityCheckEligible: Journey

  def journeyAfterEligibilityCheckNotEligible: Journey

  def updateCanPayUpfrontYesRequest(): CanPayUpfront

  def updateCanPayUpfrontNoRequest(): CanPayUpfront

  def journeyAfterCanPayUpfrontYes: Journey

  def journeyAfterCanPayUpfrontNo: Journey

  def updateUpfrontPaymentAmountRequest(): UpfrontPaymentAmount

  def journeyAfterUpfrontPaymentAmount: Journey

  def updateExtremeDatesRequest(): ExtremeDatesResponse

  def journeyAfterExtremeDates: Journey

  def updateInstalmentAmountsRequest(): InstalmentAmounts

  def journeyAfterInstalmentAmounts: Journey

  def updateMonthlyPaymentAmountRequest(): MonthlyPaymentAmount

  def journeyAfterMonthlyPaymentAmount: Journey

  def updateDayOfMonthRequest(): DayOfMonth

  def journeyAfterDayOfMonth: Journey

  def updateStartDatesResponse(): StartDatesResponse

  def journeyAfterStartDatesResponse: Journey & JourneyStage.AfterStartDatesResponse

  def updateAffordableQuotesResponse(): AffordableQuotesResponse

  def journeyAfterAffordableQuotesResponse: Journey & JourneyStage.AfterAffordableQuotesResponse

  def updateSelectedPaymentPlanRequest(): PaymentPlan

  def journeyAfterSelectedPaymentPlan: Journey & JourneyStage.AfterSelectedPaymentPlan

  def updateCheckedPaymentPlanRequest(): JsNull.type

  def journeyAfterCheckedPaymentPlanNonAffordability: Journey & JourneyStage.AfterCheckedPaymentPlan

  def updateCanSetUpDirectDebitRequest(isAccountHolder: Boolean): CanSetUpDirectDebit

  def journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(
    isAccountHolder: Boolean
  ): Journey & JourneyStage.AfterEnteredCanYouSetUpDirectDebit

  def journeyAfterChosenTypeOfBankAccount(typeOfBankAccount: TypeOfBankAccount): ChosenTypeOfBankAccount

  def updateDirectDebitDetailsRequest(): BankDetails

  def journeyAfterEnteredDirectDebitDetailsNoAffordability(): Journey & JourneyStage.AfterEnteredDirectDebitDetails

  def updateConfirmedDirectDebitDetailsRequest(): JsNull.type

  def journeyAfterConfirmedDirectDebitDetailsNoAffordability: Journey & JourneyStage.AfterConfirmedDirectDebitDetails

  def updateAgreedTermsAndConditionsRequest(isEmailAddressRequired: Boolean): IsEmailAddressRequired

  def journeyAfterAgreedTermsAndConditionsNoAffordability(
    isEmailAddressRequired: Boolean
  ): Journey & JourneyStage.AfterAgreedTermsAndConditions

  def updateArrangementRequest(): ArrangementResponse

  def journeyAfterSubmittedArrangementNoAffordability(
    isEmailAddressRequired: Boolean = false
  ): Journey & JourneyStage.AfterArrangementSubmitted

}
