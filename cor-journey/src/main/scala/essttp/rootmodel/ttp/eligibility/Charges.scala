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
import play.api.libs.json.{Json, OFormat, Reads, Writes}

// OPS-12826 convoluted splitting of case class is due to 22 object limit in scala 2
// TODO after scala 3 upgrade, merge back into one simple charges class
final case class Charges(charges1: Charges1, charges2: Charges2) derives CanEqual

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
  given Writes[Charges] = Writes { charge =>
    Json.obj(
      "chargeType"                    -> charge.charges1.chargeType,
      "mainType"                      -> charge.charges1.mainType,
      "mainTrans"                     -> charge.charges1.mainTrans,
      "subTrans"                      -> charge.charges1.subTrans,
      "outstandingAmount"             -> charge.charges1.outstandingAmount,
      "interestStartDate"             -> charge.charges1.interestStartDate,
      "dueDate"                       -> charge.charges1.dueDate,
      "accruedInterest"               -> charge.charges1.accruedInterest,
      "ineligibleChargeType"          -> charge.charges1.ineligibleChargeType,
      "chargeOverMaxDebtAge"          -> charge.charges1.chargeOverMaxDebtAge,
      "locks"                         -> charge.charges1.locks,
      "dueDateNotReached"             -> charge.charges1.dueDateNotReached,
      "isInterestBearingCharge"       -> charge.charges1.isInterestBearingCharge,
      "useChargeReference"            -> charge.charges2.useChargeReference,
      "chargeBeforeMaxAccountingDate" -> charge.charges2.chargeBeforeMaxAccountingDate,
      "ddInProgress"                  -> charge.charges2.ddInProgress,
      "chargeSource"                  -> charge.charges2.chargeSource,
      "parentChargeReference"         -> charge.charges2.parentChargeReference,
      "parentMainTrans"               -> charge.charges2.parentMainTrans,
      "originalCreationDate"          -> charge.charges2.originalCreationDate,
      "tieBreaker"                    -> charge.charges2.tieBreaker,
      "originalTieBreaker"            -> charge.charges2.originalTieBreaker,
      "saTaxYearEnd"                  -> charge.charges2.saTaxYearEnd,
      "creationDate"                  -> charge.charges2.creationDate,
      "originalChargeType"            -> charge.charges2.originalChargeType
    )
  }

  given Reads[Charges] = { json =>
    for {
      chargeType                    <- (json \ "chargeType").validate[ChargeType]
      mainType                      <- (json \ "mainType").validate[MainType]
      mainTrans                     <- (json \ "mainTrans").validate[MainTrans]
      subTrans                      <- (json \ "subTrans").validate[SubTrans]
      outstandingAmount             <- (json \ "outstandingAmount").validate[OutstandingAmount]
      interestStartDate             <- (json \ "interestStartDate").validateOpt[InterestStartDate]
      dueDate                       <- (json \ "dueDate").validate[DueDate]
      accruedInterest               <- (json \ "accruedInterest").validate[AccruedInterest]
      ineligibleChargeType          <- (json \ "ineligibleChargeType").validate[IneligibleChargeType]
      chargeOverMaxDebtAge          <- (json \ "chargeOverMaxDebtAge").validateOpt[ChargeOverMaxDebtAge]
      locks                         <- (json \ "locks").validateOpt[List[Lock]]
      dueDateNotReached             <- (json \ "dueDateNotReached").validate[Boolean]
      isInterestBearingCharge       <- (json \ "isInterestBearingCharge").validateOpt[IsInterestBearingCharge]
      useChargeReference            <- (json \ "useChargeReference").validateOpt[UseChargeReference]
      chargeBeforeMaxAccountingDate <-
        (json \ "chargeBeforeMaxAccountingDate").validateOpt[ChargeBeforeMaxAccountingDate]
      ddInProgress                  <- (json \ "ddInProgress").validateOpt[DdInProgress]
      chargeSource                  <- (json \ "chargeSource").validateOpt[ChargeSource]
      parentChargeReference         <- (json \ "parentChargeReference").validateOpt[ParentChargeReference]
      parentMainTrans               <- (json \ "parentMainTrans").validateOpt[ParentMainTrans]
      originalCreationDate          <- (json \ "originalCreationDate").validateOpt[OriginalCreationDate]
      tieBreaker                    <- (json \ "tieBreaker").validateOpt[TieBreaker]
      originalTieBreaker            <- (json \ "originalTieBreaker").validateOpt[OriginalTieBreaker]
      saTaxYearEnd                  <- (json \ "saTaxYearEnd").validateOpt[SaTaxYearEnd]
      creationDate                  <- (json \ "creationDate").validateOpt[CreationDate]
      originalChargeType            <- (json \ "originalChargeType").validateOpt[OriginalChargeType]
    } yield Charges(
      Charges1(
        chargeType,
        mainType,
        mainTrans,
        subTrans,
        outstandingAmount,
        interestStartDate,
        dueDate,
        accruedInterest,
        ineligibleChargeType,
        chargeOverMaxDebtAge,
        locks,
        dueDateNotReached,
        isInterestBearingCharge
      ),
      Charges2(
        useChargeReference,
        chargeBeforeMaxAccountingDate,
        ddInProgress,
        chargeSource,
        parentChargeReference,
        parentMainTrans,
        originalCreationDate,
        tieBreaker,
        originalTieBreaker,
        saTaxYearEnd,
        creationDate,
        originalChargeType
      )
    )
  }
}

object Charges1 {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  given OFormat[Charges1] = Json.format[Charges1]
}

object Charges2 {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  given OFormat[Charges2] = Json.format[Charges2]
}
