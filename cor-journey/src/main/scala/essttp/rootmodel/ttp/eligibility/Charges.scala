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

import essttp.rootmodel.ttp._
import essttp.rootmodel.ttp.affordablequotes.DueDate
import play.api.libs.json.{Json, OFormat}

final case class Charges(charges1: Charges1, charges2: Charges2)

final case class Charges1(
    chargeType:              ChargeType,
    mainType:                MainType,
    mainTrans:               MainTrans,
    subTrans:                SubTrans,
    outstandingAmount:       OutstandingAmount,
    interestStartDate:       Option[InterestStartDate],
    dueDate:                 DueDate,
    accruedInterest:         AccruedInterest,
    ineligibleChargeType:    IneligibleChargeType,
    chargeOverMaxDebtAge:    Option[ChargeOverMaxDebtAge],
    locks:                   Option[List[Lock]],
    dueDateNotReached:       Boolean,
    isInterestBearingCharge: Option[IsInterestBearingCharge]
)

final case class Charges2(
    useChargeReference:            Option[UseChargeReference],
    chargeBeforeMaxAccountingDate: Option[ChargeBeforeMaxAccountingDate],
    ddInProgress:                  Option[DdInProgress],
    chargeSource:                  Option[ChargeSource],
    parentChargeReference:         Option[ParentChargeReference],
    parentMainTrans:               Option[ParentMainTrans],
    originalCreationDate:          Option[OriginalCreationDate],
    tieBreaker:                    Option[TieBreaker],
    originalTieBreaker:            Option[OriginalTieBreaker],
    saTaxYearEnd:                  Option[SaTaxYearEnd],
    creationDate:                  Option[CreationDate],
    originalChargeType:            Option[OriginalChargeType]
)

object Charges {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[Charges] = Json.format[Charges]
}

object Charges1 {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[Charges1] = Json.format[Charges1]
}

object Charges2 {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[Charges2] = Json.format[Charges2]
}
