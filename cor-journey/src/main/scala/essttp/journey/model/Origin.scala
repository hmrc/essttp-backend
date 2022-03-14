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

import julienrf.json.derived
import play.api.libs.json.OFormat

sealed trait Origin {
  def show: String
}

object Origin {

  implicit val format: OFormat[Origin] = derived.oformat[Origin]()

  /**
   * Marking trait aggregating all Epaye [[Origin]]s
   */
  sealed trait Epaye extends Origin { self: Origin => }

  object Epaye {
    implicit val format: OFormat[Origin.Epaye] = derived.oformat[Origin.Epaye]()
    case object Bta extends Origin with Epaye { def show = "Origin.Epaye.Bta" }
    case object GovUk extends Origin with Epaye { def show = "Origin.Epaye.GovUk" }

    /**
     * This represents situation when user receives link to the application in watsapp/email/etc and it's not clear
     * where the journey actually started from.
     */
    case object DetachedUrl extends Origin with Epaye { def show = "Origin.Epaye.DetachedUrl" }
  }

  /**
   * Marking trait aggregating all Vat [[Origin]]s
   */
  sealed trait Vat extends Origin { self: Origin => }

  object Vat {
    implicit val format: OFormat[Origin.Vat] = derived.oformat[Origin.Vat]()
    case object Bta extends Origin with Vat { def show = "Origin.Vat.Bta" }
  }

}
