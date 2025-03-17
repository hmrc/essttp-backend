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
import play.api.libs.json.Json
import testsupport.UnitSpec
import testsupport.Givens.canEqualJsValue

class EligibilityRulesSpec extends UnitSpec {

  "isEligible should work when" - {

    "all fields are populated" in {
      EligibilityRules(
        hasRlsOnAddress = false,
        markedAsInsolvent = false,
        isLessThanMinDebtAllowance = false,
        isMoreThanMaxDebtAllowance = false,
        disallowedChargeLockTypes = false,
        existingTTP = false,
        chargesOverMaxDebtAge = Some(false),
        ineligibleChargeTypes = false,
        missingFiledReturns = false,
        hasInvalidInterestSignals = Some(false),
        dmSpecialOfficeProcessingRequired = Some(false),
        noDueDatesReached = false,
        cannotFindLockReason = Some(false),
        creditsNotAllowed = Some(false),
        isMoreThanMaxPaymentReference = Some(false),
        chargesBeforeMaxAccountingDate = Some(false),
        hasInvalidInterestSignalsCESA = Some(false),
        hasDisguisedRemuneration = Some(false),
        hasCapacitor = Some(false),
        dmSpecialOfficeProcessingRequiredCDCS = Some(false),
        isAnMtdCustomer = Some(false),
        dmSpecialOfficeProcessingRequiredCESA = Some(false),
        noMtditsaEnrollment = Some(false)
      ).isEligible shouldBe true
    }

    "when optional fields are not populated" in {
      EligibilityRules(
        hasRlsOnAddress = false,
        markedAsInsolvent = false,
        isLessThanMinDebtAllowance = false,
        isMoreThanMaxDebtAllowance = false,
        disallowedChargeLockTypes = false,
        existingTTP = false,
        chargesOverMaxDebtAge = None,
        ineligibleChargeTypes = false,
        missingFiledReturns = false,
        hasInvalidInterestSignals = None,
        dmSpecialOfficeProcessingRequired = None,
        noDueDatesReached = false,
        cannotFindLockReason = None,
        creditsNotAllowed = None,
        isMoreThanMaxPaymentReference = None,
        chargesBeforeMaxAccountingDate = None,
        hasInvalidInterestSignalsCESA = None,
        hasDisguisedRemuneration = None,
        hasCapacitor = None,
        dmSpecialOfficeProcessingRequiredCDCS = None,
        isAnMtdCustomer = None,
        dmSpecialOfficeProcessingRequiredCESA = None,
        noMtditsaEnrollment = None
      ).isEligible shouldBe true
    }

  }

  "EligibilityRules JSON serialization" - {

    "should serialize and deserialize to/from a flat JSON structure correctly" in {
      val eligibilityRules = EligibilityRules(
        hasRlsOnAddress = true,
        markedAsInsolvent = true,
        isLessThanMinDebtAllowance = false,
        isMoreThanMaxDebtAllowance = true,
        disallowedChargeLockTypes = false,
        existingTTP = false,
        chargesOverMaxDebtAge = Some(true),
        ineligibleChargeTypes = false,
        missingFiledReturns = true,
        hasInvalidInterestSignals = Some(false),
        dmSpecialOfficeProcessingRequired = Some(true),
        noDueDatesReached = false,
        cannotFindLockReason = Some(false),
        creditsNotAllowed = Some(false),
        isMoreThanMaxPaymentReference = Some(true),
        chargesBeforeMaxAccountingDate = Some(true),
        hasInvalidInterestSignalsCESA = Some(false),
        hasDisguisedRemuneration = Some(true),
        hasCapacitor = Some(false),
        dmSpecialOfficeProcessingRequiredCDCS = Some(true),
        isAnMtdCustomer = Some(false),
        dmSpecialOfficeProcessingRequiredCESA = Some(true),
        noMtditsaEnrollment = Some(false)
      )

      val expectedJson = Json.obj(
        "hasRlsOnAddress"                       -> true,
        "markedAsInsolvent"                     -> true,
        "isLessThanMinDebtAllowance"            -> false,
        "isMoreThanMaxDebtAllowance"            -> true,
        "disallowedChargeLockTypes"             -> false,
        "existingTTP"                           -> false,
        "chargesOverMaxDebtAge"                 -> true,
        "ineligibleChargeTypes"                 -> false,
        "missingFiledReturns"                   -> true,
        "hasInvalidInterestSignals"             -> false,
        "dmSpecialOfficeProcessingRequired"     -> true,
        "noDueDatesReached"                     -> false,
        "cannotFindLockReason"                  -> false,
        "creditsNotAllowed"                     -> false,
        "isMoreThanMaxPaymentReference"         -> true,
        "chargesBeforeMaxAccountingDate"        -> true,
        "hasInvalidInterestSignalsCESA"         -> false,
        "hasDisguisedRemuneration"              -> true,
        "hasCapacitor"                          -> false,
        "dmSpecialOfficeProcessingRequiredCDCS" -> true,
        "isAnMtdCustomer"                       -> false,
        "dmSpecialOfficeProcessingRequiredCESA" -> true,
        "noMtditsaEnrollment"                   -> false
      )

      val json = Json.toJson(eligibilityRules)
      json shouldBe expectedJson

      val deserialized = json.as[EligibilityRules]
      deserialized shouldBe eligibilityRules
    }
  }

}
