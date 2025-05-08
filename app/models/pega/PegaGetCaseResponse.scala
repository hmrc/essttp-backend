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

package models.pega

import models.pega.PegaGetCaseResponse.AA
import play.api.libs.json.{JsError, JsResult, Json, Reads}

import java.time.LocalDate
import scala.util.Try

final case class PegaGetCaseResponse(`AA`: AA)

object PegaGetCaseResponse {

  final case class AA(
    paymentDay:  Int,
    paymentPlan: Seq[PegaPaymentPlan],
    expenditure: Seq[ExpenditureItem],
    income:      Seq[IncomeItem]
  )

  object AA {
    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    given Reads[AA] =
      Reads(json =>
        for {
          paymentDay  <- (json \ "paymentDay")
                           .validate[String]
                           .flatMap(s =>
                             JsResult.fromTry(
                               Try(s.toInt),
                               e => JsError(s"Could not read paymentDay: ${e.getMessage}")
                             )
                           )
          paymentPlan <- (json \ "paymentPlan").validate[Seq[PegaPaymentPlan]]
          expenditure <- (json \ "expenditure").validate[Seq[ExpenditureItem]]
          income      <- (json \ "income").validate[Seq[IncomeItem]]
        } yield AA(paymentDay, paymentPlan, expenditure, income)
      )

  }

  final case class ExpenditureItem(
    amountValue: String,
    pyLabel:     String
  )

  object ExpenditureItem {
    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    given Reads[ExpenditureItem] = Json.reads
  }

  final case class IncomeItem(
    amountValue: String,
    pyLabel:     String
  )

  object IncomeItem {
    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    given Reads[IncomeItem] = Json.reads
  }

  final case class PegaPaymentPlan(
    numberOfInstalments: Int,
    planDuration:        Int,
    totalDebt:           Long,
    totalDebtIncInt:     Long,
    planInterest:        Long,
    collections:         PegaCollections,
    instalments:         List[PegaInstalment],
    planSelected:        Boolean
  )

  object PegaPaymentPlan {
    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    given Reads[PegaPaymentPlan] = Json.reads[PegaPaymentPlan]
  }

  final case class PegaCollections(
    initialCollection:  Option[PegaCollection],
    regularCollections: List[PegaCollection]
  )

  object PegaCollections {
    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    given Reads[PegaCollections] = Json.reads[PegaCollections]
  }

  final case class PegaCollection(dueDate: LocalDate, amountDue: Long)

  object PegaCollection {
    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    given Reads[PegaCollection] = Json.reads[PegaCollection]
  }

  final case class PegaInstalment(
    instalmentNumber:          Int,
    dueDate:                   LocalDate,
    instalmentInterestAccrued: Long,
    instalmentBalance:         Long,
    debtItemChargeId:          String,
    amountDue:                 Long,
    debtItemOriginalDueDate:   LocalDate,
    expectedPayment:           Option[Int]
  )

  object PegaInstalment {
    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    given Reads[PegaInstalment] = Json.reads[PegaInstalment]
  }

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  given Reads[PegaGetCaseResponse] = Json.reads

}
