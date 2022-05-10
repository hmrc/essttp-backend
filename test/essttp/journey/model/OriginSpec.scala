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

package essttp.journey.model

import play.api.libs.json.{JsString, Json}
import testsupport.UnitSpec

class OriginSpec extends UnitSpec {
  "serialise from Origin" - {
    "Epaye" in {
      Json.toJson[Origin](Origins.Epaye.GovUk) shouldBe JsString("Origins.Epaye.GovUk")
      Json.toJson[Origin](Origins.Epaye.DetachedUrl) shouldBe JsString("Origins.Epaye.DetachedUrl")
      Json.toJson[Origin](Origins.Epaye.Bta) shouldBe JsString("Origins.Epaye.Bta")
    }
    "Vat" in {
      //TODO: Uncomment once Vat is enabled
      //      Json.toJson[Origin](Origins.Vat.Bta) shouldBe JsString("Origins.Vat.Bta")
    }
  }

  "serialise from specific Origin" - {
    "Epaye" in {
      Json.toJson[Origins.Epaye](Origins.Epaye.GovUk) shouldBe JsString("Origins.Epaye.GovUk")
      Json.toJson[Origins.Epaye](Origins.Epaye.DetachedUrl) shouldBe JsString("Origins.Epaye.DetachedUrl")
      Json.toJson[Origins.Epaye](Origins.Epaye.Bta) shouldBe JsString("Origins.Epaye.Bta")
    }
    "Vat" in {
      //TODO: Uncomment once Vat is enabled
      //      Json.toJson[Origins.Vat](Origins.Vat.Bta) shouldBe JsString("Origins.Vat.Bta")
    }
  }

  "deserialise as Origin" - {
    "Epaye" in {
      JsString("Origins.Epaye.GovUk").as[Origin] shouldBe Origins.Epaye.GovUk
      JsString("Origins.Epaye.DetachedUrl").as[Origin] shouldBe Origins.Epaye.DetachedUrl
      JsString("Origins.Epaye.Bta").as[Origin] shouldBe Origins.Epaye.Bta
    }

    "Vat" in {
      //TODO: Uncomment once Vat is enabled
      //      JsString("Origins.Vat.Bta").as[Origin] shouldBe Origins.Vat.Bta
    }
  }

  "deserialise as more specific Origin" - {
    "Epaye" in {
      JsString("Origins.Epaye.GovUk").as[Origins.Epaye] shouldBe Origins.Epaye.GovUk
      JsString("Origins.Epaye.DetachedUrl").as[Origins.Epaye] shouldBe Origins.Epaye.DetachedUrl
      JsString("Origins.Epaye.Bta").as[Origins.Epaye] shouldBe Origins.Epaye.Bta
    }

    "Vat" in {
      //TODO: Uncomment once Vat is enabled
      //      JsString("Origins.Vat.Bta").as[Origins.Vat] shouldBe Origins.Vat.Bta
    }
  }

  "toString" in {
    withClue("it has better name then just 'Bta' so it's easier to distinguish between Bta in Epaye and Bta in Vat") {
      //just few examples
      // TODO: Uncomment once Vat is enabled
      //      Origins.Vat.Bta.toString() shouldBe "Origins.Vat.Bta"
      //      Origins.Epaye.Bta.toString() shouldBe "Origins.Epaye.Bta"
    }
  }

}
