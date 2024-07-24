/*
 * Copyright 2024 HM Revenue & Customs
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
import essttp.journey.model.{CanPayWithinSixMonthsAnswers, Journey, JourneyId, Stage}
import essttp.utils.Errors
import io.scalaland.chimney.dsl.TransformationOps
import play.api.mvc.{Action, ControllerComponents, Request}
import services.JourneyService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateCanPayWithinSixMonthsController @Inject() (
    actions:        Actions,
    journeyService: JourneyService,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext, cryptoFormat: OperationalCryptoFormat) extends BackendController(cc) {

  def updateCanPayWithinSixMonthsAnswers(journeyId: JourneyId): Action[CanPayWithinSixMonthsAnswers] =
    actions.authenticatedAction.async(parse.json[CanPayWithinSixMonthsAnswers]) { implicit request =>
      for {
        journey <- journeyService.get(journeyId)
        newJourney <- journey match {
          case j: Journey.BeforeRetrievedAffordabilityResult  => Errors.throwBadRequestExceptionF(s"UpdateCanPayWithinSixMonthsAnswers update is not possible in that state: [${j.stage.toString}]")
          case j: Journey.Stages.RetrievedAffordabilityResult => updateJourneyWithNewValue(j, request.body)
          case j: Journey.AfterCanPayWithinSixMonthsAnswers   => updateJourneyWithExistingValue(j, request.body)
        }
      } yield Ok(newJourney.json)
    }

  private def updateJourneyWithNewValue(
      journey: Stages.RetrievedAffordabilityResult,
      answers: CanPayWithinSixMonthsAnswers
  )(implicit request: Request[_]): Future[Journey] = {
    val newJourney: Journey.AfterCanPayWithinSixMonthsAnswers = journey match {
      case j: Epaye.RetrievedAffordabilityResult =>
        j.into[Epaye.ObtainedCanPayWithinSixMonthsAnswers]
          .withFieldConst(_.stage, determineStage(answers))
          .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
          .transform
      case j: Vat.RetrievedAffordabilityResult =>
        j.into[Vat.ObtainedCanPayWithinSixMonthsAnswers]
          .withFieldConst(_.stage, determineStage(answers))
          .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
          .transform
      case j: Sa.RetrievedAffordabilityResult =>
        j.into[Sa.ObtainedCanPayWithinSixMonthsAnswers]
          .withFieldConst(_.stage, determineStage(answers))
          .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
          .transform
    }
    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingValue(
      journey: Journey.AfterCanPayWithinSixMonthsAnswers,
      answers: CanPayWithinSixMonthsAnswers
  )(implicit request: Request[_]): Future[Journey] =
    if (journey.canPayWithinSixMonthsAnswers === answers) {
      Future.successful(journey)
    } else {
      val newJourney: Journey = journey match {
        case j: Epaye.ObtainedCanPayWithinSixMonthsAnswers =>
          j.copy(canPayWithinSixMonthsAnswers = answers, stage = determineStage(answers))
        case j: Vat.ObtainedCanPayWithinSixMonthsAnswers =>
          j.copy(canPayWithinSixMonthsAnswers = answers, stage = determineStage(answers))
        case j: Sa.ObtainedCanPayWithinSixMonthsAnswers =>
          j.copy(canPayWithinSixMonthsAnswers = answers, stage = determineStage(answers))

        case j: Epaye.EnteredMonthlyPaymentAmount =>
          j.into[Epaye.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.stage, determineStage(answers))
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform
        case j: Vat.EnteredMonthlyPaymentAmount =>
          j.into[Vat.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.stage, determineStage(answers))
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform
        case j: Sa.EnteredMonthlyPaymentAmount =>
          j.into[Sa.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.stage, determineStage(answers))
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform

        case j: Epaye.EnteredDayOfMonth =>
          j.into[Epaye.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.stage, determineStage(answers))
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform
        case j: Vat.EnteredDayOfMonth =>
          j.into[Vat.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.stage, determineStage(answers))
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform
        case j: Sa.EnteredDayOfMonth =>
          j.into[Sa.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.stage, determineStage(answers))
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform

        case j: Epaye.RetrievedStartDates =>
          j.into[Epaye.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.stage, determineStage(answers))
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform
        case j: Vat.RetrievedStartDates =>
          j.into[Vat.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.stage, determineStage(answers))
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform
        case j: Sa.RetrievedStartDates =>
          j.into[Sa.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.stage, determineStage(answers))
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform

        case j: Epaye.RetrievedAffordableQuotes =>
          j.into[Epaye.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.stage, determineStage(answers))
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform
        case j: Vat.RetrievedAffordableQuotes =>
          j.into[Vat.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.stage, determineStage(answers))
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform
        case j: Sa.RetrievedAffordableQuotes =>
          j.into[Sa.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.stage, determineStage(answers))
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform

        case j: Epaye.ChosenPaymentPlan =>
          j.into[Epaye.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.stage, determineStage(answers))
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform
        case j: Vat.ChosenPaymentPlan =>
          j.into[Vat.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.stage, determineStage(answers))
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform
        case j: Sa.ChosenPaymentPlan =>
          j.into[Sa.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.stage, determineStage(answers))
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform

        case j: Epaye.CheckedPaymentPlan =>
          j.into[Epaye.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.stage, determineStage(answers))
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform
        case j: Vat.CheckedPaymentPlan =>
          j.into[Vat.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.stage, determineStage(answers))
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform
        case j: Sa.CheckedPaymentPlan =>
          j.into[Sa.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.stage, determineStage(answers))
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform

        case j: Epaye.EnteredDetailsAboutBankAccount =>
          j.into[Epaye.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.stage, determineStage(answers))
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform
        case j: Vat.EnteredDetailsAboutBankAccount =>
          j.into[Vat.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.stage, determineStage(answers))
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform
        case j: Sa.EnteredDetailsAboutBankAccount =>
          j.into[Sa.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.stage, determineStage(answers))
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform

        case j: Epaye.EnteredDirectDebitDetails =>
          j.into[Epaye.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.stage, determineStage(answers))
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform
        case j: Vat.EnteredDirectDebitDetails =>
          j.into[Vat.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.stage, determineStage(answers))
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform
        case j: Sa.EnteredDirectDebitDetails =>
          j.into[Sa.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.stage, determineStage(answers))
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform

        case j: Epaye.ConfirmedDirectDebitDetails =>
          j.into[Epaye.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.stage, determineStage(answers))
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform
        case j: Vat.ConfirmedDirectDebitDetails =>
          j.into[Vat.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.stage, determineStage(answers))
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform
        case j: Sa.ConfirmedDirectDebitDetails =>
          j.into[Sa.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.stage, determineStage(answers))
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform

        case j: Epaye.AgreedTermsAndConditions =>
          j.into[Epaye.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.stage, determineStage(answers))
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform
        case j: Vat.AgreedTermsAndConditions =>
          j.into[Vat.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.stage, determineStage(answers))
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform
        case j: Sa.AgreedTermsAndConditions =>
          j.into[Sa.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.stage, determineStage(answers))
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform

        case j: Epaye.SelectedEmailToBeVerified =>
          j.into[Epaye.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.stage, determineStage(answers))
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform
        case j: Vat.SelectedEmailToBeVerified =>
          j.into[Vat.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.stage, determineStage(answers))
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform
        case j: Sa.SelectedEmailToBeVerified =>
          j.into[Sa.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.stage, determineStage(answers))
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform

        case j: Epaye.EmailVerificationComplete =>
          j.into[Epaye.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.stage, determineStage(answers))
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform
        case j: Vat.EmailVerificationComplete =>
          j.into[Vat.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.stage, determineStage(answers))
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform
        case j: Sa.EmailVerificationComplete =>
          j.into[Sa.ObtainedCanPayWithinSixMonthsAnswers]
            .withFieldConst(_.stage, determineStage(answers))
            .withFieldConst(_.canPayWithinSixMonthsAnswers, answers)
            .transform

        case _: Stages.SubmittedArrangement =>
          Errors.throwBadRequestException("Cannot update CanPayWithinSixMonthsAnswers when journey is in completed state")
      }

      journeyService.upsert(newJourney)
    }

  private def determineStage(canPayWithinSixMonthsAnswers: CanPayWithinSixMonthsAnswers): Stage.AfterCanPayWithinSixMonthsAnswers =
    canPayWithinSixMonthsAnswers match {
      case CanPayWithinSixMonthsAnswers.AnswerNotRequired        => Stage.AfterCanPayWithinSixMonthsAnswers.AnswerNotRequired
      case _: CanPayWithinSixMonthsAnswers.CanPayWithinSixMonths => Stage.AfterCanPayWithinSixMonthsAnswers.AnswerRequired
    }

}
