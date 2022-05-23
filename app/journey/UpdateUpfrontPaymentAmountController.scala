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

import cats.syntax.eq._

import com.google.inject.{Inject, Singleton}
import essttp.journey.model.{Journey, JourneyId, Stage}
import essttp.rootmodel.UpfrontPaymentAmount
import essttp.utils.Errors
import play.api.mvc.{Action, ControllerComponents, Request}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import io.scalaland.chimney.dsl.TransformerOps

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateUpfrontPaymentAmountController @Inject() (
    journeyService: JourneyService,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext) extends BackendController(cc) {

  def updateUpfrontPaymentAmount(journeyId: JourneyId): Action[UpfrontPaymentAmount] = Action.async(parse.json[UpfrontPaymentAmount]) { implicit request =>
    for {
      journey <- journeyService.get(journeyId)
      _ <- updateJourney(journey, request.body)
    } yield Ok
  }

  private def updateJourney(journey: Journey, upfrontPaymentAmount: UpfrontPaymentAmount)(implicit request: Request[_]): Future[Unit] = {
    journey match {
      case j: Journey.Epaye.AfterStarted =>
        Errors.throwBadRequestExceptionF(s"UpdateUpfrontPaymentAmount update is not possible in that state: [${j.stage.entryName}]")
      case j: Journey.Epaye.AfterComputedTaxIds =>
        Errors.throwBadRequestExceptionF(s"UpdateUpfrontPaymentAmount update is not possible in that state: [${j.stage.entryName}]")
      case j: Journey.Epaye.AfterEligibilityCheck =>
        Errors.throwBadRequestExceptionF(s"UpdateUpfrontPaymentAmount update is not possible in that state: [${j.stage.entryName}]")
      case j: Journey.Epaye.AfterCanPayUpfront if j.canPayUpfront.value =>
        val newJourney: Journey.Epaye.AfterUpfrontPaymentAmount =
          j.into[Journey.Epaye.AfterUpfrontPaymentAmount]
            .withFieldConst(_.stage, Stage.AfterUpfrontPaymentAmount.EnteredUpfrontPaymentAmount)
            .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
            .transform
        journeyService.upsert(newJourney)
      case j: Journey.Epaye.AfterCanPayUpfront if !j.canPayUpfront.value =>
        Errors.throwBadRequestExceptionF(s"UpdateUpfrontPaymentAmount update is not possible when user has selected [No] for CanPayUpfront: [${j.stage.entryName}]")
      case j: Journey.HasUpfrontPaymentAmount if j.upfrontPaymentAmount.value.value === upfrontPaymentAmount.value.value =>
        JourneyLogger.info("Nothing to update, UpfrontPaymentAmount is the same as the existing one in journey.")
        Future.successful(())
      case j: Journey.Epaye.AfterUpfrontPaymentAmount =>
        val updatedJourney: Journey.Epaye.AfterUpfrontPaymentAmount = j.copy(upfrontPaymentAmount = upfrontPaymentAmount)
        journeyService.upsert(updatedJourney)
    }
  }
}
