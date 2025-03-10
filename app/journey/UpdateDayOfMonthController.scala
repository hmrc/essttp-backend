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
import com.google.inject.{Inject, Singleton}
import essttp.crypto.CryptoFormat.OperationalCryptoFormat
import essttp.journey.model.{Journey, JourneyId, JourneyStage, PaymentPlanAnswers}
import essttp.rootmodel.DayOfMonth
import essttp.utils.Errors
import io.scalaland.chimney.dsl.*
import play.api.mvc.{Action, ControllerComponents, Request}
import services.JourneyService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateDayOfMonthController @Inject() (
  actions:        Actions,
  journeyService: JourneyService,
  cc:             ControllerComponents
)(using ExecutionContext, OperationalCryptoFormat)
    extends BackendController(cc) {

  def updateDayOfMonth(journeyId: JourneyId): Action[DayOfMonth] =
    actions.authenticatedAction.async(parse.json[DayOfMonth]) { implicit request =>
      for {
        journey    <- journeyService.get(journeyId)
        newJourney <- journey match {
                        case j: JourneyStage.BeforeEnteredMonthlyPaymentAmount =>
                          Errors.throwBadRequestExceptionF(
                            s"UpdateDayOfMonth update is not possible in that state: [${j.stage}]"
                          )
                        case j: Journey.EnteredMonthlyPaymentAmount            =>
                          updateJourneyWithNewValue(j, request.body)
                        case j: JourneyStage.AfterEnteredDayOfMonth            =>
                          updateJourneyWithExistingValue(Left(j), request.body)
                        case j: JourneyStage.AfterCheckedPaymentPlan           =>
                          j match {
                            case _: JourneyStage.BeforeArrangementSubmitted =>
                              updateJourneyWithExistingValue(Right(j), request.body)
                            case _: JourneyStage.AfterArrangementSubmitted  =>
                              Errors.throwBadRequestExceptionF(
                                "Cannot update DayOfMonth when journey is in completed state"
                              )
                          }
                        case _: JourneyStage.AfterStartedPegaCase              =>
                          Errors.throwBadRequestExceptionF(
                            "Not expecting to update DayOfMonth after starting PEGA case"
                          )
                      }
      } yield Ok(newJourney.json)
    }

  private def updateJourneyWithNewValue(
    journey:    Journey.EnteredMonthlyPaymentAmount,
    dayOfMonth: DayOfMonth
  )(using Request[?]): Future[Journey] = {
    val newJourney: Journey =
      journey
        .into[Journey.EnteredDayOfMonth]
        .withFieldConst(_.dayOfMonth, dayOfMonth)
        .transform

    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingValue(
    journey:    Either[JourneyStage.AfterEnteredDayOfMonth & Journey, JourneyStage.AfterCheckedPaymentPlan & Journey],
    dayOfMonth: DayOfMonth
  )(using Request[?]): Future[Journey] =
    journey match {
      case Left(afterEnteredDayOfMonth) =>
        updateJourneyWithExistingValue(
          afterEnteredDayOfMonth.dayOfMonth,
          afterEnteredDayOfMonth,
          dayOfMonth,
          afterEnteredDayOfMonth match {
            case j: Journey.EnteredDayOfMonth => j.copy(dayOfMonth = dayOfMonth)

            case j: Journey.RetrievedStartDates =>
              j.into[Journey.EnteredDayOfMonth]
                .withFieldConst(_.dayOfMonth, dayOfMonth)
                .transform

            case j: Journey.RetrievedAffordableQuotes =>
              j.into[Journey.EnteredDayOfMonth]
                .withFieldConst(_.dayOfMonth, dayOfMonth)
                .transform

            case j: Journey.ChosenPaymentPlan =>
              j.into[Journey.EnteredDayOfMonth]
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
                case j: Journey.CheckedPaymentPlan =>
                  j.into[Journey.EnteredDayOfMonth]
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, dayOfMonth)
                    .transform

                case j: Journey.EnteredCanYouSetUpDirectDebit =>
                  j.into[Journey.EnteredDayOfMonth]
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, dayOfMonth)
                    .transform

                case j: Journey.EnteredDirectDebitDetails =>
                  j.into[Journey.EnteredDayOfMonth]
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, dayOfMonth)
                    .transform

                case j: Journey.ConfirmedDirectDebitDetails =>
                  j.into[Journey.EnteredDayOfMonth]
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, dayOfMonth)
                    .transform

                case j: Journey.AgreedTermsAndConditions =>
                  j.into[Journey.EnteredDayOfMonth]
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, dayOfMonth)
                    .transform

                case j: Journey.SelectedEmailToBeVerified =>
                  j.into[Journey.EnteredDayOfMonth]
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, dayOfMonth)
                    .transform

                case j: Journey.EmailVerificationComplete =>
                  j.into[Journey.EnteredDayOfMonth]
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, dayOfMonth)
                    .transform

                case _: Journey.SubmittedArrangement =>
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
  )(using Request[?]): Future[Journey] =
    if (existingValue == newValue) {
      JourneyLogger.info("Nothing to update, DayOfMonth is the same as the existing one in journey.")
      Future.successful(existingJourney)
    } else {
      journeyService.upsert(newJourney)
    }

}
