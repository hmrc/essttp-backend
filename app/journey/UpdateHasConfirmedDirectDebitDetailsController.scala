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
import essttp.journey.model.{Journey, JourneyId, Stage}
import essttp.utils.Errors
import io.scalaland.chimney.dsl.TransformerOps
import play.api.mvc.{Action, AnyContent, ControllerComponents, Request}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateHasConfirmedDirectDebitDetailsController @Inject() (
    journeyService: JourneyService,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext) extends BackendController(cc) {

  def updateConfirmedDirectDebitDetails(journeyId: JourneyId): Action[AnyContent] = Action.async { implicit request =>
    for {
      journey <- journeyService.get(journeyId)
      _ <- journey match {
        case j: Journey.BeforeEnteredDirectDebitDetails  => Errors.throwBadRequestExceptionF(s"UpdateHasConfirmedDirectDebitDetails is not possible in that state: [${j.stage}]")
        case j: Journey.Stages.EnteredDirectDebitDetails => updateJourneyWithNewValue(j)
        case _: Journey.AfterConfirmedDirectDebitDetails => Future.successful(())
      }
    } yield Ok
  }

  private def updateJourneyWithNewValue(
      journey: Journey.Stages.EnteredDirectDebitDetails
  )(implicit request: Request[_]): Future[Unit] = {
    val newJourney: Journey.AfterConfirmedDirectDebitDetails = journey match {
      case j: Journey.Epaye.EnteredDirectDebitDetails =>
        j.into[Journey.Epaye.ConfirmedDirectDebitDetails]
          .withFieldConst(_.stage, Stage.AfterConfirmedDirectDebitDetails.ConfirmedDetails)
          .transform
    }
    journeyService.upsert(newJourney)
  }
}
