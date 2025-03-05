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

import essttp.journey.model.PaymentPlanAnswers

package object controllers {

  extension (p: PaymentPlanAnswers) {

    def affordabilityAnswers = p match {
      case p: PaymentPlanAnswers.PaymentPlanNoAffordability    =>
        sys.error(s"Expected affordability answers but got non affordability answers ${p.toString}")
      case p: PaymentPlanAnswers.PaymentPlanAfterAffordability =>
        p
    }

    def nonAffordabilityAnswers = p match {
      case p: PaymentPlanAnswers.PaymentPlanNoAffordability    =>
        p
      case p: PaymentPlanAnswers.PaymentPlanAfterAffordability =>
        sys.error(s"Expected non-affordability answers but got affordability answers ${p.toString}")
    }

  }

}
