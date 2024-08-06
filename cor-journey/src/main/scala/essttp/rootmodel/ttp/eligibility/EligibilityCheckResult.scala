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

package essttp.rootmodel.ttp.eligibility

import essttp.crypto.CryptoFormat
import essttp.rootmodel.Email
import essttp.rootmodel.ttp._
import play.api.libs.json.{Json, OFormat}

/**
 * This represents response from the Eligibylity API
 * https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?spaceKey=DTDT&title=Eligibility+API
 */
final case class EligibilityCheckResult(
    processingDateTime:              ProcessingDateTime,
    identification:                  List[Identification],
    customerPostcodes:               List[CustomerPostcode],
    regimePaymentFrequency:          PaymentPlanFrequency,
    paymentPlanFrequency:            PaymentPlanFrequency,
    paymentPlanMinLength:            PaymentPlanMinLength,
    paymentPlanMaxLength:            PaymentPlanMaxLength,
    eligibilityStatus:               EligibilityStatus,
    eligibilityRules:                EligibilityRules,
    chargeTypeAssessment:            List[ChargeTypeAssessment],
    customerDetails:                 Option[List[CustomerDetail]],
    addresses:                       Option[List[Address]],
    regimeDigitalCorrespondence:     Option[RegimeDigitalCorrespondence],
    chargeTypesExcluded:             Option[Boolean],
    futureChargeLiabilitiesExcluded: Boolean,
    invalidSignals:                  Option[List[InvalidSignals]],
    customerType:                    Option[CustomerType],
    transitionToCDCS:                Option[TransitionToCDCS]
)

object EligibilityCheckResult {

  implicit class EligibilityCheckResultOps(private val e: EligibilityCheckResult) extends AnyVal {

    def isEligible: Boolean = e.eligibilityStatus.eligibilityPass.value

    def email: Option[Email] = e.customerDetails.flatMap(_.collectFirst{ case CustomerDetail(Some(email), _, _, _, _, _, _, _) => email })

  }

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit def format(implicit cryptoFormat: CryptoFormat): OFormat[EligibilityCheckResult] = Json.format[EligibilityCheckResult]

}
