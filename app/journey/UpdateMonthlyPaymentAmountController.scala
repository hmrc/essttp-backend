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
import essttp.rootmodel.MonthlyPaymentAmount
import essttp.utils.Errors
import io.scalaland.chimney.dsl.*
import play.api.mvc.{Action, ControllerComponents, Request}
import services.JourneyService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateMonthlyPaymentAmountController @Inject() (
  actions:        Actions,
  journeyService: JourneyService,
  cc:             ControllerComponents
)(using ExecutionContext, OperationalCryptoFormat)
    extends BackendController(cc) {

  def updateMonthlyPaymentAmount(journeyId: JourneyId): Action[MonthlyPaymentAmount] =
    actions.authenticatedAction.async(parse.json[MonthlyPaymentAmount]) { implicit request =>
      for {
        journey    <- journeyService.get(journeyId)
        newJourney <- journey match {
                        case j: JourneyStage.BeforeCanPayWithinSixMonthsAnswers =>
                          Errors.throwBadRequestExceptionF(
                            s"UpdateMonthlyPaymentAmount update is not possible in that state: [${j.stage}]"
                          )
                        case j: Journey.ObtainedCanPayWithinSixMonthsAnswers    =>
                          updateJourneyWithNewValue(j, request.body)
                        case j: JourneyStage.AfterEnteredMonthlyPaymentAmount   =>
                          updateJourneyWithExistingValue(Left(j), request.body)
                        case _: JourneyStage.AfterStartedPegaCase               =>
                          Errors.throwBadRequestExceptionF(
                            "Not expecting monthly payment amount to be updated after PEGA case started"
                          )
                        case j: JourneyStage.AfterCheckedPaymentPlan            =>
                          updateJourneyWithExistingValue(Right(j), request.body)
                      }
      } yield Ok(newJourney.json)
    }

  private def updateJourneyWithNewValue(
    journey:              Journey.ObtainedCanPayWithinSixMonthsAnswers,
    monthlyPaymentAmount: MonthlyPaymentAmount
  )(using Request[_]): Future[Journey] = {
    val newJourney: Journey =
      journey
        .into[Journey.EnteredMonthlyPaymentAmount]
        .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
        .transform

    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingValue(
    journey:              Either[
      JourneyStage.AfterEnteredMonthlyPaymentAmount & Journey,
      JourneyStage.AfterCheckedPaymentPlan & Journey
    ],
    monthlyPaymentAmount: MonthlyPaymentAmount
  )(using Request[_]): Future[Journey] =
    journey match {
      case Left(afterEnteredMonthlyPaymentAmount) =>
        updateJourneyWithExistingValue(
          afterEnteredMonthlyPaymentAmount.monthlyPaymentAmount,
          afterEnteredMonthlyPaymentAmount,
          monthlyPaymentAmount,
          afterEnteredMonthlyPaymentAmount match {
            case j: Journey.EnteredMonthlyPaymentAmount =>
              j.copy(monthlyPaymentAmount = monthlyPaymentAmount)

            case j: Journey.EnteredDayOfMonth =>
              j.into[Journey.EnteredMonthlyPaymentAmount]
                .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                .transform

            case j: Journey.RetrievedStartDates =>
              j.into[Journey.EnteredMonthlyPaymentAmount]
                .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                .transform

            case j: Journey.RetrievedAffordableQuotes =>
              j.into[Journey.EnteredMonthlyPaymentAmount]
                .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                .transform

            case j: Journey.ChosenPaymentPlan =>
              j.into[Journey.EnteredMonthlyPaymentAmount]
                .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                .transform

          }
        )

      case Right(afterCheckedPaymentPlan) =>
        afterCheckedPaymentPlan.paymentPlanAnswers match {
          case _: PaymentPlanAnswers.PaymentPlanAfterAffordability =>
            Errors.throwBadRequestExceptionF("Cannot update MonthlyPaymentAmount on affordability journey")

          case p: PaymentPlanAnswers.PaymentPlanNoAffordability =>
            updateJourneyWithExistingValue(
              p.monthlyPaymentAmount,
              afterCheckedPaymentPlan,
              monthlyPaymentAmount,
              afterCheckedPaymentPlan match {
                case j: Journey.CheckedPaymentPlan =>
                  j.into[Journey.EnteredMonthlyPaymentAmount]
                    .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                    .transform

                case j: Journey.EnteredCanYouSetUpDirectDebit =>
                  j.into[Journey.EnteredMonthlyPaymentAmount]
                    .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                    .transform

                case j: Journey.EnteredDirectDebitDetails =>
                  j.into[Journey.EnteredMonthlyPaymentAmount]
                    .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                    .transform

                case j: Journey.ConfirmedDirectDebitDetails =>
                  j.into[Journey.EnteredMonthlyPaymentAmount]
                    .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                    .transform

                case j: Journey.AgreedTermsAndConditions =>
                  j.into[Journey.EnteredMonthlyPaymentAmount]
                    .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                    .transform

                case j: Journey.SelectedEmailToBeVerified =>
                  j.into[Journey.EnteredMonthlyPaymentAmount]
                    .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                    .transform

                case j: Journey.EmailVerificationComplete =>
                  j.into[Journey.EnteredMonthlyPaymentAmount]
                    .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                    .transform

                case _: Journey.SubmittedArrangement =>
                  Errors.throwBadRequestException("Cannot update MonthlyAmount when journey is in completed state")
              }
            )
        }
    }

  private def updateJourneyWithExistingValue(
    existingValue:   MonthlyPaymentAmount,
    existingJourney: Journey,
    newValue:        MonthlyPaymentAmount,
    newJourney:      Journey
  )(using Request[_]): Future[Journey] =
    if (existingValue.value == newValue.value) {
      JourneyLogger.info("Nothing to update, selected MonthlyPaymentAmount is the same as the existing one in journey.")
      Future.successful(existingJourney)
    } else {
      journeyService.upsert(newJourney)
    }

}
