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

import essttp.journey.model.ttp.EligibilityCheckResult
import essttp.rootmodel._
import essttp.utils.Errors
import julienrf.json.derived
import play.api.libs.json.{Json, OFormat, OWrites}

import java.time.LocalDateTime

sealed trait DdDetails

sealed trait Journey {
  def _id: JourneyId
  def origin: Origin
  def createdOn: LocalDateTime
  def sjRequest: SjRequest
  def sessionId: SessionId
  def taxRegime: TaxRegime
  def stage: Stage

  /* derived stuff: */

  def id: JourneyId = _id
  def journeyId: JourneyId = _id
  val traceId: TraceId = TraceId(journeyId)

  def name: String = {
    val className = getClass.getName
    val packageName = getClass.getPackage.getName
    className
      .replaceAllLiterally(s"$packageName.", "")
      .replaceAllLiterally("$", ".")
  }

  def backUrl: Option[BackUrl]
  def returnUrl: Option[ReturnUrl]
}
object Journey {

  implicit val format: OFormat[Journey] = {

    val defaultFormat: OFormat[Journey] = derived.oformat[Journey]()

    //we need to write some extra fields on the top of the structure so it's
    //possible to index on them and use them in queries
    val customWrites = OWrites[Journey](j =>
      defaultFormat.writes(j) ++ Json.obj(
        "sessionId" -> j.sessionId,
        "createdAt" -> j.createdOn
      ))
    OFormat(
      defaultFormat,
      customWrites
    )
  }

  sealed trait BeforeComputedTaxId extends Journey with Stages.JourneyStage

  sealed trait AfterComputedTaxId extends Journey {
    def taxId: TaxId
  }

  sealed trait BeforeEligibilityChecked extends Journey with Stages.JourneyStage

  sealed trait AfterEligibilityChecked extends Journey {
    def eligibilityCheckResult: EligibilityCheckResult
  }

  sealed trait BeforeAnsweredCanPayUpfront extends Journey with Stages.JourneyStage

  sealed trait AfterAnsweredCanPayUpfront extends Journey {
    def canPayUpfront: CanPayUpfront
  }

  sealed trait BeforeEnteredUpfrontPaymentAmount extends Journey with Stages.JourneyStage

  sealed trait AfterEnteredUpfrontPaymentAmount extends Journey {
    def upfrontPaymentAmount: UpfrontPaymentAmount
  }

  //  sealed trait AfterEnteredDayOfMonth extends Journey {
  //    def dayOfMonth: DayOfMonth
  //  }
  //
  //  sealed trait AfterEnteredInstalmentAmount extends Journey {
  //    def amount: AmountInPence
  //  }
  //
  //  sealed trait AfterSelectedPlan extends Journey {
  //    def selectedPlan: SelectedPlan
  //  }

  /**
   * Journey extractors extracting journeys in particular stage.
   * They correspond to actual [[Stage]] values
   */
  object Stages {

    /**
     * Marking trait for selecting journey in stage
     */
    sealed trait JourneyStage extends Journey {
      def stage: Stage
    }

    private val sanityMessage = "Sanity check just in case if you messed journey traits up"

    sealed trait Started
      extends Journey
      with JourneyStage
      with BeforeComputedTaxId
      with BeforeEligibilityChecked
      with BeforeAnsweredCanPayUpfront
      with BeforeEnteredUpfrontPaymentAmount {
      Errors.sanityCheck(Stage.AfterStarted.values.contains(stage), sanityMessage)
      def stage: Stage.AfterStarted
    }

    sealed trait ComputedTaxId
      extends Journey
      with JourneyStage
      with AfterComputedTaxId
      with BeforeEligibilityChecked
      with BeforeAnsweredCanPayUpfront
      with BeforeEnteredUpfrontPaymentAmount {
      Errors.sanityCheck(Stage.AfterComputedTaxId.values.contains(stage), sanityMessage)
      def stage: Stage.AfterComputedTaxId
    }

    sealed trait EligibilityChecked
      extends Journey
      with JourneyStage
      with AfterComputedTaxId
      with AfterEligibilityChecked
      with BeforeAnsweredCanPayUpfront
      with BeforeEnteredUpfrontPaymentAmount {
      Errors.sanityCheck(Stage.AfterEligibilityCheck.values.contains(stage), sanityMessage)
      def stage: Stage.AfterEligibilityCheck
    }

