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

import play.api.inject.bind
import essttp.journey.JourneyConnector
import essttp.journey.model.{CorrelationId, JourneyId, SjResponse}
import essttp.rootmodel.{IsEmailAddressRequired, TaxRegime}
import journey.JourneyControllerAffordabilityEnabledSpec.TestAffordabilityEnablerService
import paymentsEmailVerification.models.EmailVerificationResult
import play.api.inject.guice.GuiceableModule
import play.api.mvc.Request
import services.AffordabilityEnablerService
import testsupport.ItSpec
import testsupport.testdata.TdAll

class JourneyControllerSpec extends ItSpec {

  override val overrideConfig: Map[String, Any] = Map(
    // test empty tax regime strings get ignored and don't blow up
    "affordability.tax-regimes" -> Seq("")
  )

  def journeyConnector: JourneyConnector = app.injector.instanceOf[JourneyConnector]

  private val epayeTestNameJourneyStages: String =
    "[StartJourney]" +
      "[UpdateTaxId]" +
      "[UpdateEligibilityCheck]" +
      "[UpdateWhyCannotPayInFull]" +
      "[UpdateCanPayUpfront]" +
      "[UpdateUpfrontPaymentAmount]" +
      "[UpdateExtremeDates]" +
      "[UpdateAffordabilityResult]" +
      "[UpdateMonthlyPaymentAmount]" +
      "[UpdateDayOfMonth]" +
      "[UpdateStartDatesResponse]" +
      "[UpdateAffordableQuotes]" +
      "[UpdateCanPayWithinSixMonths]" +
      "[UpdateSelectedPaymentPlan]" +
      "[UpdateHasCheckedPaymentPlan]" +
      "[UpdateEnteredCanYouSetUpDirectDebit]" +
      "[UpdateEnteredDirectDebitDetails]" +
      "[UpdateConfirmedDirectDebitDetails]" +
      "[UpdateChosenEmail]" +
      "[UpdateEmailVerificationResult]" +
      "[UpdateSubmittedArrangement]"

