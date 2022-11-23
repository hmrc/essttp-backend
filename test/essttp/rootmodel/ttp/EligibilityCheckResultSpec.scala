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

import essttp.rootmodel.Email
import essttp.rootmodel.ttp.eligibility.CustomerDetail
import essttp.testdata.TdAll
import testsupport.UnitSpec
import uk.gov.hmrc.crypto.Sensitive.SensitiveString

class EligibilityCheckResultSpec extends UnitSpec {

  "EligibilityCheckResult should have" - {

    "isEligible" in {
      TdAll.eligibleEligibilityCheckResult.isEligible shouldBe true
      TdAll.ineligibleEligibilityCheckResult.isEligible shouldBe false
    }

    "email when" - {

      "there are no emails" in {
        TdAll.eligibleEligibilityCheckResult.copy(customerDetails = None).email shouldBe None
        TdAll.eligibleEligibilityCheckResult.copy(customerDetails = Some(List.empty)).email shouldBe None
      }

      "there are emails" in {
        val expectedEmail = Email(SensitiveString("abc@email.com"))
        TdAll.eligibleEligibilityCheckResult.copy(
          customerDetails = Some(List(
            CustomerDetail(None, None),
            CustomerDetail(Some(expectedEmail), None),
            CustomerDetail(Some(Email(SensitiveString("xyz@email.com"))), None)
          ))
        ).email shouldBe Some(expectedEmail)
      }
    }

  }

}