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

package journey

import essttp.journey.JourneyConnector
import essttp.journey.model.{CorrelationId, JourneyId, SjResponse}
import essttp.rootmodel.IsEmailAddressRequired
import paymentsEmailVerification.models.EmailVerificationResult
import play.api.mvc.Request
import testsupport.ItSpec
import testsupport.testdata.TdAll

class JourneyControllerSpec extends ItSpec {

  def journeyConnector: JourneyConnector = app.injector.instanceOf[JourneyConnector]

  private val epayeTestNameJourneyStages: String =
    "[StartJourney]" +
      "[UpdateTaxId]" +
      "[UpdateEligibilityCheck]" +
      "[UpdateCanPayUpfront]" +
      "[UpdateUpfrontPaymentAmount]" +
      "[UpdateExtremeDates]" +
      "[UpdateAffordabilityResult]" +
      "[UpdateMonthlyPaymentAmount]" +
      "[UpdateDayOfMonth]" +
      "[UpdateStartDatesResponse]" +
      "[UpdateAffordableQuotes]" +
      "[UpdateSelectedPaymentPlan]" +
      "[UpdateHasCheckedPaymentPlan]" +
      "[UpdateEnteredDetailsAboutBankAccount]" +
      "[UpdateEnteredDirectDebitDetails]" +
      "[UpdateConfirmedDirectDebitDetails]" +
      "[UpdateChosenEmail]" +
      "[UpdateEmailVerificationResult]" +
      "[UpdateSubmittedArrangement]"

