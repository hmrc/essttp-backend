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

  val eligibleEligibilityRules =
    EligibilityRules(
      hasRlsOnAddress = false,
      markedAsInsolvent = false,
      existingTTP = false,
      missingFiledReturns = false,
      hasInvalidInterestSignals = Some(false),
      dmSpecialOfficeProcessingRequired = Some(false),
      cannotFindLockReason = Some(false),
      creditsNotAllowed = Some(false),
      isMoreThanMaxPaymentReference = Some(false),
      hasInvalidInterestSignalsCESA = Some(false),
      hasDisguisedRemuneration = Some(false),
      hasCapacitor = Some(false),
      dmSpecialOfficeProcessingRequiredCDCS = Some(false),
      isAnMtdCustomer = Some(false),
      dmSpecialOfficeProcessingRequiredCESA = Some(false),
      noMtditsaEnrollment = Some(false),
      allChargeTypeAssessmentsFailed = Some(false),
      noValidPlanAfterAssessments = Some(false)
    )

  "isEligible should work when" - {

    "all fields are populated" in {
      eligibleEligibilityRules.isEligible shouldBe true
    }

    "when optional fields are not populated" in {
      EligibilityRules(
        hasRlsOnAddress = false,
        markedAsInsolvent = false,
        existingTTP = false,
        missingFiledReturns = false,
        hasInvalidInterestSignals = None,
        dmSpecialOfficeProcessingRequired = None,
        cannotFindLockReason = None,
        creditsNotAllowed = None,
        isMoreThanMaxPaymentReference = None,
        hasInvalidInterestSignalsCESA = None,
        hasDisguisedRemuneration = None,
        hasCapacitor = None,
        dmSpecialOfficeProcessingRequiredCDCS = None,
        isAnMtdCustomer = None,
        dmSpecialOfficeProcessingRequiredCESA = None,
        noMtditsaEnrollment = None,
        allChargeTypeAssessmentsFailed = None,
        noValidPlanAfterAssessments = None
      ).isEligible shouldBe true
    }

  }

  "moreThanOneReasonForIneligibility should work when" - {

    "there are no ineligibility reasons" in {
      eligibleEligibilityRules.moreThanOneReasonForIneligibility shouldBe false
    }

    "there is one ineligibility reason" in {
      eligibleEligibilityRules.copy(hasRlsOnAddress = true).moreThanOneReasonForIneligibility shouldBe false
    }

    "there is more than one ineligibility reason" in {
      eligibleEligibilityRules
        .copy(hasRlsOnAddress = true, markedAsInsolvent = true)
        .moreThanOneReasonForIneligibility shouldBe true
    }

    "there is one ineligibility reason alongside 'allChargeTypeAssessmentsFailed'" in {
      eligibleEligibilityRules
        .copy(hasRlsOnAddress = true, allChargeTypeAssessmentsFailed = Some(true))
        .moreThanOneReasonForIneligibility shouldBe false
    }

  }

  "atLeastOneReasonForIneligibility should work when" - {

    "there are no ineligibility reasons" in {
      eligibleEligibilityRules.atLeastOneReasonForIneligibility shouldBe false
    }

    "there is one ineligibility reason" in {
      eligibleEligibilityRules.copy(hasRlsOnAddress = true).atLeastOneReasonForIneligibility shouldBe true
    }

    "there is more than one ineligibility reason" in {
      eligibleEligibilityRules
        .copy(hasRlsOnAddress = true, markedAsInsolvent = true)
        .atLeastOneReasonForIneligibility shouldBe true
    }

    "the only ineligibility reason is 'allChargeTypeAssessmentsFailed'" in {
      eligibleEligibilityRules
        .copy(allChargeTypeAssessmentsFailed = Some(true))
        .atLeastOneReasonForIneligibility shouldBe false
    }

  }

  "EligibilityRules JSON serialization" - {

    "should serialize and deserialize to/from a flat JSON structure correctly" in {
      val eligibilityRules = EligibilityRules(
        hasRlsOnAddress = true,
        markedAsInsolvent = true,
        existingTTP = false,
        missingFiledReturns = true,
        hasInvalidInterestSignals = Some(false),
        dmSpecialOfficeProcessingRequired = Some(true),
        cannotFindLockReason = Some(false),
        creditsNotAllowed = Some(false),
        isMoreThanMaxPaymentReference = Some(true),
        hasInvalidInterestSignalsCESA = Some(false),
        hasDisguisedRemuneration = Some(true),
        hasCapacitor = Some(false),
        dmSpecialOfficeProcessingRequiredCDCS = Some(true),
        isAnMtdCustomer = Some(false),
        dmSpecialOfficeProcessingRequiredCESA = Some(true),
        noMtditsaEnrollment = Some(false),
        allChargeTypeAssessmentsFailed = Some(false),
        noValidPlanAfterAssessments = Some(false)
      )

      val expectedJson = Json.obj(
        "hasRlsOnAddress"                       -> true,
        "markedAsInsolvent"                     -> true,
        "existingTTP"                           -> false,
        "missingFiledReturns"                   -> true,
        "hasInvalidInterestSignals"             -> false,
        "dmSpecialOfficeProcessingRequired"     -> true,
        "cannotFindLockReason"                  -> false,
        "creditsNotAllowed"                     -> false,
        "isMoreThanMaxPaymentReference"         -> true,
        "hasInvalidInterestSignalsCESA"         -> false,
        "hasDisguisedRemuneration"              -> true,
        "hasCapacitor"                          -> false,
        "dmSpecialOfficeProcessingRequiredCDCS" -> true,
        "isAnMtdCustomer"                       -> false,
        "dmSpecialOfficeProcessingRequiredCESA" -> true,
        "noMtditsaEnrollment"                   -> false,
        "allChargeTypeAssessmentsFailed"        -> false,
        "noValidPlanAfterAssessments"           -> false
      )

      val json = Json.toJson(eligibilityRules)
      json shouldBe expectedJson

      val deserialized = json.as[EligibilityRules]
      deserialized shouldBe eligibilityRules
    }
  }

}
