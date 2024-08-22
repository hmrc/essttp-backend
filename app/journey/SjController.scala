/*
 * Copyright 2023 HM Revenue & Customs
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
import com.google.inject.{Inject, Singleton}
import config.JourneyConfig
import essttp.journey.model.Origins.Sa
import essttp.journey.model._
import essttp.utils.RequestSupport
import play.api.libs.json.{Json, Reads}
import play.api.mvc.{Action, ControllerComponents, Request, Result}
import services.{JourneyFactory, JourneyService}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

/**
 * Start Journey (Sj) Controller
 */
@Singleton
class SjController @Inject() (
    actions:        Actions,
    journeyService: JourneyService,
    journeyConfig:  JourneyConfig,
    journeyFactory: JourneyFactory,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext) extends BackendController(cc) {

  val startJourneyEpayeFromBta: Action[SjRequest.Epaye.Simple] = startJourneyEpaye[SjRequest.Epaye.Simple](Origins.Epaye.Bta)

  val startJourneyEpayeFromEpayeService: Action[SjRequest.Epaye.Simple] = startJourneyEpaye[SjRequest.Epaye.Simple](Origins.Epaye.EpayeService)

  val startJourneyEpayeFromGovUk: Action[SjRequest.Epaye.Empty] = startJourneyEpaye[SjRequest.Epaye.Empty](Origins.Epaye.GovUk)

  val startJourneyEpayeFromDetachedUrl: Action[SjRequest.Epaye.Empty] = startJourneyEpaye[SjRequest.Epaye.Empty](Origins.Epaye.DetachedUrl)

  private def startJourneyEpaye[StartRequest <: SjRequest.Epaye: Reads](origin: Origins.Epaye): Action[StartRequest] =
    actions.authenticatedAction.async(parse.json[StartRequest]) { implicit request =>
      val originatedSjRequest = OriginatedSjRequest.Epaye(origin, request.body)
      doJourneyStart(originatedSjRequest)
    }

  val startJourneyVatFromBta: Action[SjRequest.Vat.Simple] = startJourneyVat[SjRequest.Vat.Simple](Origins.Vat.Bta)

  val startJourneyVatFromVatService: Action[SjRequest.Vat.Simple] = startJourneyVat[SjRequest.Vat.Simple](Origins.Vat.VatService)

  val startJourneyVatFromGovUk: Action[SjRequest.Vat.Empty] = startJourneyVat[SjRequest.Vat.Empty](Origins.Vat.GovUk)

  val startJourneyVatFromDetachedUrl: Action[SjRequest.Vat.Empty] = startJourneyVat[SjRequest.Vat.Empty](Origins.Vat.DetachedUrl)

  val startJourneyVatFroVatPenalties: Action[SjRequest.Vat.Simple] = startJourneyVat[SjRequest.Vat.Simple](Origins.Vat.VatPenalties)

  private def startJourneyVat[StartRequest <: SjRequest.Vat: Reads](origin: Origins.Vat): Action[StartRequest] =
    actions.authenticatedAction.async(parse.json[StartRequest]) { implicit request =>
      val originatedSjRequest = OriginatedSjRequest.Vat(origin, request.body)
      doJourneyStart(originatedSjRequest)
    }

  val startJourneySaFromBta: Action[SjRequest.Sa.Simple] = startJourneySa[SjRequest.Sa.Simple](Origins.Sa.Bta)

  val startJourneySaFromPta: Action[SjRequest.Sa.Simple] = startJourneySa[SjRequest.Sa.Simple](Origins.Sa.Pta)

  val startJourneySaFromMobile: Action[SjRequest.Sa.Simple] = startJourneySa[SjRequest.Sa.Simple](Origins.Sa.Mobile)

  val startJourneySaFromGovUk: Action[SjRequest.Sa.Empty] = startJourneySa[SjRequest.Sa.Empty](Origins.Sa.GovUk)

  val startJourneySaFromDetachedUrl: Action[SjRequest.Sa.Empty] = startJourneySa[SjRequest.Sa.Empty](Origins.Sa.DetachedUrl)

  private def startJourneySa[StartRequest <: SjRequest.Sa: Reads](origin: Origins.Sa): Action[StartRequest] =
    actions.authenticatedAction.async(parse.json[StartRequest]) { implicit request =>
      val originatedSjRequest = OriginatedSjRequest.Sa(origin, request.body)
      doJourneyStart(originatedSjRequest)
    }

  private def doJourneyStart(
      originatedRequest: OriginatedSjRequest
  )(implicit request: Request[_]): Future[Result] = {
    val journey: Journey = journeyFactory.makeJourney(originatedRequest, RequestSupport.getSessionId())

    journeyService.upsert(journey).map { _ =>
      val description: String = journeyDescription(originatedRequest.origin)
      val nextUrl: NextUrl = NextUrl(s"${journeyConfig.nextUrlHost}/set-up-a-payment-plan${originToRelativeUrl(originatedRequest.origin)}")
      val sjResponse: SjResponse = SjResponse(nextUrl, journey.journeyId)
      val response: Result = Created(Json.toJson(sjResponse))
      JourneyLogger.info(s"Started $description [journeyId:${journey.id.toString}]")
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
      case Origins.Vat.Bta          => "Journey for Vat from BTA"
      case Origins.Vat.VatService   => "Journey for Vat from VAT service"
      case Origins.Vat.GovUk        => "Journey for Vat from GovUk"
      case Origins.Vat.DetachedUrl  => "Journey for Vat from DetachedUrl"
      case Origins.Vat.VatPenalties => "Journey for Vat from VAT Penalties"
    }
    case o: Origins.Sa => o match {
      case Sa.Bta         => "Journey for Sa from BTA"
      case Sa.Pta         => "Journey for Sa from PTA"
      case Sa.Mobile      => "Journey for Sa from Mobile"
      case Sa.GovUk       => "Journey for Sa from GovUk"
      case Sa.DetachedUrl => "Journey for Sa from DetachedUrl"
    }
  }

  private def originToRelativeUrl(origin: Origin): String = origin match {
    case _: Origins.Epaye => "/epaye-payment-plan"
    case _: Origins.Vat   => "/vat-payment-plan"
    case _: Origins.Sa    => "/sa-payment-plan"
  }

}
