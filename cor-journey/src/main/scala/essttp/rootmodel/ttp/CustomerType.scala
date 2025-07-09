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
import essttp.utils.EnumFormat
import play.api.libs.json.Format

import scala.collection.immutable

sealed trait CustomerType extends EnumEntry {
  val value: String
}

object CustomerType {

  given Format[CustomerType] = EnumFormat(CustomerTypes)
}

object CustomerTypes extends Enum[CustomerType] {

  case object MTDITSA extends CustomerType {
    val value: String = "MTD(ITSA)"
  }

  case object ClassicSATransitioned extends CustomerType {
    val value: String = "Classic SA - Transitioned"
  }

  case object ClassicSANonTransitioned extends CustomerType {
    val value: String = "Classic SA - Non Transitioned"
  }

  override val values: immutable.IndexedSeq[CustomerType] = findValues
}
