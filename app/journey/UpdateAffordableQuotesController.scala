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
import cats.syntax.eq._
import com.google.inject.{Inject, Singleton}
import essttp.crypto.CryptoFormat.OperationalCryptoFormat
import essttp.journey.model.{Journey, JourneyId, Stage}
import essttp.rootmodel.ttp.affordablequotes.AffordableQuotesResponse
import essttp.utils.Errors
import io.scalaland.chimney.dsl.TransformerOps
import play.api.mvc.{Action, ControllerComponents, Request}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateAffordableQuotesController @Inject() (
    actions:        Actions,
    journeyService: JourneyService,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext, cryptoFormat: OperationalCryptoFormat) extends BackendController(cc) {

  def updateAffordableQuotes(journeyId: JourneyId): Action[AffordableQuotesResponse] = actions.authenticatedAction.async(parse.json[AffordableQuotesResponse]) { implicit request =>
    for {
      journey <- journeyService.get(journeyId)
      newJourney <- journey match {
        case j: Journey.BeforeStartDatesResponse   => Errors.throwBadRequestExceptionF(s"UpdateAffordableQuotes is not possible in that state: [${j.stage.toString}]")
        case j: Journey.Stages.RetrievedStartDates => updateJourneyWithNewValue(j, request.body)
        case j: Journey.AfterAffordableQuotesResponse => j match {
          case _: Journey.BeforeArrangementSubmitted => updateJourneyWithExistingValue(j, request.body)
          case _: Journey.AfterArrangementSubmitted  => Errors.throwBadRequestExceptionF("Cannot update AffordableQuotes when journey is in completed state")
        }
      }
    } yield Ok(newJourney.json)
  }

  private def updateJourneyWithNewValue(
      journey:                  Journey.Stages.RetrievedStartDates,
      affordableQuotesResponse: AffordableQuotesResponse
  )(implicit request: Request[_]): Future[Journey] = {
    val newJourney: Journey.AfterAffordableQuotesResponse = journey match {
      case j: Journey.Epaye.RetrievedStartDates =>
        j.into[Journey.Epaye.RetrievedAffordableQuotes]
          .withFieldConst(_.stage, Stage.AfterAffordableQuotesResponse.AffordableQuotesRetrieved)
          .withFieldConst(_.affordableQuotesResponse, affordableQuotesResponse)
          .transform
      case j: Journey.Vat.RetrievedStartDates =>
        j.into[Journey.Vat.RetrievedAffordableQuotes]
          .withFieldConst(_.stage, Stage.AfterAffordableQuotesResponse.AffordableQuotesRetrieved)
          .withFieldConst(_.affordableQuotesResponse, affordableQuotesResponse)
          .transform
    }
    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingValue(
      journey:                  Journey.AfterAffordableQuotesResponse,
      affordableQuotesResponse: AffordableQuotesResponse
  )(implicit request: Request[_]): Future[Journey] = {
    if (journey.affordableQuotesResponse === affordableQuotesResponse) {
      JourneyLogger.info("Nothing to update, AffordableQuotesResponse is the same as the existing one in journey.")
      Future.successful(journey)
    } else {
      val newJourney: Journey.AfterAffordableQuotesResponse = journey match {

        case j: Journey.Epaye.RetrievedAffordableQuotes => j.copy(affordableQuotesResponse = affordableQuotesResponse)
        case j: Journey.Vat.RetrievedAffordableQuotes   => j.copy(affordableQuotesResponse = affordableQuotesResponse)

        case j: Journey.Epaye.ChosenPaymentPlan =>
          j.into[Journey.Epaye.RetrievedAffordableQuotes]
            .withFieldConst(_.stage, Stage.AfterAffordableQuotesResponse.AffordableQuotesRetrieved)
            .withFieldConst(_.affordableQuotesResponse, affordableQuotesResponse)
            .transform
        case j: Journey.Vat.ChosenPaymentPlan =>
          j.into[Journey.Vat.RetrievedAffordableQuotes]
            .withFieldConst(_.stage, Stage.AfterAffordableQuotesResponse.AffordableQuotesRetrieved)
            .withFieldConst(_.affordableQuotesResponse, affordableQuotesResponse)
            .transform

        case j: Journey.Epaye.CheckedPaymentPlan =>
          j.into[Journey.Epaye.RetrievedAffordableQuotes]
            .withFieldConst(_.stage, Stage.AfterAffordableQuotesResponse.AffordableQuotesRetrieved)
            .withFieldConst(_.affordableQuotesResponse, affordableQuotesResponse)
            .transform
        case j: Journey.Vat.CheckedPaymentPlan =>
          j.into[Journey.Vat.RetrievedAffordableQuotes]
            .withFieldConst(_.stage, Stage.AfterAffordableQuotesResponse.AffordableQuotesRetrieved)
            .withFieldConst(_.affordableQuotesResponse, affordableQuotesResponse)
            .transform

        case j: Journey.Epaye.EnteredDetailsAboutBankAccount =>
          j.into[Journey.Epaye.RetrievedAffordableQuotes]
            .withFieldConst(_.stage, Stage.AfterAffordableQuotesResponse.AffordableQuotesRetrieved)
            .withFieldConst(_.affordableQuotesResponse, affordableQuotesResponse)
            .transform
        case j: Journey.Vat.EnteredDetailsAboutBankAccount =>
          j.into[Journey.Vat.RetrievedAffordableQuotes]
            .withFieldConst(_.stage, Stage.AfterAffordableQuotesResponse.AffordableQuotesRetrieved)
            .withFieldConst(_.affordableQuotesResponse, affordableQuotesResponse)
            .transform

        case j: Journey.Epaye.EnteredDirectDebitDetails =>
          j.into[Journey.Epaye.RetrievedAffordableQuotes]
            .withFieldConst(_.stage, Stage.AfterAffordableQuotesResponse.AffordableQuotesRetrieved)
            .withFieldConst(_.affordableQuotesResponse, affordableQuotesResponse)
            .transform
        case j: Journey.Vat.EnteredDirectDebitDetails =>
          j.into[Journey.Vat.RetrievedAffordableQuotes]
            .withFieldConst(_.stage, Stage.AfterAffordableQuotesResponse.AffordableQuotesRetrieved)
            .withFieldConst(_.affordableQuotesResponse, affordableQuotesResponse)
            .transform

        case j: Journey.Epaye.ConfirmedDirectDebitDetails =>
          j.into[Journey.Epaye.RetrievedAffordableQuotes]
            .withFieldConst(_.stage, Stage.AfterAffordableQuotesResponse.AffordableQuotesRetrieved)
            .withFieldConst(_.affordableQuotesResponse, affordableQuotesResponse)
            .transform
        case j: Journey.Vat.ConfirmedDirectDebitDetails =>
          j.into[Journey.Vat.RetrievedAffordableQuotes]
            .withFieldConst(_.stage, Stage.AfterAffordableQuotesResponse.AffordableQuotesRetrieved)
            .withFieldConst(_.affordableQuotesResponse, affordableQuotesResponse)
            .transform

        case j: Journey.Epaye.AgreedTermsAndConditions =>
          j.into[Journey.Epaye.RetrievedAffordableQuotes]
            .withFieldConst(_.stage, Stage.AfterAffordableQuotesResponse.AffordableQuotesRetrieved)
            .withFieldConst(_.affordableQuotesResponse, affordableQuotesResponse)
            .transform
        case j: Journey.Vat.AgreedTermsAndConditions =>
          j.into[Journey.Vat.RetrievedAffordableQuotes]
            .withFieldConst(_.stage, Stage.AfterAffordableQuotesResponse.AffordableQuotesRetrieved)
            .withFieldConst(_.affordableQuotesResponse, affordableQuotesResponse)
            .transform

        case j: Journey.Epaye.SelectedEmailToBeVerified =>
          j.into[Journey.Epaye.RetrievedAffordableQuotes]
            .withFieldConst(_.stage, Stage.AfterAffordableQuotesResponse.AffordableQuotesRetrieved)
            .withFieldConst(_.affordableQuotesResponse, affordableQuotesResponse)
            .transform
        case j: Journey.Epaye.EmailVerificationComplete =>
          j.into[Journey.Epaye.RetrievedAffordableQuotes]
            .withFieldConst(_.stage, Stage.AfterAffordableQuotesResponse.AffordableQuotesRetrieved)
            .withFieldConst(_.affordableQuotesResponse, affordableQuotesResponse)
            .transform
        case _: Journey.Epaye.SubmittedArrangement =>
          Errors.throwBadRequestException("Cannot update AffordableQuotes when journey is in completed state")
      }

      journeyService.upsert(newJourney)
    }
  }
}
