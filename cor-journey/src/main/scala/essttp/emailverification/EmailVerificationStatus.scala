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

package essttp.emailverification

import essttp.crypto.CryptoFormat
import essttp.journey.model.CorrelationId
import essttp.rootmodel.{Email, GGCredId}
import play.api.libs.json.{Format, Json, OFormat}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant

final case class EmailVerificationStatus(
    _id:                             String,
    credId:                          GGCredId,
    email:                           Email,
    numberOfPasscodeJourneysStarted: NumberOfPasscodeJourneysStarted,
    verificationResult:              Option[EmailVerificationResult],
    createdAt:                       Instant                         = Instant.now,
    lastUpdated:                     Instant                         = Instant.now
)

object EmailVerificationStatus {

  implicit val instantFormat: Format[Instant] = MongoJavatimeFormats.instantFormat

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit def format(implicit cryptoFormat: CryptoFormat): OFormat[EmailVerificationStatus] = {
    Json.format[EmailVerificationStatus]
  }

  def apply(correlationId: CorrelationId, credId: GGCredId, email: Email, verificationResult: Option[EmailVerificationResult]): EmailVerificationStatus = EmailVerificationStatus(
    _id                             = correlationId.value.toString,
    credId                          = credId,
    email                           = email,
    numberOfPasscodeJourneysStarted = NumberOfPasscodeJourneysStarted(1),
    verificationResult              = verificationResult
  )

}
