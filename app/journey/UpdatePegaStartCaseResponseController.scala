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
import essttp.journey.model.Journey.{Epaye, Sa, Sia, Stages, Vat}
import essttp.journey.model.{Journey, JourneyId, PaymentPlanAnswers, Stage}
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
        case j: Journey.AfterStartedPegaCase                        => updateJourneyWithExistingValue(Left(j), request.body)
        case _: Journey.AfterEnteredMonthlyPaymentAmount            => Errors.throwBadRequestExceptionF("update PEGA start case response not expected after entered monthly payment amount")
        case j: Journey.AfterCheckedPaymentPlan                     => updateJourneyWithExistingValue(Right(j), request.body)
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
      case j: Sia.ObtainedCanPayWithinSixMonthsAnswers =>
        j.into[Sia.StartedPegaCase]
          .withFieldConst(_.stage, Stage.AfterStartedPegaCase.StartedPegaCase)
          .withFieldConst(_.startCaseResponse, startCaseResponse)
          .transform
    }
    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingValue(
      journey:           Either[Journey.AfterStartedPegaCase, Journey.AfterCheckedPaymentPlan],
      startCaseResponse: StartCaseResponse
  )(implicit request: Request[_]): Future[Journey] =
    journey match {
      case Left(afterStartedPegaCase) =>
        updateJourneyWithExistingValue(
          afterStartedPegaCase.startCaseResponse,
          afterStartedPegaCase,
          startCaseResponse,
          afterStartedPegaCase match {
            case j: Epaye.StartedPegaCase =>
              j.copy(startCaseResponse = startCaseResponse)
            case j: Vat.StartedPegaCase =>
              j.copy(startCaseResponse = startCaseResponse)
            case j: Sa.StartedPegaCase =>
              j.copy(startCaseResponse = startCaseResponse)
            case j: Sia.StartedPegaCase =>
              j.copy(startCaseResponse = startCaseResponse)
          }
        )

      case Right(afterCheckedPaymentPlan) =>
        afterCheckedPaymentPlan.paymentPlanAnswers match {
          case _: PaymentPlanAnswers.PaymentPlanNoAffordability =>
            sys.error("Cannot update PEGA StartCaseEResponse on affordability journey")

          case p: PaymentPlanAnswers.PaymentPlanAfterAffordability =>
            updateJourneyWithExistingValue(
              p.startCaseResponse,
              afterCheckedPaymentPlan,
              startCaseResponse,
              afterCheckedPaymentPlan match {
                case j: Epaye.CheckedPaymentPlan =>
                  j.into[Epaye.StartedPegaCase]
                    .withFieldConst(_.stage, Stage.AfterStartedPegaCase.StartedPegaCase)
                    .withFieldConst(_.startCaseResponse, startCaseResponse)
                    .transform
                case j: Vat.CheckedPaymentPlan =>
                  j.into[Vat.StartedPegaCase]
                    .withFieldConst(_.stage, Stage.AfterStartedPegaCase.StartedPegaCase)
                    .withFieldConst(_.startCaseResponse, startCaseResponse)
                    .transform
                case j: Sa.CheckedPaymentPlan =>
                  j.into[Sa.StartedPegaCase]
                    .withFieldConst(_.stage, Stage.AfterStartedPegaCase.StartedPegaCase)
                    .withFieldConst(_.startCaseResponse, startCaseResponse)
                    .transform
                case j: Sia.CheckedPaymentPlan =>
                  j.into[Sa.StartedPegaCase]
                    .withFieldConst(_.stage, Stage.AfterStartedPegaCase.StartedPegaCase)
                    .withFieldConst(_.startCaseResponse, startCaseResponse)
                    .transform

                case j: Epaye.EnteredDetailsAboutBankAccount =>
                  j.into[Epaye.StartedPegaCase]
                    .withFieldConst(_.stage, Stage.AfterStartedPegaCase.StartedPegaCase)
                    .withFieldConst(_.startCaseResponse, startCaseResponse)
                    .transform
                case j: Vat.EnteredDetailsAboutBankAccount =>
                  j.into[Vat.StartedPegaCase]
                    .withFieldConst(_.stage, Stage.AfterStartedPegaCase.StartedPegaCase)
                    .withFieldConst(_.startCaseResponse, startCaseResponse)
                    .transform
                case j: Sa.EnteredDetailsAboutBankAccount =>
                  j.into[Sa.StartedPegaCase]
                    .withFieldConst(_.stage, Stage.AfterStartedPegaCase.StartedPegaCase)
                    .withFieldConst(_.startCaseResponse, startCaseResponse)
                    .transform
                case j: Sia.EnteredDetailsAboutBankAccount =>
                  j.into[Sia.StartedPegaCase]
                    .withFieldConst(_.stage, Stage.AfterStartedPegaCase.StartedPegaCase)
                    .withFieldConst(_.startCaseResponse, startCaseResponse)
                    .transform

                case j: Epaye.EnteredDirectDebitDetails =>
                  j.into[Epaye.StartedPegaCase]
                    .withFieldConst(_.stage, Stage.AfterStartedPegaCase.StartedPegaCase)
                    .withFieldConst(_.startCaseResponse, startCaseResponse)
                    .transform
                case j: Vat.EnteredDirectDebitDetails =>
                  j.into[Vat.StartedPegaCase]
                    .withFieldConst(_.stage, Stage.AfterStartedPegaCase.StartedPegaCase)
                    .withFieldConst(_.startCaseResponse, startCaseResponse)
                    .transform
                case j: Sa.EnteredDirectDebitDetails =>
                  j.into[Sa.StartedPegaCase]
                    .withFieldConst(_.stage, Stage.AfterStartedPegaCase.StartedPegaCase)
                    .withFieldConst(_.startCaseResponse, startCaseResponse)
                    .transform
                case j: Sia.EnteredDirectDebitDetails =>
                  j.into[Sia.StartedPegaCase]
                    .withFieldConst(_.stage, Stage.AfterStartedPegaCase.StartedPegaCase)
                    .withFieldConst(_.startCaseResponse, startCaseResponse)
                    .transform

                case j: Epaye.ConfirmedDirectDebitDetails =>
                  j.into[Epaye.StartedPegaCase]
                    .withFieldConst(_.stage, Stage.AfterStartedPegaCase.StartedPegaCase)
                    .withFieldConst(_.startCaseResponse, startCaseResponse)
                    .transform
                case j: Vat.ConfirmedDirectDebitDetails =>
                  j.into[Vat.StartedPegaCase]
                    .withFieldConst(_.stage, Stage.AfterStartedPegaCase.StartedPegaCase)
                    .withFieldConst(_.startCaseResponse, startCaseResponse)
                    .transform
                case j: Sa.ConfirmedDirectDebitDetails =>
                  j.into[Sa.StartedPegaCase]
                    .withFieldConst(_.stage, Stage.AfterStartedPegaCase.StartedPegaCase)
                    .withFieldConst(_.startCaseResponse, startCaseResponse)
                    .transform
                case j: Sia.ConfirmedDirectDebitDetails =>
                  j.into[Sia.StartedPegaCase]
                    .withFieldConst(_.stage, Stage.AfterStartedPegaCase.StartedPegaCase)
                    .withFieldConst(_.startCaseResponse, startCaseResponse)
                    .transform

                case j: Epaye.AgreedTermsAndConditions =>
                  j.into[Epaye.StartedPegaCase]
                    .withFieldConst(_.stage, Stage.AfterStartedPegaCase.StartedPegaCase)
                    .withFieldConst(_.startCaseResponse, startCaseResponse)
                    .transform
                case j: Vat.AgreedTermsAndConditions =>
                  j.into[Vat.StartedPegaCase]
                    .withFieldConst(_.stage, Stage.AfterStartedPegaCase.StartedPegaCase)
                    .withFieldConst(_.startCaseResponse, startCaseResponse)
                    .transform
                case j: Sa.AgreedTermsAndConditions =>
                  j.into[Sa.StartedPegaCase]
                    .withFieldConst(_.stage, Stage.AfterStartedPegaCase.StartedPegaCase)
                    .withFieldConst(_.startCaseResponse, startCaseResponse)
                    .transform
                case j: Sia.AgreedTermsAndConditions =>
                  j.into[Sia.StartedPegaCase]
                    .withFieldConst(_.stage, Stage.AfterStartedPegaCase.StartedPegaCase)
                    .withFieldConst(_.startCaseResponse, startCaseResponse)
                    .transform

                case j: Epaye.SelectedEmailToBeVerified =>
                  j.into[Epaye.StartedPegaCase]
                    .withFieldConst(_.stage, Stage.AfterStartedPegaCase.StartedPegaCase)
                    .withFieldConst(_.startCaseResponse, startCaseResponse)
                    .transform
                case j: Vat.SelectedEmailToBeVerified =>
                  j.into[Vat.StartedPegaCase]
                    .withFieldConst(_.stage, Stage.AfterStartedPegaCase.StartedPegaCase)
                    .withFieldConst(_.startCaseResponse, startCaseResponse)
                    .transform
                case j: Sa.SelectedEmailToBeVerified =>
                  j.into[Sa.StartedPegaCase]
                    .withFieldConst(_.stage, Stage.AfterStartedPegaCase.StartedPegaCase)
                    .withFieldConst(_.startCaseResponse, startCaseResponse)
                    .transform
                case j: Sia.SelectedEmailToBeVerified =>
                  j.into[Sia.StartedPegaCase]
                    .withFieldConst(_.stage, Stage.AfterStartedPegaCase.StartedPegaCase)
                    .withFieldConst(_.startCaseResponse, startCaseResponse)
                    .transform

                case j: Epaye.EmailVerificationComplete =>
                  j.into[Epaye.StartedPegaCase]
                    .withFieldConst(_.stage, Stage.AfterStartedPegaCase.StartedPegaCase)
                    .withFieldConst(_.startCaseResponse, startCaseResponse)
                    .transform
                case j: Vat.EmailVerificationComplete =>
                  j.into[Vat.StartedPegaCase]
                    .withFieldConst(_.stage, Stage.AfterStartedPegaCase.StartedPegaCase)
                    .withFieldConst(_.startCaseResponse, startCaseResponse)
                    .transform
                case j: Sa.EmailVerificationComplete =>
                  j.into[Sa.StartedPegaCase]
                    .withFieldConst(_.stage, Stage.AfterStartedPegaCase.StartedPegaCase)
                    .withFieldConst(_.startCaseResponse, startCaseResponse)
                    .transform
                case j: Sia.EmailVerificationComplete =>
                  j.into[Sia.StartedPegaCase]
                    .withFieldConst(_.stage, Stage.AfterStartedPegaCase.StartedPegaCase)
                    .withFieldConst(_.startCaseResponse, startCaseResponse)
                    .transform

                case _: Stages.SubmittedArrangement =>
                  Errors.throwBadRequestException("Cannot update PEGA StartCaseResponse when journey is in completed state")
              }
            )
        }
    }

  private def updateJourneyWithExistingValue(
      existingValue:   StartCaseResponse,
      existingJourney: Journey,
      newValue:        StartCaseResponse,
      newJourney:      Journey
  )(implicit request: Request[_]): Future[Journey] =
    if (existingValue === newValue) {
      JourneyLogger.info("Nothing to update, PEGA StartCaseResponse is the same as the existing one in journey.")
      Future.successful(existingJourney)
    } else {
      journeyService.upsert(newJourney)
    }

}
