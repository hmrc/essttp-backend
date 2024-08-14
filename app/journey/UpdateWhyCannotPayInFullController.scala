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
import cats.Eq
import cats.syntax.eq._
import com.google.inject.{Inject, Singleton}
import essttp.crypto.CryptoFormat.OperationalCryptoFormat
import essttp.journey.model.Journey.{Epaye, Sa, Stages, Vat}
import essttp.journey.model.{Journey, JourneyId, Stage, WhyCannotPayInFullAnswers}
import essttp.rootmodel.ttp.eligibility.EligibilityCheckResult
import essttp.utils.Errors
import io.scalaland.chimney.dsl.TransformationOps
import play.api.mvc.{Action, ControllerComponents, Request}
import services.JourneyService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateWhyCannotPayInFullController @Inject() (
    actions:        Actions,
    journeyService: JourneyService,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext, cryptoFormat: OperationalCryptoFormat) extends BackendController(cc) {

  implicit val eq: Eq[EligibilityCheckResult] = Eq.fromUniversalEquals

  def updateWhyCannotPayinFull(journeyId: JourneyId): Action[WhyCannotPayInFullAnswers] =
    actions.authenticatedAction.async(parse.json[WhyCannotPayInFullAnswers]) { implicit request =>
      for {
        journey <- journeyService.get(journeyId)
        newJourney <- journey match {
          case _: Journey.BeforeEligibilityChecked =>
            Errors.throwBadRequestExceptionF("WhyCannotPayInFullAnswers update is not possible in that state.")

          case j: Journey.Stages.EligibilityChecked =>
            updateJourneyWithNewValue(j, request.body)

          case j: Journey.AfterWhyCannotPayInFullAnswers => j match {
            case j: Journey.BeforeArrangementSubmitted => updateJourneyWithExistingValue(j, request.body)
            case _: Journey.AfterArrangementSubmitted  => Errors.throwBadRequestExceptionF("Cannot update WhyCannotPayInFullAnswers when journey is in completed state")
          }
        }
      } yield Ok(newJourney.json)
    }

  private def updateJourneyWithNewValue(
      journey:                   Stages.EligibilityChecked,
      whyCannotPayInFullAnswers: WhyCannotPayInFullAnswers
  )(implicit request: Request[_]): Future[Journey] = {
    val newJourney: Journey = journey match {
      case j: Journey.Epaye.EligibilityChecked =>
        j.into[Journey.Epaye.ObtainedWhyCannotPayInFullAnswers]
          .withFieldConst(_.whyCannotPayInFullAnswers, whyCannotPayInFullAnswers)
          .withFieldConst(_.stage, deriveStage(whyCannotPayInFullAnswers))
          .transform
      case j: Journey.Vat.EligibilityChecked =>
        j.into[Journey.Vat.ObtainedWhyCannotPayInFullAnswers]
          .withFieldConst(_.whyCannotPayInFullAnswers, whyCannotPayInFullAnswers)
          .withFieldConst(_.stage, deriveStage(whyCannotPayInFullAnswers))
          .transform

      case j: Journey.Sa.EligibilityChecked =>
        j.into[Journey.Sa.ObtainedWhyCannotPayInFullAnswers]
          .withFieldConst(_.whyCannotPayInFullAnswers, whyCannotPayInFullAnswers)
          .withFieldConst(_.stage, deriveStage(whyCannotPayInFullAnswers))
          .transform
    }
    journeyService.upsert(newJourney)
  }

  // don't need to wipe answers subsequent to the one being updated so can just use .copy and leave the journey in the same stage
  private def updateJourneyWithExistingValue(
      journey:                   Journey.AfterWhyCannotPayInFullAnswers,
      whyCannotPayInFullAnswers: WhyCannotPayInFullAnswers
  )(implicit request: Request[_]): Future[Journey] = {
    if (journey.whyCannotPayInFullAnswers === whyCannotPayInFullAnswers) {
      Future.successful(journey)
    } else {
      val newJourney: Journey = journey match {
        case j: Epaye.ObtainedWhyCannotPayInFullAnswers =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)
            .copy(stage = deriveStage(whyCannotPayInFullAnswers))
        case j: Vat.ObtainedWhyCannotPayInFullAnswers =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)
            .copy(stage = deriveStage(whyCannotPayInFullAnswers))
        case j: Sa.ObtainedWhyCannotPayInFullAnswers =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)
            .copy(stage = deriveStage(whyCannotPayInFullAnswers))

        case j: Epaye.AnsweredCanPayUpfront =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)
        case j: Vat.AnsweredCanPayUpfront =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)
        case j: Sa.AnsweredCanPayUpfront =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)

        case j: Epaye.EnteredUpfrontPaymentAmount =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)
        case j: Vat.EnteredUpfrontPaymentAmount =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)
        case j: Sa.EnteredUpfrontPaymentAmount =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)

        case j: Epaye.RetrievedExtremeDates =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)
        case j: Vat.RetrievedExtremeDates =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)
        case j: Sa.RetrievedExtremeDates =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)

        case j: Epaye.RetrievedAffordabilityResult =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)
        case j: Vat.RetrievedAffordabilityResult =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)
        case j: Sa.RetrievedAffordabilityResult =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)

        case j: Epaye.ObtainedCanPayWithinSixMonthsAnswers =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)
        case j: Vat.ObtainedCanPayWithinSixMonthsAnswers =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)
        case j: Sa.ObtainedCanPayWithinSixMonthsAnswers =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)

        case j: Epaye.StartedPegaCase =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)
        case j: Vat.StartedPegaCase =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)
        case j: Sa.StartedPegaCase =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)

        case j: Epaye.EnteredMonthlyPaymentAmount =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)
        case j: Vat.EnteredMonthlyPaymentAmount =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)
        case j: Sa.EnteredMonthlyPaymentAmount =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)

        case j: Epaye.EnteredDayOfMonth =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)
        case j: Vat.EnteredDayOfMonth =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)
        case j: Sa.EnteredDayOfMonth =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)

        case j: Epaye.RetrievedStartDates =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)
        case j: Vat.RetrievedStartDates =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)
        case j: Sa.RetrievedStartDates =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)

        case j: Epaye.RetrievedAffordableQuotes =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)
        case j: Vat.RetrievedAffordableQuotes =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)
        case j: Sa.RetrievedAffordableQuotes =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)

        case j: Epaye.ChosenPaymentPlan =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)
        case j: Vat.ChosenPaymentPlan =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)
        case j: Sa.ChosenPaymentPlan =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)

        case j: Epaye.CheckedPaymentPlan =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)
        case j: Vat.CheckedPaymentPlan =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)
        case j: Sa.CheckedPaymentPlan =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)

        case j: Epaye.EnteredDetailsAboutBankAccount =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)
        case j: Vat.EnteredDetailsAboutBankAccount =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)
        case j: Sa.EnteredDetailsAboutBankAccount =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)

        case j: Epaye.EnteredDirectDebitDetails =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)
        case j: Vat.EnteredDirectDebitDetails =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)
        case j: Sa.EnteredDirectDebitDetails =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)

        case j: Epaye.ConfirmedDirectDebitDetails =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)
        case j: Vat.ConfirmedDirectDebitDetails =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)
        case j: Sa.ConfirmedDirectDebitDetails =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)

        case j: Epaye.AgreedTermsAndConditions =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)
        case j: Vat.AgreedTermsAndConditions =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)
        case j: Sa.AgreedTermsAndConditions =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)

        case j: Epaye.SelectedEmailToBeVerified =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)
        case j: Vat.SelectedEmailToBeVerified =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)
        case j: Sa.SelectedEmailToBeVerified =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)

        case j: Epaye.EmailVerificationComplete =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)
        case j: Vat.EmailVerificationComplete =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)
        case j: Sa.EmailVerificationComplete =>
          j.copy(whyCannotPayInFullAnswers = whyCannotPayInFullAnswers)

        case _: Stages.SubmittedArrangement =>
          Errors.throwBadRequestException("Cannot update WhyCannotPayInFullAnswers when journey is in completed state")
      }

      journeyService.upsert(newJourney)
    }
  }

  private def deriveStage(whyCannotPayInFullAnswers: WhyCannotPayInFullAnswers): Stage.AfterWhyCannotPayInFullAnswers =
    whyCannotPayInFullAnswers match {
      case WhyCannotPayInFullAnswers.AnswerNotRequired     => Stage.AfterWhyCannotPayInFullAnswers.AnswerNotRequired
      case WhyCannotPayInFullAnswers.WhyCannotPayInFull(_) => Stage.AfterWhyCannotPayInFullAnswers.AnswerRequired
    }

}
