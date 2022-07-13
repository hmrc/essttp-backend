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
import essttp.journey.model.{Journey, JourneyId, Stage, UpfrontPaymentAnswers}
import essttp.rootmodel.{CanPayUpfront, UpfrontPaymentAmount}
import essttp.utils.Errors
import io.scalaland.chimney.dsl.TransformerOps
import play.api.mvc.{Action, ControllerComponents, Request}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateUpfrontPaymentAmountController @Inject() (
    journeyService: JourneyService,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext) extends BackendController(cc) {

  def updateUpfrontPaymentAmount(journeyId: JourneyId): Action[UpfrontPaymentAmount] = Action.async(parse.json[UpfrontPaymentAmount]) { implicit request =>
    for {
      journey <- journeyService.get(journeyId)
      _ <- journey match {
        case j: Journey.BeforeAnsweredCanPayUpfront      => Errors.throwBadRequestExceptionF(s"UpdateUpfrontPaymentAmount update is not possible in that state: [${j.stage}]")
        case j: Journey.Stages.AnsweredCanPayUpfront     => updateJourneyWithNewValue(j, request.body)
        case j: Journey.AfterEnteredUpfrontPaymentAmount => updateJourneyWithExistingValue(Left(j), request.body)
        case j: Journey.AfterUpfrontPaymentAnswers =>
          j.upfrontPaymentAnswers match {
            case _: UpfrontPaymentAnswers.DeclaredUpfrontPayment => updateJourneyWithExistingValue(Right(j), request.body)
            case UpfrontPaymentAnswers.NoUpfrontPayment          => Errors.throwBadRequestExceptionF(s"UpdateUpfrontPaymentAmount update is not possible in that state: [${j.stage}]")
          }
      }
    } yield Ok
  }

  private def updateJourneyWithNewValue(
      journey:              Stages.AnsweredCanPayUpfront,
      upfrontPaymentAmount: UpfrontPaymentAmount
  )(implicit request: Request[_]): Future[Unit] = {
    if (journey.canPayUpfront.value) {
      journey match {
        case j: Epaye.AnsweredCanPayUpfront =>
          val newJourney: Epaye.EnteredUpfrontPaymentAmount =
            j.into[Epaye.EnteredUpfrontPaymentAmount]
              .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
              .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
              .transform
          journeyService.upsert(newJourney)
      }
    } else {
      Errors.throwBadRequestExceptionF(s"UpdateUpfrontPaymentAmount update is not possible when user has selected [No] for CanPayUpfront: [${journey.stage}]")
    }
  }

  private def updateJourneyWithExistingValue(
      journey:              Either[Journey.AfterEnteredUpfrontPaymentAmount, Journey.AfterUpfrontPaymentAnswers],
      upfrontPaymentAmount: UpfrontPaymentAmount
  )(implicit request: Request[_]): Future[Unit] = {

    journey match {
      case Left(j: Epaye.EnteredUpfrontPaymentAmount) =>
        if (j.upfrontPaymentAmount.value.value === upfrontPaymentAmount.value.value) {
          JourneyLogger.info("Nothing to update, UpfrontPaymentAmount is the same as the existing one in journey.")
          Future.successful(())
        } else {
          val updatedJourney: Epaye.EnteredUpfrontPaymentAmount = j.copy(upfrontPaymentAmount = upfrontPaymentAmount)
          journeyService.upsert(updatedJourney)
        }

      case Right(j) =>
        withUpfrontPaymentAmount(j, j.upfrontPaymentAnswers){ amount =>
          val updatedJourney = j match {
            case j: Epaye.EnteredMonthlyPaymentAmount =>
              j.into[Journey.Epaye.EnteredUpfrontPaymentAmount]
                .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
                .withFieldConst(_.canPayUpfront, CanPayUpfront(true))
                .withFieldConst(_.upfrontPaymentAmount, amount)
                .transform
            case j: Epaye.RetrievedExtremeDates =>
              j.into[Journey.Epaye.EnteredUpfrontPaymentAmount]
                .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
                .withFieldConst(_.canPayUpfront, CanPayUpfront(true))
                .withFieldConst(_.upfrontPaymentAmount, amount)
                .transform
            case j: Epaye.RetrievedAffordabilityResult =>
              j.into[Journey.Epaye.EnteredUpfrontPaymentAmount]
                .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
                .withFieldConst(_.canPayUpfront, CanPayUpfront(true))
                .withFieldConst(_.upfrontPaymentAmount, amount)
                .transform
            case j: Epaye.EnteredDayOfMonth =>
              j.into[Journey.Epaye.EnteredUpfrontPaymentAmount]
                .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
                .withFieldConst(_.canPayUpfront, CanPayUpfront(true))
                .withFieldConst(_.upfrontPaymentAmount, amount)
                .transform
            case j: Epaye.RetrievedStartDates =>
              j.into[Journey.Epaye.EnteredUpfrontPaymentAmount]
                .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
                .withFieldConst(_.canPayUpfront, CanPayUpfront(true))
                .withFieldConst(_.upfrontPaymentAmount, amount)
                .transform
            case j: Epaye.RetrievedAffordableQuotes =>
              j.into[Journey.Epaye.EnteredUpfrontPaymentAmount]
                .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
                .withFieldConst(_.canPayUpfront, CanPayUpfront(true))
                .withFieldConst(_.upfrontPaymentAmount, amount)
                .transform
            case j: Epaye.ChosenPaymentPlan =>
              j.into[Journey.Epaye.EnteredUpfrontPaymentAmount]
                .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
                .withFieldConst(_.canPayUpfront, CanPayUpfront(true))
                .withFieldConst(_.upfrontPaymentAmount, amount)
                .transform
            case j: Epaye.CheckedPaymentPlan =>
              j.into[Journey.Epaye.EnteredUpfrontPaymentAmount]
                .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
                .withFieldConst(_.canPayUpfront, CanPayUpfront(true))
                .withFieldConst(_.upfrontPaymentAmount, amount)
                .transform
            case j: Epaye.ChosenTypeOfBankAccount =>
              j.into[Journey.Epaye.EnteredUpfrontPaymentAmount]
                .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
                .withFieldConst(_.canPayUpfront, CanPayUpfront(true))
                .withFieldConst(_.upfrontPaymentAmount, amount)
                .transform
            case j: Epaye.EnteredDirectDebitDetails =>
              j.into[Journey.Epaye.EnteredUpfrontPaymentAmount]
                .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
                .withFieldConst(_.canPayUpfront, CanPayUpfront(true))
                .withFieldConst(_.upfrontPaymentAmount, amount)
                .transform
            case j: Epaye.ConfirmedDirectDebitDetails =>
              j.into[Journey.Epaye.EnteredUpfrontPaymentAmount]
                .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
                .withFieldConst(_.canPayUpfront, CanPayUpfront(true))
                .withFieldConst(_.upfrontPaymentAmount, amount)
                .transform
            case j: Epaye.AgreedTermsAndConditions =>
              j.into[Journey.Epaye.EnteredUpfrontPaymentAmount]
                .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
                .withFieldConst(_.canPayUpfront, CanPayUpfront(true))
                .withFieldConst(_.upfrontPaymentAmount, amount)
                .transform
          }

          journeyService.upsert(updatedJourney)
        }

    }
  }

  private def withUpfrontPaymentAmount[A](j: Journey, upfrontPaymentAnswers: UpfrontPaymentAnswers)(f: UpfrontPaymentAmount => Future[A]): Future[A] =
    upfrontPaymentAnswers match {
      case UpfrontPaymentAnswers.NoUpfrontPayment =>
        Errors.throwBadRequestExceptionF(s"UpdateUpfrontPaymentAmount update is not possible there is no upfront payment amount before...: [${j.stage}]")

      case UpfrontPaymentAnswers.DeclaredUpfrontPayment(amount) =>
        f(amount)
    }
}
