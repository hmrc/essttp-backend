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

package essttp.journey.model.ttp.affordability

import essttp.journey.model.ttp.{CustomerPostcode, MaxPlanLengthMonths, MinPlanLengthMonths}
import essttp.rootmodel.AmountInPence
import essttp.rootmodel.dates.InitialPaymentDate
import essttp.rootmodel.dates.extremedates.{EarliestPlanStartDate, LatestPlanStartDate}
import play.api.libs.json.{Json, OFormat}

final case class InstalmentAmountRequest(
    minPlanLength:         MinPlanLengthMonths,
    maxPlanLength:         MaxPlanLengthMonths,
    interestAccrued:       AmountInPence,
    frequency:             String,
    earliestPlanStartDate: EarliestPlanStartDate,
    latestPlanStartDate:   LatestPlanStartDate,
    initialPaymentDate:    Option[InitialPaymentDate],
    initialPaymentAmount:  Option[AmountInPence],
    debtItemCharges:       List[DebtItemCharge],
    customerPostcodes:     List[CustomerPostcode]
)

object InstalmentAmountRequest {

  implicit val format: OFormat[InstalmentAmountRequest] = Json.format[InstalmentAmountRequest]

}