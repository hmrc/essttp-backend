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
import essttp.utils.EnumFormat
import play.api.libs.json.Format

import scala.collection.immutable

sealed trait Origin extends EnumEntry with Product with Serializable {

  /**
   * Better toString which shows portion of a package
   */
  override def toString: String = {
    val packageName: String = this.getClass.getPackage.getName
    val className: String = this.getClass.getName
    className
      .replaceFirst(packageName + ".", "")
      .replaceAll("\\$", ".")
      .dropRight(1)
  }
}

object Origin {

  implicit val format: Format[Origin] = EnumFormat(Origins)

  implicit class OriginOps(private val o: Origin) extends AnyVal {

    def taxRegime: TaxRegime = o match {
      case _: Origins.Epaye => TaxRegime.Epaye
      case _: Origins.Vat   => TaxRegime.Vat
      case _: Origins.Sa    => TaxRegime.Sa
      case _: Origins.Sia   => TaxRegime.Sia

    }
  }

}

object Origins extends Enum[Origin] {

  /**
   * Marking trait aggregating all Epaye [[Origin]]s
   */
  sealed trait Epaye extends Origin { self: Origin => }

  object Epaye extends Enum[Epaye] {
    implicit val format: Format[Epaye] = EnumFormat(Epaye)

    case object Bta extends Origin with Epaye with BetterName
    case object EpayeService extends Origin with Epaye with BetterName
    case object GovUk extends Origin with Epaye with BetterName

    /**
     * This represents situation when user receives link to the application in whatsapp/email/etc and it's not clear
     * where the journey actually started from.
     */
    case object DetachedUrl extends Origin with Epaye with BetterName

    override def values: immutable.IndexedSeq[Epaye] = findValues
  }

  /**
   * Marking trait aggregating all Vat [[Origin]]s
   */
  sealed trait Vat extends Origin { self: Origin => }

  object Vat extends Enum[Vat] {
    implicit val format: Format[Vat] = EnumFormat(Vat)

    case object Bta extends Origin with Vat with BetterName
    case object VatService extends Origin with Vat with BetterName
    case object GovUk extends Origin with Vat with BetterName
    case object VatPenalties extends Origin with Vat with BetterName

    /**
     * This represents situation when user receives link to the application in whatsapp/email/etc and it's not clear
     * where the journey actually started from.
     */
    case object DetachedUrl extends Origin with Vat with BetterName

    override def values: immutable.IndexedSeq[Vat] = findValues
  }

  /**
   * Marking trait aggregating all Sa [[Origin]]s
   */
  sealed trait Sa extends Origin {
    self: Origin =>
  }

  object Sa extends Enum[Sa] {
    implicit val format: Format[Sa] = EnumFormat(Sa)

    case object Bta extends Origin with Sa with BetterName

    case object Pta extends Origin with Sa with BetterName

    case object Mobile extends Origin with Sa with BetterName

    case object GovUk extends Origin with Sa with BetterName

    /**
     * This represents situation when user receives link to the application in whatsapp/email/etc and it's not clear
     * where the journey actually started from.
     */
    case object DetachedUrl extends Origin with Sa with BetterName

    override def values: immutable.IndexedSeq[Sa] = findValues
  }

  sealed trait Sia extends Origin {
    self: Origin =>
  }

  object Sia extends Enum[Sia] {
    implicit val format: Format[Sia] = EnumFormat(Sia)

    case object Pta extends Origin with Sia with BetterName

    case object Mobile extends Origin with Sia with BetterName

    case object GovUk extends Origin with Sia with BetterName

    /**
     * This represents situation when user receives link to the application in whatsapp/email/etc and it's not clear
     * where the journey actually started from.
     */
    case object DetachedUrl extends Origin with Sia with BetterName

    override def values: immutable.IndexedSeq[Sia] = findValues
  }

  override def values: immutable.IndexedSeq[Origin] = Epaye.values ++ Vat.values ++ Sa.values
}

/**
 * Mixin to provide a better name for origins
 */
trait BetterName extends EnumEntry { self: Origin =>
  override def entryName: String = stableEntryName
  private[this] lazy val stableEntryName: String = self.toString
}
