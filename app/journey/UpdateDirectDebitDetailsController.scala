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
import essttp.journey.model.{Journey, JourneyId, JourneyStage}
import essttp.rootmodel.bank.BankDetails
import essttp.utils.Errors
import io.scalaland.chimney.dsl.*
import play.api.mvc.{Action, ControllerComponents, Request}
import services.JourneyService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateDirectDebitDetailsController @Inject() (
  actions:        Actions,
  journeyService: JourneyService,
  cc:             ControllerComponents
)(using ExecutionContext, OperationalCryptoFormat)
    extends BackendController(cc) {

  def updateDirectDebitDetails(journeyId: JourneyId): Action[BankDetails] =
    actions.authenticatedAction.async(parse.json[BankDetails]) { implicit request =>
      for {
        journey    <- journeyService.get(journeyId)
        newJourney <- journey match {
                        case j: JourneyStage.BeforeChosenTypeOfBankAccount  =>
                          Errors.throwBadRequestExceptionF(
                            s"UpdateDirectDebitDetails is not possible in that state: [${j.stage}]"
                          )
                        case j: Journey.ChosenTypeOfBankAccount             =>
                          updateJourneyWithNewValue(j, request.body)
                        case j: JourneyStage.AfterEnteredDirectDebitDetails =>
                          j match {
                            case _: JourneyStage.BeforeArrangementSubmitted =>
                              updateJourneyWithExistingValue(j, request.body)
                            case _: JourneyStage.AfterArrangementSubmitted  =>
                              Errors.throwBadRequestExceptionF(
                                "Cannot update DirectDebitDetails when journey is in completed state"
                              )
                          }
                      }
      } yield Ok(newJourney.json)
    }

  private def updateJourneyWithNewValue(
    journey:            Journey.ChosenTypeOfBankAccount,
    directDebitDetails: BankDetails
  )(using Request[?]): Future[Journey] = {
    val newJourney: Journey =
      journey
        .into[Journey.EnteredDirectDebitDetails]
        .withFieldConst(_.directDebitDetails, directDebitDetails)
        .transform

    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingValue(
    journey:            JourneyStage.AfterEnteredDirectDebitDetails & Journey,
    directDebitDetails: BankDetails
  )(using Request[?]): Future[Journey] =
    if (journey.directDebitDetails == directDebitDetails) {
      JourneyLogger.info("Direct debit details haven't changed, nothing to update")
      Future.successful(journey)
    } else {
      val updatedJourney: Journey = journey match {
        case j: Journey.EnteredDirectDebitDetails =>
          j.copy(directDebitDetails = directDebitDetails)

        case j: Journey.ConfirmedDirectDebitDetails =>
          j.into[Journey.EnteredDirectDebitDetails]
            .withFieldConst(_.directDebitDetails, directDebitDetails)
            .transform

        case j: Journey.AgreedTermsAndConditions =>
          j.into[Journey.EnteredDirectDebitDetails]
            .withFieldConst(_.directDebitDetails, directDebitDetails)
            .transform

        case j: Journey.SelectedEmailToBeVerified =>
          j.into[Journey.EnteredDirectDebitDetails]
            .withFieldConst(_.directDebitDetails, directDebitDetails)
            .transform

        case j: Journey.EmailVerificationComplete =>
          j.into[Journey.EnteredDirectDebitDetails]
            .withFieldConst(_.directDebitDetails, directDebitDetails)
            .transform

        case _: Journey.SubmittedArrangement =>
          Errors.throwBadRequestException("Cannot update DirectDebitDetails when journey is in completed state")
      }

      journeyService.upsert(updatedJourney)
    }

}
