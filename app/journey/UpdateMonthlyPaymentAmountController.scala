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
import essttp.journey.model.Journey.{Epaye, Sa, Sia, Stages, Vat}
import essttp.journey.model.{Journey, JourneyId, PaymentPlanAnswers, Stage}
import essttp.rootmodel.MonthlyPaymentAmount
import essttp.utils.Errors
import io.scalaland.chimney.dsl.TransformationOps
import play.api.mvc.{Action, ControllerComponents, Request}
import services.JourneyService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateMonthlyPaymentAmountController @Inject() (
    actions:        Actions,
    journeyService: JourneyService,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext, cryptoFormat: OperationalCryptoFormat) extends BackendController(cc) {

  def updateMonthlyPaymentAmount(journeyId: JourneyId): Action[MonthlyPaymentAmount] = actions.authenticatedAction.async(parse.json[MonthlyPaymentAmount]) { implicit request =>
    for {
      journey <- journeyService.get(journeyId)
      newJourney <- journey match {
        case j: Journey.BeforeCanPayWithinSixMonthsAnswers          => Errors.throwBadRequestExceptionF(s"UpdateMonthlyPaymentAmount update is not possible in that state: [${j.stage.toString}]")
        case j: Journey.Stages.ObtainedCanPayWithinSixMonthsAnswers => updateJourneyWithNewValue(j, request.body)
        case j: Journey.AfterEnteredMonthlyPaymentAmount            => updateJourneyWithExistingValue(Left(j), request.body)
        case _: Journey.AfterStartedPegaCase                        => Errors.throwBadRequestExceptionF("Not expecting monthly payment amount to be updated after PEGA case started")
        case j: Journey.AfterCheckedPaymentPlan                     => updateJourneyWithExistingValue(Right(j), request.body)
      }
    } yield Ok(newJourney.json)
  }

  private def updateJourneyWithNewValue(
      journey:              Stages.ObtainedCanPayWithinSixMonthsAnswers,
      monthlyPaymentAmount: MonthlyPaymentAmount
  )(implicit request: Request[_]): Future[Journey] = {
    val newJourney: Journey.AfterEnteredMonthlyPaymentAmount = journey match {
      case j: Epaye.ObtainedCanPayWithinSixMonthsAnswers =>
        j.into[Epaye.EnteredMonthlyPaymentAmount]
          .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
          .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
          .transform
      case j: Vat.ObtainedCanPayWithinSixMonthsAnswers =>
        j.into[Vat.EnteredMonthlyPaymentAmount]
          .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
          .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
          .transform
      case j: Sa.ObtainedCanPayWithinSixMonthsAnswers =>
        j.into[Sa.EnteredMonthlyPaymentAmount]
          .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
          .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
          .transform
      case j: Sia.ObtainedCanPayWithinSixMonthsAnswers =>
        j.into[Sia.EnteredMonthlyPaymentAmount]
          .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
          .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
          .transform
    }
    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingValue(
      journey:              Either[Journey.AfterEnteredMonthlyPaymentAmount, Journey.AfterCheckedPaymentPlan],
      monthlyPaymentAmount: MonthlyPaymentAmount
  )(implicit request: Request[_]): Future[Journey] =
    journey match {
      case Left(afterEnteredMonthlyPaymentAmount) =>
        updateJourneyWithExistingValue(
          afterEnteredMonthlyPaymentAmount.monthlyPaymentAmount,
          afterEnteredMonthlyPaymentAmount,
          monthlyPaymentAmount,
          afterEnteredMonthlyPaymentAmount match {
            case j: Epaye.EnteredMonthlyPaymentAmount =>
              j.copy(monthlyPaymentAmount = monthlyPaymentAmount)
            case j: Vat.EnteredMonthlyPaymentAmount =>
              j.copy(monthlyPaymentAmount = monthlyPaymentAmount)
            case j: Sa.EnteredMonthlyPaymentAmount =>
              j.copy(monthlyPaymentAmount = monthlyPaymentAmount)
            case j: Sia.EnteredMonthlyPaymentAmount =>
              j.copy(monthlyPaymentAmount = monthlyPaymentAmount)

            case j: Epaye.EnteredDayOfMonth =>
              j.into[Epaye.EnteredMonthlyPaymentAmount]
                .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
                .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                .transform
            case j: Vat.EnteredDayOfMonth =>
              j.into[Vat.EnteredMonthlyPaymentAmount]
                .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
                .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                .transform
            case j: Sa.EnteredDayOfMonth =>
              j.into[Sa.EnteredMonthlyPaymentAmount]
                .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
                .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                .transform
            case j: Sia.EnteredDayOfMonth =>
              j.into[Sia.EnteredMonthlyPaymentAmount]
                .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
                .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                .transform

            case j: Epaye.RetrievedStartDates =>
              j.into[Epaye.EnteredMonthlyPaymentAmount]
                .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
                .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                .transform
            case j: Vat.RetrievedStartDates =>
              j.into[Vat.EnteredMonthlyPaymentAmount]
                .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
                .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                .transform
            case j: Sa.RetrievedStartDates =>
              j.into[Sa.EnteredMonthlyPaymentAmount]
                .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
                .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                .transform
            case j: Sia.RetrievedStartDates =>
              j.into[Sia.EnteredMonthlyPaymentAmount]
                .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
                .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                .transform

            case j: Epaye.RetrievedAffordableQuotes =>
              j.into[Epaye.EnteredMonthlyPaymentAmount]
                .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
                .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                .transform
            case j: Vat.RetrievedAffordableQuotes =>
              j.into[Vat.EnteredMonthlyPaymentAmount]
                .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
                .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                .transform
            case j: Sa.RetrievedAffordableQuotes =>
              j.into[Sa.EnteredMonthlyPaymentAmount]
                .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
                .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                .transform
            case j: Sia.RetrievedAffordableQuotes =>
              j.into[Sia.EnteredMonthlyPaymentAmount]
                .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
                .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                .transform

            case j: Epaye.ChosenPaymentPlan =>
              j.into[Epaye.EnteredMonthlyPaymentAmount]
                .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
                .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                .transform
            case j: Vat.ChosenPaymentPlan =>
              j.into[Vat.EnteredMonthlyPaymentAmount]
                .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
                .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                .transform
            case j: Sa.ChosenPaymentPlan =>
              j.into[Sa.EnteredMonthlyPaymentAmount]
                .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
                .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                .transform
            case j: Sia.ChosenPaymentPlan =>
              j.into[Sia.EnteredMonthlyPaymentAmount]
                .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
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
                case j: Epaye.CheckedPaymentPlan =>
                  j.into[Epaye.EnteredMonthlyPaymentAmount]
                    .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
                    .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                    .transform
                case j: Vat.CheckedPaymentPlan =>
                  j.into[Vat.EnteredMonthlyPaymentAmount]
                    .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
                    .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                    .transform
                case j: Sa.CheckedPaymentPlan =>
                  j.into[Sa.EnteredMonthlyPaymentAmount]
                    .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
                    .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                    .transform
                case j: Sia.CheckedPaymentPlan =>
                  j.into[Sia.EnteredMonthlyPaymentAmount]
                    .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
                    .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                    .transform

                case j: Epaye.EnteredCanYouSetUpDirectDebit =>
                  j.into[Epaye.EnteredMonthlyPaymentAmount]
                    .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
                    .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                    .transform
                case j: Vat.EnteredCanYouSetUpDirectDebit =>
                  j.into[Vat.EnteredMonthlyPaymentAmount]
                    .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
                    .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                    .transform
                case j: Sa.EnteredCanYouSetUpDirectDebit =>
                  j.into[Sa.EnteredMonthlyPaymentAmount]
                    .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
                    .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                    .transform
                case j: Sia.EnteredCanYouSetUpDirectDebit =>
                  j.into[Sia.EnteredMonthlyPaymentAmount]
                    .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
                    .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                    .transform

                case j: Epaye.EnteredDirectDebitDetails =>
                  j.into[Epaye.EnteredMonthlyPaymentAmount]
                    .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
                    .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                    .transform
                case j: Vat.EnteredDirectDebitDetails =>
                  j.into[Vat.EnteredMonthlyPaymentAmount]
                    .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
                    .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                    .transform
                case j: Sa.EnteredDirectDebitDetails =>
                  j.into[Sa.EnteredMonthlyPaymentAmount]
                    .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
                    .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                    .transform
                case j: Sia.EnteredDirectDebitDetails =>
                  j.into[Sia.EnteredMonthlyPaymentAmount]
                    .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
                    .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                    .transform

                case j: Epaye.ConfirmedDirectDebitDetails =>
                  j.into[Epaye.EnteredMonthlyPaymentAmount]
                    .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
                    .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                    .transform
                case j: Vat.ConfirmedDirectDebitDetails =>
                  j.into[Vat.EnteredMonthlyPaymentAmount]
                    .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
                    .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                    .transform
                case j: Sa.ConfirmedDirectDebitDetails =>
                  j.into[Sa.EnteredMonthlyPaymentAmount]
                    .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
                    .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                    .transform
                case j: Sia.ConfirmedDirectDebitDetails =>
                  j.into[Sia.EnteredMonthlyPaymentAmount]
                    .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
                    .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                    .transform

                case j: Epaye.AgreedTermsAndConditions =>
                  j.into[Epaye.EnteredMonthlyPaymentAmount]
                    .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
                    .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                    .transform
                case j: Vat.AgreedTermsAndConditions =>
                  j.into[Vat.EnteredMonthlyPaymentAmount]
                    .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
                    .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                    .transform
                case j: Sa.AgreedTermsAndConditions =>
                  j.into[Sa.EnteredMonthlyPaymentAmount]
                    .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
                    .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                    .transform
                case j: Sia.AgreedTermsAndConditions =>
                  j.into[Sia.EnteredMonthlyPaymentAmount]
                    .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
                    .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                    .transform

                case j: Epaye.SelectedEmailToBeVerified =>
                  j.into[Epaye.EnteredMonthlyPaymentAmount]
                    .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
                    .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                    .transform
                case j: Vat.SelectedEmailToBeVerified =>
                  j.into[Vat.EnteredMonthlyPaymentAmount]
                    .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
                    .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                    .transform
                case j: Sa.SelectedEmailToBeVerified =>
                  j.into[Sa.EnteredMonthlyPaymentAmount]
                    .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
                    .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                    .transform
                case j: Sia.SelectedEmailToBeVerified =>
                  j.into[Sia.EnteredMonthlyPaymentAmount]
                    .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
                    .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                    .transform

                case j: Epaye.EmailVerificationComplete =>
                  j.into[Epaye.EnteredMonthlyPaymentAmount]
                    .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
                    .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                    .transform
                case j: Vat.EmailVerificationComplete =>
                  j.into[Vat.EnteredMonthlyPaymentAmount]
                    .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
                    .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                    .transform
                case j: Sa.EmailVerificationComplete =>
                  j.into[Sa.EnteredMonthlyPaymentAmount]
                    .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
                    .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                    .transform
                case j: Sia.EmailVerificationComplete =>
                  j.into[Sia.EnteredMonthlyPaymentAmount]
                    .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
                    .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
                    .transform

                case _: Stages.SubmittedArrangement =>
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
  )(implicit request: Request[_]): Future[Journey] =
    if (existingValue.value === newValue.value) {
      JourneyLogger.info("Nothing to update, selected MonthlyPaymentAmount is the same as the existing one in journey.")
      Future.successful(existingJourney)
    } else {
      journeyService.upsert(newJourney)
    }

}
