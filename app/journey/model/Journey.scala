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

import julienrf.json.derived
import play.api.libs.json.{Json, OFormat, OWrites}
import rootmodel._
import utils.Errors

import java.time.LocalDateTime

sealed trait Journey {
  def _id: JourneyId
  def origin: Origin
  def createdAt: LocalDateTime
  def sjRequest: SjRequest
  def sessionId: SessionId
  def taxRegime: TaxRegime
  def stage: Stage

  /* derived stuff: */

  def id: JourneyId = _id
  def journeyId: JourneyId = _id

  def name: String = {
    val className = getClass.getName
    val packageName = getClass.getPackage.getName
    className
      .replaceAllLiterally(s"$packageName.", "")
      .replaceAllLiterally("$", ".")
  }

  def backUrl: BackUrl
  def returnUrl: ReturnUrl
}
object Journey {

  implicit val format: OFormat[Journey] = {

    val defaultFormat: OFormat[Journey] = derived.oformat[Journey]()

    //we need to write some extra fields on the top of the structure so it's
    //possible to index on them and use them in queries
    val customWrites = OWrites[Journey](j =>
      defaultFormat.writes(j) ++ Json.obj(
        "sessionId" -> j.sessionId,
        "createdAt" -> j.createdAt
      ))
    OFormat(
      defaultFormat,
      customWrites
    )
  }

  sealed trait HasTaxId extends Journey { self: Journey =>
    def taxId: TaxId
  }

  sealed trait HasDayOfMonth extends Journey { self: Journey =>
    def dayOfMonth: DayOfMonth
  }

  sealed trait HasAmount extends Journey { self: Journey =>
    def amount: AmountInPence
  }


  /**
   * Journey extractors extracting journeys in particular stage.
   * They correspond to actual [[Stage]] values
   */
  object Stages {

    private val sanityMessage = "Sanity check just in case if you messed journey traits up"

    sealed trait AfterStarted extends Journey { self: Journey =>
      Errors.sanityCheck(Stage.AfterStarted.values.contains(stage), sanityMessage)
      def stage: Stage.AfterStarted
    }

    sealed trait AfterEligibilityCheck
      extends Journey
        with HasTaxId { self: Journey =>
      Errors.sanityCheck(Stage.AfterEligibilityCheck.values.contains(stage), sanityMessage)
      def stage: Stage.AfterEligibilityCheck
    }

    sealed trait AfterEnteredDayOfMonth
      extends Journey
      with HasTaxId
      with HasDayOfMonth
      { self: Journey =>
      Errors.sanityCheck(Stage.AfterEnteredDayOfMonth.values.contains(stage), sanityMessage)
      def stage: Stage.AfterEnteredDayOfMonth
    }

    sealed trait AfterEnteredAmount
      extends Journey
        with HasTaxId
        with HasDayOfMonth
        with HasAmount
          { self: Journey =>
      Errors.sanityCheck(Stage.AfterEnteredAmount.values.contains(stage), sanityMessage)
      def stage: Stage.AfterEnteredAmount
    }

    sealed trait AfterSelectedPlan
      extends Journey
        with HasTaxId
        with HasDayOfMonth
        with HasAmount { self: Journey =>
      Errors.sanityCheck(Stage.AfterSelectedPlan.values.contains(stage), sanityMessage)
      def stage: Stage.AfterSelectedPlan
      def selectedPlan: SelectedPlan
    }
  }

  /**
   * Marking trait for extracting Epaye [[Journey]]s
   */
  sealed trait Epaye extends Journey { self: Journey =>
    override def taxRegime: TaxRegime.Epaye.type = TaxRegime.Epaye
    override def sjRequest: SjRequest.Epaye
    override def origin: Origin.Epaye

    override val (backUrl: BackUrl, returnUrl: ReturnUrl) = sjRequest match {
      case r: SjRequest.Epaye.Simple => (r.backUrl, r.returnUrl)
      case _ => Errors.notImplemented("PAWEL TODO")
    }
  }

  object Epaye {

    /**
     * [[Journey]] after started
     * Epaye
     */
    final case class AfterStarted(
                                   override val _id:         JourneyId,
                                   override val origin:      Origin.Epaye,
                                   override val createdAt:   LocalDateTime,
                                   override val sjRequest: SjRequest.Epaye,
                                   override val sessionId:   SessionId,
                                   override val stage:       Stage.AfterStarted
                                 )
      extends Journey
        with Journey.Stages.AfterStarted
        with Journey.Epaye

    /**
     * [[Journey]] after EligibilityCheck
     * Epaye
     */
    final case class AfterEligibilityCheck(
                                          override val _id:         JourneyId,
                                          override val origin:      Origin.Epaye,
                                          override val createdAt:   LocalDateTime,
                                          override val sjRequest:   SjRequest.Epaye,
                                          override val sessionId:   SessionId,
                                          override val stage:       Stage.AfterEligibilityCheck,
                                          override val taxId:       Aor
                                        )
      extends Journey
        with Journey.Stages.AfterEligibilityCheck
        with Journey.Epaye

    /**
     * [[Journey]] after EnteredDayOfMonth
     * Epaye
     */
    final case class EnteredDayOfMonth(
                                          override val _id:         JourneyId,
                                          override val origin:      Origin.Epaye,
                                          override val createdAt:   LocalDateTime,
                                          override val sjRequest:   SjRequest.Epaye,
                                          override val sessionId:   SessionId,
                                          override val stage:       Stage.AfterEnteredDayOfMonth,
                                          override val taxId:       Aor,
                                          override val dayOfMonth: DayOfMonth,
                                        )
      extends Journey
        with Journey.Stages.AfterEnteredDayOfMonth
        with Journey.Epaye

    /**
     * [[Journey]] after EnteredAmount
     * Epaye
     */
    final case class AfterEnteredAmount(
                                          override val _id:         JourneyId,
                                          override val origin:      Origin.Epaye,
                                          override val createdAt:   LocalDateTime,
                                          override val sjRequest: SjRequest.Epaye,
                                          override val sessionId:   SessionId,
                                          override val stage:       Stage.AfterEnteredAmount,
                                          override val taxId:       Aor,
                                          override val dayOfMonth: DayOfMonth,
                                          override val amount: AmountInPence,
                                        )
      extends Journey
        with Journey.Stages.AfterEnteredAmount
        with Journey.Epaye

    /**
     * [[Journey]] after SelectedPlan
     * Epaye
     */
    final case class AfterSelectedPlan(
                                          override val _id:         JourneyId,
                                          override val origin:      Origin.Epaye,
                                          override val createdAt:   LocalDateTime,
                                          override val sjRequest: SjRequest.Epaye,
                                          override val sessionId:   SessionId,
                                          override val stage:       Stage.AfterSelectedPlan,
                                          override val taxId:       Aor,
                                          override val dayOfMonth: DayOfMonth,
                                          override val amount: AmountInPence,
                                          override val selectedPlan: SelectedPlan,
                                        )
      extends Journey
        with Journey.Stages.AfterSelectedPlan
        with Journey.Epaye
  }

}
