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
import essttp.journey.model.Journey.Stages.{AnsweredCanPayUpfront, EnteredUpfrontPaymentAmount}
import essttp.journey.model.Journey.{Epaye, Sa, Simp, Vat}
import essttp.journey.model.{Journey, JourneyId, PaymentPlanAnswers, Stage, UpfrontPaymentAnswers}
import essttp.rootmodel.dates.extremedates.ExtremeDatesResponse
import essttp.rootmodel.dates.startdates.StartDatesResponse
import essttp.utils.Errors
import io.scalaland.chimney.dsl.TransformationOps
import play.api.mvc.{Action, ControllerComponents, Request}
import services.JourneyService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateDatesController @Inject() (
    actions:        Actions,
    journeyService: JourneyService,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext, cryptoFormat: OperationalCryptoFormat) extends BackendController(cc) {

  def updateExtremeDates(journeyId: JourneyId): Action[ExtremeDatesResponse] = actions.authenticatedAction.async(parse.json[ExtremeDatesResponse]) { implicit request =>
    for {
      journey <- journeyService.get(journeyId)
      newJourney <- journey match {
        case j: Journey.Stages.EnteredUpfrontPaymentAmount => updateJourneyWithNewExtremeDatesValue(Right(j), request.body)
        case j: Journey.Stages.AnsweredCanPayUpfront       => updateJourneyWithNewExtremeDatesValue(Left(j), request.body)
        case j: Journey.AfterExtremeDatesResponse => j match {
          case _: Journey.BeforeArrangementSubmitted => updateJourneyWithExistingExtremeDatesValue (j, request.body)
          case _: Journey.AfterArrangementSubmitted  => Errors.throwBadRequestExceptionF("Cannot update ExtremeDates when journey is in completed state")
        }
        case j: Journey.BeforeUpfrontPaymentAnswers => Errors.throwBadRequestExceptionF(s"UpdateExtremeDatesResponse update is not possible in that state: [${j.stage.toString}]")
      }
    } yield Ok(newJourney.json)
  }

  private def updateJourneyWithNewExtremeDatesValue(
      journey:              Either[AnsweredCanPayUpfront, EnteredUpfrontPaymentAmount],
      extremeDatesResponse: ExtremeDatesResponse
  )(implicit request: Request[_]): Future[Journey] = {
    val newJourney: Journey.Stages.RetrievedExtremeDates = journey match {
      case Left(j: Epaye.AnsweredCanPayUpfront) => j.into[Journey.Epaye.RetrievedExtremeDates]
        .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
        .withFieldConst(_.upfrontPaymentAnswers, UpfrontPaymentAnswers.NoUpfrontPayment)
        .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
        .transform
      case Left(j: Vat.AnsweredCanPayUpfront) => j.into[Journey.Vat.RetrievedExtremeDates]
        .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
        .withFieldConst(_.upfrontPaymentAnswers, UpfrontPaymentAnswers.NoUpfrontPayment)
        .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
        .transform
      case Left(j: Sa.AnsweredCanPayUpfront) => j.into[Journey.Sa.RetrievedExtremeDates]
        .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
        .withFieldConst(_.upfrontPaymentAnswers, UpfrontPaymentAnswers.NoUpfrontPayment)
        .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
        .transform
      case Left(j: Simp.AnsweredCanPayUpfront) => j.into[Journey.Simp.RetrievedExtremeDates]
        .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
        .withFieldConst(_.upfrontPaymentAnswers, UpfrontPaymentAnswers.NoUpfrontPayment)
        .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
        .transform

      case Right(j: Epaye.EnteredUpfrontPaymentAmount) => j.into[Journey.Epaye.RetrievedExtremeDates]
        .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
        .withFieldConst(_.upfrontPaymentAnswers, UpfrontPaymentAnswers.DeclaredUpfrontPayment(j.upfrontPaymentAmount))
        .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
        .transform
      case Right(j: Vat.EnteredUpfrontPaymentAmount) => j.into[Journey.Vat.RetrievedExtremeDates]
        .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
        .withFieldConst(_.upfrontPaymentAnswers, UpfrontPaymentAnswers.DeclaredUpfrontPayment(j.upfrontPaymentAmount))
        .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
        .transform
      case Right(j: Sa.EnteredUpfrontPaymentAmount) => j.into[Journey.Sa.RetrievedExtremeDates]
        .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
        .withFieldConst(_.upfrontPaymentAnswers, UpfrontPaymentAnswers.DeclaredUpfrontPayment(j.upfrontPaymentAmount))
        .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
        .transform
      case Right(j: Simp.EnteredUpfrontPaymentAmount) => j.into[Journey.Simp.RetrievedExtremeDates]
        .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
        .withFieldConst(_.upfrontPaymentAnswers, UpfrontPaymentAnswers.DeclaredUpfrontPayment(j.upfrontPaymentAmount))
        .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
        .transform
    }
    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingExtremeDatesValue(
      journey:              Journey.AfterExtremeDatesResponse,
      extremeDatesResponse: ExtremeDatesResponse
  )(implicit request: Request[_]): Future[Journey] = {

    if (journey.extremeDatesResponse === extremeDatesResponse) {
      JourneyLogger.info("Nothing to update, ExtremeDatesResponse is the same as the existing one in journey.")
      Future.successful(journey)
    } else {
      val newJourney: Journey.AfterExtremeDatesResponse = journey match {

        case j: Journey.Epaye.RetrievedExtremeDates => j.copy(extremeDatesResponse = extremeDatesResponse)
        case j: Journey.Vat.RetrievedExtremeDates   => j.copy(extremeDatesResponse = extremeDatesResponse)
        case j: Journey.Sa.RetrievedExtremeDates    => j.copy(extremeDatesResponse = extremeDatesResponse)
        case j: Journey.Simp.RetrievedExtremeDates  => j.copy(extremeDatesResponse = extremeDatesResponse)

        case j: Journey.Epaye.RetrievedAffordabilityResult =>
          j.into[Journey.Epaye.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Vat.RetrievedAffordabilityResult =>
          j.into[Journey.Vat.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Sa.RetrievedAffordabilityResult =>
          j.into[Journey.Sa.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Simp.RetrievedAffordabilityResult =>
          j.into[Journey.Simp.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform

        case j: Journey.Epaye.ObtainedCanPayWithinSixMonthsAnswers =>
          j.into[Journey.Epaye.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Vat.ObtainedCanPayWithinSixMonthsAnswers =>
          j.into[Journey.Vat.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Sa.ObtainedCanPayWithinSixMonthsAnswers =>
          j.into[Journey.Sa.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Simp.ObtainedCanPayWithinSixMonthsAnswers =>
          j.into[Journey.Simp.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform

        case j: Journey.Epaye.StartedPegaCase =>
          j.into[Journey.Epaye.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Vat.StartedPegaCase =>
          j.into[Journey.Vat.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Sa.StartedPegaCase =>
          j.into[Journey.Sa.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Simp.StartedPegaCase =>
          j.into[Journey.Simp.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform

        case j: Journey.Epaye.EnteredMonthlyPaymentAmount =>
          j.into[Journey.Epaye.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Vat.EnteredMonthlyPaymentAmount =>
          j.into[Journey.Vat.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Sa.EnteredMonthlyPaymentAmount =>
          j.into[Journey.Sa.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Simp.EnteredMonthlyPaymentAmount =>
          j.into[Journey.Simp.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform

        case j: Journey.Epaye.EnteredDayOfMonth =>
          j.into[Journey.Epaye.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Vat.EnteredDayOfMonth =>
          j.into[Journey.Vat.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Sa.EnteredDayOfMonth =>
          j.into[Journey.Sa.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Simp.EnteredDayOfMonth =>
          j.into[Journey.Simp.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform

        case j: Journey.Epaye.RetrievedStartDates =>
          j.into[Journey.Epaye.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Vat.RetrievedStartDates =>
          j.into[Journey.Vat.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Sa.RetrievedStartDates =>
          j.into[Journey.Sa.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Simp.RetrievedStartDates =>
          j.into[Journey.Simp.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform

        case j: Journey.Epaye.RetrievedAffordableQuotes =>
          j.into[Journey.Epaye.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Vat.RetrievedAffordableQuotes =>
          j.into[Journey.Vat.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Sa.RetrievedAffordableQuotes =>
          j.into[Journey.Sa.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Simp.RetrievedAffordableQuotes =>
          j.into[Journey.Simp.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform

        case j: Journey.Epaye.ChosenPaymentPlan =>
          j.into[Journey.Epaye.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Vat.ChosenPaymentPlan =>
          j.into[Journey.Vat.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Sa.ChosenPaymentPlan =>
          j.into[Journey.Sa.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Simp.ChosenPaymentPlan =>
          j.into[Journey.Simp.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform

        case j: Journey.Epaye.CheckedPaymentPlan =>
          j.into[Journey.Epaye.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Vat.CheckedPaymentPlan =>
          j.into[Journey.Vat.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Sa.CheckedPaymentPlan =>
          j.into[Journey.Sa.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Simp.CheckedPaymentPlan =>
          j.into[Journey.Simp.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform

        case j: Journey.Epaye.EnteredCanYouSetUpDirectDebit =>
          j.into[Journey.Epaye.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Vat.EnteredCanYouSetUpDirectDebit =>
          j.into[Journey.Vat.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Sa.EnteredCanYouSetUpDirectDebit =>
          j.into[Journey.Sa.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Simp.EnteredCanYouSetUpDirectDebit =>
          j.into[Journey.Simp.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform

        case j: Journey.Epaye.EnteredDirectDebitDetails =>
          j.into[Journey.Epaye.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Vat.EnteredDirectDebitDetails =>
          j.into[Journey.Vat.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Sa.EnteredDirectDebitDetails =>
          j.into[Journey.Sa.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Simp.EnteredDirectDebitDetails =>
          j.into[Journey.Simp.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform

        case j: Journey.Epaye.ConfirmedDirectDebitDetails =>
          j.into[Journey.Epaye.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Vat.ConfirmedDirectDebitDetails =>
          j.into[Journey.Vat.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Sa.ConfirmedDirectDebitDetails =>
          j.into[Journey.Sa.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Simp.ConfirmedDirectDebitDetails =>
          j.into[Journey.Simp.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform

        case j: Journey.Epaye.AgreedTermsAndConditions =>
          j.into[Journey.Epaye.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Vat.AgreedTermsAndConditions =>
          j.into[Journey.Vat.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Sa.AgreedTermsAndConditions =>
          j.into[Journey.Sa.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Simp.AgreedTermsAndConditions =>
          j.into[Journey.Simp.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform

        case j: Journey.Epaye.SelectedEmailToBeVerified =>
          j.into[Journey.Epaye.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Vat.SelectedEmailToBeVerified =>
          j.into[Journey.Vat.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Sa.SelectedEmailToBeVerified =>
          j.into[Journey.Sa.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Simp.SelectedEmailToBeVerified =>
          j.into[Journey.Simp.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform

        case j: Journey.Epaye.EmailVerificationComplete =>
          j.into[Journey.Epaye.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Vat.EmailVerificationComplete =>
          j.into[Journey.Vat.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Sa.EmailVerificationComplete =>
          j.into[Journey.Sa.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform
        case j: Journey.Simp.EmailVerificationComplete =>
          j.into[Journey.Simp.RetrievedExtremeDates]
            .withFieldConst(_.stage, Stage.AfterExtremeDatesResponse.ExtremeDatesResponseRetrieved)
            .withFieldConst(_.extremeDatesResponse, extremeDatesResponse)
            .transform

        case _: Journey.Stages.SubmittedArrangement =>
          Errors.throwBadRequestException("Cannot update ExtremeDates when journey is in completed state")
      }
      journeyService.upsert(newJourney)
    }
  }

  def updateStartDates(journeyId: JourneyId): Action[StartDatesResponse] = actions.authenticatedAction.async(parse.json[StartDatesResponse]) { implicit request =>
    for {
      journey <- journeyService.get(journeyId)
      newJourney <- journey match {
        case j: Journey.BeforeEnteredDayOfMonth  => Errors.throwBadRequestExceptionF(s"UpdateStartDates is not possible when we don't have a chosen day of month, stage: [ ${j.stage.toString} ]")
        case j: Journey.Stages.EnteredDayOfMonth => updateJourneyWithNewStartDatesValue(j, request.body)
        case j: Journey.AfterStartDatesResponse  => updateJourneyWithExistingStartDatesValue(Left(j), request.body)
        case j: Journey.AfterCheckedPaymentPlan =>
          j match {
            case _: Journey.BeforeArrangementSubmitted => updateJourneyWithExistingStartDatesValue(Right(j), request.body)
            case _: Journey.AfterArrangementSubmitted  => Errors.throwBadRequestExceptionF("Cannot update StartDates when journey is in completed state")
          }
        case _: Journey.AfterStartedPegaCase => Errors.throwBadRequestExceptionF("Not expecting to update ExtremeDates after starting PEGA case")
      }
    } yield Ok(newJourney.json)
  }

  private def updateJourneyWithNewStartDatesValue(
      journey:            Journey.Stages.EnteredDayOfMonth,
      startDatesResponse: StartDatesResponse
  )(implicit request: Request[_]): Future[Journey] = {
    val newJourney: Journey.AfterStartDatesResponse = journey match {
      case j: Epaye.EnteredDayOfMonth =>
        j.into[Journey.Epaye.RetrievedStartDates]
          .withFieldConst(_.stage, Stage.AfterStartDatesResponse.StartDatesResponseRetrieved)
          .withFieldConst(_.startDatesResponse, startDatesResponse)
          .transform
      case j: Vat.EnteredDayOfMonth =>
        j.into[Journey.Vat.RetrievedStartDates]
          .withFieldConst(_.stage, Stage.AfterStartDatesResponse.StartDatesResponseRetrieved)
          .withFieldConst(_.startDatesResponse, startDatesResponse)
          .transform
      case j: Sa.EnteredDayOfMonth =>
        j.into[Journey.Sa.RetrievedStartDates]
          .withFieldConst(_.stage, Stage.AfterStartDatesResponse.StartDatesResponseRetrieved)
          .withFieldConst(_.startDatesResponse, startDatesResponse)
          .transform
      case j: Simp.EnteredDayOfMonth =>
        j.into[Journey.Simp.RetrievedStartDates]
          .withFieldConst(_.stage, Stage.AfterStartDatesResponse.StartDatesResponseRetrieved)
          .withFieldConst(_.startDatesResponse, startDatesResponse)
          .transform
    }
    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingStartDatesValue(
      journey:            Either[Journey.AfterStartDatesResponse, Journey.AfterCheckedPaymentPlan],
      startDatesResponse: StartDatesResponse
  )(implicit request: Request[_]): Future[Journey] =
    journey match {
      case Left(afterStartDatesResponse) =>
        updateJourneyWithExistingValue(
          afterStartDatesResponse.startDatesResponse,
          afterStartDatesResponse,
          startDatesResponse,
          afterStartDatesResponse match {
            case j: Journey.Epaye.RetrievedStartDates => j.copy(startDatesResponse = startDatesResponse)
            case j: Journey.Vat.RetrievedStartDates   => j.copy(startDatesResponse = startDatesResponse)
            case j: Journey.Sa.RetrievedStartDates    => j.copy(startDatesResponse = startDatesResponse)
            case j: Journey.Simp.RetrievedStartDates  => j.copy(startDatesResponse = startDatesResponse)

            case j: Journey.Epaye.RetrievedAffordableQuotes =>
              j.into[Journey.Epaye.RetrievedStartDates]
                .withFieldConst(_.stage, Stage.AfterStartDatesResponse.StartDatesResponseRetrieved)
                .withFieldConst(_.startDatesResponse, startDatesResponse)
                .transform
            case j: Journey.Vat.RetrievedAffordableQuotes =>
              j.into[Journey.Vat.RetrievedStartDates]
                .withFieldConst(_.stage, Stage.AfterStartDatesResponse.StartDatesResponseRetrieved)
                .withFieldConst(_.startDatesResponse, startDatesResponse)
                .transform
            case j: Journey.Sa.RetrievedAffordableQuotes =>
              j.into[Journey.Sa.RetrievedStartDates]
                .withFieldConst(_.stage, Stage.AfterStartDatesResponse.StartDatesResponseRetrieved)
                .withFieldConst(_.startDatesResponse, startDatesResponse)
                .transform
            case j: Journey.Simp.RetrievedAffordableQuotes =>
              j.into[Journey.Simp.RetrievedStartDates]
                .withFieldConst(_.stage, Stage.AfterStartDatesResponse.StartDatesResponseRetrieved)
                .withFieldConst(_.startDatesResponse, startDatesResponse)
                .transform

            case j: Journey.Epaye.ChosenPaymentPlan =>
              j.into[Journey.Epaye.RetrievedStartDates]
                .withFieldConst(_.stage, Stage.AfterStartDatesResponse.StartDatesResponseRetrieved)
                .withFieldConst(_.startDatesResponse, startDatesResponse)
                .transform
            case j: Journey.Vat.ChosenPaymentPlan =>
              j.into[Journey.Vat.RetrievedStartDates]
                .withFieldConst(_.stage, Stage.AfterStartDatesResponse.StartDatesResponseRetrieved)
                .withFieldConst(_.startDatesResponse, startDatesResponse)
                .transform
            case j: Journey.Sa.ChosenPaymentPlan =>
              j.into[Journey.Sa.RetrievedStartDates]
                .withFieldConst(_.stage, Stage.AfterStartDatesResponse.StartDatesResponseRetrieved)
                .withFieldConst(_.startDatesResponse, startDatesResponse)
                .transform
            case j: Journey.Simp.ChosenPaymentPlan =>
              j.into[Journey.Simp.RetrievedStartDates]
                .withFieldConst(_.stage, Stage.AfterStartDatesResponse.StartDatesResponseRetrieved)
                .withFieldConst(_.startDatesResponse, startDatesResponse)
                .transform
          }
        )

      case Right(afterCheckedPaymentPlan) =>
        afterCheckedPaymentPlan.paymentPlanAnswers match {
          case _: PaymentPlanAnswers.PaymentPlanAfterAffordability =>
            Errors.throwBadRequestExceptionF("Cannot update StartDatesResponse on affordability journey")

          case p: PaymentPlanAnswers.PaymentPlanNoAffordability =>
            updateJourneyWithExistingValue(
              p.startDatesResponse,
              afterCheckedPaymentPlan,
              startDatesResponse,
              afterCheckedPaymentPlan match {
                case j: Journey.Epaye.CheckedPaymentPlan =>
                  j.into[Journey.Epaye.RetrievedStartDates]
                    .withFieldConst(_.stage, Stage.AfterStartDatesResponse.StartDatesResponseRetrieved)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, startDatesResponse)
                    .transform
                case j: Journey.Vat.CheckedPaymentPlan =>
                  j.into[Journey.Vat.RetrievedStartDates]
                    .withFieldConst(_.stage, Stage.AfterStartDatesResponse.StartDatesResponseRetrieved)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, startDatesResponse)
                    .transform
                case j: Journey.Sa.CheckedPaymentPlan =>
                  j.into[Journey.Sa.RetrievedStartDates]
                    .withFieldConst(_.stage, Stage.AfterStartDatesResponse.StartDatesResponseRetrieved)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, startDatesResponse)
                    .transform
                case j: Journey.Simp.CheckedPaymentPlan =>
                  j.into[Journey.Simp.RetrievedStartDates]
                    .withFieldConst(_.stage, Stage.AfterStartDatesResponse.StartDatesResponseRetrieved)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, startDatesResponse)
                    .transform

                case j: Journey.Epaye.EnteredCanYouSetUpDirectDebit =>
                  j.into[Journey.Epaye.RetrievedStartDates]
                    .withFieldConst(_.stage, Stage.AfterStartDatesResponse.StartDatesResponseRetrieved)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, startDatesResponse)
                    .transform
                case j: Journey.Vat.EnteredCanYouSetUpDirectDebit =>
                  j.into[Journey.Vat.RetrievedStartDates]
                    .withFieldConst(_.stage, Stage.AfterStartDatesResponse.StartDatesResponseRetrieved)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, startDatesResponse)
                    .transform
                case j: Journey.Sa.EnteredCanYouSetUpDirectDebit =>
                  j.into[Journey.Sa.RetrievedStartDates]
                    .withFieldConst(_.stage, Stage.AfterStartDatesResponse.StartDatesResponseRetrieved)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, startDatesResponse)
                    .transform
                case j: Journey.Simp.EnteredCanYouSetUpDirectDebit =>
                  j.into[Journey.Simp.RetrievedStartDates]
                    .withFieldConst(_.stage, Stage.AfterStartDatesResponse.StartDatesResponseRetrieved)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, startDatesResponse)
                    .transform

                case j: Journey.Epaye.EnteredDirectDebitDetails =>
                  j.into[Journey.Epaye.RetrievedStartDates]
                    .withFieldConst(_.stage, Stage.AfterStartDatesResponse.StartDatesResponseRetrieved)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, startDatesResponse)
                    .transform
                case j: Journey.Vat.EnteredDirectDebitDetails =>
                  j.into[Journey.Vat.RetrievedStartDates]
                    .withFieldConst(_.stage, Stage.AfterStartDatesResponse.StartDatesResponseRetrieved)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, startDatesResponse)
                    .transform
                case j: Journey.Sa.EnteredDirectDebitDetails =>
                  j.into[Journey.Sa.RetrievedStartDates]
                    .withFieldConst(_.stage, Stage.AfterStartDatesResponse.StartDatesResponseRetrieved)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, startDatesResponse)
                    .transform
                case j: Journey.Simp.EnteredDirectDebitDetails =>
                  j.into[Journey.Simp.RetrievedStartDates]
                    .withFieldConst(_.stage, Stage.AfterStartDatesResponse.StartDatesResponseRetrieved)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, startDatesResponse)
                    .transform

                case j: Journey.Epaye.ConfirmedDirectDebitDetails =>
                  j.into[Journey.Epaye.RetrievedStartDates]
                    .withFieldConst(_.stage, Stage.AfterStartDatesResponse.StartDatesResponseRetrieved)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, startDatesResponse)
                    .transform
                case j: Journey.Vat.ConfirmedDirectDebitDetails =>
                  j.into[Journey.Vat.RetrievedStartDates]
                    .withFieldConst(_.stage, Stage.AfterStartDatesResponse.StartDatesResponseRetrieved)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, startDatesResponse)
                    .transform
                case j: Journey.Sa.ConfirmedDirectDebitDetails =>
                  j.into[Journey.Sa.RetrievedStartDates]
                    .withFieldConst(_.stage, Stage.AfterStartDatesResponse.StartDatesResponseRetrieved)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, startDatesResponse)
                    .transform
                case j: Journey.Simp.ConfirmedDirectDebitDetails =>
                  j.into[Journey.Simp.RetrievedStartDates]
                    .withFieldConst(_.stage, Stage.AfterStartDatesResponse.StartDatesResponseRetrieved)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, startDatesResponse)
                    .transform

                case j: Journey.Epaye.AgreedTermsAndConditions =>
                  j.into[Journey.Epaye.RetrievedStartDates]
                    .withFieldConst(_.stage, Stage.AfterStartDatesResponse.StartDatesResponseRetrieved)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, startDatesResponse)
                    .transform
                case j: Journey.Vat.AgreedTermsAndConditions =>
                  j.into[Journey.Vat.RetrievedStartDates]
                    .withFieldConst(_.stage, Stage.AfterStartDatesResponse.StartDatesResponseRetrieved)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, startDatesResponse)
                    .transform
                case j: Journey.Sa.AgreedTermsAndConditions =>
                  j.into[Journey.Sa.RetrievedStartDates]
                    .withFieldConst(_.stage, Stage.AfterStartDatesResponse.StartDatesResponseRetrieved)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, startDatesResponse)
                    .transform
                case j: Journey.Simp.AgreedTermsAndConditions =>
                  j.into[Journey.Simp.RetrievedStartDates]
                    .withFieldConst(_.stage, Stage.AfterStartDatesResponse.StartDatesResponseRetrieved)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, startDatesResponse)
                    .transform

                case j: Journey.Epaye.SelectedEmailToBeVerified =>
                  j.into[Journey.Epaye.RetrievedStartDates]
                    .withFieldConst(_.stage, Stage.AfterStartDatesResponse.StartDatesResponseRetrieved)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, startDatesResponse)
                    .transform
                case j: Journey.Vat.SelectedEmailToBeVerified =>
                  j.into[Journey.Vat.RetrievedStartDates]
                    .withFieldConst(_.stage, Stage.AfterStartDatesResponse.StartDatesResponseRetrieved)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, startDatesResponse)
                    .transform
                case j: Journey.Sa.SelectedEmailToBeVerified =>
                  j.into[Journey.Sa.RetrievedStartDates]
                    .withFieldConst(_.stage, Stage.AfterStartDatesResponse.StartDatesResponseRetrieved)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, startDatesResponse)
                    .transform
                case j: Journey.Simp.SelectedEmailToBeVerified =>
                  j.into[Journey.Simp.RetrievedStartDates]
                    .withFieldConst(_.stage, Stage.AfterStartDatesResponse.StartDatesResponseRetrieved)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, startDatesResponse)
                    .transform

                case j: Journey.Epaye.EmailVerificationComplete =>
                  j.into[Journey.Epaye.RetrievedStartDates]
                    .withFieldConst(_.stage, Stage.AfterStartDatesResponse.StartDatesResponseRetrieved)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, startDatesResponse)
                    .transform
                case j: Journey.Vat.EmailVerificationComplete =>
                  j.into[Journey.Vat.RetrievedStartDates]
                    .withFieldConst(_.stage, Stage.AfterStartDatesResponse.StartDatesResponseRetrieved)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, startDatesResponse)
                    .transform
                case j: Journey.Sa.EmailVerificationComplete =>
                  j.into[Journey.Sa.RetrievedStartDates]
                    .withFieldConst(_.stage, Stage.AfterStartDatesResponse.StartDatesResponseRetrieved)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, startDatesResponse)
                    .transform
                case j: Journey.Simp.EmailVerificationComplete =>
                  j.into[Journey.Simp.RetrievedStartDates]
                    .withFieldConst(_.stage, Stage.AfterStartDatesResponse.StartDatesResponseRetrieved)
                    .withFieldConst(_.monthlyPaymentAmount, p.monthlyPaymentAmount)
                    .withFieldConst(_.dayOfMonth, p.dayOfMonth)
                    .withFieldConst(_.startDatesResponse, startDatesResponse)
                    .transform

                case _: Journey.Stages.SubmittedArrangement =>
                  Errors.throwBadRequestException("Cannot update StartDates when journey is in completed state")
              }
            )
        }
    }

  private def updateJourneyWithExistingValue(
      existingValue:   StartDatesResponse,
      existingJourney: Journey,
      newValue:        StartDatesResponse,
      newJourney:      Journey
  )(implicit request: Request[_]): Future[Journey] =
    if (existingValue === newValue) {
      JourneyLogger.info("Nothing to update, StartDatesResponse is the same as the existing one in journey.")
      Future.successful(existingJourney)
    } else {
      journeyService.upsert(newJourney)
    }

}
