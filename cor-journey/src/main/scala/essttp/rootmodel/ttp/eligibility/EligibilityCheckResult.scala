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

package essttp.rootmodel.ttp.eligibility

import essttp.crypto.CryptoFormat
import essttp.journey.model.{Journey, JourneyStage}
import essttp.rootmodel.Email
import essttp.rootmodel.ttp.*
import play.api.libs.json.{Json, OFormat}

/** This represents response from the Eligibylity API
  * https://confluence.tools.tax.service.gov.uk/display/DTDT/TTP+Eligibility+API
  */
final case class EligibilityCheckResult(
  processingDateTime:              ProcessingDateTime,
  identification:                  List[Identification],
  invalidSignals:                  Option[List[InvalidSignals]],
  customerPostcodes:               List[CustomerPostcode],
  customerDetails:                 Option[List[CustomerDetail]],
  individualDetails:               Option[IndividualDetails],
  addresses:                       List[Address],
  regimeDigitalCorrespondence:     RegimeDigitalCorrespondence,
  regimePaymentFrequency:          PaymentPlanFrequency,
  paymentPlanFrequency:            PaymentPlanFrequency,
  paymentPlanMinLength:            PaymentPlanMinLength,
  paymentPlanMaxLength:            PaymentPlanMaxLength,
  eligibilityStatus:               EligibilityStatus,
  eligibilityRules:                EligibilityRules,
  futureChargeLiabilitiesExcluded: Boolean,
  chargeTypesExcluded:             Option[Boolean],
  chargeTypeAssessments:           List[ChargeTypeAssessments]
) derives CanEqual

object EligibilityCheckResult {

  private given assessmentCategoryOrder: Ordering[AssessmentCategory] = Ordering.by {
    case AssessmentCategory.Standard            => 1
    case AssessmentCategory.Debts               => 2
    case AssessmentCategory.Liabilities         => 3
    case AssessmentCategory.DebtsAndLiabilities => 4
  }

  extension (e: EligibilityCheckResult) {

    def isEligible: Boolean = e.eligibilityStatus.eligibilityPass.value

    def email: Option[Email] = e.addresses
      .flatMap(_.contactDetails)
      .collectFirst { case ContactDetail(_, _, _, Some(emailAddress), _, _) =>
        emailAddress
      }

    def hasInterestBearingCharge(journey: Journey): Boolean =
      relevantChargeTypeAssessments(journey).chargeTypeAssessment
        .flatMap(_.charges)
        .exists(_.isInterestBearingCharge.exists(_.value))

    // see what combo of assessment categories we have in chargeTypeAssesments and call the appropriate function,
    // throw if a combo of assessment categories is not supported
    // onDebtsAndLiabilities is called with the ChargeTypeAssessments for Debts, Liabilities and DebtsAndLiabilities in that order
    def foldOnAssessmentCategory[A](
      onStandardOnly:        ChargeTypeAssessments => A,
      onDebtsOnly:           ChargeTypeAssessments => A,
      onLiabilitiesOnly:     ChargeTypeAssessments => A,
      onDebtsAndLiabilities: (ChargeTypeAssessments, ChargeTypeAssessments, ChargeTypeAssessments) => A
    ): A =
      e.chargeTypeAssessments.map(c => c.assessmentCategory -> c).sortBy(_._1) match {
        case (AssessmentCategory.Standard, c) :: Nil    => onStandardOnly(c)
        case (AssessmentCategory.Debts, c) :: Nil       => onDebtsOnly(c)
        case (AssessmentCategory.Liabilities, c) :: Nil => onLiabilitiesOnly(c)
        case (AssessmentCategory.Debts, c1) :: (AssessmentCategory.Liabilities, c2) :: (
              AssessmentCategory.DebtsAndLiabilities,
              c3
            ) :: Nil =>
          onDebtsAndLiabilities(c1, c2, c3)
        case other                                      =>
          throw new NotImplementedError(
            s"unsupported combination of assessment categories: (${other.map(_._1.toString).mkString(", ")})"
          )
      }

    // return the ChargeTypeAssessments that is relevant to the current journey stage - the journey must be in a state where
    // the assessment category has been determined otherwise this throws an exception
    def relevantChargeTypeAssessments(journey: Journey): ChargeTypeAssessments =
      journey match {
        case j: JourneyStage.BeforeAssessmentCategoryDetermined =>
          throw new Exception(
            s"Cannot determine relevant charge type assessments before assessment category in journey stage ${journey.stage}"
          )
        case j: JourneyStage.AfterAssessmentCategoryDetermined  =>
          e.chargeTypeAssessments
            .find(_.assessmentCategory == j.assessmentCategory)
            .getOrElse(
              throw new Exception(
                s"Cannot find relevant charge type assessments for assessment category ${j.assessmentCategory.toString} in eligibility check result"
              )
            )
      }

  }

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  given (using CryptoFormat): OFormat[EligibilityCheckResult] =
    Json.format[EligibilityCheckResult]

}
