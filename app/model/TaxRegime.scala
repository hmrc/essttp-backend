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

package model

import model.TaxID.EmpRef
import play.api.mvc.PathBindable

sealed trait TaxRegime {
  def name: String

  def taxIdOf(idType: IdType, value: String): TaxID

}

object TaxRegime {

  implicit def pathBinder(implicit stringBinder: PathBindable[String]): PathBindable[TaxRegime] = new PathBindable[TaxRegime] {
    override def bind(key: String, value: String): Either[String, TaxRegime] = {
      for {
        regime <- stringBinder.bind(key, value).right
      } yield regimeOf(regime)
    }
    override def unbind(key: String, regime: TaxRegime): String = {
      regime.name.toLowerCase()
    }
  }

  def regimeOf(name: String): TaxRegime = name.toLowerCase() match {
    case "epaye" => EPaye
    case n       => throw new IllegalArgumentException(s"$n is not the name of a tax regime")
  }

  object EPaye extends TaxRegime {
    override def name: String = "EPaye"

    def taxIdOf(idType: IdType, value: String): TaxID = idType match {
      case IdType.EmployeeRef => EmpRef(value)
      case _                  => throw new IllegalArgumentException("not a valid id")
    }
  }

}
