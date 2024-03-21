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

package testsupport.testdata.sa

import essttp.rootmodel.ttp._
import essttp.rootmodel.ttp.affordablequotes.DueDate
import essttp.rootmodel.ttp.arrangement.{ArrangementResponse, CustomerReference}
import essttp.rootmodel.ttp.eligibility._
import essttp.rootmodel.{AmountInPence, SaUtr}
import testsupport.testdata.TdBase
import uk.gov.hmrc.crypto.Sensitive.SensitiveString

trait TdSa {
  dependencies: TdBase =>

  val saUtr: SaUtr = SaUtr("1234567895")

  val eligibleEligibilityCheckResultSa: EligibilityCheckResult = eligibility.EligibilityCheckResult(
    processingDateTime              = ProcessingDateTime(reusableDateAsString),
    identification                  = List(
      Identification(
        idType  = IdType("UTR"),
        idValue = IdValue(saUtr.value)
      )
    ),
    invalidSignals                  = Some(List(
      InvalidSignals(
        signalType        = "xyz",
        signalValue       = "123",
        signalDescription = "Description"
      )
    )),
    customerPostcodes               = List(CustomerPostcode(Postcode(SensitiveString("AA11AA")), PostcodeDate("2020-01-01"))),
    customerDetails                 = None,
    customerType                    = Some(CustomerTypes.MTDITSA),
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
        chargeReference = ChargeReference("A00000000001"),
        charges         = List(
          Charges(
            chargeType                    = ChargeType("InYearRTICharge-Tax"),
            mainType                      = MainType("InYearRTICharge(FPS)"),
            mainTrans                     = MainTrans("mainTrans"),
            subTrans                      = SubTrans("subTrans"),
            outstandingAmount             = OutstandingAmount(AmountInPence(100000)),
            dueDate                       = DueDate(reusableDate),
            interestStartDate             = Some(InterestStartDate(reusableDate)),
            accruedInterest               = AccruedInterest(AmountInPence(1597)),
            ineligibleChargeType          = IneligibleChargeType(false),
            chargeOverMaxDebtAge          = Some(ChargeOverMaxDebtAge(false)),
            locks                         = Some(
              List(
                Lock(
                  lockType                 = LockType("Payment"),
                  lockReason               = LockReason("Risk/Fraud"),
                  disallowedChargeLockType = DisallowedChargeLockType(false)
                )
              )
            ),
            dueDateNotReached             = false,
            isInterestBearingCharge       = None,
            useChargeReference            = None,
            chargeBeforeMaxAccountingDate = None
          )
        )
      )
    ),
    regimeDigitalCorrespondence     = None,
    futureChargeLiabilitiesExcluded = false
  )

  def ineligibleEligibilityCheckResultSa: EligibilityCheckResult = eligibleEligibilityCheckResultSa.copy(
    eligibilityStatus = EligibilityStatus(EligibilityPass(false)),
    eligibilityRules  = hasRlsAddressOn
  )

  val arrangementResponseSa: ArrangementResponse = ArrangementResponse(ProcessingDateTime(reusableDateAsString), CustomerReference(saUtr.value))

}