    sealed trait AnsweredCanPayUpfront
      extends Journey
      with JourneyStage
      with AfterComputedTaxId
      with AfterEligibilityChecked
      with AfterAnsweredCanPayUpfront
      with BeforeEnteredUpfrontPaymentAmount {
      Errors.sanityCheck(Stage.AfterCanPayUpfront.values.contains(stage), sanityMessage)
      def stage: Stage.AfterCanPayUpfront
    }

    sealed trait EnteredUpfrontPaymentAmount
      extends Journey
      with JourneyStage
      with AfterComputedTaxId
      with AfterEligibilityChecked
      with AfterAnsweredCanPayUpfront
      with AfterEnteredUpfrontPaymentAmount {
      Errors.sanityCheck(Stage.AfterUpfrontPaymentAmount.values.contains(stage), sanityMessage)
      def stage: Stage.AfterUpfrontPaymentAmount
    }

    sealed trait EnteredDayOfMonth
      extends Journey
      with JourneyStage
      with AfterComputedTaxId
      with AfterEligibilityChecked
      with AfterAnsweredCanPayUpfront
      with AfterEnteredUpfrontPaymentAmount /*with AfterEnteredDayOfMonth*/ {
      Errors.sanityCheck(Stage.AfterEnteredDayOfMonth.values.contains(stage), sanityMessage)
      def stage: Stage.AfterEnteredDayOfMonth
    }

    sealed trait EnteredInstalmentAmount
      extends Journey
      with JourneyStage
      with AfterComputedTaxId
      with AfterEligibilityChecked
      with AfterAnsweredCanPayUpfront
      with AfterEnteredUpfrontPaymentAmount /*with AfterEnteredDayOfMonth
      with AfterEnteredInstalmentAmount*/ {
      Errors.sanityCheck(Stage.AfterEnteredAmount.values.contains(stage), sanityMessage)
      def stage: Stage.AfterEnteredAmount
    }

    sealed trait HasSelectedPlan
      extends Journey
      with JourneyStage
      with AfterComputedTaxId
      with AfterEligibilityChecked
      with AfterAnsweredCanPayUpfront
      with AfterEnteredUpfrontPaymentAmount /*with AfterEnteredDayOfMonth
      with AfterEnteredInstalmentAmount
      with AfterSelectedPlan*/ {
      Errors.sanityCheck(Stage.AfterSelectedPlan.values.contains(stage), sanityMessage)
      def stage: Stage.AfterSelectedPlan

    }
  }

  /**
   * Marking trait for extracting Epaye [[Journey]]s
   */
  sealed trait Epaye extends Journey {
    override def taxRegime: TaxRegime.Epaye.type = TaxRegime.Epaye
    override def sjRequest: SjRequest.Epaye
    override def origin: Origins.Epaye

    override val (backUrl, returnUrl) = sjRequest match {
      case r: SjRequest.Epaye.Simple => (Some(r.backUrl), Some(r.returnUrl))
      case _                         => (None, None)
    }
  }

  object Epaye {

    /**
     * [[Journey]] after started
     * Epaye
     */
    final case class Started(
        override val _id:       JourneyId,
        override val origin:    Origins.Epaye,
        override val createdOn: LocalDateTime,
        override val sjRequest: SjRequest.Epaye,
        override val sessionId: SessionId,
        override val stage:     Stage.AfterStarted
    )
      extends Journey
      with Journey.Stages.Started
      with Journey.Epaye

    /**
     * [[Journey]] after computed TaxIds
     * Epaye
     */
    final case class ComputedTaxId(
        override val _id:       JourneyId,
        override val origin:    Origins.Epaye,
        override val createdOn: LocalDateTime,
        override val sjRequest: SjRequest.Epaye,
        override val sessionId: SessionId,
        override val stage:     Stage.AfterComputedTaxId,
        override val taxId:     EmpRef
    )
      extends Journey
      with Journey.Stages.ComputedTaxId
      with Journey.Epaye

    /**
     * [[Journey]] after EligibilityCheck
     * Epaye
     */
    final case class EligibilityChecked(
        override val _id:                    JourneyId,
        override val origin:                 Origins.Epaye,
        override val createdOn:              LocalDateTime,
        override val sjRequest:              SjRequest.Epaye,
        override val sessionId:              SessionId,
        override val stage:                  Stage.AfterEligibilityCheck,
        override val taxId:                  EmpRef,
        override val eligibilityCheckResult: EligibilityCheckResult
    )
      extends Journey
      with Journey.Stages.EligibilityChecked
      with Journey.Epaye

