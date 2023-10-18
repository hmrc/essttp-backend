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
import com.google.inject.{Inject, Singleton}
import essttp.crypto.CryptoFormat.OperationalCryptoFormat
import essttp.journey.model.Journey.Stages
import essttp.journey.model.{Journey, JourneyId, Stage}
import essttp.rootmodel.IsEmailAddressRequired
import essttp.utils.Errors
import io.scalaland.chimney.dsl.TransformationOps
import play.api.mvc.{Action, ControllerComponents, Request}
import services.JourneyService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateHasAgreedTermsAndConditionsController @Inject() (
    actions:        Actions,
    journeyService: JourneyService,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext, cryptoFormat: OperationalCryptoFormat) extends BackendController(cc) {

  def updateAgreedTermsAndConditions(journeyId: JourneyId): Action[IsEmailAddressRequired] = actions.authenticatedAction.async(parse.json[IsEmailAddressRequired]) { implicit request =>
    for {
      journey <- journeyService.get(journeyId)
      newJourney <- journey match {
        case j: Journey.BeforeConfirmedDirectDebitDetails  => Errors.throwBadRequestExceptionF(s"UpdateAgreedTermsAndConditions is not possible in that state: [${j.stage.toString}]")
        case j: Journey.Stages.ConfirmedDirectDebitDetails => updateJourneyWithNewValue(j, request.body)
        case j: Journey.AfterAgreedTermsAndConditions      => updateJourneyWithExistingValue(j, request.body)
      }
    } yield Ok(newJourney.json)
  }

  private def updateJourneyWithNewValue(
      journey:                Journey.Stages.ConfirmedDirectDebitDetails,
      isEmailAddressRequired: IsEmailAddressRequired
  )(implicit request: Request[_]): Future[Journey] = {
    val newJourney: Journey.AfterAgreedTermsAndConditions = journey match {
      case j: Journey.Epaye.ConfirmedDirectDebitDetails =>
        j.into[Journey.Epaye.AgreedTermsAndConditions]
          .withFieldConst(_.stage, toStage(isEmailAddressRequired))
          .withFieldConst(_.isEmailAddressRequired, isEmailAddressRequired)
          .transform
      case j: Journey.Vat.ConfirmedDirectDebitDetails =>
        j.into[Journey.Vat.AgreedTermsAndConditions]
          .withFieldConst(_.stage, toStage(isEmailAddressRequired))
          .withFieldConst(_.isEmailAddressRequired, isEmailAddressRequired)
          .transform
    }
    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingValue(
      journey:                Journey.AfterAgreedTermsAndConditions,
      isEmailAddressRequired: IsEmailAddressRequired
  )(implicit request: Request[_]): Future[Journey] =
    journey match {
      case _: Stages.SubmittedArrangement =>
        Errors.throwBadRequestException("Cannot update AgreedTermsAndConditions when journey is in completed state")

      case j: Journey.Epaye.AgreedTermsAndConditions =>
        upsertIfChanged(j, isEmailAddressRequired,
                        j.copy(
            isEmailAddressRequired = isEmailAddressRequired,
            stage                  = toStage(isEmailAddressRequired)
          ))
      case j: Journey.Vat.AgreedTermsAndConditions =>
        upsertIfChanged(j, isEmailAddressRequired,
                        j.copy(
            isEmailAddressRequired = isEmailAddressRequired,
            stage                  = toStage(isEmailAddressRequired)
          ))

      case j: Journey.Epaye.SelectedEmailToBeVerified =>
        upsertIfChanged(j, isEmailAddressRequired,
                        j.into[Journey.Epaye.AgreedTermsAndConditions]
            .withFieldConst(_.stage, toStage(isEmailAddressRequired))
            .withFieldConst(_.isEmailAddressRequired, isEmailAddressRequired).transform)
      case j: Journey.Vat.SelectedEmailToBeVerified =>
        upsertIfChanged(j, isEmailAddressRequired,
                        j.into[Journey.Vat.AgreedTermsAndConditions]
            .withFieldConst(_.stage, toStage(isEmailAddressRequired))
            .withFieldConst(_.isEmailAddressRequired, isEmailAddressRequired).transform)

      case j: Journey.Epaye.EmailVerificationComplete =>
        upsertIfChanged(j, isEmailAddressRequired,
                        j.into[Journey.Epaye.AgreedTermsAndConditions]
            .withFieldConst(_.stage, toStage(isEmailAddressRequired))
            .withFieldConst(_.isEmailAddressRequired, isEmailAddressRequired).transform)

      case j: Journey.Vat.EmailVerificationComplete =>
        upsertIfChanged(j, isEmailAddressRequired,
                        j.into[Journey.Vat.AgreedTermsAndConditions]
            .withFieldConst(_.stage, toStage(isEmailAddressRequired))
            .withFieldConst(_.isEmailAddressRequired, isEmailAddressRequired).transform)
    }

  private def upsertIfChanged(
      j:                      Journey.AfterAgreedTermsAndConditions,
      isEmailAddressRequired: IsEmailAddressRequired,
      updatedJourney:         => Journey.AfterAgreedTermsAndConditions
  )(
      implicit
      r: Request[_]
  ): Future[Journey] =
    if (j.isEmailAddressRequired === isEmailAddressRequired) Future.successful(j)
    else journeyService.upsert(updatedJourney)

  private def toStage(isEmailAddressRequired: IsEmailAddressRequired): Stage.AfterAgreedTermsAndConditions =
    if (isEmailAddressRequired) Stage.AfterAgreedTermsAndConditions.EmailAddressRequired
    else Stage.AfterAgreedTermsAndConditions.EmailAddressNotRequired
}
