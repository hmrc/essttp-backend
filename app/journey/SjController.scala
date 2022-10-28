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

import action.Actions
import com.google.inject.Inject
import essttp.journey.model._
import essttp.utils.RequestSupport
import play.api.libs.json.{Json, Reads}
import play.api.mvc.{Action, ControllerComponents, Request, Result}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

/**
 * Start Journey (Sj) Controller
 */
class SjController @Inject() (
    actions:        Actions,
    journeyService: JourneyService,
    journeyConfig:  JourneyConfig,
    journeyFactory: JourneyFactory,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext) extends BackendController(cc) {

  def startJourneyEpayeFromBta(): Action[SjRequest.Epaye.Simple] = startJourneyEpaye[SjRequest.Epaye.Simple](Origins.Epaye.Bta)
  def startJourneyEpayeFromEpayeService(): Action[SjRequest.Epaye.Simple] = startJourneyEpaye[SjRequest.Epaye.Simple](Origins.Epaye.EpayeService)
  def startJourneyEpayeFromGovUk(): Action[SjRequest.Epaye.Empty] = startJourneyEpaye[SjRequest.Epaye.Empty](Origins.Epaye.GovUk)
  def startJourneyEpayeFromDetachedUrl(): Action[SjRequest.Epaye.Empty] = startJourneyEpaye[SjRequest.Epaye.Empty](Origins.Epaye.DetachedUrl)

  private def startJourneyEpaye[StartRequest <: SjRequest.Epaye: Reads](origin: Origins.Epaye): Action[StartRequest] =
    actions.authenticatedAction.async(parse.json[StartRequest]) { implicit request =>
      val originatedSjRequest = OriginatedSjRequest.Epaye(origin, request.body)
      doJourneyStart(originatedSjRequest)
    }

  def startJourneyVatFromBta(): Action[SjRequest.Vat.Simple] = startJourneyVat[SjRequest.Vat.Simple](Origins.Vat.Bta)
  def startJourneyVatFromGovUk(): Action[SjRequest.Vat.Empty] = startJourneyVat[SjRequest.Vat.Empty](Origins.Vat.GovUk)
  def startJourneyVatFromDetachedUrl(): Action[SjRequest.Vat.Empty] = startJourneyVat[SjRequest.Vat.Empty](Origins.Vat.DetachedUrl)

  private def startJourneyVat[StartRequest <: SjRequest.Vat: Reads](origin: Origins.Vat): Action[StartRequest] =
    actions.authenticatedAction.async(parse.json[StartRequest]) { implicit request =>
      val originatedSjRequest = OriginatedSjRequest.Vat(origin, request.body)
      doJourneyStart(originatedSjRequest)
    }

  private def doJourneyStart(
      originatedRequest: OriginatedSjRequest
  )(implicit request: Request[_]): Future[Result] = {

    for {
      sessionId <- RequestSupport.getSessionId()
      journey: Journey = journeyFactory.makeJourney(originatedRequest, sessionId)
      _ <- journeyService.upsert(journey)
    } yield {
      val description: String = journeyDescription(originatedRequest.origin)
      //      val nextUrl: NextUrl = NextUrl(s"${journeyConfig.nextUrlHost}/set-up-a-payment-plan?traceId=${journey.traceId.value}")
      val nextUrlTest: NextUrl = NextUrl(s"${journeyConfig.nextUrlHost}${originToRelativeUrl(originatedRequest.origin)}")
      val sjResponse: SjResponse = SjResponse(nextUrlTest, journey.journeyId)
      val response: Result = Created(Json.toJson(sjResponse))
      JourneyLogger.info(s"Started $description [journeyId:${journey.id}]")
      response
    }
  }

  private def journeyDescription(origin: Origin): String = origin match {
    case o: Origins.Epaye => o match {
      case Origins.Epaye.Bta          => "Journey for Epaye from BTA"
      case Origins.Epaye.EpayeService => "Journey for Epaye from EPAYE service"
      case Origins.Epaye.GovUk        => "Journey for Epaye from GovUk"
      case Origins.Epaye.DetachedUrl  => "Journey for Epaye from DetachedUrl"
    }
    case o: Origins.Vat => o match {
      case Origins.Vat.Bta         => "Journey for Vat from BTA"
      case Origins.Vat.GovUk       => "Journey for Vat from GovUk"
      case Origins.Vat.DetachedUrl => "Journey for Vat from DetachedUrl"
    }
  }

  private def originToRelativeUrl(origin: Origin): String = origin match {
    case _: Origins.Epaye => "/set-up-a-payment-plan/epaye-payment-plan"
    case _: Origins.Vat   => "/set-up-a-payment-plan/vat-payment-plan"
  }

}
