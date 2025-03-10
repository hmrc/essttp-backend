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
import com.google.inject.Inject
import essttp.crypto.CryptoFormat.OperationalCryptoFormat
import paymentsEmailVerification.models.EmailVerificationResult
import essttp.journey.model.{EmailVerificationAnswers, Journey, JourneyId, JourneyStage}
import essttp.utils.Errors
import io.scalaland.chimney.dsl.*
import play.api.mvc.{Action, ControllerComponents, Request}
import services.JourneyService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

class UpdateEmailVerificationResultController @Inject() (
  journeyService: JourneyService,
  cc:             ControllerComponents,
  actions:        Actions
)(using ExecutionContext, OperationalCryptoFormat)
    extends BackendController(cc) {

  def updateEmailVerificationResult(journeyId: JourneyId): Action[EmailVerificationResult] =
    actions.authenticatedAction.async(parse.json[EmailVerificationResult]) { implicit request =>
      for {
        journey    <- journeyService.get(journeyId)
        newJourney <- journey match {
                        case j: JourneyStage.BeforeEmailAddressSelectedToBeVerified =>
                          Errors.throwBadRequestExceptionF(
                            s"UpdateEmailVerificationResult is not possible in that state: [${j.stage}]"
                          )

                        case _: JourneyStage.AfterArrangementSubmitted =>
                          Errors.throwBadRequestExceptionF(
                            "Cannot update EmailVerificationResult when journey is in completed state."
                          )

                        case j: JourneyStage.AfterEmailAddressSelectedToBeVerified =>
                          j match {
                            case j1: Journey.SelectedEmailToBeVerified =>
                              updateJourneyWithNewValue(j1, request.body)
                            case j1: Journey.EmailVerificationComplete =>
                              updateJourneyWithExistingValue(j1, request.body)
                          }

                      }
      } yield Ok(newJourney.json)
    }

  private def updateJourneyWithNewValue(
    journey: Journey.SelectedEmailToBeVerified,
    status:  EmailVerificationResult
  )(using Request[_]): Future[Journey] = {
    val newJourney: Journey =
      journey
        .into[Journey.EmailVerificationComplete]
        .withFieldConst(
          _.emailVerificationAnswers,
          EmailVerificationAnswers.EmailVerified(journey.emailToBeVerified, status)
        )
        .withFieldConst(_.emailVerificationResult, status)
        .transform

    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingValue(
    journey: Journey.EmailVerificationComplete & Journey,
    result:  EmailVerificationResult
  )(using Request[_]): Future[Journey] =
    if (journey.emailVerificationResult == result) {
      Future.successful(journey)
    } else {
      val newJourney: Journey = journey match {
        case j: Journey.EmailVerificationComplete =>
          j.copy(
            emailVerificationResult = result,
            emailVerificationAnswers = EmailVerificationAnswers.EmailVerified(j.emailToBeVerified, result)
          )
      }

      journeyService.upsert(newJourney)
    }

}
