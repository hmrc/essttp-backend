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
import essttp.rootmodel.IsEmailAddressRequired
import essttp.utils.Errors
import io.scalaland.chimney.dsl.*
import play.api.mvc.{Action, ControllerComponents, Request}
import services.JourneyService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateHasAgreedTermsAndConditionsController @Inject() (
  actions:        Actions,
  journeyService: JourneyService,
  cc:             ControllerComponents
)(using ExecutionContext, OperationalCryptoFormat)
    extends BackendController(cc) {

  def updateAgreedTermsAndConditions(journeyId: JourneyId): Action[IsEmailAddressRequired] =
    actions.authenticatedAction.async(parse.json[IsEmailAddressRequired]) { implicit request =>
      for {
        journey    <- journeyService.get(journeyId)
        newJourney <- journey match {
                        case j: JourneyStage.BeforeConfirmedDirectDebitDetails =>
                          Errors.throwBadRequestExceptionF(
                            s"UpdateAgreedTermsAndConditions is not possible in that state: [${j.stage}]"
                          )
                        case j: Journey.ConfirmedDirectDebitDetails            =>
                          updateJourneyWithNewValue(j, request.body)
                        case j: JourneyStage.AfterAgreedTermsAndConditions     =>
                          updateJourneyWithExistingValue(j, request.body)
                      }
      } yield Ok(newJourney.json)
    }

  private def updateJourneyWithNewValue(
    journey:                Journey.ConfirmedDirectDebitDetails,
    isEmailAddressRequired: IsEmailAddressRequired
  )(using Request[?]): Future[Journey] = {
    val newJourney: Journey =
      journey
        .into[Journey.AgreedTermsAndConditions]
        .withFieldConst(_.isEmailAddressRequired, isEmailAddressRequired)
        .transform

    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingValue(
    journey:                JourneyStage.AfterAgreedTermsAndConditions & Journey,
    isEmailAddressRequired: IsEmailAddressRequired
  )(using Request[?]): Future[Journey] =
    journey match {
      case _: Journey.SubmittedArrangement =>
        Errors.throwBadRequestException("Cannot update AgreedTermsAndConditions when journey is in completed state")

      case j: Journey.AgreedTermsAndConditions =>
        upsertIfChanged(
          j,
          isEmailAddressRequired,
          j.copy(
            isEmailAddressRequired = isEmailAddressRequired
          )
        )

      case j: Journey.SelectedEmailToBeVerified =>
        upsertIfChanged(
          j,
          isEmailAddressRequired,
          j.into[Journey.AgreedTermsAndConditions]
            .withFieldConst(_.isEmailAddressRequired, isEmailAddressRequired)
            .transform
        )

      case j: Journey.EmailVerificationComplete =>
        upsertIfChanged(
          j,
          isEmailAddressRequired,
          j.into[Journey.AgreedTermsAndConditions]
            .withFieldConst(_.isEmailAddressRequired, isEmailAddressRequired)
            .transform
        )
    }

  private def upsertIfChanged(
    j:                      JourneyStage.AfterAgreedTermsAndConditions & Journey,
    isEmailAddressRequired: IsEmailAddressRequired,
    updatedJourney:         => JourneyStage.AfterAgreedTermsAndConditions & Journey
  )(using Request[?]): Future[Journey] =
    if (j.isEmailAddressRequired == isEmailAddressRequired) Future.successful(j)
    else journeyService.upsert(updatedJourney)

}
