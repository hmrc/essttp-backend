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

package essttp.testdata.epaye

import essttp.rootmodel.epaye.{Aor, TaxOfficeNumber, TaxOfficeReference}
import essttp.rootmodel.ttp._
import essttp.rootmodel.ttp.affordablequotes.DueDate
import essttp.rootmodel.ttp.arrangement.{ArrangementResponse, CustomerReference}
import essttp.rootmodel.ttp.eligibility.{AccruedInterest, ChargeOverMaxDebtAge, ChargeReference, ChargeType, ChargeTypeAssessment, Charges, CustomerPostcode, DebtTotalAmount, DisallowedChargeLockType, EligibilityCheckResult, EligibilityPass, EligibilityStatus, IdType, IdValue, Identification, IneligibleChargeType, InterestStartDate, Lock, LockReason, LockType, MainTrans, MainType, OutstandingAmount, Postcode, PostcodeDate, ProcessingDateTime, SubTrans, TaxPeriodFrom, TaxPeriodTo}
import essttp.rootmodel.{AmountInPence, EmpRef}
import essttp.testdata.TdBase
import uk.gov.hmrc.crypto.Sensitive.SensitiveString

trait TdEpaye {
  dependencies: TdBase =>

  val taxOfficeNumber: TaxOfficeNumber = TaxOfficeNumber("840")

  val taxOfficeReference: TaxOfficeReference = TaxOfficeReference("GZ00064")

  val empRef: EmpRef = EmpRef("864FZ00049")

  val aor: Aor = Aor("123PA44545546")

  val eligibleEligibilityCheckResult: EligibilityCheckResult = eligibility.EligibilityCheckResult(
    processingDateTime              = ProcessingDateTime(reusableDateAsString),
    identification                  = List(
      Identification(
        idType  = IdType("EMPREF"),
        idValue = IdValue(empRef.value)
      ),
      eligibility.Identification(
        idType  = IdType("BROCS"),
        idValue = IdValue("123PA44545546")
      )
    ),
    customerPostcodes               = List(CustomerPostcode(Postcode(SensitiveString("AA11AA")), PostcodeDate("2020-01-01"))),
    regimePaymentFrequency          = PaymentPlanFrequencies.Monthly,
    paymentPlanFrequency            = PaymentPlanFrequencies.Monthly,
    paymentPlanMinLength            = PaymentPlanMinLength(1),
    paymentPlanMaxLength            = PaymentPlanMaxLength(6),
    eligibilityStatus               = EligibilityStatus(EligibilityPass(true)),
    eligibilityRules                = eligibleEligibilityRules,
    chargeTypeAssessment            = List(
      ChargeTypeAssessment(
        taxPeriodFrom   = TaxPeriodFrom("2020-08-13"),
        taxPeriodTo     = TaxPeriodTo("2020-08-14"),
        debtTotalAmount = DebtTotalAmount(AmountInPence(300000)),
        charges         = List(
          Charges(
            chargeType           = ChargeType("InYearRTICharge-Tax"),
            mainType             = MainType("InYearRTICharge(FPS)"),
            chargeReference      = ChargeReference("A00000000001"),
            mainTrans            = MainTrans("mainTrans"),
            subTrans             = SubTrans("subTrans"),
            outstandingAmount    = OutstandingAmount(AmountInPence(100000)),
            dueDate              = DueDate(reusableDate),
            interestStartDate    = Some(InterestStartDate(reusableDate)),
            accruedInterest      = AccruedInterest(AmountInPence(1597)),
            ineligibleChargeType = IneligibleChargeType(false),
            chargeOverMaxDebtAge = ChargeOverMaxDebtAge(false),
            locks                = Some(
              List(
                Lock(
                  lockType                 = LockType("Payment"),
                  lockReason               = LockReason("Risk/Fraud"),
                  disallowedChargeLockType = DisallowedChargeLockType(false)
                )
              )
            ),
            dueDateNotReached    = false
          )
        )
      )
    ),
    customerDetails                 = None,
    regimeDigitalCorrespondence     = None,
    futureChargeLiabilitiesExcluded = false
  )

  def ineligibleEligibilityCheckResult: EligibilityCheckResult = eligibleEligibilityCheckResult.copy(
    eligibilityStatus = EligibilityStatus(EligibilityPass(false)),
    eligibilityRules  = hasRlsAddressOn
  )

  val arrangementResponse: ArrangementResponse = ArrangementResponse(ProcessingDateTime(reusableDateAsString), CustomerReference(aor.value))

}
