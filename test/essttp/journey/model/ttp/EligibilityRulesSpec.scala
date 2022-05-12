package essttp.journey.model.ttp

import testsupport.UnitSpec

class EligibilityRulesSpec extends UnitSpec {

  "isEligible" in {
    val e = EligibilityRules(false, false, false, false, false, false, false, false, false)
    e.isEligible shouldBe true

  }
}
