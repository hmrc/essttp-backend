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

package essttp.rootmodel.ttp.affordability

import essttp.crypto.CryptoFormat
import essttp.rootmodel.AmountInPence
import essttp.rootmodel.dates.InitialPaymentDate
import essttp.rootmodel.dates.extremedates.{EarliestPaymentPlanStartDate, LatestPaymentPlanStartDate}
import essttp.rootmodel.ttp.affordablequotes.{AccruedDebtInterest, ChannelIdentifier, DebtItemCharge}
import essttp.rootmodel.ttp.eligibility.CustomerPostcode
import essttp.rootmodel.ttp.{PaymentPlanFrequency, PaymentPlanMaxLength, PaymentPlanMinLength, RegimeType}
import play.api.libs.json.{Json, OFormat}

final case class InstalmentAmountRequest(
    channelIdentifier:            ChannelIdentifier,
    regimeType:                   RegimeType,
    paymentPlanFrequency:         PaymentPlanFrequency,
    paymentPlanMinLength:         PaymentPlanMinLength,
    paymentPlanMaxLength:         PaymentPlanMaxLength,
    earliestPaymentPlanStartDate: EarliestPaymentPlanStartDate,
    latestPaymentPlanStartDate:   LatestPaymentPlanStartDate,
    initialPaymentDate:           Option[InitialPaymentDate],
    initialPaymentAmount:         Option[AmountInPence],
    accruedDebtInterest:          AccruedDebtInterest,
    debtItemCharges:              List[DebtItemCharge],
    customerPostcodes:            List[CustomerPostcode]
)

object InstalmentAmountRequest {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit def format(implicit cryptoFormat: CryptoFormat): OFormat[InstalmentAmountRequest] = Json.format[InstalmentAmountRequest]
}
