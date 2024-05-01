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

package testsupport.testdata.dates

import essttp.rootmodel.dates.extremedates.{EarliestPaymentPlanStartDate, ExtremeDatesRequest, ExtremeDatesResponse, LatestPaymentPlanStartDate}
import essttp.rootmodel.dates.startdates.{InstalmentStartDate, PreferredDayOfMonth, StartDatesRequest, StartDatesResponse}
import essttp.rootmodel.dates.{InitialPayment, InitialPaymentDate}
import play.api.libs.json.{JsValue, Json}

import java.time.LocalDate

object DatesTdAll {

  def startDatesRequest(initialPayment: InitialPayment, preferredDayOfMonth: PreferredDayOfMonth): StartDatesRequest =
    StartDatesRequest(initialPayment, preferredDayOfMonth)

  val initialPaymentTrue: InitialPayment = InitialPayment(value = true)
  val initialPaymentFalse: InitialPayment = initialPaymentTrue.copy(false)
  val preferredDayOfMonth: PreferredDayOfMonth = PreferredDayOfMonth(1)
  val initialPaymentDate: InitialPaymentDate = InitialPaymentDate(LocalDate.parse(TdDates.`1stJan2022`))
  val instalmentStartDate: InstalmentStartDate = InstalmentStartDate(LocalDate.parse(TdDates.`1stFeb2022`))

  val startDatesRequestWithUpfrontPayment: StartDatesRequest = StartDatesRequest(
    initialPayment      = initialPaymentTrue,
    preferredDayOfMonth = preferredDayOfMonth
  )

  val startDatesResponseWithUpfrontPayment: StartDatesResponse = StartDatesResponse(
    initialPaymentDate  = Some(initialPaymentDate),
    instalmentStartDate = instalmentStartDate
  )

  val startDatesRequestJson: JsValue = Json.parse("""{"initialPayment":true,"preferredDayOfMonth":1}""")

  val `startDatesResponse-NoInitialPaymentDate`: StartDatesResponse = StartDatesResponse(initialPaymentDate  = None, instalmentStartDate = InstalmentStartDate(LocalDate.parse(TdDates.`1stFeb2022`)))

  def startDatesResponse(initialPaymentDate: Option[InitialPaymentDate], instalmentStartDate: String): StartDatesResponse = {
    StartDatesResponse(initialPaymentDate, InstalmentStartDate(LocalDate.parse(instalmentStartDate)))
  }

  val `startDatesResponse-WithInitialPaymentDate`: StartDatesResponse = StartDatesResponse(initialPaymentDate  = Some(initialPaymentDate), instalmentStartDate = InstalmentStartDate(LocalDate.parse(TdDates.`1stFeb2022`)))
  val `startDatesResponseJson-NoInitialPaymentDate`: JsValue = Json.parse("""{"instalmentStartDate":"2022-02-01"}""")
  val `startDatesResponseJson-WithInitialPaymentDate`: JsValue = Json.parse("""{"initialPaymentDate":"2022-01-01", "instalmentStartDate":"2022-02-01"}""")

  val testDate: LocalDate = LocalDate.parse("2022-01-01")
  val testDateAsString: String = "2022-01-01"

  val extremeDatesRequest: ExtremeDatesRequest = ExtremeDatesRequest(initialPaymentTrue)
  val extremeDatesRequestJson: JsValue = Json.parse("""{"initialPayment": true}""")

  val extremeDatesResponse: ExtremeDatesResponse = ExtremeDatesResponse(Some(initialPaymentDate), EarliestPaymentPlanStartDate(testDate), LatestPaymentPlanStartDate(testDate))
  val extremeDatesNoUpfrontPaymentResponse: ExtremeDatesResponse = ExtremeDatesResponse(None, EarliestPaymentPlanStartDate(testDate), LatestPaymentPlanStartDate(testDate))
  val extremeDatesResponseJson: JsValue = Json.parse("""{"initialPaymentDate":"2022-01-01","earliestPlanStartDate":"2022-01-01","latestPlanStartDate":"2022-01-01"}""")
  val extremeDatesNoUpfrontPaymentResponseJson: JsValue = Json.parse("""{"earliestPlanStartDate":"2022-01-01","latestPlanStartDate":"2022-01-01"}""")

  object TdDates {
    val `1stJan2022` = "2022-01-01"
    val `11thJan2022` = "2022-01-11"
    val `15thJan2022` = "2022-01-15"
    val `20thJan2022` = "2022-01-20"
    val `25thJan2022` = "2022-01-25"
    val `28thJan2022` = "2022-01-28"
    val `1stFeb2022` = "2022-02-01"
    val `2ndFeb2022` = "2022-02-02"
    val `7thFeb2022` = "2022-02-07"
    val `10thFeb2022` = "2022-02-10"
    val `15thFeb2022` = "2022-02-15"
    val `25thFeb2022` = "2022-02-25"
    val `28thFeb2022` = "2022-02-28"
    val `1stMar2022` = "2022-03-01"
    val `2ndMar2022` = "2022-03-02"
    val `15thMar2022` = "2022-03-15"
    val `25thDec2022` = "2022-12-25"
    val `4thJan2023` = "2023-01-04"
    val `15thJan2023` = "2023-01-15"
    val `28thJan2023` = "2023-01-28"
    val `1stFeb2023` = "2023-02-01"
    val `7thFeb2023` = "2023-02-07"
    val `15thFeb2023` = "2023-02-15"
  }
}
