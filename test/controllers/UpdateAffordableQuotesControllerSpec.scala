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

class UpdateAffordableQuotesControllerSpec extends ItSpec {

  def journeyConnector: JourneyConnector = app.injector.instanceOf[JourneyConnector]

  "POST /journey/:journeyId/update-affordable-quotes" - {
    "should throw Bad Request when Journey is in a stage [UpdateAffordableQuotes]" in new JourneyItTest {
      stubCommonActions()

      journeyConnector.Epaye.startJourneyBta(TdAll.EpayeBta.sjRequest).futureValue
      val result: Throwable = journeyConnector.updateAffordableQuotes(tdAll.journeyId, TdAll.EpayeBta.updateAffordableQuotesResponse()).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"UpdateAffordableQuotes is not possible in that state: [Started]"}""")

      verifyCommonActions(numberOfAuthCalls = 2)
    }
    "should not update the journey when Affordable Quotes haven't changed" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterStartDatesResponse.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      journeyConnector.updateAffordableQuotes(tdAll.journeyId, TdAll.EpayeBta.updateAffordableQuotesResponse()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterAffordableQuotesResponse
      journeyConnector.updateAffordableQuotes(tdAll.journeyId, TdAll.EpayeBta.updateAffordableQuotesResponse()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterAffordableQuotesResponse

      verifyCommonActions(numberOfAuthCalls = 4)
    }
    "should update the journey when Affordable Quotes has changed" in new JourneyItTest {
      stubCommonActions()
      insertJourneyForTest(TdAll.EpayeBta.journeyAfterStartDatesResponse.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      journeyConnector.updateAffordableQuotes(tdAll.journeyId, TdAll.EpayeBta.updateAffordableQuotesResponse()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterAffordableQuotesResponse
      journeyConnector.updateAffordableQuotes(tdAll.journeyId, tdAll.affordableQuotesResponseWith2Plans).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterAffordableQuotesResponse.copy(affordableQuotesResponse = tdAll.affordableQuotesResponseWith2Plans)

      verifyCommonActions(numberOfAuthCalls = 4)
    }
    "should throw a Bad Request when journey is in stage SubmittedArrangement" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterSubmittedArrangement().copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      val result: Throwable = journeyConnector.updateAffordableQuotes(tdAll.journeyId, tdAll.EpayeBta.updateAffordableQuotesResponse()).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"Cannot update AffordableQuotes when journey is in completed state"}""")

      verifyCommonActions(numberOfAuthCalls = 1)
    }
  }
}
