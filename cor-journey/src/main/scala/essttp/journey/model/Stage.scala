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

package essttp.journey.model

import enumeratum._
import julienrf.json.derived
import play.api.libs.json.OFormat

import scala.collection.immutable

/**
 * Journey Stage
 * It defines how journey propagates through stages.
 * Each stage defines what data are available in journey at this stage.
 * Each enum value defines what states journey can be in within this stage.
 */
sealed trait Stage extends Product with Serializable

object Stage {

  sealed trait AfterStarted extends Stage with EnumEntry

  object AfterStarted extends Enum[AfterStarted] {
    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    implicit val format: OFormat[AfterStarted] = derived.oformat[AfterStarted]()
    val values: immutable.IndexedSeq[AfterStarted] = findValues

    /**
     * Journey has been just started.
     * It's new bare bone journey having nothing but origin,
     * tax regime and [[SjRequest]] which caused it.
     */
    case object Started extends AfterStarted
  }

  sealed trait AfterComputedTaxId extends Stage with EnumEntry

  object AfterComputedTaxId extends Enum[AfterComputedTaxId] {
    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    implicit val format: OFormat[AfterComputedTaxId] = derived.oformat[AfterComputedTaxId]()
    val values: immutable.IndexedSeq[AfterComputedTaxId] = findValues

    /**
     * [[Journey]] has been orchestrated with tax identifiers from Enrolments.
     */
    case object ComputedTaxId extends AfterComputedTaxId
  }

  sealed trait AfterEligibilityCheck extends Stage with EnumEntry

  /**
   * [[Journey]] has been orchestrated with eligibility check result.
   */
  object AfterEligibilityCheck extends Enum[AfterEligibilityCheck] {
    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    implicit val format: OFormat[AfterEligibilityCheck] = derived.oformat[AfterEligibilityCheck]()
    val values: immutable.IndexedSeq[AfterEligibilityCheck] = findValues

    case object Eligible extends AfterEligibilityCheck

    case object Ineligible extends AfterEligibilityCheck
  }

  sealed trait AfterWhyCannotPayInFullAnswers extends Stage with EnumEntry

  /**
   * [[Journey]] has been orchestrated with reasons why user cannot pay in ful (if needed)
   */
  object AfterWhyCannotPayInFullAnswers extends Enum[AfterWhyCannotPayInFullAnswers] {
    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    implicit val format: OFormat[AfterWhyCannotPayInFullAnswers] = derived.oformat[AfterWhyCannotPayInFullAnswers]()
    val values: immutable.IndexedSeq[AfterWhyCannotPayInFullAnswers] = findValues

    case object AnswerRequired extends AfterWhyCannotPayInFullAnswers

    case object AnswerNotRequired extends AfterWhyCannotPayInFullAnswers
  }

  sealed trait AfterCanPayUpfront extends Stage with EnumEntry

  /**
   * [[Journey]] has been orchestrated with can user make an upfront payment.
   */
  object AfterCanPayUpfront extends Enum[AfterCanPayUpfront] {
    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    implicit val format: OFormat[AfterCanPayUpfront] = derived.oformat[AfterCanPayUpfront]()
    val values: immutable.IndexedSeq[AfterCanPayUpfront] = findValues

    case object Yes extends AfterCanPayUpfront

    case object No extends AfterCanPayUpfront
  }

  sealed trait AfterUpfrontPaymentAmount extends Stage with EnumEntry

  object AfterUpfrontPaymentAmount extends Enum[AfterUpfrontPaymentAmount] {
    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    implicit val format: OFormat[AfterUpfrontPaymentAmount] = derived.oformat[AfterUpfrontPaymentAmount]()
    val values: immutable.IndexedSeq[AfterUpfrontPaymentAmount] = findValues

    /**
     * [[Journey]] has been orchestrated with Upfront payment amount.
     */
    case object EnteredUpfrontPaymentAmount extends AfterUpfrontPaymentAmount
  }

  sealed trait AfterUpfrontPaymentAnswers extends Stage with EnumEntry

  object AfterUpfrontPaymentAnswers extends Enum[AfterUpfrontPaymentAnswers] {
    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    implicit val format: OFormat[AfterUpfrontPaymentAnswers] = derived.oformat[AfterUpfrontPaymentAnswers]()
    val values: immutable.IndexedSeq[AfterUpfrontPaymentAnswers] = findValues

    /**
     * [[Journey]] has been orchestrated with Upfront payment answers.
     */
    case object SubmittedUpfrontPaymentAnswers extends AfterUpfrontPaymentAnswers
  }

  sealed trait AfterExtremeDatesResponse extends Stage with EnumEntry

