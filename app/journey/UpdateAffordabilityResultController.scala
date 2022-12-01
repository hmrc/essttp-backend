/*
 * Copyright 2022 HM Revenue & Customs
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
import essttp.journey.model.{Journey, JourneyId, Stage}
import essttp.rootmodel.ttp.affordability.InstalmentAmounts
import essttp.utils.Errors
import io.scalaland.chimney.dsl.TransformerOps
import play.api.mvc.{Action, ControllerComponents, Request}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateAffordabilityResultController @Inject() (
    actions:        Actions,
    journeyService: JourneyService,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext, cryptoFormat: OperationalCryptoFormat) extends BackendController(cc) {
  def updateAffordabilityResult(journeyId: JourneyId): Action[InstalmentAmounts] = actions.authenticatedAction.async(parse.json[InstalmentAmounts]) { implicit request =>
    for {
      journey <- journeyService.get(journeyId)
      newJourney <- journey match {
        case j: Journey.BeforeExtremeDatesResponse   => Errors.throwBadRequestExceptionF(s"UpdateAffordabilityResult update is not possible in that state: [${j.stage.toString}]")
        case j: Journey.Stages.RetrievedExtremeDates => updateJourneyWithNewValue(j, request.body)
        case j: Journey.AfterRetrievedAffordabilityResult => j match {
          case _: Journey.BeforeArrangementSubmitted => updateJourneyWithExistingValue(j, request.body)
          case _: Journey.AfterArrangementSubmitted  => Errors.throwBadRequestExceptionF("Cannot update AffordabilityResult when journey is in completed state")
        }
      }
    } yield Ok(newJourney.json)
  }

  private def updateJourneyWithNewValue(
      journey:           Journey.Stages.RetrievedExtremeDates,
      instalmentAmounts: InstalmentAmounts
  )(implicit request: Request[_]): Future[Journey] = {
    val newJourney: Stages.RetrievedAffordabilityResult = journey match {
      case j: Journey.Epaye.RetrievedExtremeDates =>
        j.into[Journey.Epaye.RetrievedAffordabilityResult]
          .withFieldConst(_.stage, Stage.AfterAffordabilityResult.RetrievedAffordabilityResult)
          .withFieldConst(_.instalmentAmounts, instalmentAmounts)
          .transform
      case j: Journey.Vat.RetrievedExtremeDates =>
        j.into[Journey.Vat.RetrievedAffordabilityResult]
          .withFieldConst(_.stage, Stage.AfterAffordabilityResult.RetrievedAffordabilityResult)
          .withFieldConst(_.instalmentAmounts, instalmentAmounts)
          .transform
    }
    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingValue(
      journey:           Journey.AfterRetrievedAffordabilityResult,
      instalmentAmounts: InstalmentAmounts
  )(implicit request: Request[_]): Future[Journey] = {
    if (journey.instalmentAmounts === instalmentAmounts) {
      JourneyLogger.info("Nothing to update, InstalmentAmounts is the same as the existing one in journey.")
      Future.successful(journey)
    } else {
      val newJourney: Journey.AfterRetrievedAffordabilityResult = journey match {

        case j: Journey.Epaye.RetrievedAffordabilityResult => j.copy(instalmentAmounts = instalmentAmounts)
        case j: Journey.Vat.RetrievedAffordabilityResult   => j.copy(instalmentAmounts = instalmentAmounts)

        case j: Journey.Epaye.EnteredMonthlyPaymentAmount =>
          j.into[Journey.Epaye.RetrievedAffordabilityResult]
            .withFieldConst(_.stage, Stage.AfterAffordabilityResult.RetrievedAffordabilityResult)
            .withFieldConst(_.instalmentAmounts, instalmentAmounts)
            .transform
        case j: Journey.Vat.EnteredMonthlyPaymentAmount =>
          j.into[Journey.Vat.RetrievedAffordabilityResult]
            .withFieldConst(_.stage, Stage.AfterAffordabilityResult.RetrievedAffordabilityResult)
            .withFieldConst(_.instalmentAmounts, instalmentAmounts)
            .transform

        case j: Journey.Epaye.EnteredDayOfMonth =>
          j.into[Journey.Epaye.RetrievedAffordabilityResult]
            .withFieldConst(_.stage, Stage.AfterAffordabilityResult.RetrievedAffordabilityResult)
            .withFieldConst(_.instalmentAmounts, instalmentAmounts)
            .transform
        case j: Journey.Vat.EnteredDayOfMonth =>
          j.into[Journey.Vat.RetrievedAffordabilityResult]
            .withFieldConst(_.stage, Stage.AfterAffordabilityResult.RetrievedAffordabilityResult)
            .withFieldConst(_.instalmentAmounts, instalmentAmounts)
            .transform

        case j: Journey.Epaye.RetrievedStartDates =>
          j.into[Journey.Epaye.RetrievedAffordabilityResult]
            .withFieldConst(_.stage, Stage.AfterAffordabilityResult.RetrievedAffordabilityResult)
            .withFieldConst(_.instalmentAmounts, instalmentAmounts)
            .transform
        case j: Journey.Vat.RetrievedStartDates =>
          j.into[Journey.Vat.RetrievedAffordabilityResult]
            .withFieldConst(_.stage, Stage.AfterAffordabilityResult.RetrievedAffordabilityResult)
            .withFieldConst(_.instalmentAmounts, instalmentAmounts)
            .transform

        case j: Journey.Epaye.RetrievedAffordableQuotes =>
          j.into[Journey.Epaye.RetrievedAffordabilityResult]
            .withFieldConst(_.stage, Stage.AfterAffordabilityResult.RetrievedAffordabilityResult)
            .withFieldConst(_.instalmentAmounts, instalmentAmounts)
            .transform
        case j: Journey.Vat.RetrievedAffordableQuotes =>
          j.into[Journey.Vat.RetrievedAffordabilityResult]
            .withFieldConst(_.stage, Stage.AfterAffordabilityResult.RetrievedAffordabilityResult)
            .withFieldConst(_.instalmentAmounts, instalmentAmounts)
            .transform

        case j: Journey.Epaye.ChosenPaymentPlan =>
          j.into[Journey.Epaye.RetrievedAffordabilityResult]
            .withFieldConst(_.stage, Stage.AfterAffordabilityResult.RetrievedAffordabilityResult)
            .withFieldConst(_.instalmentAmounts, instalmentAmounts)
            .transform
        case j: Journey.Vat.ChosenPaymentPlan =>
          j.into[Journey.Vat.RetrievedAffordabilityResult]
            .withFieldConst(_.stage, Stage.AfterAffordabilityResult.RetrievedAffordabilityResult)
            .withFieldConst(_.instalmentAmounts, instalmentAmounts)
            .transform

        case j: Journey.Epaye.CheckedPaymentPlan =>
          j.into[Journey.Epaye.RetrievedAffordabilityResult]
            .withFieldConst(_.stage, Stage.AfterAffordabilityResult.RetrievedAffordabilityResult)
            .withFieldConst(_.instalmentAmounts, instalmentAmounts)
            .transform
        case j: Journey.Vat.CheckedPaymentPlan =>
          j.into[Journey.Vat.RetrievedAffordabilityResult]
            .withFieldConst(_.stage, Stage.AfterAffordabilityResult.RetrievedAffordabilityResult)
            .withFieldConst(_.instalmentAmounts, instalmentAmounts)
            .transform

        case j: Journey.Epaye.EnteredDetailsAboutBankAccount =>
          j.into[Journey.Epaye.RetrievedAffordabilityResult]
            .withFieldConst(_.stage, Stage.AfterAffordabilityResult.RetrievedAffordabilityResult)
            .withFieldConst(_.instalmentAmounts, instalmentAmounts)
            .transform
        case j: Journey.Vat.EnteredDetailsAboutBankAccount =>
          j.into[Journey.Vat.RetrievedAffordabilityResult]
            .withFieldConst(_.stage, Stage.AfterAffordabilityResult.RetrievedAffordabilityResult)
            .withFieldConst(_.instalmentAmounts, instalmentAmounts)
            .transform

        case j: Journey.Epaye.EnteredDirectDebitDetails =>
          j.into[Journey.Epaye.RetrievedAffordabilityResult]
            .withFieldConst(_.stage, Stage.AfterAffordabilityResult.RetrievedAffordabilityResult)
            .withFieldConst(_.instalmentAmounts, instalmentAmounts)
            .transform
        case j: Journey.Vat.EnteredDirectDebitDetails =>
          j.into[Journey.Vat.RetrievedAffordabilityResult]
            .withFieldConst(_.stage, Stage.AfterAffordabilityResult.RetrievedAffordabilityResult)
            .withFieldConst(_.instalmentAmounts, instalmentAmounts)
            .transform

        case j: Journey.Epaye.ConfirmedDirectDebitDetails =>
          j.into[Journey.Epaye.RetrievedAffordabilityResult]
            .withFieldConst(_.stage, Stage.AfterAffordabilityResult.RetrievedAffordabilityResult)
            .withFieldConst(_.instalmentAmounts, instalmentAmounts)
            .transform
        case j: Journey.Vat.ConfirmedDirectDebitDetails =>
          j.into[Journey.Vat.RetrievedAffordabilityResult]
            .withFieldConst(_.stage, Stage.AfterAffordabilityResult.RetrievedAffordabilityResult)
            .withFieldConst(_.instalmentAmounts, instalmentAmounts)
            .transform

        case j: Journey.Epaye.AgreedTermsAndConditions =>
          j.into[Journey.Epaye.RetrievedAffordabilityResult]
            .withFieldConst(_.stage, Stage.AfterAffordabilityResult.RetrievedAffordabilityResult)
            .withFieldConst(_.instalmentAmounts, instalmentAmounts)
            .transform
        case j: Journey.Vat.AgreedTermsAndConditions =>
          j.into[Journey.Vat.RetrievedAffordabilityResult]
            .withFieldConst(_.stage, Stage.AfterAffordabilityResult.RetrievedAffordabilityResult)
            .withFieldConst(_.instalmentAmounts, instalmentAmounts)
            .transform

        case j: Journey.Epaye.SelectedEmailToBeVerified =>
          j.into[Journey.Epaye.RetrievedAffordabilityResult]
            .withFieldConst(_.stage, Stage.AfterAffordabilityResult.RetrievedAffordabilityResult)
            .withFieldConst(_.instalmentAmounts, instalmentAmounts)
            .transform
        case j: Journey.Vat.SelectedEmailToBeVerified =>
          j.into[Journey.Vat.RetrievedAffordabilityResult]
            .withFieldConst(_.stage, Stage.AfterAffordabilityResult.RetrievedAffordabilityResult)
            .withFieldConst(_.instalmentAmounts, instalmentAmounts)
            .transform

        case j: Journey.Epaye.EmailVerificationComplete =>
          j.into[Journey.Epaye.RetrievedAffordabilityResult]
            .withFieldConst(_.stage, Stage.AfterAffordabilityResult.RetrievedAffordabilityResult)
            .withFieldConst(_.instalmentAmounts, instalmentAmounts)
            .transform
        case j: Journey.Vat.EmailVerificationComplete =>
          j.into[Journey.Vat.RetrievedAffordabilityResult]
            .withFieldConst(_.stage, Stage.AfterAffordabilityResult.RetrievedAffordabilityResult)
            .withFieldConst(_.instalmentAmounts, instalmentAmounts)
            .transform

        case _: Journey.Stages.SubmittedArrangement =>
          Errors.throwBadRequestException("Cannot update AffordabilityResult when journey is in completed state")
      }
      journeyService.upsert(newJourney)
    }

  }
}
