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

import cats.syntax.eq._
import com.google.inject.{Inject, Singleton}
import essttp.journey.model.ttp.affordability.InstalmentAmounts
import essttp.journey.model.{Journey, JourneyId, Stage}
import essttp.utils.Errors
import io.scalaland.chimney.dsl.TransformerOps
import play.api.mvc.{Action, ControllerComponents, Request}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateAffordabilityResultController @Inject() (
    journeyService: JourneyService,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext) extends BackendController(cc) {
  def updateAffordabilityResultController(journeyId: JourneyId): Action[InstalmentAmounts] = Action.async(parse.json[InstalmentAmounts]) { implicit request =>
    for {
      journey <- journeyService.get(journeyId)
      _ <- journey match {
        case j: Journey.BeforeExtremeDatesResponse        => Errors.throwBadRequestExceptionF(s"UpdateAffordabilityResult update is not possible in that state: [${j.stage}]")
        case j: Journey.Stages.RetrievedExtremeDates      => updateJourneyWithNewValue(j, request.body)
        case j: Journey.AfterRetrievedAffordabilityResult => updateJourneyWithExistingValue(j, request.body)
      }
    } yield Ok
  }

  private def updateJourneyWithNewValue(
      journey:           Journey.Stages.RetrievedExtremeDates,
      instalmentAmounts: InstalmentAmounts
  )(implicit request: Request[_]): Future[Unit] = {
    val newJourney: Journey.Epaye.RetrievedAffordabilityResult = journey match {
      case j: Journey.Epaye.RetrievedExtremeDates => j.into[Journey.Epaye.RetrievedAffordabilityResult]
        .withFieldConst(_.stage, Stage.AfterAffordabilityResult.RetrievedAffordabilityResult)
        .withFieldConst(_.instalmentAmounts, instalmentAmounts)
        .transform
    }
    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingValue(
      journey:           Journey.AfterRetrievedAffordabilityResult,
      instalmentAmounts: InstalmentAmounts
  )(implicit request: Request[_]): Future[Unit] = {
    if (journey.instalmentAmounts === instalmentAmounts) {
      JourneyLogger.info("Nothing to update, InstalmentAmounts is the same as the existing one in journey.")
      Future.successful(())
    } else {
      val newJourney: Journey.AfterRetrievedAffordabilityResult = journey match {
        case j: Journey.Epaye.RetrievedAffordabilityResult => j.copy(instalmentAmounts = instalmentAmounts)
        case j: Journey.Epaye.EnteredMonthlyPaymentAmount =>
          j.into[Journey.Epaye.RetrievedAffordabilityResult]
            .withFieldConst(_.stage, Stage.AfterAffordabilityResult.RetrievedAffordabilityResult)
            .withFieldConst(_.instalmentAmounts, instalmentAmounts)
            .transform
      }
      journeyService.upsert(newJourney)
    }

  }
}
