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
import com.google.inject.{Inject, Singleton}
import essttp.crypto.CryptoFormat.OperationalCryptoFormat
import essttp.journey.model.{Journey, JourneyId, JourneyStage, UpfrontPaymentAnswers}
import essttp.rootmodel.{CanPayUpfront, UpfrontPaymentAmount}
import essttp.utils.Errors
import io.scalaland.chimney.dsl.*
import play.api.mvc.{Action, ControllerComponents, Request}
import services.JourneyService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateUpfrontPaymentAmountController @Inject() (
  actions:        Actions,
  journeyService: JourneyService,
  cc:             ControllerComponents
)(using ExecutionContext, OperationalCryptoFormat)
    extends BackendController(cc) {

  def updateUpfrontPaymentAmount(journeyId: JourneyId): Action[UpfrontPaymentAmount] =
    actions.authenticatedAction.async(parse.json[UpfrontPaymentAmount]) { implicit request =>
      for {
        journey    <- journeyService.get(journeyId)
        newJourney <- journey match {
                        case j: JourneyStage.BeforeAnsweredCanPayUpfront      =>
                          Errors.throwBadRequestExceptionF(
                            s"UpdateUpfrontPaymentAmount update is not possible in that state: [${j.stage}]"
                          )
                        case j: Journey.AnsweredCanPayUpfront                 => updateJourneyWithNewValue(j, request.body)
                        case j: JourneyStage.AfterEnteredUpfrontPaymentAmount =>
                          updateJourneyWithExistingValue(Left(j), request.body)
                        case j: JourneyStage.AfterUpfrontPaymentAnswers       =>
                          j.upfrontPaymentAnswers match {
                            case _: UpfrontPaymentAnswers.DeclaredUpfrontPayment =>
                              updateJourneyWithExistingValue(Right(j), request.body)
                            case UpfrontPaymentAnswers.NoUpfrontPayment          =>
                              Errors.throwBadRequestExceptionF(
                                "UpdateUpfrontPaymentAmount update is not possible when an upfront payment has not been chosen"
                              )
                          }
                      }
      } yield Ok(newJourney.json)
    }

  private def updateJourneyWithNewValue(
    journey:              Journey.AnsweredCanPayUpfront,
    upfrontPaymentAmount: UpfrontPaymentAmount
  )(using Request[?]): Future[Journey] =
    if (journey.canPayUpfront.value) {
      val updatedJourney: Journey =
        journey
          .into[Journey.EnteredUpfrontPaymentAmount]
          .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
          .transform

      journeyService.upsert(updatedJourney)
    } else {
      Errors.throwBadRequestExceptionF(
        s"UpdateUpfrontPaymentAmount update is not possible when user has selected [No] for CanPayUpfront: [${journey.stage}]"
      )
    }

  private def updateJourneyWithExistingValue(
    journey:              Either[
      JourneyStage.AfterEnteredUpfrontPaymentAmount & Journey,
      JourneyStage.AfterUpfrontPaymentAnswers & Journey
    ],
    upfrontPaymentAmount: UpfrontPaymentAmount
  )(using Request[?]): Future[Journey] = {

    journey match {
      case Left(j) =>
        if (j.upfrontPaymentAmount.value == upfrontPaymentAmount.value) {
          JourneyLogger.info("Nothing to update, UpfrontPaymentAmount is the same as the existing one in journey.")
          Future.successful(j)
        } else {
          val updatedJourney: Journey = j match {
            case j1: Journey.EnteredUpfrontPaymentAmount => j1.copy(upfrontPaymentAmount = upfrontPaymentAmount)
          }
          journeyService.upsert(updatedJourney)
        }

      case Right(j) =>
        withUpfrontPaymentAmount(j, j.upfrontPaymentAnswers) { existingAmount =>
          if (existingAmount.value == upfrontPaymentAmount.value) {
            JourneyLogger.info("Nothing to update, UpfrontPaymentAmount is the same as the existing one in journey.")
            Future.successful(j)
          } else {
            val updatedJourney: Journey = j match {

              case j: Journey.EnteredMonthlyPaymentAmount =>
                j.into[Journey.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(value = true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform

              case j: Journey.RetrievedExtremeDates =>
                j.into[Journey.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(value = true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform

              case j: Journey.RetrievedAffordabilityResult =>
                j.into[Journey.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(value = true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform

              case j: Journey.ObtainedCanPayWithinSixMonthsAnswers =>
                j.into[Journey.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(value = true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform

              case j: Journey.StartedPegaCase =>
                j.into[Journey.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(value = true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform

              case j: Journey.EnteredDayOfMonth =>
                j.into[Journey.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(value = true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform

              case j: Journey.RetrievedStartDates =>
                j.into[Journey.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(value = true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform

              case j: Journey.RetrievedAffordableQuotes =>
                j.into[Journey.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(value = true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform

              case j: Journey.ChosenPaymentPlan =>
                j.into[Journey.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(value = true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform

              case j: Journey.CheckedPaymentPlan =>
                j.into[Journey.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(value = true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform

              case j: Journey.EnteredCanYouSetUpDirectDebit =>
                j.into[Journey.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(value = true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform

              case j: Journey.EnteredDirectDebitDetails =>
                j.into[Journey.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(value = true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform

              case j: Journey.ConfirmedDirectDebitDetails =>
                j.into[Journey.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(value = true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform

              case j: Journey.AgreedTermsAndConditions =>
                j.into[Journey.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(value = true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform

              case j: Journey.SelectedEmailToBeVerified =>
                j.into[Journey.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(value = true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform

              case j: Journey.EmailVerificationComplete =>
                j.into[Journey.EnteredUpfrontPaymentAmount]
                  .withFieldConst(_.canPayUpfront, CanPayUpfront(value = true))
                  .withFieldConst(_.upfrontPaymentAmount, upfrontPaymentAmount)
                  .transform

              case _: Journey.SubmittedArrangement =>
                Errors.throwBadRequestException("Cannot update UpfrontPaymentAmount when journey is in completed state")
            }

            journeyService.upsert(updatedJourney)
          }
        }
    }
  }

  private def withUpfrontPaymentAmount[A](j: Journey, upfrontPaymentAnswers: UpfrontPaymentAnswers)(
    f: UpfrontPaymentAmount => Future[A]
  ): Future[A] =
    upfrontPaymentAnswers match {
      case UpfrontPaymentAnswers.NoUpfrontPayment =>
        Errors.throwBadRequestExceptionF(
          s"UpdateUpfrontPaymentAmount update is not possible there is no upfront payment amount before...: [${j.stage}]"
        )

      case UpfrontPaymentAnswers.DeclaredUpfrontPayment(amount) =>
        f(amount)
    }
}
