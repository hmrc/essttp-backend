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

package essttp.testdata

import essttp.journey.model.ttp._
import essttp.rootmodel.{CanPayUpfront, EmpRef}
import essttp.rootmodel.epaye.{Aor, TaxOfficeNumber, TaxOfficeReference}

trait TdEpaye {
  dependencies: TdBase =>

  val taxOfficeNumber: TaxOfficeNumber = TaxOfficeNumber("840")

  val taxOfficeReference: TaxOfficeReference = TaxOfficeReference("GZ00064")

  val empRef: EmpRef = EmpRef("840/GZ00064")

  val aor: Aor = Aor("840PZ00002232")

  val reusableDate: String = "2022-05-17"

  val eligibleEligibilityRules: EligibilityRules = EligibilityRules(hasRlsOnAddress            = false, markedAsInsolvent = false, isLessThanMinDebtAllowance = false, isMoreThanMaxDebtAllowance = false, disallowedChargeLocks = false, existingTTP = false, exceedsMaxDebtAge = false, eligibleChargeType = false, missingFiledReturns = false)

  val hasRlsAddressOn: EligibilityRules = eligibleEligibilityRules.copy(true)

  val eligibleEligibilityCheckResult: EligibilityCheckResult = EligibilityCheckResult(
    idType               = IdType("SSTTP"),
    idNumber             = IdNumber(empRef.value),
    regimeType           = RegimeType("PAYE"),
    processingDate       = ProcessingDate(reusableDate),
    customerPostcodes    = List(CustomerPostcode(PostCode("AA11AA"), PostcodeDate("2020-01-01"))),
    minPlanLengthMonths  = MinPlanLengthMonths(1),
    maxPlanLengthMonths  = MaxPlanLengthMonths(3),
    eligibilityStatus    = EligibilityStatus(OverallEligibilityStatus(true)),
    eligibilityRules     = eligibleEligibilityRules,
    chargeTypeAssessment = List(
      ChargeTypeAssessment(
        taxPeriodFrom         = TaxPeriodFrom("2020-08-13"),
        taxPeriodTo           = TaxPeriodTo("2020-08-14"),
        debtTotalAmount       = DebtTotalAmount(300000),
        disallowedChargeLocks = List(
          DisallowedChargeLocks(
            chargeId              = ChargeId("A00000000001"),
            mainTrans             = MainTrans("mainTrans"),
            mainTransDesc         = MainTransDesc("mainTransDesc"),
            subTrans              = SubTrans("subTrans"),
            subTransDesc          = SubTransDesc("subTransDesc"),
            outstandingDebtAmount = OutstandingDebtAmount(100000),
            interestStartDate     = InterestStartDate("2017-03-07"),
            accruedInterestToDate = AccruedInterestToDate(15.97),
            chargeLocks           = ChargeLocks(
              paymentLock  = PaymentLock(false, ""),
              clearingLock = PaymentLock(false, ""),
              interestLock = PaymentLock(false, ""),
              dunningLock  = PaymentLock(false, "")
            )
          )
        )
      )
    )
  )

  def ineligibleEligibilityCheckResult: EligibilityCheckResult = eligibleEligibilityCheckResult.copy(
    eligibilityStatus = EligibilityStatus(OverallEligibilityStatus(false)),
    eligibilityRules  = hasRlsAddressOn
  )

  val canPayUpfrontYes: CanPayUpfront = CanPayUpfront(true)

  val canPayUpfrontNo: CanPayUpfront = CanPayUpfront(false)

}
