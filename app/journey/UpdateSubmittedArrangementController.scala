/*
 * Copyright 2022 HM Revenue & Customs
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
import essttp.journey.model.{EmailVerificationAnswers, Journey, JourneyId, Stage}
import essttp.rootmodel.ttp.arrangement.ArrangementResponse
import essttp.utils.Errors
import io.scalaland.chimney.dsl.TransformerOps
import play.api.mvc.{Action, ControllerComponents, Request}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateSubmittedArrangementController @Inject() (
    actions:        Actions,
    journeyService: JourneyService,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext) extends BackendController(cc) {

  def updateArrangement(journeyId: JourneyId): Action[ArrangementResponse] = actions.authenticatedAction.async(parse.json[ArrangementResponse]) { implicit request =>
    for {
      journey <- journeyService.get(journeyId)
      _ <- journey match {
        case j: Journey.BeforeAgreedTermsAndConditions =>
          Errors.throwBadRequestExceptionF(s"UpdateArrangement is not possible if the user hasn't agreed to the terms and conditions, state: [${j.stage}]")

        case j: Journey.Stages.AgreedTermsAndConditions =>
          if (j.isEmailAddressRequired)
            Errors.throwBadRequestExceptionF(s"UpdateArrangement is not possible if the user still requires and email address, state: [${j.stage}]")
          else
            updateJourneyWithNewValue(j, request.body)

        case _: Journey.Stages.SelectedEmailToBeVerified =>
          Errors.throwBadRequestExceptionF("This should never happen, remember to remove me once verified email is in.")

        case _: Journey.AfterArrangementSubmitted =>
          Errors.throwBadRequestExceptionF("Cannot update SubmittedArrangement when journey is in completed state")
      }
    } yield Ok
  }

  private def updateJourneyWithNewValue(
      journey:             Journey.Stages.AgreedTermsAndConditions,
      arrangementResponse: ArrangementResponse
  )(implicit request: Request[_]): Future[Unit] = {
    val newJourney: Journey.AfterArrangementSubmitted = journey match {
      case j: Journey.Epaye.AgreedTermsAndConditions =>
        j.into[Journey.Epaye.SubmittedArrangement]
          .withFieldConst(_.stage, Stage.AfterSubmittedArrangement.Submitted)
          .withFieldConst(_.emailVerificationAnswers, EmailVerificationAnswers.NoEmailJourney)
          .withFieldConst(_.arrangementResponse, arrangementResponse)
          .transform
    }
    journeyService.upsert(newJourney)
  }

}
