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

package essttp

import essttp.rootmodel.AmountInPence
import play.api.libs.json._
import testsupport.UnitSpec

class AmountInPenceSpec extends UnitSpec {

  "AmountInPence" - {

    ".formatInPounds should show amounts with decimal" in {
      AmountInPence(20099).formatInPounds shouldBe "£200.99"
      AmountInPence(200099).formatInPounds shouldBe "£2,000.99"
    }

    ".gdsFormatInPounds should show amounts without decimals when they are .00" in {
      AmountInPence(20099).gdsFormatInPounds shouldBe "£200.99"
      AmountInPence(20000).gdsFormatInPounds shouldBe "£200"
    }

    ".formatInDecimal should show amounts in decimal" in {
      AmountInPence(20099).formatInDecimal shouldBe "200.99"
      AmountInPence(200099).formatInDecimal shouldBe "2,000.99"
    }

    ".inPounds converts BigDecimal correctly" in {
      AmountInPence(20099).inPounds shouldBe BigDecimal("200.99")
    }

    "is greater then (>)" in {
      AmountInPence(1) > AmountInPence(2) shouldBe false
      AmountInPence(2) > AmountInPence(2) shouldBe false
      AmountInPence(3) > AmountInPence(2) shouldBe true
    }

    "addition (.+)" in {
      AmountInPence(1) + AmountInPence(0) shouldBe AmountInPence(1)
      AmountInPence(2) + AmountInPence(2) shouldBe AmountInPence(4)
    }

    "subtraction (.-)" in {
      AmountInPence(1) - AmountInPence(0) shouldBe AmountInPence(1)
      AmountInPence(0) - AmountInPence(1) shouldBe AmountInPence(-1)
      AmountInPence(2) - AmountInPence(2) shouldBe AmountInPence(0)
      AmountInPence(4) - AmountInPence(2) shouldBe AmountInPence(2)
    }

    "Survive round trip de/serialisation" in {
      Json.toJson(AmountInPence(1)) shouldBe JsNumber(1)
      Json.parse("1").as[AmountInPence] shouldBe AmountInPence(1)
    }

  }
}
