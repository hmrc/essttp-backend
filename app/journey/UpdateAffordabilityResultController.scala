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
import essttp.journey.model.{Journey, JourneyId, JourneyStage}
import essttp.rootmodel.ttp.affordability.InstalmentAmounts
import essttp.utils.Errors
import io.scalaland.chimney.dsl.*
import play.api.mvc.{Action, ControllerComponents, Request}
import services.JourneyService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateAffordabilityResultController @Inject() (
  actions:        Actions,
  journeyService: JourneyService,
  cc:             ControllerComponents
)(using ExecutionContext, OperationalCryptoFormat)
    extends BackendController(cc) {
  def updateAffordabilityResult(journeyId: JourneyId): Action[InstalmentAmounts] =
    actions.authenticatedAction.async(parse.json[InstalmentAmounts]) { implicit request =>
      for {
        journey    <- journeyService.get(journeyId)
        newJourney <- journey match {
                        case j: JourneyStage.BeforeExtremeDatesResponse =>
                          Errors.throwBadRequestExceptionF(
                            s"UpdateAffordabilityResult update is not possible in that state: [${j.stage}]"
                          )
                        case j: Journey.RetrievedExtremeDates           =>
                          updateJourneyWithNewValue(j, request.body)

                        case j: JourneyStage.AfterRetrievedAffordabilityResult =>
                          j match {
                            case _: JourneyStage.BeforeArrangementSubmitted =>
                              updateJourneyWithExistingValue(j, request.body)
                            case _: JourneyStage.AfterArrangementSubmitted  =>
                              Errors.throwBadRequestExceptionF(
                                "Cannot update AffordabilityResult when journey is in completed state"
                              )
                          }
                      }
      } yield Ok(newJourney.json)
    }

  private def updateJourneyWithNewValue(
    journey:           Journey.RetrievedExtremeDates,
    instalmentAmounts: InstalmentAmounts
  )(using Request[?]): Future[Journey] = {
    val newJourney: Journey =
      journey
        .into[Journey.RetrievedAffordabilityResult]
        .withFieldConst(_.instalmentAmounts, instalmentAmounts)
        .transform

    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingValue(
    journey:           JourneyStage.AfterRetrievedAffordabilityResult & Journey,
    instalmentAmounts: InstalmentAmounts
  )(using Request[?]): Future[Journey] =
    if (journey.instalmentAmounts == instalmentAmounts) {
      JourneyLogger.info("Nothing to update, InstalmentAmounts is the same as the existing one in journey.")
      Future.successful(journey)
    } else {
      val newJourney: Journey = journey match {
        case j: Journey.RetrievedAffordabilityResult => j.copy(instalmentAmounts = instalmentAmounts)

        case j: Journey.ObtainedCanPayWithinSixMonthsAnswers =>
          j.into[Journey.RetrievedAffordabilityResult]
            .withFieldConst(_.instalmentAmounts, instalmentAmounts)
            .transform

        case j: Journey.StartedPegaCase =>
          j.into[Journey.RetrievedAffordabilityResult]
            .withFieldConst(_.instalmentAmounts, instalmentAmounts)
            .transform

        case j: Journey.EnteredMonthlyPaymentAmount =>
          j.into[Journey.RetrievedAffordabilityResult]
            .withFieldConst(_.instalmentAmounts, instalmentAmounts)
            .transform

        case j: Journey.EnteredDayOfMonth =>
          j.into[Journey.RetrievedAffordabilityResult]
            .withFieldConst(_.instalmentAmounts, instalmentAmounts)
            .transform

        case j: Journey.RetrievedStartDates =>
          j.into[Journey.RetrievedAffordabilityResult]
            .withFieldConst(_.instalmentAmounts, instalmentAmounts)
            .transform

        case j: Journey.RetrievedAffordableQuotes =>
          j.into[Journey.RetrievedAffordabilityResult]
            .withFieldConst(_.instalmentAmounts, instalmentAmounts)
            .transform

        case j: Journey.ChosenPaymentPlan =>
          j.into[Journey.RetrievedAffordabilityResult]
            .withFieldConst(_.instalmentAmounts, instalmentAmounts)
            .transform

        case j: Journey.CheckedPaymentPlan =>
          j.into[Journey.RetrievedAffordabilityResult]
            .withFieldConst(_.instalmentAmounts, instalmentAmounts)
            .transform

        case j: Journey.EnteredCanYouSetUpDirectDebit =>
          j.into[Journey.RetrievedAffordabilityResult]
            .withFieldConst(_.instalmentAmounts, instalmentAmounts)
            .transform

        case j: Journey.ChosenTypeOfBankAccount =>
          j.into[Journey.RetrievedAffordabilityResult]
            .withFieldConst(_.instalmentAmounts, instalmentAmounts)
            .transform

        case j: Journey.EnteredDirectDebitDetails =>
          j.into[Journey.RetrievedAffordabilityResult]
            .withFieldConst(_.instalmentAmounts, instalmentAmounts)
            .transform

        case j: Journey.ConfirmedDirectDebitDetails =>
          j.into[Journey.RetrievedAffordabilityResult]
            .withFieldConst(_.instalmentAmounts, instalmentAmounts)
            .transform

        case j: Journey.AgreedTermsAndConditions =>
          j.into[Journey.RetrievedAffordabilityResult]
            .withFieldConst(_.instalmentAmounts, instalmentAmounts)
            .transform

        case j: Journey.SelectedEmailToBeVerified =>
          j.into[Journey.RetrievedAffordabilityResult]
            .withFieldConst(_.instalmentAmounts, instalmentAmounts)
            .transform

        case j: Journey.EmailVerificationComplete =>
          j.into[Journey.RetrievedAffordabilityResult]
            .withFieldConst(_.instalmentAmounts, instalmentAmounts)
            .transform

        case _: Journey.SubmittedArrangement =>
          Errors.throwBadRequestException("Cannot update AffordabilityResult when journey is in completed state")
      }
      journeyService.upsert(newJourney)
    }

}
