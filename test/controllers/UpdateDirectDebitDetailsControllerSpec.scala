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

class UpdateDirectDebitDetailsControllerSpec extends ItSpec {

  def journeyConnector: JourneyConnector = app.injector.instanceOf[JourneyConnector]

  "POST /journey/:journeyId/update-direct-debit-details" - {
    "should throw Bad Request when Journey is in a stage [BeforeChosenTypeOfBankAccount]" in new JourneyItTest {
      journeyConnector.Epaye.startJourneyBta(TdAll.EpayeBta.sjRequest).futureValue
      val result: Throwable = journeyConnector.updateDirectDebitDetails(tdAll.journeyId, TdAll.EpayeBta.updateDirectDebitDetailsRequest(true)).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"UpdateDirectDebitDetails is not possible in that state: [Started]"}""")
    }
    "should not update the journey when Direct debit details haven't changed" in new JourneyItTest {
      insertJourneyForTest(TdAll.EpayeBta.journeyAfterChosenTypeOfBankAccount.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      journeyConnector.updateDirectDebitDetails(tdAll.journeyId, TdAll.EpayeBta.updateDirectDebitDetailsRequest(true)).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterEnteredDirectDebitDetails(true)
      journeyConnector.updateDirectDebitDetails(tdAll.journeyId, TdAll.EpayeBta.updateDirectDebitDetailsRequest(true)).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterEnteredDirectDebitDetails(true)
    }
    "should update the journey when Direct debit details have changed" in new JourneyItTest {
      insertJourneyForTest(TdAll.EpayeBta.journeyAfterChosenTypeOfBankAccount.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      journeyConnector.updateDirectDebitDetails(tdAll.journeyId, TdAll.EpayeBta.updateDirectDebitDetailsRequest(true)).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterEnteredDirectDebitDetails(true)
      journeyConnector.updateDirectDebitDetails(tdAll.journeyId, TdAll.EpayeBta.updateDirectDebitDetailsRequest(false)).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterEnteredDirectDebitDetails(false)
    }
    "should throw a Bad Request when journey is in stage SubmittedArrangement" in new JourneyItTest {
      insertJourneyForTest(TdAll.EpayeBta.journeyAfterSubmittedArrangement.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      val result: Throwable = journeyConnector.updateDirectDebitDetails(tdAll.journeyId, tdAll.EpayeBta.updateDirectDebitDetailsRequest(true)).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"Cannot update DirectDebitDetails when journey is in completed state"}""")
    }
  }
}
