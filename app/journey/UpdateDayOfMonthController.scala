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

import cats.syntax.eq._
import com.google.inject.{Inject, Singleton}
import essttp.journey.model.Journey.{Epaye, Stages}
import essttp.journey.model.{Journey, JourneyId, Stage}
import essttp.rootmodel.DayOfMonth
import essttp.utils.Errors
import io.scalaland.chimney.dsl.TransformerOps
import play.api.mvc.{Action, ControllerComponents, Request}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateDayOfMonthController @Inject() (
    journeyService: JourneyService,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext) extends BackendController(cc) {

  def updateDayOfMonth(journeyId: JourneyId): Action[DayOfMonth] = Action.async(parse.json[DayOfMonth]) { implicit request =>
    for {
      journey <- journeyService.get(journeyId)
      _ <- journey match {
        case j: Journey.BeforeEnteredMonthlyPaymentAmount  => Errors.throwBadRequestExceptionF(s"UpdateDayOfMonth update is not possible in that state: [${j.stage}]")
        case j: Journey.Stages.EnteredMonthlyPaymentAmount => updateJourneyWithNewValue(j, request.body)
        case j: Journey.AfterEnteredDayOfMonth             => updateJourneyWithExistingValue(j, request.body)
      }
    } yield Ok
  }

  private def updateJourneyWithNewValue(
      journey:    Stages.EnteredMonthlyPaymentAmount,
      dayOfMonth: DayOfMonth
  )(implicit request: Request[_]): Future[Unit] = {
    val newJourney: Epaye.EnteredDayOfMonth = journey match {
      case j: Epaye.EnteredMonthlyPaymentAmount =>
        j.into[Epaye.EnteredDayOfMonth]
          .withFieldConst(_.stage, Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth)
          .withFieldConst(_.dayOfMonth, dayOfMonth)
          .transform
    }
    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingValue(
      journey:    Journey.AfterEnteredDayOfMonth,
      dayOfMonth: DayOfMonth
  )(implicit request: Request[_]): Future[Unit] = {
    if (journey.dayOfMonth === dayOfMonth) {
      JourneyLogger.info("Day of month hasn't changed, nothing to update")
      Future.successful(())
    } else {
      val updatedJourney: Journey.Stages.EnteredDayOfMonth = journey match {
        case j: Epaye.EnteredDayOfMonth =>
          j.copy(dayOfMonth = dayOfMonth)
        case j: Journey.Epaye.RetrievedStartDates =>
          j.into[Journey.Epaye.EnteredDayOfMonth]
            .withFieldConst(_.stage, Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth)
            .withFieldConst(_.dayOfMonth, dayOfMonth)
            .transform
        case j: Journey.Epaye.RetrievedAffordableQuotes =>
          j.into[Journey.Epaye.EnteredDayOfMonth]
            .withFieldConst(_.stage, Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth)
            .withFieldConst(_.dayOfMonth, dayOfMonth)
            .transform
        case j: Journey.Epaye.ChosenPaymentPlan =>
          j.into[Journey.Epaye.EnteredDayOfMonth]
            .withFieldConst(_.stage, Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth)
            .withFieldConst(_.dayOfMonth, dayOfMonth)
            .transform
        case j: Journey.Epaye.CheckedPaymentPlan =>
          j.into[Journey.Epaye.EnteredDayOfMonth]
            .withFieldConst(_.stage, Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth)
            .withFieldConst(_.dayOfMonth, dayOfMonth)
            .transform
        case j: Journey.Epaye.EnteredDirectDebitDetails =>
          j.into[Journey.Epaye.EnteredDayOfMonth]
            .withFieldConst(_.stage, Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth)
            .withFieldConst(_.dayOfMonth, dayOfMonth)
            .transform
        case j: Journey.Epaye.ConfirmedDirectDebitDetails =>
          j.into[Journey.Epaye.EnteredDayOfMonth]
            .withFieldConst(_.stage, Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth)
            .withFieldConst(_.dayOfMonth, dayOfMonth)
            .transform
        case j: Journey.Epaye.AgreedTermsAndConditions =>
          j.into[Journey.Epaye.EnteredDayOfMonth]
            .withFieldConst(_.stage, Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth)
            .withFieldConst(_.dayOfMonth, dayOfMonth)
            .transform
      }
      journeyService.upsert(updatedJourney)
    }
  }

}
