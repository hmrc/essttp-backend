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
import essttp.journey.model.{Journey, JourneyId, JourneyStage, PaymentPlanAnswers}
import essttp.rootmodel.ttp.affordablequotes.PaymentPlan
import essttp.utils.Errors
import io.scalaland.chimney.dsl.*
import play.api.mvc.{Action, ControllerComponents, Request}
import services.JourneyService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateInstalmentPlanController @Inject() (
  actions:        Actions,
  journeyService: JourneyService,
  cc:             ControllerComponents
)(using ExecutionContext, OperationalCryptoFormat)
    extends BackendController(cc) {

  def updateChosenInstalmentPlan(journeyId: JourneyId): Action[PaymentPlan] =
    actions.authenticatedAction.async(parse.json[PaymentPlan]) { implicit request =>
      for {
        journey    <- journeyService.get(journeyId)
        newJourney <- journey match {
                        case j: JourneyStage.BeforeAffordableQuotesResponse =>
                          Errors.throwBadRequestExceptionF(
                            s"UpdateSelectedPaymentPlan is not possible in that state: [${j.stage}]"
                          )
                        case j: model.Journey.RetrievedAffordableQuotes     =>
                          updateJourneyWithNewValue(j, request.body)

                        case j: JourneyStage.AfterSelectedPaymentPlan =>
                          updateJourneyWithExistingValue(Left(j), request.body)

                        case j: JourneyStage.AfterCheckedPaymentPlan =>
                          j match {
                            case _: JourneyStage.BeforeArrangementSubmitted =>
                              updateJourneyWithExistingValue(Right(j), request.body)
                            case _: JourneyStage.AfterArrangementSubmitted  =>
                              Errors.throwBadRequestExceptionF(
                                "Cannot update ChosenPlan when journey is in completed state"
                              )
                          }

                        case _: JourneyStage.AfterStartedPegaCase =>
                          Errors.throwBadRequestExceptionF(
                            s"Not expecting to update SelectedPaymentPlan after starting PEGA case"
                          )
                      }
      } yield Ok(newJourney.json)
    }

  private def updateJourneyWithNewValue(
    journey:     Journey.RetrievedAffordableQuotes,
    paymentPlan: PaymentPlan
  )(using Request[?]): Future[Journey] = {
    val newJourney: Journey =
      journey
        .into[Journey.ChosenPaymentPlan]
        .withFieldConst(_.selectedPaymentPlan, paymentPlan)
        .transform

    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingValue(
    journey:     Either[JourneyStage.AfterSelectedPaymentPlan & Journey, JourneyStage.AfterCheckedPaymentPlan & Journey],
    paymentPlan: PaymentPlan
  )(using Request[?]): Future[Journey] =
    journey match {
      case Left(afterSelectedPaymentPlan) =>
        updateJourneyWithExistingValue(
          afterSelectedPaymentPlan.selectedPaymentPlan,
          afterSelectedPaymentPlan,
          paymentPlan,
          afterSelectedPaymentPlan match {
            case j: Journey.ChosenPaymentPlan =>
              j.copy(selectedPaymentPlan = paymentPlan)
          }
        )

      case Right(afterCheckedPaymentPlan) =>
        afterCheckedPaymentPlan.paymentPlanAnswers match {
          case _: PaymentPlanAnswers.PaymentPlanAfterAffordability =>
            Errors.throwBadRequestExceptionF("Cannot update SelectedPaymentPlan on affordability journey")

          case p: PaymentPlanAnswers.PaymentPlanNoAffordability =>
            updateJourneyWithExistingValue(
              p.selectedPaymentPlan,
              afterCheckedPaymentPlan,
              paymentPlan,
              afterCheckedPaymentPlan match {
                case j: Journey.CheckedPaymentPlan =>
                  j.into[Journey.ChosenPaymentPlan]
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, p.startDatesResponse)
                    .withFieldConst(_.affordableQuotesResponse, p.affordableQuotesResponse)
                    .withFieldConst(_.selectedPaymentPlan, paymentPlan)
                    .transform

                case j: Journey.EnteredCanYouSetUpDirectDebit =>
                  j.into[Journey.ChosenPaymentPlan]
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, p.startDatesResponse)
                    .withFieldConst(_.affordableQuotesResponse, p.affordableQuotesResponse)
                    .withFieldConst(_.selectedPaymentPlan, paymentPlan)
                    .transform

                case j: Journey.EnteredDirectDebitDetails =>
                  j.into[Journey.ChosenPaymentPlan]
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, p.startDatesResponse)
                    .withFieldConst(_.affordableQuotesResponse, p.affordableQuotesResponse)
                    .withFieldConst(_.selectedPaymentPlan, paymentPlan)
                    .transform

                case j: Journey.ConfirmedDirectDebitDetails =>
                  j.into[Journey.ChosenPaymentPlan]
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, p.startDatesResponse)
                    .withFieldConst(_.affordableQuotesResponse, p.affordableQuotesResponse)
                    .withFieldConst(_.selectedPaymentPlan, paymentPlan)
                    .transform

                case j: Journey.AgreedTermsAndConditions =>
                  j.into[Journey.ChosenPaymentPlan]
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, p.startDatesResponse)
                    .withFieldConst(_.affordableQuotesResponse, p.affordableQuotesResponse)
                    .withFieldConst(_.selectedPaymentPlan, paymentPlan)
                    .transform

                case j: Journey.SelectedEmailToBeVerified =>
                  j.into[Journey.ChosenPaymentPlan]
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, p.startDatesResponse)
                    .withFieldConst(_.affordableQuotesResponse, p.affordableQuotesResponse)
                    .withFieldConst(_.selectedPaymentPlan, paymentPlan)
                    .transform

                case j: Journey.EmailVerificationComplete =>
                  j.into[Journey.ChosenPaymentPlan]
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, p.startDatesResponse)
                    .withFieldConst(_.affordableQuotesResponse, p.affordableQuotesResponse)
                    .withFieldConst(_.selectedPaymentPlan, paymentPlan)
                    .transform

                case _: model.Journey.SubmittedArrangement =>
                  Errors.throwBadRequestException("Cannot update ChosenPlan when journey is in completed state")
              }
            )
        }
    }

  private def updateJourneyWithExistingValue(
    existingValue:   PaymentPlan,
    existingJourney: Journey,
    newValue:        PaymentPlan,
    newJourney:      Journey
  )(using Request[?]): Future[Journey] =
    if (existingValue == newValue) {
      JourneyLogger.info("Nothing to update, selected PaymentPlan is the same as the existing one in journey.")
      Future.successful(existingJourney)
    } else {
      journeyService.upsert(newJourney)
    }

}
