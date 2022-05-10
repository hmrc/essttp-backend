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

import com.google.inject.Inject
import essttp.journey.model._
import essttp.journey.model.ttp.EligibilityCheckResult
import essttp.rootmodel.{Aor, TaxId, Vrn}
import essttp.utils.Errors
import io.scalaland.chimney.dsl._
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

class UpdateTaxIdController @Inject() (
    journeyService: JourneyService,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext) extends BackendController(cc) {

  def updateTaxId(journeyId: JourneyId): Action[TaxId] = Action.async(parse.json[TaxId]) { implicit request =>
    for {
      journey <- journeyService.get(journeyId)
      _ <- (request.body, journey) match {
        case (aor: Aor, journey: Journey.Epaye)     => updateJourney(journey, aor)
        case (vrn: Vrn, journey: Journey /*.Vat*/ ) => Errors.throwBadRequestExceptionF("Vat not supported yet")
      }
    } yield Ok
  }

  private def updateJourney(journey: Journey.Epaye, aor: Aor)(implicit request: Request[_]): Future[Unit] = {
    journey match {
      case j: Journey.Epaye.AfterStarted =>
        val newJourney: Journey.Epaye.AfterComputedTaxIds = j
          .into[Journey.Epaye.AfterComputedTaxIds]
          .withFieldConst(_.stage, Stage.AfterComputedTaxId.ComputedTaxId)
          .withFieldConst(_.taxId, aor)
          .transform
        journeyService.upsert(newJourney)
      case j: Journey.HasTaxId if j.taxId == aor =>
        JourneyLogger.info("Nothing to update, journey has already updated tax id.")
        Future.successful(())
      case j: Journey.HasTaxId if j.taxId != aor =>
        Errors.notImplemented("Incorrect taxId type. For Epaye it must be Aor")
    }
  }
}
