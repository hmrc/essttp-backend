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
import essttp.crypto.{Crypto, CryptoFormat}
import essttp.journey.model.Journey.Epaye
import essttp.journey.model.{Journey, JourneyId, Stage}
import essttp.rootmodel.Email
import essttp.utils.Errors
import io.scalaland.chimney.dsl.TransformerOps
import play.api.mvc.{Action, ControllerComponents, Request}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateChosenEmailController @Inject() (
    journeyService: JourneyService,
    cc:             ControllerComponents,
    actions:        Actions,
    mongoCrypto:    Crypto
)(implicit exec: ExecutionContext) extends BackendController(cc) {

  implicit val cryptoFormat: CryptoFormat = CryptoFormat.OperationalCryptoFormat(mongoCrypto)

  def updateChosenEmail(journeyId: JourneyId): Action[Email] = actions.authenticatedAction.async(parse.json[Email]) { implicit request =>
    for {
      journey <- journeyService.get(journeyId)
      _ <- journey match {
        case j: Journey.BeforeAgreedTermsAndConditions => Errors.throwBadRequestExceptionF(s"UpdateChosenEmail is not possible in that state: [${j.stage}]")
        case j: Journey.AfterAgreedTermsAndConditions =>
          j match {
            case j1: Journey.Stages.AgreedTermsAndConditions =>
              if (j.isEmailAddressRequired) updateJourneyWithNewValue(j1, request.body)
              else Errors.throwBadRequestExceptionF(s"Cannot update selected email address when isEmailAddressRequired is false for journey.")
            case j1: Journey.Stages.SelectedEmailToBeVerified => updateJourneyWithExistingValue(Left(j1), request.body)
            case _: Journey.Epaye.SubmittedArrangement        => Errors.throwBadRequestExceptionF(s"Cannot update ChosenEmail when journey is in completed state.")
          }
      }
    } yield Ok
  }

  private def updateJourneyWithNewValue(
      journey: Journey.Stages.AgreedTermsAndConditions,
      email:   Email
  )(implicit request: Request[_]): Future[Unit] = {
    val newJourney = journey match {
      case j: Epaye.AgreedTermsAndConditions =>
        j.into[Epaye.SelectedEmailToBeVerified]
          .withFieldConst(_.stage, Stage.AfterSelectedAnEmailToBeVerified.EmailChosen)
          .withFieldConst(_.emailToBeVerified, email)
          .transform
    }
    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingValue(
      journey: Either[Journey.AfterEmailAddressSelectedToBeVerified, Journey.AfterEmailVerification],
      email:   Email
  )(implicit request: Request[_]): Future[Unit] = {
    journey match {
      case Right(_: Journey.Epaye.SubmittedArrangement) => //todo, this will work better when new stage for after email answers added and there's the verified part too, bare with me Andy :)
        Errors.throwBadRequestException("Cannot update Chosen email when journey is in completed state")
      case Left(j: Journey.Epaye.SelectedEmailToBeVerified) =>
        if (j.emailToBeVerified === email) Future.successful(())
        else journeyService.upsert(j.copy(emailToBeVerified = email))
    }
  }

}
