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

import cats.Eq
import cats.syntax.eq._
import com.google.inject.{Inject, Singleton}
import essttp.journey.model.Journey.{Epaye, Stages}
import essttp.journey.model._
import essttp.journey.model.ttp.EligibilityCheckResult
import essttp.utils.Errors
import io.scalaland.chimney.dsl._
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateEligibilityCheckResultController @Inject() (
    journeyService: JourneyService,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext) extends BackendController(cc) {

  implicit val eq: Eq[EligibilityCheckResult] = Eq.fromUniversalEquals

  def updateEligibilityResult(journeyId: JourneyId): Action[EligibilityCheckResult] = Action.async(parse.json[EligibilityCheckResult]) { implicit request =>
    for {
      journey <- journeyService.get(journeyId)
      _ <- journey match {
        case _: Journey.BeforeComputedTaxId     => Errors.throwBadRequestExceptionF("EligibilityCheckResult update is not possible in that state.")
        case j: Journey.Stages.ComputedTaxId    => updateJourneyWithNewValue(j, request.body)
        case j: Journey.AfterEligibilityChecked => updateJourneyWithExistingValue(j, request.body)
      }
    } yield Ok
  }

  private def updateJourneyWithNewValue(
      journey:                Stages.ComputedTaxId,
      eligibilityCheckResult: EligibilityCheckResult
  )(implicit request: Request[_]): Future[Unit] = {
    journey match {
      case j: Journey.Epaye.ComputedTaxId =>
        val newJourney = j
          .into[Journey.Epaye.EligibilityChecked]
          .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
          .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
          .transform
        journeyService.upsert(newJourney)
    }
  }

  private def updateJourneyWithExistingValue(
      journey:                Journey.AfterEligibilityChecked,
      eligibilityCheckResult: EligibilityCheckResult
  )(implicit request: Request[_]): Future[Unit] = {
    if (journey.eligibilityCheckResult === eligibilityCheckResult) {
      JourneyLogger.info("Nothing to update, EligibilityCheckResult is the same as the existing one in journey.")
      Future.successful(())
    } else {
      val updatedJourney: Journey.AfterEligibilityChecked = journey match {
        case j: Epaye.EligibilityChecked =>
          j.copy(eligibilityCheckResult = eligibilityCheckResult)
        case j: Epaye.AnsweredCanPayUpfront =>
          j.into[Journey.Epaye.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Epaye.EnteredUpfrontPaymentAmount =>
          j.into[Journey.Epaye.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Epaye.RetrievedExtremeDates =>
          j.into[Journey.Epaye.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Epaye.RetrievedAffordabilityResult =>
          j.into[Journey.Epaye.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Epaye.EnteredMonthlyPaymentAmount =>
          j.into[Journey.Epaye.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Epaye.EnteredDayOfMonth =>
          j.into[Journey.Epaye.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Epaye.RetrievedStartDates =>
          j.into[Journey.Epaye.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Epaye.RetrievedAffordableQuotes =>
          j.into[Journey.Epaye.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Epaye.ChosenPaymentPlan =>
          j.into[Journey.Epaye.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Epaye.CheckedPaymentPlan =>
          j.into[Journey.Epaye.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Epaye.EnteredDirectDebitDetails =>
          j.into[Journey.Epaye.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Epaye.ConfirmedDirectDebitDetails =>
          j.into[Journey.Epaye.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Epaye.AgreedTermsAndConditions =>
          j.into[Journey.Epaye.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
      }
      journeyService.upsert(updatedJourney)
    }
  }

  private def deriveEligibilityEnum(eligibilityCheckResult: EligibilityCheckResult): Stage.AfterEligibilityCheck = {
    if (eligibilityCheckResult.isEligible) Stage.AfterEligibilityCheck.Eligible
    else Stage.AfterEligibilityCheck.Ineligible
  }

}
