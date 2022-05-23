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

  sealed trait HasTaxId extends Journey {
    def taxId: TaxId
  }

  sealed trait HasEligibilityCheckResult extends Journey {
    def eligibilityCheckResult: EligibilityCheckResult
  }

  sealed trait HasCanPayUpfront extends Journey {
    def canPayUpfront: CanPayUpfront
  }

  sealed trait HasUpfrontPaymentAmount extends Journey {
    def upfrontPaymentAmount: UpfrontPaymentAmount
  }

  sealed trait HasDayOfMonth extends Journey {
    def dayOfMonth: DayOfMonth
  }

  sealed trait HasAmount extends Journey {
    def amount: AmountInPence
  }

  sealed trait HasSelectedPlan extends Journey {
    def selectedPlan: SelectedPlan
  }

  /**
   * Journey extractors extracting journeys in particular stage.
   * They correspond to actual [[Stage]] values
   */
  object Stages {

    /**
     * Marking trait for selecting journey in stage
     */
    sealed trait JourneyStage extends Journey {}

    private val sanityMessage = "Sanity check just in case if you messed journey traits up"

    sealed trait AfterStarted extends Journey with JourneyStage {
      Errors.sanityCheck(Stage.AfterStarted.values.contains(stage), sanityMessage)
      def stage: Stage.AfterStarted
    }

    sealed trait AfterComputedTaxId
      extends Journey
      with JourneyStage
      with HasTaxId {
      Errors.sanityCheck(Stage.AfterComputedTaxId.values.contains(stage), sanityMessage)
      def stage: Stage.AfterComputedTaxId
    }

    sealed trait AfterEligibilityCheck
      extends Journey
      with JourneyStage
      with HasTaxId
      with HasEligibilityCheckResult {
      Errors.sanityCheck(Stage.AfterEligibilityCheck.values.contains(stage), sanityMessage)
      def stage: Stage.AfterEligibilityCheck
    }

    sealed trait AfterCanPayUpfront
      extends Journey
      with JourneyStage
      with HasTaxId
      with HasEligibilityCheckResult
      with HasCanPayUpfront {
      Errors.sanityCheck(Stage.AfterCanPayUpfront.values.contains(stage), sanityMessage)
      def stage: Stage.AfterCanPayUpfront
    }

    sealed trait AfterUpfrontPaymentAmount
      extends Journey
      with JourneyStage
      with HasTaxId
      with HasEligibilityCheckResult
      with HasCanPayUpfront
      with HasUpfrontPaymentAmount { self: Journey =>
      Errors.sanityCheck(Stage.AfterUpfrontPaymentAmount.values.contains(stage), sanityMessage)
      def stage: Stage.AfterUpfrontPaymentAmount
      // If entering amount to pay upfront, must have canPayUpfront = true
      Errors.sanityCheck(self.canPayUpfront.value, sanityMessage)
    }

    sealed trait AfterEnteredDayOfMonth
      extends Journey
      with JourneyStage
      with HasTaxId
      with HasEligibilityCheckResult
      with HasDayOfMonth {
      Errors.sanityCheck(Stage.AfterEnteredDayOfMonth.values.contains(stage), sanityMessage)
      def stage: Stage.AfterEnteredDayOfMonth
    }

    sealed trait AfterEnteredAmount
      extends Journey
      with JourneyStage
      with HasTaxId
      with HasEligibilityCheckResult
      with HasDayOfMonth
      with HasAmount {
      Errors.sanityCheck(Stage.AfterEnteredAmount.values.contains(stage), sanityMessage)
      def stage: Stage.AfterEnteredAmount
    }

    sealed trait AfterSelectedPlan
      extends Journey
      with JourneyStage
      with HasTaxId
      with HasEligibilityCheckResult
      with HasDayOfMonth
      with HasAmount
      with HasSelectedPlan {
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
    final case class AfterStarted(
        override val _id:       JourneyId,
        override val origin:    Origins.Epaye,
        override val createdOn: LocalDateTime,
        override val sjRequest: SjRequest.Epaye,
        override val sessionId: SessionId,
        override val stage:     Stage.AfterStarted
    )
      extends Journey
      with Journey.Stages.AfterStarted
      with Journey.Epaye

    /**
     * [[Journey]] after computed TaxIds
     * Epaye
     */
    final case class AfterComputedTaxIds(
        override val _id:       JourneyId,
        override val origin:    Origins.Epaye,
        override val createdOn: LocalDateTime,
        override val sjRequest: SjRequest.Epaye,
        override val sessionId: SessionId,
        override val stage:     Stage.AfterComputedTaxId,
        override val taxId:     EmpRef
    )
      extends Journey
      with Journey.Stages.AfterComputedTaxId
      with Journey.Epaye

    /**
     * [[Journey]] after EligibilityCheck
     * Epaye
     */
    final case class AfterEligibilityCheck(
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
      with Journey.Stages.AfterEligibilityCheck
      with Journey.Epaye

    /**
     * [[Journey]] after CanPayUpfront
     * Epaye
     */
    final case class AfterCanPayUpfront(
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
      with Journey.Stages.AfterCanPayUpfront
      with Journey.Epaye

    /**
     * [[Journey]] after UpfrontPaymentAmount
     * Epaye
     */
    final case class AfterUpfrontPaymentAmount(
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
      with Journey.Stages.AfterUpfrontPaymentAmount
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
        override val dayOfMonth:             DayOfMonth
    )
      extends Journey
      with Journey.Stages.AfterEnteredDayOfMonth
      with Journey.Epaye

    /**
     * [[Journey]] after EnteredAmount
     * Epaye
     */
    final case class AfterEnteredAmount(
        override val _id:                    JourneyId,
        override val origin:                 Origins.Epaye,
        override val createdOn:              LocalDateTime,
        override val sjRequest:              SjRequest.Epaye,
        override val sessionId:              SessionId,
        override val stage:                  Stage.AfterEnteredAmount,
        override val taxId:                  EmpRef,
        override val eligibilityCheckResult: EligibilityCheckResult,
        override val dayOfMonth:             DayOfMonth,
        override val amount:                 AmountInPence
    )
      extends Journey
      with Journey.Stages.AfterEnteredAmount
      with Journey.Epaye

    /**
     * [[Journey]] after SelectedPlan
     * Epaye
     */
    final case class AfterSelectedPlan(
        override val _id:                    JourneyId,
        override val origin:                 Origins.Epaye,
        override val createdOn:              LocalDateTime,
        override val sjRequest:              SjRequest.Epaye,
        override val sessionId:              SessionId,
        override val stage:                  Stage.AfterSelectedPlan,
        override val taxId:                  EmpRef,
        override val eligibilityCheckResult: EligibilityCheckResult,
        override val dayOfMonth:             DayOfMonth,
        override val amount:                 AmountInPence,
        override val selectedPlan:           SelectedPlan
    )
      extends Journey
      with Journey.Stages.AfterSelectedPlan
      with Journey.Epaye
  }

}
