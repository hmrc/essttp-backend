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
import essttp.utils.Errors
import io.scalaland.chimney.dsl.*
import play.api.mvc.{Action, ControllerComponents, Request}
import services.JourneyService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateHasCheckedInstalmentPlanController @Inject() (
  actions:        Actions,
  journeyService: JourneyService,
  cc:             ControllerComponents
)(using ExecutionContext, OperationalCryptoFormat)
    extends BackendController(cc) {

  def updateHasCheckedInstalmentPlan(journeyId: JourneyId): Action[PaymentPlanAnswers] =
    actions.authenticatedAction(parse.json[PaymentPlanAnswers]).async { implicit request =>
      for {
        journey    <- journeyService.get(journeyId)
        newJourney <- journey match {
                        case j: JourneyStage.BeforeSelectedPaymentPlan =>
                          Errors.throwBadRequestExceptionF(
                            s"UpdateHasCheckedInstalmentPlan is not possible in that state: [${j.stage}]"
                          )
                        case j: model.Journey.ChosenPaymentPlan        =>
                          updateJourneyWithNewValue(Right(j), request.body)
                        case j: model.Journey.StartedPegaCase          =>
                          updateJourneyWithNewValue(Left(j), request.body)
                        case j: JourneyStage.AfterCheckedPaymentPlan   =>
                          j match {
                            case _: JourneyStage.BeforeArrangementSubmitted =>
                              updateJourneyWithExistingValue(j, request.body)
                            case _: JourneyStage.AfterArrangementSubmitted  =>
                              Errors.throwBadRequestExceptionF(
                                "Cannot update HasCheckedPaymentPlan when journey is in completed state"
                              )
                          }

                      }
      } yield Ok(newJourney.json)
    }

  private def updateJourneyWithNewValue(
    journey: Either[model.Journey.StartedPegaCase, model.Journey.ChosenPaymentPlan],
    answers: PaymentPlanAnswers
  )(using Request[?]): Future[Journey] = {
    val newJourney: Journey = journey match {
      case Left(j: Journey.StartedPegaCase) =>
        j.into[Journey.CheckedPaymentPlan]
          .withFieldConst(_.paymentPlanAnswers, answers)
          .transform

      case Right(j: Journey.ChosenPaymentPlan) =>
        j.into[Journey.CheckedPaymentPlan]
          .withFieldConst(_.paymentPlanAnswers, answers)
          .transform
    }
    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingValue(
    journey: JourneyStage.AfterCheckedPaymentPlan & Journey,
    answers: PaymentPlanAnswers
  )(using Request[?]): Future[Journey] =
    if (journey.paymentPlanAnswers == answers)
      Future.successful(journey)
    else {
      val updatedJourney: Journey = journey match {
        case j: Journey.CheckedPaymentPlan =>
          j.copy(paymentPlanAnswers = answers)

        case j: Journey.EnteredCanYouSetUpDirectDebit =>
          j.into[Journey.CheckedPaymentPlan]
            .withFieldConst(_.paymentPlanAnswers, answers)
            .transform

        case j: Journey.EnteredDirectDebitDetails =>
          j.into[Journey.CheckedPaymentPlan]
            .withFieldConst(_.paymentPlanAnswers, answers)
            .transform

        case j: Journey.ConfirmedDirectDebitDetails =>
          j.into[Journey.CheckedPaymentPlan]
            .withFieldConst(_.paymentPlanAnswers, answers)
            .transform

        case j: Journey.AgreedTermsAndConditions =>
          j.into[Journey.CheckedPaymentPlan]
            .withFieldConst(_.paymentPlanAnswers, answers)
            .transform

        case j: Journey.SelectedEmailToBeVerified =>
          j.into[Journey.CheckedPaymentPlan]
            .withFieldConst(_.paymentPlanAnswers, answers)
            .transform

        case j: Journey.EmailVerificationComplete =>
          j.into[Journey.CheckedPaymentPlan]
            .withFieldConst(_.paymentPlanAnswers, answers)
            .transform

        case _: Journey.SubmittedArrangement =>
          Errors.throwBadRequestException(
            "Cannot update UpdateHasCheckedInstalmentPlan when journey is in completed state"
          )

      }
      journeyService.upsert(updatedJourney)
    }

}
