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

    "have a selectedPaymentPlan method that returns the correct value" in {
      TdAll.paymentPlanAnswersNoAffordability.selectedPaymentPlan shouldBe TdAll.paymentPlan(1)
      TdAll.paymentPlanAnswersWithAffordability.selectedPaymentPlan shouldBe TdAll.paymentPlan(1)
    }

    "have a day of the month method that" - {

      "returns the correct value" in {
        TdAll.paymentPlanAnswersNoAffordability.dayOfMonth shouldBe TdAll.dayOfMonth
        TdAll.paymentPlanAnswersWithAffordability.dayOfMonth.value shouldBe TdAll.dueDate.value.getDayOfMonth
      }

      "throws an exception if there are no regular collections in the payment plan" in {
        val exception = intercept[Exception](
          TdAll.paymentPlanAnswersWithAffordability
            .copy(
              selectedPaymentPlan = TdAll.paymentPlanAnswersNoAffordability.selectedPaymentPlan.copy(
                collections = TdAll.paymentPlanAnswersNoAffordability.selectedPaymentPlan.collections.copy(
                  regularCollections = List.empty
                )
              )
            )
            .dayOfMonth
        )

        exception.getMessage shouldBe "Could not find day of month from regularCollections in selected payment plan"
      }
    }

  }

}