  object AfterExtremeDatesResponse extends Enum[AfterExtremeDatesResponse] {
    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    implicit val format: OFormat[AfterExtremeDatesResponse] = derived.oformat[AfterExtremeDatesResponse]()
    val values: immutable.IndexedSeq[AfterExtremeDatesResponse] = findValues

    /**
     * [[Journey]] has been orchestrated with Extreme dates response.
     */
    case object ExtremeDatesResponseRetrieved extends AfterExtremeDatesResponse
  }

  sealed trait AfterAffordabilityResult extends Stage with EnumEntry

  object AfterAffordabilityResult extends Enum[AfterAffordabilityResult] {
    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    implicit val format: OFormat[AfterAffordabilityResult] = derived.oformat[AfterAffordabilityResult]()
    val values: immutable.IndexedSeq[AfterAffordabilityResult] = findValues

    /**
     * [[Journey]] has been orchestrated with Affordability result.
     */
    case object RetrievedAffordabilityResult extends AfterAffordabilityResult
  }

  sealed trait AfterCanPayWithinSixMonthsAnswers extends Stage with EnumEntry

  object AfterCanPayWithinSixMonthsAnswers extends Enum[AfterCanPayWithinSixMonthsAnswers] {
    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    implicit val format: OFormat[AfterCanPayWithinSixMonthsAnswers] = derived.oformat[AfterCanPayWithinSixMonthsAnswers]()
    val values: immutable.IndexedSeq[AfterCanPayWithinSixMonthsAnswers] = findValues

    case object AnswerRequired extends AfterCanPayWithinSixMonthsAnswers

    case object AnswerNotRequired extends AfterCanPayWithinSixMonthsAnswers
  }

  sealed trait AfterMonthlyPaymentAmount extends Stage with EnumEntry

  object AfterMonthlyPaymentAmount extends Enum[AfterMonthlyPaymentAmount] {
    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    implicit val format: OFormat[AfterMonthlyPaymentAmount] = derived.oformat[AfterMonthlyPaymentAmount]()
    val values: immutable.IndexedSeq[AfterMonthlyPaymentAmount] = findValues

    /**
     * [[Journey]] has been orchestrated with Monthly payment amount.
     */
    case object EnteredMonthlyPaymentAmount extends AfterMonthlyPaymentAmount
  }

  sealed trait AfterEnteredDayOfMonth extends Stage with EnumEntry

  /**
   * [[Journey]] has been orchestrated with day of month
   */
  object AfterEnteredDayOfMonth extends Enum[AfterEnteredDayOfMonth] {
    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    implicit val format: OFormat[AfterEnteredDayOfMonth] = derived.oformat[AfterEnteredDayOfMonth]()
    val values: immutable.IndexedSeq[AfterEnteredDayOfMonth] = findValues

    case object EnteredDayOfMonth extends AfterEnteredDayOfMonth
  }

  sealed trait AfterStartDatesResponse extends Stage with EnumEntry

  /**
   * [[Journey]] has been orchestrated with start dates api call
   */
  object AfterStartDatesResponse extends Enum[AfterStartDatesResponse] {
    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    implicit val format: OFormat[AfterStartDatesResponse] = derived.oformat[AfterStartDatesResponse]()
    val values: immutable.IndexedSeq[AfterStartDatesResponse] = findValues

    case object StartDatesResponseRetrieved extends AfterStartDatesResponse
  }

  sealed trait AfterAffordableQuotesResponse extends Stage with EnumEntry

  /**
   * [[Journey]] has been orchestrated with affordable quotes api call from ttp
   */
  object AfterAffordableQuotesResponse extends Enum[AfterAffordableQuotesResponse] {
    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    implicit val format: OFormat[AfterAffordableQuotesResponse] = derived.oformat[AfterAffordableQuotesResponse]()
    val values: immutable.IndexedSeq[AfterAffordableQuotesResponse] = findValues

    case object AffordableQuotesRetrieved extends AfterAffordableQuotesResponse
  }

  sealed trait AfterSelectedPlan extends Stage with EnumEntry

  /**
   * [[Journey]] has been orchestrated with selected instalment plan
   */
  object AfterSelectedPlan extends Enum[AfterSelectedPlan] {
    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    implicit val format: OFormat[AfterSelectedPlan] = derived.oformat[AfterSelectedPlan]()
    val values: immutable.IndexedSeq[AfterSelectedPlan] = findValues

    case object SelectedPlan extends AfterSelectedPlan
  }

  sealed trait AfterCheckedPlan extends Stage with EnumEntry

  /**
   * [[Journey]] has been orchestrated to indicate the user has checked and accepted the a payment plan
   */
  object AfterCheckedPlan extends Enum[AfterCheckedPlan] {
    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    implicit val format: OFormat[AfterCheckedPlan] = derived.oformat[AfterCheckedPlan]()
    val values: immutable.IndexedSeq[AfterCheckedPlan] = findValues

