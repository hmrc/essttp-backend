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

package essttp.rootmodel.ttp.arrangement

import essttp.rootmodel.ttp.PaymentPlanFrequency
import essttp.rootmodel.ttp.affordablequotes.{Collection, Instalment, NumberOfInstalments, PlanDuration, PlanInterest, TotalDebt, TotalDebtIncludingInterest}
import play.api.libs.json.{Json, OFormat}

final case class EnactPaymentPlan(
    planDuration:         PlanDuration,
    paymentPlanFrequency: PaymentPlanFrequency,
    numberOfInstalments:  NumberOfInstalments,
    totalDebt:            TotalDebt,
    totalDebtIncInt:      TotalDebtIncludingInterest,
    planInterest:         PlanInterest,
    collections:          Collection,
    instalments:          List[Instalment]
)

object EnactPaymentPlan {
  implicit val format: OFormat[EnactPaymentPlan] = Json.format[EnactPaymentPlan]
}
