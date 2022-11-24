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

final case class EligibilityRules(
    hasRlsOnAddress:                   Boolean,
    markedAsInsolvent:                 Boolean,
    isLessThanMinDebtAllowance:        Boolean,
    isMoreThanMaxDebtAllowance:        Boolean,
    disallowedChargeLockTypes:         Boolean,
    existingTTP:                       Boolean,
    chargesOverMaxDebtAge:             Boolean,
    ineligibleChargeTypes:             Boolean,
    missingFiledReturns:               Boolean,
    hasInvalidInterestSignals:         Option[Boolean],
    dmSpecialOfficeProcessingRequired: Option[Boolean],
    noDueDatesReached:                 Option[Boolean]

) {

  val moreThanOneReasonForIneligibility: Boolean = {
    List(
      hasRlsOnAddress,
      markedAsInsolvent,
      isLessThanMinDebtAllowance,
      isMoreThanMaxDebtAllowance,
      disallowedChargeLockTypes,
      existingTTP,
      chargesOverMaxDebtAge,
      ineligibleChargeTypes,
      missingFiledReturns,
      hasInvalidInterestSignals.getOrElse(false),
      dmSpecialOfficeProcessingRequired.getOrElse(false),
      noDueDatesReached.getOrElse(false)
    ).map{ if (_) 1 else 0 }.sum > 1
  }

  val isEligible: Boolean = {
    List(
      hasRlsOnAddress,
      markedAsInsolvent,
      isLessThanMinDebtAllowance,
      isMoreThanMaxDebtAllowance,
      disallowedChargeLockTypes,
      existingTTP,
      chargesOverMaxDebtAge,
      ineligibleChargeTypes,
      missingFiledReturns,
      hasInvalidInterestSignals.getOrElse(false),
      dmSpecialOfficeProcessingRequired.getOrElse(false),
      noDueDatesReached.getOrElse(false)
    ).forall(flag => !flag) //if all flags are false then isEligible is true
  }
}

object EligibilityRules {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[EligibilityRules] = Json.format[EligibilityRules]
}