  "[Epaye]" - {
    s"[Bta][Happy path with upfront payment]$epayeTestNameJourneyStages" in {
      stubCommonActions()

      val tdAll = new TdAll {
        override val journeyId: JourneyId         = journeyIdGenerator.readNextJourneyId()
        override val correlationId: CorrelationId = correlationIdGenerator.readNextCorrelationId()
      }

      given Request[_]         = tdAll.request
      val response: SjResponse = journeyConnector.Epaye.startJourneyBta(tdAll.EpayeBta.sjRequest).futureValue

      /** Start journey */
      response shouldBe tdAll.EpayeBta.sjResponse
      journeyConnector.getJourney(response.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterStarted

      /** Update tax id */
      journeyConnector.updateTaxId(tdAll.journeyId, tdAll.EpayeBta.updateTaxIdRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterDetermineTaxIds

      /** Update eligibility result */
      journeyConnector
        .updateEligibilityCheckResult(tdAll.journeyId, tdAll.EpayeBta.updateEligibilityCheckRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.EpayeBta.journeyAfterEligibilityCheckEligible

      /** Update why cannot pay in full */
      journeyConnector.updateWhyCannotPayInFullAnswers(tdAll.journeyId, tdAll.whyCannotPayInFullNotRequired).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.EpayeBta.journeyAfterWhyCannotPayInFullNotRequired

      /** Update CanPayUpfront */
      journeyConnector.updateCanPayUpfront(tdAll.journeyId, tdAll.EpayeBta.updateCanPayUpfrontYesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterCanPayUpfrontYes

      /** Update UpfrontPaymentAmount */
      journeyConnector
        .updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.EpayeBta.updateUpfrontPaymentAmountRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterUpfrontPaymentAmount

      /** Update ExtremeDates */
      journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.EpayeBta.updateExtremeDatesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterExtremeDates

      /** Update AffordabilityResult */
      journeyConnector
        .updateAffordabilityResult(tdAll.journeyId, tdAll.EpayeBta.updateInstalmentAmountsRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterInstalmentAmounts

      /** Update CanPayWithinSixMonths */
      journeyConnector
        .updateCanPayWithinSixMonthsAnswers(tdAll.journeyId, tdAll.canPayWithinSixMonthsNotRequired)
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.EpayeBta.journeyAfterCanPayWithinSixMonthsNotRequired

      /** Update MonthlyPaymentAmount */
      journeyConnector
        .updateMonthlyPaymentAmount(tdAll.journeyId, tdAll.EpayeBta.updateMonthlyPaymentAmountRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterMonthlyPaymentAmount

      /** Update DayOfMonth */
      journeyConnector.updateDayOfMonth(tdAll.journeyId, tdAll.EpayeBta.updateDayOfMonthRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterDayOfMonth

      /** Update StartDates */
      journeyConnector.updateStartDates(tdAll.journeyId, tdAll.EpayeBta.updateStartDatesResponse()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterStartDatesResponse

      /** Update AffordableQuotes */
      journeyConnector
        .updateAffordableQuotes(tdAll.journeyId, tdAll.EpayeBta.updateAffordableQuotesResponse())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.EpayeBta.journeyAfterAffordableQuotesResponse

      /** Update Chosen Instalment plan */
      journeyConnector
        .updateChosenPaymentPlan(tdAll.journeyId, tdAll.EpayeBta.updateSelectedPaymentPlanRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterSelectedPaymentPlan

      /** Update Checked Instalment plan */
      journeyConnector.updateHasCheckedPaymentPlan(tdAll.journeyId, tdAll.paymentPlanAnswersNoAffordability).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.EpayeBta.journeyAfterCheckedPaymentPlanNonAffordability

      /** Update Details about Bank Account */
      journeyConnector
        .updateCanSetUpDirectDebit(
          tdAll.journeyId,
          tdAll.EpayeBta.updateCanSetUpDirectDebitRequest(isAccountHolder = true)
        )
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta
        .journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true)

      /** Update Direct debit details */
      journeyConnector
        .updateDirectDebitDetails(tdAll.journeyId, tdAll.EpayeBta.updateDirectDebitDetailsRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta
        .journeyAfterEnteredDirectDebitDetailsNoAffordability()

      /** Update Confirm Direct debit details */
      journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.EpayeBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability

      /** Update Agreed terms and conditions */
      journeyConnector
        .updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(value = true))
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta
        .journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true)

      /** Update Email Address */
      journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.email).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.EpayeBta.journeyAfterSelectedEmailNoAffordability

      /** Update Email Verification Status */
      journeyConnector.updateEmailVerificationResult(tdAll.journeyId, EmailVerificationResult.Verified).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta
        .journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified)

      /** Update Arrangement (journey completed) */
      journeyConnector.updateArrangement(tdAll.journeyId, tdAll.EpayeBta.updateArrangementRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta
        .journeyAfterSubmittedArrangementNoAffordability(isEmailAddressRequired = true)

      verifyCommonActions(numberOfAuthCalls = 44)
    }

    s"[GovUk][Happy path with upfront payment]$epayeTestNameJourneyStages" in {
      stubCommonActions()

      val tdAll = new TdAll {
        override val journeyId: JourneyId         = journeyIdGenerator.readNextJourneyId()
        override val correlationId: CorrelationId = correlationIdGenerator.readNextCorrelationId()
      }

      given Request[_]         = tdAll.request
      val response: SjResponse = journeyConnector.Epaye.startJourneyGovUk(tdAll.EpayeGovUk.sjRequest).futureValue

      /** Start journey */
      response shouldBe tdAll.EpayeGovUk.sjResponse
      journeyConnector.getJourney(response.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterStarted

      /** Update tax id */
      journeyConnector.updateTaxId(tdAll.journeyId, tdAll.EpayeGovUk.updateTaxIdRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterDetermineTaxIds

      /** Update eligibility result */
      journeyConnector
        .updateEligibilityCheckResult(tdAll.journeyId, tdAll.EpayeGovUk.updateEligibilityCheckRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.EpayeGovUk.journeyAfterEligibilityCheckEligible

      /** Update why cannot pay in full */
      journeyConnector.updateWhyCannotPayInFullAnswers(tdAll.journeyId, tdAll.whyCannotPayInFullNotRequired).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.EpayeGovUk.journeyAfterWhyCannotPayInFullNotRequired

      /** Update CanPayUpfront */
      journeyConnector
        .updateCanPayUpfront(tdAll.journeyId, tdAll.EpayeGovUk.updateCanPayUpfrontYesRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterCanPayUpfrontYes

      /** Update UpfrontPaymentAmount */
      journeyConnector
        .updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.EpayeGovUk.updateUpfrontPaymentAmountRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.EpayeGovUk.journeyAfterUpfrontPaymentAmount

      /** Update ExtremeDates */
      journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.EpayeGovUk.updateExtremeDatesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterExtremeDates

      /** Update AffordabilityResult */
      journeyConnector
        .updateAffordabilityResult(tdAll.journeyId, tdAll.EpayeGovUk.updateInstalmentAmountsRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterInstalmentAmounts

      /** Update CanPayWithinSixMonths */
      journeyConnector
        .updateCanPayWithinSixMonthsAnswers(tdAll.journeyId, tdAll.canPayWithinSixMonthsNotRequired)
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.EpayeGovUk.journeyAfterCanPayWithinSixMonths

      /** Update MonthlyPaymentAmount */
      journeyConnector
        .updateMonthlyPaymentAmount(tdAll.journeyId, tdAll.EpayeGovUk.updateMonthlyPaymentAmountRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.EpayeGovUk.journeyAfterMonthlyPaymentAmount

      /** Update DayOfMonth */
      journeyConnector.updateDayOfMonth(tdAll.journeyId, tdAll.EpayeGovUk.updateDayOfMonthRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterDayOfMonth

      /** Update StartDates */
      journeyConnector.updateStartDates(tdAll.journeyId, tdAll.EpayeGovUk.updateStartDatesResponse()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterStartDatesResponse

      /** Update AffordableQuotes */
      journeyConnector
        .updateAffordableQuotes(tdAll.journeyId, tdAll.EpayeGovUk.updateAffordableQuotesResponse())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.EpayeGovUk.journeyAfterAffordableQuotesResponse

      /** Update Chosen Instalment plan */
      journeyConnector
        .updateChosenPaymentPlan(tdAll.journeyId, tdAll.EpayeGovUk.updateSelectedPaymentPlanRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterSelectedPaymentPlan

      /** Update Checked Instalment plan */
      journeyConnector.updateHasCheckedPaymentPlan(tdAll.journeyId, tdAll.paymentPlanAnswersNoAffordability).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.EpayeGovUk.journeyAfterCheckedPaymentPlanNonAffordability

      /** Update Details about Bank Account */
      journeyConnector
        .updateCanSetUpDirectDebit(
          tdAll.journeyId,
          tdAll.EpayeGovUk.updateCanSetUpDirectDebitRequest(isAccountHolder = true)
        )
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk
        .journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true)

      /** Update Direct debit details */
      journeyConnector
        .updateDirectDebitDetails(tdAll.journeyId, tdAll.EpayeGovUk.updateDirectDebitDetailsRequest()())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk
        .journeyAfterEnteredDirectDebitDetailsNoAffordability()

      /** Update Confirm Direct debit details */
      journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.EpayeGovUk.journeyAfterConfirmedDirectDebitDetailsNoAffordability

      /** Update Agreed terms and conditions */
      journeyConnector
        .updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(value = true))
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk
        .journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true)

      /** Update Email Address */
      journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.email).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterSelectedEmail

      /** Update Email Verification Status */
      journeyConnector.updateEmailVerificationResult(tdAll.journeyId, EmailVerificationResult.Verified).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk
        .journeyAfterEmailVerificationResult(EmailVerificationResult.Verified)

      /** Update Arrangement (journey completed) */
      journeyConnector.updateArrangement(tdAll.journeyId, tdAll.EpayeGovUk.updateArrangementRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk
        .journeyAfterSubmittedArrangementNoAffordability(isEmailAddressRequired = true)

      verifyCommonActions(numberOfAuthCalls = 44)
    }

    s"[DetachedUrl][Happy path with upfront payment]$epayeTestNameJourneyStages" in {
      stubCommonActions()

      val tdAll = new TdAll {
        override val journeyId: JourneyId         = journeyIdGenerator.readNextJourneyId()
        override val correlationId: CorrelationId = correlationIdGenerator.readNextCorrelationId()
      }

      given Request[_]         = tdAll.request
      val response: SjResponse =
        journeyConnector.Epaye.startJourneyDetachedUrl(tdAll.EpayeDetachedUrl.sjRequest).futureValue

      /** Start journey * */
      response shouldBe tdAll.EpayeDetachedUrl.sjResponse
      journeyConnector.getJourney(response.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterStarted

      /** Update tax id * */
      journeyConnector.updateTaxId(tdAll.journeyId, tdAll.EpayeDetachedUrl.updateTaxIdRequest()).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterDetermineTaxIds

      /** Update eligibility result * */
      journeyConnector
        .updateEligibilityCheckResult(tdAll.journeyId, tdAll.EpayeDetachedUrl.updateEligibilityCheckRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterEligibilityCheckEligible

      /** Update why cannot pay in full */
      journeyConnector.updateWhyCannotPayInFullAnswers(tdAll.journeyId, tdAll.whyCannotPayInFullNotRequired).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterWhyCannotPayInFullNotRequired

      /** Update CanPayUpfront * */
      journeyConnector
        .updateCanPayUpfront(tdAll.journeyId, tdAll.EpayeDetachedUrl.updateCanPayUpfrontYesRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterCanPayUpfrontYes

      /** Update UpfrontPaymentAmount */
      journeyConnector
        .updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.EpayeDetachedUrl.updateUpfrontPaymentAmountRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterUpfrontPaymentAmount

      /** Update ExtremeDates */
      journeyConnector
        .updateExtremeDates(tdAll.journeyId, tdAll.EpayeDetachedUrl.updateExtremeDatesRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterExtremeDates

      /** Update AffordabilityResult */
      journeyConnector
        .updateAffordabilityResult(tdAll.journeyId, tdAll.EpayeDetachedUrl.updateInstalmentAmountsRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterInstalmentAmounts

      /** Update CanPayWithinSixMonths */
      journeyConnector
        .updateCanPayWithinSixMonthsAnswers(tdAll.journeyId, tdAll.canPayWithinSixMonthsNotRequired)
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterCanPayWithinSixMonths

      /** Update MonthlyPaymentAmount */
      journeyConnector
        .updateMonthlyPaymentAmount(tdAll.journeyId, tdAll.EpayeDetachedUrl.updateMonthlyPaymentAmountRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterMonthlyPaymentAmount

      /** Update DayOfMonth */
      journeyConnector.updateDayOfMonth(tdAll.journeyId, tdAll.EpayeDetachedUrl.updateDayOfMonthRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterDayOfMonth

      /** Update StartDates */
      journeyConnector.updateStartDates(tdAll.journeyId, tdAll.EpayeDetachedUrl.updateStartDatesResponse()).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterStartDatesResponse

      /** Update AffordableQuotes */
      journeyConnector
        .updateAffordableQuotes(tdAll.journeyId, tdAll.EpayeDetachedUrl.updateAffordableQuotesResponse())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterAffordableQuotesResponse

      /** Update Chosen Instalment plan */
      journeyConnector
        .updateChosenPaymentPlan(tdAll.journeyId, tdAll.EpayeDetachedUrl.updateSelectedPaymentPlanRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterSelectedPaymentPlan

      /** Update Checked Instalment plan */
      journeyConnector.updateHasCheckedPaymentPlan(tdAll.journeyId, tdAll.paymentPlanAnswersNoAffordability).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterCheckedPaymentPlanNonAffordability

      /** Update Details about Bank Account */
      journeyConnector
        .updateCanSetUpDirectDebit(
          tdAll.journeyId,
          tdAll.EpayeDetachedUrl.updateCanSetUpDirectDebitRequest(isAccountHolder = true)
        )
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl
        .journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true)

      /** Update Direct debit details */
      journeyConnector
        .updateDirectDebitDetails(tdAll.journeyId, tdAll.EpayeDetachedUrl.updateDirectDebitDetailsRequest()())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl
        .journeyAfterEnteredDirectDebitDetailsNoAffordability()

      /** Update Confirm Direct debit details */
      journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterConfirmedDirectDebitDetailsNoAffordability

      /** Update Agreed terms and conditions */
      journeyConnector
        .updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(value = true))
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl
        .journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true)

      /** Update Email Address */
      journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.email).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterSelectedEmail

      /** Update Email Verification Status */
      journeyConnector.updateEmailVerificationResult(tdAll.journeyId, EmailVerificationResult.Verified).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl
        .journeyAfterEmailVerificationResult(EmailVerificationResult.Verified)

      /** Update Arrangement (journey completed) */
      journeyConnector.updateArrangement(tdAll.journeyId, tdAll.EpayeDetachedUrl.updateArrangementRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl
        .journeyAfterSubmittedArrangementNoAffordability(isEmailAddressRequired = true)

      verifyCommonActions(numberOfAuthCalls = 44)
    }

    s"[EpayeService][Happy path with upfront payment]$epayeTestNameJourneyStages" in {
      stubCommonActions()

      val tdAll = new TdAll {
        override val journeyId: JourneyId         = journeyIdGenerator.readNextJourneyId()
        override val correlationId: CorrelationId = correlationIdGenerator.readNextCorrelationId()
      }

      given Request[_]         = tdAll.request
      val response: SjResponse =
        journeyConnector.Epaye.startJourneyEpayeService(tdAll.EpayeEpayeService.sjRequest).futureValue

      /** Start journey * */
      response shouldBe tdAll.EpayeEpayeService.sjResponse
      journeyConnector.getJourney(response.journeyId).futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterStarted

      /** Update tax id * */
      journeyConnector.updateTaxId(tdAll.journeyId, tdAll.EpayeEpayeService.updateTaxIdRequest()).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterDetermineTaxIds

      /** Update eligibility result * */
      journeyConnector
        .updateEligibilityCheckResult(tdAll.journeyId, tdAll.EpayeEpayeService.updateEligibilityCheckRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterEligibilityCheckEligible

      /** Update why cannot pay in full */
      journeyConnector.updateWhyCannotPayInFullAnswers(tdAll.journeyId, tdAll.whyCannotPayInFullNotRequired).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterWhyCannotPayInFullNotRequired

      /** Update CanPayUpfront * */
      journeyConnector
        .updateCanPayUpfront(tdAll.journeyId, tdAll.EpayeEpayeService.updateCanPayUpfrontYesRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterCanPayUpfrontYes

      /** Update UpfrontPaymentAmount */
      journeyConnector
        .updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.EpayeEpayeService.updateUpfrontPaymentAmountRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterUpfrontPaymentAmount

      /** Update ExtremeDates */
      journeyConnector
        .updateExtremeDates(tdAll.journeyId, tdAll.EpayeEpayeService.updateExtremeDatesRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterExtremeDates

      /** Update AffordabilityResult */
      journeyConnector
        .updateAffordabilityResult(tdAll.journeyId, tdAll.EpayeEpayeService.updateInstalmentAmountsRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterInstalmentAmounts

      /** Update CanPayWithinSixMonths */
      journeyConnector
        .updateCanPayWithinSixMonthsAnswers(tdAll.journeyId, tdAll.canPayWithinSixMonthsNotRequired)
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterCanPayWithinSixMonths

      /** Update MonthlyPaymentAmount */
      journeyConnector
        .updateMonthlyPaymentAmount(tdAll.journeyId, tdAll.EpayeEpayeService.updateMonthlyPaymentAmountRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterMonthlyPaymentAmount

      /** Update DayOfMonth */
      journeyConnector.updateDayOfMonth(tdAll.journeyId, tdAll.EpayeEpayeService.updateDayOfMonthRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterDayOfMonth

      /** Update StartDates */
      journeyConnector.updateStartDates(tdAll.journeyId, tdAll.EpayeEpayeService.updateStartDatesResponse()).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterStartDatesResponse

      /** Update AffordableQuotes */
      journeyConnector
        .updateAffordableQuotes(tdAll.journeyId, tdAll.EpayeEpayeService.updateAffordableQuotesResponse())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterAffordableQuotesResponse

      /** Update Chosen Instalment plan */
      journeyConnector
        .updateChosenPaymentPlan(tdAll.journeyId, tdAll.EpayeEpayeService.updateSelectedPaymentPlanRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterSelectedPaymentPlan

      /** Update Checked Instalment plan */
      journeyConnector.updateHasCheckedPaymentPlan(tdAll.journeyId, tdAll.paymentPlanAnswersNoAffordability).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterCheckedPaymentPlanNonAffordability

      /** Update Details about Bank Account */
      journeyConnector
        .updateCanSetUpDirectDebit(
          tdAll.journeyId,
          tdAll.EpayeEpayeService.updateCanSetUpDirectDebitRequest(isAccountHolder = true)
        )
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeEpayeService
        .journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true)

      /** Update Direct debit details */
      journeyConnector
        .updateDirectDebitDetails(tdAll.journeyId, tdAll.EpayeEpayeService.updateDirectDebitDetailsRequest()())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeEpayeService
        .journeyAfterEnteredDirectDebitDetailsNoAffordability()

      /** Update Confirm Direct debit details */
      journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterConfirmedDirectDebitDetailsNoAffordability

      /** Update Agreed terms and conditions */
      journeyConnector
        .updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(value = true))
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeEpayeService
        .journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true)

      /** Update Email Address */
      journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.email).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.EpayeEpayeService.journeyAfterSelectedEmail

      /** Update Email Verification Status */
      journeyConnector.updateEmailVerificationResult(tdAll.journeyId, EmailVerificationResult.Verified).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeEpayeService
        .journeyAfterEmailVerificationResult(EmailVerificationResult.Verified)

      /** Update Arrangement (journey completed) */
      journeyConnector
        .updateArrangement(tdAll.journeyId, tdAll.EpayeEpayeService.updateArrangementRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeEpayeService
        .journeyAfterSubmittedArrangementNoAffordability(isEmailAddressRequired = true)

      verifyCommonActions(numberOfAuthCalls = 44)
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
      "[EnteredCanYouSetUpDirectDebit]" +
      "[UpdateEnteredDirectDebitDetails]" +
      "[UpdateConfirmedDirectDebitDetails]" +
      "[UpdateHasAgreedTermsAndConditions]"

  "[Vat]" - {

    s"[Bta]$vatTestNameJourneyStages" in {
      stubCommonActions()
      val tdAll                = new TdAll {
        override val journeyId: JourneyId         = journeyIdGenerator.readNextJourneyId()
        override val correlationId: CorrelationId = correlationIdGenerator.readNextCorrelationId()
      }
      given Request[_]         = tdAll.request
      val response: SjResponse = journeyConnector.Vat.startJourneyBta(tdAll.VatBta.sjRequest).futureValue

      /** Start journey */
      response shouldBe tdAll.VatBta.sjResponse
      journeyConnector.getJourney(response.journeyId).futureValue shouldBe tdAll.VatBta.journeyAfterStarted

      /** Update tax id */
      journeyConnector.updateTaxId(tdAll.journeyId, tdAll.VatBta.updateTaxIdRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatBta.journeyAfterDetermineTaxIds

      /** Update eligibility result * */
      journeyConnector
        .updateEligibilityCheckResult(tdAll.journeyId, tdAll.VatBta.updateEligibilityCheckRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatBta.journeyAfterEligibilityCheckEligible

      /** Update why cannot pay in full */
      journeyConnector.updateWhyCannotPayInFullAnswers(tdAll.journeyId, tdAll.whyCannotPayInFullNotRequired).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatBta.journeyAfterWhyCannotPayInFullNotRequired

      /** Update CanPayUpfront */
      journeyConnector.updateCanPayUpfront(tdAll.journeyId, tdAll.VatBta.updateCanPayUpfrontYesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatBta.journeyAfterCanPayUpfrontYes

      /** Update UpfrontPaymentAmount */
      journeyConnector
        .updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.VatBta.updateUpfrontPaymentAmountRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatBta.journeyAfterUpfrontPaymentAmount

      /** Update ExtremeDates */
      journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.VatBta.updateExtremeDatesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatBta.journeyAfterExtremeDates

      /** Update AffordabilityResult */
      journeyConnector
        .updateAffordabilityResult(tdAll.journeyId, tdAll.VatBta.updateInstalmentAmountsRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatBta.journeyAfterInstalmentAmounts

      /** Update CanPayWithinSixMonths */
      journeyConnector
        .updateCanPayWithinSixMonthsAnswers(tdAll.journeyId, tdAll.canPayWithinSixMonthsNotRequired)
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatBta.journeyAfterCanPayWithinSixMonthsNotRequired

      /** Update MonthlyPaymentAmount */
      journeyConnector
        .updateMonthlyPaymentAmount(tdAll.journeyId, tdAll.VatBta.updateMonthlyPaymentAmountRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatBta.journeyAfterMonthlyPaymentAmount

      /** Update DayOfMonth */
      journeyConnector.updateDayOfMonth(tdAll.journeyId, tdAll.VatBta.updateDayOfMonthRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatBta.journeyAfterDayOfMonth

      /** Update StartDates */
      journeyConnector.updateStartDates(tdAll.journeyId, tdAll.VatBta.updateStartDatesResponse()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatBta.journeyAfterStartDatesResponse

      /** Update AffordableQuotes */
      journeyConnector
        .updateAffordableQuotes(tdAll.journeyId, tdAll.VatBta.updateAffordableQuotesResponse())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatBta.journeyAfterAffordableQuotesResponse

      /** Update Chosen Instalment plan */
      journeyConnector
        .updateChosenPaymentPlan(tdAll.journeyId, tdAll.VatBta.updateSelectedPaymentPlanRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatBta.journeyAfterSelectedPaymentPlan

      /** Update Checked Instalment plan */
      journeyConnector.updateHasCheckedPaymentPlan(tdAll.journeyId, tdAll.paymentPlanAnswersNoAffordability).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatBta.journeyAfterCheckedPaymentPlanNonAffordability

      /** Update Details about Bank Account */
      journeyConnector
        .updateCanSetUpDirectDebit(
          tdAll.journeyId,
          tdAll.VatBta.updateCanSetUpDirectDebitRequest(isAccountHolder = true)
        )
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatBta
        .journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true)

      /** Update Direct debit details */
      journeyConnector
        .updateDirectDebitDetails(tdAll.journeyId, tdAll.VatBta.updateDirectDebitDetailsRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatBta
        .journeyAfterEnteredDirectDebitDetailsNoAffordability()

      /** Update Confirm Direct debit details */
      journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability

      /** Update Agreed terms and conditions */
      journeyConnector
        .updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(value = true))
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatBta
        .journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true)

      /** Update Email Address */
      journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.email).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatBta.journeyAfterSelectedEmailNoAffordability

      /** Update Email Verification Status */
      journeyConnector.updateEmailVerificationResult(tdAll.journeyId, EmailVerificationResult.Verified).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatBta
        .journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified)

      /** Update Arrangement (journey completed) */
      journeyConnector.updateArrangement(tdAll.journeyId, tdAll.VatBta.updateArrangementRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatBta
        .journeyAfterSubmittedArrangementNoAffordability(isEmailAddressRequired = true)

      verifyCommonActions(numberOfAuthCalls = 44)
    }

    s"[GovUk]$vatTestNameJourneyStages" in {
      stubCommonActions()
      val tdAll                = new TdAll {
        override val journeyId: JourneyId         = journeyIdGenerator.readNextJourneyId()
        override val correlationId: CorrelationId = correlationIdGenerator.readNextCorrelationId()
      }
      given Request[_]         = tdAll.request
      val response: SjResponse = journeyConnector.Vat.startJourneyGovUk(tdAll.VatGovUk.sjRequest).futureValue

      /** Start journey */
      response shouldBe tdAll.VatGovUk.sjResponse
      journeyConnector.getJourney(response.journeyId).futureValue shouldBe tdAll.VatGovUk.journeyAfterStarted

      /** Update tax id */
      journeyConnector.updateTaxId(tdAll.journeyId, tdAll.VatGovUk.updateTaxIdRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatGovUk.journeyAfterDetermineTaxIds

      /** Update eligibility result * */
      journeyConnector
        .updateEligibilityCheckResult(tdAll.journeyId, tdAll.VatGovUk.updateEligibilityCheckRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatGovUk.journeyAfterEligibilityCheckEligible

      /** Update why cannot pay in full */
      journeyConnector.updateWhyCannotPayInFullAnswers(tdAll.journeyId, tdAll.whyCannotPayInFullNotRequired).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatGovUk.journeyAfterWhyCannotPayInFullNotRequired

      /** Update CanPayUpfront */
      journeyConnector.updateCanPayUpfront(tdAll.journeyId, tdAll.VatGovUk.updateCanPayUpfrontYesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatGovUk.journeyAfterCanPayUpfrontYes

      /** Update UpfrontPaymentAmount */
      journeyConnector
        .updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.VatGovUk.updateUpfrontPaymentAmountRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatGovUk.journeyAfterUpfrontPaymentAmount

      /** Update ExtremeDates */
      journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.VatGovUk.updateExtremeDatesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatGovUk.journeyAfterExtremeDates

      /** Update AffordabilityResult */
      journeyConnector
        .updateAffordabilityResult(tdAll.journeyId, tdAll.VatGovUk.updateInstalmentAmountsRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatGovUk.journeyAfterInstalmentAmounts

      /** Update CanPayWithinSixMonths */
      journeyConnector
        .updateCanPayWithinSixMonthsAnswers(tdAll.journeyId, tdAll.canPayWithinSixMonthsNotRequired)
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatGovUk.journeyAfterCanPayWithinSixMonths

      /** Update MonthlyPaymentAmount */
      journeyConnector
        .updateMonthlyPaymentAmount(tdAll.journeyId, tdAll.VatGovUk.updateMonthlyPaymentAmountRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatGovUk.journeyAfterMonthlyPaymentAmount

      /** Update DayOfMonth */
      journeyConnector.updateDayOfMonth(tdAll.journeyId, tdAll.VatGovUk.updateDayOfMonthRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatGovUk.journeyAfterDayOfMonth

      /** Update StartDates */
      journeyConnector.updateStartDates(tdAll.journeyId, tdAll.VatGovUk.updateStartDatesResponse()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatGovUk.journeyAfterStartDatesResponse

      /** Update AffordableQuotes */
      journeyConnector
        .updateAffordableQuotes(tdAll.journeyId, tdAll.VatGovUk.updateAffordableQuotesResponse())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatGovUk.journeyAfterAffordableQuotesResponse

      /** Update Chosen Instalment plan */
      journeyConnector
        .updateChosenPaymentPlan(tdAll.journeyId, tdAll.VatGovUk.updateSelectedPaymentPlanRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatGovUk.journeyAfterSelectedPaymentPlan

      /** Update Checked Instalment plan */
      journeyConnector.updateHasCheckedPaymentPlan(tdAll.journeyId, tdAll.paymentPlanAnswersNoAffordability).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatGovUk.journeyAfterCheckedPaymentPlanNonAffordability

      /** Update Details about Bank Account */
      journeyConnector
        .updateCanSetUpDirectDebit(
          tdAll.journeyId,
          tdAll.VatGovUk.updateCanSetUpDirectDebitRequest(isAccountHolder = true)
        )
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatGovUk
        .journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true)

      /** Update Direct debit details */
      journeyConnector
        .updateDirectDebitDetails(tdAll.journeyId, tdAll.VatGovUk.updateDirectDebitDetailsRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatGovUk
        .journeyAfterEnteredDirectDebitDetailsNoAffordability()

      /** Update Confirm Direct debit details */
      journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatGovUk.journeyAfterConfirmedDirectDebitDetailsNoAffordability

      /** Update Agreed terms and conditions */
      journeyConnector
        .updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(value = true))
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatGovUk
        .journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true)

      /** Update Email Address */
      journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.email).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatGovUk.journeyAfterSelectedEmail

      /** Update Email Verification Status */
      journeyConnector.updateEmailVerificationResult(tdAll.journeyId, EmailVerificationResult.Verified).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatGovUk
        .journeyAfterEmailVerificationResult(EmailVerificationResult.Verified)

      /** Update Arrangement (journey completed) */
      journeyConnector.updateArrangement(tdAll.journeyId, tdAll.VatGovUk.updateArrangementRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatGovUk
        .journeyAfterSubmittedArrangementNoAffordability(isEmailAddressRequired = true)

      verifyCommonActions(numberOfAuthCalls = 44)
    }

    s"[DetachedUrl]$vatTestNameJourneyStages" in {
      stubCommonActions()
      val tdAll                = new TdAll {
        override val journeyId: JourneyId         = journeyIdGenerator.readNextJourneyId()
        override val correlationId: CorrelationId = correlationIdGenerator.readNextCorrelationId()
      }
      given Request[_]         = tdAll.request
      val response: SjResponse =
        journeyConnector.Vat.startJourneyDetachedUrl(tdAll.VatDetachedUrl.sjRequest).futureValue

      /** Start journey */
      response shouldBe tdAll.VatDetachedUrl.sjResponse
      journeyConnector.getJourney(response.journeyId).futureValue shouldBe tdAll.VatDetachedUrl.journeyAfterStarted

      /** Update tax id */
      journeyConnector.updateTaxId(tdAll.journeyId, tdAll.VatDetachedUrl.updateTaxIdRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatDetachedUrl.journeyAfterDetermineTaxIds

      /** Update eligibility result * */
      journeyConnector
        .updateEligibilityCheckResult(tdAll.journeyId, tdAll.VatDetachedUrl.updateEligibilityCheckRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatDetachedUrl.journeyAfterEligibilityCheckEligible

      /** Update why cannot pay in full */
      journeyConnector.updateWhyCannotPayInFullAnswers(tdAll.journeyId, tdAll.whyCannotPayInFullNotRequired).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatDetachedUrl.journeyAfterWhyCannotPayInFullNotRequired

      /** Update CanPayUpfront */
      journeyConnector
        .updateCanPayUpfront(tdAll.journeyId, tdAll.VatDetachedUrl.updateCanPayUpfrontYesRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatDetachedUrl.journeyAfterCanPayUpfrontYes

      /** Update UpfrontPaymentAmount */
      journeyConnector
        .updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.VatDetachedUrl.updateUpfrontPaymentAmountRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatDetachedUrl.journeyAfterUpfrontPaymentAmount

      /** Update ExtremeDates */
      journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.VatDetachedUrl.updateExtremeDatesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatDetachedUrl.journeyAfterExtremeDates

      /** Update AffordabilityResult */
      journeyConnector
        .updateAffordabilityResult(tdAll.journeyId, tdAll.VatDetachedUrl.updateInstalmentAmountsRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatDetachedUrl.journeyAfterInstalmentAmounts

      /** Update CanPayWithinSixMonths */
      journeyConnector
        .updateCanPayWithinSixMonthsAnswers(tdAll.journeyId, tdAll.canPayWithinSixMonthsNotRequired)
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatDetachedUrl.journeyAfterCanPayWithinSixMonths

      /** Update MonthlyPaymentAmount */
      journeyConnector
        .updateMonthlyPaymentAmount(tdAll.journeyId, tdAll.VatDetachedUrl.updateMonthlyPaymentAmountRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatDetachedUrl.journeyAfterMonthlyPaymentAmount

      /** Update DayOfMonth */
      journeyConnector.updateDayOfMonth(tdAll.journeyId, tdAll.VatDetachedUrl.updateDayOfMonthRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatDetachedUrl.journeyAfterDayOfMonth

      /** Update StartDates */
      journeyConnector.updateStartDates(tdAll.journeyId, tdAll.VatDetachedUrl.updateStartDatesResponse()).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatDetachedUrl.journeyAfterStartDatesResponse

      /** Update AffordableQuotes */
      journeyConnector
        .updateAffordableQuotes(tdAll.journeyId, tdAll.VatDetachedUrl.updateAffordableQuotesResponse())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatDetachedUrl.journeyAfterAffordableQuotesResponse

      /** Update Chosen Instalment plan */
      journeyConnector
        .updateChosenPaymentPlan(tdAll.journeyId, tdAll.VatDetachedUrl.updateSelectedPaymentPlanRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatDetachedUrl.journeyAfterSelectedPaymentPlan

      /** Update Checked Instalment plan */
      journeyConnector.updateHasCheckedPaymentPlan(tdAll.journeyId, tdAll.paymentPlanAnswersNoAffordability).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatDetachedUrl.journeyAfterCheckedPaymentPlanNonAffordability

      /** Update Details about Bank Account */
      journeyConnector
        .updateCanSetUpDirectDebit(
          tdAll.journeyId,
          tdAll.VatDetachedUrl.updateCanSetUpDirectDebitRequest(isAccountHolder = true)
        )
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatDetachedUrl
        .journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true)

      /** Update Direct debit details */
      journeyConnector
        .updateDirectDebitDetails(tdAll.journeyId, tdAll.VatDetachedUrl.updateDirectDebitDetailsRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatDetachedUrl
        .journeyAfterEnteredDirectDebitDetailsNoAffordability()

      /** Update Confirm Direct debit details */
      journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatDetachedUrl.journeyAfterConfirmedDirectDebitDetailsNoAffordability

      /** Update Agreed terms and conditions */
      journeyConnector
        .updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(value = true))
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatDetachedUrl
        .journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true)

      /** Update Email Address */
      journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.email).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatDetachedUrl.journeyAfterSelectedEmail

      /** Update Email Verification Status */
      journeyConnector.updateEmailVerificationResult(tdAll.journeyId, EmailVerificationResult.Verified).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatDetachedUrl
        .journeyAfterEmailVerificationResult(EmailVerificationResult.Verified)

      /** Update Arrangement (journey completed) */
      journeyConnector.updateArrangement(tdAll.journeyId, tdAll.VatDetachedUrl.updateArrangementRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatDetachedUrl
        .journeyAfterSubmittedArrangementNoAffordability(isEmailAddressRequired = true)

      verifyCommonActions(numberOfAuthCalls = 44)
    }

    s"[VatService]$vatTestNameJourneyStages" in {
      stubCommonActions()
      val tdAll                = new TdAll {
        override val journeyId: JourneyId         = journeyIdGenerator.readNextJourneyId()
        override val correlationId: CorrelationId = correlationIdGenerator.readNextCorrelationId()
      }
      given Request[_]         = tdAll.request
      val response: SjResponse = journeyConnector.Vat.startJourneyVatService(tdAll.VatVatService.sjRequest).futureValue

      /** Start journey */
      response shouldBe tdAll.VatVatService.sjResponse
      journeyConnector.getJourney(response.journeyId).futureValue shouldBe tdAll.VatVatService.journeyAfterStarted

      /** Update tax id */
      journeyConnector.updateTaxId(tdAll.journeyId, tdAll.VatVatService.updateTaxIdRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatService.journeyAfterDetermineTaxIds

      /** Update eligibility result * */
      journeyConnector
        .updateEligibilityCheckResult(tdAll.journeyId, tdAll.VatVatService.updateEligibilityCheckRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatVatService.journeyAfterEligibilityCheckEligible

      /** Update why cannot pay in full */
      journeyConnector.updateWhyCannotPayInFullAnswers(tdAll.journeyId, tdAll.whyCannotPayInFullNotRequired).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatVatService.journeyAfterWhyCannotPayInFullNotRequired

      /** Update CanPayUpfront */
      journeyConnector
        .updateCanPayUpfront(tdAll.journeyId, tdAll.VatVatService.updateCanPayUpfrontYesRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatService.journeyAfterCanPayUpfrontYes

      /** Update UpfrontPaymentAmount */
      journeyConnector
        .updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.VatVatService.updateUpfrontPaymentAmountRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatVatService.journeyAfterUpfrontPaymentAmount

      /** Update ExtremeDates */
      journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.VatVatService.updateExtremeDatesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatService.journeyAfterExtremeDates

      /** Update AffordabilityResult */
      journeyConnector
        .updateAffordabilityResult(tdAll.journeyId, tdAll.VatVatService.updateInstalmentAmountsRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatVatService.journeyAfterInstalmentAmounts

      /** Update CanPayWithinSixMonths */
      journeyConnector
        .updateCanPayWithinSixMonthsAnswers(tdAll.journeyId, tdAll.canPayWithinSixMonthsNotRequired)
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatVatService.journeyAfterCanPayWithinSixMonths

      /** Update MonthlyPaymentAmount */
      journeyConnector
        .updateMonthlyPaymentAmount(tdAll.journeyId, tdAll.VatVatService.updateMonthlyPaymentAmountRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatVatService.journeyAfterMonthlyPaymentAmount

      /** Update DayOfMonth */
      journeyConnector.updateDayOfMonth(tdAll.journeyId, tdAll.VatVatService.updateDayOfMonthRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatService.journeyAfterDayOfMonth

      /** Update StartDates */
      journeyConnector.updateStartDates(tdAll.journeyId, tdAll.VatVatService.updateStartDatesResponse()).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatVatService.journeyAfterStartDatesResponse

      /** Update AffordableQuotes */
      journeyConnector
        .updateAffordableQuotes(tdAll.journeyId, tdAll.VatVatService.updateAffordableQuotesResponse())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatVatService.journeyAfterAffordableQuotesResponse

      /** Update Chosen Instalment plan */
      journeyConnector
        .updateChosenPaymentPlan(tdAll.journeyId, tdAll.VatVatService.updateSelectedPaymentPlanRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatVatService.journeyAfterSelectedPaymentPlan

      /** Update Checked Instalment plan */
      journeyConnector.updateHasCheckedPaymentPlan(tdAll.journeyId, tdAll.paymentPlanAnswersNoAffordability).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatVatService.journeyAfterCheckedPaymentPlanNonAffordability

      /** Update Details about Bank Account */
      journeyConnector
        .updateCanSetUpDirectDebit(
          tdAll.journeyId,
          tdAll.VatVatService.updateCanSetUpDirectDebitRequest(isAccountHolder = true)
        )
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatService
        .journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true)

      /** Update Direct debit details */
      journeyConnector
        .updateDirectDebitDetails(tdAll.journeyId, tdAll.VatVatService.updateDirectDebitDetailsRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatService
        .journeyAfterEnteredDirectDebitDetailsNoAffordability()

      /** Update Confirm Direct debit details */
      journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatVatService.journeyAfterConfirmedDirectDebitDetailsNoAffordability

      /** Update Agreed terms and conditions */
      journeyConnector
        .updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(value = true))
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatService
        .journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true)

      /** Update Email Address */
      journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.email).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatService.journeyAfterSelectedEmail

      /** Update Email Verification Status */
      journeyConnector.updateEmailVerificationResult(tdAll.journeyId, EmailVerificationResult.Verified).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatService
        .journeyAfterEmailVerificationResult(EmailVerificationResult.Verified)

      /** Update Arrangement (journey completed) */
      journeyConnector.updateArrangement(tdAll.journeyId, tdAll.VatVatService.updateArrangementRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatService
        .journeyAfterSubmittedArrangementNoAffordability(isEmailAddressRequired = true)

      verifyCommonActions(numberOfAuthCalls = 44)
    }

    s"[VatPenalties]$vatTestNameJourneyStages" in {
      stubCommonActions()
      val tdAll                = new TdAll {
        override val journeyId: JourneyId         = journeyIdGenerator.readNextJourneyId()
        override val correlationId: CorrelationId = correlationIdGenerator.readNextCorrelationId()
      }
      given Request[_]         = tdAll.request
      val response: SjResponse =
        journeyConnector.Vat.startJourneyVatPenalties(tdAll.VatVatPenalties.sjRequest).futureValue

      /** Start journey */
      response shouldBe tdAll.VatVatPenalties.sjResponse
      journeyConnector.getJourney(response.journeyId).futureValue shouldBe tdAll.VatVatPenalties.journeyAfterStarted

      /** Update tax id */
      journeyConnector.updateTaxId(tdAll.journeyId, tdAll.VatVatPenalties.updateTaxIdRequest()).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatVatPenalties.journeyAfterDetermineTaxIds

      /** Update eligibility result * */
      journeyConnector
        .updateEligibilityCheckResult(tdAll.journeyId, tdAll.VatVatPenalties.updateEligibilityCheckRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatVatPenalties.journeyAfterEligibilityCheckEligible

      /** Update why cannot pay in full */
      journeyConnector.updateWhyCannotPayInFullAnswers(tdAll.journeyId, tdAll.whyCannotPayInFullNotRequired).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatVatPenalties.journeyAfterWhyCannotPayInFullNotRequired

      /** Update CanPayUpfront */
      journeyConnector
        .updateCanPayUpfront(tdAll.journeyId, tdAll.VatVatPenalties.updateCanPayUpfrontYesRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatVatPenalties.journeyAfterCanPayUpfrontYes

      /** Update UpfrontPaymentAmount */
      journeyConnector
        .updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.VatVatPenalties.updateUpfrontPaymentAmountRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatVatPenalties.journeyAfterUpfrontPaymentAmount

      /** Update ExtremeDates */
      journeyConnector
        .updateExtremeDates(tdAll.journeyId, tdAll.VatVatPenalties.updateExtremeDatesRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatPenalties.journeyAfterExtremeDates

      /** Update AffordabilityResult */
      journeyConnector
        .updateAffordabilityResult(tdAll.journeyId, tdAll.VatVatPenalties.updateInstalmentAmountsRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatVatPenalties.journeyAfterInstalmentAmounts

      /** Update CanPayWithinSixMonths */
      journeyConnector
        .updateCanPayWithinSixMonthsAnswers(tdAll.journeyId, tdAll.canPayWithinSixMonthsNotRequired)
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatVatPenalties.journeyAfterCanPayWithinSixMonths

      /** Update MonthlyPaymentAmount */
      journeyConnector
        .updateMonthlyPaymentAmount(tdAll.journeyId, tdAll.VatVatPenalties.updateMonthlyPaymentAmountRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatVatPenalties.journeyAfterMonthlyPaymentAmount

      /** Update DayOfMonth */
      journeyConnector.updateDayOfMonth(tdAll.journeyId, tdAll.VatVatPenalties.updateDayOfMonthRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatPenalties.journeyAfterDayOfMonth

      /** Update StartDates */
      journeyConnector.updateStartDates(tdAll.journeyId, tdAll.VatVatPenalties.updateStartDatesResponse()).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatVatPenalties.journeyAfterStartDatesResponse

      /** Update AffordableQuotes */
      journeyConnector
        .updateAffordableQuotes(tdAll.journeyId, tdAll.VatVatPenalties.updateAffordableQuotesResponse())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatVatPenalties.journeyAfterAffordableQuotesResponse

      /** Update Chosen Instalment plan */
      journeyConnector
        .updateChosenPaymentPlan(tdAll.journeyId, tdAll.VatVatPenalties.updateSelectedPaymentPlanRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatVatPenalties.journeyAfterSelectedPaymentPlan

      /** Update Checked Instalment plan */
      journeyConnector.updateHasCheckedPaymentPlan(tdAll.journeyId, tdAll.paymentPlanAnswersNoAffordability).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatVatPenalties.journeyAfterCheckedPaymentPlanNonAffordability

      /** Update Details about Bank Account */
      journeyConnector
        .updateCanSetUpDirectDebit(
          tdAll.journeyId,
          tdAll.VatVatPenalties.updateCanSetUpDirectDebitRequest(isAccountHolder = true)
        )
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatPenalties
        .journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true)

      /** Update Direct debit details */
      journeyConnector
        .updateDirectDebitDetails(tdAll.journeyId, tdAll.VatVatPenalties.updateDirectDebitDetailsRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatPenalties
        .journeyAfterEnteredDirectDebitDetailsNoAffordability()

      /** Update Confirm Direct debit details */
      journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.VatVatPenalties.journeyAfterConfirmedDirectDebitDetailsNoAffordability

      /** Update Agreed terms and conditions */
      journeyConnector
        .updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(value = true))
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatPenalties
        .journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true)

      /** Update Email Address */
      journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.email).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatPenalties.journeyAfterSelectedEmail

      /** Update Email Verification Status */
      journeyConnector.updateEmailVerificationResult(tdAll.journeyId, EmailVerificationResult.Verified).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatPenalties
        .journeyAfterEmailVerificationResult(EmailVerificationResult.Verified)

      /** Update Arrangement (journey completed) */
      journeyConnector.updateArrangement(tdAll.journeyId, tdAll.VatVatPenalties.updateArrangementRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.VatVatPenalties
        .journeyAfterSubmittedArrangementNoAffordability(isEmailAddressRequired = true)

      verifyCommonActions(numberOfAuthCalls = 44)
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
      "[EnteredCanYouSetUpDirectDebit]" +
      "[UpdateEnteredDirectDebitDetails]" +
      "[UpdateConfirmedDirectDebitDetails]" +
      "[UpdateHasAgreedTermsAndConditions]"

  "[Sa]" - {

    s"[Bta]$saTestNameJourneyStages" in {
      stubCommonActions()
      val tdAll                = new TdAll {
        override val journeyId: JourneyId         = journeyIdGenerator.readNextJourneyId()
        override val correlationId: CorrelationId = correlationIdGenerator.readNextCorrelationId()
      }
      given Request[_]         = tdAll.request
      val response: SjResponse = journeyConnector.Sa.startJourneyBta(tdAll.SaBta.sjRequest).futureValue

      /** Start journey */
      response shouldBe tdAll.SaBta.sjResponse
      journeyConnector.getJourney(response.journeyId).futureValue shouldBe tdAll.SaBta.journeyAfterStarted

      /** Update tax id */
      journeyConnector.updateTaxId(tdAll.journeyId, tdAll.SaBta.updateTaxIdRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta.journeyAfterDetermineTaxIds

      /** Update eligibility result * */
      journeyConnector
        .updateEligibilityCheckResult(tdAll.journeyId, tdAll.SaBta.updateEligibilityCheckRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta.journeyAfterEligibilityCheckEligible

      /** Update why cannot pay in full */
      journeyConnector.updateWhyCannotPayInFullAnswers(tdAll.journeyId, tdAll.whyCannotPayInFullNotRequired).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SaBta.journeyAfterWhyCannotPayInFullNotRequired

      /** Update CanPayUpfront */
      journeyConnector.updateCanPayUpfront(tdAll.journeyId, tdAll.SaBta.updateCanPayUpfrontYesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta.journeyAfterCanPayUpfrontYes

      /** Update UpfrontPaymentAmount */
      journeyConnector
        .updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.SaBta.updateUpfrontPaymentAmountRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta.journeyAfterUpfrontPaymentAmount

      /** Update ExtremeDates */
      journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.SaBta.updateExtremeDatesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta.journeyAfterExtremeDates

      /** Update AffordabilityResult */
      journeyConnector
        .updateAffordabilityResult(tdAll.journeyId, tdAll.SaBta.updateInstalmentAmountsRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta.journeyAfterInstalmentAmounts

      /** Update CanPayWithinSixMonths */
      journeyConnector
        .updateCanPayWithinSixMonthsAnswers(tdAll.journeyId, tdAll.canPayWithinSixMonthsNotRequired)
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta.journeyAfterCanPayWithinSixMonths

      /** Update MonthlyPaymentAmount */
      journeyConnector
        .updateMonthlyPaymentAmount(tdAll.journeyId, tdAll.SaBta.updateMonthlyPaymentAmountRequest())
        .futureValue
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
      journeyConnector
        .updateChosenPaymentPlan(tdAll.journeyId, tdAll.SaBta.updateSelectedPaymentPlanRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta.journeyAfterSelectedPaymentPlan

      /** Update Checked Instalment plan */
      journeyConnector.updateHasCheckedPaymentPlan(tdAll.journeyId, tdAll.paymentPlanAnswersNoAffordability).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SaBta.journeyAfterCheckedPaymentPlanNonAffordability

      /** Update Details about Bank Account */
      journeyConnector
        .updateCanSetUpDirectDebit(
          tdAll.journeyId,
          tdAll.SaBta.updateCanSetUpDirectDebitRequest(isAccountHolder = true)
        )
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta
        .journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true)

      /** Update Direct debit details */
      journeyConnector
        .updateDirectDebitDetails(tdAll.journeyId, tdAll.SaBta.updateDirectDebitDetailsRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta
        .journeyAfterEnteredDirectDebitDetailsNoAffordability()

      /** Update Confirm Direct debit details */
      journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SaBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability

      /** Update Agreed terms and conditions */
      journeyConnector
        .updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(value = true))
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta
        .journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true)

      /** Update Email Address */
      journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.email).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SaBta.journeyAfterSelectedEmailNoAffordability

      /** Update Email Verification Status */
      journeyConnector.updateEmailVerificationResult(tdAll.journeyId, EmailVerificationResult.Verified).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta
        .journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified)

      /** Update Arrangement (journey completed) */
      journeyConnector.updateArrangement(tdAll.journeyId, tdAll.SaBta.updateArrangementRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta
        .journeyAfterSubmittedArrangementNoAffordability(isEmailAddressRequired = true)

      verifyCommonActions(numberOfAuthCalls = 44)
    }

    s"[Pta]$saTestNameJourneyStages" in {
      stubCommonActions()
      val tdAll                = new TdAll {
        override val journeyId: JourneyId         = journeyIdGenerator.readNextJourneyId()
        override val correlationId: CorrelationId = correlationIdGenerator.readNextCorrelationId()
      }
      given Request[_]         = tdAll.request
      val response: SjResponse = journeyConnector.Sa.startJourneyPta(tdAll.SaPta.sjRequest).futureValue

      /** Start journey */
      response shouldBe tdAll.SaPta.sjResponse
      journeyConnector.getJourney(response.journeyId).futureValue shouldBe tdAll.SaPta.journeyAfterStarted

      /** Update tax id */
      journeyConnector.updateTaxId(tdAll.journeyId, tdAll.SaPta.updateTaxIdRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaPta.journeyAfterDetermineTaxIds

      /** Update eligibility result * */
      journeyConnector
        .updateEligibilityCheckResult(tdAll.journeyId, tdAll.SaPta.updateEligibilityCheckRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaPta.journeyAfterEligibilityCheckEligible

      /** Update why cannot pay in full */
      journeyConnector.updateWhyCannotPayInFullAnswers(tdAll.journeyId, tdAll.whyCannotPayInFullNotRequired).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SaPta.journeyAfterWhyCannotPayInFullNotRequired

      /** Update CanPayUpfront */
      journeyConnector.updateCanPayUpfront(tdAll.journeyId, tdAll.SaPta.updateCanPayUpfrontYesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaPta.journeyAfterCanPayUpfrontYes

      /** Update UpfrontPaymentAmount */
      journeyConnector
        .updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.SaPta.updateUpfrontPaymentAmountRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaPta.journeyAfterUpfrontPaymentAmount

      /** Update ExtremeDates */
      journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.SaPta.updateExtremeDatesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaPta.journeyAfterExtremeDates

      /** Update AffordabilityResult */
      journeyConnector
        .updateAffordabilityResult(tdAll.journeyId, tdAll.SaPta.updateInstalmentAmountsRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaPta.journeyAfterInstalmentAmounts

      /** Update CanPayWithinSixMonths */
      journeyConnector
        .updateCanPayWithinSixMonthsAnswers(tdAll.journeyId, tdAll.canPayWithinSixMonthsNotRequired)
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaPta.journeyAfterCanPayWithinSixMonths

      /** Update MonthlyPaymentAmount */
      journeyConnector
        .updateMonthlyPaymentAmount(tdAll.journeyId, tdAll.SaPta.updateMonthlyPaymentAmountRequest())
        .futureValue
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
      journeyConnector
        .updateChosenPaymentPlan(tdAll.journeyId, tdAll.SaPta.updateSelectedPaymentPlanRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaPta.journeyAfterSelectedPaymentPlan

      /** Update Checked Instalment plan */
      journeyConnector.updateHasCheckedPaymentPlan(tdAll.journeyId, tdAll.paymentPlanAnswersNoAffordability).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SaPta.journeyAfterCheckedPaymentPlanNonAffordability

      /** Update Details about Bank Account */
      journeyConnector
        .updateCanSetUpDirectDebit(
          tdAll.journeyId,
          tdAll.SaPta.updateCanSetUpDirectDebitRequest(isAccountHolder = true)
        )
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaPta
        .journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true)

      /** Update Direct debit details */
      journeyConnector
        .updateDirectDebitDetails(tdAll.journeyId, tdAll.SaPta.updateDirectDebitDetailsRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaPta
        .journeyAfterEnteredDirectDebitDetailsNoAffordability()

      /** Update Confirm Direct debit details */
      journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SaPta.journeyAfterConfirmedDirectDebitDetailsNoAffordability

      /** Update Agreed terms and conditions */
      journeyConnector
        .updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(value = true))
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaPta
        .journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true)

      /** Update Email Address */
      journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.email).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaPta.journeyAfterSelectedEmail

      /** Update Email Verification Status */
      journeyConnector.updateEmailVerificationResult(tdAll.journeyId, EmailVerificationResult.Verified).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaPta.journeyAfterEmailVerificationResult(
        EmailVerificationResult.Verified
      )

      /** Update Arrangement (journey completed) */
      journeyConnector.updateArrangement(tdAll.journeyId, tdAll.SaPta.updateArrangementRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaPta
        .journeyAfterSubmittedArrangementNoAffordability(isEmailAddressRequired = true)

      verifyCommonActions(numberOfAuthCalls = 44)
    }

    s"[Mobile]$saTestNameJourneyStages" in {
      stubCommonActions()
      val tdAll                = new TdAll {
        override val journeyId: JourneyId         = journeyIdGenerator.readNextJourneyId()
        override val correlationId: CorrelationId = correlationIdGenerator.readNextCorrelationId()
      }
      given Request[_]         = tdAll.request
      val response: SjResponse = journeyConnector.Sa.startJourneyMobile(tdAll.SaMobile.sjRequest).futureValue

      /** Start journey */
      response shouldBe tdAll.SaMobile.sjResponse
      journeyConnector.getJourney(response.journeyId).futureValue shouldBe tdAll.SaMobile.journeyAfterStarted

      /** Update tax id */
      journeyConnector.updateTaxId(tdAll.journeyId, tdAll.SaMobile.updateTaxIdRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaMobile.journeyAfterDetermineTaxIds

      /** Update eligibility result * */
      journeyConnector
        .updateEligibilityCheckResult(tdAll.journeyId, tdAll.SaMobile.updateEligibilityCheckRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SaMobile.journeyAfterEligibilityCheckEligible

      /** Update why cannot pay in full */
      journeyConnector.updateWhyCannotPayInFullAnswers(tdAll.journeyId, tdAll.whyCannotPayInFullNotRequired).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SaMobile.journeyAfterWhyCannotPayInFullNotRequired

      /** Update CanPayUpfront */
      journeyConnector.updateCanPayUpfront(tdAll.journeyId, tdAll.SaMobile.updateCanPayUpfrontYesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaMobile.journeyAfterCanPayUpfrontYes

      /** Update UpfrontPaymentAmount */
      journeyConnector
        .updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.SaMobile.updateUpfrontPaymentAmountRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaMobile.journeyAfterUpfrontPaymentAmount

      /** Update ExtremeDates */
      journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.SaMobile.updateExtremeDatesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaMobile.journeyAfterExtremeDates

      /** Update AffordabilityResult */
      journeyConnector
        .updateAffordabilityResult(tdAll.journeyId, tdAll.SaMobile.updateInstalmentAmountsRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaMobile.journeyAfterInstalmentAmounts

      /** Update CanPayWithinSixMonths */
      journeyConnector
        .updateCanPayWithinSixMonthsAnswers(tdAll.journeyId, tdAll.canPayWithinSixMonthsNotRequired)
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaMobile.journeyAfterCanPayWithinSixMonths

      /** Update MonthlyPaymentAmount */
      journeyConnector
        .updateMonthlyPaymentAmount(tdAll.journeyId, tdAll.SaMobile.updateMonthlyPaymentAmountRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaMobile.journeyAfterMonthlyPaymentAmount

      /** Update DayOfMonth */
      journeyConnector.updateDayOfMonth(tdAll.journeyId, tdAll.SaMobile.updateDayOfMonthRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaMobile.journeyAfterDayOfMonth

      /** Update StartDates */
      journeyConnector.updateStartDates(tdAll.journeyId, tdAll.SaMobile.updateStartDatesResponse()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaMobile.journeyAfterStartDatesResponse

      /** Update AffordableQuotes */
      journeyConnector
        .updateAffordableQuotes(tdAll.journeyId, tdAll.SaMobile.updateAffordableQuotesResponse())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SaMobile.journeyAfterAffordableQuotesResponse

      /** Update Chosen Instalment plan */
      journeyConnector
        .updateChosenPaymentPlan(tdAll.journeyId, tdAll.SaMobile.updateSelectedPaymentPlanRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaMobile.journeyAfterSelectedPaymentPlan

      /** Update Checked Instalment plan */
      journeyConnector.updateHasCheckedPaymentPlan(tdAll.journeyId, tdAll.paymentPlanAnswersNoAffordability).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SaMobile.journeyAfterCheckedPaymentPlanNonAffordability

      /** Update Details about Bank Account */
      journeyConnector
        .updateCanSetUpDirectDebit(
          tdAll.journeyId,
          tdAll.SaMobile.updateCanSetUpDirectDebitRequest(isAccountHolder = true)
        )
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaMobile
        .journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true)

      /** Update Direct debit details */
      journeyConnector
        .updateDirectDebitDetails(tdAll.journeyId, tdAll.SaMobile.updateDirectDebitDetailsRequest()())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaMobile
        .journeyAfterEnteredDirectDebitDetailsNoAffordability()

      /** Update Confirm Direct debit details */
      journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SaMobile.journeyAfterConfirmedDirectDebitDetailsNoAffordability

      /** Update Agreed terms and conditions */
      journeyConnector
        .updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(value = true))
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaMobile
        .journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true)

      /** Update Email Address */
      journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.email).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaMobile.journeyAfterSelectedEmail

      /** Update Email Verification Status */
      journeyConnector.updateEmailVerificationResult(tdAll.journeyId, EmailVerificationResult.Verified).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaMobile
        .journeyAfterEmailVerificationResult(EmailVerificationResult.Verified)

      /** Update Arrangement (journey completed) */
      journeyConnector.updateArrangement(tdAll.journeyId, tdAll.SaMobile.updateArrangementRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaMobile
        .journeyAfterSubmittedArrangementNoAffordability(isEmailAddressRequired = true)

      verifyCommonActions(numberOfAuthCalls = 44)
    }

    s"[GovUk]$saTestNameJourneyStages" in {
      stubCommonActions()
      val tdAll                = new TdAll {
        override val journeyId: JourneyId         = journeyIdGenerator.readNextJourneyId()
        override val correlationId: CorrelationId = correlationIdGenerator.readNextCorrelationId()
      }
      given Request[_]         = tdAll.request
      val response: SjResponse = journeyConnector.Sa.startJourneyGovUk(tdAll.SaGovUk.sjRequest).futureValue

      /** Start journey */
      response shouldBe tdAll.SaGovUk.sjResponse
      journeyConnector.getJourney(response.journeyId).futureValue shouldBe tdAll.SaGovUk.journeyAfterStarted

      /** Update tax id */
      journeyConnector.updateTaxId(tdAll.journeyId, tdAll.SaGovUk.updateTaxIdRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaGovUk.journeyAfterDetermineTaxIds

      /** Update eligibility result * */
      journeyConnector
        .updateEligibilityCheckResult(tdAll.journeyId, tdAll.SaGovUk.updateEligibilityCheckRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SaGovUk.journeyAfterEligibilityCheckEligible

      /** Update why cannot pay in full */
      journeyConnector.updateWhyCannotPayInFullAnswers(tdAll.journeyId, tdAll.whyCannotPayInFullNotRequired).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SaGovUk.journeyAfterWhyCannotPayInFullNotRequired

      /** Update CanPayUpfront */
      journeyConnector.updateCanPayUpfront(tdAll.journeyId, tdAll.SaGovUk.updateCanPayUpfrontYesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaGovUk.journeyAfterCanPayUpfrontYes

      /** Update UpfrontPaymentAmount */
      journeyConnector
        .updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.SaGovUk.updateUpfrontPaymentAmountRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaGovUk.journeyAfterUpfrontPaymentAmount

      /** Update ExtremeDates */
      journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.SaGovUk.updateExtremeDatesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaGovUk.journeyAfterExtremeDates

      /** Update AffordabilityResult */
      journeyConnector
        .updateAffordabilityResult(tdAll.journeyId, tdAll.SaGovUk.updateInstalmentAmountsRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaGovUk.journeyAfterInstalmentAmounts

      /** Update CanPayWithinSixMonths */
      journeyConnector
        .updateCanPayWithinSixMonthsAnswers(tdAll.journeyId, tdAll.canPayWithinSixMonthsNotRequired)
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaGovUk.journeyAfterCanPayWithinSixMonths

      /** Update MonthlyPaymentAmount */
      journeyConnector
        .updateMonthlyPaymentAmount(tdAll.journeyId, tdAll.SaGovUk.updateMonthlyPaymentAmountRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaGovUk.journeyAfterMonthlyPaymentAmount

      /** Update DayOfMonth */
      journeyConnector.updateDayOfMonth(tdAll.journeyId, tdAll.SaGovUk.updateDayOfMonthRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaGovUk.journeyAfterDayOfMonth

      /** Update StartDates */
      journeyConnector.updateStartDates(tdAll.journeyId, tdAll.SaGovUk.updateStartDatesResponse()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaGovUk.journeyAfterStartDatesResponse

      /** Update AffordableQuotes */
      journeyConnector
        .updateAffordableQuotes(tdAll.journeyId, tdAll.SaGovUk.updateAffordableQuotesResponse())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SaGovUk.journeyAfterAffordableQuotesResponse

      /** Update Chosen Instalment plan */
      journeyConnector
        .updateChosenPaymentPlan(tdAll.journeyId, tdAll.SaGovUk.updateSelectedPaymentPlanRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaGovUk.journeyAfterSelectedPaymentPlan

      /** Update Checked Instalment plan */
      journeyConnector.updateHasCheckedPaymentPlan(tdAll.journeyId, tdAll.paymentPlanAnswersNoAffordability).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SaGovUk.journeyAfterCheckedPaymentPlanNonAffordability

      /** Update Details about Bank Account */
      journeyConnector
        .updateCanSetUpDirectDebit(
          tdAll.journeyId,
          tdAll.SaGovUk.updateCanSetUpDirectDebitRequest(isAccountHolder = true)
        )
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaGovUk
        .journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true)

      /** Update Direct debit details */
      journeyConnector
        .updateDirectDebitDetails(tdAll.journeyId, tdAll.SaGovUk.updateDirectDebitDetailsRequest()())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaGovUk
        .journeyAfterEnteredDirectDebitDetailsNoAffordability()

      /** Update Confirm Direct debit details */
      journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SaGovUk.journeyAfterConfirmedDirectDebitDetailsNoAffordability

      /** Update Agreed terms and conditions */
      journeyConnector
        .updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(value = true))
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaGovUk
        .journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true)

      /** Update Email Address */
      journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.email).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaGovUk.journeyAfterSelectedEmail

      /** Update Email Verification Status */
      journeyConnector.updateEmailVerificationResult(tdAll.journeyId, EmailVerificationResult.Verified).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaGovUk
        .journeyAfterEmailVerificationResult(EmailVerificationResult.Verified)

      /** Update Arrangement (journey completed) */
      journeyConnector.updateArrangement(tdAll.journeyId, tdAll.SaGovUk.updateArrangementRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaGovUk
        .journeyAfterSubmittedArrangementNoAffordability(isEmailAddressRequired = true)

      verifyCommonActions(numberOfAuthCalls = 44)
    }

    s"[DetachedUrl]$saTestNameJourneyStages" in {
      stubCommonActions()
      val tdAll                = new TdAll {
        override val journeyId: JourneyId         = journeyIdGenerator.readNextJourneyId()
        override val correlationId: CorrelationId = correlationIdGenerator.readNextCorrelationId()
      }
      given Request[_]         = tdAll.request
      val response: SjResponse = journeyConnector.Sa.startJourneyDetachedUrl(tdAll.SaDetachedUrl.sjRequest).futureValue

      /** Start journey */
      response shouldBe tdAll.SaDetachedUrl.sjResponse
      journeyConnector.getJourney(response.journeyId).futureValue shouldBe tdAll.SaDetachedUrl.journeyAfterStarted

      /** Update tax id */
      journeyConnector.updateTaxId(tdAll.journeyId, tdAll.SaDetachedUrl.updateTaxIdRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaDetachedUrl.journeyAfterDetermineTaxIds

      /** Update eligibility result * */
      journeyConnector
        .updateEligibilityCheckResult(tdAll.journeyId, tdAll.SaDetachedUrl.updateEligibilityCheckRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SaDetachedUrl.journeyAfterEligibilityCheckEligible

      /** Update why cannot pay in full */
      journeyConnector.updateWhyCannotPayInFullAnswers(tdAll.journeyId, tdAll.whyCannotPayInFullNotRequired).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SaDetachedUrl.journeyAfterWhyCannotPayInFullNotRequired

      /** Update CanPayUpfront */
      journeyConnector
        .updateCanPayUpfront(tdAll.journeyId, tdAll.SaDetachedUrl.updateCanPayUpfrontYesRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaDetachedUrl.journeyAfterCanPayUpfrontYes

      /** Update UpfrontPaymentAmount */
      journeyConnector
        .updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.SaDetachedUrl.updateUpfrontPaymentAmountRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SaDetachedUrl.journeyAfterUpfrontPaymentAmount

      /** Update ExtremeDates */
      journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.SaDetachedUrl.updateExtremeDatesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaDetachedUrl.journeyAfterExtremeDates

      /** Update AffordabilityResult */
      journeyConnector
        .updateAffordabilityResult(tdAll.journeyId, tdAll.SaDetachedUrl.updateInstalmentAmountsRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SaDetachedUrl.journeyAfterInstalmentAmounts

      /** Update CanPayWithinSixMonths */
      journeyConnector
        .updateCanPayWithinSixMonthsAnswers(tdAll.journeyId, tdAll.canPayWithinSixMonthsNotRequired)
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SaDetachedUrl.journeyAfterCanPayWithinSixMonths

      /** Update MonthlyPaymentAmount */
      journeyConnector
        .updateMonthlyPaymentAmount(tdAll.journeyId, tdAll.SaDetachedUrl.updateMonthlyPaymentAmountRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SaDetachedUrl.journeyAfterMonthlyPaymentAmount

      /** Update DayOfMonth */
      journeyConnector.updateDayOfMonth(tdAll.journeyId, tdAll.SaDetachedUrl.updateDayOfMonthRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaDetachedUrl.journeyAfterDayOfMonth

      /** Update StartDates */
      journeyConnector.updateStartDates(tdAll.journeyId, tdAll.SaDetachedUrl.updateStartDatesResponse()).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SaDetachedUrl.journeyAfterStartDatesResponse

      /** Update AffordableQuotes */
      journeyConnector
        .updateAffordableQuotes(tdAll.journeyId, tdAll.SaDetachedUrl.updateAffordableQuotesResponse())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SaDetachedUrl.journeyAfterAffordableQuotesResponse

      /** Update Chosen Instalment plan */
      journeyConnector
        .updateChosenPaymentPlan(tdAll.journeyId, tdAll.SaDetachedUrl.updateSelectedPaymentPlanRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SaDetachedUrl.journeyAfterSelectedPaymentPlan

      /** Update Checked Instalment plan */
      journeyConnector.updateHasCheckedPaymentPlan(tdAll.journeyId, tdAll.paymentPlanAnswersNoAffordability).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SaDetachedUrl.journeyAfterCheckedPaymentPlanNonAffordability

      /** Update Details about Bank Account */
      journeyConnector
        .updateCanSetUpDirectDebit(
          tdAll.journeyId,
          tdAll.SaDetachedUrl.updateCanSetUpDirectDebitRequest(isAccountHolder = true)
        )
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaDetachedUrl
        .journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true)

      /** Update Direct debit details */
      journeyConnector
        .updateDirectDebitDetails(tdAll.journeyId, tdAll.SaDetachedUrl.updateDirectDebitDetailsRequest()())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaDetachedUrl
        .journeyAfterEnteredDirectDebitDetailsNoAffordability()

      /** Update Confirm Direct debit details */
      journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SaDetachedUrl.journeyAfterConfirmedDirectDebitDetailsNoAffordability

      /** Update Agreed terms and conditions */
      journeyConnector
        .updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(value = true))
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaDetachedUrl
        .journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true)

      /** Update Email Address */
      journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.email).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaDetachedUrl.journeyAfterSelectedEmail

      /** Update Email Verification Status */
      journeyConnector.updateEmailVerificationResult(tdAll.journeyId, EmailVerificationResult.Verified).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaDetachedUrl
        .journeyAfterEmailVerificationResult(EmailVerificationResult.Verified)

      /** Update Arrangement (journey completed) */
      journeyConnector.updateArrangement(tdAll.journeyId, tdAll.SaDetachedUrl.updateArrangementRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaDetachedUrl
        .journeyAfterSubmittedArrangementNoAffordability(isEmailAddressRequired = true)

      verifyCommonActions(numberOfAuthCalls = 44)
    }

    s"[ItsaViewAndChange]$saTestNameJourneyStages" in {
      stubCommonActions()
      val tdAll                = new TdAll {
        override val journeyId: JourneyId         = journeyIdGenerator.readNextJourneyId()
        override val correlationId: CorrelationId = correlationIdGenerator.readNextCorrelationId()
      }
      given Request[_]         = tdAll.request
      val response: SjResponse =
        journeyConnector.Sa.startJourneyItsaViewAndChange(tdAll.SaItsaViewAndChange.sjRequest).futureValue

      /** Start journey */
      response shouldBe tdAll.SaItsaViewAndChange.sjResponse
      journeyConnector.getJourney(response.journeyId).futureValue shouldBe tdAll.SaItsaViewAndChange.journeyAfterStarted

      /** Update tax id */
      journeyConnector.updateTaxId(tdAll.journeyId, tdAll.SaItsaViewAndChange.updateTaxIdRequest()).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SaItsaViewAndChange.journeyAfterDetermineTaxIds

      /** Update eligibility result * */
      journeyConnector
        .updateEligibilityCheckResult(tdAll.journeyId, tdAll.SaItsaViewAndChange.updateEligibilityCheckRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SaItsaViewAndChange.journeyAfterEligibilityCheckEligible

      /** Update why cannot pay in full */
      journeyConnector.updateWhyCannotPayInFullAnswers(tdAll.journeyId, tdAll.whyCannotPayInFullNotRequired).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SaItsaViewAndChange.journeyAfterWhyCannotPayInFullNotRequired

      /** Update CanPayUpfront */
      journeyConnector
        .updateCanPayUpfront(tdAll.journeyId, tdAll.SaItsaViewAndChange.updateCanPayUpfrontYesRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SaItsaViewAndChange.journeyAfterCanPayUpfrontYes

      /** Update UpfrontPaymentAmount */
      journeyConnector
        .updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.SaItsaViewAndChange.updateUpfrontPaymentAmountRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SaItsaViewAndChange.journeyAfterUpfrontPaymentAmount

      /** Update ExtremeDates */
      journeyConnector
        .updateExtremeDates(tdAll.journeyId, tdAll.SaItsaViewAndChange.updateExtremeDatesRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SaItsaViewAndChange.journeyAfterExtremeDates

      /** Update AffordabilityResult */
      journeyConnector
        .updateAffordabilityResult(tdAll.journeyId, tdAll.SaItsaViewAndChange.updateInstalmentAmountsRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SaItsaViewAndChange.journeyAfterInstalmentAmounts

      /** Update CanPayWithinSixMonths */
      journeyConnector
        .updateCanPayWithinSixMonthsAnswers(tdAll.journeyId, tdAll.canPayWithinSixMonthsNotRequired)
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SaItsaViewAndChange.journeyAfterCanPayWithinSixMonths

      /** Update MonthlyPaymentAmount */
      journeyConnector
        .updateMonthlyPaymentAmount(tdAll.journeyId, tdAll.SaItsaViewAndChange.updateMonthlyPaymentAmountRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SaItsaViewAndChange.journeyAfterMonthlyPaymentAmount

      /** Update DayOfMonth */
      journeyConnector
        .updateDayOfMonth(tdAll.journeyId, tdAll.SaItsaViewAndChange.updateDayOfMonthRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaItsaViewAndChange.journeyAfterDayOfMonth

      /** Update StartDates */
      journeyConnector
        .updateStartDates(tdAll.journeyId, tdAll.SaItsaViewAndChange.updateStartDatesResponse())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SaItsaViewAndChange.journeyAfterStartDatesResponse

      /** Update AffordableQuotes */
      journeyConnector
        .updateAffordableQuotes(tdAll.journeyId, tdAll.SaItsaViewAndChange.updateAffordableQuotesResponse())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SaItsaViewAndChange.journeyAfterAffordableQuotesResponse

      /** Update Chosen Instalment plan */
      journeyConnector
        .updateChosenPaymentPlan(tdAll.journeyId, tdAll.SaItsaViewAndChange.updateSelectedPaymentPlanRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SaItsaViewAndChange.journeyAfterSelectedPaymentPlan

      /** Update Checked Instalment plan */
      journeyConnector.updateHasCheckedPaymentPlan(tdAll.journeyId, tdAll.paymentPlanAnswersNoAffordability).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SaItsaViewAndChange.journeyAfterCheckedPaymentPlanNonAffordability

      /** Update Details about Bank Account */
      journeyConnector
        .updateCanSetUpDirectDebit(
          tdAll.journeyId,
          tdAll.SaItsaViewAndChange.updateCanSetUpDirectDebitRequest(isAccountHolder = true)
        )
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaItsaViewAndChange
        .journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true)

      /** Update Direct debit details */
      journeyConnector
        .updateDirectDebitDetails(tdAll.journeyId, tdAll.SaItsaViewAndChange.updateDirectDebitDetailsRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaItsaViewAndChange
        .journeyAfterEnteredDirectDebitDetailsNoAffordability()

      /** Update Confirm Direct debit details */
      journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SaItsaViewAndChange.journeyAfterConfirmedDirectDebitDetailsNoAffordability

      /** Update Agreed terms and conditions */
      journeyConnector
        .updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(value = true))
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaItsaViewAndChange
        .journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true)

      /** Update Email Address */
      journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.email).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SaItsaViewAndChange.journeyAfterSelectedEmailNoAffordability

      /** Update Email Verification Status */
      journeyConnector.updateEmailVerificationResult(tdAll.journeyId, EmailVerificationResult.Verified).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaItsaViewAndChange
        .journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified)

      /** Update Arrangement (journey completed) */
      journeyConnector
        .updateArrangement(tdAll.journeyId, tdAll.SaItsaViewAndChange.updateArrangementRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaItsaViewAndChange
        .journeyAfterSubmittedArrangementNoAffordability(isEmailAddressRequired = true)

      verifyCommonActions(numberOfAuthCalls = 44)
    }
  }

  private val simpTestNameJourneyStages: String =
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
      "[EnteredCanYouSetUpDirectDebit]" +
      "[UpdateEnteredDirectDebitDetails]" +
      "[UpdateConfirmedDirectDebitDetails]" +
      "[UpdateHasAgreedTermsAndConditions]"

  "[Simp]" - {
    s"[Pta]$simpTestNameJourneyStages" in {
      stubCommonActions()
      val tdAll                = new TdAll {
        override val journeyId: JourneyId         = journeyIdGenerator.readNextJourneyId()
        override val correlationId: CorrelationId = correlationIdGenerator.readNextCorrelationId()
      }
      given Request[_]         = tdAll.request
      val response: SjResponse = journeyConnector.Simp.startJourneyPta(tdAll.SimpPta.sjRequest).futureValue

      /** Start journey */
      response shouldBe tdAll.SimpPta.sjResponse
      journeyConnector.getJourney(response.journeyId).futureValue shouldBe tdAll.SimpPta.journeyAfterStarted

      /** Update tax id */
      journeyConnector.updateTaxId(tdAll.journeyId, tdAll.SimpPta.updateTaxIdRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpPta.journeyAfterDetermineTaxIds

      /** Update eligibility result * */
      journeyConnector
        .updateEligibilityCheckResult(tdAll.journeyId, tdAll.SimpPta.updateEligibilityCheckRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SimpPta.journeyAfterEligibilityCheckEligible

      /** Update why cannot pay in full */
      journeyConnector.updateWhyCannotPayInFullAnswers(tdAll.journeyId, tdAll.whyCannotPayInFullNotRequired).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SimpPta.journeyAfterWhyCannotPayInFullNotRequired

      /** Update CanPayUpfront */
      journeyConnector.updateCanPayUpfront(tdAll.journeyId, tdAll.SimpPta.updateCanPayUpfrontYesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpPta.journeyAfterCanPayUpfrontYes

      /** Update UpfrontPaymentAmount */
      journeyConnector
        .updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.SimpPta.updateUpfrontPaymentAmountRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpPta.journeyAfterUpfrontPaymentAmount

      /** Update ExtremeDates */
      journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.SimpPta.updateExtremeDatesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpPta.journeyAfterExtremeDates

      /** Update AffordabilityResult */
      journeyConnector
        .updateAffordabilityResult(tdAll.journeyId, tdAll.SimpPta.updateInstalmentAmountsRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpPta.journeyAfterInstalmentAmounts

      /** Update CanPayWithinSixMonths */
      journeyConnector
        .updateCanPayWithinSixMonthsAnswers(tdAll.journeyId, tdAll.canPayWithinSixMonthsNotRequired)
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpPta.journeyAfterCanPayWithinSixMonths

      /** Update MonthlyPaymentAmount */
      journeyConnector
        .updateMonthlyPaymentAmount(tdAll.journeyId, tdAll.SimpPta.updateMonthlyPaymentAmountRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpPta.journeyAfterMonthlyPaymentAmount

      /** Update DayOfMonth */
      journeyConnector.updateDayOfMonth(tdAll.journeyId, tdAll.SimpPta.updateDayOfMonthRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpPta.journeyAfterDayOfMonth

      /** Update StartDates */
      journeyConnector.updateStartDates(tdAll.journeyId, tdAll.SimpPta.updateStartDatesResponse()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpPta.journeyAfterStartDatesResponse

      /** Update AffordableQuotes */
      journeyConnector
        .updateAffordableQuotes(tdAll.journeyId, tdAll.SimpPta.updateAffordableQuotesResponse())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SimpPta.journeyAfterAffordableQuotesResponse

      /** Update Chosen Instalment plan */
      journeyConnector
        .updateChosenPaymentPlan(tdAll.journeyId, tdAll.SimpPta.updateSelectedPaymentPlanRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpPta.journeyAfterSelectedPaymentPlan

      /** Update Checked Instalment plan */
      journeyConnector.updateHasCheckedPaymentPlan(tdAll.journeyId, tdAll.paymentPlanAnswersNoAffordability).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SimpPta.journeyAfterCheckedPaymentPlanNonAffordability

      /** Update Details about Bank Account */
      journeyConnector
        .updateCanSetUpDirectDebit(
          tdAll.journeyId,
          tdAll.SimpPta.updateCanSetUpDirectDebitRequest(isAccountHolder = true)
        )
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpPta
        .journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true)

      /** Update Direct debit details */
      journeyConnector
        .updateDirectDebitDetails(tdAll.journeyId, tdAll.SimpPta.updateDirectDebitDetailsRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpPta
        .journeyAfterEnteredDirectDebitDetailsNoAffordability()

      /** Update Confirm Direct debit details */
      journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SimpPta.journeyAfterConfirmedDirectDebitDetailsNoAffordability

      /** Update Agreed terms and conditions */
      journeyConnector
        .updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(value = true))
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpPta
        .journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true)

      /** Update Email Address */
      journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.email).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpPta.journeyAfterSelectedEmail

      /** Update Email Verification Status */
      journeyConnector.updateEmailVerificationResult(tdAll.journeyId, EmailVerificationResult.Verified).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpPta
        .journeyAfterEmailVerificationResult(EmailVerificationResult.Verified)

      /** Update Arrangement (journey completed) */
      journeyConnector.updateArrangement(tdAll.journeyId, tdAll.SimpPta.updateArrangementRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpPta
        .journeyAfterSubmittedArrangementNoAffordability(isEmailAddressRequired = true)

      verifyCommonActions(numberOfAuthCalls = 44)
    }

    s"[Mobile]$simpTestNameJourneyStages" in {
      stubCommonActions()
      val tdAll                = new TdAll {
        override val journeyId: JourneyId         = journeyIdGenerator.readNextJourneyId()
        override val correlationId: CorrelationId = correlationIdGenerator.readNextCorrelationId()
      }
      given Request[_]         = tdAll.request
      val response: SjResponse = journeyConnector.Simp.startJourneyMobile(tdAll.SimpMobile.sjRequest).futureValue

      /** Start journey */
      response shouldBe tdAll.SimpMobile.sjResponse
      journeyConnector.getJourney(response.journeyId).futureValue shouldBe tdAll.SimpMobile.journeyAfterStarted

      /** Update tax id */
      journeyConnector.updateTaxId(tdAll.journeyId, tdAll.SimpMobile.updateTaxIdRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpMobile.journeyAfterDetermineTaxIds

      /** Update eligibility result * */
      journeyConnector
        .updateEligibilityCheckResult(tdAll.journeyId, tdAll.SimpMobile.updateEligibilityCheckRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SimpMobile.journeyAfterEligibilityCheckEligible

      /** Update why cannot pay in full */
      journeyConnector.updateWhyCannotPayInFullAnswers(tdAll.journeyId, tdAll.whyCannotPayInFullNotRequired).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SimpMobile.journeyAfterWhyCannotPayInFullNotRequired

      /** Update CanPayUpfront */
      journeyConnector
        .updateCanPayUpfront(tdAll.journeyId, tdAll.SimpMobile.updateCanPayUpfrontYesRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpMobile.journeyAfterCanPayUpfrontYes

      /** Update UpfrontPaymentAmount */
      journeyConnector
        .updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.SimpMobile.updateUpfrontPaymentAmountRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SimpMobile.journeyAfterUpfrontPaymentAmount

      /** Update ExtremeDates */
      journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.SimpMobile.updateExtremeDatesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpMobile.journeyAfterExtremeDates

      /** Update AffordabilityResult */
      journeyConnector
        .updateAffordabilityResult(tdAll.journeyId, tdAll.SimpMobile.updateInstalmentAmountsRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpMobile.journeyAfterInstalmentAmounts

      /** Update CanPayWithinSixMonths */
      journeyConnector
        .updateCanPayWithinSixMonthsAnswers(tdAll.journeyId, tdAll.canPayWithinSixMonthsNotRequired)
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SimpMobile.journeyAfterCanPayWithinSixMonths

      /** Update MonthlyPaymentAmount */
      journeyConnector
        .updateMonthlyPaymentAmount(tdAll.journeyId, tdAll.SimpMobile.updateMonthlyPaymentAmountRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SimpMobile.journeyAfterMonthlyPaymentAmount

      /** Update DayOfMonth */
      journeyConnector.updateDayOfMonth(tdAll.journeyId, tdAll.SimpMobile.updateDayOfMonthRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpMobile.journeyAfterDayOfMonth

      /** Update StartDates */
      journeyConnector.updateStartDates(tdAll.journeyId, tdAll.SimpMobile.updateStartDatesResponse()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpMobile.journeyAfterStartDatesResponse

      /** Update AffordableQuotes */
      journeyConnector
        .updateAffordableQuotes(tdAll.journeyId, tdAll.SimpMobile.updateAffordableQuotesResponse())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SimpMobile.journeyAfterAffordableQuotesResponse

      /** Update Chosen Instalment plan */
      journeyConnector
        .updateChosenPaymentPlan(tdAll.journeyId, tdAll.SimpMobile.updateSelectedPaymentPlanRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpMobile.journeyAfterSelectedPaymentPlan

      /** Update Checked Instalment plan */
      journeyConnector.updateHasCheckedPaymentPlan(tdAll.journeyId, tdAll.paymentPlanAnswersNoAffordability).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SimpMobile.journeyAfterCheckedPaymentPlanNonAffordability

      /** Update Details about Bank Account */
      journeyConnector
        .updateCanSetUpDirectDebit(
          tdAll.journeyId,
          tdAll.SimpMobile.updateCanSetUpDirectDebitRequest(isAccountHolder = true)
        )
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpMobile
        .journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true)

      /** Update Direct debit details */
      journeyConnector
        .updateDirectDebitDetails(tdAll.journeyId, tdAll.SimpMobile.updateDirectDebitDetailsRequest()())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpMobile
        .journeyAfterEnteredDirectDebitDetailsNoAffordability()

      /** Update Confirm Direct debit details */
      journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SimpMobile.journeyAfterConfirmedDirectDebitDetailsNoAffordability

      /** Update Agreed terms and conditions */
      journeyConnector
        .updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(value = true))
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpMobile
        .journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true)

      /** Update Email Address */
      journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.email).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpMobile.journeyAfterSelectedEmail

      /** Update Email Verification Status */
      journeyConnector.updateEmailVerificationResult(tdAll.journeyId, EmailVerificationResult.Verified).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpMobile
        .journeyAfterEmailVerificationResult(EmailVerificationResult.Verified)

      /** Update Arrangement (journey completed) */
      journeyConnector.updateArrangement(tdAll.journeyId, tdAll.SimpMobile.updateArrangementRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpMobile
        .journeyAfterSubmittedArrangementNoAffordability(isEmailAddressRequired = true)

      verifyCommonActions(numberOfAuthCalls = 44)
    }

    s"[GovUk]$simpTestNameJourneyStages" in {
      stubCommonActions()
      val tdAll                = new TdAll {
        override val journeyId: JourneyId         = journeyIdGenerator.readNextJourneyId()
        override val correlationId: CorrelationId = correlationIdGenerator.readNextCorrelationId()
      }
      given Request[_]         = tdAll.request
      val response: SjResponse = journeyConnector.Simp.startJourneyGovUk(tdAll.SimpGovUk.sjRequest).futureValue

      /** Start journey */
      response shouldBe tdAll.SimpGovUk.sjResponse
      journeyConnector.getJourney(response.journeyId).futureValue shouldBe tdAll.SimpGovUk.journeyAfterStarted

      /** Update tax id */
      journeyConnector.updateTaxId(tdAll.journeyId, tdAll.SimpGovUk.updateTaxIdRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpGovUk.journeyAfterDetermineTaxIds

      /** Update eligibility result * */
      journeyConnector
        .updateEligibilityCheckResult(tdAll.journeyId, tdAll.SimpGovUk.updateEligibilityCheckRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SimpGovUk.journeyAfterEligibilityCheckEligible

      /** Update why cannot pay in full */
      journeyConnector.updateWhyCannotPayInFullAnswers(tdAll.journeyId, tdAll.whyCannotPayInFullNotRequired).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SimpGovUk.journeyAfterWhyCannotPayInFullNotRequired

      /** Update CanPayUpfront */
      journeyConnector.updateCanPayUpfront(tdAll.journeyId, tdAll.SimpGovUk.updateCanPayUpfrontYesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpGovUk.journeyAfterCanPayUpfrontYes

      /** Update UpfrontPaymentAmount */
      journeyConnector
        .updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.SimpGovUk.updateUpfrontPaymentAmountRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpGovUk.journeyAfterUpfrontPaymentAmount

      /** Update ExtremeDates */
      journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.SimpGovUk.updateExtremeDatesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpGovUk.journeyAfterExtremeDates

      /** Update AffordabilityResult */
      journeyConnector
        .updateAffordabilityResult(tdAll.journeyId, tdAll.SimpGovUk.updateInstalmentAmountsRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpGovUk.journeyAfterInstalmentAmounts

      /** Update CanPayWithinSixMonths */
      journeyConnector
        .updateCanPayWithinSixMonthsAnswers(tdAll.journeyId, tdAll.canPayWithinSixMonthsNotRequired)
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SimpGovUk.journeyAfterCanPayWithinSixMonths

      /** Update MonthlyPaymentAmount */
      journeyConnector
        .updateMonthlyPaymentAmount(tdAll.journeyId, tdAll.SimpGovUk.updateMonthlyPaymentAmountRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpGovUk.journeyAfterMonthlyPaymentAmount

      /** Update DayOfMonth */
      journeyConnector.updateDayOfMonth(tdAll.journeyId, tdAll.SimpGovUk.updateDayOfMonthRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpGovUk.journeyAfterDayOfMonth

      /** Update StartDates */
      journeyConnector.updateStartDates(tdAll.journeyId, tdAll.SimpGovUk.updateStartDatesResponse()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpGovUk.journeyAfterStartDatesResponse

      /** Update AffordableQuotes */
      journeyConnector
        .updateAffordableQuotes(tdAll.journeyId, tdAll.SimpGovUk.updateAffordableQuotesResponse())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SimpGovUk.journeyAfterAffordableQuotesResponse

      /** Update Chosen Instalment plan */
      journeyConnector
        .updateChosenPaymentPlan(tdAll.journeyId, tdAll.SimpGovUk.updateSelectedPaymentPlanRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpGovUk.journeyAfterSelectedPaymentPlan

      /** Update Checked Instalment plan */
      journeyConnector.updateHasCheckedPaymentPlan(tdAll.journeyId, tdAll.paymentPlanAnswersNoAffordability).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SimpGovUk.journeyAfterCheckedPaymentPlanNonAffordability

      /** Update Details about Bank Account */
      journeyConnector
        .updateCanSetUpDirectDebit(
          tdAll.journeyId,
          tdAll.SimpGovUk.updateCanSetUpDirectDebitRequest(isAccountHolder = true)
        )
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpGovUk
        .journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true)

      /** Update Direct debit details */
      journeyConnector
        .updateDirectDebitDetails(tdAll.journeyId, tdAll.SimpGovUk.updateDirectDebitDetailsRequest()())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpGovUk
        .journeyAfterEnteredDirectDebitDetailsNoAffordability()

      /** Update Confirm Direct debit details */
      journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SimpGovUk.journeyAfterConfirmedDirectDebitDetailsNoAffordability

      /** Update Agreed terms and conditions */
      journeyConnector
        .updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(value = true))
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpGovUk
        .journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true)

      /** Update Email Address */
      journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.email).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpGovUk.journeyAfterSelectedEmail

      /** Update Email Verification Status */
      journeyConnector.updateEmailVerificationResult(tdAll.journeyId, EmailVerificationResult.Verified).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpGovUk
        .journeyAfterEmailVerificationResult(EmailVerificationResult.Verified)

      /** Update Arrangement (journey completed) */
      journeyConnector.updateArrangement(tdAll.journeyId, tdAll.SimpGovUk.updateArrangementRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpGovUk
        .journeyAfterSubmittedArrangementNoAffordability(isEmailAddressRequired = true)

      verifyCommonActions(numberOfAuthCalls = 44)
    }

    s"[DetachedUrl]$simpTestNameJourneyStages" in {
      stubCommonActions()
      val tdAll                = new TdAll {
        override val journeyId: JourneyId         = journeyIdGenerator.readNextJourneyId()
        override val correlationId: CorrelationId = correlationIdGenerator.readNextCorrelationId()
      }
      given Request[_]         = tdAll.request
      val response: SjResponse =
        journeyConnector.Simp.startJourneyDetachedUrl(tdAll.SimpDetachedUrl.sjRequest).futureValue

      /** Start journey */
      response shouldBe tdAll.SimpDetachedUrl.sjResponse
      journeyConnector.getJourney(response.journeyId).futureValue shouldBe tdAll.SimpDetachedUrl.journeyAfterStarted

      /** Update tax id */
      journeyConnector.updateTaxId(tdAll.journeyId, tdAll.SimpDetachedUrl.updateTaxIdRequest()).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SimpDetachedUrl.journeyAfterDetermineTaxIds

      /** Update eligibility result * */
      journeyConnector
        .updateEligibilityCheckResult(tdAll.journeyId, tdAll.SimpDetachedUrl.updateEligibilityCheckRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SimpDetachedUrl.journeyAfterEligibilityCheckEligible

      /** Update why cannot pay in full */
      journeyConnector.updateWhyCannotPayInFullAnswers(tdAll.journeyId, tdAll.whyCannotPayInFullNotRequired).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SimpDetachedUrl.journeyAfterWhyCannotPayInFullNotRequired

      /** Update CanPayUpfront */
      journeyConnector
        .updateCanPayUpfront(tdAll.journeyId, tdAll.SimpDetachedUrl.updateCanPayUpfrontYesRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SimpDetachedUrl.journeyAfterCanPayUpfrontYes

      /** Update UpfrontPaymentAmount */
      journeyConnector
        .updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.SimpDetachedUrl.updateUpfrontPaymentAmountRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SimpDetachedUrl.journeyAfterUpfrontPaymentAmount

      /** Update ExtremeDates */
      journeyConnector
        .updateExtremeDates(tdAll.journeyId, tdAll.SimpDetachedUrl.updateExtremeDatesRequest())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpDetachedUrl.journeyAfterExtremeDates

      /** Update AffordabilityResult */
      journeyConnector
        .updateAffordabilityResult(tdAll.journeyId, tdAll.SimpDetachedUrl.updateInstalmentAmountsRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SimpDetachedUrl.journeyAfterInstalmentAmounts

      /** Update CanPayWithinSixMonths */
      journeyConnector
        .updateCanPayWithinSixMonthsAnswers(tdAll.journeyId, tdAll.canPayWithinSixMonthsNotRequired)
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SimpDetachedUrl.journeyAfterCanPayWithinSixMonths

      /** Update MonthlyPaymentAmount */
      journeyConnector
        .updateMonthlyPaymentAmount(tdAll.journeyId, tdAll.SimpDetachedUrl.updateMonthlyPaymentAmountRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SimpDetachedUrl.journeyAfterMonthlyPaymentAmount

      /** Update DayOfMonth */
      journeyConnector.updateDayOfMonth(tdAll.journeyId, tdAll.SimpDetachedUrl.updateDayOfMonthRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpDetachedUrl.journeyAfterDayOfMonth

      /** Update StartDates */
      journeyConnector.updateStartDates(tdAll.journeyId, tdAll.SimpDetachedUrl.updateStartDatesResponse()).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SimpDetachedUrl.journeyAfterStartDatesResponse

      /** Update AffordableQuotes */
      journeyConnector
        .updateAffordableQuotes(tdAll.journeyId, tdAll.SimpDetachedUrl.updateAffordableQuotesResponse())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SimpDetachedUrl.journeyAfterAffordableQuotesResponse

      /** Update Chosen Instalment plan */
      journeyConnector
        .updateChosenPaymentPlan(tdAll.journeyId, tdAll.SimpDetachedUrl.updateSelectedPaymentPlanRequest())
        .futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SimpDetachedUrl.journeyAfterSelectedPaymentPlan

      /** Update Checked Instalment plan */
      journeyConnector.updateHasCheckedPaymentPlan(tdAll.journeyId, tdAll.paymentPlanAnswersNoAffordability).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SimpDetachedUrl.journeyAfterCheckedPaymentPlanNonAffordability

      /** Update Details about Bank Account */
      journeyConnector
        .updateCanSetUpDirectDebit(
          tdAll.journeyId,
          tdAll.SimpDetachedUrl.updateCanSetUpDirectDebitRequest(isAccountHolder = true)
        )
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpDetachedUrl
        .journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true)

      /** Update Direct debit details */
      journeyConnector
        .updateDirectDebitDetails(tdAll.journeyId, tdAll.SimpDetachedUrl.updateDirectDebitDetailsRequest()())
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpDetachedUrl
        .journeyAfterEnteredDirectDebitDetailsNoAffordability()

      /** Update Confirm Direct debit details */
      journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
      journeyConnector
        .getJourney(tdAll.journeyId)
        .futureValue shouldBe tdAll.SimpDetachedUrl.journeyAfterConfirmedDirectDebitDetailsNoAffordability

      /** Update Agreed terms and conditions */
      journeyConnector
        .updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(value = true))
        .futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpDetachedUrl
        .journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true)

      /** Update Email Address */
      journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.email).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpDetachedUrl.journeyAfterSelectedEmail

      /** Update Email Verification Status */
      journeyConnector.updateEmailVerificationResult(tdAll.journeyId, EmailVerificationResult.Verified).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpDetachedUrl
        .journeyAfterEmailVerificationResult(EmailVerificationResult.Verified)

      /** Update Arrangement (journey completed) */
      journeyConnector.updateArrangement(tdAll.journeyId, tdAll.SimpDetachedUrl.updateArrangementRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SimpDetachedUrl
        .journeyAfterSubmittedArrangementNoAffordability(isEmailAddressRequired = true)

      verifyCommonActions(numberOfAuthCalls = 44)
    }
  }

}

object JourneyControllerAffordabilityEnabledSpec {

  class TestAffordabilityEnablerService extends AffordabilityEnablerService {

    @SuppressWarnings(Array("org.wartremover.warts.Var")) // var will only work in single threaded run
    private var enabled: Map[TaxRegime, Boolean] = TaxRegime.values.map(_ -> false).toMap

    def enable(taxRegime: TaxRegime): Unit = enabled = enabled.updated(taxRegime, true)

    def disable(taxRegime: TaxRegime): Unit = enabled = enabled.updated(taxRegime, false)

    override def affordabilityEnabled(taxRegime: TaxRegime): Boolean =
      enabled(taxRegime)
  }

}

class JourneyControllerAffordabilityEnabledSpec extends ItSpec {

  val testAffordabilityEnablerService = new TestAffordabilityEnablerService

  override lazy val overrideBindings: Seq[GuiceableModule] =
    Seq[GuiceableModule](
      bind[AffordabilityEnablerService].toInstance(testAffordabilityEnablerService)
    )

  def journeyConnector: JourneyConnector = app.injector.instanceOf[JourneyConnector]

  "The journey must be able to be updated when affordability is enabled for a tax regime" in {
    testAffordabilityEnablerService.enable(TaxRegime.Sa)

    stubCommonActions()
    val tdAll                = new TdAll {
      override val journeyId: JourneyId         = journeyIdGenerator.readNextJourneyId()
      override val correlationId: CorrelationId = correlationIdGenerator.readNextCorrelationId()
    }
    given Request[_]         = tdAll.request
    val response: SjResponse = journeyConnector.Sa.startJourneyBta(tdAll.SaBta.sjRequest).futureValue

    /** Start journey */
    response shouldBe tdAll.SaBta.sjResponse
    journeyConnector.getJourney(response.journeyId).futureValue shouldBe tdAll.SaBta.journeyAfterStarted
      .copy(affordabilityEnabled = Some(true))

    /** Update tax id */
    journeyConnector.updateTaxId(tdAll.journeyId, tdAll.SaBta.updateTaxIdRequest()).futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta.journeyAfterDetermineTaxIds
      .copy(affordabilityEnabled = Some(true))

    /** Update eligibility result * */
    journeyConnector
      .updateEligibilityCheckResult(tdAll.journeyId, tdAll.SaBta.updateEligibilityCheckRequest())
      .futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta.journeyAfterEligibilityCheckEligible
      .copy(affordabilityEnabled = Some(true))

    /** Update why cannot pay in full */
    journeyConnector.updateWhyCannotPayInFullAnswers(tdAll.journeyId, tdAll.whyCannotPayInFullRequired).futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta.journeyAfterWhyCannotPayInFullRequired
      .copy(affordabilityEnabled = Some(true))

    /** Update CanPayUpfront */
    journeyConnector.updateCanPayUpfront(tdAll.journeyId, tdAll.SaBta.updateCanPayUpfrontYesRequest()).futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta.journeyAfterCanPayUpfrontYes
      .copy(affordabilityEnabled = Some(true), whyCannotPayInFullAnswers = tdAll.whyCannotPayInFullRequired)

    /** Update UpfrontPaymentAmount */
    journeyConnector
      .updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.SaBta.updateUpfrontPaymentAmountRequest())
      .futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta.journeyAfterUpfrontPaymentAmount
      .copy(affordabilityEnabled = Some(true), whyCannotPayInFullAnswers = tdAll.whyCannotPayInFullRequired)

    /** Update ExtremeDates */
    journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.SaBta.updateExtremeDatesRequest()).futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta.journeyAfterExtremeDates
      .copy(affordabilityEnabled = Some(true), whyCannotPayInFullAnswers = tdAll.whyCannotPayInFullRequired)

    /** Update AffordabilityResult */
    journeyConnector
      .updateAffordabilityResult(tdAll.journeyId, tdAll.SaBta.updateInstalmentAmountsRequest())
      .futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta.journeyAfterInstalmentAmounts
      .copy(affordabilityEnabled = Some(true), whyCannotPayInFullAnswers = tdAll.whyCannotPayInFullRequired)

    /** Update CanPayWithinSixMonths */
    journeyConnector.updateCanPayWithinSixMonthsAnswers(tdAll.journeyId, tdAll.canPayWithinSixMonthsNo).futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta.journeyAfterCanPayWithinSixMonths
      .copy(
        affordabilityEnabled = Some(true),
        whyCannotPayInFullAnswers = tdAll.whyCannotPayInFullRequired,
        canPayWithinSixMonthsAnswers = tdAll.canPayWithinSixMonthsNo
      )

    /** Update StartCaseResponse */
    journeyConnector.updatePegaStartCaseResponse(tdAll.journeyId, tdAll.startCaseResponse).futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta.journeyAfterStartedPegaCase
      .copy(
        affordabilityEnabled = Some(true),
        whyCannotPayInFullAnswers = tdAll.whyCannotPayInFullRequired,
        canPayWithinSixMonthsAnswers = tdAll.canPayWithinSixMonthsNo
      )

    /** Update Checked Instalment plan */
    journeyConnector.updateHasCheckedPaymentPlan(tdAll.journeyId, tdAll.paymentPlanAnswersWithAffordability).futureValue
    journeyConnector
      .getJourney(tdAll.journeyId)
      .futureValue shouldBe tdAll.SaBta.journeyAfterCheckedPaymentPlanWithAffordability
      .copy(
        affordabilityEnabled = Some(true),
        whyCannotPayInFullAnswers = tdAll.whyCannotPayInFullRequired,
        canPayWithinSixMonthsAnswers = tdAll.canPayWithinSixMonthsNo
      )

    /** Update Details about Bank Account */
    journeyConnector
      .updateCanSetUpDirectDebit(tdAll.journeyId, tdAll.SaBta.updateCanSetUpDirectDebitRequest(isAccountHolder = true))
      .futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta
      .journeyAfterEnteredCanYouSetUpDirectDebitWithAffordability(isAccountHolder = true)
      .copy(
        affordabilityEnabled = Some(true),
        whyCannotPayInFullAnswers = tdAll.whyCannotPayInFullRequired,
        canPayWithinSixMonthsAnswers = tdAll.canPayWithinSixMonthsNo
      )

    /** Update Direct debit details */
    journeyConnector
      .updateDirectDebitDetails(tdAll.journeyId, tdAll.SaBta.updateDirectDebitDetailsRequest())
      .futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta
      .journeyAfterEnteredDirectDebitDetailsWithAffordability()
      .copy(
        affordabilityEnabled = Some(true),
        whyCannotPayInFullAnswers = tdAll.whyCannotPayInFullRequired,
        canPayWithinSixMonthsAnswers = tdAll.canPayWithinSixMonthsNo
      )

    /** Update Confirm Direct debit details */
    journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
    journeyConnector
      .getJourney(tdAll.journeyId)
      .futureValue shouldBe tdAll.SaBta.journeyAfterConfirmedDirectDebitDetailsWithAffordability
      .copy(
        affordabilityEnabled = Some(true),
        whyCannotPayInFullAnswers = tdAll.whyCannotPayInFullRequired,
        canPayWithinSixMonthsAnswers = tdAll.canPayWithinSixMonthsNo
      )

    /** Update Agreed terms and conditions */
    journeyConnector
      .updateHasAgreedTermsAndConditions(tdAll.journeyId, IsEmailAddressRequired(value = true))
      .futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta
      .journeyAfterAgreedTermsAndConditionsWithAffordability(isEmailAddressRequired = true)
      .copy(
        affordabilityEnabled = Some(true),
        whyCannotPayInFullAnswers = tdAll.whyCannotPayInFullRequired,
        canPayWithinSixMonthsAnswers = tdAll.canPayWithinSixMonthsNo
      )

    /** Update Email Address */
    journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.email).futureValue
    journeyConnector
      .getJourney(tdAll.journeyId)
      .futureValue shouldBe tdAll.SaBta.journeyAfterSelectedEmailWithAffordability
      .copy(
        affordabilityEnabled = Some(true),
        whyCannotPayInFullAnswers = tdAll.whyCannotPayInFullRequired,
        canPayWithinSixMonthsAnswers = tdAll.canPayWithinSixMonthsNo
      )

    /** Update Email Verification Status */
    journeyConnector.updateEmailVerificationResult(tdAll.journeyId, EmailVerificationResult.Verified).futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta
      .journeyAfterEmailVerificationResultWithAffordability(EmailVerificationResult.Verified)
      .copy(
        affordabilityEnabled = Some(true),
        whyCannotPayInFullAnswers = tdAll.whyCannotPayInFullRequired,
        canPayWithinSixMonthsAnswers = tdAll.canPayWithinSixMonthsNo
      )

    /** Update Arrangement (journey completed) */
    journeyConnector.updateArrangement(tdAll.journeyId, tdAll.SaBta.updateArrangementRequest()).futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.SaBta
      .journeyAfterSubmittedArrangementWithAffordability(isEmailAddressRequired = true)
      .copy(
        affordabilityEnabled = Some(true),
        whyCannotPayInFullAnswers = tdAll.whyCannotPayInFullRequired,
        canPayWithinSixMonthsAnswers = tdAll.canPayWithinSixMonthsNo
      )

    verifyCommonActions(numberOfAuthCalls = 36)
  }

  "A journey must have affordability disabled if the AffordabilityEnablerService disables it" in {
    testAffordabilityEnablerService.disable(TaxRegime.Sa)

    stubCommonActions()
    val tdAll                = new TdAll {
      override val journeyId: JourneyId         = journeyIdGenerator.readNextJourneyId()
      override val correlationId: CorrelationId = correlationIdGenerator.readNextCorrelationId()
    }
    given Request[_]         = tdAll.request
    val response: SjResponse = journeyConnector.Sa.startJourneyBta(tdAll.SaBta.sjRequest).futureValue

    /** Start journey */
    response shouldBe tdAll.SaBta.sjResponse
    journeyConnector.getJourney(response.journeyId).futureValue shouldBe tdAll.SaBta.journeyAfterStarted
      .copy(affordabilityEnabled = Some(false))
  }

}
