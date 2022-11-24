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
import com.google.inject.Inject
import essttp.crypto.CryptoFormat.OperationalCryptoFormat
import essttp.journey.model._
import essttp.rootmodel.SessionId
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents, Request, RequestHeader, Result}
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.play.bootstrap.backend.http.ErrorResponse

import scala.concurrent.ExecutionContext

/**
 * Start Journey (Sj) Controller
 */
class JourneyController @Inject() (
    actions:        Actions,
    journeyService: JourneyService,
    cc:             ControllerComponents
)(implicit exec: ExecutionContext, cryptoFormat: OperationalCryptoFormat) extends BackendController(cc) {

  def getJourney(journeyId: JourneyId): Action[AnyContent] = actions.authenticatedAction.async { implicit request: Request[AnyContent] =>
    journeyService
      .get(journeyId).map(journey => Ok(Json.toJson(journey)))
      .recover {
        case _: NotFoundException =>
          notFound(journeyId)
      }
  }

  def findLatestJourneyBySessionId(): Action[AnyContent] = actions.authenticatedAction.async { implicit request =>
    val sessionId: SessionId =
      implicitly[HeaderCarrier]
        .sessionId
        .map(x => SessionId(x.value))
        .getOrElse(throw new RuntimeException("Missing required 'SessionId'"))

    journeyService.findLatestJourney(sessionId).map {
      case Some(journey: Journey) => Ok(Json.toJson(journey))
      case None                   => notFound(s"sessionId:${sessionId.toString}")
    }
  }

  private def notFound[Key](key: Key)(implicit request: RequestHeader): Result = {
    JourneyLogger.warn(s"Journey not found [${key.toString}]")
    val response = ErrorResponse(NOT_FOUND, s"Journey not found [${key.toString}]")
    NotFound(Json.toJson(response))
  }
}
