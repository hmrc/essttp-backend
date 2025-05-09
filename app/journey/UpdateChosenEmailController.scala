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
import essttp.rootmodel.Email
import essttp.utils.Errors
import io.scalaland.chimney.dsl.*
import play.api.mvc.{Action, ControllerComponents, Request}
import services.JourneyService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateChosenEmailController @Inject() (
  journeyService: JourneyService,
  cc:             ControllerComponents,
  actions:        Actions
)(using ExecutionContext, OperationalCryptoFormat)
    extends BackendController(cc) {

  def updateChosenEmail(journeyId: JourneyId): Action[Email] = actions.authenticatedAction.async(parse.json[Email]) {
    implicit request =>
      for {
        journey    <- journeyService.get(journeyId)
        newJourney <- journey match {
                        case j: JourneyStage.BeforeAgreedTermsAndConditions =>
                          Errors.throwBadRequestExceptionF(
                            s"UpdateChosenEmail is not possible in that state: [${j.stage}]"
                          )
                        case j: JourneyStage.AfterAgreedTermsAndConditions  =>
                          j match {
                            case j1: Journey.AgreedTermsAndConditions  =>
                              if (j.isEmailAddressRequired) updateJourneyWithNewValue(j1, request.body)
                              else
                                Errors.throwBadRequestExceptionF(
                                  s"Cannot update selected email address when isEmailAddressRequired is false for journey."
                                )
                            case j1: Journey.SelectedEmailToBeVerified =>
                              updateJourneyWithExistingValue(j1, request.body)
                            case j1: Journey.EmailVerificationComplete =>
                              updateJourneyWithExistingValue(j1, request.body)
                            case _: Journey.SubmittedArrangement       =>
                              Errors.throwBadRequestExceptionF(
                                s"Cannot update ChosenEmail when journey is in completed state."
                              )
                          }
                      }
      } yield Ok(newJourney.json)
  }

  private def updateJourneyWithNewValue(
    journey: Journey.AgreedTermsAndConditions,
    email:   Email
  )(using Request[?]): Future[Journey] = {
    val newJourney: Journey =
      journey
        .into[Journey.SelectedEmailToBeVerified]
        .withFieldConst(_.emailToBeVerified, email)
        .transform

    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingValue(
    journey: JourneyStage.AfterEmailAddressSelectedToBeVerified & Journey,
    email:   Email
  )(using Request[?]): Future[Journey] = {
    // don't check to see if email is same to allow for passcodes to be requested again for same email
    val newJourney: Journey = journey match {
      case j: Journey.SelectedEmailToBeVerified =>
        j.copy(emailToBeVerified = email)

      case j: Journey.EmailVerificationComplete =>
        j.into[Journey.SelectedEmailToBeVerified]
          .withFieldConst(_.emailToBeVerified, email)
          .transform
    }
    journeyService.upsert(newJourney)
  }

}
