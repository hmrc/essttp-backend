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

import cats.syntax.either._
import enumeratum._
import play.api.mvc.{PathBindable, QueryStringBindable}

import java.util.Locale
import scala.collection.immutable

sealed trait TaxRegime extends EnumEntry, Product, Serializable derives CanEqual

object TaxRegime extends Enum[TaxRegime] {

  given pathBindable: PathBindable[TaxRegime] = new PathBindable[TaxRegime] {
    override def bind(key: String, value: String): Either[String, TaxRegime] =
      TaxRegime.withNameInsensitiveEither(value).leftMap(_.toString)

    override def unbind(key: String, value: TaxRegime): String =
      value.entryName.toLowerCase(Locale.UK)
  }

  given queryStringBindable: QueryStringBindable[TaxRegime] = new QueryStringBindable[TaxRegime] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, TaxRegime]] =
      params.get(key).map {
        _.toList match {
          case regime :: Nil => TaxRegime.withNameInsensitiveEither(regime).leftMap(_.toString)
          case _             => Left(s"Could not read tax regime from query parameter values ${params.toString}")
        }
      }

    override def unbind(key: String, value: TaxRegime): String =
      s"$key=${value.entryName.toLowerCase(Locale.UK)}"
  }

  /** Tax regime for Employers' Pay as you earn (Epaye)
    */
  case object Epaye extends TaxRegime

  /** Tax regime for Value Added Tax (Vat)
    */
  case object Vat extends TaxRegime

  /** Tax regime for Self Assessment (Sa)
    */
  case object Sa extends TaxRegime

  /** Tax regime for Simple Assessment (Simp)
    */
  case object Simp extends TaxRegime

  override val values: immutable.IndexedSeq[TaxRegime] = findValues
}
