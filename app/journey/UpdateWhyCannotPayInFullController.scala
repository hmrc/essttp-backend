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
import essttp.journey.model.{Journey, JourneyId, JourneyStage, WhyCannotPayInFullAnswers}
import essttp.utils.Errors
import io.scalaland.chimney.dsl.*
import play.api.mvc.{Action, ControllerComponents, Request}
import services.JourneyService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateWhyCannotPayInFullController @Inject() (
  actions:        Actions,
  journeyService: JourneyService,
  cc:             ControllerComponents
)(using ExecutionContext, OperationalCryptoFormat)
    extends BackendController(cc) {

  def updateWhyCannotPayinFull(journeyId: JourneyId): Action[WhyCannotPayInFullAnswers] =
    actions.authenticatedAction.async(parse.json[WhyCannotPayInFullAnswers]) { implicit request =>
      for {
        journey    <- journeyService.get(journeyId)
        newJourney <- journey match {
                        case _: JourneyStage.BeforeEligibilityChecked =>
                          Errors.throwBadRequestExceptionF(
                            "WhyCannotPayInFullAnswers update is not possible in that state."
                          )

                        case j: Journey.EligibilityChecked =>
                          updateJourneyWithNewValue(j, request.body)

                        case j: JourneyStage.AfterWhyCannotPayInFullAnswers =>
                          j match {
                            case j: JourneyStage.BeforeArrangementSubmitted =>
                              updateJourneyWithExistingValue(j, request.body)
                            case _: JourneyStage.AfterArrangementSubmitted  =>
                              Errors.throwBadRequestExceptionF(
                                "Cannot update WhyCannotPayInFullAnswers when journey is in completed state"
                              )
                          }
                      }
      } yield Ok(newJourney.json)
    }

  private def updateJourneyWithNewValue(
    journey:                   Journey.EligibilityChecked,
    whyCannotPayInFullAnswers: WhyCannotPayInFullAnswers
  )(using Request[_]): Future[Journey] = {
    val newJourney: Journey =
      journey
        .into[Journey.ObtainedWhyCannotPayInFullAnswers]
        .withFieldConst(_.whyCannotPayInFullAnswers, whyCannotPayInFullAnswers)
        .transform

    journeyService.upsert(newJourney)
  }

  // don't need to wipe answers subsequent to the one being updated so can just use .copy and leave the journey in the same stage
  private def updateJourneyWithExistingValue(
    journey:                   JourneyStage.AfterWhyCannotPayInFullAnswers & Journey,
    whyCannotPayInFullAnswers: WhyCannotPayInFullAnswers
  )(using Request[_]): Future[Journey] =
    if (journey.whyCannotPayInFullAnswers == whyCannotPayInFullAnswers) {
      Future.successful(journey)
    } else {
      val newJourney: Journey = journey match {
        case j: Journey.ObtainedWhyCannotPayInFullAnswers =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)

        case j: Journey.AnsweredCanPayUpfront =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)

        case j: Journey.EnteredUpfrontPaymentAmount =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)

        case j: Journey.RetrievedExtremeDates =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)

        case j: Journey.RetrievedAffordabilityResult =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)

        case j: Journey.ObtainedCanPayWithinSixMonthsAnswers =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)

        case j: Journey.StartedPegaCase =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)

        case j: Journey.EnteredMonthlyPaymentAmount =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)

        case j: Journey.EnteredDayOfMonth =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)

        case j: Journey.RetrievedStartDates =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)

        case j: Journey.RetrievedAffordableQuotes =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)

        case j: Journey.ChosenPaymentPlan =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)

        case j: Journey.CheckedPaymentPlan =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)

        case j: Journey.EnteredCanYouSetUpDirectDebit =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)

        case j: Journey.EnteredDirectDebitDetails =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)

        case j: Journey.ConfirmedDirectDebitDetails =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)

        case j: Journey.AgreedTermsAndConditions =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)

        case j: Journey.SelectedEmailToBeVerified =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)

        case j: Journey.EmailVerificationComplete =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)

        case _: Journey.SubmittedArrangement =>
          Errors.throwBadRequestException("Cannot update WhyCannotPayInFullAnswers when journey is in completed state")
      }

      journeyService.upsert(newJourney)
    }

}
