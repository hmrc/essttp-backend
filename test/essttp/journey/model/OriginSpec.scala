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

package essttp.journey.model

import play.api.libs.json.{JsString, Json}
import testsupport.UnitSpec
import testsupport.Givens.canEqualJsValue

class OriginSpec extends UnitSpec {
  "serialise from Origin" - {
    "Epaye" in {
      Json.toJson[Origin](Origins.Epaye.GovUk) shouldBe JsString("Origins.Epaye.GovUk")
      Json.toJson[Origin](Origins.Epaye.DetachedUrl) shouldBe JsString("Origins.Epaye.DetachedUrl")
      Json.toJson[Origin](Origins.Epaye.Bta) shouldBe JsString("Origins.Epaye.Bta")
      Json.toJson[Origin](Origins.Epaye.EpayeService) shouldBe JsString("Origins.Epaye.EpayeService")
    }
    "Vat" in {
      Json.toJson[Origin](Origins.Vat.GovUk) shouldBe JsString("Origins.Vat.GovUk")
      Json.toJson[Origin](Origins.Vat.DetachedUrl) shouldBe JsString("Origins.Vat.DetachedUrl")
      Json.toJson[Origin](Origins.Vat.Bta) shouldBe JsString("Origins.Vat.Bta")
      Json.toJson[Origin](Origins.Vat.VatService) shouldBe JsString("Origins.Vat.VatService")
    }
  }

  "serialise from specific Origin" - {
    "Epaye" in {
      Json.toJson[Origins.Epaye](Origins.Epaye.GovUk) shouldBe JsString("Origins.Epaye.GovUk")
      Json.toJson[Origins.Epaye](Origins.Epaye.DetachedUrl) shouldBe JsString("Origins.Epaye.DetachedUrl")
      Json.toJson[Origins.Epaye](Origins.Epaye.Bta) shouldBe JsString("Origins.Epaye.Bta")
      Json.toJson[Origins.Epaye](Origins.Epaye.EpayeService) shouldBe JsString("Origins.Epaye.EpayeService")
    }
    "Vat" in {
      Json.toJson[Origins.Vat](Origins.Vat.GovUk) shouldBe JsString("Origins.Vat.GovUk")
      Json.toJson[Origins.Vat](Origins.Vat.DetachedUrl) shouldBe JsString("Origins.Vat.DetachedUrl")
      Json.toJson[Origins.Vat](Origins.Vat.Bta) shouldBe JsString("Origins.Vat.Bta")
    }
  }

  "deserialise as Origin" - {
    "Epaye" in {
      JsString("Origins.Epaye.GovUk").as[Origin] shouldBe Origins.Epaye.GovUk
      JsString("Origins.Epaye.DetachedUrl").as[Origin] shouldBe Origins.Epaye.DetachedUrl
      JsString("Origins.Epaye.Bta").as[Origin] shouldBe Origins.Epaye.Bta
      JsString("Origins.Epaye.EpayeService").as[Origin] shouldBe Origins.Epaye.EpayeService
    }
    "Vat" in {
      JsString("Origins.Vat.GovUk").as[Origin] shouldBe Origins.Vat.GovUk
      JsString("Origins.Vat.DetachedUrl").as[Origin] shouldBe Origins.Vat.DetachedUrl
      JsString("Origins.Vat.Bta").as[Origin] shouldBe Origins.Vat.Bta
      JsString("Origins.Vat.VatService").as[Origin] shouldBe Origins.Vat.VatService
    }
  }

  "deserialise as more specific Origin" - {
    "Epaye" in {
      JsString("Origins.Epaye.GovUk").as[Origins.Epaye] shouldBe Origins.Epaye.GovUk
      JsString("Origins.Epaye.DetachedUrl").as[Origins.Epaye] shouldBe Origins.Epaye.DetachedUrl
      JsString("Origins.Epaye.Bta").as[Origins.Epaye] shouldBe Origins.Epaye.Bta
      JsString("Origins.Epaye.EpayeService").as[Origins.Epaye] shouldBe Origins.Epaye.EpayeService
    }
    "Vat" in {
      JsString("Origins.Vat.GovUk").as[Origins.Vat] shouldBe Origins.Vat.GovUk
      JsString("Origins.Vat.DetachedUrl").as[Origins.Vat] shouldBe Origins.Vat.DetachedUrl
      JsString("Origins.Vat.Bta").as[Origins.Vat] shouldBe Origins.Vat.Bta
      JsString("Origins.Vat.VatService").as[Origins.Vat] shouldBe Origins.Vat.VatService
    }
  }

}
