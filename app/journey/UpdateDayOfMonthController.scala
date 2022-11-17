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

import action.Actions
import cats.syntax.eq._
import com.google.inject.{Inject, Singleton}
import essttp.crypto.CryptoFormat.OperationalCryptoFormat
import essttp.journey.model.Journey.{Epaye, Vat, Stages}
import essttp.journey.model.{Journey, JourneyId, Stage}
import essttp.rootmodel.DayOfMonth
import essttp.utils.Errors
import io.scalaland.chimney.dsl.TransformerOps
import play.api.mvc.{Action, ControllerComponents, Request}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateDayOfMonthController @Inject() (
    actions:        Actions,
    journeyService: JourneyService,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext, cryptoFormat: OperationalCryptoFormat) extends BackendController(cc) {

  def updateDayOfMonth(journeyId: JourneyId): Action[DayOfMonth] = actions.authenticatedAction.async(parse.json[DayOfMonth]) { implicit request =>
    for {
      journey <- journeyService.get(journeyId)
      newJourney <- journey match {
        case j: Journey.BeforeEnteredMonthlyPaymentAmount  => Errors.throwBadRequestExceptionF(s"UpdateDayOfMonth update is not possible in that state: [${j.stage}]")
        case j: Journey.Stages.EnteredMonthlyPaymentAmount => updateJourneyWithNewValue(j, request.body)
        case j: Journey.AfterEnteredDayOfMonth => j match {
          case _: Journey.BeforeArrangementSubmitted => updateJourneyWithExistingValue(j, request.body)
          case _: Journey.AfterArrangementSubmitted  => Errors.throwBadRequestExceptionF("Cannot update DayOfMonth when journey is in completed state")
        }
      }
    } yield Ok(newJourney.json)
  }

  private def updateJourneyWithNewValue(
      journey:    Stages.EnteredMonthlyPaymentAmount,
      dayOfMonth: DayOfMonth
  )(implicit request: Request[_]): Future[Journey] = {
    val newJourney: Journey.AfterEnteredDayOfMonth = journey match {
      case j: Epaye.EnteredMonthlyPaymentAmount =>
        j.into[Epaye.EnteredDayOfMonth]
          .withFieldConst(_.stage, Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth)
          .withFieldConst(_.dayOfMonth, dayOfMonth)
          .transform
      case j: Vat.EnteredMonthlyPaymentAmount =>
        j.into[Vat.EnteredDayOfMonth]
          .withFieldConst(_.stage, Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth)
          .withFieldConst(_.dayOfMonth, dayOfMonth)
          .transform
    }
    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingValue(
      journey:    Journey.AfterEnteredDayOfMonth,
      dayOfMonth: DayOfMonth
  )(implicit request: Request[_]): Future[Journey] = {
    if (journey.dayOfMonth === dayOfMonth) {
      JourneyLogger.info("Day of month hasn't changed, nothing to update")
      Future.successful(journey)
    } else {
      val updatedJourney: Journey.Stages.EnteredDayOfMonth = journey match {

        case j: Epaye.EnteredDayOfMonth =>
          j.copy(dayOfMonth = dayOfMonth)
        case j: Vat.EnteredDayOfMonth =>
          j.copy(dayOfMonth = dayOfMonth)

        case j: Journey.Epaye.RetrievedStartDates =>
          j.into[Journey.Epaye.EnteredDayOfMonth]
            .withFieldConst(_.stage, Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth)
            .withFieldConst(_.dayOfMonth, dayOfMonth)
            .transform
        case j: Journey.Vat.RetrievedStartDates =>
          j.into[Journey.Vat.EnteredDayOfMonth]
            .withFieldConst(_.stage, Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth)
            .withFieldConst(_.dayOfMonth, dayOfMonth)
            .transform

        case j: Journey.Epaye.RetrievedAffordableQuotes =>
          j.into[Journey.Epaye.EnteredDayOfMonth]
            .withFieldConst(_.stage, Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth)
            .withFieldConst(_.dayOfMonth, dayOfMonth)
            .transform
        case j: Journey.Vat.RetrievedAffordableQuotes =>
          j.into[Journey.Vat.EnteredDayOfMonth]
            .withFieldConst(_.stage, Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth)
            .withFieldConst(_.dayOfMonth, dayOfMonth)
            .transform

        case j: Journey.Epaye.ChosenPaymentPlan =>
          j.into[Journey.Epaye.EnteredDayOfMonth]
            .withFieldConst(_.stage, Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth)
            .withFieldConst(_.dayOfMonth, dayOfMonth)
            .transform
        case j: Journey.Vat.ChosenPaymentPlan =>
          j.into[Journey.Vat.EnteredDayOfMonth]
            .withFieldConst(_.stage, Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth)
            .withFieldConst(_.dayOfMonth, dayOfMonth)
            .transform

        case j: Journey.Epaye.CheckedPaymentPlan =>
          j.into[Journey.Epaye.EnteredDayOfMonth]
            .withFieldConst(_.stage, Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth)
            .withFieldConst(_.dayOfMonth, dayOfMonth)
            .transform
        case j: Journey.Vat.CheckedPaymentPlan =>
          j.into[Journey.Vat.EnteredDayOfMonth]
            .withFieldConst(_.stage, Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth)
            .withFieldConst(_.dayOfMonth, dayOfMonth)
            .transform

        case j: Journey.Epaye.EnteredDetailsAboutBankAccount =>
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
        case j: Journey.Epaye.SelectedEmailToBeVerified =>
          j.into[Journey.Epaye.EnteredDayOfMonth]
            .withFieldConst(_.stage, Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth)
            .withFieldConst(_.dayOfMonth, dayOfMonth)
            .transform
        case j: Journey.Epaye.EmailVerificationComplete =>
          j.into[Journey.Epaye.EnteredDayOfMonth]
            .withFieldConst(_.stage, Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth)
            .withFieldConst(_.dayOfMonth, dayOfMonth)
            .transform
        case _: Journey.Epaye.SubmittedArrangement =>
          Errors.throwBadRequestException("Cannot update DayOfMonth when journey is in completed state")
      }
      journeyService.upsert(updatedJourney)
    }
  }

}
