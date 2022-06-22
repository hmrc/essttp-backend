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
import essttp.journey.model.Journey.Epaye
import essttp.journey.model.Journey.Stages.{AnsweredCanPayUpfront, EnteredUpfrontPaymentAmount}
import essttp.journey.model.{Journey, JourneyId, Stage, UpfrontPaymentAnswers}
import essttp.rootmodel.dates.extremedates.ExtremeDatesResponse
import essttp.rootmodel.dates.startdates.StartDatesResponse
import essttp.utils.Errors
import io.scalaland.chimney.dsl.TransformerOps
import play.api.mvc.{Action, ControllerComponents, Request}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateDatesController @Inject() (
    journeyService: JourneyService,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext) extends BackendController(cc) {

  def updateExtremeDates(journeyId: JourneyId): Action[ExtremeDatesResponse] = Action.async(parse.json[ExtremeDatesResponse]) { implicit request =>
    for {
      journey <- journeyService.get(journeyId)
      _ <- journey match {
        case j: Journey.Stages.EnteredUpfrontPaymentAmount => updateJourneyWithNewExtremeDatesValue(Right(j), request.body)
        case j: Journey.Stages.AnsweredCanPayUpfront       => updateJourneyWithNewExtremeDatesValue(Left(j), request.body)
        case j: Journey.AfterExtremeDatesResponse          => updateJourneyWithExistingExtremeDatesValue(j, request.body)
        case j: Journey.BeforeUpfrontPaymentAnswers        => Errors.throwBadRequestExceptionF(s"UpdateExtremeDatesResponse update is not possible in that state: [${j.stage}]")
      }
    } yield Ok
  }

  private def updateJourneyWithNewExtremeDatesValue(
      journey:              Either[AnsweredCanPayUpfront, EnteredUpfrontPaymentAmount],
      extremeDatesResponse: ExtremeDatesResponse
  )(implicit request: Request[_]): Future[Unit] = {
    val newJourney: Journey.Stages.RetrievedExtremeDates = journey match {
      case Left(j: Epaye.AnsweredCanPayUpfront) => j.into[Journey.Epaye.RetrievedExtremeDates]
        .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
        .withFieldConst(_.upfrontPaymentAnswers, UpfrontPaymentAnswers.NoUpfrontPayment)
        .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
        .transform
      case Right(j: Epaye.EnteredUpfrontPaymentAmount) => j.into[Journey.Epaye.RetrievedExtremeDates]
        .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
        .withFieldConst(_.upfrontPaymentAnswers, UpfrontPaymentAnswers.DeclaredUpfrontPayment(j.upfrontPaymentAmount))
        .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
        .transform
    }
    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingExtremeDatesValue(
      journey:              Journey.AfterExtremeDatesResponse,
      extremeDatesResponse: ExtremeDatesResponse
  )(implicit request: Request[_]): Future[Unit] = {

    if (journey.extremeDatesResponse === extremeDatesResponse) {
      JourneyLogger.info("Nothing to update, ExtremeDatesResponse is the same as the existing one in journey.")
      Future.successful(())
    } else {
      val newJourney: Journey.AfterExtremeDatesResponse = journey match {
        case j: Journey.Epaye.RetrievedExtremeDates => j.copy(extremeDatesResponse = extremeDatesResponse)
        case j: Journey.Epaye.RetrievedAffordabilityResult =>
          j.into[Journey.Epaye.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Epaye.EnteredMonthlyPaymentAmount =>
          j.into[Journey.Epaye.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Epaye.EnteredDayOfMonth =>
          j.into[Journey.Epaye.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Epaye.RetrievedStartDates =>
          j.into[Journey.Epaye.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Epaye.RetrievedAffordableQuotes =>
          j.into[Journey.Epaye.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
      }
      journeyService.upsert(newJourney)
    }
  }

  def updateStartDates(journeyId: JourneyId): Action[StartDatesResponse] = Action.async(parse.json[StartDatesResponse]) { implicit request =>
    for {
      journey <- journeyService.get(journeyId)
      _ <- journey match {
        case j: Journey.BeforeEnteredDayOfMonth  => Errors.throwBadRequestExceptionF(s"UpdateStartDates is not possible when we don't have a chosen day of month, stage: [ ${j.stage} ]")
        case j: Journey.Stages.EnteredDayOfMonth => updateJourneyWithNewStartDatesValue(j, request.body)
        case j: Journey.AfterStartDatesResponse  => updateJourneyWithExistingStartDatesValue(j, request.body)
      }
    } yield Ok
  }

  private def updateJourneyWithNewStartDatesValue(
      journey:            Journey.Stages.EnteredDayOfMonth,
      startDatesResponse: StartDatesResponse
  )(implicit request: Request[_]): Future[Unit] = {
    val newJourney: Journey.AfterStartDatesResponse = journey match {
      case j: Epaye.EnteredDayOfMonth =>
        j.into[Journey.Epaye.RetrievedStartDates]
          .withFieldConst(_.stage, Stage.AfterStartDatesResponse.StartDatesResponseRetrieved)
          .withFieldConst(_.startDatesResponse, startDatesResponse)
          .transform
    }
    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingStartDatesValue(
      journey:            Journey.AfterStartDatesResponse,
      startDatesResponse: StartDatesResponse
  )(implicit request: Request[_]): Future[Unit] = {
    if (journey.startDatesResponse === startDatesResponse) {
      JourneyLogger.info("Nothing to update, StartDatesResponse is the same as the existing one in journey.")
      Future.successful(())
    } else {
      val newJourney: Journey.AfterStartDatesResponse = journey match {
        case j: Journey.Epaye.RetrievedStartDates => j.copy(startDatesResponse = startDatesResponse)
        case j: Journey.Epaye.RetrievedAffordableQuotes =>
          j.into[Journey.Epaye.RetrievedStartDates]
            .withFieldConst(_.stage, Stage.AfterStartDatesResponse.StartDatesResponseRetrieved)
            .withFieldConst(_.startDatesResponse, startDatesResponse)
            .transform
        //        case j: Journey.Epaye.SelectedPaymentPlan =>
        //          j.into[Journey.Epaye.RetrievedExtremeDates]
        //            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
        //            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
        //            .transform
      }
      journeyService.upsert(newJourney)
    }
  }
}
