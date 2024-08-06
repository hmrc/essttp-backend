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

package essttp.rootmodel.ttp.eligibility

import essttp.crypto.CryptoFormat
import essttp.rootmodel.Email
import play.api.libs.json.{Format, Json}

final case class ContactDetail(
    telephoneNumber: Option[TelNumber],
    fax:             Option[FaxNumber],
    mobile:          Option[TelNumber],
    emailAddress:    Option[Email],
    altFormat:       Option[AltLetterFormat]
)

object ContactDetail {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit def format(implicit cryptoFormat: CryptoFormat): Format[ContactDetail] = Json.format[ContactDetail]
}
