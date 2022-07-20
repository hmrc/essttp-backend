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

import cats.instances.boolean._
import cats.syntax.eq._
import com.google.inject.{Inject, Singleton}
import essttp.journey.model.Journey.Stages
import essttp.journey.model.{Journey, JourneyId, Stage}
import essttp.rootmodel.CanPayUpfront
import essttp.utils.Errors
import io.scalaland.chimney.dsl.TransformerOps
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateCanPayUpfrontController @Inject() (
    journeyService: JourneyService,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext) extends BackendController(cc) {

  def updateCanPayUpfront(journeyId: JourneyId): Action[CanPayUpfront] = Action.async(parse.json[CanPayUpfront]) { implicit request =>
    for {
      journey <- journeyService.get(journeyId)
      _ <- journey match {
        case _: Journey.BeforeEligibilityChecked   => Errors.throwBadRequestExceptionF("UpdateCanPayUpfront is not possible in that state.")
        case j: Journey.Stages.EligibilityChecked  => updateJourneyWithNewValue(j, request.body)
        case j: Journey.AfterAnsweredCanPayUpfront => updateJourneyWithExistingValue(Left(j), request.body)
        case j: Journey.AfterUpfrontPaymentAnswers => updateJourneyWithExistingValue(Right(j), request.body)
      }
    } yield Ok
  }

  private def updateJourneyWithNewValue(
      journey:       Stages.EligibilityChecked,
      canPayUpfront: CanPayUpfront
  )(implicit request: Request[_]): Future[Unit] = {
    journey match {
      case j: Journey.Epaye.EligibilityChecked =>
        val newJourney: Journey.Epaye.AnsweredCanPayUpfront =
          j.into[Journey.Epaye.AnsweredCanPayUpfront]
            .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
            .withFieldConst(_.canPayUpfront, canPayUpfront)
            .transform
        journeyService.upsert(newJourney)
    }
  }

  private def updateJourneyWithExistingValue(
      journey:       Either[Journey.AfterAnsweredCanPayUpfront, Journey.AfterUpfrontPaymentAnswers],
      canPayUpfront: CanPayUpfront
  )(implicit request: Request[_]): Future[Unit] = {
    journey match {
      case Left(j: Journey.AfterAnsweredCanPayUpfront) =>
        if (j.canPayUpfront.value === canPayUpfront.value) {
          JourneyLogger.info("Nothing to update, CanPayUpfront is the same as the existing one in journey.")
          Future.successful(())
        } else {
          val updatedJourney: Journey = j match {
            case j1: Journey.Epaye.AnsweredCanPayUpfront =>
              j1.copy(
                stage         = determineCanPayUpFrontEnum(canPayUpfront),
                canPayUpfront = canPayUpfront
              )
            case j1: Journey.Epaye.EnteredUpfrontPaymentAmount =>
              j1.into[Journey.Epaye.AnsweredCanPayUpfront]
                .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
          }
          journeyService.upsert(updatedJourney)
        }
      case Right(j: Journey.AfterUpfrontPaymentAnswers) =>
        val updatedJourney: Journey = j match {
          case j1: Journey.Epaye.EnteredMonthlyPaymentAmount =>
            j1.into[Journey.Epaye.AnsweredCanPayUpfront]
              .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
              .withFieldConst(_.canPayUpfront, canPayUpfront)
              .transform
          case j1: Journey.Epaye.RetrievedExtremeDates =>
            j1.into[Journey.Epaye.AnsweredCanPayUpfront]
              .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
              .withFieldConst(_.canPayUpfront, canPayUpfront)
              .transform
          case j1: Journey.Epaye.RetrievedAffordabilityResult =>
            j1.into[Journey.Epaye.AnsweredCanPayUpfront]
              .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
              .withFieldConst(_.canPayUpfront, canPayUpfront)
              .transform
          case j1: Journey.Epaye.EnteredDayOfMonth =>
            j1.into[Journey.Epaye.AnsweredCanPayUpfront]
              .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
              .withFieldConst(_.canPayUpfront, canPayUpfront)
              .transform
          case j1: Journey.Epaye.RetrievedStartDates =>
            j1.into[Journey.Epaye.AnsweredCanPayUpfront]
              .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
              .withFieldConst(_.canPayUpfront, canPayUpfront)
              .transform
          case j1: Journey.Epaye.RetrievedAffordableQuotes =>
            j1.into[Journey.Epaye.AnsweredCanPayUpfront]
              .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
              .withFieldConst(_.canPayUpfront, canPayUpfront)
              .transform
          case j1: Journey.Epaye.ChosenPaymentPlan =>
            j1.into[Journey.Epaye.AnsweredCanPayUpfront]
              .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
              .withFieldConst(_.canPayUpfront, canPayUpfront)
              .transform
          case j1: Journey.Epaye.CheckedPaymentPlan =>
            j1.into[Journey.Epaye.AnsweredCanPayUpfront]
              .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
              .withFieldConst(_.canPayUpfront, canPayUpfront)
              .transform
          case j1: Journey.Epaye.ChosenTypeOfBankAccount =>
            j1.into[Journey.Epaye.AnsweredCanPayUpfront]
              .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
              .withFieldConst(_.canPayUpfront, canPayUpfront)
              .transform
          case j1: Journey.Epaye.EnteredDirectDebitDetails =>
            j1.into[Journey.Epaye.AnsweredCanPayUpfront]
              .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
              .withFieldConst(_.canPayUpfront, canPayUpfront)
              .transform
          case j1: Journey.Epaye.ConfirmedDirectDebitDetails =>
            j1.into[Journey.Epaye.AnsweredCanPayUpfront]
              .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
              .withFieldConst(_.canPayUpfront, canPayUpfront)
              .transform
          case j1: Journey.Epaye.AgreedTermsAndConditions =>
            j1.into[Journey.Epaye.AnsweredCanPayUpfront]
              .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
              .withFieldConst(_.canPayUpfront, canPayUpfront)
              .transform
          case _: Journey.Epaye.SubmittedArrangement =>
            Errors.throwBadRequestException("Cannot update AnsweredCanPayUpFront when journey is in completed state")
        }

        journeyService.upsert(updatedJourney)
    }
  }

  private def determineCanPayUpFrontEnum(latestCanPayUpfrontValue: CanPayUpfront): Stage.AfterCanPayUpfront = {
    if (latestCanPayUpfrontValue.value) Stage.AfterCanPayUpfront.Yes
    else Stage.AfterCanPayUpfront.No
  }

}
