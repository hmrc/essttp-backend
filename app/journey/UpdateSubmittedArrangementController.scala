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

import com.google.inject.{Inject, Singleton}
import essttp.journey.model.ttp.arrangement.ArrangementResponse
import essttp.journey.model.{Journey, JourneyId, Stage}
import essttp.utils.Errors
import io.scalaland.chimney.dsl.TransformerOps
import play.api.mvc.{Action, ControllerComponents, Request}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateSubmittedArrangementController @Inject() (
    journeyService: JourneyService,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext) extends BackendController(cc) {

  def updateArrangement(journeyId: JourneyId): Action[ArrangementResponse] = Action.async(parse.json[ArrangementResponse]) { implicit request =>
    for {
      journey <- journeyService.get(journeyId)
      _ <- journey match {
        case j: Journey.BeforeAgreedTermsAndConditions  => Errors.throwBadRequestExceptionF(s"UpdateArrangement is not possible if the user hasn't agreed to the terms and conditions, state: [${j.stage}]")
        case j: Journey.Stages.AgreedTermsAndConditions => updateJourneyWithNewValue(j, request.body)
        case _: Journey.AfterArrangementSubmitted       => updateJourneyWithExistingValue()
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
          .withFieldConst(_.arrangementResponse, arrangementResponse)
          .transform
    }
    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingValue(): Future[Unit] = {
    Errors.throwBadRequestExceptionF("Cannot update SubmittedArrangement when journey is in completed state")
  }
}
