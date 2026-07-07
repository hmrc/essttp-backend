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

import essttp.rootmodel.ttp.eligibility.AssessmentEligibilityRules
import play.api.libs.json.Json
import testsupport.Givens.canEqualJsValue
import testsupport.UnitSpec

class AssessmentEligibilityRulesSpec extends UnitSpec {

  "isEligible should work when" - {

    "all fields are populated" in {
      AssessmentEligibilityRules(
        isLessThanMinDebtAllowance = false,
        isMoreThanMaxDebtAllowance = false,
        disallowedChargeLockTypes = false,
        chargesOverMaxDebtAge = Some(false),
        ineligibleChargeTypes = false,
        noDueDatesReached = false,
        chargesBeforeMaxAccountingDate = Some(false)
      ).isEligible shouldBe true
    }

    "when optional fields are not populated" in {
      AssessmentEligibilityRules(
        isLessThanMinDebtAllowance = false,
        isMoreThanMaxDebtAllowance = false,
        disallowedChargeLockTypes = false,
        chargesOverMaxDebtAge = None,
        ineligibleChargeTypes = false,
        noDueDatesReached = false,
        chargesBeforeMaxAccountingDate = None
      ).isEligible shouldBe true
    }

  }

  "EligibilityRules JSON serialization" - {

    "should serialize and deserialize to/from a flat JSON structure correctly" in {
      val eligibilityRules = AssessmentEligibilityRules(
        isLessThanMinDebtAllowance = true,
        isMoreThanMaxDebtAllowance = false,
        disallowedChargeLockTypes = false,
        chargesOverMaxDebtAge = Some(false),
        ineligibleChargeTypes = false,
        noDueDatesReached = false,
        chargesBeforeMaxAccountingDate = Some(false)
      )

      val expectedJson = Json.obj(
        "isLessThanMinDebtAllowance"     -> true,
        "isMoreThanMaxDebtAllowance"     -> false,
        "isMoreThanMaxDebtAllowance"     -> false,
        "disallowedChargeLockTypes"      -> false,
        "chargesOverMaxDebtAge"          -> false,
        "ineligibleChargeTypes"          -> false,
        "noDueDatesReached"              -> false,
        "chargesBeforeMaxAccountingDate" -> false
      )

      val json = Json.toJson(eligibilityRules)
      json shouldBe expectedJson

      val deserialized = json.as[AssessmentEligibilityRules]
      deserialized shouldBe eligibilityRules
    }
  }

}
