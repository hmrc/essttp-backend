/*
 * Copyright 2024 HM Revenue & Customs
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
import essttp.journey.model.{CanPayWithinSixMonthsAnswers, Journey, JourneyId, JourneyStage}
import essttp.utils.Errors
import io.scalaland.chimney.dsl.*
import play.api.mvc.{Action, ControllerComponents, Request}
import services.JourneyService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateCanPayWithinSixMonthsController @Inject() (
  actions:        Actions,
  journeyService: JourneyService,
  cc:             ControllerComponents
)(using ExecutionContext, OperationalCryptoFormat)
    extends BackendController(cc) {

  def updateCanPayWithinSixMonthsAnswers(journeyId: JourneyId): Action[CanPayWithinSixMonthsAnswers] =
    actions.authenticatedAction.async(parse.json[CanPayWithinSixMonthsAnswers]) { implicit request =>
      for {
        journey    <- journeyService.get(journeyId)
        newJourney <- journey match {
                        case j: JourneyStage.BeforeRetrievedAffordabilityResult =>
                          Errors.throwBadRequestExceptionF(
                            s"UpdateCanPayWithinSixMonthsAnswers update is not possible in that state: [${j.stage}]"
                          )
                        case j: Journey.RetrievedAffordabilityResult            =>
                          updateJourneyWithNewValue(j, request.body)
                        case j: JourneyStage.AfterCanPayWithinSixMonthsAnswers  =>
                          updateJourneyWithExistingValue(j, request.body)
                      }
      } yield Ok(newJourney.json)
    }

  private def updateJourneyWithNewValue(
    journey: Journey.RetrievedAffordabilityResult,
    answers: CanPayWithinSixMonthsAnswers
  )(using Request[_]): Future[Journey] = {
    val newJourney: Journey =
      journey
        .into[Journey.ObtainedCanPayWithinSixMonthsAnswers]
        .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
        .transform

    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingValue(
    journey: JourneyStage.AfterCanPayWithinSixMonthsAnswers & Journey,
    answers: CanPayWithinSixMonthsAnswers
  )(using Request[_]): Future[Journey] =
    if (journey.canPayWithinSixMonthsAnswers == answers) {
      Future.successful(journey)
    } else {
      val newJourney: Journey = journey match {
        case j: Journey.ObtainedCanPayWithinSixMonthsAnswers =>
          j.copy(canPayWithinSixMonthsAnswers = answers)

        case j: Journey.StartedPegaCase =>
          j.into[Journey.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform

        case j: Journey.EnteredMonthlyPaymentAmount =>
          j.into[Journey.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform

        case j: Journey.EnteredDayOfMonth =>
          j.into[Journey.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform

        case j: Journey.RetrievedStartDates =>
          j.into[Journey.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform

        case j: Journey.RetrievedAffordableQuotes =>
          j.into[Journey.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform

        case j: Journey.ChosenPaymentPlan =>
          j.into[Journey.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform

        case j: Journey.CheckedPaymentPlan =>
          j.into[Journey.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform

        case j: Journey.EnteredCanYouSetUpDirectDebit =>
          j.into[Journey.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform

        case j: Journey.EnteredDirectDebitDetails =>
          j.into[Journey.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform

        case j: Journey.ConfirmedDirectDebitDetails =>
          j.into[Journey.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform

        case j: Journey.AgreedTermsAndConditions =>
          j.into[Journey.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform

        case j: Journey.SelectedEmailToBeVerified =>
          j.into[Journey.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform

        case j: Journey.EmailVerificationComplete =>
          j.into[Journey.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform

        case _: Journey.SubmittedArrangement =>
          Errors.throwBadRequestException(
            "Cannot update CanPayWithinSixMonthsAnswers when journey is in completed state"
          )
      }

      journeyService.upsert(newJourney)
    }

}
