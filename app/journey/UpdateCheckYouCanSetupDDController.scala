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
import essttp.rootmodel.bank.CanSetUpDirectDebit
import essttp.utils.Errors
import io.scalaland.chimney.dsl.*
import play.api.mvc.{Action, ControllerComponents, Request}
import services.JourneyService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateCheckYouCanSetupDDController @Inject() (
  actions:        Actions,
  journeyService: JourneyService,
  cc:             ControllerComponents
)(using ExecutionContext, OperationalCryptoFormat)
    extends BackendController(cc) {

  def updateCheckYouCanSetupDD(journeyId: JourneyId): Action[CanSetUpDirectDebit] =
    actions.authenticatedAction.async(parse.json[CanSetUpDirectDebit]) { implicit request =>
      for {
        journey    <- journeyService.get(journeyId)
        newJourney <- journey match {
                        case j: JourneyStage.BeforeCheckedPaymentPlan           =>
                          Errors.throwBadRequestExceptionF(
                            s"UpdateCheckYouCanSetupDD is not possible in that state: [${j.stage}]"
                          )
                        case j: Journey.CheckedPaymentPlan                      =>
                          updateJourneyWithNewValue(j, request.body)
                        case j: JourneyStage.AfterEnteredCanYouSetUpDirectDebit =>
                          j match {
                            case _: JourneyStage.BeforeArrangementSubmitted =>
                              updateJourneyWithExistingValue(j, request.body)
                            case _: JourneyStage.AfterArrangementSubmitted  =>
                              Errors.throwBadRequestExceptionF(
                                "Cannot update CanSetUpDirectDebit when journey is in completed state"
                              )
                          }
                      }
      } yield Ok(newJourney.json)
    }

  private def updateJourneyWithNewValue(
    journey:             Journey.CheckedPaymentPlan,
    canSetUpDirectDebit: CanSetUpDirectDebit
  )(using Request[?]): Future[Journey] = {
    val newJourney: Journey =
      journey
        .into[Journey.EnteredCanYouSetUpDirectDebit]
        .withFieldConst(_.canSetUpDirectDebitAnswer, canSetUpDirectDebit)
        .transform

    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingValue(
    journey:             JourneyStage.AfterEnteredCanYouSetUpDirectDebit & Journey,
    canSetUpDirectDebit: CanSetUpDirectDebit
  )(using Request[?]): Future[Journey] =
    if (journey.canSetUpDirectDebitAnswer == canSetUpDirectDebit) {
      JourneyLogger.info("Chosen type of bank account hasn't changed, nothing to update")
      Future.successful(journey)
    } else {
      val updatedJourney: Journey = journey match {
        case j: Journey.EnteredCanYouSetUpDirectDebit =>
          j.copy(canSetUpDirectDebitAnswer = canSetUpDirectDebit)

        case j: Journey.EnteredDirectDebitDetails =>
          j.into[Journey.EnteredCanYouSetUpDirectDebit]
            .withFieldConst(_.canSetUpDirectDebitAnswer, canSetUpDirectDebit)
            .transform

        case j: Journey.ConfirmedDirectDebitDetails =>
          j.into[Journey.EnteredCanYouSetUpDirectDebit]
            .withFieldConst(_.canSetUpDirectDebitAnswer, canSetUpDirectDebit)
            .transform

        case j: Journey.AgreedTermsAndConditions =>
          j.into[Journey.EnteredCanYouSetUpDirectDebit]
            .withFieldConst(_.canSetUpDirectDebitAnswer, canSetUpDirectDebit)
            .transform

        case j: Journey.SelectedEmailToBeVerified =>
          j.into[Journey.EnteredCanYouSetUpDirectDebit]
            .withFieldConst(_.canSetUpDirectDebitAnswer, canSetUpDirectDebit)
            .transform

        case j: Journey.EmailVerificationComplete =>
          j.into[Journey.EnteredCanYouSetUpDirectDebit]
            .withFieldConst(_.canSetUpDirectDebitAnswer, canSetUpDirectDebit)
            .transform

        case _: Journey.SubmittedArrangement =>
          Errors.throwBadRequestException("Cannot update CanSetUpDirectDebit when journey is in completed state")
      }

      journeyService.upsert(updatedJourney)
    }

}
