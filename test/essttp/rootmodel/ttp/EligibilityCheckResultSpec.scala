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
import essttp.rootmodel.ttp.eligibility._
import testsupport.UnitSpec
import testsupport.testdata.TdAll
import uk.gov.hmrc.crypto.Sensitive.SensitiveString

import java.time.LocalDate

class EligibilityCheckResultSpec extends UnitSpec {

  "EligibilityCheckResult should have" - {

    "isEligible" in {
      TdAll.eligibleEligibilityCheckResultSa.isEligible shouldBe true
      TdAll.ineligibleEligibilityCheckResultSa.isEligible shouldBe false
    }

    "email when" - {
      val addressesWithNoEmail = List(
        Address(
          AddressType("Residential"),
          None,
          None,
          None,
          None,
          None,
          Some(ContactDetail(None, None, None, None, None, None)),
          None,
          None,
          List(PostcodeHistory(Postcode(SensitiveString("POSTCODE")), PostcodeDate(LocalDate.now)))
        )
      )

      val addressesWithNoContactDetails = List(
        Address(
          AddressType("Residential"),
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          List(PostcodeHistory(Postcode(SensitiveString("POSTCODE")), PostcodeDate(LocalDate.now)))
        )
      )

      val addressesWithEmailInContactDetails = List(
        Address(
          AddressType("Residential"),
          None,
          None,
          None,
          None,
          None,
          Some(ContactDetail(None, None, None, Some(Email(SensitiveString("abc@email.com"))), None, None)),
          None,
          None,
          List(PostcodeHistory(Postcode(SensitiveString("POSTCODE")), PostcodeDate(LocalDate.now)))
        )
      )

      "there are no emails" in {
        TdAll.eligibleEligibilityCheckResultSa
          .copy(customerDetails = List(CustomerDetail(None, None)), addresses = addressesWithNoEmail)
          .email shouldBe None
        TdAll.eligibleEligibilityCheckResultSa
          .copy(customerDetails = List(CustomerDetail(None, None)), addresses = addressesWithNoContactDetails)
          .email shouldBe None
      }

      "there are emails in addresses" in {
        val expectedEmail = Email(SensitiveString("abc@email.com"))
        TdAll.eligibleEligibilityCheckResultSa
          .copy(customerDetails = List(CustomerDetail(None, None)), addresses = addressesWithEmailInContactDetails)
          .email shouldBe Some(expectedEmail)
      }

      "there are no emails in addresses, but there are emails in customerDetails" in {
        TdAll.eligibleEligibilityCheckResultSa
          .copy(
            customerDetails = List(CustomerDetail(Some(Email(SensitiveString("abc@email.com"))), None)),
            addresses = addressesWithNoContactDetails
          )
          .email shouldBe None
      }
    }

    "hasInterestBearingCharge when" - {

      def eligibilityCheckResultWithInterestBearingCharge(isInterestBearingCharge: Option[Boolean]) =
        TdAll.eligibleEligibilityCheckResultSa.copy(
          chargeTypeAssessment = TdAll.eligibleEligibilityCheckResultSa.chargeTypeAssessment.map(assessment =>
            assessment.copy(
              charges = assessment.charges.map(charge =>
                charge.copy(charges1 =
                  charge.charges1.copy(
                    isInterestBearingCharge = isInterestBearingCharge.map(IsInterestBearingCharge.apply)
                  )
                )
              )
            )
          )
        )

      "there are no interest bearing charges" in {
        eligibilityCheckResultWithInterestBearingCharge(None).hasInterestBearingCharge shouldBe false
        eligibilityCheckResultWithInterestBearingCharge(Some(false)).hasInterestBearingCharge shouldBe false

      }

      "there is an interest bearing charge" in {
        eligibilityCheckResultWithInterestBearingCharge(Some(true)).hasInterestBearingCharge shouldBe true
      }

    }

  }

}
