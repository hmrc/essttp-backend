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

import com.google.inject.{ImplementedBy, Inject, Singleton}
import essttp.rootmodel.TaxRegime
import play.api.Configuration

import java.time.LocalTime
import java.time.temporal.ChronoField

@ImplementedBy(classOf[AffordabilityEnablerServiceImpl])
trait AffordabilityEnablerService {
  def affordabilityEnabled(taxRegime: TaxRegime): Boolean
}

@Singleton
class AffordabilityEnablerServiceImpl @Inject() (config: Configuration) extends AffordabilityEnablerService {

  private val affordabilityEnabledFor: Seq[TaxRegime] =
    config.get[Seq[String]]("affordability.tax-regimes").filter(_.nonEmpty).map(TaxRegime.withNameInsensitive)

  private val passThroughPercentages: Map[TaxRegime, Int] =
    TaxRegime.values.map { regime =>
      val configKey  =
        regime match {
          case TaxRegime.Epaye => "affordability.pass-through-percentages.epaye"
          case TaxRegime.Vat   => "affordability.pass-through-percentages.vat"
          case TaxRegime.Sa    => "affordability.pass-through-percentages.sa"
          case TaxRegime.Simp  => "affordability.pass-through-percentages.simp"
        }
      val percentage = config.get[Int](configKey)

      if (percentage < 0)
        throw new IllegalArgumentException(
          s"Affordability pass-through percentage for ${regime.toString} should not be less than zero"
        )
      else if (percentage > 100)
        throw new IllegalArgumentException(
          s"Affordability pass-through percentage for ${regime.toString} should not be more than 100"
        )
      else
        regime -> percentage
    }.toMap

  def affordabilityEnabled(taxRegime: TaxRegime): Boolean =
    if (affordabilityEnabledFor.contains(taxRegime)) {
      val percentage = passThroughPercentages(taxRegime)

      if (percentage == 0)
        false
      else if (percentage == 100)
        true
      else {
        val timeNowMillis = LocalTime.now().get(ChronoField.MILLI_OF_SECOND)
        // (timeNowMillis % 100) will give a number between 0 and 99. Adding 1 gives us
        // a number between 1 and 100
        (timeNowMillis % 100) + 1 <= percentage
      }
    } else
      false

}
