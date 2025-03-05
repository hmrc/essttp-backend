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
import essttp.journey.model.{Journey, JourneyId, JourneyStage, PaymentPlanAnswers, UpfrontPaymentAnswers}
import essttp.rootmodel.dates.extremedates.ExtremeDatesResponse
import essttp.rootmodel.dates.startdates.StartDatesResponse
import essttp.utils.Errors
import io.scalaland.chimney.dsl.*
import play.api.mvc.{Action, ControllerComponents, Request}
import services.JourneyService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateDatesController @Inject() (
  actions:        Actions,
  journeyService: JourneyService,
  cc:             ControllerComponents
)(using ExecutionContext, OperationalCryptoFormat)
    extends BackendController(cc) {

  def updateExtremeDates(journeyId: JourneyId): Action[ExtremeDatesResponse] =
    actions.authenticatedAction.async(parse.json[ExtremeDatesResponse]) { implicit request =>
      for {
        journey    <- journeyService.get(journeyId)
        newJourney <- journey match {
                        case j: Journey.EnteredUpfrontPaymentAmount      =>
                          updateJourneyWithNewExtremeDatesValue(Right(j), request.body)
                        case j: Journey.AnsweredCanPayUpfront            =>
                          updateJourneyWithNewExtremeDatesValue(Left(j), request.body)
                        case j: JourneyStage.AfterExtremeDatesResponse   =>
                          j match {
                            case _: JourneyStage.BeforeArrangementSubmitted =>
                              updateJourneyWithExistingExtremeDatesValue(j, request.body)
                            case _: JourneyStage.AfterArrangementSubmitted  =>
                              Errors.throwBadRequestExceptionF(
                                "Cannot update ExtremeDates when journey is in completed state"
                              )
                          }
                        case j: JourneyStage.BeforeUpfrontPaymentAnswers =>
                          Errors.throwBadRequestExceptionF(
                            s"UpdateExtremeDatesResponse update is not possible in that state: [${j.stage}]"
                          )
                      }
      } yield Ok(newJourney.json)
    }

  private def updateJourneyWithNewExtremeDatesValue(
    journey:              Either[Journey.AnsweredCanPayUpfront, Journey.EnteredUpfrontPaymentAmount],
    extremeDatesResponse: ExtremeDatesResponse
  )(using Request[_]): Future[Journey] = {
    val newJourney: Journey = journey match {
      case Left(j: Journey.AnsweredCanPayUpfront) =>
        j.into[Journey.RetrievedExtremeDates]
          .withFieldConst(_.upfrontPaymentAnswers, UpfrontPaymentAnswers.NoUpfrontPayment)
          .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
          .transform

      case Right(j: Journey.EnteredUpfrontPaymentAmount) =>
        j.into[Journey.RetrievedExtremeDates]
          .withFieldConst(_.upfrontPaymentAnswers, UpfrontPaymentAnswers.DeclaredUpfrontPayment(j.upfrontPaymentAmount))
          .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
          .transform
    }
    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingExtremeDatesValue(
    journey:              JourneyStage.AfterExtremeDatesResponse & Journey,
    extremeDatesResponse: ExtremeDatesResponse
  )(using Request[_]): Future[Journey] =
    if (journey.extremeDatesResponse == extremeDatesResponse) {
      JourneyLogger.info("Nothing to update, ExtremeDatesResponse is the same as the existing one in journey.")
      Future.successful(journey)
    } else {
      val newJourney: Journey = journey match {
        case j: Journey.RetrievedExtremeDates => j.copy(extremeDatesResponse = extremeDatesResponse)

        case j: Journey.RetrievedAffordabilityResult =>
          j.into[Journey.RetrievedExtremeDates]
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform

        case j: Journey.ObtainedCanPayWithinSixMonthsAnswers =>
          j.into[Journey.RetrievedExtremeDates]
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform

        case j: Journey.StartedPegaCase =>
          j.into[Journey.RetrievedExtremeDates]
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform

        case j: Journey.EnteredMonthlyPaymentAmount =>
          j.into[Journey.RetrievedExtremeDates]
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform

        case j: Journey.EnteredDayOfMonth =>
          j.into[Journey.RetrievedExtremeDates]
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform

        case j: Journey.RetrievedStartDates =>
          j.into[Journey.RetrievedExtremeDates]
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform

        case j: Journey.RetrievedAffordableQuotes =>
          j.into[Journey.RetrievedExtremeDates]
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform

        case j: Journey.ChosenPaymentPlan =>
          j.into[Journey.RetrievedExtremeDates]
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform

        case j: Journey.CheckedPaymentPlan =>
          j.into[Journey.RetrievedExtremeDates]
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform

        case j: Journey.EnteredCanYouSetUpDirectDebit =>
          j.into[Journey.RetrievedExtremeDates]
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform

        case j: Journey.EnteredDirectDebitDetails =>
          j.into[Journey.RetrievedExtremeDates]
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform

        case j: Journey.ConfirmedDirectDebitDetails =>
          j.into[Journey.RetrievedExtremeDates]
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform

        case j: Journey.AgreedTermsAndConditions =>
          j.into[Journey.RetrievedExtremeDates]
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform

        case j: Journey.SelectedEmailToBeVerified =>
          j.into[Journey.RetrievedExtremeDates]
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform

        case j: Journey.EmailVerificationComplete =>
          j.into[Journey.RetrievedExtremeDates]
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform

        case _: Journey.SubmittedArrangement =>
          Errors.throwBadRequestException("Cannot update ExtremeDates when journey is in completed state")
      }
      journeyService.upsert(newJourney)
    }

  def updateStartDates(journeyId: JourneyId): Action[StartDatesResponse] =
    actions.authenticatedAction.async(parse.json[StartDatesResponse]) { implicit request =>
      for {
        journey    <- journeyService.get(journeyId)
        newJourney <- journey match {
                        case j: JourneyStage.BeforeEnteredDayOfMonth =>
                          Errors.throwBadRequestExceptionF(
                            s"UpdateStartDates is not possible when we don't have a chosen day of month, stage: [ ${j.stage} ]"
                          )
                        case j: Journey.EnteredDayOfMonth            =>
                          updateJourneyWithNewStartDatesValue(j, request.body)

                        case j: JourneyStage.AfterStartDatesResponse =>
                          updateJourneyWithExistingStartDatesValue(Left(j), request.body)

                        case j: JourneyStage.AfterCheckedPaymentPlan =>
                          j match {
                            case _: JourneyStage.BeforeArrangementSubmitted =>
                              updateJourneyWithExistingStartDatesValue(Right(j), request.body)
                            case _: JourneyStage.AfterArrangementSubmitted  =>
                              Errors.throwBadRequestExceptionF(
                                "Cannot update StartDates when journey is in completed state"
                              )
                          }
                        case _: JourneyStage.AfterStartedPegaCase    =>
                          Errors.throwBadRequestExceptionF(
                            "Not expecting to update ExtremeDates after starting PEGA case"
                          )
                      }
      } yield Ok(newJourney.json)
    }

  private def updateJourneyWithNewStartDatesValue(
    journey:            Journey.EnteredDayOfMonth,
    startDatesResponse: StartDatesResponse
  )(using Request[_]): Future[Journey] = {
    val newJourney: Journey =
      journey
        .into[Journey.RetrievedStartDates]
        .withFieldConst(_.startDatesResponse, startDatesResponse)
        .transform

    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingStartDatesValue(
    journey:            Either[JourneyStage.AfterStartDatesResponse & Journey, JourneyStage.AfterCheckedPaymentPlan & Journey],
    startDatesResponse: StartDatesResponse
  )(using Request[_]): Future[Journey] =
    journey match {
      case Left(afterStartDatesResponse) =>
        updateJourneyWithExistingValue(
          afterStartDatesResponse.startDatesResponse,
          afterStartDatesResponse,
          startDatesResponse,
          afterStartDatesResponse match {
            case j: Journey.RetrievedStartDates => j.copy(startDatesResponse = startDatesResponse)

            case j: Journey.RetrievedAffordableQuotes =>
              j.into[Journey.RetrievedStartDates]
                .withFieldConst(_.startDatesResponse, startDatesResponse)
                .transform

            case j: Journey.ChosenPaymentPlan =>
              j.into[Journey.RetrievedStartDates]
                .withFieldConst(_.startDatesResponse, startDatesResponse)
                .transform
          }
        )

      case Right(afterCheckedPaymentPlan) =>
        afterCheckedPaymentPlan.paymentPlanAnswers match {
          case _: PaymentPlanAnswers.PaymentPlanAfterAffordability =>
            Errors.throwBadRequestExceptionF("Cannot update StartDatesResponse on affordability journey")

          case p: PaymentPlanAnswers.PaymentPlanNoAffordability =>
            updateJourneyWithExistingValue(
              p.startDatesResponse,
              afterCheckedPaymentPlan,
              startDatesResponse,
              afterCheckedPaymentPlan match {
                case j: Journey.CheckedPaymentPlan =>
                  j.into[Journey.RetrievedStartDates]
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, startDatesResponse)
                    .transform

                case j: Journey.EnteredCanYouSetUpDirectDebit =>
                  j.into[Journey.RetrievedStartDates]
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, startDatesResponse)
                    .transform

                case j: Journey.EnteredDirectDebitDetails =>
                  j.into[Journey.RetrievedStartDates]
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, startDatesResponse)
                    .transform

                case j: Journey.ConfirmedDirectDebitDetails =>
                  j.into[Journey.RetrievedStartDates]
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, startDatesResponse)
                    .transform

                case j: Journey.AgreedTermsAndConditions =>
                  j.into[Journey.RetrievedStartDates]
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, startDatesResponse)
                    .transform

                case j: Journey.SelectedEmailToBeVerified =>
                  j.into[Journey.RetrievedStartDates]
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, startDatesResponse)
                    .transform

                case j: Journey.EmailVerificationComplete =>
                  j.into[Journey.RetrievedStartDates]
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, startDatesResponse)
                    .transform

                case _: Journey.SubmittedArrangement =>
                  Errors.throwBadRequestException("Cannot update StartDates when journey is in completed state")
              }
            )
        }
    }

  private def updateJourneyWithExistingValue(
    existingValue:   StartDatesResponse,
    existingJourney: Journey,
    newValue:        StartDatesResponse,
    newJourney:      Journey
  )(using Request[_]): Future[Journey] =
    if (existingValue == newValue) {
      JourneyLogger.info("Nothing to update, StartDatesResponse is the same as the existing one in journey.")
      Future.successful(existingJourney)
    } else {
      journeyService.upsert(newJourney)
    }

}
