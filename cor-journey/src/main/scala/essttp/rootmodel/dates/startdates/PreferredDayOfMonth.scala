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

package essttp.rootmodel.dates.startdates

import play.api.libs.json.{Format, Json}

final case class PreferredDayOfMonth(value: Int) extends AnyVal derives CanEqual

object PreferredDayOfMonth {

  given Format[PreferredDayOfMonth] =
    Json
      .valueFormat[PreferredDayOfMonth]
      .bimap(
        (day: PreferredDayOfMonth) =>
          if (day.value < 1) throw IllegalArgumentException("Day of month can't be less then 1")
          else if (day.value > 28) throw IllegalArgumentException("Day of month can't be greater then 28")
          else day,
        identity
      )

}
