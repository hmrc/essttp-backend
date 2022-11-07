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

package journey

import essttp.emailverification.EmailVerificationStatus
import essttp.journey.JourneyConnector
import essttp.journey.model.{CorrelationId, JourneyId, SjResponse}
import essttp.rootmodel.IsEmailAddressRequired
import essttp.testdata.TdAll
import play.api.mvc.Request
import testsupport.ItSpec

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
      "[UpdateChosenTypeOfBankAccount]" +
      "[UpdateEnteredDirectDebitDetails]" +
      "[UpdateConfirmedDirectDebitDetails]" +
      "[UpdateChosenEmail]" +
      "[UpdateEmailVerificationStatus]" +
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

      /** Update Type of Bank Account */
      journeyConnector.updateDetailsAboutBankAccount(tdAll.journeyId, tdAll.EpayeBta.updateDetailsAboutBankAccountRequest(isAccountHolder = true)).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder = true)

      /** Update Direct debit details */
      journeyConnector.updateDirectDebitDetails(tdAll.journeyId, tdAll.EpayeBta.updateDirectDebitDetailsRequest).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterEnteredDirectDebitDetails()

      /** Update Confirm Direct debit details */
      journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterConfirmedDirectDebitDetails

      /** Update Agreed terms and conditions */
      journeyConnector.updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(true)).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterAgreedTermsAndConditions(isEmailAddressRequired = true)

      /** Update Email Address */
      journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.email).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterSelectedEmail

      /** Update Email Verification Status */
      journeyConnector.updateEmailVerificationStatus(tdAll.journeyId, EmailVerificationStatus.Verified).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterEmailVerificationStatus(EmailVerificationStatus.Verified)

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

      /** Update Type of Bank Account */
      journeyConnector.updateDetailsAboutBankAccount(tdAll.journeyId, tdAll.EpayeGovUk.updateDetailsAboutBankAccountRequest(isAccountHolder = true)).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder = true)

      /** Update Direct debit details */
      journeyConnector.updateDirectDebitDetails(tdAll.journeyId, tdAll.EpayeGovUk.updateDirectDebitDetailsRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterEnteredDirectDebitDetails()

      /** Update Confirm Direct debit details */
      journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterConfirmedDirectDebitDetails

      /** Update Agreed terms and conditions */
      journeyConnector.updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(true)).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterAgreedTermsAndConditions(isEmailAddressRequired = true)

      /** Update Email Address */
      journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.email).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterSelectedEmail

      /** Update Email Verification Status */
      journeyConnector.updateEmailVerificationStatus(tdAll.journeyId, EmailVerificationStatus.Verified).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterEmailVerificationStatus(EmailVerificationStatus.Verified)

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

      /** Update Type of Bank Account */
      journeyConnector.updateDetailsAboutBankAccount(tdAll.journeyId, tdAll.EpayeDetachedUrl.updateDetailsAboutBankAccountRequest(isAccountHolder = true)).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder = true)

      /** Update Direct debit details */
      journeyConnector.updateDirectDebitDetails(tdAll.journeyId, tdAll.EpayeDetachedUrl.updateDirectDebitDetailsRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterEnteredDirectDebitDetails()

      /** Update Confirm Direct debit details */
      journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterConfirmedDirectDebitDetails

      /** Update Agreed terms and conditions */
      journeyConnector.updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(true)).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterAgreedTermsAndConditions(isEmailAddressRequired = true)

      /** Update Email Address */
      journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.email).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterSelectedEmail

      /** Update Email Verification Status */
      journeyConnector.updateEmailVerificationStatus(tdAll.journeyId, EmailVerificationStatus.Verified).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterEmailVerificationStatus(EmailVerificationStatus.Verified)

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

      /** Update Type of Bank Account */
      journeyConnector.updateDetailsAboutBankAccount(tdAll.journeyId, tdAll.EpayeEpayeService.updateDetailsAboutBankAccountRequest(isAccountHolder = true)).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder = true)

      /** Update Direct debit details */
      journeyConnector.updateDirectDebitDetails(tdAll.journeyId, tdAll.EpayeEpayeService.updateDirectDebitDetailsRequest).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterEnteredDirectDebitDetails()

      /** Update Confirm Direct debit details */
      journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterConfirmedDirectDebitDetails

      /** Update Agreed terms and conditions */
      journeyConnector.updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(true)).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterAgreedTermsAndConditions(isEmailAddressRequired = true)

      /** Update Email Address */
      journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.email).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterSelectedEmail

      /** Update Email Verification Status */
      journeyConnector.updateEmailVerificationStatus(tdAll.journeyId, EmailVerificationStatus.Verified).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterEmailVerificationStatus(EmailVerificationStatus.Verified)

      /** Update Arrangement (journey completed) */
      journeyConnector.updateArrangement(tdAll.journeyId, tdAll.EpayeEpayeService.updateArrangementRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterSubmittedArrangement(isEmailAddressRequired = true)

      verifyCommonActions(numberOfAuthCalls = 40)
    }
  }

  private val vatTestNameJourneyStages: String =
    "[StartJourney]" +
      "[UpdateTaxId]" +
      "[UpdateEligibilityCheck]"

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

      verifyCommonActions(numberOfAuthCalls = 6)
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

      verifyCommonActions(numberOfAuthCalls = 6)
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

      verifyCommonActions(numberOfAuthCalls = 6)
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

      verifyCommonActions(numberOfAuthCalls = 6)
    }
  }

}
