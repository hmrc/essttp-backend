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
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import com.google.inject.{Inject, Singleton}
import essttp.crypto.CryptoFormat.OperationalCryptoFormat
import essttp.journey.model.*
import essttp.rootmodel.{EmpRef, Nino, SaUtr, TaxId, TaxRegime, Vrn}
import essttp.utils.Errors
import io.scalaland.chimney.dsl.*
import play.api.mvc.*
import services.JourneyService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateTaxIdController @Inject() (
  actions:        Actions,
  journeyService: JourneyService,
  cc:             ControllerComponents
)(using ExecutionContext, OperationalCryptoFormat)
    extends BackendController(cc) {

  def updateTaxId(journeyId: JourneyId): Action[TaxId] = actions.authenticatedAction.async(parse.json[TaxId]) {
    implicit request =>
      for {
        journey    <- journeyService.get(journeyId)
        newJourney <- updateJourney(journey, request.body)
      } yield Ok(newJourney.json)
  }

  private def updateJourney(journey: Journey, taxId: TaxId)(using Request[?]): Future[Journey] =
    journey match {
      case j: Journey.Started =>
        val validatedId: Validated[String, TaxId] = (j.origin.taxRegime, taxId) match {
          case (TaxRegime.Epaye, id: EmpRef) => Valid(id)
          case (TaxRegime.Vat, id: Vrn)      => Valid(id)
          case (TaxRegime.Sa, id: SaUtr)     => Valid(id)
          case (TaxRegime.Simp, id: Nino)    => Valid(id)
          case (regime, id)                  =>
            Invalid(s"Why is there a ${id.getClass.getSimpleName}, this is for ${regime.entryName}...")
        }

        validatedId.fold(
          message => Errors.throwBadRequestExceptionF(message),
          { id =>
            val newJourney = j
              .into[Journey.ComputedTaxId]
              .withFieldConst(_.taxId, id)
              .transform
            journeyService.upsert(newJourney)
          }
        )

      case j: JourneyStage.AfterComputedTaxId =>
        Errors.throwBadRequestExceptionF(
          s"UpdateTaxId is not possible in this stage, why is it happening? Debug me... [${j.stage}]"
        )
    }

}