    /**
     * [[Journey]] after CanPayUpfront
     * Epaye
     */
    final case class AnsweredCanPayUpfront(
        override val _id:                    JourneyId,
        override val origin:                 Origins.Epaye,
        override val createdOn:              LocalDateTime,
        override val sjRequest:              SjRequest.Epaye,
        override val sessionId:              SessionId,
        override val stage:                  Stage.AfterCanPayUpfront,
        override val taxId:                  EmpRef,
        override val eligibilityCheckResult: EligibilityCheckResult,
        override val canPayUpfront:          CanPayUpfront
    )
      extends Journey
      with Journey.Stages.AnsweredCanPayUpfront
      with Journey.Epaye

    /**
     * [[Journey]] after UpfrontPaymentAmount
     * Epaye
     */
    final case class EnteredUpfrontPaymentAmount(
        override val _id:                    JourneyId,
        override val origin:                 Origins.Epaye,
        override val createdOn:              LocalDateTime,
        override val sjRequest:              SjRequest.Epaye,
        override val sessionId:              SessionId,
        override val stage:                  Stage.AfterUpfrontPaymentAmount,
        override val taxId:                  EmpRef,
        override val eligibilityCheckResult: EligibilityCheckResult,
        override val canPayUpfront:          CanPayUpfront,
        override val upfrontPaymentAmount:   UpfrontPaymentAmount
    )
      extends Journey
      with Journey.Stages.EnteredUpfrontPaymentAmount
      with Journey.Epaye

    /**
     * [[Journey]] after EnteredDayOfMonth
     * Epaye
     */
    final case class EnteredDayOfMonth(
        override val _id:                    JourneyId,
        override val origin:                 Origins.Epaye,
        override val createdOn:              LocalDateTime,
        override val sjRequest:              SjRequest.Epaye,
        override val sessionId:              SessionId,
        override val stage:                  Stage.AfterEnteredDayOfMonth,
        override val taxId:                  EmpRef,
        override val eligibilityCheckResult: EligibilityCheckResult,
        override val canPayUpfront:          CanPayUpfront,
        override val upfrontPaymentAmount:   UpfrontPaymentAmount //,
    //        override val dayOfMonth:             DayOfMonth
    )
      extends Journey
      with Journey.Stages.EnteredDayOfMonth
      with Journey.Epaye

    /**
     * [[Journey]] after EnteredAmount
     * Epaye
     */
    final case class EnteredInstalmentAmount(
        override val _id:                    JourneyId,
        override val origin:                 Origins.Epaye,
        override val createdOn:              LocalDateTime,
        override val sjRequest:              SjRequest.Epaye,
        override val sessionId:              SessionId,
        override val stage:                  Stage.AfterEnteredAmount,
        override val taxId:                  EmpRef,
        override val eligibilityCheckResult: EligibilityCheckResult,
        override val canPayUpfront:          CanPayUpfront,
        override val upfrontPaymentAmount:   UpfrontPaymentAmount //,
    //        override val dayOfMonth:             DayOfMonth,
    //        override val amount:                 AmountInPence
    )
      extends Journey
      with Journey.Stages.EnteredInstalmentAmount
      with Journey.Epaye

    /**
     * [[Journey]] after SelectedPlan
     * Epaye
     */
    final case class HasSelectedPlan(
        override val _id:                    JourneyId,
        override val origin:                 Origins.Epaye,
        override val createdOn:              LocalDateTime,
        override val sjRequest:              SjRequest.Epaye,
        override val sessionId:              SessionId,
        override val stage:                  Stage.AfterSelectedPlan,
        override val taxId:                  EmpRef,
        override val eligibilityCheckResult: EligibilityCheckResult,
        override val canPayUpfront:          CanPayUpfront,
        override val upfrontPaymentAmount:   UpfrontPaymentAmount //,
    //        override val dayOfMonth:             DayOfMonth,
    //        override val amount:                 AmountInPence,
    //        override val selectedPlan:           SelectedPlan
    )
      extends Journey
      with Journey.Stages.HasSelectedPlan
      with Journey.Epaye
  }

}
