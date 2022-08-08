/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers

import essttp.journey.JourneyConnector
import essttp.testdata.TdAll
import testsupport.ItSpec

class UpdateDatesControllerSpec extends ItSpec {

  def journeyConnector: JourneyConnector = app.injector.instanceOf[JourneyConnector]

  "POST /journey/:journeyId/update-extreme-dates" - {
    "should throw Bad Request when Journey is in a stage [BeforeUpfrontPaymentAnswers]" in new JourneyItTest {
      journeyConnector.Epaye.startJourneyBta(TdAll.EpayeBta.sjRequest).futureValue
      val result: Throwable = journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.EpayeBta.updateExtremeDatesRequest()).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"UpdateExtremeDatesResponse update is not possible in that state: [Started]"}""")
    }
    "should not update the journey when extreme dates response hasn't changed" in new JourneyItTest {
      insertJourneyForTest(TdAll.EpayeBta.journeyAfterUpfrontPaymentAmount.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.EpayeBta.updateExtremeDatesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterExtremeDates
      journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.EpayeBta.updateExtremeDatesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterExtremeDates
    }
    "should update the journey when extreme dates response has changed" in new JourneyItTest {
      insertJourneyForTest(TdAll.EpayeBta.journeyAfterUpfrontPaymentAmount.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.EpayeBta.updateExtremeDatesRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterExtremeDates
      journeyConnector.updateExtremeDates(tdAll.journeyId, TdAll.extremeDatesWithoutUpfrontPayment).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterExtremeDates.copy(extremeDatesResponse = TdAll.extremeDatesWithoutUpfrontPayment)
    }
    "should throw a Bad Request when journey is in stage SubmittedArrangement" in new JourneyItTest {
      insertJourneyForTest(TdAll.EpayeBta.journeyAfterSubmittedArrangement.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      val result: Throwable = journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.EpayeBta.updateExtremeDatesRequest()).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"Cannot update ExtremeDates when journey is in completed state"}""")
    }
  }

  "POST /journey/:journeyId/update-start-dates" - {
    "should throw Bad Request when Journey is in a stage [BeforeEnteredDayOfMonth]" in new JourneyItTest {
      journeyConnector.Epaye.startJourneyBta(TdAll.EpayeBta.sjRequest).futureValue
      val result: Throwable = journeyConnector.updateStartDates(tdAll.journeyId, tdAll.EpayeBta.updateStartDatesResponse()).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"UpdateStartDates is not possible when we don't have a chosen day of month, stage: [ Started ]"}""")
    }
    "should not update the journey when start dates response hasn't changed" in new JourneyItTest {
      insertJourneyForTest(TdAll.EpayeBta.journeyAfterDayOfMonth.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      journeyConnector.updateStartDates(tdAll.journeyId, tdAll.EpayeBta.updateStartDatesResponse()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterStartDatesResponse
      journeyConnector.updateStartDates(tdAll.journeyId, tdAll.EpayeBta.updateStartDatesResponse()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterStartDatesResponse
    }
    "should update the journey when start dates response has changed" in new JourneyItTest {
      insertJourneyForTest(TdAll.EpayeBta.journeyAfterDayOfMonth.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      journeyConnector.updateStartDates(tdAll.journeyId, tdAll.EpayeBta.updateStartDatesResponse()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterStartDatesResponse
      journeyConnector.updateStartDates(tdAll.journeyId, TdAll.startDatesResponseWithoutInitialPayment).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterStartDatesResponse.copy(startDatesResponse = TdAll.startDatesResponseWithoutInitialPayment)
    }
    "should throw a Bad Request when journey is in stage SubmittedArrangement" in new JourneyItTest {
      insertJourneyForTest(TdAll.EpayeBta.journeyAfterSubmittedArrangement.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      val result: Throwable = journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.EpayeBta.updateExtremeDatesRequest()).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"Cannot update ExtremeDates when journey is in completed state"}""")
    }
  }
}
