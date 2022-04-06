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

import play.api.mvc.PathBindable

sealed trait TaxID {
  def value: String
}

object TaxID {
  case class EmpRef(value: String) extends TaxID
}

case class IdType private (ordinal: Int, name: String)

object IdType {

  val EmployeeRef = IdType(0, "EMPREF")

  def idTypeOf(name: String): IdType = name.toLowerCase() match {
    case "empref" => IdType.EmployeeRef
    case n        => throw new IllegalArgumentException(s"$n is not the name of an id type")
  }

  implicit def pathBinder(implicit stringBinder: PathBindable[String]): PathBindable[IdType] = new PathBindable[IdType] {
    override def bind(key: String, value: String): Either[String, IdType] = {
      for {
        idType <- stringBinder.bind(key, value).right
      } yield idTypeOf(idType)
    }
    override def unbind(key: String, regime: IdType): String = {
      regime.name.toLowerCase()
    }
  }
}

