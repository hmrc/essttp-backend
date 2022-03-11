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

package journey.model

import enumeratum._
import julienrf.json.derived
import play.api.libs.json.OFormat

/**
 * Journey Stage
 * It defines how journey propagates through stages.
 * Each stage defines what data are available in journey at this stage.
 * Each enum value defines what states journey can be in within this stage.
 */
sealed trait Stage

object Stage {

  sealed trait AfterStarted extends Stage with EnumEntry

  object AfterStarted extends Enum[AfterStarted] {
    implicit val format: OFormat[AfterStarted] = derived.oformat[AfterStarted]()
    val values = findValues

    /**
     * Journey has been just started.
     * It's new bare bone journey having nothing but origin,
     * tax regime and [[SjRequest]] which caused it.
     */
    case object New extends AfterStarted
  }

  sealed trait AfterEligibilityCheck extends Stage with EnumEntry

  /**
   * [[Journey]] has been orchestrated with tax identifiers from Enrolments
   * and eligibility check has been made.
   */
  object AfterEligibilityCheck extends Enum[AfterEligibilityCheck] {
    implicit val format: OFormat[AfterEligibilityCheck] = derived.oformat[AfterEligibilityCheck]()
    val values = findValues

    case object Eligible extends AfterEligibilityCheck

    case object Ineligible extends AfterEligibilityCheck
  }

  sealed trait AfterEnteredDayOfMonth extends Stage with EnumEntry

  /**
   * [[Journey]] has been orchestrated with day of month
   */
  object AfterEnteredDayOfMonth extends Enum[AfterEnteredDayOfMonth] {
    implicit val format: OFormat[AfterEnteredDayOfMonth] = derived.oformat[AfterEnteredDayOfMonth]()
    val values = findValues

    case object EnteredDayOfMonth extends AfterEnteredDayOfMonth
  }

  sealed trait AfterEnteredAmount extends Stage with EnumEntry

  /**
   * [[Journey]] has been orchestrated with affordable amount and available plans.
   */
  object AfterEnteredAmount extends Enum[AfterEnteredAmount] {
    implicit val format: OFormat[AfterEnteredAmount] = derived.oformat[AfterEnteredAmount]()
    val values = findValues

    case object EnteredAmount extends AfterEnteredAmount
  }

  sealed trait AfterSelectedPlan extends Stage with EnumEntry

  /**
   * [[Journey]] has been orchestrated with affordable amount
   */
  object AfterSelectedPlan extends Enum[AfterSelectedPlan] {
    implicit val format: OFormat[AfterSelectedPlan] = derived.oformat[AfterSelectedPlan]()
    val values = findValues

    case object SelectedPlan extends AfterSelectedPlan

    /**
     * Journey has been finished. Now it's possible to make only Survey but no go back.
     */
    case object SubmittedDirectDebit extends AfterSelectedPlan

    /**
     * Journey has been finished and survey is completed. User should not re-take the survey.
     */
    case object SurveyComplete extends AfterSelectedPlan
  }

}