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

import essttp.rootmodel.ttp.{ChargeReference, InterestAccrued}
import play.api.libs.json.{Json, OFormat}

final case class Instalment(
    instalmentNumber:          InstalmentNumber,
    dueDate:                   DueDate,
    instalmentInterestAccrued: InterestAccrued,
    instalmentBalance:         InstalmentBalance,
    debtItemChargeId:          ChargeReference,
    amountDue:                 AmountDue,
    debtItemOriginalDueDate:   DebtItemOriginalDueDate
)

object Instalment {
  implicit val format: OFormat[Instalment] = Json.format[Instalment]
}
