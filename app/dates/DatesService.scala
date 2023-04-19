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

package dates

import com.google.inject.{Inject, Singleton}
import essttp.rootmodel.dates.{InitialPayment, InitialPaymentDate}

import java.time.{Clock, LocalDate}

@Singleton
class DatesService @Inject() (clock: Clock) {
  def today(): LocalDate = LocalDate.now(clock)

  def todayPlusDays(numberOfDays: Int): LocalDate = today().plusDays(numberOfDays)

  def initialPaymentDate(initialPayment: InitialPayment, numberOfDaysToAdd: Int): Option[InitialPaymentDate] = initialPayment match {
    case InitialPayment(true)  => Some(InitialPaymentDate(todayPlusDays(numberOfDaysToAdd)))
    case InitialPayment(false) => None
  }
}
