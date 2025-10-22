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

import com.google.inject.{ImplementedBy, Inject, Singleton}
import com.typesafe.config.Config
import essttp.rootmodel.TaxRegime
import util.PercentagePassThrough

@ImplementedBy(classOf[RedirectToLegacySaServiceServiceImpl])
trait RedirectToLegacySaServiceService {

  def shouldRedirectToLegacySaService(taxRegime: TaxRegime): Option[Boolean]

}

@Singleton
class RedirectToLegacySaServiceServiceImpl @Inject() (config: Config) extends RedirectToLegacySaServiceService {

  private val saLegacyPassThroughPercentage: Int = {
    val percentage = config.getInt("sa-legacy.pass-through-percentage")

    if (percentage < 0)
      throw new IllegalArgumentException("SA legacy pass-through percentage should not be less than zero")
    else if (percentage > 100)
      throw new IllegalArgumentException("SA legacy pass-through percentage should not be more than 100")
    else
      percentage
  }

  def shouldRedirectToLegacySaService(taxRegime: TaxRegime): Option[Boolean] =
    taxRegime match {
      case TaxRegime.Sa =>
        // "pass through" == continue to this essttp service
        Some(!PercentagePassThrough.shouldPassThrough(saLegacyPassThroughPercentage))

      case _ =>
        None
    }

}
