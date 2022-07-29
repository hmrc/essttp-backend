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

package essttp.rootmodel.ttp.affordablequotes

import essttp.rootmodel.dates.InitialPaymentDate
import essttp.rootmodel.dates.startdates.InstalmentStartDate
import essttp.rootmodel.UpfrontPaymentAmount
import essttp.rootmodel.ttp.{CustomerPostcode, PaymentPlanFrequency, PaymentPlanMaxLength, PaymentPlanMinLength}
import play.api.libs.json.{Json, OFormat}

final case class AffordableQuotesRequest(
    channelIdentifier:           ChannelIdentifier,
    paymentPlanAffordableAmount: PaymentPlanAffordableAmount,
    paymentPlanFrequency:        PaymentPlanFrequency,
    paymentPlanMaxLength:        PaymentPlanMaxLength,
    paymentPlanMinLength:        PaymentPlanMinLength,
    accruedDebtInterest:         AccruedDebtInterest,
    paymentPlanStartDate:        InstalmentStartDate,
    initialPaymentDate:          Option[InitialPaymentDate],
    initialPaymentAmount:        Option[UpfrontPaymentAmount],
    debtItemCharges:             List[DebtItemCharge],
    customerPostcodes:           List[CustomerPostcode]
)

object AffordableQuotesRequest {
  implicit val format: OFormat[AffordableQuotesRequest] = Json.format[AffordableQuotesRequest]
}
