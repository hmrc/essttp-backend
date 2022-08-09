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

package essttp.rootmodel.ttp

import play.api.libs.json.{Json, OFormat}

/**
 * This represents response from the Eligibylity API
 * https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?spaceKey=DTDT&title=Eligibility+API
 */
final case class EligibilityCheckResult(
    processingDateTime:     ProcessingDateTime,
    identification:         List[Identification],
    customerPostcodes:      List[CustomerPostcode],
    regimePaymentFrequency: PaymentPlanFrequency,
    paymentPlanFrequency:   PaymentPlanFrequency,
    paymentPlanMinLength:   PaymentPlanMinLength,
    paymentPlanMaxLength:   PaymentPlanMaxLength,
    eligibilityStatus:      EligibilityStatus,
    eligibilityRules:       EligibilityRules,
    chargeTypeAssessment:   List[ChargeTypeAssessment]
) {
  val isEligible: Boolean = eligibilityStatus.eligibilityPass.value
}

object EligibilityCheckResult {
  implicit val format: OFormat[EligibilityCheckResult] = Json.format[EligibilityCheckResult]

}
