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
import essttp.crypto.CryptoFormat.OperationalCryptoFormat
import essttp.journey.model._
import essttp.rootmodel.{EmpRef, SaUtr, TaxId, Vrn, Nino}
import essttp.utils.Errors
import io.scalaland.chimney.dsl._
import play.api.mvc._
import services.JourneyService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateTaxIdController @Inject() (
    actions:        Actions,
    journeyService: JourneyService,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext, cryptoFormat: OperationalCryptoFormat) extends BackendController(cc) {

  def updateTaxId(journeyId: JourneyId): Action[TaxId] = actions.authenticatedAction.async(parse.json[TaxId]) { implicit request =>
    for {
      journey <- journeyService.get(journeyId)
      newJourney <- updateJourney(journey, request.body)
    } yield Ok(newJourney.json)
  }

  private def updateJourney(journey: Journey, taxId: TaxId)(implicit request: Request[_]): Future[Journey] = {
    journey match {
      case j: Journey.Epaye.Started =>
        taxId match {
          case empRef: EmpRef => journeyService.upsert(asEpayeComputedTaxId(j, empRef))
          case other          => Errors.throwBadRequestExceptionF(s"Why is there a ${other.getClass.getSimpleName}, this is for EPAYE...")
        }
      case j: Journey.Vat.Started =>
        taxId match {
          case vrn: Vrn => journeyService.upsert(asVatComputedTaxId(j, vrn))
          case other    => Errors.throwBadRequestExceptionF(s"Why is there a ${other.getClass.getSimpleName}, this is for Vat...")
        }
      case j: Journey.Sa.Started =>
        taxId match {
          case saUtr: SaUtr => journeyService.upsert(asSaComputedTaxId(j, saUtr))
          case other        => Errors.throwBadRequestExceptionF(s"Why is there a ${other.getClass.getSimpleName}, this is for Sa...")
        }
      case j: Journey.Simp.Started =>
        taxId match {
          case nino: Nino => journeyService.upsert(asSimpComputedTaxId(j, nino))
          case other      => Errors.throwBadRequestExceptionF(s"Why is there a ${other.getClass.getSimpleName}, this is for Simp...")
        }
      case j: Journey.AfterComputedTaxId =>
        Errors.throwBadRequestExceptionF(s"UpdateTaxId is not possible in this stage, why is it happening? Debug me... [${j.stage.toString}]")
    }
  }

  private def asEpayeComputedTaxId(journey: Journey.Epaye.Started, empRef: EmpRef): Journey.Epaye.ComputedTaxId =
    journey.into[Journey.Epaye.ComputedTaxId]
      .withFieldConst(_.stage, Stage.AfterComputedTaxId.ComputedTaxId)
      .withFieldConst(_.taxId, empRef)
      .transform

  private def asVatComputedTaxId(journey: Journey.Vat.Started, vrn: Vrn): Journey.Vat.ComputedTaxId =
    journey.into[Journey.Vat.ComputedTaxId]
      .withFieldConst(_.stage, Stage.AfterComputedTaxId.ComputedTaxId)
      .withFieldConst(_.taxId, vrn)
      .transform

  private def asSaComputedTaxId(journey: Journey.Sa.Started, saUtr: SaUtr): Journey.Sa.ComputedTaxId =
    journey.into[Journey.Sa.ComputedTaxId]
      .withFieldConst(_.stage, Stage.AfterComputedTaxId.ComputedTaxId)
      .withFieldConst(_.taxId, saUtr)
      .transform

  private def asSimpComputedTaxId(journey: Journey.Simp.Started, nino: Nino): Journey.Simp.ComputedTaxId =
    journey.into[Journey.Simp.ComputedTaxId]
      .withFieldConst(_.stage, Stage.AfterComputedTaxId.ComputedTaxId)
      .withFieldConst(_.taxId, nino)
      .transform

}
