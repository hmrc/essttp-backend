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

package essttp.testdata.emailverificationstatus

import essttp.emailverification.{EmailVerificationStatus, NumberOfPasscodeJourneysStarted}
import essttp.rootmodel.{Email, GGCredId}
import uk.gov.hmrc.crypto.Sensitive.SensitiveString

import java.time.{LocalDateTime, ZoneOffset}
import java.util.UUID

object EmailVerificationStatusTestData {

  def email(emailString: String): Email = Email(SensitiveString(emailString))

  def emailVerificationStatus(
      email: Email  = Email(SensitiveString("email@test.com")),
      id:    String = "5838794a-5419-496c-a5dd-e807f91d6da6"
  ): EmailVerificationStatus = EmailVerificationStatus(
    _id                             = id,
    credId                          = GGCredId("authId-b580f0ba-991f-4cd0-9a30-f833ec58de03"),
    email                           = email,
    numberOfPasscodeJourneysStarted = NumberOfPasscodeJourneysStarted(1),
    verificationResult              = None,
    createdAt                       = LocalDateTime.parse("2057-11-02T16:28:55.185").toInstant(ZoneOffset.UTC),
    lastUpdated                     = LocalDateTime.parse("2057-11-02T16:28:55.185").toInstant(ZoneOffset.UTC)
  )

  def createXNumberOfStatuses(numberOfStatuses: Int, credId: GGCredId = GGCredId("authId-b580f0ba-991f-4cd0-9a30-f833ec58de03")): Seq[EmailVerificationStatus] = (1 to numberOfStatuses) map { _ =>
    emailVerificationStatus(email(UUID.randomUUID().toString), UUID.randomUUID().toString).copy(credId = credId)
  }

}
