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

package journey

import action.Actions
import cats.syntax.eq._
import com.google.inject.{Inject, Singleton}
import essttp.crypto.CryptoFormat.OperationalCryptoFormat
import essttp.journey.model.Journey.{Epaye, Sa, Stages, Vat}
import essttp.journey.model.{Journey, JourneyId, PaymentPlanAnswers, Stage}
import essttp.rootmodel.DayOfMonth
import essttp.utils.Errors
import io.scalaland.chimney.dsl.TransformationOps
import play.api.mvc.{Action, ControllerComponents, Request}
import services.JourneyService
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
        case j: Journey.BeforeEnteredMonthlyPaymentAmount  => Errors.throwBadRequestExceptionF(s"UpdateDayOfMonth update is not possible in that state: [${j.stage.toString}]")
        case j: Journey.Stages.EnteredMonthlyPaymentAmount => updateJourneyWithNewValue(j, request.body)
        case j: Journey.AfterEnteredDayOfMonth             => updateJourneyWithExistingValue(Left(j), request.body)
        case j: Journey.AfterCheckedPaymentPlan =>
          j match {
            case _: Journey.BeforeArrangementSubmitted => updateJourneyWithExistingValue(Right(j), request.body)
            case _: Journey.AfterArrangementSubmitted  => Errors.throwBadRequestExceptionF("Cannot update DayOfMonth when journey is in completed state")
          }
        case _: Journey.AfterStartedPegaCase => Errors.throwBadRequestExceptionF("Not expecting to update DayOfMonth after starting PEGA case")
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
      case j: Sa.EnteredMonthlyPaymentAmount =>
        j.into[Sa.EnteredDayOfMonth]
          .withFieldConst(_.stage, Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth)
          .withFieldConst(_.dayOfMonth, dayOfMonth)
          .transform
    }
    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingValue(
      journey:    Either[Journey.AfterEnteredDayOfMonth, Journey.AfterCheckedPaymentPlan],
      dayOfMonth: DayOfMonth
  )(implicit request: Request[_]): Future[Journey] =
    journey match {
      case Left(afterEnteredDayOfMonth) =>
        updateJourneyWithExistingValue(
          afterEnteredDayOfMonth.dayOfMonth,
          afterEnteredDayOfMonth,
          dayOfMonth,
          afterEnteredDayOfMonth match {
            case j: Epaye.EnteredDayOfMonth => j.copy(dayOfMonth = dayOfMonth)
            case j: Vat.EnteredDayOfMonth   => j.copy(dayOfMonth = dayOfMonth)
            case j: Sa.EnteredDayOfMonth    => j.copy(dayOfMonth = dayOfMonth)

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
            case j: Journey.Sa.RetrievedStartDates =>
              j.into[Journey.Sa.EnteredDayOfMonth]
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
            case j: Journey.Sa.RetrievedAffordableQuotes =>
              j.into[Journey.Sa.EnteredDayOfMonth]
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
            case j: Journey.Sa.ChosenPaymentPlan =>
              j.into[Journey.Sa.EnteredDayOfMonth]
                .withFieldConst(_.stage, Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth)
                .withFieldConst(_.dayOfMonth, dayOfMonth)
                .transform
          }
        )

      case Right(afterCheckedPaymentPlan) =>
        afterCheckedPaymentPlan.paymentPlanAnswers match {
          case _: PaymentPlanAnswers.PaymentPlanAfterAffordability =>
            Errors.throwBadRequestExceptionF("Cannot update DayOfMonth on affordability journey")

          case p: PaymentPlanAnswers.PaymentPlanNoAffordability =>
            updateJourneyWithExistingValue(
              p.dayOfMonth,
              afterCheckedPaymentPlan,
              dayOfMonth,
              afterCheckedPaymentPlan match {
                case j: Journey.Epaye.CheckedPaymentPlan =>
                  j.into[Journey.Epaye.EnteredDayOfMonth]
                    .withFieldConst(_.stage, Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, dayOfMonth)
                    .transform
                case j: Journey.Vat.CheckedPaymentPlan =>
                  j.into[Journey.Vat.EnteredDayOfMonth]
                    .withFieldConst(_.stage, Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, dayOfMonth)
                    .transform
                case j: Journey.Sa.CheckedPaymentPlan =>
                  j.into[Journey.Sa.EnteredDayOfMonth]
                    .withFieldConst(_.stage, Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, dayOfMonth)
                    .transform

                case j: Journey.Epaye.EnteredDetailsAboutBankAccount =>
                  j.into[Journey.Epaye.EnteredDayOfMonth]
                    .withFieldConst(_.stage, Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, dayOfMonth)
                    .transform
                case j: Journey.Vat.EnteredDetailsAboutBankAccount =>
                  j.into[Journey.Vat.EnteredDayOfMonth]
                    .withFieldConst(_.stage, Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, dayOfMonth)
                    .transform
                case j: Journey.Sa.EnteredDetailsAboutBankAccount =>
                  j.into[Journey.Sa.EnteredDayOfMonth]
                    .withFieldConst(_.stage, Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, dayOfMonth)
                    .transform

                case j: Journey.Epaye.EnteredDirectDebitDetails =>
                  j.into[Journey.Epaye.EnteredDayOfMonth]
                    .withFieldConst(_.stage, Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, dayOfMonth)
                    .transform
                case j: Journey.Vat.EnteredDirectDebitDetails =>
                  j.into[Journey.Vat.EnteredDayOfMonth]
                    .withFieldConst(_.stage, Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, dayOfMonth)
                    .transform
                case j: Journey.Sa.EnteredDirectDebitDetails =>
                  j.into[Journey.Sa.EnteredDayOfMonth]
                    .withFieldConst(_.stage, Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, dayOfMonth)
                    .transform

                case j: Journey.Epaye.ConfirmedDirectDebitDetails =>
                  j.into[Journey.Epaye.EnteredDayOfMonth]
                    .withFieldConst(_.stage, Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, dayOfMonth)
                    .transform
                case j: Journey.Vat.ConfirmedDirectDebitDetails =>
                  j.into[Journey.Vat.EnteredDayOfMonth]
                    .withFieldConst(_.stage, Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, dayOfMonth)
                    .transform
                case j: Journey.Sa.ConfirmedDirectDebitDetails =>
                  j.into[Journey.Sa.EnteredDayOfMonth]
                    .withFieldConst(_.stage, Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, dayOfMonth)
                    .transform

                case j: Journey.Epaye.AgreedTermsAndConditions =>
                  j.into[Journey.Epaye.EnteredDayOfMonth]
                    .withFieldConst(_.stage, Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, dayOfMonth)
                    .transform
                case j: Journey.Vat.AgreedTermsAndConditions =>
                  j.into[Journey.Vat.EnteredDayOfMonth]
                    .withFieldConst(_.stage, Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, dayOfMonth)
                    .transform
                case j: Journey.Sa.AgreedTermsAndConditions =>
                  j.into[Journey.Sa.EnteredDayOfMonth]
                    .withFieldConst(_.stage, Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, dayOfMonth)
                    .transform

                case j: Journey.Epaye.SelectedEmailToBeVerified =>
                  j.into[Journey.Epaye.EnteredDayOfMonth]
                    .withFieldConst(_.stage, Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, dayOfMonth)
                    .transform
                case j: Journey.Vat.SelectedEmailToBeVerified =>
                  j.into[Journey.Vat.EnteredDayOfMonth]
                    .withFieldConst(_.stage, Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, dayOfMonth)
                    .transform
                case j: Journey.Sa.SelectedEmailToBeVerified =>
                  j.into[Journey.Sa.EnteredDayOfMonth]
                    .withFieldConst(_.stage, Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, dayOfMonth)
                    .transform

                case j: Journey.Epaye.EmailVerificationComplete =>
                  j.into[Journey.Epaye.EnteredDayOfMonth]
                    .withFieldConst(_.stage, Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, dayOfMonth)
                    .transform
                case j: Journey.Vat.EmailVerificationComplete =>
                  j.into[Journey.Vat.EnteredDayOfMonth]
                    .withFieldConst(_.stage, Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, dayOfMonth)
                    .transform
                case j: Journey.Sa.EmailVerificationComplete =>
                  j.into[Journey.Sa.EnteredDayOfMonth]
                    .withFieldConst(_.stage, Stage.AfterEnteredDayOfMonth.EnteredDayOfMonth)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, dayOfMonth)
                    .transform

                case _: Journey.Stages.SubmittedArrangement =>
                  Errors.throwBadRequestException("Cannot update DayOfMonth when journey is in completed state")
              }
            )
        }

    }

  private def updateJourneyWithExistingValue(
      existingValue:   DayOfMonth,
      existingJourney: Journey,
      newValue:        DayOfMonth,
      newJourney:      Journey
  )(implicit request: Request[_]): Future[Journey] =
    if (existingValue === newValue) {
      JourneyLogger.info("Nothing to update, DayOfMonth is the same as the existing one in journey.")
      Future.successful(existingJourney)
    } else {
      journeyService.upsert(newJourney)
    }

}