  "[Epaye]" - {
    s"[Bta][Happy path with upfront payment]$epayeTestNameJourneyStages" in {
      stubCommonActions()

      val tdAll = new TdAll {
        override val journeyId: JourneyId = journeyIdGenerator.readNextJourneyId()
        override val correlationId: CorrelationId = correlationIdGenerator.readNextCorrelationId()
      }

      implicit val request: Request[_] = tdAll.request
      val response: SjResponse = journeyConnector.Epaye.startJourneyBta(tdAll.EpayeBta.sjRequest).futureValue

      /** Start journey */
      response shouldBe tdAll.EpayeBta.sjResponse
      journeyConnector.getJourney(response.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterStarted

      /** Update tax id */
      journeyConnector.updateTaxId(tdAll.journeyId, tdAll.EpayeBta.updateTaxIdRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterDetermineTaxIds

      /** Update eligibility result */
      journeyConnector.updateEligibilityCheckResult(tdAll.journeyId, tdAll.EpayeBta.updateEligibilityCheckRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterEligibilityCheckEligible

      /** Update CanPayUpfront */
      journeyConnector.updateCanPayUpfront(tdAll.journeyId, tdAll.EpayeBta.updateCanPayUpfrontYesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterCanPayUpfrontYes

      /** Update UpfrontPaymentAmount */
      journeyConnector.updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.EpayeBta.updateUpfrontPaymentAmountRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterUpfrontPaymentAmount

      /** Update ExtremeDates */
      journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.EpayeBta.updateExtremeDatesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterExtremeDates

      /** Update AffordabilityResult */
      journeyConnector.updateAffordabilityResult(tdAll.journeyId, tdAll.EpayeBta.updateInstalmentAmountsRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterInstalmentAmounts

      /** Update MonthlyPaymentAmount */
      journeyConnector.updateMonthlyPaymentAmount(tdAll.journeyId, tdAll.EpayeBta.updateMonthlyPaymentAmountRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterMonthlyPaymentAmount

      /** Update DayOfMonth */
      journeyConnector.updateDayOfMonth(tdAll.journeyId, tdAll.EpayeBta.updateDayOfMonthRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterDayOfMonth

      /** Update StartDates */
      journeyConnector.updateStartDates(tdAll.journeyId, tdAll.EpayeBta.updateStartDatesResponse()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterStartDatesResponse

      /** Update AffordableQuotes */
      journeyConnector.updateAffordableQuotes(tdAll.journeyId, tdAll.EpayeBta.updateAffordableQuotesResponse()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterAffordableQuotesResponse

      /** Update Chosen Instalment plan */
      journeyConnector.updateChosenPaymentPlan(tdAll.journeyId, tdAll.EpayeBta.updateSelectedPaymentPlanRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterSelectedPaymentPlan

      /** Update Checked Instalment plan */
      journeyConnector.updateHasCheckedPaymentPlan(tdAll.journeyId).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterCheckedPaymentPlan

      /** Update Details about Bank Account */
      journeyConnector.updateDetailsAboutBankAccount(tdAll.journeyId, tdAll.EpayeBta.updateDetailsAboutBankAccountRequest(isAccountHolder = true)).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder = true)

      /** Update Direct debit details */
      journeyConnector.updateDirectDebitDetails(tdAll.journeyId, tdAll.EpayeBta.updateDirectDebitDetailsRequest).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterEnteredDirectDebitDetails()

      /** Update Confirm Direct debit details */
      journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterConfirmedDirectDebitDetails

      /** Update Agreed terms and conditions */
      journeyConnector.updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(value = true)).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterAgreedTermsAndConditions(isEmailAddressRequired = true)

      /** Update Email Address */
      journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.email).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterSelectedEmail

      /** Update Email Verification Status */
      journeyConnector.updateEmailVerificationResult(tdAll.journeyId, EmailVerificationResult.Verified).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified)

      /** Update Arrangement (journey completed) */
      journeyConnector.updateArrangement(tdAll.journeyId, tdAll.EpayeBta.updateArrangementRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterSubmittedArrangement(isEmailAddressRequired = true)

      verifyCommonActions(numberOfAuthCalls = 40)
    }

    s"[GovUk][Happy path with upfront payment]$epayeTestNameJourneyStages" in {
      stubCommonActions()

      val tdAll = new TdAll {
        override val journeyId: JourneyId = journeyIdGenerator.readNextJourneyId()
        override val correlationId: CorrelationId = correlationIdGenerator.readNextCorrelationId()
      }

      implicit val request: Request[_] = tdAll.request
      val response: SjResponse = journeyConnector.Epaye.startJourneyGovUk(tdAll.EpayeGovUk.sjRequest).futureValue

      /** Start journey */
      response shouldBe tdAll.EpayeGovUk.sjResponse
      journeyConnector.getJourney(response.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterStarted

      /** Update tax id */
      journeyConnector.updateTaxId(tdAll.journeyId, tdAll.EpayeGovUk.updateTaxIdRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterDetermineTaxIds

      /** Update eligibility result */
      journeyConnector.updateEligibilityCheckResult(tdAll.journeyId, tdAll.EpayeGovUk.updateEligibilityCheckRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterEligibilityCheckEligible

      /** Update CanPayUpfront */
      journeyConnector.updateCanPayUpfront(tdAll.journeyId, tdAll.EpayeGovUk.updateCanPayUpfrontYesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterCanPayUpfrontYes

      /** Update UpfrontPaymentAmount */
      journeyConnector.updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.EpayeGovUk.updateUpfrontPaymentAmountRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterUpfrontPaymentAmount

      /** Update ExtremeDates */
      journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.EpayeGovUk.updateExtremeDatesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterExtremeDates

      /** Update AffordabilityResult */
      journeyConnector.updateAffordabilityResult(tdAll.journeyId, tdAll.EpayeGovUk.updateInstalmentAmountsRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterInstalmentAmounts

      /** Update MonthlyPaymentAmount */
      journeyConnector.updateMonthlyPaymentAmount(tdAll.journeyId, tdAll.EpayeGovUk.updateMonthlyPaymentAmountRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterMonthlyPaymentAmount

      /** Update DayOfMonth */
      journeyConnector.updateDayOfMonth(tdAll.journeyId, tdAll.EpayeGovUk.updateDayOfMonthRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterDayOfMonth

      /** Update StartDates */
      journeyConnector.updateStartDates(tdAll.journeyId, tdAll.EpayeGovUk.updateStartDatesResponse()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterStartDatesResponse

      /** Update AffordableQuotes */
      journeyConnector.updateAffordableQuotes(tdAll.journeyId, tdAll.EpayeGovUk.updateAffordableQuotesResponse()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterAffordableQuotesResponse

      /** Update Chosen Instalment plan */
      journeyConnector.updateChosenPaymentPlan(tdAll.journeyId, tdAll.EpayeGovUk.updateSelectedPaymentPlanRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterSelectedPaymentPlan

      /** Update Checked Instalment plan */
      journeyConnector.updateHasCheckedPaymentPlan(tdAll.journeyId).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterCheckedPaymentPlan

      /** Update Details about Bank Account */
      journeyConnector.updateDetailsAboutBankAccount(tdAll.journeyId, tdAll.EpayeGovUk.updateDetailsAboutBankAccountRequest(isAccountHolder = true)).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder = true)

      /** Update Direct debit details */
      journeyConnector.updateDirectDebitDetails(tdAll.journeyId, tdAll.EpayeGovUk.updateDirectDebitDetailsRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterEnteredDirectDebitDetails()

      /** Update Confirm Direct debit details */
      journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterConfirmedDirectDebitDetails

      /** Update Agreed terms and conditions */
      journeyConnector.updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(value = true)).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterAgreedTermsAndConditions(isEmailAddressRequired = true)

      /** Update Email Address */
      journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.email).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterSelectedEmail

      /** Update Email Verification Status */
      journeyConnector.updateEmailVerificationResult(tdAll.journeyId, EmailVerificationResult.Verified).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified)

      /** Update Arrangement (journey completed) */
      journeyConnector.updateArrangement(tdAll.journeyId, tdAll.EpayeGovUk.updateArrangementRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterSubmittedArrangement(isEmailAddressRequired = true)

      verifyCommonActions(numberOfAuthCalls = 40)
    }

    s"[DetachedUrl][Happy path with upfront payment]$epayeTestNameJourneyStages" in {
      stubCommonActions()

      val tdAll = new TdAll {
        override val journeyId: JourneyId = journeyIdGenerator.readNextJourneyId()
        override val correlationId: CorrelationId = correlationIdGenerator.readNextCorrelationId()
      }

      implicit val request: Request[_] = tdAll.request
      val response: SjResponse = journeyConnector.Epaye.startJourneyDetachedUrl(tdAll.EpayeDetachedUrl.sjRequest).futureValue

      /** Start journey * */
      response shouldBe tdAll.EpayeDetachedUrl.sjResponse
      journeyConnector.getJourney(response.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterStarted

      /** Update tax id * */
      journeyConnector.updateTaxId(tdAll.journeyId, tdAll.EpayeDetachedUrl.updateTaxIdRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterDetermineTaxIds

      /** Update eligibility result * */
      journeyConnector.updateEligibilityCheckResult(tdAll.journeyId, tdAll.EpayeDetachedUrl.updateEligibilityCheckRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterEligibilityCheckEligible

      /** Update CanPayUpfront * */
      journeyConnector.updateCanPayUpfront(tdAll.journeyId, tdAll.EpayeDetachedUrl.updateCanPayUpfrontYesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterCanPayUpfrontYes

      /** Update UpfrontPaymentAmount */
      journeyConnector.updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.EpayeDetachedUrl.updateUpfrontPaymentAmountRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterUpfrontPaymentAmount

      /** Update ExtremeDates */
      journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.EpayeDetachedUrl.updateExtremeDatesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterExtremeDates

      /** Update AffordabilityResult */
      journeyConnector.updateAffordabilityResult(tdAll.journeyId, tdAll.EpayeDetachedUrl.updateInstalmentAmountsRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterInstalmentAmounts

      /** Update MonthlyPaymentAmount */
      journeyConnector.updateMonthlyPaymentAmount(tdAll.journeyId, tdAll.EpayeDetachedUrl.updateMonthlyPaymentAmountRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterMonthlyPaymentAmount

      /** Update DayOfMonth */
      journeyConnector.updateDayOfMonth(tdAll.journeyId, tdAll.EpayeDetachedUrl.updateDayOfMonthRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterDayOfMonth

      /** Update StartDates */
      journeyConnector.updateStartDates(tdAll.journeyId, tdAll.EpayeDetachedUrl.updateStartDatesResponse()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterStartDatesResponse

      /** Update AffordableQuotes */
      journeyConnector.updateAffordableQuotes(tdAll.journeyId, tdAll.EpayeDetachedUrl.updateAffordableQuotesResponse()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterAffordableQuotesResponse

      /** Update Chosen Instalment plan */
      journeyConnector.updateChosenPaymentPlan(tdAll.journeyId, tdAll.EpayeDetachedUrl.updateSelectedPaymentPlanRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterSelectedPaymentPlan

      /** Update Checked Instalment plan */
      journeyConnector.updateHasCheckedPaymentPlan(tdAll.journeyId).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterCheckedPaymentPlan

      /** Update Details about Bank Account */
      journeyConnector.updateDetailsAboutBankAccount(tdAll.journeyId, tdAll.EpayeDetachedUrl.updateDetailsAboutBankAccountRequest(isAccountHolder = true)).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder = true)

      /** Update Direct debit details */
      journeyConnector.updateDirectDebitDetails(tdAll.journeyId, tdAll.EpayeDetachedUrl.updateDirectDebitDetailsRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterEnteredDirectDebitDetails()

      /** Update Confirm Direct debit details */
      journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterConfirmedDirectDebitDetails

      /** Update Agreed terms and conditions */
      journeyConnector.updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(value = true)).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterAgreedTermsAndConditions(isEmailAddressRequired = true)

      /** Update Email Address */
      journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.email).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterSelectedEmail

      /** Update Email Verification Status */
      journeyConnector.updateEmailVerificationResult(tdAll.journeyId, EmailVerificationResult.Verified).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified)

      /** Update Arrangement (journey completed) */
      journeyConnector.updateArrangement(tdAll.journeyId, tdAll.EpayeDetachedUrl.updateArrangementRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterSubmittedArrangement(isEmailAddressRequired = true)

      verifyCommonActions(numberOfAuthCalls = 40)
    }

    s"[EpayeService][Happy path with upfront payment]$epayeTestNameJourneyStages" in {
      stubCommonActions()

      val tdAll = new TdAll {
        override val journeyId: JourneyId = journeyIdGenerator.readNextJourneyId()
        override val correlationId: CorrelationId = correlationIdGenerator.readNextCorrelationId()
      }

      implicit val request: Request[_] = tdAll.request
      val response: SjResponse = journeyConnector.Epaye.startJourneyEpayeService(tdAll.EpayeEpayeService.sjRequest).futureValue

      /** Start journey * */
      response shouldBe tdAll.EpayeEpayeService.sjResponse
      journeyConnector.getJourney(response.journeyId).futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterStarted

      /** Update tax id * */
      journeyConnector.updateTaxId(tdAll.journeyId, tdAll.EpayeEpayeService.updateTaxIdRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterDetermineTaxIds

      /** Update eligibility result * */
      journeyConnector.updateEligibilityCheckResult(tdAll.journeyId, tdAll.EpayeEpayeService.updateEligibilityCheckRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterEligibilityCheckEligible

      /** Update CanPayUpfront * */
      journeyConnector.updateCanPayUpfront(tdAll.journeyId, tdAll.EpayeEpayeService.updateCanPayUpfrontYesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterCanPayUpfrontYes

      /** Update UpfrontPaymentAmount */
      journeyConnector.updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.EpayeEpayeService.updateUpfrontPaymentAmountRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterUpfrontPaymentAmount

      /** Update ExtremeDates */
      journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.EpayeEpayeService.updateExtremeDatesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterExtremeDates

      /** Update AffordabilityResult */
      journeyConnector.updateAffordabilityResult(tdAll.journeyId, tdAll.EpayeEpayeService.updateInstalmentAmountsRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterInstalmentAmounts

      /** Update MonthlyPaymentAmount */
      journeyConnector.updateMonthlyPaymentAmount(tdAll.journeyId, tdAll.EpayeEpayeService.updateMonthlyPaymentAmountRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterMonthlyPaymentAmount

      /** Update DayOfMonth */
      journeyConnector.updateDayOfMonth(tdAll.journeyId, tdAll.EpayeEpayeService.updateDayOfMonthRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterDayOfMonth

      /** Update StartDates */
      journeyConnector.updateStartDates(tdAll.journeyId, tdAll.EpayeEpayeService.updateStartDatesResponse()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterStartDatesResponse

      /** Update AffordableQuotes */
      journeyConnector.updateAffordableQuotes(tdAll.journeyId, tdAll.EpayeEpayeService.updateAffordableQuotesResponse()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterAffordableQuotesResponse

      /** Update Chosen Instalment plan */
      journeyConnector.updateChosenPaymentPlan(tdAll.journeyId, tdAll.EpayeEpayeService.updateSelectedPaymentPlanRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterSelectedPaymentPlan

      /** Update Checked Instalment plan */
      journeyConnector.updateHasCheckedPaymentPlan(tdAll.journeyId).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterCheckedPaymentPlan

      /** Update Details about Bank Account */
      journeyConnector.updateDetailsAboutBankAccount(tdAll.journeyId, tdAll.EpayeEpayeService.updateDetailsAboutBankAccountRequest(isAccountHolder = true)).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder = true)

      /** Update Direct debit details */
      journeyConnector.updateDirectDebitDetails(tdAll.journeyId, tdAll.EpayeEpayeService.updateDirectDebitDetailsRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterEnteredDirectDebitDetails()

      /** Update Confirm Direct debit details */
      journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterConfirmedDirectDebitDetails

      /** Update Agreed terms and conditions */
      journeyConnector.updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(value = true)).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterAgreedTermsAndConditions(isEmailAddressRequired = true)

      /** Update Email Address */
      journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.email).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterSelectedEmail

      /** Update Email Verification Status */
      journeyConnector.updateEmailVerificationResult(tdAll.journeyId, EmailVerificationResult.Verified).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified)

      /** Update Arrangement (journey completed) */
      journeyConnector.updateArrangement(tdAll.journeyId, tdAll.EpayeEpayeService.updateArrangementRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterSubmittedArrangement(isEmailAddressRequired = true)

      verifyCommonActions(numberOfAuthCalls = 40)
    }
  }

  private val vatTestNameJourneyStages: String =
    "[StartJourney]" +
      "[UpdateTaxId]" +
      "[UpdateEligibilityCheck]" +
      "[UpdateCanPayUpfront]" +
      "[UpdateUpfrontPaymentAmount]" +
      "[UpdateExtremeDates]" +
      "[UpdateAffordabilityResult]" +
      "[UpdateMonthlyPaymentAmount]" +
      "[UpdateDayOfMonth]" +
      "[UpdateStartDatesResponse]" +
      "[UpdateAffordableQuotes]" +
      "[UpdateSelectedPaymentPlan]" +
      "[UpdateHasCheckedPaymentPlan]" +
      "[EnteredDetailsAboutBankAccount]" +
      "[UpdateEnteredDirectDebitDetails]" +
      "[UpdateConfirmedDirectDebitDetails]" +
      "[UpdateHasAgreedTermsAndConditions]"

  "[Vat]" - {

    s"[Bta]$vatTestNameJourneyStages" in {
      stubCommonActions()
      val tdAll = new TdAll {
        override val journeyId: JourneyId = journeyIdGenerator.readNextJourneyId()
        override val correlationId: CorrelationId = correlationIdGenerator.readNextCorrelationId()
      }
      implicit val request: Request[_] = tdAll.request
      val response: SjResponse = journeyConnector.Vat.startJourneyBta(tdAll.VatBta.sjRequest).futureValue

      /** Start journey */
      response shouldBe tdAll.VatBta.sjResponse
      journeyConnector.getJourney(response.journeyId).futureValue shouldBe tdAll.VatBta.journeyAfterStarted

      /** Update tax id */
      journeyConnector.updateTaxId(tdAll.journeyId, tdAll.VatBta.updateTaxIdRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatBta.journeyAfterDetermineTaxIds

      /** Update eligibility result * */
      journeyConnector.updateEligibilityCheckResult(tdAll.journeyId, tdAll.VatBta.updateEligibilityCheckRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatBta.journeyAfterEligibilityCheckEligible

      /** Update CanPayUpfront */
      journeyConnector.updateCanPayUpfront(tdAll.journeyId, tdAll.VatBta.updateCanPayUpfrontYesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatBta.journeyAfterCanPayUpfrontYes

      /** Update UpfrontPaymentAmount */
      journeyConnector.updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.VatBta.updateUpfrontPaymentAmountRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatBta.journeyAfterUpfrontPaymentAmount

      /** Update ExtremeDates */
      journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.VatBta.updateExtremeDatesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatBta.journeyAfterExtremeDates

      /** Update AffordabilityResult */
      journeyConnector.updateAffordabilityResult(tdAll.journeyId, tdAll.VatBta.updateInstalmentAmountsRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatBta.journeyAfterInstalmentAmounts

      /** Update MonthlyPaymentAmount */
      journeyConnector.updateMonthlyPaymentAmount(tdAll.journeyId, tdAll.VatBta.updateMonthlyPaymentAmountRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatBta.journeyAfterMonthlyPaymentAmount

      /** Update DayOfMonth */
      journeyConnector.updateDayOfMonth(tdAll.journeyId, tdAll.VatBta.updateDayOfMonthRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatBta.journeyAfterDayOfMonth

      /** Update StartDates */
      journeyConnector.updateStartDates(tdAll.journeyId, tdAll.VatBta.updateStartDatesResponse()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatBta.journeyAfterStartDatesResponse

      /** Update AffordableQuotes */
      journeyConnector.updateAffordableQuotes(tdAll.journeyId, tdAll.VatBta.updateAffordableQuotesResponse()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatBta.journeyAfterAffordableQuotesResponse

      /** Update Chosen Instalment plan */
      journeyConnector.updateChosenPaymentPlan(tdAll.journeyId, tdAll.VatBta.updateSelectedPaymentPlanRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatBta.journeyAfterSelectedPaymentPlan

      /** Update Checked Instalment plan */
      journeyConnector.updateHasCheckedPaymentPlan(tdAll.journeyId).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatBta.journeyAfterCheckedPaymentPlan

      /** Update Details about Bank Account */
      journeyConnector.updateDetailsAboutBankAccount(tdAll.journeyId, tdAll.VatBta.updateDetailsAboutBankAccountRequest(isAccountHolder = true)).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatBta.journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder = true)

      /** Update Direct debit details */
      journeyConnector.updateDirectDebitDetails(tdAll.journeyId, tdAll.VatBta.updateDirectDebitDetailsRequest).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatBta.journeyAfterEnteredDirectDebitDetails()

      /** Update Confirm Direct debit details */
      journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatBta.journeyAfterConfirmedDirectDebitDetails

      /** Update Agreed terms and conditions */
      journeyConnector.updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(value = true)).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatBta.journeyAfterAgreedTermsAndConditions(isEmailAddressRequired = true)

      /** Update Email Address */
      journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.email).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatBta.journeyAfterSelectedEmail

      /** Update Email Verification Status */
      journeyConnector.updateEmailVerificationResult(tdAll.journeyId, EmailVerificationResult.Verified).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatBta.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified)

      /** Update Arrangement (journey completed) */
      journeyConnector.updateArrangement(tdAll.journeyId, tdAll.VatBta.updateArrangementRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatBta.journeyAfterSubmittedArrangement(isEmailAddressRequired = true)

      verifyCommonActions(numberOfAuthCalls = 40)
    }

    s"[GovUk]$vatTestNameJourneyStages" in {
      stubCommonActions()
      val tdAll = new TdAll {
        override val journeyId: JourneyId = journeyIdGenerator.readNextJourneyId()
        override val correlationId: CorrelationId = correlationIdGenerator.readNextCorrelationId()
      }
      implicit val request: Request[_] = tdAll.request
      val response: SjResponse = journeyConnector.Vat.startJourneyGovUk(tdAll.VatGovUk.sjRequest).futureValue

      /** Start journey */
      response shouldBe tdAll.VatGovUk.sjResponse
      journeyConnector.getJourney(response.journeyId).futureValue shouldBe tdAll.VatGovUk.journeyAfterStarted

      /** Update tax id */
      journeyConnector.updateTaxId(tdAll.journeyId, tdAll.VatGovUk.updateTaxIdRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatGovUk.journeyAfterDetermineTaxIds

      /** Update eligibility result * */
      journeyConnector.updateEligibilityCheckResult(tdAll.journeyId, tdAll.VatGovUk.updateEligibilityCheckRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatGovUk.journeyAfterEligibilityCheckEligible

      /** Update CanPayUpfront */
      journeyConnector.updateCanPayUpfront(tdAll.journeyId, tdAll.VatGovUk.updateCanPayUpfrontYesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatGovUk.journeyAfterCanPayUpfrontYes

      /** Update UpfrontPaymentAmount */
      journeyConnector.updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.VatGovUk.updateUpfrontPaymentAmountRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatGovUk.journeyAfterUpfrontPaymentAmount

      /** Update ExtremeDates */
      journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.VatGovUk.updateExtremeDatesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatGovUk.journeyAfterExtremeDates

      /** Update AffordabilityResult */
      journeyConnector.updateAffordabilityResult(tdAll.journeyId, tdAll.VatGovUk.updateInstalmentAmountsRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatGovUk.journeyAfterInstalmentAmounts

      /** Update MonthlyPaymentAmount */
      journeyConnector.updateMonthlyPaymentAmount(tdAll.journeyId, tdAll.VatGovUk.updateMonthlyPaymentAmountRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatGovUk.journeyAfterMonthlyPaymentAmount

      /** Update DayOfMonth */
      journeyConnector.updateDayOfMonth(tdAll.journeyId, tdAll.VatGovUk.updateDayOfMonthRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatGovUk.journeyAfterDayOfMonth

      /** Update StartDates */
      journeyConnector.updateStartDates(tdAll.journeyId, tdAll.VatGovUk.updateStartDatesResponse()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatGovUk.journeyAfterStartDatesResponse

      /** Update AffordableQuotes */
      journeyConnector.updateAffordableQuotes(tdAll.journeyId, tdAll.VatGovUk.updateAffordableQuotesResponse()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatGovUk.journeyAfterAffordableQuotesResponse

      /** Update Chosen Instalment plan */
      journeyConnector.updateChosenPaymentPlan(tdAll.journeyId, tdAll.VatGovUk.updateSelectedPaymentPlanRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatGovUk.journeyAfterSelectedPaymentPlan

      /** Update Checked Instalment plan */
      journeyConnector.updateHasCheckedPaymentPlan(tdAll.journeyId).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatGovUk.journeyAfterCheckedPaymentPlan

      /** Update Details about Bank Account */
      journeyConnector.updateDetailsAboutBankAccount(tdAll.journeyId, tdAll.VatGovUk.updateDetailsAboutBankAccountRequest(isAccountHolder = true)).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatGovUk.journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder = true)

      /** Update Direct debit details */
      journeyConnector.updateDirectDebitDetails(tdAll.journeyId, tdAll.VatGovUk.updateDirectDebitDetailsRequest).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatGovUk.journeyAfterEnteredDirectDebitDetails()

      /** Update Confirm Direct debit details */
      journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatGovUk.journeyAfterConfirmedDirectDebitDetails

      /** Update Agreed terms and conditions */
      journeyConnector.updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(value = true)).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatGovUk.journeyAfterAgreedTermsAndConditions(isEmailAddressRequired = true)

      /** Update Email Address */
      journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.email).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatGovUk.journeyAfterSelectedEmail

      /** Update Email Verification Status */
      journeyConnector.updateEmailVerificationResult(tdAll.journeyId, EmailVerificationResult.Verified).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatGovUk.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified)

      /** Update Arrangement (journey completed) */
      journeyConnector.updateArrangement(tdAll.journeyId, tdAll.VatGovUk.updateArrangementRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatGovUk.journeyAfterSubmittedArrangement(isEmailAddressRequired = true)

      verifyCommonActions(numberOfAuthCalls = 40)
    }

    s"[DetachedUrl]$vatTestNameJourneyStages" in {
      stubCommonActions()
      val tdAll = new TdAll {
        override val journeyId: JourneyId = journeyIdGenerator.readNextJourneyId()
        override val correlationId: CorrelationId = correlationIdGenerator.readNextCorrelationId()
      }
      implicit val request: Request[_] = tdAll.request
      val response: SjResponse = journeyConnector.Vat.startJourneyDetachedUrl(tdAll.VatDetachedUrl.sjRequest).futureValue

      /** Start journey */
      response shouldBe tdAll.VatDetachedUrl.sjResponse
      journeyConnector.getJourney(response.journeyId).futureValue shouldBe tdAll.VatDetachedUrl.journeyAfterStarted

      /** Update tax id */
      journeyConnector.updateTaxId(tdAll.journeyId, tdAll.VatDetachedUrl.updateTaxIdRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatDetachedUrl.journeyAfterDetermineTaxIds

      /** Update eligibility result * */
      journeyConnector.updateEligibilityCheckResult(tdAll.journeyId, tdAll.VatDetachedUrl.updateEligibilityCheckRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatDetachedUrl.journeyAfterEligibilityCheckEligible

      /** Update CanPayUpfront */
      journeyConnector.updateCanPayUpfront(tdAll.journeyId, tdAll.VatDetachedUrl.updateCanPayUpfrontYesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatDetachedUrl.journeyAfterCanPayUpfrontYes

      /** Update UpfrontPaymentAmount */
      journeyConnector.updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.VatDetachedUrl.updateUpfrontPaymentAmountRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatDetachedUrl.journeyAfterUpfrontPaymentAmount

      /** Update ExtremeDates */
      journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.VatDetachedUrl.updateExtremeDatesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatDetachedUrl.journeyAfterExtremeDates

      /** Update AffordabilityResult */
      journeyConnector.updateAffordabilityResult(tdAll.journeyId, tdAll.VatDetachedUrl.updateInstalmentAmountsRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatDetachedUrl.journeyAfterInstalmentAmounts

      /** Update MonthlyPaymentAmount */
      journeyConnector.updateMonthlyPaymentAmount(tdAll.journeyId, tdAll.VatDetachedUrl.updateMonthlyPaymentAmountRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatDetachedUrl.journeyAfterMonthlyPaymentAmount

      /** Update DayOfMonth */
      journeyConnector.updateDayOfMonth(tdAll.journeyId, tdAll.VatDetachedUrl.updateDayOfMonthRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatDetachedUrl.journeyAfterDayOfMonth

      /** Update StartDates */
      journeyConnector.updateStartDates(tdAll.journeyId, tdAll.VatDetachedUrl.updateStartDatesResponse()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatDetachedUrl.journeyAfterStartDatesResponse

      /** Update AffordableQuotes */
      journeyConnector.updateAffordableQuotes(tdAll.journeyId, tdAll.VatDetachedUrl.updateAffordableQuotesResponse()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatDetachedUrl.journeyAfterAffordableQuotesResponse

      /** Update Chosen Instalment plan */
      journeyConnector.updateChosenPaymentPlan(tdAll.journeyId, tdAll.VatDetachedUrl.updateSelectedPaymentPlanRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatDetachedUrl.journeyAfterSelectedPaymentPlan

      /** Update Checked Instalment plan */
      journeyConnector.updateHasCheckedPaymentPlan(tdAll.journeyId).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatDetachedUrl.journeyAfterCheckedPaymentPlan

      /** Update Details about Bank Account */
      journeyConnector.updateDetailsAboutBankAccount(tdAll.journeyId, tdAll.VatDetachedUrl.updateDetailsAboutBankAccountRequest(isAccountHolder = true)).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatDetachedUrl.journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder = true)

      /** Update Direct debit details */
      journeyConnector.updateDirectDebitDetails(tdAll.journeyId, tdAll.VatDetachedUrl.updateDirectDebitDetailsRequest).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatDetachedUrl.journeyAfterEnteredDirectDebitDetails()

      /** Update Confirm Direct debit details */
      journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatDetachedUrl.journeyAfterConfirmedDirectDebitDetails

      /** Update Agreed terms and conditions */
      journeyConnector.updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(value = true)).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatDetachedUrl.journeyAfterAgreedTermsAndConditions(isEmailAddressRequired = true)

      /** Update Email Address */
      journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.email).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatDetachedUrl.journeyAfterSelectedEmail

      /** Update Email Verification Status */
      journeyConnector.updateEmailVerificationResult(tdAll.journeyId, EmailVerificationResult.Verified).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatDetachedUrl.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified)

      /** Update Arrangement (journey completed) */
      journeyConnector.updateArrangement(tdAll.journeyId, tdAll.VatDetachedUrl.updateArrangementRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatDetachedUrl.journeyAfterSubmittedArrangement(isEmailAddressRequired = true)

      verifyCommonActions(numberOfAuthCalls = 40)
    }

    s"[VatService]$vatTestNameJourneyStages" in {
      stubCommonActions()
      val tdAll = new TdAll {
        override val journeyId: JourneyId = journeyIdGenerator.readNextJourneyId()
        override val correlationId: CorrelationId = correlationIdGenerator.readNextCorrelationId()
      }
      implicit val request: Request[_] = tdAll.request
      val response: SjResponse = journeyConnector.Vat.startJourneyVatService(tdAll.VatVatService.sjRequest).futureValue

      /** Start journey */
      response shouldBe tdAll.VatVatService.sjResponse
      journeyConnector.getJourney(response.journeyId).futureValue shouldBe tdAll.VatVatService.journeyAfterStarted

      /** Update tax id */
      journeyConnector.updateTaxId(tdAll.journeyId, tdAll.VatVatService.updateTaxIdRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatService.journeyAfterDetermineTaxIds

      /** Update eligibility result * */
      journeyConnector.updateEligibilityCheckResult(tdAll.journeyId, tdAll.VatVatService.updateEligibilityCheckRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatService.journeyAfterEligibilityCheckEligible

      /** Update CanPayUpfront */
      journeyConnector.updateCanPayUpfront(tdAll.journeyId, tdAll.VatVatService.updateCanPayUpfrontYesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatService.journeyAfterCanPayUpfrontYes

      /** Update UpfrontPaymentAmount */
      journeyConnector.updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.VatVatService.updateUpfrontPaymentAmountRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatService.journeyAfterUpfrontPaymentAmount

      /** Update ExtremeDates */
      journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.VatVatService.updateExtremeDatesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatService.journeyAfterExtremeDates

      /** Update AffordabilityResult */
      journeyConnector.updateAffordabilityResult(tdAll.journeyId, tdAll.VatVatService.updateInstalmentAmountsRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatService.journeyAfterInstalmentAmounts

      /** Update MonthlyPaymentAmount */
      journeyConnector.updateMonthlyPaymentAmount(tdAll.journeyId, tdAll.VatVatService.updateMonthlyPaymentAmountRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatService.journeyAfterMonthlyPaymentAmount

      /** Update DayOfMonth */
      journeyConnector.updateDayOfMonth(tdAll.journeyId, tdAll.VatVatService.updateDayOfMonthRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatService.journeyAfterDayOfMonth

      /** Update StartDates */
      journeyConnector.updateStartDates(tdAll.journeyId, tdAll.VatVatService.updateStartDatesResponse()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatService.journeyAfterStartDatesResponse

      /** Update AffordableQuotes */
      journeyConnector.updateAffordableQuotes(tdAll.journeyId, tdAll.VatVatService.updateAffordableQuotesResponse()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatService.journeyAfterAffordableQuotesResponse

      /** Update Chosen Instalment plan */
      journeyConnector.updateChosenPaymentPlan(tdAll.journeyId, tdAll.VatVatService.updateSelectedPaymentPlanRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatService.journeyAfterSelectedPaymentPlan

      /** Update Checked Instalment plan */
      journeyConnector.updateHasCheckedPaymentPlan(tdAll.journeyId).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatService.journeyAfterCheckedPaymentPlan

      /** Update Details about Bank Account */
      journeyConnector.updateDetailsAboutBankAccount(tdAll.journeyId, tdAll.VatVatService.updateDetailsAboutBankAccountRequest(isAccountHolder = true)).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatService.journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder = true)

      /** Update Direct debit details */
      journeyConnector.updateDirectDebitDetails(tdAll.journeyId, tdAll.VatVatService.updateDirectDebitDetailsRequest).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatService.journeyAfterEnteredDirectDebitDetails()

      /** Update Confirm Direct debit details */
      journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatService.journeyAfterConfirmedDirectDebitDetails

      /** Update Agreed terms and conditions */
      journeyConnector.updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(value = true)).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatService.journeyAfterAgreedTermsAndConditions(isEmailAddressRequired = true)

      /** Update Email Address */
      journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.email).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatService.journeyAfterSelectedEmail

      /** Update Email Verification Status */
      journeyConnector.updateEmailVerificationResult(tdAll.journeyId, EmailVerificationResult.Verified).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatService.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified)

      /** Update Arrangement (journey completed) */
      journeyConnector.updateArrangement(tdAll.journeyId, tdAll.VatVatService.updateArrangementRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatService.journeyAfterSubmittedArrangement(isEmailAddressRequired = true)

      verifyCommonActions(numberOfAuthCalls = 40)
    }

    s"[VatPenalties]$vatTestNameJourneyStages" in {
      stubCommonActions()
      val tdAll = new TdAll {
        override val journeyId: JourneyId = journeyIdGenerator.readNextJourneyId()
        override val correlationId: CorrelationId = correlationIdGenerator.readNextCorrelationId()
      }
      implicit val request: Request[_] = tdAll.request
      val response: SjResponse = journeyConnector.Vat.startJourneyVatPenalties(tdAll.VatVatPenalties.sjRequest).futureValue

      /** Start journey */
      response shouldBe tdAll.VatVatPenalties.sjResponse
      journeyConnector.getJourney(response.journeyId).futureValue shouldBe tdAll.VatVatPenalties.journeyAfterStarted

      /** Update tax id */
      journeyConnector.updateTaxId(tdAll.journeyId, tdAll.VatVatPenalties.updateTaxIdRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatPenalties.journeyAfterDetermineTaxIds

      /** Update eligibility result * */
      journeyConnector.updateEligibilityCheckResult(tdAll.journeyId, tdAll.VatVatPenalties.updateEligibilityCheckRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatPenalties.journeyAfterEligibilityCheckEligible

      /** Update CanPayUpfront */
      journeyConnector.updateCanPayUpfront(tdAll.journeyId, tdAll.VatVatPenalties.updateCanPayUpfrontYesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatPenalties.journeyAfterCanPayUpfrontYes

      /** Update UpfrontPaymentAmount */
      journeyConnector.updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.VatVatPenalties.updateUpfrontPaymentAmountRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatPenalties.journeyAfterUpfrontPaymentAmount

      /** Update ExtremeDates */
      journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.VatVatPenalties.updateExtremeDatesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatPenalties.journeyAfterExtremeDates

      /** Update AffordabilityResult */
      journeyConnector.updateAffordabilityResult(tdAll.journeyId, tdAll.VatVatPenalties.updateInstalmentAmountsRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatPenalties.journeyAfterInstalmentAmounts

      /** Update MonthlyPaymentAmount */
      journeyConnector.updateMonthlyPaymentAmount(tdAll.journeyId, tdAll.VatVatPenalties.updateMonthlyPaymentAmountRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatPenalties.journeyAfterMonthlyPaymentAmount

      /** Update DayOfMonth */
      journeyConnector.updateDayOfMonth(tdAll.journeyId, tdAll.VatVatPenalties.updateDayOfMonthRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatPenalties.journeyAfterDayOfMonth

      /** Update StartDates */
      journeyConnector.updateStartDates(tdAll.journeyId, tdAll.VatVatPenalties.updateStartDatesResponse()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatPenalties.journeyAfterStartDatesResponse

      /** Update AffordableQuotes */
      journeyConnector.updateAffordableQuotes(tdAll.journeyId, tdAll.VatVatPenalties.updateAffordableQuotesResponse()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatPenalties.journeyAfterAffordableQuotesResponse

      /** Update Chosen Instalment plan */
      journeyConnector.updateChosenPaymentPlan(tdAll.journeyId, tdAll.VatVatPenalties.updateSelectedPaymentPlanRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatPenalties.journeyAfterSelectedPaymentPlan

      /** Update Checked Instalment plan */
      journeyConnector.updateHasCheckedPaymentPlan(tdAll.journeyId).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatPenalties.journeyAfterCheckedPaymentPlan

      /** Update Details about Bank Account */
      journeyConnector.updateDetailsAboutBankAccount(tdAll.journeyId, tdAll.VatVatPenalties.updateDetailsAboutBankAccountRequest(isAccountHolder = true)).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatPenalties.journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder = true)

      /** Update Direct debit details */
      journeyConnector.updateDirectDebitDetails(tdAll.journeyId, tdAll.VatVatPenalties.updateDirectDebitDetailsRequest).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatPenalties.journeyAfterEnteredDirectDebitDetails()

      /** Update Confirm Direct debit details */
      journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatPenalties.journeyAfterConfirmedDirectDebitDetails

      /** Update Agreed terms and conditions */
      journeyConnector.updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(value = true)).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatPenalties.journeyAfterAgreedTermsAndConditions(isEmailAddressRequired = true)

      /** Update Email Address */
      journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.email).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatPenalties.journeyAfterSelectedEmail

      /** Update Email Verification Status */
      journeyConnector.updateEmailVerificationResult(tdAll.journeyId, EmailVerificationResult.Verified).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatPenalties.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified)

      /** Update Arrangement (journey completed) */
      journeyConnector.updateArrangement(tdAll.journeyId, tdAll.VatVatPenalties.updateArrangementRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatPenalties.journeyAfterSubmittedArrangement(isEmailAddressRequired = true)

      verifyCommonActions(numberOfAuthCalls = 40)
    }
  }

  private val saTestNameJourneyStages: String =
    "[StartJourney]" +
      "[UpdateTaxId]" +
      "[UpdateEligibilityCheck]" +
      "[UpdateCanPayUpfront]" +
      "[UpdateUpfrontPaymentAmount]" +
      "[UpdateExtremeDates]" +
      "[UpdateAffordabilityResult]" +
      "[UpdateMonthlyPaymentAmount]" +
      "[UpdateDayOfMonth]" +
      "[UpdateStartDatesResponse]" +
      "[UpdateAffordableQuotes]" +
      "[UpdateSelectedPaymentPlan]" +
      "[UpdateHasCheckedPaymentPlan]" +
      "[EnteredDetailsAboutBankAccount]" +
      "[UpdateEnteredDirectDebitDetails]" +
      "[UpdateConfirmedDirectDebitDetails]" +
      "[UpdateHasAgreedTermsAndConditions]"

  "[Sa]" - {

    s"[Bta]$saTestNameJourneyStages" in {
      stubCommonActions()
      val tdAll = new TdAll {
        override val journeyId: JourneyId = journeyIdGenerator.readNextJourneyId()
        override val correlationId: CorrelationId = correlationIdGenerator.readNextCorrelationId()
      }
      implicit val request: Request[_] = tdAll.request
      val response: SjResponse = journeyConnector.Sa.startJourneyBta(tdAll.SaBta.sjRequest).futureValue

      /** Start journey */
      response shouldBe tdAll.SaBta.sjResponse
      journeyConnector.getJourney(response.journeyId).futureValue shouldBe tdAll.SaBta.journeyAfterStarted

      /** Update tax id */
      journeyConnector.updateTaxId(tdAll.journeyId, tdAll.SaBta.updateTaxIdRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta.journeyAfterDetermineTaxIds

      /** Update eligibility result * */
      journeyConnector.updateEligibilityCheckResult(tdAll.journeyId, tdAll.SaBta.updateEligibilityCheckRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta.journeyAfterEligibilityCheckEligible

      /** Update CanPayUpfront */
      journeyConnector.updateCanPayUpfront(tdAll.journeyId, tdAll.SaBta.updateCanPayUpfrontYesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta.journeyAfterCanPayUpfrontYes

      /** Update UpfrontPaymentAmount */
      journeyConnector.updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.SaBta.updateUpfrontPaymentAmountRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta.journeyAfterUpfrontPaymentAmount

      /** Update ExtremeDates */
      journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.SaBta.updateExtremeDatesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta.journeyAfterExtremeDates

      /** Update AffordabilityResult */
      journeyConnector.updateAffordabilityResult(tdAll.journeyId, tdAll.SaBta.updateInstalmentAmountsRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta.journeyAfterInstalmentAmounts

      /** Update MonthlyPaymentAmount */
      journeyConnector.updateMonthlyPaymentAmount(tdAll.journeyId, tdAll.SaBta.updateMonthlyPaymentAmountRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta.journeyAfterMonthlyPaymentAmount

      /** Update DayOfMonth */
      journeyConnector.updateDayOfMonth(tdAll.journeyId, tdAll.SaBta.updateDayOfMonthRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta.journeyAfterDayOfMonth

      /** Update StartDates */
      journeyConnector.updateStartDates(tdAll.journeyId, tdAll.SaBta.updateStartDatesResponse()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta.journeyAfterStartDatesResponse

      /** Update AffordableQuotes */
      journeyConnector.updateAffordableQuotes(tdAll.journeyId, tdAll.SaBta.updateAffordableQuotesResponse()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta.journeyAfterAffordableQuotesResponse

      /** Update Chosen Instalment plan */
      journeyConnector.updateChosenPaymentPlan(tdAll.journeyId, tdAll.SaBta.updateSelectedPaymentPlanRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta.journeyAfterSelectedPaymentPlan

      /** Update Checked Instalment plan */
      journeyConnector.updateHasCheckedPaymentPlan(tdAll.journeyId).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta.journeyAfterCheckedPaymentPlan

      /** Update Details about Bank Account */
      journeyConnector.updateDetailsAboutBankAccount(tdAll.journeyId, tdAll.SaBta.updateDetailsAboutBankAccountRequest(isAccountHolder = true)).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta.journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder = true)

      /** Update Direct debit details */
      journeyConnector.updateDirectDebitDetails(tdAll.journeyId, tdAll.SaBta.updateDirectDebitDetailsRequest).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta.journeyAfterEnteredDirectDebitDetails()

      /** Update Confirm Direct debit details */
      journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta.journeyAfterConfirmedDirectDebitDetails

      /** Update Agreed terms and conditions */
      journeyConnector.updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(value = true)).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta.journeyAfterAgreedTermsAndConditions(isEmailAddressRequired = true)

      /** Update Email Address */
      journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.email).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta.journeyAfterSelectedEmail

      /** Update Email Verification Status */
      journeyConnector.updateEmailVerificationResult(tdAll.journeyId, EmailVerificationResult.Verified).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified)

      /** Update Arrangement (journey completed) */
      journeyConnector.updateArrangement(tdAll.journeyId, tdAll.SaBta.updateArrangementRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta.journeyAfterSubmittedArrangement(isEmailAddressRequired = true)

      verifyCommonActions(numberOfAuthCalls = 40)
    }

    s"[Pta]$saTestNameJourneyStages" in {
      stubCommonActions()
      val tdAll = new TdAll {
        override val journeyId: JourneyId = journeyIdGenerator.readNextJourneyId()
        override val correlationId: CorrelationId = correlationIdGenerator.readNextCorrelationId()
      }
      implicit val request: Request[_] = tdAll.request
      val response: SjResponse = journeyConnector.Sa.startJourneyPta(tdAll.SaPta.sjRequest).futureValue

      /** Start journey */
      response shouldBe tdAll.SaPta.sjResponse
      journeyConnector.getJourney(response.journeyId).futureValue shouldBe tdAll.SaPta.journeyAfterStarted

      /** Update tax id */
      journeyConnector.updateTaxId(tdAll.journeyId, tdAll.SaPta.updateTaxIdRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaPta.journeyAfterDetermineTaxIds

      /** Update eligibility result * */
      journeyConnector.updateEligibilityCheckResult(tdAll.journeyId, tdAll.SaPta.updateEligibilityCheckRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaPta.journeyAfterEligibilityCheckEligible

      /** Update CanPayUpfront */
      journeyConnector.updateCanPayUpfront(tdAll.journeyId, tdAll.SaPta.updateCanPayUpfrontYesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaPta.journeyAfterCanPayUpfrontYes

      /** Update UpfrontPaymentAmount */
      journeyConnector.updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.SaPta.updateUpfrontPaymentAmountRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaPta.journeyAfterUpfrontPaymentAmount

      /** Update ExtremeDates */
      journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.SaPta.updateExtremeDatesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaPta.journeyAfterExtremeDates

      /** Update AffordabilityResult */
      journeyConnector.updateAffordabilityResult(tdAll.journeyId, tdAll.SaPta.updateInstalmentAmountsRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaPta.journeyAfterInstalmentAmounts

      /** Update MonthlyPaymentAmount */
      journeyConnector.updateMonthlyPaymentAmount(tdAll.journeyId, tdAll.SaPta.updateMonthlyPaymentAmountRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaPta.journeyAfterMonthlyPaymentAmount

      /** Update DayOfMonth */
      journeyConnector.updateDayOfMonth(tdAll.journeyId, tdAll.SaPta.updateDayOfMonthRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaPta.journeyAfterDayOfMonth

      /** Update StartDates */
      journeyConnector.updateStartDates(tdAll.journeyId, tdAll.SaPta.updateStartDatesResponse()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaPta.journeyAfterStartDatesResponse

      /** Update AffordableQuotes */
      journeyConnector.updateAffordableQuotes(tdAll.journeyId, tdAll.SaPta.updateAffordableQuotesResponse()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaPta.journeyAfterAffordableQuotesResponse

      /** Update Chosen Instalment plan */
      journeyConnector.updateChosenPaymentPlan(tdAll.journeyId, tdAll.SaPta.updateSelectedPaymentPlanRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaPta.journeyAfterSelectedPaymentPlan

      /** Update Checked Instalment plan */
      journeyConnector.updateHasCheckedPaymentPlan(tdAll.journeyId).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaPta.journeyAfterCheckedPaymentPlan

      /** Update Details about Bank Account */
      journeyConnector.updateDetailsAboutBankAccount(tdAll.journeyId, tdAll.SaPta.updateDetailsAboutBankAccountRequest(isAccountHolder = true)).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaPta.journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder = true)

      /** Update Direct debit details */
      journeyConnector.updateDirectDebitDetails(tdAll.journeyId, tdAll.SaPta.updateDirectDebitDetailsRequest).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaPta.journeyAfterEnteredDirectDebitDetails()

      /** Update Confirm Direct debit details */
      journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaPta.journeyAfterConfirmedDirectDebitDetails

      /** Update Agreed terms and conditions */
      journeyConnector.updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(value = true)).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaPta.journeyAfterAgreedTermsAndConditions(isEmailAddressRequired = true)

      /** Update Email Address */
      journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.email).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaPta.journeyAfterSelectedEmail

      /** Update Email Verification Status */
      journeyConnector.updateEmailVerificationResult(tdAll.journeyId, EmailVerificationResult.Verified).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaPta.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified)

      /** Update Arrangement (journey completed) */
      journeyConnector.updateArrangement(tdAll.journeyId, tdAll.SaPta.updateArrangementRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaPta.journeyAfterSubmittedArrangement(isEmailAddressRequired = true)

      verifyCommonActions(numberOfAuthCalls = 40)
    }

    s"[Mobile]$saTestNameJourneyStages" in {
      stubCommonActions()
      val tdAll = new TdAll {
        override val journeyId: JourneyId = journeyIdGenerator.readNextJourneyId()
        override val correlationId: CorrelationId = correlationIdGenerator.readNextCorrelationId()
      }
      implicit val request: Request[_] = tdAll.request
      val response: SjResponse = journeyConnector.Sa.startJourneyMobile(tdAll.SaMobile.sjRequest).futureValue

      /** Start journey */
      response shouldBe tdAll.SaMobile.sjResponse
      journeyConnector.getJourney(response.journeyId).futureValue shouldBe tdAll.SaMobile.journeyAfterStarted

      /** Update tax id */
      journeyConnector.updateTaxId(tdAll.journeyId, tdAll.SaMobile.updateTaxIdRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaMobile.journeyAfterDetermineTaxIds

      /** Update eligibility result * */
      journeyConnector.updateEligibilityCheckResult(tdAll.journeyId, tdAll.SaMobile.updateEligibilityCheckRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaMobile.journeyAfterEligibilityCheckEligible

      /** Update CanPayUpfront */
      journeyConnector.updateCanPayUpfront(tdAll.journeyId, tdAll.SaMobile.updateCanPayUpfrontYesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaMobile.journeyAfterCanPayUpfrontYes

      /** Update UpfrontPaymentAmount */
      journeyConnector.updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.SaMobile.updateUpfrontPaymentAmountRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaMobile.journeyAfterUpfrontPaymentAmount

      /** Update ExtremeDates */
      journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.SaMobile.updateExtremeDatesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaMobile.journeyAfterExtremeDates

      /** Update AffordabilityResult */
      journeyConnector.updateAffordabilityResult(tdAll.journeyId, tdAll.SaMobile.updateInstalmentAmountsRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaMobile.journeyAfterInstalmentAmounts

      /** Update MonthlyPaymentAmount */
      journeyConnector.updateMonthlyPaymentAmount(tdAll.journeyId, tdAll.SaMobile.updateMonthlyPaymentAmountRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaMobile.journeyAfterMonthlyPaymentAmount

      /** Update DayOfMonth */
      journeyConnector.updateDayOfMonth(tdAll.journeyId, tdAll.SaMobile.updateDayOfMonthRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaMobile.journeyAfterDayOfMonth

      /** Update StartDates */
      journeyConnector.updateStartDates(tdAll.journeyId, tdAll.SaMobile.updateStartDatesResponse()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaMobile.journeyAfterStartDatesResponse

      /** Update AffordableQuotes */
      journeyConnector.updateAffordableQuotes(tdAll.journeyId, tdAll.SaMobile.updateAffordableQuotesResponse()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaMobile.journeyAfterAffordableQuotesResponse

      /** Update Chosen Instalment plan */
      journeyConnector.updateChosenPaymentPlan(tdAll.journeyId, tdAll.SaMobile.updateSelectedPaymentPlanRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaMobile.journeyAfterSelectedPaymentPlan

      /** Update Checked Instalment plan */
      journeyConnector.updateHasCheckedPaymentPlan(tdAll.journeyId).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaMobile.journeyAfterCheckedPaymentPlan

      /** Update Details about Bank Account */
      journeyConnector.updateDetailsAboutBankAccount(tdAll.journeyId, tdAll.SaMobile.updateDetailsAboutBankAccountRequest(isAccountHolder = true)).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaMobile.journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder = true)

      /** Update Direct debit details */
      journeyConnector.updateDirectDebitDetails(tdAll.journeyId, tdAll.SaMobile.updateDirectDebitDetailsRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaMobile.journeyAfterEnteredDirectDebitDetails()

      /** Update Confirm Direct debit details */
      journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaMobile.journeyAfterConfirmedDirectDebitDetails

      /** Update Agreed terms and conditions */
      journeyConnector.updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(value = true)).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaMobile.journeyAfterAgreedTermsAndConditions(isEmailAddressRequired = true)

      /** Update Email Address */
      journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.email).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaMobile.journeyAfterSelectedEmail

      /** Update Email Verification Status */
      journeyConnector.updateEmailVerificationResult(tdAll.journeyId, EmailVerificationResult.Verified).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaMobile.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified)

      /** Update Arrangement (journey completed) */
      journeyConnector.updateArrangement(tdAll.journeyId, tdAll.SaMobile.updateArrangementRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaMobile.journeyAfterSubmittedArrangement(isEmailAddressRequired = true)

      verifyCommonActions(numberOfAuthCalls = 40)
    }

    s"[GovUk]$saTestNameJourneyStages" in {
      stubCommonActions()
      val tdAll = new TdAll {
        override val journeyId: JourneyId = journeyIdGenerator.readNextJourneyId()
        override val correlationId: CorrelationId = correlationIdGenerator.readNextCorrelationId()
      }
      implicit val request: Request[_] = tdAll.request
      val response: SjResponse = journeyConnector.Sa.startJourneyGovUk(tdAll.SaGovUk.sjRequest).futureValue

      /** Start journey */
      response shouldBe tdAll.SaGovUk.sjResponse
      journeyConnector.getJourney(response.journeyId).futureValue shouldBe tdAll.SaGovUk.journeyAfterStarted

      /** Update tax id */
      journeyConnector.updateTaxId(tdAll.journeyId, tdAll.SaGovUk.updateTaxIdRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaGovUk.journeyAfterDetermineTaxIds

      /** Update eligibility result * */
      journeyConnector.updateEligibilityCheckResult(tdAll.journeyId, tdAll.SaGovUk.updateEligibilityCheckRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaGovUk.journeyAfterEligibilityCheckEligible

      /** Update CanPayUpfront */
      journeyConnector.updateCanPayUpfront(tdAll.journeyId, tdAll.SaGovUk.updateCanPayUpfrontYesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaGovUk.journeyAfterCanPayUpfrontYes

      /** Update UpfrontPaymentAmount */
      journeyConnector.updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.SaGovUk.updateUpfrontPaymentAmountRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaGovUk.journeyAfterUpfrontPaymentAmount

      /** Update ExtremeDates */
      journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.SaGovUk.updateExtremeDatesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaGovUk.journeyAfterExtremeDates

      /** Update AffordabilityResult */
      journeyConnector.updateAffordabilityResult(tdAll.journeyId, tdAll.SaGovUk.updateInstalmentAmountsRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaGovUk.journeyAfterInstalmentAmounts

      /** Update MonthlyPaymentAmount */
      journeyConnector.updateMonthlyPaymentAmount(tdAll.journeyId, tdAll.SaGovUk.updateMonthlyPaymentAmountRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaGovUk.journeyAfterMonthlyPaymentAmount

      /** Update DayOfMonth */
      journeyConnector.updateDayOfMonth(tdAll.journeyId, tdAll.SaGovUk.updateDayOfMonthRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaGovUk.journeyAfterDayOfMonth

      /** Update StartDates */
      journeyConnector.updateStartDates(tdAll.journeyId, tdAll.SaGovUk.updateStartDatesResponse()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaGovUk.journeyAfterStartDatesResponse

      /** Update AffordableQuotes */
      journeyConnector.updateAffordableQuotes(tdAll.journeyId, tdAll.SaGovUk.updateAffordableQuotesResponse()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaGovUk.journeyAfterAffordableQuotesResponse

      /** Update Chosen Instalment plan */
      journeyConnector.updateChosenPaymentPlan(tdAll.journeyId, tdAll.SaGovUk.updateSelectedPaymentPlanRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaGovUk.journeyAfterSelectedPaymentPlan

      /** Update Checked Instalment plan */
      journeyConnector.updateHasCheckedPaymentPlan(tdAll.journeyId).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaGovUk.journeyAfterCheckedPaymentPlan

      /** Update Details about Bank Account */
      journeyConnector.updateDetailsAboutBankAccount(tdAll.journeyId, tdAll.SaGovUk.updateDetailsAboutBankAccountRequest(isAccountHolder = true)).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaGovUk.journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder = true)

      /** Update Direct debit details */
      journeyConnector.updateDirectDebitDetails(tdAll.journeyId, tdAll.SaGovUk.updateDirectDebitDetailsRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaGovUk.journeyAfterEnteredDirectDebitDetails()

      /** Update Confirm Direct debit details */
      journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaGovUk.journeyAfterConfirmedDirectDebitDetails

      /** Update Agreed terms and conditions */
      journeyConnector.updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(value = true)).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaGovUk.journeyAfterAgreedTermsAndConditions(isEmailAddressRequired = true)

      /** Update Email Address */
      journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.email).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaGovUk.journeyAfterSelectedEmail

      /** Update Email Verification Status */
      journeyConnector.updateEmailVerificationResult(tdAll.journeyId, EmailVerificationResult.Verified).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaGovUk.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified)

      /** Update Arrangement (journey completed) */
      journeyConnector.updateArrangement(tdAll.journeyId, tdAll.SaGovUk.updateArrangementRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaGovUk.journeyAfterSubmittedArrangement(isEmailAddressRequired = true)

      verifyCommonActions(numberOfAuthCalls = 40)
    }

    s"[DetachedUrl]$saTestNameJourneyStages" in {
      stubCommonActions()
      val tdAll = new TdAll {
        override val journeyId: JourneyId = journeyIdGenerator.readNextJourneyId()
        override val correlationId: CorrelationId = correlationIdGenerator.readNextCorrelationId()
      }
      implicit val request: Request[_] = tdAll.request
      val response: SjResponse = journeyConnector.Sa.startJourneyDetachedUrl(tdAll.SaDetachedUrl.sjRequest).futureValue

      /** Start journey */
      response shouldBe tdAll.SaDetachedUrl.sjResponse
      journeyConnector.getJourney(response.journeyId).futureValue shouldBe tdAll.SaDetachedUrl.journeyAfterStarted

      /** Update tax id */
      journeyConnector.updateTaxId(tdAll.journeyId, tdAll.SaDetachedUrl.updateTaxIdRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaDetachedUrl.journeyAfterDetermineTaxIds

      /** Update eligibility result * */
      journeyConnector.updateEligibilityCheckResult(tdAll.journeyId, tdAll.SaDetachedUrl.updateEligibilityCheckRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaDetachedUrl.journeyAfterEligibilityCheckEligible

      /** Update CanPayUpfront */
      journeyConnector.updateCanPayUpfront(tdAll.journeyId, tdAll.SaDetachedUrl.updateCanPayUpfrontYesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaDetachedUrl.journeyAfterCanPayUpfrontYes

      /** Update UpfrontPaymentAmount */
      journeyConnector.updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.SaDetachedUrl.updateUpfrontPaymentAmountRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaDetachedUrl.journeyAfterUpfrontPaymentAmount

      /** Update ExtremeDates */
      journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.SaDetachedUrl.updateExtremeDatesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaDetachedUrl.journeyAfterExtremeDates

      /** Update AffordabilityResult */
      journeyConnector.updateAffordabilityResult(tdAll.journeyId, tdAll.SaDetachedUrl.updateInstalmentAmountsRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaDetachedUrl.journeyAfterInstalmentAmounts

      /** Update MonthlyPaymentAmount */
      journeyConnector.updateMonthlyPaymentAmount(tdAll.journeyId, tdAll.SaDetachedUrl.updateMonthlyPaymentAmountRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaDetachedUrl.journeyAfterMonthlyPaymentAmount

      /** Update DayOfMonth */
      journeyConnector.updateDayOfMonth(tdAll.journeyId, tdAll.SaDetachedUrl.updateDayOfMonthRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaDetachedUrl.journeyAfterDayOfMonth

      /** Update StartDates */
      journeyConnector.updateStartDates(tdAll.journeyId, tdAll.SaDetachedUrl.updateStartDatesResponse()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaDetachedUrl.journeyAfterStartDatesResponse

      /** Update AffordableQuotes */
      journeyConnector.updateAffordableQuotes(tdAll.journeyId, tdAll.SaDetachedUrl.updateAffordableQuotesResponse()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaDetachedUrl.journeyAfterAffordableQuotesResponse

      /** Update Chosen Instalment plan */
      journeyConnector.updateChosenPaymentPlan(tdAll.journeyId, tdAll.SaDetachedUrl.updateSelectedPaymentPlanRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaDetachedUrl.journeyAfterSelectedPaymentPlan

      /** Update Checked Instalment plan */
      journeyConnector.updateHasCheckedPaymentPlan(tdAll.journeyId).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaDetachedUrl.journeyAfterCheckedPaymentPlan

      /** Update Details about Bank Account */
      journeyConnector.updateDetailsAboutBankAccount(tdAll.journeyId, tdAll.SaDetachedUrl.updateDetailsAboutBankAccountRequest(isAccountHolder = true)).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaDetachedUrl.journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder = true)

      /** Update Direct debit details */
      journeyConnector.updateDirectDebitDetails(tdAll.journeyId, tdAll.SaDetachedUrl.updateDirectDebitDetailsRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaDetachedUrl.journeyAfterEnteredDirectDebitDetails()

      /** Update Confirm Direct debit details */
      journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaDetachedUrl.journeyAfterConfirmedDirectDebitDetails

      /** Update Agreed terms and conditions */
      journeyConnector.updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(value = true)).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaDetachedUrl.journeyAfterAgreedTermsAndConditions(isEmailAddressRequired = true)

      /** Update Email Address */
      journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.email).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaDetachedUrl.journeyAfterSelectedEmail

      /** Update Email Verification Status */
      journeyConnector.updateEmailVerificationResult(tdAll.journeyId, EmailVerificationResult.Verified).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaDetachedUrl.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified)

      /** Update Arrangement (journey completed) */
      journeyConnector.updateArrangement(tdAll.journeyId, tdAll.SaDetachedUrl.updateArrangementRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaDetachedUrl.journeyAfterSubmittedArrangement(isEmailAddressRequired = true)

      verifyCommonActions(numberOfAuthCalls = 40)
    }

  }

}
