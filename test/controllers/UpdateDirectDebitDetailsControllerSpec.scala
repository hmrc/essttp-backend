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
import essttp.journey.model.Journey
import essttp.journey.model.Journey.Epaye
import essttp.rootmodel.bank.{AccountNumber, BankDetails}
import testsupport.ItSpec
import testsupport.testdata.TdAll
import uk.gov.hmrc.crypto.Sensitive.SensitiveString

class UpdateDirectDebitDetailsControllerSpec extends ItSpec {

  def journeyConnector: JourneyConnector = app.injector.instanceOf[JourneyConnector]

  "POST /journey/:journeyId/update-direct-debit-details" - {
    "should throw Bad Request when Journey is in a stage [BeforeChosenTypeOfBankAccount]" in new JourneyItTest {
      stubCommonActions()

      journeyConnector.Epaye.startJourneyBta(TdAll.EpayeBta.sjRequest).futureValue

      val result: Throwable = journeyConnector.updateDirectDebitDetails(tdAll.journeyId, TdAll.EpayeBta.updateDirectDebitDetailsRequest).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"UpdateDirectDebitDetails is not possible in that state: [Started]"}""")

      verifyCommonActions(numberOfAuthCalls = 2)
    }
    "should not update the journey when Direct debit details haven't changed" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(
        TdAll.EpayeBta.journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder = true)
          .copy(_id           = tdAll.journeyId, correlationId = tdAll.correlationId)
      )

      val result1: Journey = journeyConnector.updateDirectDebitDetails(tdAll.journeyId, TdAll.EpayeBta.updateDirectDebitDetailsRequest).futureValue
      result1 shouldBe tdAll.EpayeBta.journeyAfterEnteredDirectDebitDetails()
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterEnteredDirectDebitDetails()

      val result2: Journey = journeyConnector.updateDirectDebitDetails(tdAll.journeyId, TdAll.EpayeBta.updateDirectDebitDetailsRequest).futureValue
      result2 shouldBe tdAll.EpayeBta.journeyAfterEnteredDirectDebitDetails()
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterEnteredDirectDebitDetails()

      verifyCommonActions(numberOfAuthCalls = 4)
    }
    "should update the journey when Direct debit details have changed" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(
        TdAll.EpayeBta.journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder = true)
          .copy(_id           = tdAll.journeyId, correlationId = tdAll.correlationId)
      )

      val result1: Journey = journeyConnector.updateDirectDebitDetails(tdAll.journeyId, TdAll.EpayeBta.updateDirectDebitDetailsRequest).futureValue
      result1 shouldBe tdAll.EpayeBta.journeyAfterEnteredDirectDebitDetails()
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterEnteredDirectDebitDetails()

      val updateRequest: BankDetails = TdAll.EpayeBta.updateDirectDebitDetailsRequest.copy(accountNumber = AccountNumber(SensitiveString("accounts")))
      val expectedUpdatedJourney: Epaye.EnteredDirectDebitDetails = tdAll.EpayeBta.journeyAfterEnteredDirectDebitDetails().copy(
        directDebitDetails = updateRequest
      )
      val result2: Journey = journeyConnector.updateDirectDebitDetails(tdAll.journeyId, updateRequest).futureValue
      result2 shouldBe expectedUpdatedJourney
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe expectedUpdatedJourney

      verifyCommonActions(numberOfAuthCalls = 4)
    }
    "should throw a Bad Request when journey is in stage SubmittedArrangement" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterSubmittedArrangement().copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      val result: Throwable = journeyConnector.updateDirectDebitDetails(tdAll.journeyId, tdAll.EpayeBta.updateDirectDebitDetailsRequest).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"Cannot update DirectDebitDetails when journey is in completed state"}""")

      verifyCommonActions(numberOfAuthCalls = 1)
    }
  }
}
