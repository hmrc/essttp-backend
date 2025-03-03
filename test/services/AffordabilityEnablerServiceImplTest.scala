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

package services

import essttp.rootmodel.TaxRegime
import services.AffordabilityEnablerServiceImplTest.Result
import testsupport.ItSpec

import scala.util.Random

class AffordabilityEnablerServiceImplTest extends ItSpec {

  override val overrideConfig: Map[String, Any] = Map(
    "affordability.tax-regimes"                    -> Seq("epaye", "vat", "sa"),
    "affordability.pass-through-percentages.epaye" -> 100,
    "affordability.pass-through-percentages.vat"   -> 0,
    "affordability.pass-through-percentages.sa"    -> 50,
    "affordability.pass-through-percentages.simp"  -> 100
  )

  lazy val service = app.injector.instanceOf[AffordabilityEnablerService]

  "AffordabilityEnablerServiceImpl must" - {

    @SuppressWarnings(Array("org.wartremover.warts.ThreadSleep"))
    def gatherResult(taxRegime: TaxRegime): Result = {
      val (enabled, disabled) = List
        .fill(1000) {
          Thread.sleep(Random.nextLong(3L))
          service.affordabilityEnabled(taxRegime)
        }
        .partition(identity)
      Result(enabled.size, disabled.size, enabled.size + disabled.size)
    }

    def percentage(i: Int, total: Int): Double =
      100.0 * i.toDouble / total.toDouble

    "enable affordability for all requests if the tax regime is enabled and the pass-through percentage is 100" in {
      val result = gatherResult(TaxRegime.Epaye)
      result.affordabilityEnabled shouldBe result.total
      result.affordabilityDisabled shouldBe 0
    }

    "disable affordability for all requests if the tax regime is enabled and the pass-through percentage is 0" in {
      val result = gatherResult(TaxRegime.Vat)
      result.affordabilityDisabled shouldBe result.total
      result.affordabilityEnabled shouldBe 0
    }

    "disable affordability for all requests if the tax regime is disabled" in {
      val result = gatherResult(TaxRegime.Simp)
      result.affordabilityDisabled shouldBe result.total
      result.affordabilityEnabled shouldBe 0
    }

    "enable affordability for some requests if the tax regime is enabled and the pass-through percentage is between 0 and 100" in {
      val result = gatherResult(TaxRegime.Sa)

      percentage(result.affordabilityEnabled, result.total) shouldBe 50.0 +- 5.0
      percentage(result.affordabilityDisabled, result.total) shouldBe 50.0 +- 5.0
    }

  }

}

object AffordabilityEnablerServiceImplTest {

  final case class Result(affordabilityEnabled: Int, affordabilityDisabled: Int, total: Int)

}
