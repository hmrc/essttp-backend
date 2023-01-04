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

package essttp.journey.model.ttp

import essttp.rootmodel.ttp.eligibility.EmailSource
import play.api.libs.json.{JsString, Json}
import testsupport.UnitSpec

class EmailSourceSpec extends UnitSpec {

  "survive round trip de/serialisation" in {
    Json.toJson[EmailSource](EmailSource.`ETMP`) shouldBe JsString("ETMP")
    JsString("ETMP").as[EmailSource] shouldBe EmailSource.`ETMP`
    Json.toJson[EmailSource](EmailSource.`TEMP`) shouldBe JsString("TEMP")
    JsString("TEMP").as[EmailSource] shouldBe EmailSource.`TEMP`
  }
}
