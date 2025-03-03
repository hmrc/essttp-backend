/*
 * Copyright 2024 HM Revenue & Customs
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

import testsupport.UnitSpec

class TaxRegimeSpec extends UnitSpec {

  "TaxRegime must" - {

    "have a PathBindable" in {
      TaxRegime.pathBindable.bind("key", "epaye") shouldBe Right(TaxRegime.Epaye)
      TaxRegime.pathBindable.bind("key", "vat") shouldBe Right(TaxRegime.Vat)
      TaxRegime.pathBindable.bind("key", "sa") shouldBe Right(TaxRegime.Sa)
      TaxRegime.pathBindable.bind("key", "other") shouldBe a[Left[_, _]]

      TaxRegime.pathBindable.unbind("key", TaxRegime.Epaye) shouldBe "epaye"
      TaxRegime.pathBindable.unbind("key", TaxRegime.Vat) shouldBe "vat"
      TaxRegime.pathBindable.unbind("key", TaxRegime.Sa) shouldBe "sa"
    }

    "have a QueryStringBindable" in {
      TaxRegime.queryStringBindable.bind("key", Map("key" -> Seq("epaye"))) shouldBe Some(Right(TaxRegime.Epaye))
      TaxRegime.queryStringBindable.bind("key", Map("key" -> Seq("vat"))) shouldBe Some(Right(TaxRegime.Vat))
      TaxRegime.queryStringBindable.bind("key", Map("key" -> Seq("sa"))) shouldBe Some(Right(TaxRegime.Sa))
      TaxRegime.queryStringBindable.bind("key", Map("otherKey" -> Seq("epaye"))) shouldBe None
      TaxRegime.queryStringBindable.bind("key", Map("key" -> Seq("unknown"))).getOrElse(fail()) shouldBe a[Left[_, _]]
      TaxRegime.queryStringBindable
        .bind("key", Map("key" -> Seq("epaye", "epaye")))
        .getOrElse(fail()) shouldBe a[Left[_, _]]

      TaxRegime.queryStringBindable.unbind("key", TaxRegime.Epaye) shouldBe "key=epaye"
      TaxRegime.queryStringBindable.unbind("key", TaxRegime.Vat) shouldBe "key=vat"
      TaxRegime.queryStringBindable.unbind("key", TaxRegime.Sa) shouldBe "key=sa"
    }

  }

}
