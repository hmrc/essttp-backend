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

package essttp.journey.model

import essttp.rootmodel.CannotPayReason
import essttp.utils.DerivedJson
import essttp.utils.DerivedJson.Circe.formatToCodec
import io.circe.generic.semiauto.deriveCodec
import play.api.libs.json.OFormat

sealed trait WhyCannotPayInFullAnswers derives CanEqual

object WhyCannotPayInFullAnswers {

  case object AnswerNotRequired extends WhyCannotPayInFullAnswers

  final case class WhyCannotPayInFull(reasons: Set[CannotPayReason]) extends WhyCannotPayInFullAnswers

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  given OFormat[WhyCannotPayInFullAnswers] =
    DerivedJson.Circe.format(deriveCodec[WhyCannotPayInFullAnswers])

}
