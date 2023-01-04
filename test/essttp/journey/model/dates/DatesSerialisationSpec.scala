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

package essttp.journey.model.dates

import essttp.dates.DatesTdAll
import essttp.rootmodel.dates.InitialPayment
import essttp.rootmodel.dates.extremedates.{ExtremeDatesRequest, ExtremeDatesResponse}
import essttp.rootmodel.dates.startdates.{PreferredDayOfMonth, StartDatesRequest, StartDatesResponse}
import play.api.libs.json.Json
import testsupport.UnitSpec

class DatesSerialisationSpec extends UnitSpec {

  "InitialPayment should" - {
    "serialise from InitialPayment" in {
      Json.toJson[InitialPayment](InitialPayment(true)) shouldBe Json.parse("true")
      Json.toJson[InitialPayment](InitialPayment(false)) shouldBe Json.parse("false")
    }
    "de-serialise as InitialPayment" in {
      Json.parse("true").as[InitialPayment] shouldBe InitialPayment(true)
      Json.parse("false").as[InitialPayment] shouldBe InitialPayment(false)
    }
  }

  "PreferredDayOfMonth should" - {
    "serialise from PreferredDayOfMonth" in {
      Json.toJson[PreferredDayOfMonth](PreferredDayOfMonth(28)) shouldBe Json.parse("28")
    }
    "de-serialise as PreferredDayOfMonth" in {
      Json.parse("28").as[PreferredDayOfMonth] shouldBe PreferredDayOfMonth(28)
    }
    "input of less than 1 throws IllegalArgumentException" in {
      intercept[IllegalArgumentException] {
        Json.parse("0").as[PreferredDayOfMonth]
      }.getMessage should include("Day of month can't be less then 1")
    }
    "input of greater than 28 throws IllegalArgumentException" in {
      intercept[IllegalArgumentException] {
        Json.parse("29").as[PreferredDayOfMonth]
      }.getMessage should include("Day of month can't be grater then 28")
    }
    "negative input throws IllegalArgumentException" in {
      intercept[IllegalArgumentException] {
        Json.parse("-1").as[PreferredDayOfMonth]
      }.getMessage should include("Day of month can't be less then 1")
    }
  }

  "StartDatesRequest should" - {
    "serialise from StartDatesRequest" in {
      Json.toJson[StartDatesRequest](DatesTdAll.startDatesRequestWithUpfrontPayment) shouldBe DatesTdAll.startDatesRequestJson
    }
    "de-serialise as StartDatesRequest" in {
      DatesTdAll.startDatesRequestJson.as[StartDatesRequest] shouldBe DatesTdAll.startDatesRequestWithUpfrontPayment
    }
  }

  "StartDatesResponse should" - {
    "serialise from StartDatesResponse - None for initialPaymentDate" in {
      Json.toJson[StartDatesResponse](DatesTdAll.`startDatesResponse-NoInitialPaymentDate`) shouldBe DatesTdAll.`startDatesResponseJson-NoInitialPaymentDate`
    }
    "de-serialise as StartDatesResponse - None for initialPaymentDate" in {
      DatesTdAll.`startDatesResponseJson-NoInitialPaymentDate`.as[StartDatesResponse] shouldBe DatesTdAll.`startDatesResponse-NoInitialPaymentDate`
    }
    "serialise from StartDatesResponse - Some for initialPaymentDate" in {
      Json.toJson[StartDatesResponse](DatesTdAll.`startDatesResponse-WithInitialPaymentDate`) shouldBe DatesTdAll.`startDatesResponseJson-WithInitialPaymentDate`
    }
    "de-serialise as StartDatesResponse - Some for initialPaymentDate" in {
      DatesTdAll.`startDatesResponseJson-WithInitialPaymentDate`.as[StartDatesResponse] shouldBe DatesTdAll.`startDatesResponse-WithInitialPaymentDate`
    }
  }

  "ExtremeDatesRequest should" - {
    "serialise from ExtremeDatesRequest" in {
      Json.toJson[ExtremeDatesRequest](DatesTdAll.extremeDatesRequest) shouldBe DatesTdAll.extremeDatesRequestJson
    }
    "de-serialise as ExtremeDatesRequest" in {
      DatesTdAll.extremeDatesRequestJson.as[ExtremeDatesRequest] shouldBe DatesTdAll.extremeDatesRequest
    }
  }

  "ExtremeDatesResponse should" - {
    "serialise from ExtremeDatesResponse" in {
      Json.toJson[ExtremeDatesResponse](DatesTdAll.extremeDatesResponse) shouldBe DatesTdAll.extremeDatesResponseJson
      Json.toJson[ExtremeDatesResponse](DatesTdAll.extremeDatesNoUpfrontPaymentResponse) shouldBe DatesTdAll.extremeDatesNoUpfrontPaymentResponseJson
    }
    "de-serialise as ExtremeDatesResponse" in {
      DatesTdAll.extremeDatesResponseJson.as[ExtremeDatesResponse] shouldBe DatesTdAll.extremeDatesResponse
      DatesTdAll.extremeDatesNoUpfrontPaymentResponseJson.as[ExtremeDatesResponse] shouldBe DatesTdAll.extremeDatesNoUpfrontPaymentResponse
    }
  }
}
