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

    def eligibilityCheckResultWithAssessmentCategory(
      assessmentCategories: Seq[AssessmentCategory]
    ): EligibilityCheckResult =
      TdAll.eligibleEligibilityCheckResultSa.copy(
        chargeTypeAssessments = assessmentCategories.toList.map(assessmentCategory =>
          TdAll.chargeTypeAssessmentsStandardSa.copy(
            assessmentCategory = assessmentCategory
          )
        )
      )

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
          .copy(customerDetails = Some(List(CustomerDetail(None, None))), addresses = addressesWithNoEmail)
          .email shouldBe None
        TdAll.eligibleEligibilityCheckResultSa
          .copy(customerDetails = Some(List(CustomerDetail(None, None))), addresses = addressesWithNoContactDetails)
          .email shouldBe None
      }

      "there are emails in addresses" in {
        val expectedEmail = Email(SensitiveString("abc@email.com"))
        TdAll.eligibleEligibilityCheckResultSa
          .copy(
            customerDetails = Some(List(CustomerDetail(None, None))),
            addresses = addressesWithEmailInContactDetails
          )
          .email shouldBe Some(expectedEmail)
      }

      "there are no emails in addresses, but there are emails in customerDetails" in {
        TdAll.eligibleEligibilityCheckResultSa
          .copy(
            customerDetails = Some(List(CustomerDetail(Some(Email(SensitiveString("abc@email.com"))), None))),
            addresses = addressesWithNoContactDetails
          )
          .email shouldBe None
      }
    }

    "hasInterestBearingCharge when" - {

      def eligibilityCheckResultWithInterestBearingCharge(
        isInterestBearingCharge: Option[Boolean]
      ): EligibilityCheckResult =
        TdAll.eligibleEligibilityCheckResultSa.copy(
          chargeTypeAssessments = TdAll.eligibleEligibilityCheckResultSa.chargeTypeAssessments.map(assessments =>
            assessments.copy(
              chargeTypeAssessment = assessments.chargeTypeAssessment.map(assessment =>
                assessment.copy(
                  charges = assessment.charges.map(charge =>
                    charge.copy(
                      isInterestBearingCharge = isInterestBearingCharge.map(IsInterestBearingCharge.apply)
                    )
                  )
                )
              )
            )
          )
        )

      "there are no interest bearing charges" - {
        "when isInterestCharge is None" in {
          val checkResult = eligibilityCheckResultWithInterestBearingCharge(None)
          checkResult.hasInterestBearingCharge shouldBe false
        }
        "when isInterestCharge is false" in {
          val checkResult = eligibilityCheckResultWithInterestBearingCharge(Some(false))
          checkResult.hasInterestBearingCharge shouldBe false
        }

      }

      "there is an interest bearing charge" in {
        val checkResult = eligibilityCheckResultWithInterestBearingCharge(Some(true))
        checkResult.hasInterestBearingCharge shouldBe true
      }

    }

    "foldOnAssessmentCategory when given a valid set of assessment categories" in {
      Seq(
        (Seq(AssessmentCategory.Standard), 1),
        (Seq(AssessmentCategory.Debts), 2),
        (Seq(AssessmentCategory.Liabilities), 3)
      ).foreach { (assessmentCategories, expectedResult) =>
        eligibilityCheckResultWithAssessmentCategory(assessmentCategories)
          .foldOnAssessmentCategory(
            onStandardOnly = _ => 1,
            onDebtsOnly = _ => 2,
            onLiabilitiesOnly = _ => 3
          ) shouldBe expectedResult
      }

    }

    "foldOnAssessmentCategory when given an invalid set of assessment categories" in {
      forEachUnsupportedAssessmentCategories { assessmentCategories =>
        val error = intercept[NotImplementedError](
          eligibilityCheckResultWithAssessmentCategory(assessmentCategories)
            .foldOnAssessmentCategory(
              onStandardOnly = _ => 1,
              onDebtsOnly = _ => 2,
              onLiabilitiesOnly = _ => 3
            )
        )
        error.getMessage shouldBe s"unsupported combination of assessment categories: (${assessmentCategories.map(_.toString).mkString(", ")})"
      }
    }

    "relevanChargeTypeAssessments when" - {
      "given valid assessment categories" in {
        def eligibilityCheckResultWithAssessmentCategory(
          assessmentCategories: Seq[AssessmentCategory]
        ): EligibilityCheckResult =
          TdAll.eligibleEligibilityCheckResultSa.copy(
            chargeTypeAssessments = assessmentCategories.toList.map(assessmentCategory =>
              TdAll.chargeTypeAssessmentsStandardSa.copy(
                assessmentCategory = assessmentCategory
              )
            )
          )

        Seq(
          (Seq(AssessmentCategory.Standard), AssessmentCategory.Standard),
          (Seq(AssessmentCategory.Debts), AssessmentCategory.Debts),
          (Seq(AssessmentCategory.Liabilities), AssessmentCategory.Liabilities)
        ).foreach { (assessmentCategories, expectedAssessmentCategory) =>
          eligibilityCheckResultWithAssessmentCategory(
            assessmentCategories
          ).relevantChargeTypeAssessments shouldBe TdAll.chargeTypeAssessmentsStandardSa.copy(
            assessmentCategory = expectedAssessmentCategory
          )
        }

      }

      "given invalid assessment categories" in {
        forEachUnsupportedAssessmentCategories { assessmentCategories =>
          val error = intercept[NotImplementedError](
            eligibilityCheckResultWithAssessmentCategory(assessmentCategories).relevantChargeTypeAssessments
          )
          error.getMessage shouldBe s"unsupported combination of assessment categories: (${assessmentCategories.map(_.toString).mkString(", ")})"
        }
      }

    }
  }

  def forEachUnsupportedAssessmentCategories[A](f: Seq[AssessmentCategory] => A): Unit = {
    val supportedAssessmentCategories = Seq(
      Set(AssessmentCategory.Standard),
      Set(AssessmentCategory.Debts),
      Set(AssessmentCategory.Liabilities)
    )

    for {
      i                    <- 1 to AssessmentCategory.values.size
      assessmentCategories <- AssessmentCategory.values.combinations(i)
      if !supportedAssessmentCategories.contains(assessmentCategories.toSet)
    } f(assessmentCategories)
  }

}
