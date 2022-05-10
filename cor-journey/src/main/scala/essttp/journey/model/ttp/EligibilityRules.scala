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

package essttp.journey.model.ttp

import play.api.libs.json.{Format, Json, OFormat}

final case class EligibilityRules(
    rlsOnAddress:         Boolean,
    rlsReason:            String,
    markedAsInsolvent:    Boolean,
    minimumDebtAllowance: Boolean,
    maxDebtAllowance:     Boolean,
    disallowedChargeLock: Boolean,
    existingTTP:          Boolean,
    minInstalmentAmount:  Int,
    maxInstalmentAmount:  Int,
    maxDebtAge:           Boolean,
    eligibleChargeType:   Boolean,
    returnsFiled:         Boolean
)

object EligibilityRules {
  implicit val format: OFormat[EligibilityRules] = Json.format[EligibilityRules]
}

