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
import play.api.libs.json.{Format, Json}

//TODO OPS-12584 - Clean this up when TTP has implemented the changes to the Eligibility API. addressType will be required
final case class Address(
    addressType:     Option[AddressType],
    addressLine1:    Option[AddressLine],
    addressLine2:    Option[AddressLine],
    addressLine3:    Option[AddressLine],
    addressLine4:    Option[AddressLine],
    rls:             Option[IsReturnedLetterService],
    contactDetails:  Option[List[ContactDetail]],
    postcode:        Option[Postcode],
    country:         Option[Country],
    postCodeHistory: Option[List[PostCodeHistory]]
)

object Address {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit def format(implicit cryptoFormat: CryptoFormat): Format[Address] = Json.format[Address]
}
