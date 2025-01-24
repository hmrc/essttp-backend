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
) {
  private val allEligibilityErrors: Seq[Boolean] = List(
    part1.hasRlsOnAddress,
    part1.markedAsInsolvent,
    part1.isLessThanMinDebtAllowance,
    part1.isMoreThanMaxDebtAllowance,
    part1.disallowedChargeLockTypes,
    part1.existingTTP,
    part1.chargesOverMaxDebtAge.getOrElse(false),
    part1.ineligibleChargeTypes,
    part1.missingFiledReturns,
    part1.hasInvalidInterestSignals.getOrElse(false),
    part1.dmSpecialOfficeProcessingRequired.getOrElse(false),
    part1.noDueDatesReached,
    part1.cannotFindLockReason.getOrElse(false),
    part1.creditsNotAllowed.getOrElse(false),
    part1.isMoreThanMaxPaymentReference.getOrElse(false),
    part1.chargesBeforeMaxAccountingDate.getOrElse(false),
    part1.hasInvalidInterestSignalsCESA.getOrElse(false),
    part1.hasDisguisedRemuneration.getOrElse(false),
    part1.hasCapacitor.getOrElse(false),
    part1.dmSpecialOfficeProcessingRequiredCDCS.getOrElse(false),
    part1.isAnMtdCustomer.getOrElse(false),
    part1.dmSpecialOfficeProcessingRequiredCESA.getOrElse(false),
    part2.noMtditsaEnrollment.getOrElse(false)
  )

  val moreThanOneReasonForIneligibility: Boolean = allEligibilityErrors.map{ if (_) 1 else 0 }.sum > 1

  val isEligible: Boolean = allEligibilityErrors.forall(flag => !flag) // if all flags are false then isEligible is true
}

object EligibilityRules {

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val part1Format: OFormat[EligibilityRulesPart1] = Json.format[EligibilityRulesPart1]
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val part2Format: OFormat[EligibilityRulesPart2] = Json.format[EligibilityRulesPart2]

  implicit val customFormat: OFormat[EligibilityRules] = new OFormat[EligibilityRules] {

    override def reads(json: JsValue): JsResult[EligibilityRules] = {
      for {
        part1 <- part1Format.reads(json)
        part2 <- part2Format.reads(json)
      } yield EligibilityRules(part1, part2)
    }

    override def writes(rules: EligibilityRules): JsObject = {
      part1Format.writes(rules.part1).deepMerge(part2Format.writes(rules.part2))
    }
  }
}

