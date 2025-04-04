/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers

import action.Actions
import essttp.crypto.CryptoFormat.OperationalCryptoFormat
import essttp.journey.model.JourneyId
import essttp.rootmodel.TaxRegime
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.PegaService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class PegaController @Inject() (
  pegaService: PegaService,
  actions:     Actions,
  cc:          ControllerComponents
)(using ExecutionContext, OperationalCryptoFormat)
    extends BackendController(cc) {

  def startCase(journeyId: JourneyId, recalculationNeeded: Boolean): Action[AnyContent] =
    actions.authenticatedAction.async { implicit request =>
      pegaService
        .startCase(journeyId, recalculationNeeded)
        .map(response => Created(Json.toJson(response)))
    }

  def getCase(journeyId: JourneyId): Action[AnyContent] = actions.authenticatedAction.async { implicit request =>
    pegaService
      .getCase(journeyId)
      .map(response => Ok(Json.toJson(response)))
  }

  def saveJourney(journeyId: JourneyId): Action[AnyContent] = actions.authenticatedAction.async { implicit request =>
    pegaService
      .saveJourney(journeyId)
      .map(_ => Ok)
  }

  def recreateSession(taxRegime: TaxRegime): Action[AnyContent] = actions.authenticatedAction.async {
    implicit request =>
      pegaService
        .recreateSession(taxRegime, request.enrolments)
        .map(journey => Ok(Json.toJson(journey)))
  }

}
