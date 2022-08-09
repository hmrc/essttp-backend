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
import essttp.journey.model.Stage.AfterChosenTypeOfBankAccount
import essttp.testdata.TdAll
import testsupport.ItSpec

class UpdateTypeOfBankAccountControllerSpec extends ItSpec {

  def journeyConnector: JourneyConnector = app.injector.instanceOf[JourneyConnector]

  "POST /journey/:journeyId/update-chosen-type-of-bank-account" - {
    "should throw Bad Request when Journey is in a stage [BeforeCheckedPaymentPlan]" in new JourneyItTest {
      journeyConnector.Epaye.startJourneyBta(TdAll.EpayeBta.sjRequest).futureValue
      val result: Throwable = journeyConnector.updateChosenTypeOfBankAccount(tdAll.journeyId, TdAll.EpayeBta.updateChosenTypeOfBankAccountRequest()).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"UpdateTypeOfBankAccount is not possible in that state: [Started]"}""")
    }
    "should not update the journey when Type of bank account hasn't changed" in new JourneyItTest {
      insertJourneyForTest(TdAll.EpayeBta.journeyAfterCheckedPaymentPlan.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      journeyConnector.updateChosenTypeOfBankAccount(tdAll.journeyId, TdAll.EpayeBta.updateChosenTypeOfBankAccountRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterChosenTypeOfBankAccount
      journeyConnector.updateChosenTypeOfBankAccount(tdAll.journeyId, TdAll.EpayeBta.updateChosenTypeOfBankAccountRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterChosenTypeOfBankAccount
    }
    "should update the journey when Type of bank account has changed" in new JourneyItTest {
      insertJourneyForTest(TdAll.EpayeBta.journeyAfterCheckedPaymentPlan.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      journeyConnector.updateChosenTypeOfBankAccount(tdAll.journeyId, TdAll.businessBankAccount).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterChosenTypeOfBankAccount
      journeyConnector.updateChosenTypeOfBankAccount(tdAll.journeyId, TdAll.personalBankAccount).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe
        tdAll.EpayeBta.journeyAfterChosenTypeOfBankAccount
        .copy(typeOfBankAccount = TdAll.personalBankAccount)
        .copy(stage = AfterChosenTypeOfBankAccount.Personal)
    }
    "should throw a Bad Request when journey is in stage SubmittedArrangement" in new JourneyItTest {
      insertJourneyForTest(TdAll.EpayeBta.journeyAfterSubmittedArrangement.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      val result: Throwable = journeyConnector.updateChosenTypeOfBankAccount(tdAll.journeyId, tdAll.EpayeBta.updateChosenTypeOfBankAccountRequest()).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"Cannot update TypeOfBankAccount when journey is in completed state"}""")
    }
  }
}
