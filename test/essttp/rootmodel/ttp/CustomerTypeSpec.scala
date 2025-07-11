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

package essttp.rootmodel.ttp

import play.api.libs.json.Json
import testsupport.Givens.canEqualJsValue
import testsupport.UnitSpec

class CustomerTypeSpec extends UnitSpec {

  given CanEqual[CustomerType, CustomerType] = CanEqual.derived

  "serialize from CustomerType" in {
    Json.toJson[CustomerType](CustomerTypes.MTDITSA) shouldBe Json.parse(""""MTD(ITSA)"""")
    Json.toJson[CustomerType](CustomerTypes.ClassicSATransitioned) shouldBe Json.parse(
      """"Classic SA - Transitioned""""
    )
    Json.toJson[CustomerType](CustomerTypes.ClassicSANonTransitioned) shouldBe Json.parse(
      """"Classic SA - Non Transitioned""""
    )
  }

  "deserialize as CustomerType" in {
    Json.parse(""""MTD(ITSA)"""").as[CustomerType] shouldBe CustomerTypes.MTDITSA
    Json.parse(""""Classic SA - Transitioned"""").as[CustomerType] shouldBe CustomerTypes.ClassicSATransitioned
    Json.parse(""""Classic SA - Non Transitioned"""").as[CustomerType] shouldBe CustomerTypes.ClassicSANonTransitioned
  }

}
