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
import essttp.journey.model
import essttp.journey.model.{Journey, JourneyId, JourneyStage}
import essttp.utils.Errors
import io.scalaland.chimney.dsl.*
import play.api.mvc.{Action, AnyContent, ControllerComponents, Request}
import services.JourneyService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateHasConfirmedDirectDebitDetailsController @Inject() (
  actions:        Actions,
  journeyService: JourneyService,
  cc:             ControllerComponents
)(using ExecutionContext, OperationalCryptoFormat)
    extends BackendController(cc) {

  def updateConfirmedDirectDebitDetails(journeyId: JourneyId): Action[AnyContent] = actions.authenticatedAction.async {
    implicit request =>
      for {
        journey    <- journeyService.get(journeyId)
        newJourney <- journey match {
                        case j: JourneyStage.BeforeEnteredDirectDebitDetails  =>
                          Errors.throwBadRequestExceptionF(
                            s"UpdateHasConfirmedDirectDebitDetails is not possible in that state: [${j.stage}]"
                          )
                        case j: model.Journey.EnteredDirectDebitDetails       =>
                          updateJourneyWithNewValue(j)
                        case j: JourneyStage.AfterConfirmedDirectDebitDetails =>
                          j match {
                            case _: JourneyStage.BeforeArrangementSubmitted => Future.successful(j)
                            case _: JourneyStage.AfterArrangementSubmitted  =>
                              Errors.throwBadRequestExceptionF(
                                "Cannot update ConfirmedDirectDebitDetails when journey is in completed state"
                              )
                          }
                      }
      } yield Ok(newJourney.json)
  }

  private def updateJourneyWithNewValue(
    journey: Journey.EnteredDirectDebitDetails
  )(using Request[_]): Future[Journey] = {
    val newJourney: Journey = journey.into[Journey.ConfirmedDirectDebitDetails].transform

    journeyService.upsert(newJourney)
  }
}
