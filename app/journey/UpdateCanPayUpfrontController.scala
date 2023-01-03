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
import cats.instances.boolean._
import cats.syntax.eq._
import com.google.inject.{Inject, Singleton}
import essttp.crypto.CryptoFormat.OperationalCryptoFormat
import essttp.journey.model.Journey.Stages
import essttp.journey.model.{Journey, JourneyId, Stage, UpfrontPaymentAnswers}
import essttp.rootmodel.CanPayUpfront
import essttp.utils.Errors
import io.scalaland.chimney.dsl.TransformerOps
import play.api.mvc._
import services.JourneyService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateCanPayUpfrontController @Inject() (
    actions:        Actions,
    journeyService: JourneyService,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext, cryptoFormat: OperationalCryptoFormat) extends BackendController(cc) {

  def updateCanPayUpfront(journeyId: JourneyId): Action[CanPayUpfront] = actions.authenticatedAction.async(parse.json[CanPayUpfront]) { implicit request =>
    for {
      journey <- journeyService.get(journeyId)
      newJourney <- journey match {
        case _: Journey.BeforeEligibilityChecked   => Errors.throwBadRequestExceptionF("UpdateCanPayUpfront is not possible in that state.")
        case j: Journey.Stages.EligibilityChecked  => updateJourneyWithNewValue(j, request.body)
        case j: Journey.AfterAnsweredCanPayUpfront => updateJourneyWithExistingValue(Left(j), request.body)
        case j: Journey.AfterUpfrontPaymentAnswers => updateJourneyWithExistingValue(Right(j), request.body)
      }
    } yield Ok(newJourney.json)
  }

  private def updateJourneyWithNewValue(
      journey:       Stages.EligibilityChecked,
      canPayUpfront: CanPayUpfront
  )(implicit request: Request[_]): Future[Journey] = {
    val updatedJourney: Stages.AnsweredCanPayUpfront = journey match {
      case j: Journey.Epaye.EligibilityChecked =>
        j.into[Journey.Epaye.AnsweredCanPayUpfront]
          .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
          .withFieldConst(_.canPayUpfront, canPayUpfront)
          .transform
      case j: Journey.Vat.EligibilityChecked =>
        j.into[Journey.Vat.AnsweredCanPayUpfront]
          .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
          .withFieldConst(_.canPayUpfront, canPayUpfront)
          .transform
    }
    journeyService.upsert(updatedJourney)
  }

  private def updateJourneyWithExistingValue(
      journey:       Either[Journey.AfterAnsweredCanPayUpfront, Journey.AfterUpfrontPaymentAnswers],
      canPayUpfront: CanPayUpfront
  )(implicit request: Request[_]): Future[Journey] = {
    journey match {
      case Left(j: Journey.AfterAnsweredCanPayUpfront) =>
        if (j.canPayUpfront.value === canPayUpfront.value) {
          JourneyLogger.info("Nothing to update, CanPayUpfront is the same as the existing one in journey.")
          Future.successful(j)
        } else {
          val updatedJourney: Journey = j match {
            case j1: Journey.Epaye.AnsweredCanPayUpfront =>
              j1.copy(
                stage         = determineCanPayUpFrontEnum(canPayUpfront),
                canPayUpfront = canPayUpfront
              )
            case j1: Journey.Vat.AnsweredCanPayUpfront =>
              j1.copy(
                stage         = determineCanPayUpFrontEnum(canPayUpfront),
                canPayUpfront = canPayUpfront
              )
            case j1: Journey.Epaye.EnteredUpfrontPaymentAmount =>
              j1.into[Journey.Epaye.AnsweredCanPayUpfront]
                .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            case j1: Journey.Vat.EnteredUpfrontPaymentAmount =>
              j1.into[Journey.Vat.AnsweredCanPayUpfront]
                .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
          }
          journeyService.upsert(updatedJourney)
        }
      case Right(j: Journey.AfterUpfrontPaymentAnswers) =>
        val existingCanPayUpfront = j.upfrontPaymentAnswers match {
          case UpfrontPaymentAnswers.NoUpfrontPayment          => CanPayUpfront(false)
          case _: UpfrontPaymentAnswers.DeclaredUpfrontPayment => CanPayUpfront(true)
        }
          def upsertIfChanged(updatedJourney: => Journey): Future[Journey] =
            if (canPayUpfront.value === existingCanPayUpfront.value) {
              JourneyLogger.info("Nothing to update, CanPayUpfront is the same as the existing one in journey.")
              Future.successful(j)
            } else journeyService.upsert(updatedJourney)

        j match {
          case j1: Journey.Epaye.EnteredMonthlyPaymentAmount =>
            upsertIfChanged(
              j1.into[Journey.Epaye.AnsweredCanPayUpfront]
                .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )
          case j1: Journey.Vat.EnteredMonthlyPaymentAmount =>
            upsertIfChanged(
              j1.into[Journey.Vat.AnsweredCanPayUpfront]
                .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )

          case j1: Journey.Epaye.RetrievedExtremeDates =>
            upsertIfChanged(
              j1.into[Journey.Epaye.AnsweredCanPayUpfront]
                .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )
          case j1: Journey.Vat.RetrievedExtremeDates =>
            upsertIfChanged(
              j1.into[Journey.Vat.AnsweredCanPayUpfront]
                .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )

          case j1: Journey.Epaye.RetrievedAffordabilityResult =>
            upsertIfChanged(
              j1.into[Journey.Epaye.AnsweredCanPayUpfront]
                .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )
          case j1: Journey.Vat.RetrievedAffordabilityResult =>
            upsertIfChanged(
              j1.into[Journey.Vat.AnsweredCanPayUpfront]
                .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )

          case j1: Journey.Epaye.EnteredDayOfMonth =>
            upsertIfChanged(
              j1.into[Journey.Epaye.AnsweredCanPayUpfront]
                .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )
          case j1: Journey.Vat.EnteredDayOfMonth =>
            upsertIfChanged(
              j1.into[Journey.Vat.AnsweredCanPayUpfront]
                .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )

          case j1: Journey.Epaye.RetrievedStartDates =>
            upsertIfChanged(
              j1.into[Journey.Epaye.AnsweredCanPayUpfront]
                .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )
          case j1: Journey.Vat.RetrievedStartDates =>
            upsertIfChanged(
              j1.into[Journey.Vat.AnsweredCanPayUpfront]
                .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )

          case j1: Journey.Epaye.RetrievedAffordableQuotes =>
            upsertIfChanged(
              j1.into[Journey.Epaye.AnsweredCanPayUpfront]
                .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )
          case j1: Journey.Vat.RetrievedAffordableQuotes =>
            upsertIfChanged(
              j1.into[Journey.Vat.AnsweredCanPayUpfront]
                .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )

          case j1: Journey.Epaye.ChosenPaymentPlan =>
            upsertIfChanged(
              j1.into[Journey.Epaye.AnsweredCanPayUpfront]
                .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )
          case j1: Journey.Vat.ChosenPaymentPlan =>
            upsertIfChanged(
              j1.into[Journey.Vat.AnsweredCanPayUpfront]
                .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )

          case j1: Journey.Epaye.CheckedPaymentPlan =>
            upsertIfChanged(
              j1.into[Journey.Epaye.AnsweredCanPayUpfront]
                .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )
          case j1: Journey.Vat.CheckedPaymentPlan =>
            upsertIfChanged(
              j1.into[Journey.Vat.AnsweredCanPayUpfront]
                .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )

          case j1: Journey.Epaye.EnteredDetailsAboutBankAccount =>
            upsertIfChanged(
              j1.into[Journey.Epaye.AnsweredCanPayUpfront]
                .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )
          case j1: Journey.Vat.EnteredDetailsAboutBankAccount =>
            upsertIfChanged(
              j1.into[Journey.Vat.AnsweredCanPayUpfront]
                .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )

          case j1: Journey.Epaye.EnteredDirectDebitDetails =>
            upsertIfChanged(
              j1.into[Journey.Epaye.AnsweredCanPayUpfront]
                .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )
          case j1: Journey.Vat.EnteredDirectDebitDetails =>
            upsertIfChanged(
              j1.into[Journey.Vat.AnsweredCanPayUpfront]
                .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )

          case j1: Journey.Epaye.ConfirmedDirectDebitDetails =>
            upsertIfChanged(
              j1.into[Journey.Epaye.AnsweredCanPayUpfront]
                .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )
          case j1: Journey.Vat.ConfirmedDirectDebitDetails =>
            upsertIfChanged(
              j1.into[Journey.Vat.AnsweredCanPayUpfront]
                .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )

          case j1: Journey.Epaye.AgreedTermsAndConditions =>
            upsertIfChanged(
              j1.into[Journey.Epaye.AnsweredCanPayUpfront]
                .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )
          case j1: Journey.Vat.AgreedTermsAndConditions =>
            upsertIfChanged(
              j1.into[Journey.Vat.AnsweredCanPayUpfront]
                .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )

          case j1: Journey.Epaye.SelectedEmailToBeVerified =>
            upsertIfChanged(
              j1.into[Journey.Epaye.AnsweredCanPayUpfront]
                .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )
          case j1: Journey.Vat.SelectedEmailToBeVerified =>
            upsertIfChanged(
              j1.into[Journey.Vat.AnsweredCanPayUpfront]
                .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )

          case j1: Journey.Epaye.EmailVerificationComplete =>
            upsertIfChanged(
              j1.into[Journey.Epaye.AnsweredCanPayUpfront]
                .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )
          case j1: Journey.Vat.EmailVerificationComplete =>
            upsertIfChanged(
              j1.into[Journey.Vat.AnsweredCanPayUpfront]
                .withFieldConst(_.stage, determineCanPayUpFrontEnum(canPayUpfront))
                .withFieldConst(_.canPayUpfront, canPayUpfront)
                .transform
            )

          case _: Journey.Stages.SubmittedArrangement =>
            Errors.throwBadRequestException("Cannot update AnsweredCanPayUpFront when journey is in completed state")
        }
    }
  }

  private def determineCanPayUpFrontEnum(latestCanPayUpfrontValue: CanPayUpfront): Stage.AfterCanPayUpfront = {
    if (latestCanPayUpfrontValue.value) Stage.AfterCanPayUpfront.Yes
    else Stage.AfterCanPayUpfront.No
  }

}
