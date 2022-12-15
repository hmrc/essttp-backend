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
import essttp.journey.model.Journey.{Epaye, Stages, Vat}
import essttp.journey.model.{Journey, JourneyId, Stage}
import essttp.rootmodel.MonthlyPaymentAmount
import essttp.utils.Errors
import io.scalaland.chimney.dsl.TransformerOps
import play.api.mvc.{Action, ControllerComponents, Request}
import services.JourneyService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateMonthlyPaymentAmountController @Inject() (
    actions:        Actions,
    journeyService: JourneyService,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext, cryptoFormat: OperationalCryptoFormat) extends BackendController(cc) {

  def updateMonthlyPaymentAmount(journeyId: JourneyId): Action[MonthlyPaymentAmount] = actions.authenticatedAction.async(parse.json[MonthlyPaymentAmount]) { implicit request =>
    for {
      journey <- journeyService.get(journeyId)
      newJourney <- journey match {
        case j: Journey.BeforeRetrievedAffordabilityResult  => Errors.throwBadRequestExceptionF(s"UpdateMonthlyPaymentAmount update is not possible in that state: [${j.stage.toString}]")
        case j: Journey.Stages.RetrievedAffordabilityResult => updateJourneyWithNewValue(j, request.body)
        case j: Journey.AfterEnteredMonthlyPaymentAmount    => updateJourneyWithExistingValue(j, request.body)
      }
    } yield Ok(newJourney.json)
  }

  private def updateJourneyWithNewValue(
      journey:              Stages.RetrievedAffordabilityResult,
      monthlyPaymentAmount: MonthlyPaymentAmount
  )(implicit request: Request[_]): Future[Journey] = {
    val newJourney: Journey.AfterEnteredMonthlyPaymentAmount = journey match {
      case j: Epaye.RetrievedAffordabilityResult =>
        j.into[Epaye.EnteredMonthlyPaymentAmount]
          .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
          .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
          .transform
      case j: Vat.RetrievedAffordabilityResult =>
        j.into[Vat.EnteredMonthlyPaymentAmount]
          .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
          .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
          .transform
    }
    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingValue(
      journey:              Journey.AfterEnteredMonthlyPaymentAmount,
      monthlyPaymentAmount: MonthlyPaymentAmount
  )(implicit request: Request[_]): Future[Journey] = {
      def upsertIfChanged(updatedJourney: => Journey): Future[Journey] =
        if (journey.monthlyPaymentAmount.value === monthlyPaymentAmount.value) Future.successful(journey)
        else journeyService.upsert(updatedJourney)

    journey match {

      case j: Epaye.EnteredMonthlyPaymentAmount =>
        upsertIfChanged(j.copy(monthlyPaymentAmount = monthlyPaymentAmount))
      case j: Vat.EnteredMonthlyPaymentAmount =>
        upsertIfChanged(j.copy(monthlyPaymentAmount = monthlyPaymentAmount))

      case j: Epaye.EnteredDayOfMonth =>
        upsertIfChanged(
          j.into[Epaye.EnteredMonthlyPaymentAmount]
            .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
            .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
            .transform
        )
      case j: Vat.EnteredDayOfMonth =>
        upsertIfChanged(
          j.into[Vat.EnteredMonthlyPaymentAmount]
            .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
            .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
            .transform
        )

      case j: Epaye.RetrievedStartDates =>
        upsertIfChanged(
          j.into[Epaye.EnteredMonthlyPaymentAmount]
            .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
            .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
            .transform
        )
      case j: Vat.RetrievedStartDates =>
        upsertIfChanged(
          j.into[Vat.EnteredMonthlyPaymentAmount]
            .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
            .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
            .transform
        )

      case j: Epaye.RetrievedAffordableQuotes =>
        upsertIfChanged(
          j.into[Epaye.EnteredMonthlyPaymentAmount]
            .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
            .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
            .transform
        )
      case j: Vat.RetrievedAffordableQuotes =>
        upsertIfChanged(
          j.into[Vat.EnteredMonthlyPaymentAmount]
            .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
            .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
            .transform
        )

      case j: Epaye.ChosenPaymentPlan =>
        upsertIfChanged(
          j.into[Epaye.EnteredMonthlyPaymentAmount]
            .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
            .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
            .transform
        )
      case j: Vat.ChosenPaymentPlan =>
        upsertIfChanged(
          j.into[Vat.EnteredMonthlyPaymentAmount]
            .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
            .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
            .transform
        )

      case j: Epaye.CheckedPaymentPlan =>
        upsertIfChanged(
          j.into[Epaye.EnteredMonthlyPaymentAmount]
            .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
            .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
            .transform
        )
      case j: Vat.CheckedPaymentPlan =>
        upsertIfChanged(
          j.into[Vat.EnteredMonthlyPaymentAmount]
            .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
            .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
            .transform
        )

      case j: Epaye.EnteredDetailsAboutBankAccount =>
        upsertIfChanged(
          j.into[Epaye.EnteredMonthlyPaymentAmount]
            .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
            .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
            .transform
        )
      case j: Vat.EnteredDetailsAboutBankAccount =>
        upsertIfChanged(
          j.into[Vat.EnteredMonthlyPaymentAmount]
            .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
            .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
            .transform
        )

      case j: Epaye.EnteredDirectDebitDetails =>
        upsertIfChanged(
          j.into[Epaye.EnteredMonthlyPaymentAmount]
            .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
            .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
            .transform
        )
      case j: Vat.EnteredDirectDebitDetails =>
        upsertIfChanged(
          j.into[Vat.EnteredMonthlyPaymentAmount]
            .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
            .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
            .transform
        )

      case j: Epaye.ConfirmedDirectDebitDetails =>
        upsertIfChanged(
          j.into[Epaye.EnteredMonthlyPaymentAmount]
            .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
            .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
            .transform
        )
      case j: Vat.ConfirmedDirectDebitDetails =>
        upsertIfChanged(
          j.into[Vat.EnteredMonthlyPaymentAmount]
            .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
            .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
            .transform
        )

      case j: Epaye.AgreedTermsAndConditions =>
        upsertIfChanged(
          j.into[Epaye.EnteredMonthlyPaymentAmount]
            .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
            .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
            .transform
        )
      case j: Vat.AgreedTermsAndConditions =>
        upsertIfChanged(
          j.into[Vat.EnteredMonthlyPaymentAmount]
            .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
            .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
            .transform
        )

      case j: Epaye.SelectedEmailToBeVerified =>
        upsertIfChanged(
          j.into[Epaye.EnteredMonthlyPaymentAmount]
            .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
            .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
            .transform
        )
      case j: Vat.SelectedEmailToBeVerified =>
        upsertIfChanged(
          j.into[Vat.EnteredMonthlyPaymentAmount]
            .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
            .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
            .transform
        )

      case j: Epaye.EmailVerificationComplete =>
        upsertIfChanged(
          j.into[Epaye.EnteredMonthlyPaymentAmount]
            .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
            .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
            .transform
        )
      case j: Vat.EmailVerificationComplete =>
        upsertIfChanged(
          j.into[Vat.EnteredMonthlyPaymentAmount]
            .withFieldConst(_.stage, Stage.AfterMonthlyPaymentAmount.EnteredMonthlyPaymentAmount)
            .withFieldConst(_.monthlyPaymentAmount, monthlyPaymentAmount)
            .transform
        )

      case _: Stages.SubmittedArrangement =>
        Errors.throwBadRequestException("Cannot update MonthlyAmount when journey is in completed state")

    }
  }

}
