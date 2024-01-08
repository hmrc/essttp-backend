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

/**
 * A temporary structure which binds both [[Origin]] and [[SjRequest]] of the same "type"
 * (Vat to Vat, Sdil to Sdil, Cds to Cds, etc)
 */
sealed trait OriginatedSjRequest {
  def origin: Origin
  def sjRequest: SjRequest
}

object OriginatedSjRequest {

  final case class Epaye(
      override val origin:    Origins.Epaye,
      override val sjRequest: SjRequest.Epaye
  ) extends OriginatedSjRequest

  final case class Vat(
      override val origin:    Origins.Vat,
      override val sjRequest: SjRequest.Vat
  ) extends OriginatedSjRequest

  final case class Sa(
      override val origin:    Origins.Sa,
      override val sjRequest: SjRequest.Sa
  ) extends OriginatedSjRequest

}
