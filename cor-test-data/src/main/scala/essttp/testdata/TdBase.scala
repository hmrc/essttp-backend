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

package essttp.testdata

import essttp.journey.model.JourneyId
import essttp.rootmodel._
import essttp.utils.TdSupport.FakeRequestOps
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest

import java.time.LocalDateTime

trait TdBase {
  def journeyId: JourneyId = JourneyId("b6217497-ab5b-4e93-855a-afc9f9e933b6")
  def traceId: TraceId = TraceId(journeyId)
  def sessionId: SessionId = SessionId("session-2082fcd4-70f6-49cc-a4bf-845917981cd7")

  def createdOn: LocalDateTime = LocalDateTime.parse("2057-11-02T16:28:55.185")
  def amountToUpdate: AmountInPence = AmountInPence(123499)
  def amountInPence: AmountInPence = AmountInPence(1000)
  def upfrontPaymentAmount: UpfrontPaymentAmount = UpfrontPaymentAmount(amountInPence)

  def backUrl: BackUrl = BackUrl("https://www.tax.service.gov.uk/back-url")
  def returnUrl: ReturnUrl = ReturnUrl("https://www.tax.service.gov.uk/return-url")

  def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
    .withSessionId()
    .withAuthToken()
    .withAkamaiReputationHeader()
    .withRequestId()
    .withTrueClientIp()
    .withTrueClientPort()
    .withDeviceId()

  def requestNotLoggedIn: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
    .withSessionId()
    .withAkamaiReputationHeader()
    .withRequestId()
    .withTrueClientIp()
    .withTrueClientPort()
    .withDeviceId()

  def authToken = "authorization-value-123"
  def akamaiReputationValue = "akamai-reputation-value-123"
  def requestId = "request-id-value-123"
  def trueClientIp = "client-ip-123"
  def trueClientPort = "client-port-123"
  def deviceId = "device-id-123"

}
