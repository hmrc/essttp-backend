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

package essttp.rootmodel

import cats.Eq
import enumeratum._

import scala.collection.immutable

sealed trait TaxRegime extends EnumEntry with Product with Serializable

object TaxRegime extends Enum[TaxRegime] {

  implicit val eqTaxRegime: Eq[TaxRegime] = Eq.fromUniversalEquals

  /**
   * Tax regime for Employers' Pay as you earn (Epaye)
   */
  case object Epaye extends TaxRegime

  /**
   * Tax regime for Value Added Tax (Vat)
   */
  case object Vat extends TaxRegime

  /**
   * Tax regime for Self Assessment (Sa)
   */
  case object Sa extends TaxRegime

  override val values: immutable.IndexedSeq[TaxRegime] = findValues
}
