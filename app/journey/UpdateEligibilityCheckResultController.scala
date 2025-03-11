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
import essttp.journey.model._
import essttp.rootmodel.ttp.eligibility.EligibilityCheckResult
import essttp.utils.Errors
import io.scalaland.chimney.dsl.*
import play.api.mvc._
import services.JourneyService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateEligibilityCheckResultController @Inject() (
  actions:        Actions,
  journeyService: JourneyService,
  cc:             ControllerComponents
)(using ExecutionContext, OperationalCryptoFormat)
    extends BackendController(cc) {

  def updateEligibilityResult(journeyId: JourneyId): Action[EligibilityCheckResult] =
    actions.authenticatedAction.async(parse.json[EligibilityCheckResult]) { implicit request =>
      for {
        journey    <- journeyService.get(journeyId)
        newJourney <- journey match {
                        case _: JourneyStage.BeforeComputedTaxId     =>
                          Errors.throwBadRequestExceptionF(
                            "EligibilityCheckResult update is not possible in that state."
                          )
                        case j: Journey.ComputedTaxId                =>
                          updateJourneyWithNewValue(j, request.body)
                        case j: JourneyStage.AfterEligibilityChecked =>
                          j match {
                            case j: JourneyStage.BeforeArrangementSubmitted =>
                              updateJourneyWithExistingValue(j, request.body)
                            case _: JourneyStage.AfterArrangementSubmitted  =>
                              Errors.throwBadRequestExceptionF(
                                "Cannot update EligibilityCheckResult when journey is in completed state"
                              )
                          }
                      }
      } yield Ok(newJourney.json)
    }

  private def updateJourneyWithNewValue(
    journey:                Journey.ComputedTaxId & Journey,
    eligibilityCheckResult: EligibilityCheckResult
  )(using Request[?]): Future[Journey] = {
    val newJourney = journey
      .into[Journey.EligibilityChecked]
      .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
      .transform

    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingValue(
    journey:                JourneyStage.AfterEligibilityChecked & Journey,
    eligibilityCheckResult: EligibilityCheckResult
  )(using Request[?]): Future[Journey] = {
    if (journey.eligibilityCheckResult == eligibilityCheckResult) {
      JourneyLogger.info("Nothing to update, EligibilityCheckResult is the same as the existing one in journey.")
      Future.successful(journey)
    } else {
      val updatedJourney: Journey = journey match {
        case j: Journey.EligibilityChecked =>
          j.copy(eligibilityCheckResult = eligibilityCheckResult)

        case j: Journey.ObtainedWhyCannotPayInFullAnswers =>
          j.into[Journey.EligibilityChecked]
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform

        case j: Journey.AnsweredCanPayUpfront =>
          j.into[Journey.EligibilityChecked]
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform

        case j: Journey.EnteredUpfrontPaymentAmount =>
          j.into[Journey.EligibilityChecked]
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform

        case j: Journey.RetrievedExtremeDates =>
          j.into[Journey.EligibilityChecked]
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform

        case j: Journey.RetrievedAffordabilityResult =>
          j.into[Journey.EligibilityChecked]
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform

        case j: Journey.ObtainedCanPayWithinSixMonthsAnswers =>
          j.into[Journey.EligibilityChecked]
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform

        case j: Journey.StartedPegaCase =>
          j.into[Journey.EligibilityChecked]
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform

        case j: Journey.EnteredMonthlyPaymentAmount =>
          j.into[Journey.EligibilityChecked]
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform

        case j: Journey.EnteredDayOfMonth =>
          j.into[Journey.EligibilityChecked]
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform

        case j: Journey.RetrievedStartDates =>
          j.into[Journey.EligibilityChecked]
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform

        case j: Journey.RetrievedAffordableQuotes =>
          j.into[Journey.EligibilityChecked]
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform

        case j: Journey.ChosenPaymentPlan =>
          j.into[Journey.EligibilityChecked]
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform

        case j: Journey.CheckedPaymentPlan =>
          j.into[Journey.EligibilityChecked]
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform

        case j: Journey.EnteredCanYouSetUpDirectDebit =>
          j.into[Journey.EligibilityChecked]
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform

        case j: Journey.EnteredDirectDebitDetails =>
          j.into[Journey.EligibilityChecked]
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform

        case j: Journey.ConfirmedDirectDebitDetails =>
          j.into[Journey.EligibilityChecked]
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform

        case j: Journey.AgreedTermsAndConditions =>
          j.into[Journey.EligibilityChecked]
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform

        case j: Journey.SelectedEmailToBeVerified =>
          j.into[Journey.EligibilityChecked]
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform

        case j: Journey.EmailVerificationComplete =>
          j.into[Journey.EligibilityChecked]
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform

        case _: Journey.SubmittedArrangement =>
          Errors.throwBadRequestException("Cannot update Eligibility when journey is in completed state")
      }

      journeyService.upsert(updatedJourney)
    }
  }

}
