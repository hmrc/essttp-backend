/*
 * Copyright 2025 HM Revenue & Customs
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

import com.typesafe.config.{Config, ConfigFactory}
import essttp.rootmodel.TaxRegime
import testsupport.UnitSpec

import scala.util.Random

class RedirectToLegacySaServiceServiceImplSpec extends UnitSpec {

  def config(passThroughPercentage: Int): Config =
    ConfigFactory.parseString(
      s"""
        |  sa-legacy.pass-through-percentage = ${passThroughPercentage.toString}
        |""".stripMargin
    )

  "RedirectToLegacySaServiceServiceImpl.shouldRedirectToLegacySaService should" - {

    @SuppressWarnings(Array("org.wartremover.warts.ThreadSleep"))
    def gatherResults(service: RedirectToLegacySaServiceService, regime: TaxRegime, n: Int): List[Option[Boolean]] =
      List.fill(n) {
        Thread.sleep(Random.nextLong(3L))
        service.shouldRedirectToLegacySaService(regime)
      }

    "throw an IllegalArgumentException when" - {

      "the pass through percentage in config is less than zero" in {
        val exception = intercept[IllegalArgumentException](
          new RedirectToLegacySaServiceServiceImpl(config(-1))
        )
        exception.getMessage shouldBe "SA legacy pass-through percentage should not be less than zero"
      }

      "the pass through percentage in config is more than 100" in {
        val exception = intercept[IllegalArgumentException](
          new RedirectToLegacySaServiceServiceImpl(config(101))
        )
        exception.getMessage shouldBe "SA legacy pass-through percentage should not be more than 100"
      }

    }

    "return None for all tax regimes not equal to SA" in {
      val service = new RedirectToLegacySaServiceServiceImpl(config(50))

      TaxRegime.values.filter(_ != TaxRegime.Sa).foreach { regime =>
        withClue(s"For regime ${regime.toString}") {
          val result = gatherResults(service, regime, 1000)
          result.count(_.isEmpty) shouldBe 1000
        }
      }
    }

    "return Some(false) all the time if the pass-through percentage is 100" in {
      val service = new RedirectToLegacySaServiceServiceImpl(config(100))
      val result  = gatherResults(service, TaxRegime.Sa, 1000)
      result.count(_.contains(false)) shouldBe 1000
    }

    "return Some(true) all the time if the pass-through percentage is 0" in {
      val service = new RedirectToLegacySaServiceServiceImpl(config(0))
      val result  = gatherResults(service, TaxRegime.Sa, 1000)
      result.count(_.contains(true)) shouldBe 1000
    }

    "return a proportion of flags that correspond to the configured pass-through percentage" in {
      val service = new RedirectToLegacySaServiceServiceImpl(config(50))
      val result  = gatherResults(service, TaxRegime.Sa, 1000)
      result.count(_.isEmpty) shouldBe 0
      result.count(_.contains(false)) shouldBe 500 +- 50
      result.count(_.contains(true)) shouldBe 500 +- 50

    }

  }

}
