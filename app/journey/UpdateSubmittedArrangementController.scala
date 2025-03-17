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
import essttp.journey.model.{EmailVerificationAnswers, Journey, JourneyId, JourneyStage}
import essttp.rootmodel.ttp.arrangement.ArrangementResponse
import essttp.utils.Errors
import io.scalaland.chimney.dsl.*
import paymentsEmailVerification.models.EmailVerificationResult
import play.api.mvc.{Action, ControllerComponents, Request}
import services.JourneyService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateSubmittedArrangementController @Inject() (
  actions:        Actions,
  journeyService: JourneyService,
  cc:             ControllerComponents
)(using ExecutionContext, OperationalCryptoFormat)
    extends BackendController(cc) {

  def updateArrangement(journeyId: JourneyId): Action[ArrangementResponse] =
    actions.authenticatedAction.async(parse.json[ArrangementResponse]) { implicit request =>
      for {
        journey    <- journeyService.get(journeyId)
        newJourney <- journey match {
                        case j: JourneyStage.BeforeAgreedTermsAndConditions =>
                          Errors.throwBadRequestExceptionF(
                            s"UpdateArrangement is not possible if the user hasn't agreed to the terms and conditions, state: [${j.stage}]"
                          )

                        case j: Journey.AgreedTermsAndConditions =>
                          if (j.isEmailAddressRequired)
                            Errors.throwBadRequestExceptionF(
                              s"UpdateArrangement is not possible if the user still requires and email address, state: [${j.stage}]"
                            )
                          else
                            updateJourneyWithNewValue(Left(j), request.body)

                        case j: Journey.SelectedEmailToBeVerified =>
                          Errors.throwBadRequestExceptionF(
                            s"UpdateArrangement is not possible if the user has not verified their email address yet, state: [${j.stage}]"
                          )

                        case j: Journey.EmailVerificationComplete =>
                          j.emailVerificationResult match {
                            case EmailVerificationResult.Locked =>
                              Errors.throwBadRequestExceptionF(
                                s"UpdateArrangement is not possible if the user has been locked out from verifying their email address, state: [${j.stage}]"
                              )

                            case EmailVerificationResult.Verified =>
                              updateJourneyWithNewValue(Right(j), request.body)
                          }

                        case _: JourneyStage.AfterArrangementSubmitted =>
                          Errors.throwBadRequestExceptionF(
                            "Cannot update SubmittedArrangement when journey is in completed state"
                          )
                      }
      } yield Ok(newJourney.json)
    }

  private def updateJourneyWithNewValue(
    journey:             Either[Journey.AgreedTermsAndConditions, Journey.EmailVerificationComplete],
    arrangementResponse: ArrangementResponse
  )(using Request[?]): Future[Journey] = {
    val newJourney: Journey = journey match {
      case Left(j: Journey.AgreedTermsAndConditions) =>
        j.into[Journey.SubmittedArrangement]
          .withFieldConst(_.emailVerificationAnswers, EmailVerificationAnswers.NoEmailJourney)
          .withFieldConst(_.arrangementResponse, arrangementResponse)
          .transform

      case Right(j: Journey.EmailVerificationComplete) =>
        j.into[Journey.SubmittedArrangement]
          .withFieldConst(_.arrangementResponse, arrangementResponse)
          .transform
    }

    journeyService.upsert(newJourney)
  }

}
