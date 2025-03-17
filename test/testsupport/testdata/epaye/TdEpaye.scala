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

package testsupport.testdata.epaye

import essttp.rootmodel.epaye.{TaxOfficeNumber, TaxOfficeReference}
import essttp.rootmodel.ttp._
import essttp.rootmodel.ttp.affordablequotes.DueDate
import essttp.rootmodel.ttp.arrangement.{ArrangementResponse, CustomerReference}
import essttp.rootmodel.ttp.eligibility._
import essttp.rootmodel.{AmountInPence, Email, EmpRef}
import testsupport.testdata.TdBase
import uk.gov.hmrc.crypto.Sensitive.SensitiveString

import java.time.LocalDate

trait TdEpaye {
  dependencies: TdBase =>

  val taxOfficeNumber: TaxOfficeNumber = TaxOfficeNumber("840")

  val taxOfficeReference: TaxOfficeReference = TaxOfficeReference("GZ00064")

  val empRef: EmpRef = EmpRef("864FZ00049")

  val eligibleEligibilityCheckResultEpaye: EligibilityCheckResult = eligibility.EligibilityCheckResult(
    processingDateTime = ProcessingDateTime(reusableDateAsString),
    identification = List(
      Identification(
        idType = IdType("EMPREF"),
        idValue = IdValue(empRef.value)
      ),
      eligibility.Identification(
        idType = IdType("BROCS"),
        idValue = IdValue("123PA44545546")
      )
    ),
    invalidSignals = None,
    customerPostcodes =
      List(CustomerPostcode(Postcode(SensitiveString("AA11AA")), PostcodeDate(LocalDate.of(2020, 1, 1)))),
    customerDetails = List(
      CustomerDetail(
        emailAddress = None,
        emailSource = None
      )
    ),
    individualDetails = None,
    addresses = List(
      Address(
        addressType = AddressType("Residential"),
        addressLine1 = None,
        addressLine2 = None,
        addressLine3 = None,
        addressLine4 = None,
        rls = None,
        contactDetails = Some(
          ContactDetail(
            telephoneNumber = None,
            fax = None,
            mobile = None,
            emailAddress = Some(Email(SensitiveString("some@email"))),
            emailSource = None,
            altFormat = None
          )
        ),
        postCode = None,
        country = None,
        postcodeHistory = List(
          PostcodeHistory(
            addressPostcode = Postcode(SensitiveString("POSTCODE")),
            postcodeDate = PostcodeDate(LocalDate.now())
          )
        )
      )
    ),
    regimePaymentFrequency = PaymentPlanFrequencies.Monthly,
    paymentPlanFrequency = PaymentPlanFrequencies.Monthly,
    paymentPlanMinLength = PaymentPlanMinLength(1),
    paymentPlanMaxLength = PaymentPlanMaxLength(6),
    eligibilityStatus = EligibilityStatus(EligibilityPass(value = true)),
    eligibilityRules = eligibleEligibilityRules,
    chargeTypeAssessment = List(
      ChargeTypeAssessment(
        taxPeriodFrom = TaxPeriodFrom("2020-08-13"),
        taxPeriodTo = TaxPeriodTo("2020-08-14"),
        debtTotalAmount = DebtTotalAmount(AmountInPence(300000)),
        chargeReference = ChargeReference("A00000000001"),
        charges = List(
          Charges(
            chargeType = ChargeType("InYearRTICharge-Tax"),
            mainType = MainType("InYearRTICharge(FPS)"),
            mainTrans = MainTrans("mainTrans"),
            subTrans = SubTrans("subTrans"),
            outstandingAmount = OutstandingAmount(AmountInPence(100000)),
            dueDate = DueDate(reusableDate),
            interestStartDate = Some(InterestStartDate(reusableDate)),
            accruedInterest = AccruedInterest(AmountInPence(1597)),
            ineligibleChargeType = IneligibleChargeType(value = false),
            chargeOverMaxDebtAge = Some(ChargeOverMaxDebtAge(value = false)),
            locks = Some(
              List(
                Lock(
                  lockType = LockType("Payment"),
                  lockReason = LockReason("Risk/Fraud"),
                  disallowedChargeLockType = DisallowedChargeLockType(value = false)
                )
              )
            ),
            dueDateNotReached = false,
            isInterestBearingCharge = None,
            useChargeReference = None,
            chargeBeforeMaxAccountingDate = None,
            ddInProgress = Some(DdInProgress(value = false)),
            chargeSource = None,
            parentChargeReference = None,
            parentMainTrans = None,
            originalCreationDate = None,
            tieBreaker = None,
            originalTieBreaker = None,
            saTaxYearEnd = None,
            creationDate = None,
            originalChargeType = None
          )
        )
      )
    ),
    regimeDigitalCorrespondence = RegimeDigitalCorrespondence(value = true),
    futureChargeLiabilitiesExcluded = false,
    chargeTypesExcluded = None
  )

  def ineligibleEligibilityCheckResultEpaye: EligibilityCheckResult = eligibleEligibilityCheckResultEpaye.copy(
    eligibilityStatus = EligibilityStatus(EligibilityPass(value = false)),
    eligibilityRules = hasRlsAddressOn
  )

  val arrangementResponseEpaye: ArrangementResponse =
    ArrangementResponse(ProcessingDateTime(reusableDateAsString), CustomerReference("123PA44545546"))

}