    case object AcceptedPlan extends AfterCheckedPlan
  }

  sealed trait AfterEnteredDetailsAboutBankAccount extends Stage with EnumEntry

  /**
   * [[Journey]] has been orchestrated with details about the user's bank account
   */
  object AfterEnteredDetailsAboutBankAccount extends Enum[AfterEnteredDetailsAboutBankAccount] {
    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    implicit val format: OFormat[AfterEnteredDetailsAboutBankAccount] = derived.oformat[AfterEnteredDetailsAboutBankAccount]()
    val values: immutable.IndexedSeq[AfterEnteredDetailsAboutBankAccount] = findValues

    case object Business extends AfterEnteredDetailsAboutBankAccount

    case object Personal extends AfterEnteredDetailsAboutBankAccount

    case object IsNotAccountHolder extends AfterEnteredDetailsAboutBankAccount
  }

  sealed trait AfterEnteredDirectDebitDetails extends Stage with EnumEntry

  /**
   * [[Journey]] has been orchestrated to indicate the user has checked and accepted the a payment plan
   */
  object AfterEnteredDirectDebitDetails extends Enum[AfterEnteredDirectDebitDetails] {
    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    implicit val format: OFormat[AfterEnteredDirectDebitDetails] = derived.oformat[AfterEnteredDirectDebitDetails]()
    val values: immutable.IndexedSeq[AfterEnteredDirectDebitDetails] = findValues

    case object EnteredDirectDebitDetails extends AfterEnteredDirectDebitDetails

  }

  sealed trait AfterConfirmedDirectDebitDetails extends Stage with EnumEntry

  /**
   * [[Journey]] has been orchestrated to indicate the user has checked and confirmed their direct debit details
   */
  object AfterConfirmedDirectDebitDetails extends Enum[AfterConfirmedDirectDebitDetails] {
    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    implicit val format: OFormat[AfterConfirmedDirectDebitDetails] = derived.oformat[AfterConfirmedDirectDebitDetails]()
    val values: immutable.IndexedSeq[AfterConfirmedDirectDebitDetails] = findValues

    case object ConfirmedDetails extends AfterConfirmedDirectDebitDetails
  }

  sealed trait AfterAgreedTermsAndConditions extends Stage with EnumEntry

  /**
   * [[Journey]] has been orchestrated to indicate the user has agreed to terms and conditions
   */
  object AfterAgreedTermsAndConditions extends Enum[AfterAgreedTermsAndConditions] {
    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    implicit val format: OFormat[AfterAgreedTermsAndConditions] = derived.oformat[AfterAgreedTermsAndConditions]()
    val values: immutable.IndexedSeq[AfterAgreedTermsAndConditions] = findValues

    case object EmailAddressRequired extends AfterAgreedTermsAndConditions

    case object EmailAddressNotRequired extends AfterAgreedTermsAndConditions

  }

  sealed trait AfterSelectedAnEmailToBeVerified extends Stage with EnumEntry

  /**
   * [[Journey]] has been orchestrated to indicate the user has selected an email to verify
   */
  object AfterSelectedAnEmailToBeVerified extends Enum[AfterSelectedAnEmailToBeVerified] {
    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    implicit val format: OFormat[AfterSelectedAnEmailToBeVerified] = derived.oformat[AfterSelectedAnEmailToBeVerified]()
    val values: immutable.IndexedSeq[AfterSelectedAnEmailToBeVerified] = findValues

    case object EmailChosen extends AfterSelectedAnEmailToBeVerified
  }

  sealed trait AfterEmailVerificationPhase extends Stage with EnumEntry

  /**
   * [[Journey]] has been orchestrated to indicate the user has gone through email verification
   */
  object AfterEmailVerificationPhase extends Enum[AfterEmailVerificationPhase] {
    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    implicit val format: OFormat[AfterEmailVerificationPhase] = derived.oformat[AfterEmailVerificationPhase]()
    val values: immutable.IndexedSeq[AfterEmailVerificationPhase] = findValues

    case object VerificationSuccess extends AfterEmailVerificationPhase

    case object Locked extends AfterEmailVerificationPhase
  }

  sealed trait AfterSubmittedArrangement extends Stage with EnumEntry

  /**
   * [[Journey]] has been orchestrated to indicate the user has submitted their arrangement to ttp api
   */
  object AfterSubmittedArrangement extends Enum[AfterSubmittedArrangement] {
    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    implicit val format: OFormat[AfterSubmittedArrangement] = derived.oformat[AfterSubmittedArrangement]()
    val values: immutable.IndexedSeq[AfterSubmittedArrangement] = findValues

    case object Submitted extends AfterSubmittedArrangement
  }

}
