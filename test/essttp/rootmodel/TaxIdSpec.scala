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

import play.api.libs.json.Json
import testsupport.Givens.canEqualJsValue
import testsupport.UnitSpec

class TaxIdSpec extends UnitSpec {

  "serialize from TaxId" in {
    Json.toJson[TaxId](EmpRef("sialala")) shouldBe Json.parse("""{"EmpRef":{"value":"sialala"}}""")
    Json.toJson[TaxId](Vrn("sialala")) shouldBe Json.parse("""{"Vrn":{"value":"sialala"}}""")
  }

  "deserialise as TaxId" in {
    Json.parse("""{"EmpRef":{"value":"sialala"}}""").as[TaxId] shouldBe EmpRef("sialala")
    Json.parse("""{"Vrn":{"value":"sialala"}}""").as[TaxId] shouldBe Vrn("sialala")
  }

  "serialise from specific TaxId" in {
    Json.toJson[EmpRef](EmpRef("sialala")) shouldBe Json.parse("""{"value":"sialala"}""")
    Json.toJson[Vrn](Vrn("sialala")) shouldBe Json.parse("""{"value":"sialala"}""")
  }

  "deserialise as specific TaxId" in {
    Json.parse("""{"value":"sialala"}""").as[EmpRef] shouldBe EmpRef("sialala")
    Json.parse("""{"value":"sialala"}""").as[Vrn] shouldBe Vrn("sialala")
  }

}
