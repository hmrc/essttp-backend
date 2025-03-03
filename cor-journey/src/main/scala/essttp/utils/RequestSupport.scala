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

package essttp.utils

import essttp.rootmodel.SessionId
import play.api.i18n._
import play.api.mvc.{Request, RequestHeader}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject

/** Repeating the pattern which was brought originally by play-framework and putting some more data which can be derived
  * from a request
  *
  * Use it to provide HeaderCarrier, Lang, or Messages Note that Lang and Messages will be developed soon (see
  * pay-frontend code for reference)
  */
class RequestSupport @Inject() (override val messagesApi: MessagesApi) extends I18nSupport {

  given hc(using RequestHeader): HeaderCarrier = RequestSupport.hc
}

object RequestSupport {

  given hc(using request: RequestHeader): HeaderCarrier =
    HeaderCarrierConverter.fromRequestAndSession(request, request.session)

  def getSessionId()(using Request[_]): SessionId =
    hc.sessionId
      .map(s => SessionId(s.value))
      .getOrElse(
        Errors.throwBadRequestException("Session id must be provided")
      )
}
