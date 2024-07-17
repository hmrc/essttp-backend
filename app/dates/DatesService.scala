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

import cats.syntax.eq._
import com.google.inject.{Inject, Singleton}
import dates.models.{AddWorkingDaysRequest, AddWorkingDaysResponse, Region}
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{Clock, LocalDate}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DatesService @Inject() (clock: Clock, dateCalculatorConnector: DateCalculatorConnector)(implicit ec: ExecutionContext) {

  private def today(): LocalDate = LocalDate.now(clock)

  def todayPlusCalendarDays(numberOfDays: Int): LocalDate = today().plusDays(numberOfDays)

  def todayPlusWorkingDays(numberOfDaysToAdd: Int)(implicit hc: HeaderCarrier): Future[LocalDate] = {
    dateCalculatorConnector
      .addWorkingDays(AddWorkingDaysRequest(today(), numberOfDaysToAdd, Set(Region.EnglandAndWales)))
      .map { response =>
        if (response.status === 200) {
          response.json.validate[AddWorkingDaysResponse] match {
            case JsSuccess(result, _) => result.result
            case JsError(_)           => throw new Exception("Could not parse date calculator response")
          }
        } else {
          throw new Exception(s"Call to date-calculator came back with unexpected http status ${response.status.toString}")
        }
      }
  }

}
