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
import cats.Eq
import cats.syntax.eq._
import com.google.inject.{Inject, Singleton}
import essttp.crypto.CryptoFormat.OperationalCryptoFormat
import essttp.journey.model.Journey.{Epaye, Sa, Simp, Stages, Vat}
import essttp.journey.model._
import essttp.rootmodel.ttp.eligibility.EligibilityCheckResult
import essttp.utils.Errors
import io.scalaland.chimney.dsl._
import play.api.mvc._
import services.JourneyService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateEligibilityCheckResultController @Inject() (
    actions:        Actions,
    journeyService: JourneyService,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext, cryptoFormat: OperationalCryptoFormat) extends BackendController(cc) {

  implicit val eq: Eq[EligibilityCheckResult] = Eq.fromUniversalEquals

  def updateEligibilityResult(journeyId: JourneyId): Action[EligibilityCheckResult] = actions.authenticatedAction.async(parse.json[EligibilityCheckResult]) { implicit request =>
    for {
      journey <- journeyService.get(journeyId)
      newJourney <- journey match {
        case _: Journey.BeforeComputedTaxId  => Errors.throwBadRequestExceptionF("EligibilityCheckResult update is not possible in that state.")
        case j: Journey.Stages.ComputedTaxId => updateJourneyWithNewValue(j, request.body)
        case j: Journey.AfterEligibilityChecked => j match {
          case j: Journey.BeforeArrangementSubmitted => updateJourneyWithExistingValue(j, request.body)
          case _: Journey.AfterArrangementSubmitted  => Errors.throwBadRequestExceptionF("Cannot update EligibilityCheckResult when journey is in completed state")
        }
      }
    } yield Ok(newJourney.json)
  }

  private def updateJourneyWithNewValue(
      journey:                Stages.ComputedTaxId,
      eligibilityCheckResult: EligibilityCheckResult
  )(implicit request: Request[_]): Future[Journey] = {
    journey match {
      case j: Journey.Epaye.ComputedTaxId =>
        val newJourney = j.into[Journey.Epaye.EligibilityChecked]
          .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
          .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
          .transform
        journeyService.upsert(newJourney)
      case j: Journey.Vat.ComputedTaxId =>
        val newJourney = j.into[Journey.Vat.EligibilityChecked]
          .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
          .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
          .transform
        journeyService.upsert(newJourney)
      case j: Journey.Sa.ComputedTaxId =>
        val newJourney = j.into[Journey.Sa.EligibilityChecked]
          .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
          .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
          .transform
        journeyService.upsert(newJourney)
      case j: Journey.Simp.ComputedTaxId =>
        val newJourney = j.into[Journey.Simp.EligibilityChecked]
          .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
          .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
          .transform
        journeyService.upsert(newJourney)
    }
  }

  private def updateJourneyWithExistingValue(
      journey:                Journey.AfterEligibilityChecked,
      eligibilityCheckResult: EligibilityCheckResult
  )(implicit request: Request[_]): Future[Journey] = {
    if (journey.eligibilityCheckResult === eligibilityCheckResult) {
      JourneyLogger.info("Nothing to update, EligibilityCheckResult is the same as the existing one in journey.")
      Future.successful(journey)
    } else {
      val updatedJourney: Journey.AfterEligibilityChecked = journey match {

        case j: Epaye.EligibilityChecked =>
          j.copy(eligibilityCheckResult = eligibilityCheckResult)
            .copy(stage = deriveEligibilityEnum(eligibilityCheckResult))
        case j: Vat.EligibilityChecked =>
          j.copy(eligibilityCheckResult = eligibilityCheckResult)
            .copy(stage = deriveEligibilityEnum(eligibilityCheckResult))
        case j: Sa.EligibilityChecked =>
          j.copy(eligibilityCheckResult = eligibilityCheckResult)
            .copy(stage = deriveEligibilityEnum(eligibilityCheckResult))
        case j: Simp.EligibilityChecked =>
          j.copy(eligibilityCheckResult = eligibilityCheckResult)
            .copy(stage = deriveEligibilityEnum(eligibilityCheckResult))

        case j: Epaye.ObtainedWhyCannotPayInFullAnswers =>
          j.into[Journey.Epaye.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Vat.ObtainedWhyCannotPayInFullAnswers =>
          j.into[Journey.Vat.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Sa.ObtainedWhyCannotPayInFullAnswers =>
          j.into[Journey.Sa.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Simp.ObtainedWhyCannotPayInFullAnswers =>
          j.into[Journey.Simp.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform

        case j: Epaye.AnsweredCanPayUpfront =>
          j.into[Journey.Epaye.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Vat.AnsweredCanPayUpfront =>
          j.into[Journey.Vat.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Sa.AnsweredCanPayUpfront =>
          j.into[Journey.Sa.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Simp.AnsweredCanPayUpfront =>
          j.into[Journey.Simp.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform

        case j: Epaye.EnteredUpfrontPaymentAmount =>
          j.into[Journey.Epaye.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Vat.EnteredUpfrontPaymentAmount =>
          j.into[Journey.Vat.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Sa.EnteredUpfrontPaymentAmount =>
          j.into[Journey.Sa.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Simp.EnteredUpfrontPaymentAmount =>
          j.into[Journey.Simp.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform

        case j: Epaye.RetrievedExtremeDates =>
          j.into[Journey.Epaye.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Vat.RetrievedExtremeDates =>
          j.into[Journey.Vat.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Sa.RetrievedExtremeDates =>
          j.into[Journey.Sa.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Simp.RetrievedExtremeDates =>
          j.into[Journey.Simp.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform

        case j: Epaye.RetrievedAffordabilityResult =>
          j.into[Journey.Epaye.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Vat.RetrievedAffordabilityResult =>
          j.into[Journey.Vat.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Sa.RetrievedAffordabilityResult =>
          j.into[Journey.Sa.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Simp.RetrievedAffordabilityResult =>
          j.into[Journey.Simp.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform

        case j: Epaye.ObtainedCanPayWithinSixMonthsAnswers =>
          j.into[Journey.Epaye.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Vat.ObtainedCanPayWithinSixMonthsAnswers =>
          j.into[Journey.Vat.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Sa.ObtainedCanPayWithinSixMonthsAnswers =>
          j.into[Journey.Sa.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Simp.ObtainedCanPayWithinSixMonthsAnswers =>
          j.into[Journey.Simp.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform

        case j: Epaye.StartedPegaCase =>
          j.into[Journey.Epaye.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Vat.StartedPegaCase =>
          j.into[Journey.Vat.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Sa.StartedPegaCase =>
          j.into[Journey.Sa.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Simp.StartedPegaCase =>
          j.into[Journey.Simp.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform

        case j: Epaye.EnteredMonthlyPaymentAmount =>
          j.into[Journey.Epaye.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Vat.EnteredMonthlyPaymentAmount =>
          j.into[Journey.Vat.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Sa.EnteredMonthlyPaymentAmount =>
          j.into[Journey.Sa.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Simp.EnteredMonthlyPaymentAmount =>
          j.into[Journey.Simp.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform

        case j: Epaye.EnteredDayOfMonth =>
          j.into[Journey.Epaye.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Vat.EnteredDayOfMonth =>
          j.into[Journey.Vat.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Sa.EnteredDayOfMonth =>
          j.into[Journey.Sa.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Simp.EnteredDayOfMonth =>
          j.into[Journey.Simp.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform

        case j: Epaye.RetrievedStartDates =>
          j.into[Journey.Epaye.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Vat.RetrievedStartDates =>
          j.into[Journey.Vat.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Sa.RetrievedStartDates =>
          j.into[Journey.Sa.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Simp.RetrievedStartDates =>
          j.into[Journey.Simp.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform

        case j: Epaye.RetrievedAffordableQuotes =>
          j.into[Journey.Epaye.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Vat.RetrievedAffordableQuotes =>
          j.into[Journey.Vat.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Sa.RetrievedAffordableQuotes =>
          j.into[Journey.Sa.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Simp.RetrievedAffordableQuotes =>
          j.into[Journey.Simp.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform

        case j: Epaye.ChosenPaymentPlan =>
          j.into[Journey.Epaye.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Vat.ChosenPaymentPlan =>
          j.into[Journey.Vat.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Sa.ChosenPaymentPlan =>
          j.into[Journey.Sa.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Simp.ChosenPaymentPlan =>
          j.into[Journey.Simp.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform

        case j: Epaye.CheckedPaymentPlan =>
          j.into[Journey.Epaye.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Vat.CheckedPaymentPlan =>
          j.into[Journey.Vat.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Sa.CheckedPaymentPlan =>
          j.into[Journey.Sa.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Simp.CheckedPaymentPlan =>
          j.into[Journey.Simp.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform

        case j: Epaye.EnteredCanYouSetUpDirectDebit =>
          j.into[Journey.Epaye.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Vat.EnteredCanYouSetUpDirectDebit =>
          j.into[Journey.Vat.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Sa.EnteredCanYouSetUpDirectDebit =>
          j.into[Journey.Sa.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Simp.EnteredCanYouSetUpDirectDebit =>
          j.into[Journey.Simp.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform

        case j: Epaye.EnteredDirectDebitDetails =>
          j.into[Journey.Epaye.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Vat.EnteredDirectDebitDetails =>
          j.into[Journey.Vat.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Sa.EnteredDirectDebitDetails =>
          j.into[Journey.Sa.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Simp.EnteredDirectDebitDetails =>
          j.into[Journey.Simp.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform

        case j: Epaye.ConfirmedDirectDebitDetails =>
          j.into[Journey.Epaye.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Vat.ConfirmedDirectDebitDetails =>
          j.into[Journey.Vat.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Sa.ConfirmedDirectDebitDetails =>
          j.into[Journey.Sa.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Simp.ConfirmedDirectDebitDetails =>
          j.into[Journey.Simp.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform

        case j: Epaye.AgreedTermsAndConditions =>
          j.into[Journey.Epaye.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Vat.AgreedTermsAndConditions =>
          j.into[Journey.Vat.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Sa.AgreedTermsAndConditions =>
          j.into[Journey.Sa.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Simp.AgreedTermsAndConditions =>
          j.into[Journey.Simp.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform

        case j: Epaye.SelectedEmailToBeVerified =>
          j.into[Journey.Epaye.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Vat.SelectedEmailToBeVerified =>
          j.into[Journey.Vat.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Sa.SelectedEmailToBeVerified =>
          j.into[Journey.Sa.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Simp.SelectedEmailToBeVerified =>
          j.into[Journey.Simp.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform

        case j: Epaye.EmailVerificationComplete =>
          j.into[Journey.Epaye.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Vat.EmailVerificationComplete =>
          j.into[Journey.Vat.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Sa.EmailVerificationComplete =>
          j.into[Journey.Sa.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform
        case j: Simp.EmailVerificationComplete =>
          j.into[Journey.Simp.EligibilityChecked]
            .withFieldConst(_.stage, deriveEligibilityEnum(eligibilityCheckResult))
            .withFieldConst(_.eligibilityCheckResult, eligibilityCheckResult)
            .transform

        case _: Stages.SubmittedArrangement =>
          Errors.throwBadRequestException("Cannot update Eligibility when journey is in completed state")
      }

      journeyService.upsert(updatedJourney)
    }
  }

  private def deriveEligibilityEnum(eligibilityCheckResult: EligibilityCheckResult): Stage.AfterEligibilityCheck = {
    if (eligibilityCheckResult.isEligible) Stage.AfterEligibilityCheck.Eligible
    else Stage.AfterEligibilityCheck.Ineligible
  }

}
