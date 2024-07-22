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

package essttp.rootmodel

import enumeratum.{Enum, EnumEntry}
import essttp.utils.EnumFormat
import play.api.libs.json.Format

sealed trait CannotPayReason extends EnumEntry with Product with Serializable

object CannotPayReason extends Enum[CannotPayReason] {

  case object Bankrupt extends CannotPayReason

  case object Bereavement extends CannotPayReason

  case object ChangeToPersonalCircumstances extends CannotPayReason

  case object FloodFireTheft extends CannotPayReason

  case object IllHealth extends CannotPayReason

  case object LocalDisaster extends CannotPayReason

  case object LostReducedBusiness extends CannotPayReason

  case object LowIncome extends CannotPayReason

  case object NationalDisaster extends CannotPayReason

  case object NoProvisions extends CannotPayReason

  case object OverRepayment extends CannotPayReason

  case object Unemployed extends CannotPayReason

  case object Other extends CannotPayReason

  val values: IndexedSeq[CannotPayReason] = findValues

  implicit val format: Format[CannotPayReason] = EnumFormat(CannotPayReason)

}
