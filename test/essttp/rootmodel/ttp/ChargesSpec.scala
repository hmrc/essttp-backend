/*
 * Copyright 2024 HM Revenue & Customs
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

import essttp.rootmodel.AmountInPence
import essttp.rootmodel.ttp.affordablequotes.DueDate
import essttp.rootmodel.ttp.eligibility._
import play.api.libs.json.{JsSuccess, Json}
import testsupport.Givens.{canEqualJsResult, canEqualJsValue}
import testsupport.UnitSpec

import java.time.LocalDate

class ChargesSpec extends UnitSpec {

  val reusableDateAsString: String = "2022-05-17"
  val reusableDate: LocalDate      = LocalDate.parse(reusableDateAsString)

  val json = Json.parse(
    """
      |        {
      |          "chargeType": "VAT Return Debit Charge",
      |          "mainType": "VAT Return Debit Charge",
      |          "mainTrans": "4700",
      |          "subTrans": "1174",
      |          "outstandingAmount": 148781,
      |          "interestStartDate": "2022-05-17",
      |          "dueDate": "2022-05-17",
      |          "accruedInterest": 1597,
      |          "ineligibleChargeType": false,
      |          "chargeOverMaxDebtAge": false,
      |          "locks": [
      |            {
      |              "lockType": "Payment",
      |              "lockReason": "Risk/Fraud",
      |              "disallowedChargeLockType": false
      |            }
      |          ],
      |          "dueDateNotReached": false,
      |          "isInterestBearingCharge": false,
      |          "useChargeReference": false,
      |          "chargeBeforeMaxAccountingDate": false,
      |          "ddInProgress": false,
      |          "chargeSource": "CESA",
      |          "parentChargeReference": "XW006559808862",
      |          "parentMainTrans": "4700",
      |          "originalCreationDate": "2022-05-17",
      |          "tieBreaker": "xyz",
      |          "originalTieBreaker": "xyz",
      |          "saTaxYearEnd": "2022-05-17",
      |          "creationDate": "2022-05-17",
      |          "originalChargeType": "VAT Return Debit Charge"
      |        }
      |""".stripMargin
  )

  val expectedCharges: Charges = Charges(
    chargeType = ChargeType("VAT Return Debit Charge"),
    mainType = MainType("VAT Return Debit Charge"),
    mainTrans = MainTrans("4700"),
    subTrans = SubTrans("1174"),
    outstandingAmount = OutstandingAmount(AmountInPence(148781)),
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
    isInterestBearingCharge = Some(IsInterestBearingCharge(value = false)),
    useChargeReference = Some(UseChargeReference(value = false)),
    chargeBeforeMaxAccountingDate = Some(ChargeBeforeMaxAccountingDate(value = false)),
    ddInProgress = Some(DdInProgress(value = false)),
    chargeSource = Some(ChargeSource("CESA")),
    parentChargeReference = Some(ParentChargeReference("XW006559808862")),
    parentMainTrans = Some(ParentMainTrans("4700")),
    originalCreationDate = Some(OriginalCreationDate(reusableDate)),
    tieBreaker = Some(TieBreaker("xyz")),
    originalTieBreaker = Some(OriginalTieBreaker("xyz")),
    saTaxYearEnd = Some(SaTaxYearEnd(reusableDate)),
    creationDate = Some(CreationDate(reusableDate)),
    originalChargeType = Some(OriginalChargeType("VAT Return Debit Charge"))
  )

  "charges object should" - {
    "validate json into charges object" in {
      json.validate[Charges] shouldBe JsSuccess(expectedCharges)
    }

    "convert the charges object into a valid json" in {
      Json.toJson(expectedCharges) shouldBe json
    }

  }
}
