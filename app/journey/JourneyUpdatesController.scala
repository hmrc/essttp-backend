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
import essttp.journey.model.Journey.Epaye
import essttp.journey.model._
import essttp.rootmodel.{Aor, SessionId, TaxId}
import essttp.rootmodel.epaye.{TaxOfficeNumber, TaxOfficeReference}
import essttp.utils.Errors
import play.api.libs.json.{Json, OFormat}
import play.api.mvc._
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.play.bootstrap.backend.http.ErrorResponse

import scala.concurrent.{ExecutionContext, Future}
import io.scalaland.chimney.dsl._

final case class UpdateTaxIdRequest(
    taxId: TaxId
)

object UpdateTaxIdRequest {
  implicit val format: OFormat[UpdateTaxIdRequest] = Json.format[UpdateTaxIdRequest]
}

/**
 * Start Journey (Sj) Controller
 */
class JourneyUpdatesController @Inject() (
    journeyService: JourneyService,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext) extends BackendController(cc) {

  def updateTaxId(journeyId: JourneyId): Action[UpdateTaxIdRequest] = Action.async(parse.json[UpdateTaxIdRequest]) { implicit request =>
    for {
      journey <- journeyService.get(journeyId)
      _ <- journey match {
        case j: Journey.Epaye => updateAor(j, request.body)
      }
    } yield Ok
  }

  private def updateAor(journey: Journey.Epaye, updateTaxIdRequest: UpdateTaxIdRequest)(implicit request: Request[_]): Future[Unit] = {
    val aor = updateTaxIdRequest.taxId match {
      case aor: Aor => aor
      case _        => Errors.throwBadRequestException("TaxId for Epaye must be Aor")
    }
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
      case j: Journey.HasTaxId if j.taxId != updateTaxIdRequest.taxId =>
        val m = "Journey already has a tax id with a different value."
        JourneyLogger.error(m)
        Future.failed(new UnsupportedOperationException(m))
    }
  }

  def updateEligibilityResult(journeyId: JourneyId): Action[AnyContent] = Action.async { implicit request =>
    Future.failed(new NotImplementedError("TODO pawel"))
  }

}
