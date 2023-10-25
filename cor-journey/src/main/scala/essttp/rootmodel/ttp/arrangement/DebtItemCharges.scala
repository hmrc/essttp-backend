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

import essttp.rootmodel.ttp.affordablequotes.{DebtItemOriginalDueDate, OutstandingDebtAmount}
import essttp.rootmodel.ttp.eligibility.{AccruedInterest, ChargeReference}
import play.api.libs.json.{Format, Json}

final case class DebtItemCharges(
                                  outstandingDebtAmount:   OutstandingDebtAmount,
                                  debtItemChargeId:        ChargeReference,
                                  debtItemOriginalDueDate: DebtItemOriginalDueDate,
                                  accruedInterest:         AccruedInterest,
                                  isInterestBearingCharge: Option[Boolean],
                                  useChargeReference: Option[Boolean]
)

object DebtItemCharges {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: Format[DebtItemCharges] = Json.format[DebtItemCharges]
}
