/*
 * Copyright 2026 HM Revenue & Customs
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

import play.api.libs.json.{Json, OFormat, Reads}

final case class AssessmentEligibilityRules(
  isLessThanMinDebtAllowance:     Boolean,
  isMoreThanMaxDebtAllowance:     Boolean,
  disallowedChargeLockTypes:      Boolean,
  chargesOverMaxDebtAge:          Boolean,
  ineligibleChargeTypes:          Boolean,
  noDueDatesReached:              Boolean,
  chargesBeforeMaxAccountingDate: Boolean
) derives CanEqual {

  private given CanEqual[Boolean, Any] = CanEqual.derived

  @SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.Product"))
  private val alliIneligibleFieldNames: List[String] = {
    val fieldNames  = this.productElementNames.toList
    val fieldValues = this.productIterator.toList

    fieldNames.zip(fieldValues).collect {
      case (rule, true)       => rule
      case (rule, Some(true)) => rule
    }
  }

  val moreThanOneReasonForIneligibility: Boolean = alliIneligibleFieldNames.sizeIs > 1

  val isEligible: Boolean = alliIneligibleFieldNames.isEmpty // If all rules are false, then isEligible is true

}

object AssessmentEligibilityRules {

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  given format: OFormat[AssessmentEligibilityRules] = Json.format[AssessmentEligibilityRules]

}
