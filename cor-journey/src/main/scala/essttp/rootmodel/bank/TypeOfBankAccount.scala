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

package essttp.rootmodel.bank

import cats.Eq
import enumeratum.{Enum, EnumEntry}
import essttp.utils.EnumFormat
import play.api.libs.json.Format

import scala.collection.immutable

sealed trait TypeOfBankAccount extends EnumEntry

object TypeOfBankAccount {
  implicit val format: Format[TypeOfBankAccount] = EnumFormat(TypesOfBankAccount)
  implicit val eq: Eq[TypeOfBankAccount] = Eq.fromUniversalEquals
}

object TypesOfBankAccount extends Enum[TypeOfBankAccount] {

  case object Personal extends TypeOfBankAccount
  case object Business extends TypeOfBankAccount

  override val values: immutable.IndexedSeq[TypeOfBankAccount] = findValues
}
