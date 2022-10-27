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
import com.google.inject.Inject
import essttp.crypto.{Crypto, CryptoFormat}
import essttp.emailverification.EmailVerificationStatus
import essttp.journey.model.Journey.Epaye
import essttp.journey.model.{EmailVerificationAnswers, Journey, JourneyId, Stage}
import essttp.utils.Errors
import io.scalaland.chimney.dsl.TransformerOps
import play.api.mvc.{Action, ControllerComponents, Request}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

class UpdateEmailVerificationStatusController @Inject() (
    journeyService: JourneyService,
    cc:             ControllerComponents,
    actions:        Actions,
    mongoCrypto:    Crypto
)(implicit exec: ExecutionContext) extends BackendController(cc) {

  implicit val cryptoFormat: CryptoFormat = CryptoFormat.OperationalCryptoFormat(mongoCrypto)

  def updateEmailVerificationStatus(journeyId: JourneyId): Action[EmailVerificationStatus] =
    actions.authenticatedAction.async(parse.json[EmailVerificationStatus]) { implicit request =>
      for {
        journey <- journeyService.get(journeyId)
        _ <- journey match {
          case j: Journey.BeforeEmailAddressSelectedToBeVerified =>
            Errors.throwBadRequestExceptionF(s"UpdateEmailVerificationStatus is not possible in that state: [${j.stage}]")

          case _: Journey.AfterArrangementSubmitted =>
            Errors.throwBadRequestExceptionF("Cannot update EmailVerificationStatus when journey is in completed state.")

          case j: Journey.AfterEmailAddressSelectedToBeVerified =>
            j match {
              case j1: Journey.Stages.SelectedEmailToBeVerified => updateJourneyWithNewValue(j1, request.body)
              case j1: Journey.Stages.EmailVerificationComplete => updateJourneyWithExistingValue(j1, request.body)
            }

        }
      } yield Ok
    }

  private def updateJourneyWithNewValue(
      journey: Journey.Stages.SelectedEmailToBeVerified,
      status:  EmailVerificationStatus
  )(implicit request: Request[_]): Future[Unit] = {
    val newJourney = journey match {
      case j: Epaye.SelectedEmailToBeVerified =>
        j.into[Epaye.EmailVerificationComplete]
          .withFieldConst(_.stage, determineStage(status))
          .withFieldConst(_.emailVerificationAnswers, EmailVerificationAnswers.EmailVerified(j.emailToBeVerified, status))
          .withFieldConst(_.emailVerificationStatus, status)
          .transform
    }
    journeyService.upsert(newJourney)
  }

  private def determineStage(status: EmailVerificationStatus): Stage.AfterEmailVerificationPhase = status match {
    case EmailVerificationStatus.Verified => Stage.AfterEmailVerificationPhase.VerificationSuccess
    case EmailVerificationStatus.Locked   => Stage.AfterEmailVerificationPhase.Locked
  }

  private def updateJourneyWithExistingValue(
      journey: Journey.Stages.EmailVerificationComplete,
      status:  EmailVerificationStatus
  )(implicit request: Request[_]): Future[Unit] = {
    if (journey.emailVerificationStatus === status) {
      Future.successful(())
    } else {
      val newJourney = journey match {
        case j: Epaye.EmailVerificationComplete =>
          j.copy(
            stage                    = determineStage(status),
            emailVerificationStatus  = status,
            emailVerificationAnswers = EmailVerificationAnswers.EmailVerified(j.emailToBeVerified, status)
          )
      }

      journeyService.upsert(newJourney)
    }
  }

}
