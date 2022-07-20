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

import cats.syntax.eq._
import com.google.inject.{Inject, Singleton}
import essttp.journey.model.{Journey, JourneyId, Stage}
import essttp.rootmodel.bank.DirectDebitDetails
import essttp.utils.Errors
import io.scalaland.chimney.dsl.TransformerOps
import play.api.mvc.{Action, ControllerComponents, Request}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateDirectDebitDetailsController @Inject() (
    journeyService: JourneyService,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext) extends BackendController(cc) {

  def updateDirectDebitDetails(journeyId: JourneyId): Action[DirectDebitDetails] = Action.async(parse.json[DirectDebitDetails]) { implicit request =>
    for {
      journey <- journeyService.get(journeyId)
      _ <- journey match {
        case j: Journey.BeforeChosenTypeOfBankAccount  => Errors.throwBadRequestExceptionF(s"UpdateDirectDebitDetails is not possible in that state: [${j.stage}]")
        case j: Journey.Stages.ChosenTypeOfBankAccount => updateJourneyWithNewValue(j, request.body)
        case j: Journey.AfterEnteredDirectDebitDetails => j match {
          case _: Journey.BeforeArrangementSubmitted => updateJourneyWithExistingValue(j, request.body)
          case _: Journey.AfterArrangementSubmitted  => Errors.throwBadRequestExceptionF("Cannot update DirectDebitDetails when journey is in completed state")
        }
      }
    } yield Ok
  }

  private def updateJourneyWithNewValue(
      journey:            Journey.Stages.ChosenTypeOfBankAccount,
      directDebitDetails: DirectDebitDetails
  )(implicit request: Request[_]): Future[Unit] = {
    val newJourney: Journey.AfterEnteredDirectDebitDetails = journey match {
      case j: Journey.Epaye.ChosenTypeOfBankAccount =>
        j.into[Journey.Epaye.EnteredDirectDebitDetails]
          .withFieldConst(_.stage, determineStage(directDebitDetails))
          .withFieldConst(_.directDebitDetails, directDebitDetails)
          .transform
    }
    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingValue(
      journey:            Journey.AfterEnteredDirectDebitDetails,
      directDebitDetails: DirectDebitDetails
  )(implicit request: Request[_]): Future[Unit] = {
    if (journey.directDebitDetails === directDebitDetails) {
      JourneyLogger.info("Direct debit details haven't changed, nothing to update")
      Future.successful(())
    } else {
      val updatedJourney = journey match {
        case j: Journey.Epaye.EnteredDirectDebitDetails =>
          j.copy(
            directDebitDetails = directDebitDetails,
            stage              = determineStage(directDebitDetails)
          )
        case j: Journey.Epaye.ConfirmedDirectDebitDetails =>
          j.into[Journey.Epaye.EnteredDirectDebitDetails]
            .withFieldConst(_.stage, determineStage(directDebitDetails))
            .transform
        case j: Journey.Epaye.AgreedTermsAndConditions =>
          j.into[Journey.Epaye.EnteredDirectDebitDetails]
            .withFieldConst(_.stage, determineStage(directDebitDetails))
            .transform
        case _: Journey.Epaye.SubmittedArrangement =>
          Errors.throwBadRequestException("Cannot update DirectDebitDetails when journey is in completed state")
      }

      journeyService.upsert(updatedJourney)
    }

  }

  private def determineStage(directDebitDetails: DirectDebitDetails): Stage.AfterEnteredDirectDebitDetails =
    if (directDebitDetails.isAccountHolder) Stage.AfterEnteredDirectDebitDetails.IsAccountHolder
    else Stage.AfterEnteredDirectDebitDetails.IsNotAccountHolder

}
