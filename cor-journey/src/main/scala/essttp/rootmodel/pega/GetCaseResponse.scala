/*
 * Copyright 2024 HM Revenue & Customs
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

package essttp.rootmodel.pega

import essttp.rootmodel.DayOfMonth
import essttp.rootmodel.ttp.affordablequotes.PaymentPlan
import play.api.libs.json.{Json, OFormat}

final case class GetCaseResponse(
    paymentDay:    DayOfMonth,
    paymentPlan:   PaymentPlan,
    expenditure:   Map[String, BigDecimal],
    income:        Map[String, BigDecimal],
    correlationId: String
)

object GetCaseResponse {

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[GetCaseResponse] = Json.format

}
