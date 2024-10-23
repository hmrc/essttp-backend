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
import essttp.journey.model.{Journey, JourneyId, Stage}
import essttp.rootmodel.bank.BankDetails
import essttp.utils.Errors
import io.scalaland.chimney.dsl.TransformationOps
import play.api.mvc.{Action, ControllerComponents, Request}
import services.JourneyService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateDirectDebitDetailsController @Inject() (
    actions:        Actions,
    journeyService: JourneyService,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext, cryptoFormat: OperationalCryptoFormat) extends BackendController(cc) {

  def updateDirectDebitDetails(journeyId: JourneyId): Action[BankDetails] = actions.authenticatedAction.async(parse.json[BankDetails]) { implicit request =>
    for {
      journey <- journeyService.get(journeyId)
      newJourney <- journey match {
        case j: Journey.BeforeEnteredCanYouSetUpDirectDebit  => Errors.throwBadRequestExceptionF(s"UpdateDirectDebitDetails is not possible in that state: [${j.stage.toString}]")
        case j: Journey.Stages.EnteredCanYouSetUpDirectDebit => updateJourneyWithNewValue(j, request.body)
        case j: Journey.AfterEnteredDirectDebitDetails => j match {
          case _: Journey.BeforeArrangementSubmitted => updateJourneyWithExistingValue(j, request.body)
          case _: Journey.AfterArrangementSubmitted  => Errors.throwBadRequestExceptionF("Cannot update DirectDebitDetails when journey is in completed state")
        }
      }
    } yield Ok(newJourney.json)
  }

  private def updateJourneyWithNewValue(
      journey:            Journey.Stages.EnteredCanYouSetUpDirectDebit,
      directDebitDetails: BankDetails
  )(implicit request: Request[_]): Future[Journey] = {
    val newJourney: Journey.AfterEnteredDirectDebitDetails = journey match {
      case j: Journey.Epaye.EnteredCanYouSetUpDirectDebit =>
        j.into[Journey.Epaye.EnteredDirectDebitDetails]
          .withFieldConst(_.stage, Stage.AfterEnteredDirectDebitDetails.EnteredDirectDebitDetails)
          .withFieldConst(_.directDebitDetails, directDebitDetails)
          .transform
      case j: Journey.Vat.EnteredCanYouSetUpDirectDebit =>
        j.into[Journey.Vat.EnteredDirectDebitDetails]
          .withFieldConst(_.stage, Stage.AfterEnteredDirectDebitDetails.EnteredDirectDebitDetails)
          .withFieldConst(_.directDebitDetails, directDebitDetails)
          .transform
      case j: Journey.Sa.EnteredCanYouSetUpDirectDebit =>
        j.into[Journey.Sa.EnteredDirectDebitDetails]
          .withFieldConst(_.stage, Stage.AfterEnteredDirectDebitDetails.EnteredDirectDebitDetails)
          .withFieldConst(_.directDebitDetails, directDebitDetails)
          .transform
      case j: Journey.Sia.EnteredCanYouSetUpDirectDebit =>
        j.into[Journey.Sia.EnteredDirectDebitDetails]
          .withFieldConst(_.stage, Stage.AfterEnteredDirectDebitDetails.EnteredDirectDebitDetails)
          .withFieldConst(_.directDebitDetails, directDebitDetails)
          .transform
    }
    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingValue(
      journey:            Journey.AfterEnteredDirectDebitDetails,
      directDebitDetails: BankDetails
  )(implicit request: Request[_]): Future[Journey] = {
    if (journey.directDebitDetails === directDebitDetails) {
      JourneyLogger.info("Direct debit details haven't changed, nothing to update")
      Future.successful(journey)
    } else {
      val updatedJourney: Journey.AfterEnteredDirectDebitDetails = journey match {

        case j: Journey.Epaye.EnteredDirectDebitDetails =>
          j.copy(
            directDebitDetails = directDebitDetails,
            stage              = Stage.AfterEnteredDirectDebitDetails.EnteredDirectDebitDetails
          )
        case j: Journey.Vat.EnteredDirectDebitDetails =>
          j.copy(
            directDebitDetails = directDebitDetails,
            stage              = Stage.AfterEnteredDirectDebitDetails.EnteredDirectDebitDetails
          )
        case j: Journey.Sa.EnteredDirectDebitDetails =>
          j.copy(
            directDebitDetails = directDebitDetails,
            stage              = Stage.AfterEnteredDirectDebitDetails.EnteredDirectDebitDetails
          )
        case j: Journey.Sia.EnteredDirectDebitDetails =>
          j.copy(
            directDebitDetails = directDebitDetails,
            stage              = Stage.AfterEnteredDirectDebitDetails.EnteredDirectDebitDetails
          )

        case j: Journey.Epaye.ConfirmedDirectDebitDetails =>
          j.into[Journey.Epaye.EnteredDirectDebitDetails]
            .withFieldConst(_.directDebitDetails, directDebitDetails)
            .withFieldConst(_.stage, Stage.AfterEnteredDirectDebitDetails.EnteredDirectDebitDetails)
            .transform
        case j: Journey.Vat.ConfirmedDirectDebitDetails =>
          j.into[Journey.Vat.EnteredDirectDebitDetails]
            .withFieldConst(_.directDebitDetails, directDebitDetails)
            .withFieldConst(_.stage, Stage.AfterEnteredDirectDebitDetails.EnteredDirectDebitDetails)
            .transform
        case j: Journey.Sa.ConfirmedDirectDebitDetails =>
          j.into[Journey.Sa.EnteredDirectDebitDetails]
            .withFieldConst(_.directDebitDetails, directDebitDetails)
            .withFieldConst(_.stage, Stage.AfterEnteredDirectDebitDetails.EnteredDirectDebitDetails)
            .transform
        case j: Journey.Sia.ConfirmedDirectDebitDetails =>
          j.into[Journey.Sia.EnteredDirectDebitDetails]
            .withFieldConst(_.directDebitDetails, directDebitDetails)
            .withFieldConst(_.stage, Stage.AfterEnteredDirectDebitDetails.EnteredDirectDebitDetails)
            .transform

        case j: Journey.Epaye.AgreedTermsAndConditions =>
          j.into[Journey.Epaye.EnteredDirectDebitDetails]
            .withFieldConst(_.directDebitDetails, directDebitDetails)
            .withFieldConst(_.stage, Stage.AfterEnteredDirectDebitDetails.EnteredDirectDebitDetails)
            .transform
        case j: Journey.Vat.AgreedTermsAndConditions =>
          j.into[Journey.Vat.EnteredDirectDebitDetails]
            .withFieldConst(_.directDebitDetails, directDebitDetails)
            .withFieldConst(_.stage, Stage.AfterEnteredDirectDebitDetails.EnteredDirectDebitDetails)
            .transform
        case j: Journey.Sa.AgreedTermsAndConditions =>
          j.into[Journey.Sa.EnteredDirectDebitDetails]
            .withFieldConst(_.directDebitDetails, directDebitDetails)
            .withFieldConst(_.stage, Stage.AfterEnteredDirectDebitDetails.EnteredDirectDebitDetails)
            .transform
        case j: Journey.Sia.AgreedTermsAndConditions =>
          j.into[Journey.Sia.EnteredDirectDebitDetails]
            .withFieldConst(_.directDebitDetails, directDebitDetails)
            .withFieldConst(_.stage, Stage.AfterEnteredDirectDebitDetails.EnteredDirectDebitDetails)
            .transform

        case j: Journey.Epaye.SelectedEmailToBeVerified =>
          j.into[Journey.Epaye.EnteredDirectDebitDetails]
            .withFieldConst(_.directDebitDetails, directDebitDetails)
            .withFieldConst(_.stage, Stage.AfterEnteredDirectDebitDetails.EnteredDirectDebitDetails)
            .transform
        case j: Journey.Vat.SelectedEmailToBeVerified =>
          j.into[Journey.Vat.EnteredDirectDebitDetails]
            .withFieldConst(_.directDebitDetails, directDebitDetails)
            .withFieldConst(_.stage, Stage.AfterEnteredDirectDebitDetails.EnteredDirectDebitDetails)
            .transform
        case j: Journey.Sa.SelectedEmailToBeVerified =>
          j.into[Journey.Sa.EnteredDirectDebitDetails]
            .withFieldConst(_.directDebitDetails, directDebitDetails)
            .withFieldConst(_.stage, Stage.AfterEnteredDirectDebitDetails.EnteredDirectDebitDetails)
            .transform
        case j: Journey.Sia.SelectedEmailToBeVerified =>
          j.into[Journey.Sia.EnteredDirectDebitDetails]
            .withFieldConst(_.directDebitDetails, directDebitDetails)
            .withFieldConst(_.stage, Stage.AfterEnteredDirectDebitDetails.EnteredDirectDebitDetails)
            .transform

        case j: Journey.Epaye.EmailVerificationComplete =>
          j.into[Journey.Epaye.EnteredDirectDebitDetails]
            .withFieldConst(_.directDebitDetails, directDebitDetails)
            .withFieldConst(_.stage, Stage.AfterEnteredDirectDebitDetails.EnteredDirectDebitDetails)
            .transform
        case j: Journey.Vat.EmailVerificationComplete =>
          j.into[Journey.Vat.EnteredDirectDebitDetails]
            .withFieldConst(_.directDebitDetails, directDebitDetails)
            .withFieldConst(_.stage, Stage.AfterEnteredDirectDebitDetails.EnteredDirectDebitDetails)
            .transform
        case j: Journey.Sa.EmailVerificationComplete =>
          j.into[Journey.Sa.EnteredDirectDebitDetails]
            .withFieldConst(_.directDebitDetails, directDebitDetails)
            .withFieldConst(_.stage, Stage.AfterEnteredDirectDebitDetails.EnteredDirectDebitDetails)
            .transform
        case j: Journey.Sia.EmailVerificationComplete =>
          j.into[Journey.Sia.EnteredDirectDebitDetails]
            .withFieldConst(_.directDebitDetails, directDebitDetails)
            .withFieldConst(_.stage, Stage.AfterEnteredDirectDebitDetails.EnteredDirectDebitDetails)
            .transform

        case _: Journey.Stages.SubmittedArrangement =>
          Errors.throwBadRequestException("Cannot update DirectDebitDetails when journey is in completed state")
      }

      journeyService.upsert(updatedJourney)
    }

  }

}
