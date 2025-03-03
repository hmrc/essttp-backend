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

import essttp.rootmodel.{BackUrl, ReturnUrl}
import essttp.utils.DerivedJson
import essttp.utils.DerivedJson.Circe.formatToCodec
import io.circe.generic.semiauto.deriveCodec
import play.api.libs.json.*

import scala.util.Try

/** Start Journey (Sj) Request
  */
sealed trait SjRequest

object SjRequest {

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  given OFormat[SjRequest] =
    DerivedJson.Circe.format(deriveCodec[SjRequest])

  def isAbsoluteUrl(urlStr: String): Boolean = Try(java.net.URI.create(urlStr).isAbsolute).getOrElse(false)

  /** Marking trait aggregating all Epaye [[SjRequest]]s
    */
  sealed trait Epaye extends SjRequest { self: SjRequest => }

  /** SjRequest for Epaye tax regime
    */
  object Epaye {

    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    given OFormat[SjRequest.Epaye] =
      DerivedJson.Circe.format(deriveCodec[SjRequest.Epaye])

    /** Start Journey (Sj) Request for Epaye (Employers' Pay as you earn) used by [[Origin]]s which provide only back
      * and return urls
      */
    final case class Simple(
      returnUrl: ReturnUrl,
      backUrl:   BackUrl
    ) extends SjRequest
        with Epaye

    object Simple {
      @SuppressWarnings(Array("org.wartremover.warts.Any"))
      given OFormat[Simple] = Json.format
    }

    /** Start Journey (Sj) Request for Epaye (Employers' Pay as you earn) It is used by origins which doesn't provide
      * any data
      */
    final case class Empty() extends SjRequest with Epaye

    object Empty {
      given OFormat[Empty] = OFormat[Empty]((_: JsValue) => JsSuccess(Empty()), (_: Empty) => Json.obj())
    }
  }

  /** Marking trait aggregating all Vat [[SjRequest]]s
    */
  sealed trait Vat extends SjRequest { self: SjRequest => }

  /** SjRequest for Vat tax regime
    */
  object Vat {

    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    given OFormat[SjRequest.Vat] =
      DerivedJson.Circe.format(deriveCodec[SjRequest.Vat])

    /** Start Journey (Sj) Request for Value Added Tax (Vat)
      */
    final case class Simple(
      returnUrl: ReturnUrl,
      backUrl:   BackUrl
    ) extends SjRequest
        with Vat

    object Simple {
      @SuppressWarnings(Array("org.wartremover.warts.Any"))
      given OFormat[Simple] = Json.format
    }

    /** Start Journey (Sj) Request for VAT (Value Added Tax) It is used by origins which doesn't provide any data
      */
    final case class Empty() extends SjRequest with Vat

    object Empty {
      given OFormat[Empty] = OFormat[Empty]((_: JsValue) => JsSuccess(Empty()), (_: Empty) => Json.obj())
    }
  }

  /** Marking trait aggregating all Sa [[SjRequest]]s
    */
  sealed trait Sa extends SjRequest {
    self: SjRequest =>
  }

  /** SjRequest for Sa tax regime
    */
  object Sa {

    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    given OFormat[SjRequest.Sa] =
      DerivedJson.Circe.format(deriveCodec[SjRequest.Sa])

    /** Start Journey (Sj) Request for Sa (self assessment) used by [[Origin]]s which provide only back and return urls
      */
    final case class Simple(
      returnUrl: ReturnUrl,
      backUrl:   BackUrl
    ) extends SjRequest
        with Sa

    object Simple {
      @SuppressWarnings(Array("org.wartremover.warts.Any"))
      given OFormat[Simple] = Json.format
    }

    /** Start Journey (Sj) Request for Sa (self assessment) It is used by origins which doesn't provide any data
      */
    final case class Empty() extends SjRequest with Sa

    object Empty {
      given OFormat[Empty] = OFormat[Empty]((_: JsValue) => JsSuccess(Empty()), (_: Empty) => Json.obj())
    }
  }

  /** Marking trait aggregating all Simp [[SjRequest]]s
    */
  sealed trait Simp extends SjRequest {
    self: SjRequest =>
  }

  /** SjRequest for Simp tax regime
    */
  object Simp {

    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    given OFormat[SjRequest.Simp] =
      DerivedJson.Circe.format(deriveCodec[SjRequest.Simp])

    /** Start Journey (Sj) Request for Simp (simple assessment) used by [[Origin]]s which provide only back and return
      * urls
      */
    final case class Simple(
      returnUrl: ReturnUrl,
      backUrl:   BackUrl
    ) extends SjRequest
        with Simp

    object Simple {
      @SuppressWarnings(Array("org.wartremover.warts.Any"))
      given OFormat[Simple] = Json.format
    }

    /** Start Journey (Sj) Request for Simp (simple assessment) It is used by origins which doesn't provide any data
      */
    final case class Empty() extends SjRequest with Simp

    object Empty {
      given OFormat[Empty] = OFormat[Empty]((_: JsValue) => JsSuccess(Empty()), (_: Empty) => Json.obj())
    }
  }

}
