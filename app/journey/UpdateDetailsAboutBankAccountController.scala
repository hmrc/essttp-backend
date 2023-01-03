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
import essttp.rootmodel.bank.{DetailsAboutBankAccount, TypesOfBankAccount}
import essttp.utils.Errors
import io.scalaland.chimney.dsl.TransformerOps
import play.api.mvc.{Action, ControllerComponents, Request}
import services.JourneyService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateDetailsAboutBankAccountController @Inject() (
    actions:        Actions,
    journeyService: JourneyService,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext, cryptoFormat: OperationalCryptoFormat) extends BackendController(cc) {

  def updateDetailsAboutBankAccount(journeyId: JourneyId): Action[DetailsAboutBankAccount] = actions.authenticatedAction.async(parse.json[DetailsAboutBankAccount]) { implicit request =>
    for {
      journey <- journeyService.get(journeyId)
      newJourney <- journey match {
        case j: Journey.BeforeCheckedPaymentPlan  => Errors.throwBadRequestExceptionF(s"UpdateDetailsAboutBankAccount is not possible in that state: [${j.stage.toString}]")
        case j: Journey.Stages.CheckedPaymentPlan => updateJourneyWithNewValue(j, request.body)
        case j: Journey.AfterEnteredDetailsAboutBankAccount => j match {
          case _: Journey.BeforeArrangementSubmitted => updateJourneyWithExistingValue(j, request.body)
          case _: Journey.AfterArrangementSubmitted  => Errors.throwBadRequestExceptionF("Cannot update DetailsAboutBankAccount when journey is in completed state")
        }
      }
    } yield Ok(newJourney.json)
  }

  private def updateJourneyWithNewValue(
      journey:                 Journey.Stages.CheckedPaymentPlan,
      detailsAboutBankAccount: DetailsAboutBankAccount
  )(implicit request: Request[_]): Future[Journey] = {
    val newJourney: Journey.AfterEnteredDetailsAboutBankAccount = journey match {
      case j: Journey.Epaye.CheckedPaymentPlan =>
        j.into[Journey.Epaye.EnteredDetailsAboutBankAccount]
          .withFieldConst(_.stage, determineStage(detailsAboutBankAccount))
          .withFieldConst(_.detailsAboutBankAccount, detailsAboutBankAccount)
          .transform
      case j: Journey.Vat.CheckedPaymentPlan =>
        j.into[Journey.Vat.EnteredDetailsAboutBankAccount]
          .withFieldConst(_.stage, determineStage(detailsAboutBankAccount))
          .withFieldConst(_.detailsAboutBankAccount, detailsAboutBankAccount)
          .transform
    }
    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingValue(
      journey:                 Journey.AfterEnteredDetailsAboutBankAccount,
      detailsAboutBankAccount: DetailsAboutBankAccount
  )(implicit request: Request[_]): Future[Journey] = {
    if (journey.detailsAboutBankAccount === detailsAboutBankAccount) {
      JourneyLogger.info("Chosen type of bank account hasn't changed, nothing to update")
      Future.successful(journey)
    } else {
      val updatedJourney: Journey.AfterEnteredDetailsAboutBankAccount = journey match {

        case j: Journey.Epaye.EnteredDetailsAboutBankAccount =>
          j.copy(
            detailsAboutBankAccount = detailsAboutBankAccount,
            stage                   = determineStage(detailsAboutBankAccount)
          )
        case j: Journey.Vat.EnteredDetailsAboutBankAccount =>
          j.copy(
            detailsAboutBankAccount = detailsAboutBankAccount,
            stage                   = determineStage(detailsAboutBankAccount)
          )

        case j: Journey.Epaye.EnteredDirectDebitDetails =>
          j.into[Journey.Epaye.EnteredDetailsAboutBankAccount]
            .withFieldConst(_.detailsAboutBankAccount, detailsAboutBankAccount)
            .withFieldConst(_.stage, determineStage(detailsAboutBankAccount))
            .transform
        case j: Journey.Vat.EnteredDirectDebitDetails =>
          j.into[Journey.Vat.EnteredDetailsAboutBankAccount]
            .withFieldConst(_.detailsAboutBankAccount, detailsAboutBankAccount)
            .withFieldConst(_.stage, determineStage(detailsAboutBankAccount))
            .transform

        case j: Journey.Epaye.ConfirmedDirectDebitDetails =>
          j.into[Journey.Epaye.EnteredDetailsAboutBankAccount]
            .withFieldConst(_.detailsAboutBankAccount, detailsAboutBankAccount)
            .withFieldConst(_.stage, determineStage(detailsAboutBankAccount))
            .transform
        case j: Journey.Vat.ConfirmedDirectDebitDetails =>
          j.into[Journey.Vat.EnteredDetailsAboutBankAccount]
            .withFieldConst(_.detailsAboutBankAccount, detailsAboutBankAccount)
            .withFieldConst(_.stage, determineStage(detailsAboutBankAccount))
            .transform

        case j: Journey.Epaye.AgreedTermsAndConditions =>
          j.into[Journey.Epaye.EnteredDetailsAboutBankAccount]
            .withFieldConst(_.detailsAboutBankAccount, detailsAboutBankAccount)
            .withFieldConst(_.stage, determineStage(detailsAboutBankAccount))
            .transform
        case j: Journey.Vat.AgreedTermsAndConditions =>
          j.into[Journey.Vat.EnteredDetailsAboutBankAccount]
            .withFieldConst(_.detailsAboutBankAccount, detailsAboutBankAccount)
            .withFieldConst(_.stage, determineStage(detailsAboutBankAccount))
            .transform

        case j: Journey.Epaye.SelectedEmailToBeVerified =>
          j.into[Journey.Epaye.EnteredDetailsAboutBankAccount]
            .withFieldConst(_.detailsAboutBankAccount, detailsAboutBankAccount)
            .withFieldConst(_.stage, determineStage(detailsAboutBankAccount))
            .transform
        case j: Journey.Vat.SelectedEmailToBeVerified =>
          j.into[Journey.Vat.EnteredDetailsAboutBankAccount]
            .withFieldConst(_.detailsAboutBankAccount, detailsAboutBankAccount)
            .withFieldConst(_.stage, determineStage(detailsAboutBankAccount))
            .transform

        case j: Journey.Epaye.EmailVerificationComplete =>
          j.into[Journey.Epaye.EnteredDetailsAboutBankAccount]
            .withFieldConst(_.detailsAboutBankAccount, detailsAboutBankAccount)
            .withFieldConst(_.stage, determineStage(detailsAboutBankAccount))
            .transform
        case j: Journey.Vat.EmailVerificationComplete =>
          j.into[Journey.Vat.EnteredDetailsAboutBankAccount]
            .withFieldConst(_.detailsAboutBankAccount, detailsAboutBankAccount)
            .withFieldConst(_.stage, determineStage(detailsAboutBankAccount))
            .transform

        case _: Journey.Stages.SubmittedArrangement =>
          Errors.throwBadRequestException("Cannot update DetailsAboutBankAccount when journey is in completed state")
      }

      journeyService.upsert(updatedJourney)
    }

  }

  private def determineStage(detailsAboutBankAccount: DetailsAboutBankAccount): Stage.AfterEnteredDetailsAboutBankAccount =
    if (detailsAboutBankAccount.isAccountHolder)
      detailsAboutBankAccount.typeOfBankAccount match {
        case TypesOfBankAccount.Business => Stage.AfterEnteredDetailsAboutBankAccount.Business
        case TypesOfBankAccount.Personal => Stage.AfterEnteredDetailsAboutBankAccount.Personal
      }
    else
      Stage.AfterEnteredDetailsAboutBankAccount.IsNotAccountHolder

}

