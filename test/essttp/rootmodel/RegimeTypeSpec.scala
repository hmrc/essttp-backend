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

import essttp.rootmodel.ttp.RegimeType
import play.api.libs.json.{JsString, Json}
import testsupport.UnitSpec

class RegimeTypeSpec extends UnitSpec {

  "RegimeType should" - {
    "survive round trip de/serialisation" in {
      RegimeType.values.foreach{ regimeType =>
        val expectedJsonString = regimeType match {
          case RegimeType.EPAYE => "PAYE"
          case RegimeType.VAT   => "VATC"
          case RegimeType.SA    => "SA"
          case RegimeType.SIA   => "SIA"
        }

        val jsValue = Json.toJson(regimeType)

        jsValue shouldBe JsString(expectedJsonString) withClue s"serialize ${regimeType.toString}"
        jsValue.as[RegimeType] shouldBe regimeType withClue s"deserialize ${regimeType.toString}"
      }
    }

    "have a method which converts from TaxRegime" in {
      TaxRegime.values.foreach{ taxRegime =>
        val expectedTaxRegime = taxRegime match {
          case TaxRegime.Epaye => RegimeType.EPAYE
          case TaxRegime.Vat   => RegimeType.VAT
          case TaxRegime.Sa    => RegimeType.SA
          case TaxRegime.Sia   => RegimeType.SIA
        }

        RegimeType.fromTaxRegime(taxRegime) shouldBe expectedTaxRegime
      }
    }
  }

}
