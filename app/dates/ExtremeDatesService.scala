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
import essttp.rootmodel.dates.extremedates.{EarliestPaymentPlanStartDate, ExtremeDatesRequest, ExtremeDatesResponse, LatestPaymentPlanStartDate}
import essttp.rootmodel.dates.{InitialPayment, InitialPaymentDate}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ExtremeDatesService @Inject() (datesService: DatesService)(implicit ec: ExecutionContext) {

  def calculateExtremeDates(extremeDatesRequest: ExtremeDatesRequest): Future[ExtremeDatesResponse] = {
    val earliestDatePaymentCanBeTakenF = datesService.todayPlusWorkingDays(6)

    earliestDatePaymentCanBeTakenF.map{ earliestDatePaymentCanBeTaken =>
      val initialPaymentDate: Option[InitialPaymentDate] =
        if (extremeDatesRequest.initialPayment.value) Some(InitialPaymentDate(earliestDatePaymentCanBeTaken))
        else None

      val earliestPlanStartDate: EarliestPaymentPlanStartDate = extremeDatesRequest.initialPayment match {
        case InitialPayment(true)  => EarliestPaymentPlanStartDate(datesService.todayPlusCalendarDays(30))
        case InitialPayment(false) => EarliestPaymentPlanStartDate(earliestDatePaymentCanBeTaken)
      }
      val latestPlanStartDate: LatestPaymentPlanStartDate = extremeDatesRequest.initialPayment match {
        case InitialPayment(true)  => LatestPaymentPlanStartDate(datesService.todayPlusCalendarDays(60))
        case InitialPayment(false) => LatestPaymentPlanStartDate(datesService.todayPlusCalendarDays(40))
      }

      ExtremeDatesResponse(
        initialPaymentDate    = initialPaymentDate,
        earliestPlanStartDate = earliestPlanStartDate,
        latestPlanStartDate   = latestPlanStartDate
      )

    }

  }
}
