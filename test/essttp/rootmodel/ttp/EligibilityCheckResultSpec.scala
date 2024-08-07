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

package essttp.rootmodel.ttp

import essttp.rootmodel.Email
import essttp.rootmodel.ttp.eligibility.{Address, ContactDetail, CustomerDetail}
import testsupport.UnitSpec
import testsupport.testdata.TdAll
import uk.gov.hmrc.crypto.Sensitive.SensitiveString

class EligibilityCheckResultSpec extends UnitSpec {

  "EligibilityCheckResult should have" - {

    "isEligible" in {
      TdAll.eligibleEligibilityCheckResultSa.isEligible shouldBe true
      TdAll.ineligibleEligibilityCheckResultSa.isEligible shouldBe false
    }

    "email when" - {

      "there are no emails" in {
        TdAll.eligibleEligibilityCheckResultSa.copy(customerDetails = None, addresses = None).email shouldBe None
        TdAll.eligibleEligibilityCheckResultSa.copy(customerDetails = Some(List.empty), addresses = None).email shouldBe None
        TdAll.eligibleEligibilityCheckResultSa.copy(customerDetails = None, addresses = Some(List(Address(None, None, None, None, None, None, Some(List.empty), None, None, None)))).email shouldBe None
        TdAll.eligibleEligibilityCheckResultSa.copy(customerDetails = None, addresses = Some(List(Address(None, None, None, None, None, None, Some(List(ContactDetail(None, None, None, None, None))), None, None, None)))).email shouldBe None
      }

      "there are emails in customerDetails" in {
        val expectedEmail = Email(SensitiveString("abc@email.com"))
        TdAll.eligibleEligibilityCheckResultSa.copy(
          customerDetails = Some(List(
            CustomerDetail(None, None, None, None, None, None, None, None),
            CustomerDetail(Some(expectedEmail), None, None, None, None, None, None, None),
            CustomerDetail(Some(Email(SensitiveString("xyz@email.com"))), None, None, None, None, None, None, None)
          )), addresses = None
        ).email shouldBe Some(expectedEmail)
      }

      "there are no emails in customerDetails, but there are emails in addresses" in {
        val expectedEmail = Email(SensitiveString("abc@email.com"))
        TdAll.eligibleEligibilityCheckResultSa.copy(customerDetails = None, addresses = Some(List(
          Address(None, None, None, None, None, None, Some(List(ContactDetail(None, None, None, None, None))), None, None, None),
          Address(None, None, None, None, None, None, Some(List(ContactDetail(None, None, None, Some(Email(SensitiveString("abc@email.com"))), None))), None, None, None),
          Address(None, None, None, None, None, None, Some(List(ContactDetail(None, None, None, Some(Email(SensitiveString("xyz@email.com"))), None))), None, None, None)
        ))).email shouldBe Some(expectedEmail)
      }
    }

  }

}
