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
import essttp.rootmodel.bank.CanSetUpDirectDebit
import essttp.utils.Errors
import io.scalaland.chimney.dsl.TransformationOps
import play.api.mvc.{Action, ControllerComponents, Request}
import services.JourneyService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateCheckYouCanSetupDDController @Inject() (
    actions:        Actions,
    journeyService: JourneyService,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext, cryptoFormat: OperationalCryptoFormat) extends BackendController(cc) {

  def updateCheckYouCanSetupDD(journeyId: JourneyId): Action[CanSetUpDirectDebit] = actions.authenticatedAction.async(parse.json[CanSetUpDirectDebit]) { implicit request =>
    for {
      journey <- journeyService.get(journeyId)
      newJourney <- journey match {
        case j: Journey.BeforeCheckedPaymentPlan  => Errors.throwBadRequestExceptionF(s"UpdateCheckYouCanSetupDD is not possible in that state: [${j.stage.toString}]")
        case j: Journey.Stages.CheckedPaymentPlan => updateJourneyWithNewValue(j, request.body)
        case j: Journey.AfterEnteredCanYouSetUpDirectDebit => j match {
          case _: Journey.BeforeArrangementSubmitted => updateJourneyWithExistingValue(j, request.body)
          case _: Journey.AfterArrangementSubmitted  => Errors.throwBadRequestExceptionF("Cannot update CanSetUpDirectDebit when journey is in completed state")
        }
      }
    } yield Ok(newJourney.json)
  }

  private def updateJourneyWithNewValue(
      journey:             Journey.Stages.CheckedPaymentPlan,
      canSetUpDirectDebit: CanSetUpDirectDebit
  )(implicit request: Request[_]): Future[Journey] = {
    val newJourney: Journey.AfterEnteredCanYouSetUpDirectDebit = journey match {
      case j: Journey.Epaye.CheckedPaymentPlan =>
        j.into[Journey.Epaye.EnteredCanYouSetUpDirectDebit]
          .withFieldConst(_.stage, determineStage(canSetUpDirectDebit))
          .withFieldConst(_.canSetUpDirectDebitAnswer, canSetUpDirectDebit)
          .transform
      case j: Journey.Vat.CheckedPaymentPlan =>
        j.into[Journey.Vat.EnteredCanYouSetUpDirectDebit]
          .withFieldConst(_.stage, determineStage(canSetUpDirectDebit))
          .withFieldConst(_.canSetUpDirectDebitAnswer, canSetUpDirectDebit)
          .transform
      case j: Journey.Sa.CheckedPaymentPlan =>
        j.into[Journey.Sa.EnteredCanYouSetUpDirectDebit]
          .withFieldConst(_.stage, determineStage(canSetUpDirectDebit))
          .withFieldConst(_.canSetUpDirectDebitAnswer, canSetUpDirectDebit)
          .transform
      case j: Journey.Sia.CheckedPaymentPlan =>
        j.into[Journey.Sia.EnteredCanYouSetUpDirectDebit]
          .withFieldConst(_.stage, determineStage(canSetUpDirectDebit))
          .withFieldConst(_.canSetUpDirectDebitAnswer, canSetUpDirectDebit)
          .transform
    }
    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingValue(
      journey:             Journey.AfterEnteredCanYouSetUpDirectDebit,
      canSetUpDirectDebit: CanSetUpDirectDebit
  )(implicit request: Request[_]): Future[Journey] = {
    if (journey.canSetUpDirectDebitAnswer === canSetUpDirectDebit) {
      JourneyLogger.info("Chosen type of bank account hasn't changed, nothing to update")
      Future.successful(journey)
    } else {
      val updatedJourney: Journey.AfterEnteredCanYouSetUpDirectDebit = journey match {

        case j: Journey.Epaye.EnteredCanYouSetUpDirectDebit =>
          j.copy(
            canSetUpDirectDebitAnswer = canSetUpDirectDebit,
            stage                     = determineStage(canSetUpDirectDebit)
          )
        case j: Journey.Vat.EnteredCanYouSetUpDirectDebit =>
          j.copy(
            canSetUpDirectDebitAnswer = canSetUpDirectDebit,
            stage                     = determineStage(canSetUpDirectDebit)
          )
        case j: Journey.Sa.EnteredCanYouSetUpDirectDebit =>
          j.copy(
            canSetUpDirectDebitAnswer = canSetUpDirectDebit,
            stage                     = determineStage(canSetUpDirectDebit)
          )
        case j: Journey.Sia.EnteredCanYouSetUpDirectDebit =>
          j.copy(
            canSetUpDirectDebitAnswer = canSetUpDirectDebit,
            stage                     = determineStage(canSetUpDirectDebit)
          )

        case j: Journey.Epaye.EnteredDirectDebitDetails =>
          j.into[Journey.Epaye.EnteredCanYouSetUpDirectDebit]
            .withFieldConst(_.canSetUpDirectDebitAnswer, canSetUpDirectDebit)
            .withFieldConst(_.stage, determineStage(canSetUpDirectDebit))
            .transform
        case j: Journey.Vat.EnteredDirectDebitDetails =>
          j.into[Journey.Vat.EnteredCanYouSetUpDirectDebit]
            .withFieldConst(_.canSetUpDirectDebitAnswer, canSetUpDirectDebit)
            .withFieldConst(_.stage, determineStage(canSetUpDirectDebit))
            .transform
        case j: Journey.Sa.EnteredDirectDebitDetails =>
          j.into[Journey.Sa.EnteredCanYouSetUpDirectDebit]
            .withFieldConst(_.canSetUpDirectDebitAnswer, canSetUpDirectDebit)
            .withFieldConst(_.stage, determineStage(canSetUpDirectDebit))
            .transform
        case j: Journey.Sia.EnteredDirectDebitDetails =>
          j.into[Journey.Sia.EnteredCanYouSetUpDirectDebit]
            .withFieldConst(_.canSetUpDirectDebitAnswer, canSetUpDirectDebit)
            .withFieldConst(_.stage, determineStage(canSetUpDirectDebit))
            .transform

        case j: Journey.Epaye.ConfirmedDirectDebitDetails =>
          j.into[Journey.Epaye.EnteredCanYouSetUpDirectDebit]
            .withFieldConst(_.canSetUpDirectDebitAnswer, canSetUpDirectDebit)
            .withFieldConst(_.stage, determineStage(canSetUpDirectDebit))
            .transform
        case j: Journey.Vat.ConfirmedDirectDebitDetails =>
          j.into[Journey.Vat.EnteredCanYouSetUpDirectDebit]
            .withFieldConst(_.canSetUpDirectDebitAnswer, canSetUpDirectDebit)
            .withFieldConst(_.stage, determineStage(canSetUpDirectDebit))
            .transform
        case j: Journey.Sa.ConfirmedDirectDebitDetails =>
          j.into[Journey.Sa.EnteredCanYouSetUpDirectDebit]
            .withFieldConst(_.canSetUpDirectDebitAnswer, canSetUpDirectDebit)
            .withFieldConst(_.stage, determineStage(canSetUpDirectDebit))
            .transform
        case j: Journey.Sia.ConfirmedDirectDebitDetails =>
          j.into[Journey.Sia.EnteredCanYouSetUpDirectDebit]
            .withFieldConst(_.canSetUpDirectDebitAnswer, canSetUpDirectDebit)
            .withFieldConst(_.stage, determineStage(canSetUpDirectDebit))
            .transform

        case j: Journey.Epaye.AgreedTermsAndConditions =>
          j.into[Journey.Epaye.EnteredCanYouSetUpDirectDebit]
            .withFieldConst(_.canSetUpDirectDebitAnswer, canSetUpDirectDebit)
            .withFieldConst(_.stage, determineStage(canSetUpDirectDebit))
            .transform
        case j: Journey.Vat.AgreedTermsAndConditions =>
          j.into[Journey.Vat.EnteredCanYouSetUpDirectDebit]
            .withFieldConst(_.canSetUpDirectDebitAnswer, canSetUpDirectDebit)
            .withFieldConst(_.stage, determineStage(canSetUpDirectDebit))
            .transform
        case j: Journey.Sa.AgreedTermsAndConditions =>
          j.into[Journey.Sa.EnteredCanYouSetUpDirectDebit]
            .withFieldConst(_.canSetUpDirectDebitAnswer, canSetUpDirectDebit)
            .withFieldConst(_.stage, determineStage(canSetUpDirectDebit))
            .transform
        case j: Journey.Sia.AgreedTermsAndConditions =>
          j.into[Journey.Sia.EnteredCanYouSetUpDirectDebit]
            .withFieldConst(_.canSetUpDirectDebitAnswer, canSetUpDirectDebit)
            .withFieldConst(_.stage, determineStage(canSetUpDirectDebit))
            .transform

        case j: Journey.Epaye.SelectedEmailToBeVerified =>
          j.into[Journey.Epaye.EnteredCanYouSetUpDirectDebit]
            .withFieldConst(_.canSetUpDirectDebitAnswer, canSetUpDirectDebit)
            .withFieldConst(_.stage, determineStage(canSetUpDirectDebit))
            .transform
        case j: Journey.Vat.SelectedEmailToBeVerified =>
          j.into[Journey.Vat.EnteredCanYouSetUpDirectDebit]
            .withFieldConst(_.canSetUpDirectDebitAnswer, canSetUpDirectDebit)
            .withFieldConst(_.stage, determineStage(canSetUpDirectDebit))
            .transform
        case j: Journey.Sa.SelectedEmailToBeVerified =>
          j.into[Journey.Sa.EnteredCanYouSetUpDirectDebit]
            .withFieldConst(_.canSetUpDirectDebitAnswer, canSetUpDirectDebit)
            .withFieldConst(_.stage, determineStage(canSetUpDirectDebit))
            .transform
        case j: Journey.Sia.SelectedEmailToBeVerified =>
          j.into[Journey.Sia.EnteredCanYouSetUpDirectDebit]
            .withFieldConst(_.canSetUpDirectDebitAnswer, canSetUpDirectDebit)
            .withFieldConst(_.stage, determineStage(canSetUpDirectDebit))
            .transform

        case j: Journey.Epaye.EmailVerificationComplete =>
          j.into[Journey.Epaye.EnteredCanYouSetUpDirectDebit]
            .withFieldConst(_.canSetUpDirectDebitAnswer, canSetUpDirectDebit)
            .withFieldConst(_.stage, determineStage(canSetUpDirectDebit))
            .transform
        case j: Journey.Vat.EmailVerificationComplete =>
          j.into[Journey.Vat.EnteredCanYouSetUpDirectDebit]
            .withFieldConst(_.canSetUpDirectDebitAnswer, canSetUpDirectDebit)
            .withFieldConst(_.stage, determineStage(canSetUpDirectDebit))
            .transform
        case j: Journey.Sa.EmailVerificationComplete =>
          j.into[Journey.Sa.EnteredCanYouSetUpDirectDebit]
            .withFieldConst(_.canSetUpDirectDebitAnswer, canSetUpDirectDebit)
            .withFieldConst(_.stage, determineStage(canSetUpDirectDebit))
            .transform
        case j: Journey.Sia.EmailVerificationComplete =>
          j.into[Journey.Sia.EnteredCanYouSetUpDirectDebit]
            .withFieldConst(_.canSetUpDirectDebitAnswer, canSetUpDirectDebit)
            .withFieldConst(_.stage, determineStage(canSetUpDirectDebit))
            .transform

        case _: Journey.Stages.SubmittedArrangement =>
          Errors.throwBadRequestException("Cannot update CanSetUpDirectDebit when journey is in completed state")
      }

      journeyService.upsert(updatedJourney)
    }

  }

  private def determineStage(canSetUpDirectDebit: CanSetUpDirectDebit): Stage.AfterEnteredCanYouSetUpDirectDebit =
    if (canSetUpDirectDebit.isAccountHolder)
      Stage.AfterEnteredCanYouSetUpDirectDebit.CanSetUpDirectDebit
    else
      Stage.AfterEnteredCanYouSetUpDirectDebit.CannotSetUpDirectDebit

}

