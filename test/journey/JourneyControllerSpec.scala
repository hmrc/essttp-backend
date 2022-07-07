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

import essttp.journey.JourneyConnector
import essttp.journey.model.{JourneyId, SjResponse}
import essttp.testdata.TdAll
import play.api.mvc.Request
import testsupport.ItSpec

class JourneyControllerSpec extends ItSpec {

  def journeyConnector: JourneyConnector = app.injector.instanceOf[JourneyConnector]

  private val testNameJourneyStages: String =
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
      "[UpdateEnteredDirectDebitDetails]" +
      "[UpdateConfirmedDirectDebitDetails]"

  s"[Epaye.Bta][Happy path with upfront payment]$testNameJourneyStages" in {
    val tdAll = new TdAll {
      override val journeyId: JourneyId = journeyIdGenerator.readNextJourneyId()
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

    /** Update Direct debit details */
    journeyConnector.updateDirectDebitDetails(tdAll.journeyId, tdAll.EpayeBta.updateDirectDebitDetailsRequest(isAccountHolder = true)).futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterEnteredDirectDebitDetails(isAccountHolder = true)

    /** Update Confirm Direct debit details */
    journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterConfirmedDirectDebitDetails

    /** Update Agreed terms and conditions */
    journeyConnector.updateHasAgreedTermsAndConditions(tdAll.journeyId).futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterAgreedTermsAndConditions

  }

  s"[Epaye.GovUk][Happy path with upfront payment]$testNameJourneyStages" in {
    val tdAll = new TdAll {
      override val journeyId: JourneyId = journeyIdGenerator.readNextJourneyId()
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

    /** Update Direct debit details */
    journeyConnector.updateDirectDebitDetails(tdAll.journeyId, tdAll.EpayeGovUk.updateDirectDebitDetailsRequest(isAccountHolder = true)).futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterEnteredDirectDebitDetails(isAccountHolder = true)

    /** Update Confirm Direct debit details */
    journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterConfirmedDirectDebitDetails

    /** Update Agreed terms and conditions */
    journeyConnector.updateHasAgreedTermsAndConditions(tdAll.journeyId).futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterAgreedTermsAndConditions
  }

  s"[Epaye.DetachedUrl][Happy path with upfront payment]$testNameJourneyStages" in {
    val tdAll = new TdAll {
      override val journeyId: JourneyId = journeyIdGenerator.readNextJourneyId()
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

    /** Update Direct debit details */
    journeyConnector.updateDirectDebitDetails(tdAll.journeyId, tdAll.EpayeDetachedUrl.updateDirectDebitDetailsRequest(isAccountHolder = true)).futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterEnteredDirectDebitDetails(isAccountHolder = true)

    /** Update Confirm Direct debit details */
    journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterConfirmedDirectDebitDetails

    /** Update Agreed terms and conditions */
    journeyConnector.updateHasAgreedTermsAndConditions(tdAll.journeyId).futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterAgreedTermsAndConditions
  }
}
