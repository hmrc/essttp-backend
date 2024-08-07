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

//TODO OPS-12584 - Clean this up when TTP has implemented the changes to the Eligibility API. emailAddress and emailSource to be removed
final case class CustomerDetail(
    emailAddress:   Option[Email],
    emailSource:    Option[EmailSource],
    title:          Option[Title],
    firstName:      Option[FirstName],
    lastName:       Option[LastName],
    dateOfBirth:    Option[DateOfBirth],
    dateOfDeath:    Option[DateOfDeath],
    districtNumber: Option[DistrictNumber]
)

object CustomerDetail {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit def format(implicit cryptoFormat: CryptoFormat): Format[CustomerDetail] = Json.format[CustomerDetail]

}
