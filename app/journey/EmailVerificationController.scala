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
import email.EmailVerificationStatusService
import essttp.crypto.CryptoFormat.OperationalCryptoFormat
import essttp.emailverification.{GetEmailVerificationResultRequest, StartEmailVerificationJourneyRequest}
import essttp.journey.model.JourneyId
import essttp.rootmodel.GGCredId
import play.api.libs.json.Json
import play.api.mvc.{Action, ControllerComponents}
import services.{EmailVerificationService, JourneyService}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.ExecutionContext

@Singleton
class EmailVerificationController @Inject() (
    actions:                        Actions,
    emailVerificationService:       EmailVerificationService,
    emailVerificationStatusService: EmailVerificationStatusService,
    journeyService:                 JourneyService,
    cc:                             ControllerComponents
)(implicit exec: ExecutionContext, cryptoFormat: OperationalCryptoFormat) extends BackendController(cc) {

  def startEmailVerificationJourney(journeyId: JourneyId): Action[StartEmailVerificationJourneyRequest] =
    actions.authenticatedAction(parse.json[StartEmailVerificationJourneyRequest]).async { implicit request =>
      for {
        journey <- journeyService.get(journeyId)
        result <- emailVerificationService.startEmailVerificationJourney(request.body, journey)
      } yield Ok(Json.toJson(result))
    }

  def getEmailVerificationResult(journeyId: JourneyId): Action[GetEmailVerificationResultRequest] =
    actions.authenticatedAction(parse.json[GetEmailVerificationResultRequest]).async{ implicit request =>
      for {
        journey <- journeyService.get(journeyId)
        result <- emailVerificationService.getVerificationResult(request.body, journey)
      } yield Ok(Json.toJson(result))
    }

  val getEarliestCreatedAt: Action[GGCredId] =
    actions.authenticatedAction(parse.json[GGCredId]).async { implicit request =>
      emailVerificationStatusService.findEarliestCreatedAt(request.body)
        .map(result => Ok(Json.toJson(result)))
    }

}
