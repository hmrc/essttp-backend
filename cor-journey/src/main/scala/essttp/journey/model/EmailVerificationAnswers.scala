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

package essttp.journey.model

import essttp.crypto.CryptoFormat
import essttp.rootmodel.Email
import essttp.utils.DerivedJson
import essttp.utils.DerivedJson.Circe.formatToCodec
import io.circe.generic.semiauto.deriveCodec
import paymentsEmailVerification.models.EmailVerificationResult
import play.api.libs.json.OFormat

sealed trait EmailVerificationAnswers derives CanEqual

object EmailVerificationAnswers {

  case object NoEmailJourney extends EmailVerificationAnswers

  final case class EmailVerified(email: Email, emailVerificationResult: EmailVerificationResult)
      extends EmailVerificationAnswers

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit def format(using CryptoFormat): OFormat[EmailVerificationAnswers] =
    DerivedJson.Circe.format(deriveCodec[EmailVerificationAnswers])

}
