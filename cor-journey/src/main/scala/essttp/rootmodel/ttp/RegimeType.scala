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

import enumeratum._
import essttp.rootmodel.TaxRegime
import play.api.libs.json.{Format, JsError, JsString, JsSuccess, Reads, Writes}

sealed trait RegimeType extends EnumEntry with Product with Serializable

object RegimeType extends Enum[RegimeType] {

  case object EPAYE extends RegimeType

  case object VAT extends RegimeType

  case object SA extends RegimeType

  case object SIMP extends RegimeType

  def fromTaxRegime(taxRegime: TaxRegime): RegimeType = taxRegime match {
    case TaxRegime.Epaye => EPAYE
    case TaxRegime.Vat   => VAT
    case TaxRegime.Sa    => SA
    case TaxRegime.Simp  => SIMP
  }

  implicit val format: Format[RegimeType] = Format(
    Reads {
      case JsString("PAYE") => JsSuccess(EPAYE)
      case JsString("VATC") => JsSuccess(VAT)
      case JsString("SA")   => JsSuccess(SA)
      case JsString("SIMP") => JsSuccess(SIMP)
      case JsString(other)  => JsError(s"Unknown tax regime type '$other'")
      case other            => JsError(s"Expected JsString but got ${other.getClass.getSimpleName}")
    },
    Writes {
      case EPAYE => JsString("PAYE")
      case VAT   => JsString("VATC")
      case SA    => JsString("SA")
      case SIMP  => JsString("SIMP")
    }
  )

  override def values: IndexedSeq[RegimeType] = findValues

}
