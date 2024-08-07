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

import cats.Eq
import julienrf.json.derived
import play.api.libs.json.OFormat

sealed trait CanPayWithinSixMonthsAnswers

object CanPayWithinSixMonthsAnswers {

  case object AnswerNotRequired extends CanPayWithinSixMonthsAnswers

  final case class CanPayWithinSixMonths(value: Boolean) extends CanPayWithinSixMonthsAnswers

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[CanPayWithinSixMonthsAnswers] = derived.oformat[CanPayWithinSixMonthsAnswers]()

  implicit val eq: Eq[CanPayWithinSixMonthsAnswers] = Eq.fromUniversalEquals[CanPayWithinSixMonthsAnswers]

}

