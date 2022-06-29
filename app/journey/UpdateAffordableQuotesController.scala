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
import essttp.journey.model.ttp.affordablequotes.AffordableQuotesResponse
import essttp.journey.model.{Journey, JourneyId, Stage}
import essttp.utils.Errors
import io.scalaland.chimney.dsl.TransformerOps
import play.api.mvc.{Action, ControllerComponents, Request}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateAffordableQuotesController @Inject() (
    journeyService: JourneyService,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext) extends BackendController(cc) {

  def updateAffordableQuotes(journeyId: JourneyId): Action[AffordableQuotesResponse] = Action.async(parse.json[AffordableQuotesResponse]) { implicit request =>
    for {
      journey <- journeyService.get(journeyId)
      _ <- journey match {
        case j: Journey.BeforeStartDatesResponse      => Errors.throwBadRequestExceptionF(s"UpdateAffordableQuotes is not possible in that state: [${j.stage}]")
        case j: Journey.Stages.RetrievedStartDates    => updateJourneyWithNewValue(j, request.body)
        case j: Journey.AfterAffordableQuotesResponse => updateJourneyWithExistingValue(j, request.body)
      }
    } yield Ok
  }

  private def updateJourneyWithNewValue(
      journey:                  Journey.Stages.RetrievedStartDates,
      affordableQuotesResponse: AffordableQuotesResponse
  )(implicit request: Request[_]): Future[Unit] = {
    val newJourney: Journey.AfterAffordableQuotesResponse = journey match {
      case j: Journey.Epaye.RetrievedStartDates =>
        j.into[Journey.Epaye.RetrievedAffordableQuotes]
          .withFieldConst(_.stage, Stage.AfterAffordableQuotesResponse.AffordableQuotesRetrieved)
          .withFieldConst(_.affordableQuotesResponse, affordableQuotesResponse)
          .transform
    }
    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingValue(
      journey:                  Journey.AfterAffordableQuotesResponse,
      affordableQuotesResponse: AffordableQuotesResponse
  )(implicit request: Request[_]): Future[Unit] = {
    if (journey.affordableQuotesResponse === affordableQuotesResponse) {
      JourneyLogger.info("Nothing to update, AffordableQuotesResponse is the same as the existing one in journey.")
      Future.successful(())
    } else {
      val newJourney: Journey.AfterAffordableQuotesResponse = journey match {
        case j: Journey.Epaye.RetrievedAffordableQuotes =>
          j.copy(affordableQuotesResponse = affordableQuotesResponse)
        case j: Journey.Epaye.ChosenPaymentPlan =>
          j.into[Journey.Epaye.RetrievedAffordableQuotes]
            .withFieldConst(_.stage, Stage.AfterAffordableQuotesResponse.AffordableQuotesRetrieved)
            .withFieldConst(_.affordableQuotesResponse, affordableQuotesResponse)
            .transform
        case j: Journey.Epaye.CheckedPaymentPlan =>
          j.into[Journey.Epaye.RetrievedAffordableQuotes]
            .withFieldConst(_.stage, Stage.AfterAffordableQuotesResponse.AffordableQuotesRetrieved)
            .withFieldConst(_.affordableQuotesResponse, affordableQuotesResponse)
            .transform
      }
      journeyService.upsert(newJourney)
    }
  }
}
