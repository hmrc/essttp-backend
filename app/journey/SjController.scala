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
import essttp.rootmodel.{SessionId, TraceId}
import essttp.utils.{Errors, RequestSupport}
import play.api.libs.json.{Json, Reads}
import play.api.mvc.{Action, ControllerComponents, Request, Result}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

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

  def startJourneyEpayeFromBta(): Action[SjRequest.Epaye.Simple] = startDdJourneyEpaye[SjRequest.Epaye.Simple](Origins.Epaye.Bta)
  def startJourneyEpayeFromGovUk(): Action[SjRequest.Epaye.Empty] = startDdJourneyEpaye[SjRequest.Epaye.Empty](Origins.Epaye.GovUk)
  def startJourneyEpayeFromDetachedUrl(): Action[SjRequest.Epaye.Empty] = startDdJourneyEpaye[SjRequest.Epaye.Empty](Origins.Epaye.DetachedUrl)

  private def startDdJourneyEpaye[StartRequest <: SjRequest.Epaye: Reads](origin: Origins.Epaye): Action[StartRequest] = Action.async(parse.json[StartRequest]) { implicit request =>
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
      val description: String = journeyDescription(originatedRequest.origin)
      val nextUrl: NextUrl = NextUrl(s"${journeyConfig.nextUrlHost}/set-up-a-payment-plan?traceId=${journey.traceId.value}")
      val sjResponse: SjResponse = SjResponse(nextUrl, journey.journeyId)
      val response: Result = Created(Json.toJson(sjResponse))
      JourneyLogger.info(s"Started $description [journeyId:${journey.id}]")
      response
    }
  }

  private def journeyDescription(origin: Origin): String = origin match {
    case o: Origins.Epaye => o match {
      case Origins.Epaye.Bta         => s"Journey for Epaye from BTA"
      case Origins.Epaye.GovUk       => s"Journey for Epaye from GovUk"
      case Origins.Epaye.DetachedUrl => s"Journey for Epaye from DetachedUrl"
    }
  }

}
