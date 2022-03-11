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

package requests

import play.api.i18n._
import play.api.mvc.{Request, RequestHeader}
import rootmodel.SessionId
import uk.gov.hmrc.http.HeaderCarrier
import utils.Errors

import javax.inject.Inject
import scala.concurrent.Future

/**
 * Repeating the pattern which was brought originally by play-framework
 * and putting some more data which can be derived from a request
 *
 * Use it to provide HeaderCarrier, Lang, or Messages
 * Note that Lang and Messages will be developed soon (see pay-frontend code for reference)
 */
class RequestSupport @Inject() (override val messagesApi: MessagesApi) extends I18nSupport {

  implicit def hc(implicit request: RequestHeader): HeaderCarrier = RequestSupport.hc
}

object RequestSupport {

  implicit def hc(implicit request: RequestHeader): HeaderCarrier = HcProvider.headerCarrier

  def getSessionId()(implicit request: Request[_]): Future[SessionId] = Future.successful{
    //HINT: We wrapp it into future so it can throw exception inside for comprehension and propagate
    // nicely failed result
    hc
      .sessionId
      .map(s=>SessionId(s.value))
      .getOrElse(
        Errors.throwBadRequestException("Session id must be provided")
      )
  }

  /**
   * This is because we want to give responsibility of creation of [[HeaderCarrier]] to the platform code.
   * If they refactor how hc is created our code will pick it up automatically.
   */
  private object HcProvider extends uk.gov.hmrc.play.bootstrap.backend.controller.BackendHeaderCarrierProvider {
    def headerCarrier(implicit request: RequestHeader): HeaderCarrier = hc(request)
  }
}
