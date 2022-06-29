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
    implicit val format: OFormat[AfterEligibilityCheck] = derived.oformat[AfterEligibilityCheck]()
    val values: immutable.IndexedSeq[AfterEligibilityCheck] = findValues

    case object Eligible extends AfterEligibilityCheck

    case object Ineligible extends AfterEligibilityCheck
  }

  sealed trait AfterCanPayUpfront extends Stage with EnumEntry

  /**
   * [[Journey]] has been orchestrated with can user make an upfront payment.
   */
  object AfterCanPayUpfront extends Enum[AfterCanPayUpfront] {
    implicit val format: OFormat[AfterCanPayUpfront] = derived.oformat[AfterCanPayUpfront]()
    val values: immutable.IndexedSeq[AfterCanPayUpfront] = findValues

    case object Yes extends AfterCanPayUpfront

    case object No extends AfterCanPayUpfront
  }

  sealed trait AfterUpfrontPaymentAmount extends Stage with EnumEntry

  object AfterUpfrontPaymentAmount extends Enum[AfterUpfrontPaymentAmount] {
    implicit val format: OFormat[AfterUpfrontPaymentAmount] = derived.oformat[AfterUpfrontPaymentAmount]()
    val values: immutable.IndexedSeq[AfterUpfrontPaymentAmount] = findValues

    /**
     * [[Journey]] has been orchestrated with Upfront payment amount.
     */
    case object EnteredUpfrontPaymentAmount extends AfterUpfrontPaymentAmount
  }

  sealed trait AfterUpfrontPaymentAnswers extends Stage with EnumEntry

  object AfterUpfrontPaymentAnswers extends Enum[AfterUpfrontPaymentAnswers] {
    implicit val format: OFormat[AfterUpfrontPaymentAnswers] = derived.oformat[AfterUpfrontPaymentAnswers]()
    val values: immutable.IndexedSeq[AfterUpfrontPaymentAnswers] = findValues

    /**
     * [[Journey]] has been orchestrated with Upfront payment answers.
     */
    case object SubmittedUpfrontPaymentAnswers extends AfterUpfrontPaymentAnswers
  }

  sealed trait AfterExtremeDatesResponse extends Stage with EnumEntry

  object AfterExtremeDatesResponse extends Enum[AfterExtremeDatesResponse] {
    implicit val format: OFormat[AfterExtremeDatesResponse] = derived.oformat[AfterExtremeDatesResponse]()
    val values: immutable.IndexedSeq[AfterExtremeDatesResponse] = findValues

    /**
     * [[Journey]] has been orchestrated with Extreme dates response.
     */
    case object ExtremeDatesResponseRetrieved extends AfterExtremeDatesResponse
  }

  sealed trait AfterAffordabilityResult extends Stage with EnumEntry

  object AfterAffordabilityResult extends Enum[AfterAffordabilityResult] {
    implicit val format: OFormat[AfterAffordabilityResult] = derived.oformat[AfterAffordabilityResult]()
    val values: immutable.IndexedSeq[AfterAffordabilityResult] = findValues

    /**
     * [[Journey]] has been orchestrated with Affordability result.
     */
    case object RetrievedAffordabilityResult extends AfterAffordabilityResult
  }

  sealed trait AfterMonthlyPaymentAmount extends Stage with EnumEntry

  object AfterMonthlyPaymentAmount extends Enum[AfterMonthlyPaymentAmount] {
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
    implicit val format: OFormat[AfterEnteredDayOfMonth] = derived.oformat[AfterEnteredDayOfMonth]()
    val values: immutable.IndexedSeq[AfterEnteredDayOfMonth] = findValues

    case object EnteredDayOfMonth extends AfterEnteredDayOfMonth
  }

  sealed trait AfterStartDatesResponse extends Stage with EnumEntry

  /**
   * [[Journey]] has been orchestrated with start dates api call
   */
  object AfterStartDatesResponse extends Enum[AfterStartDatesResponse] {
    implicit val format: OFormat[AfterStartDatesResponse] = derived.oformat[AfterStartDatesResponse]()
    val values: immutable.IndexedSeq[AfterStartDatesResponse] = findValues

    case object StartDatesResponseRetrieved extends AfterStartDatesResponse
  }

  sealed trait AfterAffordableQuotesResponse extends Stage with EnumEntry

  /**
   * [[Journey]] has been orchestrated with affordable quotes api call from ttp
   */
  object AfterAffordableQuotesResponse extends Enum[AfterAffordableQuotesResponse] {
    implicit val format: OFormat[AfterAffordableQuotesResponse] = derived.oformat[AfterAffordableQuotesResponse]()
    val values: immutable.IndexedSeq[AfterAffordableQuotesResponse] = findValues

    case object AffordableQuotesRetrieved extends AfterAffordableQuotesResponse
  }

  sealed trait AfterSelectedPlan extends Stage with EnumEntry

  /**
   * [[Journey]] has been orchestrated with selected instalment plan
   */
  object AfterSelectedPlan extends Enum[AfterSelectedPlan] {
    implicit val format: OFormat[AfterSelectedPlan] = derived.oformat[AfterSelectedPlan]()
    val values: immutable.IndexedSeq[AfterSelectedPlan] = findValues

    case object SelectedPlan extends AfterSelectedPlan
  }

  sealed trait AfterCheckedPlan extends Stage with EnumEntry

  /**
   * [[Journey]] has been orchestrated to indicate the user has checked and accepted the a payment plan
   */
  object AfterCheckedPlan extends Enum[AfterCheckedPlan] {
    implicit val format: OFormat[AfterCheckedPlan] = derived.oformat[AfterCheckedPlan]()
    val values: immutable.IndexedSeq[AfterCheckedPlan] = findValues

    case object AcceptedPlan extends AfterCheckedPlan
  }

}
