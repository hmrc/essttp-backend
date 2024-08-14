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
import cats.syntax.eq._
import com.google.inject.{Inject, Singleton}
import essttp.crypto.CryptoFormat.OperationalCryptoFormat
import essttp.journey.model.Journey.{Epaye, Sa, Stages, Vat}
import essttp.journey.model.{Journey, JourneyId, Stage}
import essttp.rootmodel.pega.StartCaseResponse
import essttp.utils.Errors
import io.scalaland.chimney.dsl.TransformationOps
import play.api.mvc.{Action, ControllerComponents, Request}
import services.JourneyService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdatePegaStartCaseResponseController @Inject() (
    actions:        Actions,
    journeyService: JourneyService,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext, cryptoFormat: OperationalCryptoFormat) extends BackendController(cc) {

  def updateStartCaseResponse(journeyId: JourneyId): Action[StartCaseResponse] = actions.authenticatedAction.async(parse.json[StartCaseResponse]) { implicit request =>
    for {
      journey <- journeyService.get(journeyId)
      newJourney <- journey match {
        case j: Journey.BeforeCanPayWithinSixMonthsAnswers          => Errors.throwBadRequestExceptionF(s"UpdatePegaStartCaseResponse update is not possible in that state: [${j.stage.toString}]")
        case j: Journey.Stages.ObtainedCanPayWithinSixMonthsAnswers => updateJourneyWithNewValue(j, request.body)
        case j: Journey.AfterStartedPegaCase                        => updateJourneyWithExistingValue(j, request.body)
        case _: Journey.AfterEnteredMonthlyPaymentAmount            => Errors.throwBadRequestExceptionF("update PEGA start case response not expected after entered monthly payment amount")
      }
    } yield Ok(newJourney.json)
  }

  private def updateJourneyWithNewValue(
      journey:           Stages.ObtainedCanPayWithinSixMonthsAnswers,
      startCaseResponse: StartCaseResponse
  )(implicit request: Request[_]): Future[Journey] = {
    val newJourney: Journey.AfterStartedPegaCase = journey match {
      case j: Epaye.ObtainedCanPayWithinSixMonthsAnswers =>
        j.into[Epaye.StartedPegaCase]
          .withFieldConst(_.stage, Stage.AfterStartedPegaCase.StartedPegaCase)
          .withFieldConst(_.startCaseResponse, startCaseResponse)
          .transform
      case j: Vat.ObtainedCanPayWithinSixMonthsAnswers =>
        j.into[Vat.StartedPegaCase]
          .withFieldConst(_.stage, Stage.AfterStartedPegaCase.StartedPegaCase)
          .withFieldConst(_.startCaseResponse, startCaseResponse)
          .transform
      case j: Sa.ObtainedCanPayWithinSixMonthsAnswers =>
        j.into[Sa.StartedPegaCase]
          .withFieldConst(_.stage, Stage.AfterStartedPegaCase.StartedPegaCase)
          .withFieldConst(_.startCaseResponse, startCaseResponse)
          .transform
    }
    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingValue(
      journey:           Journey.AfterStartedPegaCase,
      startCaseResponse: StartCaseResponse
  )(implicit request: Request[_]): Future[Journey] = {

    if (journey.startCaseResponse === startCaseResponse)
      Future.successful(journey)
    else {
      val updatedJourney: Journey = journey match {
        case j: Epaye.StartedPegaCase =>
          j.copy(startCaseResponse = startCaseResponse)
        case j: Vat.StartedPegaCase =>
          j.copy(startCaseResponse = startCaseResponse)
        case j: Sa.StartedPegaCase =>
          j.copy(startCaseResponse = startCaseResponse)
      }

      journeyService.upsert(updatedJourney)
    }
  }

}
