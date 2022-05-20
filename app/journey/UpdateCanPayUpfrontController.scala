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
import essttp.journey.model.Journey.Epaye
import essttp.journey.model.Stage.AfterCanPayUpfront.No
import essttp.journey.model.{Journey, JourneyId, Stage}
import essttp.rootmodel.CanPayUpfront
import essttp.utils.Errors
import io.scalaland.chimney.dsl.TransformerOps
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateCanPayUpfrontController @Inject() (
    journeyService: JourneyService,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext) extends BackendController(cc) {

  def updateCanPayUpfront(journeyId: JourneyId): Action[CanPayUpfront] = Action.async(parse.json[CanPayUpfront]) { implicit request =>
    for {
      journey <- journeyService.get(journeyId)
      _ <- updateJourney(journey, request.body)
    } yield Ok
  }

  private def updateJourney(journey: Journey, canPayUpfront: CanPayUpfront)(implicit request: Request[_]): Future[Unit] = {
    journey match {
      case j: Journey.Epaye.AfterStarted =>
        Errors.throwBadRequestExceptionF(s"CanPayUpfront update is not possible in that state: [${j.stage.entryName}]")
      case j: Journey.Epaye.AfterComputedTaxIds =>
        Errors.throwBadRequestExceptionF(s"CanPayUpfront update is not possible in that state: [${j.stage.entryName}]")
      case j: Journey.Epaye.AfterEligibilityCheck =>
        val newJourney: Journey.Epaye.AfterCanPayUpfront =
          j.into[Journey.Epaye.AfterCanPayUpfront]
            .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
            .withFieldConst(_.canPayUpfront, canPayUpfront)
            .transform
        journeyService.upsert(newJourney)
      case j: Journey.Epaye.AfterCanPayUpfront if hasChangedAnswer(j, canPayUpfront) =>
        val newJourney: Journey.Epaye.AfterCanPayUpfront = j.copy(stage         = determineCanPayUpFrontEnum(canPayUpfront), canPayUpfront = canPayUpfront)
        journeyService.upsert(newJourney)
      case j: Journey.Epaye.AfterCanPayUpfront if hasNotChangedAnswer(j, canPayUpfront) =>
        JourneyLogger.info("Nothing to update, user's choice has not changed.")
        Future.successful(())
    }
  }

  private def hasChangedAnswer(journey: Journey.HasCanPayUpfront, latestCanPayUpfront: CanPayUpfront): Boolean =
    journey.canPayUpfront.value != latestCanPayUpfront.value

  private def hasNotChangedAnswer(journey: Journey.HasCanPayUpfront, latestCanPayUpfront: CanPayUpfront): Boolean =
    !hasChangedAnswer(journey, latestCanPayUpfront)

  private def determineCanPayUpFrontEnum(latestCanPayUpfrontValue: CanPayUpfront): Stage.AfterCanPayUpfront = {
    if (latestCanPayUpfrontValue.value) Stage.AfterCanPayUpfront.Yes
    else Stage.AfterCanPayUpfront.No
  }

}
