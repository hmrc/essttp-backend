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
import essttp.rootmodel.ttp.affordablequotes.AffordableQuotesResponse
import essttp.utils.Errors
import io.scalaland.chimney.dsl.*
import play.api.mvc.{Action, ControllerComponents, Request}
import services.JourneyService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateAffordableQuotesController @Inject() (
  actions:        Actions,
  journeyService: JourneyService,
  cc:             ControllerComponents
)(using ExecutionContext, OperationalCryptoFormat)
    extends BackendController(cc) {

  def updateAffordableQuotes(journeyId: JourneyId): Action[AffordableQuotesResponse] =
    actions.authenticatedAction.async(parse.json[AffordableQuotesResponse]) { implicit request =>
      for {
        journey    <- journeyService.get(journeyId)
        newJourney <- journey match {
                        case j: JourneyStage.BeforeStartDatesResponse      =>
                          Errors.throwBadRequestExceptionF(
                            s"UpdateAffordableQuotes is not possible in that state: [${j.stage}]"
                          )
                        case j: Journey.RetrievedStartDates                =>
                          updateJourneyWithNewValue(j, request.body)
                        case j: JourneyStage.AfterAffordableQuotesResponse =>
                          updateJourneyWithExistingValue(Left(j), request.body)
                        case j: JourneyStage.AfterCheckedPaymentPlan       =>
                          j match {
                            case _: JourneyStage.BeforeArrangementSubmitted =>
                              updateJourneyWithExistingValue(Right(j), request.body)
                            case _: JourneyStage.AfterArrangementSubmitted  =>
                              Errors.throwBadRequestExceptionF(
                                "Cannot update AffordableQuotes when journey is in completed state"
                              )
                          }
                        case _: JourneyStage.AfterStartedPegaCase          =>
                          Errors.throwBadRequestExceptionF(
                            "Not expecting to update AffordableQuotes after starting PEGA case"
                          )
                      }
      } yield Ok(newJourney.json)
    }

  private def updateJourneyWithNewValue(
    journey:                  Journey.RetrievedStartDates,
    affordableQuotesResponse: AffordableQuotesResponse
  )(using Request[_]): Future[Journey] = {
    val newJourney: Journey =
      journey
        .into[Journey.RetrievedAffordableQuotes]
        .withFieldConst(_.affordableQuotesResponse, affordableQuotesResponse)
        .transform
    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingValue(
    journey:                  Either[
      JourneyStage.AfterAffordableQuotesResponse & Journey,
      JourneyStage.AfterCheckedPaymentPlan & Journey
    ],
    affordableQuotesResponse: AffordableQuotesResponse
  )(using Request[_]): Future[Journey] = journey match {
    case Left(afterAffordableQuotesResponse) =>
      updateJourneyWithExistingValue(
        afterAffordableQuotesResponse.affordableQuotesResponse,
        afterAffordableQuotesResponse,
        affordableQuotesResponse,
        afterAffordableQuotesResponse match {
          case j: Journey.RetrievedAffordableQuotes =>
            j.copy(affordableQuotesResponse = affordableQuotesResponse)

          case j: Journey.ChosenPaymentPlan =>
            j.into[Journey.RetrievedAffordableQuotes]
              .withFieldConst(_.affordableQuotesResponse, affordableQuotesResponse)
              .transform
        }
      )

    case Right(afterCheckedPaymentPlan) =>
      afterCheckedPaymentPlan.paymentPlanAnswers match {
        case _: PaymentPlanAnswers.PaymentPlanAfterAffordability =>
          Errors.throwBadRequestExceptionF("Cannot update AffordableQuotesResponse on affordability journey")

        case p: PaymentPlanAnswers.PaymentPlanNoAffordability =>
          updateJourneyWithExistingValue(
            p.affordableQuotesResponse,
            afterCheckedPaymentPlan,
            affordableQuotesResponse,
            afterCheckedPaymentPlan match {
              case j: Journey.CheckedPaymentPlan =>
                j.into[Journey.RetrievedAffordableQuotes]
                  .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                  .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                  .withFieldConst(_.startDatesResponse, p.startDatesResponse)
                  .withFieldConst(_.affordableQuotesResponse, affordableQuotesResponse)
                  .transform

              case j: Journey.EnteredCanYouSetUpDirectDebit =>
                j.into[Journey.RetrievedAffordableQuotes]
                  .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                  .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                  .withFieldConst(_.startDatesResponse, p.startDatesResponse)
                  .withFieldConst(_.affordableQuotesResponse, affordableQuotesResponse)
                  .transform

              case j: Journey.EnteredDirectDebitDetails =>
                j.into[Journey.RetrievedAffordableQuotes]
                  .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                  .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                  .withFieldConst(_.startDatesResponse, p.startDatesResponse)
                  .withFieldConst(_.affordableQuotesResponse, affordableQuotesResponse)
                  .transform

              case j: Journey.ConfirmedDirectDebitDetails =>
                j.into[Journey.RetrievedAffordableQuotes]
                  .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                  .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                  .withFieldConst(_.startDatesResponse, p.startDatesResponse)
                  .withFieldConst(_.affordableQuotesResponse, affordableQuotesResponse)
                  .transform

              case j: Journey.AgreedTermsAndConditions =>
                j.into[Journey.RetrievedAffordableQuotes]
                  .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                  .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                  .withFieldConst(_.startDatesResponse, p.startDatesResponse)
                  .withFieldConst(_.affordableQuotesResponse, affordableQuotesResponse)
                  .transform

              case j: Journey.SelectedEmailToBeVerified =>
                j.into[Journey.RetrievedAffordableQuotes]
                  .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                  .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                  .withFieldConst(_.startDatesResponse, p.startDatesResponse)
                  .withFieldConst(_.affordableQuotesResponse, affordableQuotesResponse)
                  .transform

              case j: Journey.EmailVerificationComplete =>
                j.into[Journey.RetrievedAffordableQuotes]
                  .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                  .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                  .withFieldConst(_.startDatesResponse, p.startDatesResponse)
                  .withFieldConst(_.affordableQuotesResponse, affordableQuotesResponse)
                  .transform

              case _: Journey.SubmittedArrangement =>
                Errors.throwBadRequestException("Cannot update AffordableQuotes when journey is in completed state")
            }
          )

      }
  }

  private def updateJourneyWithExistingValue(
    existingValue:   AffordableQuotesResponse,
    existingJourney: Journey,
    newValue:        AffordableQuotesResponse,
    newJourney:      Journey
  )(using Request[_]): Future[Journey] =
    if (existingValue == newValue) {
      JourneyLogger.info("Nothing to update, AffordableQuotesResponse is the same as the existing one in journey.")
      Future.successful(existingJourney)
    } else {
      journeyService.upsert(newJourney)
    }

}
