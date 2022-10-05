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

package essttp.rootmodel

import cats.kernel.Eq
import play.api.libs.json.{Format, Json}

final case class IsEmailAddressRequired(value: Boolean) extends AnyVal

object IsEmailAddressRequired {

  implicit val eq: Eq[IsEmailAddressRequired] = Eq.fromUniversalEquals

  implicit def asBoolean(e: IsEmailAddressRequired): Boolean = e.value

  implicit val format: Format[IsEmailAddressRequired] = Json.valueFormat

}
