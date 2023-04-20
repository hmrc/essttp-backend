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
import essttp.rootmodel.dates.InitialPaymentDate
import essttp.rootmodel.dates.startdates.{InstalmentStartDate, PreferredDayOfMonth, StartDatesRequest, StartDatesResponse}

import java.time.LocalDate

@Singleton
class StartDatesService @Inject() (datesService: DatesService) {

  private def calculateInstalmentStartDate(preferredDayOfMonth: PreferredDayOfMonth, proposedStartDate: LocalDate): InstalmentStartDate = {
    // if the preferred day of month is before the proposed start date day of month it should be next month on that day
    if (preferredDayOfMonth.value < proposedStartDate.getDayOfMonth) {
      InstalmentStartDate(proposedStartDate.plusMonths(1).withDayOfMonth(preferredDayOfMonth.value))
    } else {
      InstalmentStartDate(proposedStartDate.withDayOfMonth(preferredDayOfMonth.value))
    }
  }

  def calculateStartDates(startDatesRequest: StartDatesRequest): StartDatesResponse = {
    val initialPaymentDate: Option[InitialPaymentDate] =
      datesService.initialPaymentDate(
        initialPayment    = startDatesRequest.initialPayment,
        numberOfDaysToAdd = 10
      )
    val potentialInstalmentStartDate: InstalmentStartDate = initialPaymentDate match {
      case Some(_) => InstalmentStartDate(datesService.todayPlusDays(30))
      case None    => InstalmentStartDate(datesService.todayPlusDays(10))
    }
    val instalmentStartDate: InstalmentStartDate =
      calculateInstalmentStartDate(
        preferredDayOfMonth = startDatesRequest.preferredDayOfMonth,
        proposedStartDate   = potentialInstalmentStartDate.value
      )

    StartDatesResponse(initialPaymentDate, instalmentStartDate)
  }
}
