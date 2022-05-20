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

import essttp.rootmodel.{BackUrl, ReturnUrl}
import julienrf.json.derived
import play.api.libs.json._

import scala.util.Try

/**
 * Start Journey (Sj) Request
 */
sealed trait SjRequest

object SjRequest {

  implicit val format: OFormat[SjRequest] = derived.oformat[SjRequest]()

  def isAbsoluteUrl(urlStr: String): Boolean = Try(java.net.URI.create(urlStr).isAbsolute).getOrElse(false)

  /**
   * Marking trait aggregating all Epaye [[SjRequest]]s
   */
  sealed trait Epaye extends SjRequest { self: SjRequest => }

  /**
   * SjRequest for Epaye tax regime
   */
  object Epaye {

    implicit val format: OFormat[SjRequest.Epaye] = derived.oformat[SjRequest.Epaye]()

    /**
     * Start Journey (Sj) Request
     * for Epaye (Employers' Pay as you earn)
     * used by [[Origin]]s which provide only back and return urls
     */
    final case class Simple(
        returnUrl: ReturnUrl,
        backUrl:   BackUrl
    )
      extends SjRequest
      with Epaye

    object Simple {
      implicit val format: OFormat[Simple] = Json.format
    }

    /**
     * Start Journey (Sj) Request
     * for Epaye (Employers' Pay as you earn)
     * It is used by origins which doesn't provide any data
     */
    final case class Empty()
      extends SjRequest
      with Epaye

    object Empty {
      implicit val format: OFormat[Empty] = OFormat[Empty]((_: JsValue) => JsSuccess(Empty()), (_: Empty) => Json.obj())
    }
  }

  /**
   * Marking trait aggregating all Vat [[SjRequest]]s
   */
  sealed trait Vat extends SjRequest { self: SjRequest => }

  /**
   * SjRequest for Vat tax regime
   */
  object Vat {

    implicit val format: OFormat[SjRequest.Vat] = derived.oformat[SjRequest.Vat]()

    /**
     * Start Journey (Sj) Request
     * for Value Added Tax (Vat)
     */
    final case class Simple(
        returnUrl: ReturnUrl,
        backUrl:   BackUrl
    )
      extends SjRequest
      with Vat

    object Simple {
      implicit val format: OFormat[Simple] = Json.format
    }
  }

}

