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
import com.google.inject.{Inject, Singleton}
import essttp.crypto.CryptoFormat.OperationalCryptoFormat
import essttp.emailverification.{GetEmailVerificationResultRequest, StartEmailVerificationJourneyRequest}
import play.api.libs.json.Json
import play.api.mvc.{Action, ControllerComponents}
import services.EmailVerificationService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.ExecutionContext

@Singleton
class EmailVerificationController @Inject() (
    actions:                  Actions,
    emailVerificationService: EmailVerificationService,
    cc:                       ControllerComponents
)(implicit exec: ExecutionContext, cryptoFormat: OperationalCryptoFormat) extends BackendController(cc) {

  val startEmailVerificationJourney: Action[StartEmailVerificationJourneyRequest] =
    actions.authenticatedAction(parse.json[StartEmailVerificationJourneyRequest]).async{ implicit request =>
      emailVerificationService.startEmailVerificationJourney(request.body)
        .map(result => Ok(Json.toJson(result)))
    }

  val getEmailVerificationResult: Action[GetEmailVerificationResultRequest] =
    actions.authenticatedAction(parse.json[GetEmailVerificationResultRequest]).async{ implicit request =>
      emailVerificationService.getVerificationResult(request.body)
        .map(result => Ok(Json.toJson(result)))
    }

}
