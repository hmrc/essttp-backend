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
import essttp.journey.model.Journey.Stages
import essttp.journey.model.{Journey, JourneyId, PaymentPlanAnswers, Stage}
import essttp.utils.Errors
import io.scalaland.chimney.dsl.TransformationOps
import play.api.mvc.{Action, ControllerComponents, Request}
import services.JourneyService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateHasCheckedInstalmentPlanController @Inject() (
    actions:        Actions,
    journeyService: JourneyService,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext, cryptoFormat: OperationalCryptoFormat) extends BackendController(cc) {

  def updateHasCheckedInstalmentPlan(journeyId: JourneyId): Action[PaymentPlanAnswers] =
    actions.authenticatedAction(parse.json[PaymentPlanAnswers]).async { implicit request =>
      for {
        journey <- journeyService.get(journeyId)
        newJourney <- journey match {
          case j: Journey.BeforeSelectedPaymentPlan => Errors.throwBadRequestExceptionF(s"UpdateHasCheckedInstalmentPlan is not possible in that state: [${j.stage.toString}]")
          case j: Journey.BeforeStartedPegaCase     => Errors.throwBadRequestExceptionF(s"UpdateHasCheckedInstalmentPlan is not possible in that state: [${j.stage.toString}]")
          case j: Journey.Stages.ChosenPaymentPlan  => updateJourneyWithNewValue(Right(j), request.body)
          case j: Journey.Stages.StartedPegaCase    => updateJourneyWithNewValue(Left(j), request.body)
          case j: Journey.AfterCheckedPaymentPlan => j match {
            case _: Journey.BeforeArrangementSubmitted => updateJourneyWithExistingValue(j, request.body)
            case _: Journey.AfterArrangementSubmitted  => Errors.throwBadRequestExceptionF("Cannot update HasCheckedPaymentPlan when journey is in completed state")
          }

        }
      } yield Ok(newJourney.json)
    }

  private def updateJourneyWithNewValue(
      journey: Either[Journey.Stages.StartedPegaCase, Journey.Stages.ChosenPaymentPlan],
      answers: PaymentPlanAnswers
  )(implicit request: Request[_]): Future[Journey] = {
    val newJourney: Journey.AfterCheckedPaymentPlan = journey match {
      case Left(j: Journey.Epaye.StartedPegaCase) =>
        j.into[Journey.Epaye.CheckedPaymentPlan]
          .withFieldConst(_.stage, Stage.AfterCheckedPlan.AcceptedPlan)
          .withFieldConst(_.paymentPlanAnswers, answers)
          .transform
      case Left(j: Journey.Vat.StartedPegaCase) =>
        j.into[Journey.Vat.CheckedPaymentPlan]
          .withFieldConst(_.stage, Stage.AfterCheckedPlan.AcceptedPlan)
          .withFieldConst(_.paymentPlanAnswers, answers)
          .transform
      case Left(j: Journey.Sa.StartedPegaCase) =>
        j.into[Journey.Sa.CheckedPaymentPlan]
          .withFieldConst(_.stage, Stage.AfterCheckedPlan.AcceptedPlan)
          .withFieldConst(_.paymentPlanAnswers, answers)
          .transform
      case Right(j: Journey.Epaye.ChosenPaymentPlan) =>
        j.into[Journey.Epaye.CheckedPaymentPlan]
          .withFieldConst(_.stage, Stage.AfterCheckedPlan.AcceptedPlan)
          .withFieldConst(_.paymentPlanAnswers, answers)
          .transform
      case Right(j: Journey.Vat.ChosenPaymentPlan) =>
        j.into[Journey.Vat.CheckedPaymentPlan]
          .withFieldConst(_.stage, Stage.AfterCheckedPlan.AcceptedPlan)
          .withFieldConst(_.paymentPlanAnswers, answers)
          .transform
      case Right(j: Journey.Sa.ChosenPaymentPlan) =>
        j.into[Journey.Sa.CheckedPaymentPlan]
          .withFieldConst(_.stage, Stage.AfterCheckedPlan.AcceptedPlan)
          .withFieldConst(_.paymentPlanAnswers, answers)
          .transform
    }
    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingValue(
      journey: Journey.AfterCheckedPaymentPlan,
      answers: PaymentPlanAnswers
  )(implicit request: Request[_]): Future[Journey] =
    if (journey.paymentPlanAnswers === answers)
      Future.successful(journey)
    else {
      val updatedJourney: Journey = journey match {
        case j: Journey.Epaye.CheckedPaymentPlan =>
          j.copy(paymentPlanAnswers = answers)
        case j: Journey.Vat.CheckedPaymentPlan =>
          j.copy(paymentPlanAnswers = answers)
        case j: Journey.Sa.CheckedPaymentPlan =>
          j.copy(paymentPlanAnswers = answers)

        case j: Journey.Epaye.EnteredDetailsAboutBankAccount =>
          j.into[Journey.Epaye.CheckedPaymentPlan]
            .withFieldConst(_.stage, Stage.AfterCheckedPlan.AcceptedPlan)
            .withFieldConst(_.paymentPlanAnswers, answers)
            .transform
        case j: Journey.Vat.EnteredDetailsAboutBankAccount =>
          j.into[Journey.Vat.CheckedPaymentPlan]
            .withFieldConst(_.stage, Stage.AfterCheckedPlan.AcceptedPlan)
            .withFieldConst(_.paymentPlanAnswers, answers)
            .transform
        case j: Journey.Sa.EnteredDetailsAboutBankAccount =>
          j.into[Journey.Sa.CheckedPaymentPlan]
            .withFieldConst(_.stage, Stage.AfterCheckedPlan.AcceptedPlan)
            .withFieldConst(_.paymentPlanAnswers, answers)
            .transform

        case j: Journey.Epaye.EnteredDirectDebitDetails =>
          j.into[Journey.Epaye.CheckedPaymentPlan]
            .withFieldConst(_.stage, Stage.AfterCheckedPlan.AcceptedPlan)
            .withFieldConst(_.paymentPlanAnswers, answers)
            .transform
        case j: Journey.Vat.EnteredDirectDebitDetails =>
          j.into[Journey.Vat.CheckedPaymentPlan]
            .withFieldConst(_.stage, Stage.AfterCheckedPlan.AcceptedPlan)
            .withFieldConst(_.paymentPlanAnswers, answers)
            .transform
        case j: Journey.Sa.EnteredDirectDebitDetails =>
          j.into[Journey.Sa.CheckedPaymentPlan]
            .withFieldConst(_.stage, Stage.AfterCheckedPlan.AcceptedPlan)
            .withFieldConst(_.paymentPlanAnswers, answers)
            .transform

        case j: Journey.Epaye.ConfirmedDirectDebitDetails =>
          j.into[Journey.Epaye.CheckedPaymentPlan]
            .withFieldConst(_.stage, Stage.AfterCheckedPlan.AcceptedPlan)
            .withFieldConst(_.paymentPlanAnswers, answers)
            .transform
        case j: Journey.Vat.ConfirmedDirectDebitDetails =>
          j.into[Journey.Vat.CheckedPaymentPlan]
            .withFieldConst(_.stage, Stage.AfterCheckedPlan.AcceptedPlan)
            .withFieldConst(_.paymentPlanAnswers, answers)
            .transform
        case j: Journey.Sa.ConfirmedDirectDebitDetails =>
          j.into[Journey.Sa.CheckedPaymentPlan]
            .withFieldConst(_.stage, Stage.AfterCheckedPlan.AcceptedPlan)
            .withFieldConst(_.paymentPlanAnswers, answers)
            .transform

        case j: Journey.Epaye.AgreedTermsAndConditions =>
          j.into[Journey.Epaye.CheckedPaymentPlan]
            .withFieldConst(_.stage, Stage.AfterCheckedPlan.AcceptedPlan)
            .withFieldConst(_.paymentPlanAnswers, answers)
            .transform
        case j: Journey.Vat.AgreedTermsAndConditions =>
          j.into[Journey.Vat.CheckedPaymentPlan]
            .withFieldConst(_.stage, Stage.AfterCheckedPlan.AcceptedPlan)
            .withFieldConst(_.paymentPlanAnswers, answers)
            .transform
        case j: Journey.Sa.AgreedTermsAndConditions =>
          j.into[Journey.Sa.CheckedPaymentPlan]
            .withFieldConst(_.stage, Stage.AfterCheckedPlan.AcceptedPlan)
            .withFieldConst(_.paymentPlanAnswers, answers)
            .transform

        case j: Journey.Epaye.SelectedEmailToBeVerified =>
          j.into[Journey.Epaye.CheckedPaymentPlan]
            .withFieldConst(_.stage, Stage.AfterCheckedPlan.AcceptedPlan)
            .withFieldConst(_.paymentPlanAnswers, answers)
            .transform
        case j: Journey.Vat.SelectedEmailToBeVerified =>
          j.into[Journey.Vat.CheckedPaymentPlan]
            .withFieldConst(_.stage, Stage.AfterCheckedPlan.AcceptedPlan)
            .withFieldConst(_.paymentPlanAnswers, answers)
            .transform
        case j: Journey.Sa.SelectedEmailToBeVerified =>
          j.into[Journey.Sa.CheckedPaymentPlan]
            .withFieldConst(_.stage, Stage.AfterCheckedPlan.AcceptedPlan)
            .withFieldConst(_.paymentPlanAnswers, answers)
            .transform

        case j: Journey.Epaye.EmailVerificationComplete =>
          j.into[Journey.Epaye.CheckedPaymentPlan]
            .withFieldConst(_.stage, Stage.AfterCheckedPlan.AcceptedPlan)
            .withFieldConst(_.paymentPlanAnswers, answers)
            .transform
        case j: Journey.Vat.EmailVerificationComplete =>
          j.into[Journey.Vat.CheckedPaymentPlan]
            .withFieldConst(_.stage, Stage.AfterCheckedPlan.AcceptedPlan)
            .withFieldConst(_.paymentPlanAnswers, answers)
            .transform
        case j: Journey.Sa.EmailVerificationComplete =>
          j.into[Journey.Sa.CheckedPaymentPlan]
            .withFieldConst(_.stage, Stage.AfterCheckedPlan.AcceptedPlan)
            .withFieldConst(_.paymentPlanAnswers, answers)
            .transform

        case _: Stages.SubmittedArrangement =>
          Errors.throwBadRequestException("Cannot update UpdateHasCheckedInstalmentPlan when journey is in completed state")

      }
      journeyService.upsert(updatedJourney)
    }

}
