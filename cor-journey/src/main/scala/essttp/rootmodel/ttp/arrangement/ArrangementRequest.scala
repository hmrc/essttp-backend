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

package essttp.rootmodel.ttp.arrangement

import essttp.crypto.CryptoFormat
import essttp.rootmodel.pega.PegaCaseId
import essttp.rootmodel.ttp.affordablequotes.ChannelIdentifier
import essttp.rootmodel.ttp.eligibility.{Address, CustomerDetail, Identification, IndividualDetails, RegimeDigitalCorrespondence}
import essttp.rootmodel.ttp.{PaymentPlanFrequency, RegimeType}
import play.api.libs.json.{Json, OFormat}

final case class ArrangementRequest(
    channelIdentifier:           ChannelIdentifier,
    regimeType:                  RegimeType,
    hasAffordabilityAssessment:  Boolean,
    caseID:                      Option[PegaCaseId],
    regimePaymentFrequency:      PaymentPlanFrequency,
    arrangementAgreedDate:       ArrangementAgreedDate,
    identification:              List[Identification],
    directDebitInstruction:      DirectDebitInstruction,
    paymentPlan:                 EnactPaymentPlan,
    customerDetails:             Option[List[CustomerDetail]],
    individualDetails:           Option[IndividualDetails],
    addresses:                   Option[List[Address]],
    regimeDigitalCorrespondence: Option[RegimeDigitalCorrespondence]
)

object ArrangementRequest {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit def format(implicit cryptoFormat: CryptoFormat): OFormat[ArrangementRequest] = Json.format

}
