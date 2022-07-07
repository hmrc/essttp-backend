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

package essttp.journey.model.ttp

import enumeratum._
import essttp.utils.EnumFormat
import essttp.utils.EnumFormat.Transformation
import org.apache.commons.lang3.StringUtils
import play.api.libs.json.Format

import scala.collection.immutable

sealed trait PaymentPlanFrequency extends EnumEntry

object PaymentPlanFrequency {

  implicit val format: Format[PaymentPlanFrequency] =
    EnumFormat(PaymentPlanFrequencies, Transformation(StringUtils.uncapitalize, StringUtils.capitalize))

}

object PaymentPlanFrequencies extends Enum[PaymentPlanFrequency] {

  case object Monthly extends PaymentPlanFrequency

  override val values: immutable.IndexedSeq[PaymentPlanFrequency] = findValues
}
