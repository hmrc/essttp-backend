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

package util

import java.time.LocalTime
import java.time.temporal.ChronoField

object PercentagePassThrough {

  def shouldPassThrough(passThroughPercentage: Int): Boolean =
    if (passThroughPercentage == 100)
      true
    else if (passThroughPercentage == 0)
      false
    else {
      val timeNowMillis = LocalTime.now().get(ChronoField.MILLI_OF_SECOND)
      // (timeNowMillis % 100) will give a number between 0 and 99. Adding 1 gives us
      // a number between 1 and 100
      (timeNowMillis % 100) + 1 <= passThroughPercentage
    }

}
