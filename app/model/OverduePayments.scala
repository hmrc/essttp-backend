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

package model

import play.api.libs.json.{Format, Json}

import java.time.LocalDate

case class OverduePayments(
    total:    AmountInPence,
    payments: List[OverduePayment]
)
object OverduePayments {
  implicit val fmt: Format[OverduePayments] = Json.format[OverduePayments]
}
case class OverduePayment(
    invoicePeriod: InvoicePeriod,
    amount:        AmountInPence
)
object OverduePayment {

  implicit val fmt: Format[OverduePayment] = Json.format[OverduePayment]

}
case class InvoicePeriod(
    monthNumber: Int,
    start:       LocalDate,
    end:         LocalDate,
    dueDate:     LocalDate
)

object InvoicePeriod {

  implicit val fmt: Format[InvoicePeriod] = Json.format[InvoicePeriod]

}
