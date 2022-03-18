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

package essttp.utils

import play.api.libs.json.{JsObject, Json}

object JsonSyntax extends JsonSyntax

trait JsonSyntax {

  @SuppressWarnings(Array("org.wartremover.warts.ExplicitImplicitTypes"))
  implicit def toJsonOps(s: String) = new {
    def asJson: JsObject = Json.parse(s) match {
      case d: JsObject => d
      case v           => throw new RuntimeException(s"Cant parse as JsObject: $s ")
    }
  }
}
