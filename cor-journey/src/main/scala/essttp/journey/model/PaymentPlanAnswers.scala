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

package essttp.journey.model

import essttp.rootmodel.dates.startdates.StartDatesResponse
import essttp.rootmodel.pega.StartCaseResponse
import essttp.rootmodel.ttp.affordablequotes.{AffordableQuotesResponse, PaymentPlan}
import essttp.rootmodel.{DayOfMonth, MonthlyPaymentAmount}
import essttp.utils.DerivedJson
import essttp.utils.DerivedJson.Circe.formatToCodec
import io.circe.generic.semiauto.deriveCodec
import play.api.libs.json.OFormat

sealed trait PaymentPlanAnswers derives CanEqual

object PaymentPlanAnswers {

  final case class PaymentPlanNoAffordability(
    monthlyPaymentAmount:     MonthlyPaymentAmount,
    dayOfMonth:               DayOfMonth,
    startDatesResponse:       StartDatesResponse,
    affordableQuotesResponse: AffordableQuotesResponse,
    selectedPaymentPlan:      PaymentPlan
  ) extends PaymentPlanAnswers

  final case class PaymentPlanAfterAffordability(
    startCaseResponse:   StartCaseResponse,
    dayOfMonth:          DayOfMonth,
    selectedPaymentPlan: PaymentPlan
  ) extends PaymentPlanAnswers

  extension (answers: PaymentPlanAnswers) {

    def selectedPaymentPlan: PaymentPlan = answers match {
      case p: PaymentPlanNoAffordability    => p.selectedPaymentPlan
      case p: PaymentPlanAfterAffordability => p.selectedPaymentPlan
    }

    def dayOfMonth: DayOfMonth = answers match {
      case p: PaymentPlanNoAffordability    => p.dayOfMonth
      case p: PaymentPlanAfterAffordability => p.dayOfMonth
    }

  }

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  given OFormat[PaymentPlanAnswers] =
    DerivedJson.Circe.format(deriveCodec[PaymentPlanAnswers])

}
