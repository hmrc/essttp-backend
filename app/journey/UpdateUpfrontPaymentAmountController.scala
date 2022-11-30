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
import essttp.journey.model.{Journey, JourneyId, Stage, UpfrontPaymentAnswers}
import essttp.rootmodel.{CanPayUpfront, UpfrontPaymentAmount}
import essttp.utils.Errors
import io.scalaland.chimney.dsl.TransformerOps
import play.api.mvc.{Action, ControllerComponents, Request}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateUpfrontPaymentAmountController @Inject() (
    actions:        Actions,
    journeyService: JourneyService,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext, cryptoFormat: OperationalCryptoFormat) extends BackendController(cc) {

  def updateUpfrontPaymentAmount(journeyId: JourneyId): Action[UpfrontPaymentAmount] = actions.authenticatedAction.async(parse.json[UpfrontPaymentAmount]) { implicit request =>
    for {
      journey <- journeyService.get(journeyId)
      newJourney <- journey match {
        case j: Journey.BeforeAnsweredCanPayUpfront      => Errors.throwBadRequestExceptionF(s"UpdateUpfrontPaymentAmount update is not possible in that state: [${j.stage.toString}]")
        case j: Journey.Stages.AnsweredCanPayUpfront     => updateJourneyWithNewValue(j, request.body)
        case j: Journey.AfterEnteredUpfrontPaymentAmount => updateJourneyWithExistingValue(Left(j), request.body)
        case j: Journey.AfterUpfrontPaymentAnswers =>
          j.upfrontPaymentAnswers match {
            case _: UpfrontPaymentAnswers.DeclaredUpfrontPayment => updateJourneyWithExistingValue(Right(j), request.body)
            case UpfrontPaymentAnswers.NoUpfrontPayment          => Errors.throwBadRequestExceptionF("UpdateUpfrontPaymentAmount update is not possible when an upfront payment has not been chosen")
          }
      }
    } yield Ok(newJourney.json)
  }

  private def updateJourneyWithNewValue(
      journey:              Stages.AnsweredCanPayUpfront,
      upfrontPaymentAmount: UpfrontPaymentAmount
  )(implicit request: Request[_]): Future[Journey] = {
    if (journey.canPayUpfront.value) {
      val updatedJourney: Journey.AfterEnteredUpfrontPaymentAmount = journey match {
        case j: Epaye.AnsweredCanPayUpfront =>
          j.into[Epaye.EnteredUpfrontPaymentAmount]
            .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
            .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
            .transform
        case j: Vat.AnsweredCanPayUpfront =>
          j.into[Vat.EnteredUpfrontPaymentAmount]
            .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
            .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
            .transform
      }
      journeyService.upsert(updatedJourney)
    } else {
      Errors.throwBadRequestExceptionF(s"UpdateUpfrontPaymentAmount update is not possible when user has selected [No] for CanPayUpfront: [${journey.stage.toString}]")
    }
  }

  private def updateJourneyWithExistingValue(
      journey:              Either[Journey.AfterEnteredUpfrontPaymentAmount, Journey.AfterUpfrontPaymentAnswers],
      upfrontPaymentAmount: UpfrontPaymentAmount
  )(implicit request: Request[_]): Future[Journey] = {

    journey match {
      case Left(j) =>
        if (j.upfrontPaymentAmount.value === upfrontPaymentAmount.value) {
          JourneyLogger.info("Nothing to update, UpfrontPaymentAmount is the same as the existing one in journey.")
          Future.successful(j)
        } else {
          val updatedJourney: Stages.EnteredUpfrontPaymentAmount = j match {
            case j1: Epaye.EnteredUpfrontPaymentAmount => j1.copy(upfrontPaymentAmount = upfrontPaymentAmount)
            case j1: Vat.EnteredUpfrontPaymentAmount   => j1.copy(upfrontPaymentAmount = upfrontPaymentAmount)
          }
          journeyService.upsert(updatedJourney)
        }

      case Right(j) =>
        withUpfrontPaymentAmount(j, j.upfrontPaymentAnswers) { existingAmount =>
          if (existingAmount.value === upfrontPaymentAmount.value) {
            JourneyLogger.info("Nothing to update, UpfrontPaymentAmount is the same as the existing one in journey.")
            Future.successful(j)
          } else {
            val updatedJourney: Stages.EnteredUpfrontPaymentAmount = j match {

              case j: Epaye.EnteredMonthlyPaymentAmount =>
                j.into[Journey.Epaye.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform
              case j: Vat.EnteredMonthlyPaymentAmount =>
                j.into[Journey.Vat.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform

              case j: Epaye.RetrievedExtremeDates =>
                j.into[Journey.Epaye.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform
              case j: Vat.RetrievedExtremeDates =>
                j.into[Journey.Vat.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform

              case j: Epaye.RetrievedAffordabilityResult =>
                j.into[Journey.Epaye.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform
              case j: Vat.RetrievedAffordabilityResult =>
                j.into[Journey.Vat.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform

              case j: Epaye.EnteredDayOfMonth =>
                j.into[Journey.Epaye.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform
              case j: Vat.EnteredDayOfMonth =>
                j.into[Journey.Vat.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform

              case j: Epaye.RetrievedStartDates =>
                j.into[Journey.Epaye.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform
              case j: Vat.RetrievedStartDates =>
                j.into[Journey.Vat.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform

              case j: Epaye.RetrievedAffordableQuotes =>
                j.into[Journey.Epaye.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform
              case j: Vat.RetrievedAffordableQuotes =>
                j.into[Journey.Vat.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform

              case j: Epaye.ChosenPaymentPlan =>
                j.into[Journey.Epaye.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform
              case j: Vat.ChosenPaymentPlan =>
                j.into[Journey.Vat.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform

              case j: Epaye.CheckedPaymentPlan =>
                j.into[Journey.Epaye.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform
              case j: Vat.CheckedPaymentPlan =>
                j.into[Journey.Vat.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform

              case j: Epaye.EnteredDetailsAboutBankAccount =>
                j.into[Journey.Epaye.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform
              case j: Vat.EnteredDetailsAboutBankAccount =>
                j.into[Journey.Vat.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform

              case j: Epaye.EnteredDirectDebitDetails =>
                j.into[Journey.Epaye.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform
              case j: Vat.EnteredDirectDebitDetails =>
                j.into[Journey.Vat.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform

              case j: Epaye.ConfirmedDirectDebitDetails =>
                j.into[Journey.Epaye.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform
              case j: Vat.ConfirmedDirectDebitDetails =>
                j.into[Journey.Vat.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform

              case j: Epaye.AgreedTermsAndConditions =>
                j.into[Journey.Epaye.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform
              case j: Vat.AgreedTermsAndConditions =>
                j.into[Journey.Vat.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform

              case j: Epaye.SelectedEmailToBeVerified =>
                j.into[Journey.Epaye.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform
              case j: Vat.SelectedEmailToBeVerified =>
                j.into[Journey.Vat.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform

              case j: Epaye.EmailVerificationComplete =>
                j.into[Journey.Epaye.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform
              case j: Vat.EmailVerificationComplete =>
                j.into[Journey.Vat.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform

              case _: Stages.SubmittedArrangement =>
                Errors.throwBadRequestException("Cannot update UpfrontPaymentAmount when journey is in completed state")
            }

            journeyService.upsert(updatedJourney)
          }
        }
    }
  }

  private def withUpfrontPaymentAmount[A](j: Journey, upfrontPaymentAnswers: UpfrontPaymentAnswers)(f: UpfrontPaymentAmount => Future[A]): Future[A] =
    upfrontPaymentAnswers match {
      case UpfrontPaymentAnswers.NoUpfrontPayment =>
        Errors.throwBadRequestExceptionF(s"UpdateUpfrontPaymentAmount update is not possible there is no upfront payment amount before...: [${j.stage.toString}]")

      case UpfrontPaymentAnswers.DeclaredUpfrontPayment(amount) =>
        f(amount)
    }
}
