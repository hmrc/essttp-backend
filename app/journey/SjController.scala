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
import journey.model.Origin.Vat
import journey.model._
import play.api.libs.json.{Json, Reads}
import play.api.mvc.{Action, ControllerComponents, Request, Result}
import requests.RequestSupport
import rootmodel.SessionId
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.Errors

import scala.concurrent.{ExecutionContext, Future}

/**
 * Start Journey (Sj) Controller
 */
class SjController @Inject() (
    journeyService: JourneyService,
    journeyConfig:  JourneyConfig,
    journeyFactory: JourneyFactory,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext) extends BackendController(cc) {

  def startJourneyEpayeFromBta(): Action[SjRequest.Epaye.Simple] = startDdJourneyEpaye[SjRequest.Epaye.Simple](Origin.Epaye.Bta)
  def startJourneyEpayeFromGovUk(): Action[SjRequest.Epaye.Empty] = startDdJourneyEpaye[SjRequest.Epaye.Empty](Origin.Epaye.GovUk)
  def startJourneyEpayeFromDetachedUrl(): Action[SjRequest.Epaye.Empty] = startDdJourneyEpaye[SjRequest.Epaye.Empty](Origin.Epaye.DetachedUrl)

  private def startDdJourneyEpaye[StartRequest <: SjRequest.Epaye: Reads](origin: Origin.Epaye): Action[StartRequest] = Action.async(parse.json[StartRequest]) { implicit request =>
    val originatedSddjRequest = OriginatedSjRequest.Epaye(origin, request.body)
    doJourneyStart(originatedSddjRequest)
  }

  private def doJourneyStart(
      originatedRequest: OriginatedSjRequest
  )(implicit request: Request[_]): Future[Result] = {

    for {
      sessionId: SessionId <- RequestSupport.getSessionId()
      journey: Journey = journeyFactory.makeJourney(originatedRequest, sessionId)
      _ <- journeyService.upsert(journey)
    } yield {
      val description = journeyDescription(originatedRequest.origin)
      val nextUrl = NextUrl(s"${journeyConfig.nextUrlHost}/start")
      val response = Created(Json.toJson(nextUrl))
      JourneyLogger.info(s"Started $description [journeyId:${journey.id}]")
      response
    }
  }

  private def journeyDescription(origin: Origin): String = origin match {
    case o: Origin.Epaye => o match {
      case Origin.Epaye.Bta         => s"Journey for Epaye from BTA"
      case Origin.Epaye.GovUk       => s"Journey for Epaye from GovUk"
      case Origin.Epaye.DetachedUrl => s"Journey for Epaye from DetachedUrl"
    }
    case o: Origin.Vat => o match {
      case Vat.Bta => Errors.notImplemented("Vat not implemented yet")
    }
  }

}
