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

package essttp.journey.model.ttp

import essttp.rootmodel.ttp.eligibility.EligibilityRules
import testsupport.UnitSpec

class EligibilityRulesSpec extends UnitSpec {

  "isEligible should work when" - {

    "all fields are populated" in {
      EligibilityRules(
        hasRlsOnAddress                   = false,
        markedAsInsolvent                 = false,
        isLessThanMinDebtAllowance        = false,
        isMoreThanMaxDebtAllowance        = false,
        disallowedChargeLockTypes         = false,
        existingTTP                       = false,
        chargesOverMaxDebtAge             = Some(false),
        ineligibleChargeTypes             = false,
        missingFiledReturns               = false,
        hasInvalidInterestSignals         = Some(false),
        dmSpecialOfficeProcessingRequired = Some(false),
        noDueDatesReached                 = false,
        cannotFindLockReason              = Some(false),
        creditsNotAllowed                 = Some(false),
        isMoreThanMaxPaymentReference     = Some(false),
        chargesBeforeMaxAccountingDate    = Some(false)
      ).isEligible shouldBe true
    }

    "when optional fields are not populated" in {
      EligibilityRules(
        hasRlsOnAddress                   = false,
        markedAsInsolvent                 = false,
        isLessThanMinDebtAllowance        = false,
        isMoreThanMaxDebtAllowance        = false,
        disallowedChargeLockTypes         = false,
        existingTTP                       = false,
        chargesOverMaxDebtAge             = None,
        ineligibleChargeTypes             = false,
        missingFiledReturns               = false,
        hasInvalidInterestSignals         = None,
        dmSpecialOfficeProcessingRequired = None,
        noDueDatesReached                 = false,
        cannotFindLockReason              = None,
        creditsNotAllowed                 = None,
        isMoreThanMaxPaymentReference     = None,
        chargesBeforeMaxAccountingDate    = None
      ).isEligible shouldBe true
    }

  }
}
