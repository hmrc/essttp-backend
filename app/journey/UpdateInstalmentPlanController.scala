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
import essttp.journey.model.{Journey, JourneyId, PaymentPlanAnswers, Stage}
import essttp.rootmodel.ttp.affordablequotes.PaymentPlan
import essttp.utils.Errors
import io.scalaland.chimney.dsl.TransformationOps
import play.api.mvc.{Action, ControllerComponents, Request}
import services.JourneyService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateInstalmentPlanController @Inject() (
    actions:        Actions,
    journeyService: JourneyService,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext, cryptoFormat: OperationalCryptoFormat) extends BackendController(cc) {

  def updateChosenInstalmentPlan(journeyId: JourneyId): Action[PaymentPlan] = actions.authenticatedAction.async(parse.json[PaymentPlan]) { implicit request =>
    for {
      journey <- journeyService.get(journeyId)
      newJourney <- journey match {
        case j: Journey.BeforeAffordableQuotesResponse   => Errors.throwBadRequestExceptionF(s"UpdateSelectedPaymentPlan is not possible in that state: [${j.stage.toString}]")
        case j: Journey.Stages.RetrievedAffordableQuotes => updateJourneyWithNewValue(j, request.body)
        case j: Journey.AfterSelectedPaymentPlan         => updateJourneyWithExistingValue(Left(j), request.body)
        case j: Journey.AfterCheckedPaymentPlan =>
          j match {
            case _: Journey.BeforeArrangementSubmitted => updateJourneyWithExistingValue(Right(j), request.body)
            case _: Journey.AfterArrangementSubmitted  => Errors.throwBadRequestExceptionF("Cannot update ChosenPlan when journey is in completed state")
          }
        case _: Journey.AfterStartedPegaCase => Errors.throwBadRequestExceptionF(s"Not expecting to update SelectedPaymentPlan after starting PEGA case")
      }
    } yield Ok(newJourney.json)
  }

  private def updateJourneyWithNewValue(
      journey:     Journey.Stages.RetrievedAffordableQuotes,
      paymentPlan: PaymentPlan
  )(implicit request: Request[_]): Future[Journey] = {
    val newJourney: Journey.AfterSelectedPaymentPlan = journey match {
      case j: Journey.Epaye.RetrievedAffordableQuotes =>
        j.into[Journey.Epaye.ChosenPaymentPlan]
          .withFieldConst(_.stage, Stage.AfterSelectedPlan.SelectedPlan)
          .withFieldConst(_.selectedPaymentPlan, paymentPlan)
          .transform
      case j: Journey.Vat.RetrievedAffordableQuotes =>
        j.into[Journey.Vat.ChosenPaymentPlan]
          .withFieldConst(_.stage, Stage.AfterSelectedPlan.SelectedPlan)
          .withFieldConst(_.selectedPaymentPlan, paymentPlan)
          .transform
      case j: Journey.Sa.RetrievedAffordableQuotes =>
        j.into[Journey.Sa.ChosenPaymentPlan]
          .withFieldConst(_.stage, Stage.AfterSelectedPlan.SelectedPlan)
          .withFieldConst(_.selectedPaymentPlan, paymentPlan)
          .transform
      case j: Journey.Sia.RetrievedAffordableQuotes =>
        j.into[Journey.Sia.ChosenPaymentPlan]
          .withFieldConst(_.stage, Stage.AfterSelectedPlan.SelectedPlan)
          .withFieldConst(_.selectedPaymentPlan, paymentPlan)
          .transform
    }
    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingValue(
      journey:     Either[Journey.AfterSelectedPaymentPlan, Journey.AfterCheckedPaymentPlan],
      paymentPlan: PaymentPlan
  )(implicit request: Request[_]): Future[Journey] =
    journey match {
      case Left(afterSelectedPaymentPlan) =>
        updateJourneyWithExistingValue(
          afterSelectedPaymentPlan.selectedPaymentPlan,
          afterSelectedPaymentPlan,
          paymentPlan,
          afterSelectedPaymentPlan match {
            case j: Journey.Epaye.ChosenPaymentPlan =>
              j.copy(selectedPaymentPlan = paymentPlan)
            case j: Journey.Vat.ChosenPaymentPlan =>
              j.copy(selectedPaymentPlan = paymentPlan)
            case j: Journey.Sa.ChosenPaymentPlan =>
              j.copy(selectedPaymentPlan = paymentPlan)
            case j: Journey.Sia.ChosenPaymentPlan =>
              j.copy(selectedPaymentPlan = paymentPlan)
          }
        )

      case Right(afterCheckedPaymentPlan) =>
        afterCheckedPaymentPlan.paymentPlanAnswers match {
          case _: PaymentPlanAnswers.PaymentPlanAfterAffordability =>
            Errors.throwBadRequestExceptionF("Cannot update SelectedPaymentPlan on affordability journey")

          case p: PaymentPlanAnswers.PaymentPlanNoAffordability =>
            updateJourneyWithExistingValue(
              p.selectedPaymentPlan,
              afterCheckedPaymentPlan,
              paymentPlan,
              afterCheckedPaymentPlan match {
                case j: Journey.Epaye.CheckedPaymentPlan =>
                  j.into[Journey.Epaye.ChosenPaymentPlan]
                    .withFieldConst(_.stage, Stage.AfterSelectedPlan.SelectedPlan)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, p.startDatesResponse)
                    .withFieldConst(_.affordableQuotesResponse, p.affordableQuotesResponse)
                    .withFieldConst(_.selectedPaymentPlan, paymentPlan)
                    .transform
                case j: Journey.Vat.CheckedPaymentPlan =>
                  j.into[Journey.Vat.ChosenPaymentPlan]
                    .withFieldConst(_.stage, Stage.AfterSelectedPlan.SelectedPlan)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, p.startDatesResponse)
                    .withFieldConst(_.affordableQuotesResponse, p.affordableQuotesResponse)
                    .withFieldConst(_.selectedPaymentPlan, paymentPlan)
                    .transform
                case j: Journey.Sa.CheckedPaymentPlan =>
                  j.into[Journey.Sa.ChosenPaymentPlan]
                    .withFieldConst(_.stage, Stage.AfterSelectedPlan.SelectedPlan)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, p.startDatesResponse)
                    .withFieldConst(_.affordableQuotesResponse, p.affordableQuotesResponse)
                    .withFieldConst(_.selectedPaymentPlan, paymentPlan)
                    .transform
                case j: Journey.Sia.CheckedPaymentPlan =>
                  j.into[Journey.Sia.ChosenPaymentPlan]
                    .withFieldConst(_.stage, Stage.AfterSelectedPlan.SelectedPlan)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, p.startDatesResponse)
                    .withFieldConst(_.affordableQuotesResponse, p.affordableQuotesResponse)
                    .withFieldConst(_.selectedPaymentPlan, paymentPlan)
                    .transform

                case j: Journey.Epaye.EnteredDetailsAboutBankAccount =>
                  j.into[Journey.Epaye.ChosenPaymentPlan]
                    .withFieldConst(_.stage, Stage.AfterSelectedPlan.SelectedPlan)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, p.startDatesResponse)
                    .withFieldConst(_.affordableQuotesResponse, p.affordableQuotesResponse)
                    .withFieldConst(_.selectedPaymentPlan, paymentPlan)
                    .transform
                case j: Journey.Vat.EnteredDetailsAboutBankAccount =>
                  j.into[Journey.Vat.ChosenPaymentPlan]
                    .withFieldConst(_.stage, Stage.AfterSelectedPlan.SelectedPlan)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, p.startDatesResponse)
                    .withFieldConst(_.affordableQuotesResponse, p.affordableQuotesResponse)
                    .withFieldConst(_.selectedPaymentPlan, paymentPlan)
                    .transform
                case j: Journey.Sa.EnteredDetailsAboutBankAccount =>
                  j.into[Journey.Sa.ChosenPaymentPlan]
                    .withFieldConst(_.stage, Stage.AfterSelectedPlan.SelectedPlan)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, p.startDatesResponse)
                    .withFieldConst(_.affordableQuotesResponse, p.affordableQuotesResponse)
                    .withFieldConst(_.selectedPaymentPlan, paymentPlan)
                    .transform
                case j: Journey.Sia.EnteredDetailsAboutBankAccount =>
                  j.into[Journey.Sia.ChosenPaymentPlan]
                    .withFieldConst(_.stage, Stage.AfterSelectedPlan.SelectedPlan)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, p.startDatesResponse)
                    .withFieldConst(_.affordableQuotesResponse, p.affordableQuotesResponse)
                    .withFieldConst(_.selectedPaymentPlan, paymentPlan)
                    .transform

                case j: Journey.Epaye.EnteredDirectDebitDetails =>
                  j.into[Journey.Epaye.ChosenPaymentPlan]
                    .withFieldConst(_.stage, Stage.AfterSelectedPlan.SelectedPlan)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, p.startDatesResponse)
                    .withFieldConst(_.affordableQuotesResponse, p.affordableQuotesResponse)
                    .withFieldConst(_.selectedPaymentPlan, paymentPlan)
                    .transform
                case j: Journey.Vat.EnteredDirectDebitDetails =>
                  j.into[Journey.Vat.ChosenPaymentPlan]
                    .withFieldConst(_.stage, Stage.AfterSelectedPlan.SelectedPlan)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, p.startDatesResponse)
                    .withFieldConst(_.affordableQuotesResponse, p.affordableQuotesResponse)
                    .withFieldConst(_.selectedPaymentPlan, paymentPlan)
                    .transform
                case j: Journey.Sa.EnteredDirectDebitDetails =>
                  j.into[Journey.Sa.ChosenPaymentPlan]
                    .withFieldConst(_.stage, Stage.AfterSelectedPlan.SelectedPlan)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, p.startDatesResponse)
                    .withFieldConst(_.affordableQuotesResponse, p.affordableQuotesResponse)
                    .withFieldConst(_.selectedPaymentPlan, paymentPlan)
                    .transform
                case j: Journey.Sia.EnteredDirectDebitDetails =>
                  j.into[Journey.Sia.ChosenPaymentPlan]
                    .withFieldConst(_.stage, Stage.AfterSelectedPlan.SelectedPlan)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, p.startDatesResponse)
                    .withFieldConst(_.affordableQuotesResponse, p.affordableQuotesResponse)
                    .withFieldConst(_.selectedPaymentPlan, paymentPlan)
                    .transform

                case j: Journey.Epaye.ConfirmedDirectDebitDetails =>
                  j.into[Journey.Epaye.ChosenPaymentPlan]
                    .withFieldConst(_.stage, Stage.AfterSelectedPlan.SelectedPlan)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, p.startDatesResponse)
                    .withFieldConst(_.affordableQuotesResponse, p.affordableQuotesResponse)
                    .withFieldConst(_.selectedPaymentPlan, paymentPlan)
                    .transform
                case j: Journey.Vat.ConfirmedDirectDebitDetails =>
                  j.into[Journey.Vat.ChosenPaymentPlan]
                    .withFieldConst(_.stage, Stage.AfterSelectedPlan.SelectedPlan)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, p.startDatesResponse)
                    .withFieldConst(_.affordableQuotesResponse, p.affordableQuotesResponse)
                    .withFieldConst(_.selectedPaymentPlan, paymentPlan)
                    .transform
                case j: Journey.Sa.ConfirmedDirectDebitDetails =>
                  j.into[Journey.Sa.ChosenPaymentPlan]
                    .withFieldConst(_.stage, Stage.AfterSelectedPlan.SelectedPlan)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, p.startDatesResponse)
                    .withFieldConst(_.affordableQuotesResponse, p.affordableQuotesResponse)
                    .withFieldConst(_.selectedPaymentPlan, paymentPlan)
                    .transform
                case j: Journey.Sia.ConfirmedDirectDebitDetails =>
                  j.into[Journey.Sia.ChosenPaymentPlan]
                    .withFieldConst(_.stage, Stage.AfterSelectedPlan.SelectedPlan)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, p.startDatesResponse)
                    .withFieldConst(_.affordableQuotesResponse, p.affordableQuotesResponse)
                    .withFieldConst(_.selectedPaymentPlan, paymentPlan)
                    .transform

                case j: Journey.Epaye.AgreedTermsAndConditions =>
                  j.into[Journey.Epaye.ChosenPaymentPlan]
                    .withFieldConst(_.stage, Stage.AfterSelectedPlan.SelectedPlan)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, p.startDatesResponse)
                    .withFieldConst(_.affordableQuotesResponse, p.affordableQuotesResponse)
                    .withFieldConst(_.selectedPaymentPlan, paymentPlan)
                    .transform
                case j: Journey.Vat.AgreedTermsAndConditions =>
                  j.into[Journey.Vat.ChosenPaymentPlan]
                    .withFieldConst(_.stage, Stage.AfterSelectedPlan.SelectedPlan)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, p.startDatesResponse)
                    .withFieldConst(_.affordableQuotesResponse, p.affordableQuotesResponse)
                    .withFieldConst(_.selectedPaymentPlan, paymentPlan)
                    .transform
                case j: Journey.Sa.AgreedTermsAndConditions =>
                  j.into[Journey.Sa.ChosenPaymentPlan]
                    .withFieldConst(_.stage, Stage.AfterSelectedPlan.SelectedPlan)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, p.startDatesResponse)
                    .withFieldConst(_.affordableQuotesResponse, p.affordableQuotesResponse)
                    .withFieldConst(_.selectedPaymentPlan, paymentPlan)
                    .transform
                case j: Journey.Sia.AgreedTermsAndConditions =>
                  j.into[Journey.Sia.ChosenPaymentPlan]
                    .withFieldConst(_.stage, Stage.AfterSelectedPlan.SelectedPlan)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, p.startDatesResponse)
                    .withFieldConst(_.affordableQuotesResponse, p.affordableQuotesResponse)
                    .withFieldConst(_.selectedPaymentPlan, paymentPlan)
                    .transform

                case j: Journey.Epaye.SelectedEmailToBeVerified =>
                  j.into[Journey.Epaye.ChosenPaymentPlan]
                    .withFieldConst(_.stage, Stage.AfterSelectedPlan.SelectedPlan)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, p.startDatesResponse)
                    .withFieldConst(_.affordableQuotesResponse, p.affordableQuotesResponse)
                    .withFieldConst(_.selectedPaymentPlan, paymentPlan)
                    .transform
                case j: Journey.Vat.SelectedEmailToBeVerified =>
                  j.into[Journey.Vat.ChosenPaymentPlan]
                    .withFieldConst(_.stage, Stage.AfterSelectedPlan.SelectedPlan)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, p.startDatesResponse)
                    .withFieldConst(_.affordableQuotesResponse, p.affordableQuotesResponse)
                    .withFieldConst(_.selectedPaymentPlan, paymentPlan)
                    .transform
                case j: Journey.Sa.SelectedEmailToBeVerified =>
                  j.into[Journey.Sa.ChosenPaymentPlan]
                    .withFieldConst(_.stage, Stage.AfterSelectedPlan.SelectedPlan)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, p.startDatesResponse)
                    .withFieldConst(_.affordableQuotesResponse, p.affordableQuotesResponse)
                    .withFieldConst(_.selectedPaymentPlan, paymentPlan)
                    .transform
                case j: Journey.Sia.SelectedEmailToBeVerified =>
                  j.into[Journey.Sia.ChosenPaymentPlan]
                    .withFieldConst(_.stage, Stage.AfterSelectedPlan.SelectedPlan)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, p.startDatesResponse)
                    .withFieldConst(_.affordableQuotesResponse, p.affordableQuotesResponse)
                    .withFieldConst(_.selectedPaymentPlan, paymentPlan)
                    .transform

                case j: Journey.Epaye.EmailVerificationComplete =>
                  j.into[Journey.Epaye.ChosenPaymentPlan]
                    .withFieldConst(_.stage, Stage.AfterSelectedPlan.SelectedPlan)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, p.startDatesResponse)
                    .withFieldConst(_.affordableQuotesResponse, p.affordableQuotesResponse)
                    .withFieldConst(_.selectedPaymentPlan, paymentPlan)
                    .transform
                case j: Journey.Vat.EmailVerificationComplete =>
                  j.into[Journey.Vat.ChosenPaymentPlan]
                    .withFieldConst(_.stage, Stage.AfterSelectedPlan.SelectedPlan)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, p.startDatesResponse)
                    .withFieldConst(_.affordableQuotesResponse, p.affordableQuotesResponse)
                    .withFieldConst(_.selectedPaymentPlan, paymentPlan)
                    .transform
                case j: Journey.Sa.EmailVerificationComplete =>
                  j.into[Journey.Sa.ChosenPaymentPlan]
                    .withFieldConst(_.stage, Stage.AfterSelectedPlan.SelectedPlan)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, p.startDatesResponse)
                    .withFieldConst(_.affordableQuotesResponse, p.affordableQuotesResponse)
                    .withFieldConst(_.selectedPaymentPlan, paymentPlan)
                    .transform
                case j: Journey.Sia.EmailVerificationComplete =>
                  j.into[Journey.Sia.ChosenPaymentPlan]
                    .withFieldConst(_.stage, Stage.AfterSelectedPlan.SelectedPlan)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, p.startDatesResponse)
                    .withFieldConst(_.affordableQuotesResponse, p.affordableQuotesResponse)
                    .withFieldConst(_.selectedPaymentPlan, paymentPlan)
                    .transform

                case _: Journey.Stages.SubmittedArrangement =>
                  Errors.throwBadRequestException("Cannot update ChosenPlan when journey is in completed state")
              }
            )
        }
    }

  private def updateJourneyWithExistingValue(
      existingValue:   PaymentPlan,
      existingJourney: Journey,
      newValue:        PaymentPlan,
      newJourney:      Journey
  )(implicit request: Request[_]): Future[Journey] =
    if (existingValue === newValue) {
      JourneyLogger.info("Nothing to update, selected PaymentPlan is the same as the existing one in journey.")
      Future.successful(existingJourney)
    } else {
      journeyService.upsert(newJourney)
    }

}
