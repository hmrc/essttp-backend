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

class UpdateHasAgreedTermsAndConditionsControllerSpec extends ItSpec {

  def journeyConnector: JourneyConnector = app.injector.instanceOf[JourneyConnector]

  "POST /journey/:journeyId/update-has-agreed-terms-and-conditions" - {
    "should throw Bad Request when Journey is in a stage [BeforeConfirmedDirectDebitDetails]" in new JourneyItTest {
      journeyConnector.Epaye.startJourneyBta(TdAll.EpayeBta.sjRequest).futureValue
      val result: Throwable = journeyConnector.updateHasAgreedTermsAndConditions(tdAll.journeyId).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"UpdateAgreedTermsAndConditions is not possible in that state: [Started]"}""")
    }
    "should return Unit when terms and conditions have already been confirmed" in new JourneyItTest {
      insertJourneyForTest(TdAll.EpayeBta.journeyAfterConfirmedDirectDebitDetails.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      journeyConnector.updateHasAgreedTermsAndConditions(tdAll.journeyId).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterAgreedTermsAndConditions
      val result = journeyConnector.updateHasAgreedTermsAndConditions(tdAll.journeyId)
      result.futureValue shouldBe (())
    }
    "should throw a Bad Request when journey is in stage SubmittedArrangement" in new JourneyItTest {
      insertJourneyForTest(TdAll.EpayeBta.journeyAfterSubmittedArrangement.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      val result: Throwable = journeyConnector.updateHasAgreedTermsAndConditions(tdAll.journeyId).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"Cannot update AgreedTermsAndConditions when journey is in completed state"}""")
    }
  }
}
