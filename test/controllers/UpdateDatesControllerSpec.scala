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

package controllers

import essttp.journey.JourneyConnector
import essttp.testdata.TdAll
import testsupport.ItSpec

class UpdateDatesControllerSpec extends ItSpec {

  def journeyConnector: JourneyConnector = app.injector.instanceOf[JourneyConnector]

  "POST /journey/:journeyId/update-extreme-dates" - {
    "should throw Bad Request when Journey is in a stage [BeforeUpfrontPaymentAnswers]" in new JourneyItTest {
      stubCommonActions()

      journeyConnector.Epaye.startJourneyBta(TdAll.EpayeBta.sjRequest).futureValue
      val result: Throwable = journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.EpayeBta.updateExtremeDatesRequest()).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"UpdateExtremeDatesResponse update is not possible in that state: [Started]"}""")

      verifyCommonActions(numberOfAuthCalls = 2)
    }
    "should not update the journey when extreme dates response hasn't changed" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterUpfrontPaymentAmount.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      val result1 = journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.EpayeBta.updateExtremeDatesRequest()).futureValue
      result1 shouldBe tdAll.EpayeBta.journeyAfterExtremeDates
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterExtremeDates

      val result2 = journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.EpayeBta.updateExtremeDatesRequest()).futureValue
      result2 shouldBe tdAll.EpayeBta.journeyAfterExtremeDates
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterExtremeDates

      verifyCommonActions(numberOfAuthCalls = 4)
    }
    "should update the journey when extreme dates response has changed" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterUpfrontPaymentAmount.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))

      val result1 = journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.EpayeBta.updateExtremeDatesRequest()).futureValue
      result1 shouldBe tdAll.EpayeBta.journeyAfterExtremeDates
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterExtremeDates

      val result2 = journeyConnector.updateExtremeDates(tdAll.journeyId, TdAll.extremeDatesWithoutUpfrontPayment).futureValue
      val expectedUpdatedJourney2 = tdAll.EpayeBta.journeyAfterExtremeDates.copy(extremeDatesResponse = TdAll.extremeDatesWithoutUpfrontPayment)
      result2 shouldBe expectedUpdatedJourney2
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe expectedUpdatedJourney2

      verifyCommonActions(numberOfAuthCalls = 4)
    }
    "should throw a Bad Request when journey is in stage SubmittedArrangement" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterSubmittedArrangement().copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      val result: Throwable = journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.EpayeBta.updateExtremeDatesRequest()).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"Cannot update ExtremeDates when journey is in completed state"}""")

      verifyCommonActions(numberOfAuthCalls = 1)
    }
  }

  "POST /journey/:journeyId/update-start-dates" - {
    "should throw Bad Request when Journey is in a stage [BeforeEnteredDayOfMonth]" in new JourneyItTest {
      stubCommonActions()

      journeyConnector.Epaye.startJourneyBta(TdAll.EpayeBta.sjRequest).futureValue
      val result: Throwable = journeyConnector.updateStartDates(tdAll.journeyId, tdAll.EpayeBta.updateStartDatesResponse()).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"UpdateStartDates is not possible when we don't have a chosen day of month, stage: [ Started ]"}""")

      verifyCommonActions(numberOfAuthCalls = 2)
    }
    "should not update the journey when start dates response hasn't changed" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterDayOfMonth.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      val result1 = journeyConnector.updateStartDates(tdAll.journeyId, tdAll.EpayeBta.updateStartDatesResponse()).futureValue
      result1 shouldBe tdAll.EpayeBta.journeyAfterStartDatesResponse
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterStartDatesResponse

      val result2 = journeyConnector.updateStartDates(tdAll.journeyId, tdAll.EpayeBta.updateStartDatesResponse()).futureValue
      result2 shouldBe tdAll.EpayeBta.journeyAfterStartDatesResponse
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterStartDatesResponse

      verifyCommonActions(numberOfAuthCalls = 4)
    }
    "should update the journey when start dates response has changed" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterDayOfMonth.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))

      val result1 = journeyConnector.updateStartDates(tdAll.journeyId, tdAll.EpayeBta.updateStartDatesResponse()).futureValue
      result1 shouldBe tdAll.EpayeBta.journeyAfterStartDatesResponse
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterStartDatesResponse

      val result2 = journeyConnector.updateStartDates(tdAll.journeyId, TdAll.startDatesResponseWithoutInitialPayment).futureValue
      val expectedUpdatedJourney2 = tdAll.EpayeBta.journeyAfterStartDatesResponse.copy(startDatesResponse = TdAll.startDatesResponseWithoutInitialPayment)
      result2 shouldBe expectedUpdatedJourney2
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe expectedUpdatedJourney2

      verifyCommonActions(numberOfAuthCalls = 4)
    }
    "should throw a Bad Request when journey is in stage SubmittedArrangement" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterSubmittedArrangement().copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      val result: Throwable = journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.EpayeBta.updateExtremeDatesRequest()).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"Cannot update ExtremeDates when journey is in completed state"}""")

      verifyCommonActions(numberOfAuthCalls = 1)
    }
  }
}
