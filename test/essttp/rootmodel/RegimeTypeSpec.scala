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

import essttp.rootmodel.ttp.arrangement.RegimeType
import play.api.libs.json.{JsString, Json}
import testsupport.UnitSpec

class RegimeTypeSpec extends UnitSpec {

  "RegimeType should survive round trip de/serialisation" in {
    Seq(
      "PAYE" -> RegimeType("PAYE"),
      "VAT" -> RegimeType("VAT")
    ).foreach { rt =>
        val jsValue = Json.toJson(rt._2)
        jsValue shouldBe JsString(rt._1) withClue s"serialize ${rt.toString()}"
        jsValue.as[RegimeType] shouldBe rt._2 withClue s"deserialize ${rt.toString()}"
      }
  }

}
