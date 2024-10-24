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

import testsupport.UnitSpec
import testsupport.testdata.TdAll

class PaymentPlanAnswersSpec extends UnitSpec {

  "PaymentPlanAnswers must" - {

    "have a selectedPaymentPlan method that returns the correct isAccountHolder" in {
      (TdAll.paymentPlanAnswersNoAffordability: PaymentPlanAnswers).selectedPaymentPlan shouldBe TdAll.paymentPlan(1)
      (TdAll.paymentPlanAnswersWithAffordability: PaymentPlanAnswers).selectedPaymentPlan shouldBe TdAll.paymentPlan(1)
    }

    "have a day of the month method that" - {

      "returns the correct isAccountHolder" in {
        (TdAll.paymentPlanAnswersNoAffordability: PaymentPlanAnswers).dayOfMonth shouldBe TdAll.dayOfMonth
        TdAll.paymentPlanAnswersWithAffordability.dayOfMonth shouldBe TdAll.dayOfMonth
      }

    }

  }

}
