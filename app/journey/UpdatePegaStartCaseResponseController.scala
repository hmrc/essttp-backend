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
import essttp.rootmodel.pega.StartCaseResponse
import essttp.utils.Errors
import io.scalaland.chimney.dsl.*
import play.api.mvc.{Action, ControllerComponents, Request}
import services.JourneyService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdatePegaStartCaseResponseController @Inject() (
  actions:        Actions,
  journeyService: JourneyService,
  cc:             ControllerComponents
)(using ExecutionContext, OperationalCryptoFormat)
    extends BackendController(cc) {

  def updateStartCaseResponse(journeyId: JourneyId): Action[StartCaseResponse] =
    actions.authenticatedAction.async(parse.json[StartCaseResponse]) { implicit request =>
      for {
        journey    <- journeyService.get(journeyId)
        newJourney <- journey match {
                        case j: JourneyStage.BeforeCanPayWithinSixMonthsAnswers =>
                          Errors.throwBadRequestExceptionF(
                            s"UpdatePegaStartCaseResponse update is not possible in that state: [${j.stage}]"
                          )
                        case j: Journey.ObtainedCanPayWithinSixMonthsAnswers    =>
                          updateJourneyWithNewValue(j, request.body)
                        case j: JourneyStage.AfterStartedPegaCase               =>
                          updateJourneyWithExistingValue(Left(j), request.body)
                        case _: JourneyStage.AfterEnteredMonthlyPaymentAmount   =>
                          Errors.throwBadRequestExceptionF(
                            "update PEGA start case response not expected after entered monthly payment amount"
                          )
                        case j: JourneyStage.AfterCheckedPaymentPlan            =>
                          updateJourneyWithExistingValue(Right(j), request.body)
                      }
      } yield Ok(newJourney.json)
    }

  private def updateJourneyWithNewValue(
    journey:           Journey.ObtainedCanPayWithinSixMonthsAnswers,
    startCaseResponse: StartCaseResponse
  )(using Request[?]): Future[Journey] = {
    val newJourney: Journey =
      journey
        .into[Journey.StartedPegaCase]
        .withFieldConst(_.startCaseResponse, startCaseResponse)
        .withFieldConst(_.pegaCaseId, Some(startCaseResponse.caseId))
        .transform

    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingValue(
    journey:           Either[JourneyStage.AfterStartedPegaCase & Journey, JourneyStage.AfterCheckedPaymentPlan & Journey],
    startCaseResponse: StartCaseResponse
  )(using Request[?]): Future[Journey] =
    journey match {
      case Left(afterStartedPegaCase) =>
        updateJourneyWithExistingValue(
          afterStartedPegaCase.startCaseResponse,
          afterStartedPegaCase,
          startCaseResponse,
          afterStartedPegaCase match {
            case j: Journey.StartedPegaCase =>
              j.copy(startCaseResponse = startCaseResponse)
          }
        )

      case Right(afterCheckedPaymentPlan) =>
        afterCheckedPaymentPlan.paymentPlanAnswers match {
          case _: PaymentPlanAnswers.PaymentPlanNoAffordability =>
            sys.error("Cannot update PEGA StartCaseEResponse on affordability journey")

          case p: PaymentPlanAnswers.PaymentPlanAfterAffordability =>
            updateJourneyWithExistingValue(
              p.startCaseResponse,
              afterCheckedPaymentPlan,
              startCaseResponse,
              afterCheckedPaymentPlan match {
                case j: Journey.CheckedPaymentPlan =>
                  j.into[Journey.StartedPegaCase]
                    .withFieldConst(_.startCaseResponse, startCaseResponse)
                    .withFieldConst(_.pegaCaseId, Some(startCaseResponse.caseId))
                    .transform

                case j: Journey.EnteredCanYouSetUpDirectDebit =>
                  j.into[Journey.StartedPegaCase]
                    .withFieldConst(_.startCaseResponse, startCaseResponse)
                    .withFieldConst(_.pegaCaseId, Some(startCaseResponse.caseId))
                    .transform

                case j: Journey.ChosenTypeOfBankAccount =>
                  j.into[Journey.StartedPegaCase]
                    .withFieldConst(_.startCaseResponse, startCaseResponse)
                    .withFieldConst(_.pegaCaseId, Some(startCaseResponse.caseId))
                    .transform

                case j: Journey.EnteredDirectDebitDetails =>
                  j.into[Journey.StartedPegaCase]
                    .withFieldConst(_.startCaseResponse, startCaseResponse)
                    .withFieldConst(_.pegaCaseId, Some(startCaseResponse.caseId))
                    .transform

                case j: Journey.ConfirmedDirectDebitDetails =>
                  j.into[Journey.StartedPegaCase]
                    .withFieldConst(_.startCaseResponse, startCaseResponse)
                    .withFieldConst(_.pegaCaseId, Some(startCaseResponse.caseId))
                    .transform

                case j: Journey.AgreedTermsAndConditions =>
                  j.into[Journey.StartedPegaCase]
                    .withFieldConst(_.startCaseResponse, startCaseResponse)
                    .withFieldConst(_.pegaCaseId, Some(startCaseResponse.caseId))
                    .transform

                case j: Journey.SelectedEmailToBeVerified =>
                  j.into[Journey.StartedPegaCase]
                    .withFieldConst(_.startCaseResponse, startCaseResponse)
                    .withFieldConst(_.pegaCaseId, Some(startCaseResponse.caseId))
                    .transform

                case j: Journey.EmailVerificationComplete =>
                  j.into[Journey.StartedPegaCase]
                    .withFieldConst(_.startCaseResponse, startCaseResponse)
                    .withFieldConst(_.pegaCaseId, Some(startCaseResponse.caseId))
                    .transform

                case _: Journey.SubmittedArrangement =>
                  Errors.throwBadRequestException(
                    "Cannot update PEGA StartCaseResponse when journey is in completed state"
                  )
              }
            )
        }
    }

  private def updateJourneyWithExistingValue(
    existingValue:   StartCaseResponse,
    existingJourney: Journey,
    newValue:        StartCaseResponse,
    newJourney:      Journey
  )(using Request[?]): Future[Journey] =
    if (existingValue == newValue) {
      JourneyLogger.info("Nothing to update, PEGA StartCaseResponse is the same as the existing one in journey.")
      Future.successful(existingJourney)
    } else {
      journeyService.upsert(newJourney)
    }

}
