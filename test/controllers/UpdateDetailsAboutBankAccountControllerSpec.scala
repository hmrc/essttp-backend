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
import essttp.journey.model.Stage.AfterEnteredDetailsAboutBankAccount
import essttp.rootmodel.bank.DetailsAboutBankAccount
import essttp.testdata.TdAll
import testsupport.ItSpec

class UpdateDetailsAboutBankAccountControllerSpec extends ItSpec {

  def journeyConnector: JourneyConnector = app.injector.instanceOf[JourneyConnector]

  "POST /journey/:journeyId/update-details-about-bank-account" - {
    "should throw Bad Request when Journey is in a stage [BeforeCheckedPaymentPlan]" in new JourneyItTest {
      stubCommonActions()

      journeyConnector.Epaye.startJourneyBta(TdAll.EpayeBta.sjRequest).futureValue

      val requestBody = TdAll.EpayeBta.updateDetailsAboutBankAccountRequest(isAccountHolder = true)
      val result: Throwable = journeyConnector.updateDetailsAboutBankAccount(tdAll.journeyId, requestBody).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"UpdateDetailsAboutBankAccount is not possible in that state: [Started]"}""")

      verifyCommonActions(numberOfAuthCalls = 2)
    }
    "should not update the journey when Type of bank account hasn't changed" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterCheckedPaymentPlan.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))

      val requestBody = TdAll.EpayeBta.updateDetailsAboutBankAccountRequest(isAccountHolder = true)
      journeyConnector.updateDetailsAboutBankAccount(tdAll.journeyId, requestBody).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder = true)
      journeyConnector.updateDetailsAboutBankAccount(tdAll.journeyId, requestBody).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder = true)

      verifyCommonActions(numberOfAuthCalls = 4)
    }
    "should update the journey when Type of bank account has changed" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterCheckedPaymentPlan.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))

      val request1 = DetailsAboutBankAccount(TdAll.businessBankAccount, isAccountHolder = true)
      val expectedUpdatedJourney1 = tdAll.EpayeBta.journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder = true)
      journeyConnector.updateDetailsAboutBankAccount(tdAll.journeyId, request1).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe expectedUpdatedJourney1

      val request2 = DetailsAboutBankAccount(TdAll.businessBankAccount, isAccountHolder = false)
      val expectedUpdatedJourney2 = tdAll.EpayeBta.journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder = false)
      journeyConnector.updateDetailsAboutBankAccount(tdAll.journeyId, request2).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe expectedUpdatedJourney2

      val request3 = DetailsAboutBankAccount(TdAll.personalBankAccount, isAccountHolder = true)
      val expectedUpdatedJourney3 = tdAll.EpayeBta.journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder = true).copy(
        stage                   = AfterEnteredDetailsAboutBankAccount.Personal,
        detailsAboutBankAccount = request3
      )
      journeyConnector.updateDetailsAboutBankAccount(tdAll.journeyId, request3).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe expectedUpdatedJourney3

      verifyCommonActions(numberOfAuthCalls = 6)
    }
    "should throw a Bad Request when journey is in stage SubmittedArrangement" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterSubmittedArrangement.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))

      val requestBody = TdAll.EpayeBta.updateDetailsAboutBankAccountRequest(isAccountHolder = true)
      val result: Throwable = journeyConnector.updateDetailsAboutBankAccount(tdAll.journeyId, requestBody).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"Cannot update DetailsAboutBankAccount when journey is in completed state"}""")

      verifyCommonActions(numberOfAuthCalls = 1)
    }
  }
}
