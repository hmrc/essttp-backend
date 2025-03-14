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

package essttp.rootmodel.ttp.affordablequotes

import essttp.rootmodel.ttp.{IsInterestBearingCharge, UseChargeReference}
import essttp.rootmodel.ttp.eligibility.{ChargeReference, InterestStartDate, MainTrans, SubTrans}
import play.api.libs.json.{Json, OFormat}

final case class DebtItemCharge(
  outstandingDebtAmount:   OutstandingDebtAmount,
  mainTrans:               MainTrans,
  subTrans:                SubTrans,
  isInterestBearingCharge: Option[IsInterestBearingCharge],
  useChargeReference:      Option[UseChargeReference],
  debtItemChargeId:        ChargeReference,
  interestStartDate:       Option[InterestStartDate],
  debtItemOriginalDueDate: DebtItemOriginalDueDate
)

object DebtItemCharge {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  given OFormat[DebtItemCharge] = Json.format[DebtItemCharge]
}
