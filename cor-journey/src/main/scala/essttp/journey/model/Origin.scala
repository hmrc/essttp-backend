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

package essttp.journey.model

import enumeratum.{Enum, EnumEntry}
import essttp.rootmodel.TaxRegime
import essttp.utils.{EnumFormat, TypeName}
import play.api.libs.json.{Format, JsError, JsSuccess, Reads, Writes}

import scala.collection.immutable
import scala.reflect.Typeable

sealed trait Origin extends EnumEntry with Product with Serializable derives CanEqual {

  /** Better toString which shows portion of a package
    */
  override def toString: String = {
    val packageName: String = this.getClass.getPackage.getName
    val className: String   = this.getClass.getName
    className
      .replaceFirst(packageName + ".", "")
      .replaceAll("\\$", ".")
      .dropRight(1)
  }
}

object Origin {

  given format: Format[Origin] = EnumFormat(Origins)

  inline def subtypeFormat[O <: Origin: Typeable]: Format[O] = Format[O](
    Reads[O](json =>
      format.reads(json).flatMap {
        case o: O  => JsSuccess(o)
        case other => JsError(s"Expected type ${TypeName.of[O]} but got ${other.entryName}")
      }
    ),
    Writes[O](
      format.writes(_)
    )
  )

  extension (o: Origin) {

    def taxRegime: TaxRegime = o match {
      case _: Origins.Epaye => TaxRegime.Epaye
      case _: Origins.Vat   => TaxRegime.Vat
      case _: Origins.Sa    => TaxRegime.Sa
      case _: Origins.Simp  => TaxRegime.Simp

    }
  }

}

object Origins extends Enum[Origin] {

  /** Marking trait aggregating all Epaye [[Origin]]s
    */
  sealed trait Epaye extends Origin

  object Epaye extends Enum[Epaye] {
    given Format[Epaye] = Origin.subtypeFormat[Epaye]

    case object Bta          extends Origin with Epaye with BetterName
    case object EpayeService extends Origin with Epaye with BetterName
    case object GovUk        extends Origin with Epaye with BetterName

    /** This represents situation when user receives link to the application in whatsapp/email/etc and it's not clear
      * where the journey actually started from.
      */
    case object DetachedUrl extends Origin with Epaye with BetterName

    override def values: immutable.IndexedSeq[Epaye] = findValues
  }

  /** Marking trait aggregating all Vat [[Origin]]s
    */
  sealed trait Vat extends Origin

  object Vat extends Enum[Vat] {
    given Format[Vat] = Origin.subtypeFormat[Vat]

    case object Bta          extends Origin with Vat with BetterName
    case object VatService   extends Origin with Vat with BetterName
    case object GovUk        extends Origin with Vat with BetterName
    case object VatPenalties extends Origin with Vat with BetterName

    /** This represents situation when user receives link to the application in whatsapp/email/etc and it's not clear
      * where the journey actually started from.
      */
    case object DetachedUrl extends Origin with Vat with BetterName

    override def values: immutable.IndexedSeq[Vat] = findValues
  }

  sealed trait Simp extends Origin

  object Simp extends Enum[Simp] {
    given Format[Simp] = Origin.subtypeFormat[Simp]

    case object Pta extends Origin with Simp with BetterName

    case object Mobile extends Origin with Simp with BetterName

    case object GovUk extends Origin with Simp with BetterName

    /** This represents situation when user receives link to the application in whatsapp/email/etc and it's not clear
      * where the journey actually started from.
      */
    case object DetachedUrl extends Origin with Simp with BetterName

    override def values: immutable.IndexedSeq[Simp] = findValues
  }

  /** Marking trait aggregating all Sa [[Origin]]s
    */
  sealed trait Sa extends Origin

  object Sa extends Enum[Sa] {
    given Format[Sa] = Origin.subtypeFormat[Sa]

    case object Bta extends Origin with Sa with BetterName

    case object Pta extends Origin with Sa with BetterName

    case object Mobile extends Origin with Sa with BetterName

    case object GovUk extends Origin with Sa with BetterName

    case object ItsaViewAndChange extends Origin with Sa with BetterName

    /** This represents situation when user receives link to the application in whatsapp/email/etc and it's not clear
      * where the journey actually started from.
      */
    case object DetachedUrl extends Origin with Sa with BetterName

    override def values: immutable.IndexedSeq[Sa] = findValues
  }

  override def values: immutable.IndexedSeq[Origin] = Epaye.values ++ Vat.values ++ Sa.values ++ Simp.values
}

/** Mixin to provide a better name for origins
  */
trait BetterName extends EnumEntry { self: Origin =>
  override def entryName: String           = stableEntryName
  private lazy val stableEntryName: String = self.toString
}
