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

import play.api.libs.json.{JsObject, JsResult, JsValue, Json, OFormat}

final case class EligibilityRulesPart1(
  hasRlsOnAddress:                       Boolean,
  markedAsInsolvent:                     Boolean,
  isLessThanMinDebtAllowance:            Boolean,
  isMoreThanMaxDebtAllowance:            Boolean,
  disallowedChargeLockTypes:             Boolean,
  existingTTP:                           Boolean,
  chargesOverMaxDebtAge:                 Option[Boolean],
  ineligibleChargeTypes:                 Boolean,
  missingFiledReturns:                   Boolean,
  hasInvalidInterestSignals:             Option[Boolean],
  dmSpecialOfficeProcessingRequired:     Option[Boolean],
  noDueDatesReached:                     Boolean,
  cannotFindLockReason:                  Option[Boolean],
  creditsNotAllowed:                     Option[Boolean],
  isMoreThanMaxPaymentReference:         Option[Boolean],
  chargesBeforeMaxAccountingDate:        Option[Boolean],
  hasInvalidInterestSignalsCESA:         Option[Boolean],
  hasDisguisedRemuneration:              Option[Boolean],
  hasCapacitor:                          Option[Boolean],
  dmSpecialOfficeProcessingRequiredCDCS: Option[Boolean],
  isAnMtdCustomer:                       Option[Boolean],
  dmSpecialOfficeProcessingRequiredCESA: Option[Boolean]
)

final case class EligibilityRulesPart2(
  noMtditsaEnrollment: Option[Boolean]
)

// TODO after scala 3 upgrade, merge back into one case class
final case class EligibilityRules(
  part1: EligibilityRulesPart1,
  part2: EligibilityRulesPart2
) derives CanEqual {

  private given CanEqual[Boolean, Any] = CanEqual.derived

  @SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.Product"))
  private def extractErrors(obj: Product): List[String] = {
    val fieldNames  = obj.getClass.getDeclaredFields.map(_.getName).toList
    val fieldValues = obj.productIterator.toList

    fieldNames.zip(fieldValues).collect {
      case (rule, true)       => rule
      case (rule, Some(true)) => rule
    }
  }

  private val allEligibilityErrors: Seq[String] =
    extractErrors(part1) ++ extractErrors(part2)

  val moreThanOneReasonForIneligibility: Boolean = allEligibilityErrors.sizeIs > 1

  val isEligible: Boolean = allEligibilityErrors.isEmpty // If all rules are false, then isEligible is true

}

object EligibilityRules {

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  given part1Format: OFormat[EligibilityRulesPart1] = Json.format[EligibilityRulesPart1]
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  given part2Format: OFormat[EligibilityRulesPart2] = Json.format[EligibilityRulesPart2]

  given OFormat[EligibilityRules] = new OFormat[EligibilityRules] {

    override def reads(json: JsValue): JsResult[EligibilityRules] =
      for {
        part1 <- part1Format.reads(json)
        part2 <- part2Format.reads(json)
      } yield EligibilityRules(part1, part2)

    override def writes(rules: EligibilityRules): JsObject =
      part1Format.writes(rules.part1).deepMerge(part2Format.writes(rules.part2))
  }
}
