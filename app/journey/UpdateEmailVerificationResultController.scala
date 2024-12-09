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
import com.google.inject.Inject
import essttp.crypto.CryptoFormat.OperationalCryptoFormat
import paymentsEmailVerification.models.EmailVerificationResult
import essttp.journey.model.Journey.{Epaye, Sa, Simp, Stages, Vat}
import essttp.journey.model.{EmailVerificationAnswers, Journey, JourneyId, Stage}
import essttp.utils.Errors
import io.scalaland.chimney.dsl.TransformationOps
import play.api.mvc.{Action, ControllerComponents, Request}
import services.JourneyService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

class UpdateEmailVerificationResultController @Inject() (
    journeyService: JourneyService,
    cc:             ControllerComponents,
    actions:        Actions
)(implicit exec: ExecutionContext, cryptoFormat: OperationalCryptoFormat) extends BackendController(cc) {

  def updateEmailVerificationResult(journeyId: JourneyId): Action[EmailVerificationResult] =
    actions.authenticatedAction.async(parse.json[EmailVerificationResult]) { implicit request =>
      for {
        journey <- journeyService.get(journeyId)
        newJourney <- journey match {
          case j: Journey.BeforeEmailAddressSelectedToBeVerified =>
            Errors.throwBadRequestExceptionF(s"UpdateEmailVerificationResult is not possible in that state: [${j.stage.toString}]")

          case _: Journey.AfterArrangementSubmitted =>
            Errors.throwBadRequestExceptionF("Cannot update EmailVerificationResult when journey is in completed state.")

          case j: Journey.AfterEmailAddressSelectedToBeVerified =>
            j match {
              case j1: Journey.Stages.SelectedEmailToBeVerified => updateJourneyWithNewValue(j1, request.body)
              case j1: Journey.Stages.EmailVerificationComplete => updateJourneyWithExistingValue(j1, request.body)
            }

        }
      } yield Ok(newJourney.json)
    }

  private def updateJourneyWithNewValue(
      journey: Journey.Stages.SelectedEmailToBeVerified,
      status:  EmailVerificationResult
  )(implicit request: Request[_]): Future[Journey] = {
    val newJourney: Stages.EmailVerificationComplete = journey match {
      case j: Epaye.SelectedEmailToBeVerified =>
        j.into[Epaye.EmailVerificationComplete]
          .withFieldConst(_.stage, determineStage(status))
          .withFieldConst(_.emailVerificationAnswers, EmailVerificationAnswers.EmailVerified(j.emailToBeVerified, status))
          .withFieldConst(_.emailVerificationResult, status)
          .transform
      case j: Vat.SelectedEmailToBeVerified =>
        j.into[Vat.EmailVerificationComplete]
          .withFieldConst(_.stage, determineStage(status))
          .withFieldConst(_.emailVerificationAnswers, EmailVerificationAnswers.EmailVerified(j.emailToBeVerified, status))
          .withFieldConst(_.emailVerificationResult, status)
          .transform
      case j: Sa.SelectedEmailToBeVerified =>
        j.into[Sa.EmailVerificationComplete]
          .withFieldConst(_.stage, determineStage(status))
          .withFieldConst(_.emailVerificationAnswers, EmailVerificationAnswers.EmailVerified(j.emailToBeVerified, status))
          .withFieldConst(_.emailVerificationResult, status)
          .transform
      case j: Simp.SelectedEmailToBeVerified =>
        j.into[Simp.EmailVerificationComplete]
          .withFieldConst(_.stage, determineStage(status))
          .withFieldConst(_.emailVerificationAnswers, EmailVerificationAnswers.EmailVerified(j.emailToBeVerified, status))
          .withFieldConst(_.emailVerificationResult, status)
          .transform
    }
    journeyService.upsert(newJourney)
  }

  private def determineStage(status: EmailVerificationResult): Stage.AfterEmailVerificationPhase = status match {
    case EmailVerificationResult.Verified => Stage.AfterEmailVerificationPhase.VerificationSuccess
    case EmailVerificationResult.Locked   => Stage.AfterEmailVerificationPhase.Locked
  }

  private def updateJourneyWithExistingValue(
      journey: Journey.Stages.EmailVerificationComplete,
      result:  EmailVerificationResult
  )(implicit request: Request[_]): Future[Journey] = {
    if (journey.emailVerificationResult === result) {
      Future.successful(journey)
    } else {
      val newJourney: Stages.EmailVerificationComplete = journey match {
        case j: Epaye.EmailVerificationComplete =>
          j.copy(
            stage                    = determineStage(result),
            emailVerificationResult  = result,
            emailVerificationAnswers = EmailVerificationAnswers.EmailVerified(j.emailToBeVerified, result)
          )
        case j: Vat.EmailVerificationComplete =>
          j.copy(
            stage                    = determineStage(result),
            emailVerificationResult  = result,
            emailVerificationAnswers = EmailVerificationAnswers.EmailVerified(j.emailToBeVerified, result)
          )
        case j: Sa.EmailVerificationComplete =>
          j.copy(
            stage                    = determineStage(result),
            emailVerificationResult  = result,
            emailVerificationAnswers = EmailVerificationAnswers.EmailVerified(j.emailToBeVerified, result)
          )
        case j: Simp.EmailVerificationComplete =>
          j.copy(
            stage                    = determineStage(result),
            emailVerificationResult  = result,
            emailVerificationAnswers = EmailVerificationAnswers.EmailVerified(j.emailToBeVerified, result)
          )
      }

      journeyService.upsert(newJourney)
    }
  }

}
