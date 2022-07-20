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
class UpdateHasCheckedInstalmentPlanController @Inject() (
    journeyService: JourneyService,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext) extends BackendController(cc) {

  def updateHasCheckedInstalmentPlan(journeyId: JourneyId): Action[AnyContent] = Action.async { implicit request =>
    for {
      journey <- journeyService.get(journeyId)
      _ <- journey match {
        case j: Journey.BeforeSelectedPaymentPlan => Errors.throwBadRequestExceptionF(s"UpdateHasCheckedInstalmentPlan is not possible in that state: [${j.stage}]")
        case j: Journey.Stages.ChosenPaymentPlan  => updateJourneyWithNewValue(j)
        case j: Journey.AfterCheckedPaymentPlan => j match {
          case _: Journey.BeforeArrangementSubmitted => Future.successful(())
          case _: Journey.AfterArrangementSubmitted  => Errors.throwBadRequestExceptionF("Cannot update HasCheckedPaymentPlan when journey is in completed state")
        }
      }
    } yield Ok
  }

  private def updateJourneyWithNewValue(
      journey: Journey.Stages.ChosenPaymentPlan
  )(implicit request: Request[_]): Future[Unit] = {
    val newJourney: Journey.AfterCheckedPaymentPlan = journey match {
      case j: Journey.Epaye.ChosenPaymentPlan =>
        j.into[Journey.Epaye.CheckedPaymentPlan]
          .withFieldConst(_.stage, Stage.AfterCheckedPlan.AcceptedPlan)
          .transform
    }
    journeyService.upsert(newJourney)
  }

}
