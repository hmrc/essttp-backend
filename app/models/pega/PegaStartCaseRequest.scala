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

package models.pega

import essttp.crypto.CryptoFormat
import essttp.rootmodel.dates.InitialPaymentDate
import essttp.rootmodel.{AmountInPence, CannotPayReason, UpfrontPaymentAmount}
import essttp.rootmodel.ttp.PaymentPlanFrequency
import essttp.rootmodel.ttp.affordablequotes.{AccruedDebtInterest, ChannelIdentifier, DebtItemCharge}
import essttp.rootmodel.ttp.eligibility.CustomerPostcode
import models.pega.PegaStartCaseRequest.Content
import play.api.libs.json.{Json, OWrites}

final case class PegaStartCaseRequest(caseTypeID: String, processID: String, parentCaseID: String, content: Content)

object PegaStartCaseRequest {

  final case class Content(
      UniqueIdentifier:      String,
      UniqueIdentifierType:  String,
      Regime:                String,
      DebtAmount:            AmountInPence,
      `MDTPPropertyMapping`: MDTPropertyMapping
  )

  object Content {
    implicit val writes: OWrites[Content] = Json.writes
  }

  final case class MDTPropertyMapping(
      customerPostcodes:      List[CustomerPostcode],
      initialPaymentDate:     Option[InitialPaymentDate],
      channelIdentifier:      ChannelIdentifier,
      debtItemCharges:        List[DebtItemCharge],
      accruedInterest:        AccruedDebtInterest,
      initialPaymentAmount:   Option[UpfrontPaymentAmount],
      paymentPlanFrequency:   PaymentPlanFrequency,
      UnableToPayReason:      Set[CannotPayReason],
      MakeUpfrontPayment:     Boolean,
      CanDebtBePaidIn6Months: Boolean
  )

  object MDTPropertyMapping {
    implicit val writes: OWrites[MDTPropertyMapping] = {
      implicit val cryptoFormat: CryptoFormat = CryptoFormat.NoOpCryptoFormat
      Json.writes
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val writes: OWrites[PegaStartCaseRequest] = Json.writes

}