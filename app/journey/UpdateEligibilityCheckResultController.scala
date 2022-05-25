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
import essttp.utils.Errors
import io.scalaland.chimney.dsl._
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

class UpdateEligibilityCheckResultController @Inject() (
    journeyService: JourneyService,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext) extends BackendController(cc) {

  def updateEligibilityResult(journeyId: JourneyId): Action[EligibilityCheckResult] = Action.async(parse.json[EligibilityCheckResult]) { implicit request =>
    for {
      journey <- journeyService.get(journeyId)
      _ <- journey match {
        case j: Journey.Epaye.Started => Errors.throwBadRequestExceptionF("EligibilityCheckResult update is not possible in that state.")
        case j: Journey.Epaye.ComputedTaxId =>
          val newJourney = j
            .into[Journey.Epaye.EligibilityCheck]
            .withFieldConst(_.eligibilityCheckResult, request.body)
            .withFieldConst(
              _.stage,
              if (request.body.isEligible)
                Stage.AfterEligibilityCheck.Eligible
              else
                Stage.AfterEligibilityCheck.Ineligible
            )
            .transform
          journeyService.upsert(newJourney)
        case j: Journey.AfterEligibilityChecked =>
          JourneyLogger.info("Nothing to update, journey has already updated EligibilityCheckResult.")
          Future.successful(())
      }
    } yield Ok
  }

}
