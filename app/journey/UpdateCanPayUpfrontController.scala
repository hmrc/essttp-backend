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
import essttp.journey.model
import essttp.journey.model.{Journey, JourneyId, JourneyStage, UpfrontPaymentAnswers}
import essttp.rootmodel.CanPayUpfront
import essttp.utils.Errors
import io.scalaland.chimney.dsl.*
import play.api.mvc.*
import services.JourneyService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateCanPayUpfrontController @Inject() (
  actions:        Actions,
  journeyService: JourneyService,
  cc:             ControllerComponents
)(using ExecutionContext, OperationalCryptoFormat)
    extends BackendController(cc) {

  def updateCanPayUpfront(journeyId: JourneyId): Action[CanPayUpfront] =
    actions.authenticatedAction.async(parse.json[CanPayUpfront]) { implicit request =>
      for {
        journey    <- journeyService.get(journeyId)
        newJourney <- journey match {
                        case _: JourneyStage.BeforeWhyCannotPayInFullAnswers    =>
                          Errors.throwBadRequestExceptionF("UpdateCanPayUpfront is not possible in that state.")
                        case j: model.Journey.ObtainedWhyCannotPayInFullAnswers =>
                          updateJourneyWithNewValue(j, request.body)
                        case j: JourneyStage.AfterAnsweredCanPayUpfront         =>
                          updateJourneyWithExistingValue(Left(j), request.body)
                        case j: JourneyStage.AfterUpfrontPaymentAnswers         =>
                          updateJourneyWithExistingValue(Right(j), request.body)
                      }
      } yield Ok(newJourney.json)
    }

  private def updateJourneyWithNewValue(
    journey:       Journey.ObtainedWhyCannotPayInFullAnswers,
    canPayUpfront: CanPayUpfront
  )(using Request[_]): Future[Journey] = {
    val updatedJourney: Journey =
      journey
        .into[Journey.AnsweredCanPayUpfront]
        .withFieldConst(_.canPayUpfront, canPayUpfront)
        .transform

    journeyService.upsert(updatedJourney)
  }

  private def updateJourneyWithExistingValue(
    journey:       Either[
      JourneyStage.AfterAnsweredCanPayUpfront & Journey,
      JourneyStage.AfterUpfrontPaymentAnswers & Journey
    ],
    canPayUpfront: CanPayUpfront
  )(using Request[_]): Future[Journey] = {
    journey match {
      case Left(j)  =>
        if (j.canPayUpfront.value == canPayUpfront.value) {
          JourneyLogger.info("Nothing to update, CanPayUpfront is the same as the existing one in journey.")
          Future.successful(j)
        } else {
          val updatedJourney: Journey = j match {
            case j1: Journey.AnsweredCanPayUpfront =>
              j1.copy(canPayUpfront = canPayUpfront)

            case j1: Journey.EnteredUpfrontPaymentAmount =>
              j1.into[Journey.AnsweredCanPayUpfront]
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
          }
          journeyService.upsert(updatedJourney)
        }
      case Right(j) =>
        val existingCanPayUpfront                                        =
          j.upfrontPaymentAnswers match {
            case UpfrontPaymentAnswers.NoUpfrontPayment          => CanPayUpfront(value = false)
            case _: UpfrontPaymentAnswers.DeclaredUpfrontPayment => CanPayUpfront(value = true)
          }
        def upsertIfChanged(updatedJourney: => Journey): Future[Journey] =
          if (canPayUpfront.value == existingCanPayUpfront.value) {
            JourneyLogger.info("Nothing to update, CanPayUpfront is the same as the existing one in journey.")
            Future.successful(j)
          } else journeyService.upsert(updatedJourney)

        j match {
          case j1: Journey.EnteredMonthlyPaymentAmount =>
            upsertIfChanged(
              j1.into[Journey.AnsweredCanPayUpfront]
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )

          case j1: Journey.RetrievedExtremeDates =>
            upsertIfChanged(
              j1.into[Journey.AnsweredCanPayUpfront]
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )

          case j1: Journey.RetrievedAffordabilityResult =>
            upsertIfChanged(
              j1.into[Journey.AnsweredCanPayUpfront]
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )

          case j1: Journey.ObtainedCanPayWithinSixMonthsAnswers =>
            upsertIfChanged(
              j1.into[Journey.AnsweredCanPayUpfront]
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )

          case j1: Journey.StartedPegaCase =>
            upsertIfChanged(
              j1.into[Journey.AnsweredCanPayUpfront]
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )

          case j1: Journey.EnteredDayOfMonth =>
            upsertIfChanged(
              j1.into[Journey.AnsweredCanPayUpfront]
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )

          case j1: Journey.RetrievedStartDates =>
            upsertIfChanged(
              j1.into[Journey.AnsweredCanPayUpfront]
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )

          case j1: Journey.RetrievedAffordableQuotes =>
            upsertIfChanged(
              j1.into[Journey.AnsweredCanPayUpfront]
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )

          case j1: Journey.ChosenPaymentPlan =>
            upsertIfChanged(
              j1.into[Journey.AnsweredCanPayUpfront]
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )

          case j1: Journey.CheckedPaymentPlan =>
            upsertIfChanged(
              j1.into[Journey.AnsweredCanPayUpfront]
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )

          case j1: Journey.EnteredCanYouSetUpDirectDebit =>
            upsertIfChanged(
              j1.into[Journey.AnsweredCanPayUpfront]
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )

          case j1: Journey.EnteredDirectDebitDetails =>
            upsertIfChanged(
              j1.into[Journey.AnsweredCanPayUpfront]
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )

          case j1: Journey.ConfirmedDirectDebitDetails =>
            upsertIfChanged(
              j1.into[Journey.AnsweredCanPayUpfront]
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )

          case j1: Journey.AgreedTermsAndConditions =>
            upsertIfChanged(
              j1.into[Journey.AnsweredCanPayUpfront]
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )

          case j1: Journey.SelectedEmailToBeVerified =>
            upsertIfChanged(
              j1.into[Journey.AnsweredCanPayUpfront]
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )

          case j1: Journey.EmailVerificationComplete =>
            upsertIfChanged(
              j1.into[Journey.AnsweredCanPayUpfront]
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )

          case _: model.Journey.SubmittedArrangement =>
            Errors.throwBadRequestException("Cannot update AnsweredCanPayUpFront when journey is in completed state")
        }
    }
  }

}
