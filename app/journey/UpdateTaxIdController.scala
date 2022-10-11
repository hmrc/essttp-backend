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
import com.google.inject.{Inject, Singleton}
import essttp.journey.model._
import essttp.rootmodel.{EmpRef, TaxId, Vrn}
import essttp.utils.Errors
import io.scalaland.chimney.dsl._
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateTaxIdController @Inject() (
    actions:        Actions,
    journeyService: JourneyService,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext) extends BackendController(cc) {

  def updateTaxId(journeyId: JourneyId): Action[TaxId] = actions.authenticatedAction.async(parse.json[TaxId]) { implicit request =>
    for {
      journey <- journeyService.get(journeyId)
      _ <- updateJourney(journey, request.body)
    } yield Ok
  }

  private def updateJourney(journey: Journey, taxId: TaxId)(implicit request: Request[_]): Future[Unit] = {
    journey match {
      case j: Journey.Epaye.Started =>
        taxId match {
          case empRef: EmpRef =>
            val newJourney: Journey.Epaye.ComputedTaxId =
              j.into[Journey.Epaye.ComputedTaxId]
                .withFieldConst(_.stage, Stage.AfterComputedTaxId.ComputedTaxId)
                .withFieldConst(_.taxId, empRef)
                .transform
            journeyService.upsert(newJourney)
          case _: Vrn => Errors.throwBadRequestExceptionF("Why is there a vrn, this is for EPAYE...")
        }
      case j: Journey.AfterComputedTaxId =>
        Errors.throwBadRequestExceptionF(s"UpdateTaxId is not possible in this stage, why is it happening? Debug me... [${j.stage}]")
    }
  }
}
