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
import essttp.journey.model.UpfrontPaymentAnswers
import essttp.rootmodel.{AmountInPence, UpfrontPaymentAmount}
import essttp.testdata.TdAll
import testsupport.ItSpec

class UpdateUpfrontPaymentAmountControllerSpec extends ItSpec {

  def journeyConnector: JourneyConnector = app.injector.instanceOf[JourneyConnector]

  "POST /journey/:journeyId/update-upfront-payment-amount" - {
    "should throw Bad Request when Journey is in a stage [BeforeAnsweredCanPayUpfront]" in new JourneyItTest {
      stubCommonActions()

      journeyConnector.Epaye.startJourneyBta(TdAll.EpayeBta.sjRequest).futureValue
      val result: Throwable = journeyConnector.updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.EpayeBta.updateUpfrontPaymentAmountRequest()).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"UpdateUpfrontPaymentAmount update is not possible in that state: [Started]"}""")

      verifyCommonActions(numberOfAuthCalls = 2)
    }

    "when the journey stage is AnsweredCanPayUpfront" - {
      "should not update the journey when upfront amount choice hasn't changed" in new JourneyItTest {
        stubCommonActions()

        insertJourneyForTest(TdAll.EpayeBta.journeyAfterCanPayUpfrontYes.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
        journeyConnector.updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.EpayeBta.updateUpfrontPaymentAmountRequest()).futureValue
        journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterUpfrontPaymentAmount
        journeyConnector.updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.EpayeBta.updateUpfrontPaymentAmountRequest()).futureValue
        journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterUpfrontPaymentAmount

        verifyCommonActions(numberOfAuthCalls = 4)
      }
      "should update the journey when upfront payment amount has changed" in new JourneyItTest {
        stubCommonActions()

        insertJourneyForTest(TdAll.EpayeBta.journeyAfterCanPayUpfrontYes.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
        journeyConnector.updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.EpayeBta.updateUpfrontPaymentAmountRequest()).futureValue
        journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterUpfrontPaymentAmount
        journeyConnector.updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.EpayeBta.updateUpfrontPaymentAmountRequest().copy(AmountInPence(9999))).futureValue
        journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterUpfrontPaymentAmount.copy(upfrontPaymentAmount = UpfrontPaymentAmount(AmountInPence(9999)))

        verifyCommonActions(numberOfAuthCalls = 4)
      }
    }

    "when the journey stage is AfterEnteredUpfrontPaymentAmount" - {
      "should not update the journey when upfront amount choice hasn't changed" in new JourneyItTest {
        stubCommonActions()

        insertJourneyForTest(TdAll.EpayeBta.journeyAfterUpfrontPaymentAmount.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
        journeyConnector.updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.EpayeBta.updateUpfrontPaymentAmountRequest()).futureValue
        journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterUpfrontPaymentAmount
        journeyConnector.updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.EpayeBta.updateUpfrontPaymentAmountRequest()).futureValue
        journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterUpfrontPaymentAmount

        verifyCommonActions(numberOfAuthCalls = 4)
      }
      "should update the journey when upfront payment amount has changed" in new JourneyItTest {
        stubCommonActions()

        insertJourneyForTest(TdAll.EpayeBta.journeyAfterUpfrontPaymentAmount.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
        journeyConnector.updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.EpayeBta.updateUpfrontPaymentAmountRequest()).futureValue
        journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterUpfrontPaymentAmount
        journeyConnector.updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.EpayeBta.updateUpfrontPaymentAmountRequest().copy(AmountInPence(9999))).futureValue
        journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterUpfrontPaymentAmount.copy(upfrontPaymentAmount = UpfrontPaymentAmount(AmountInPence(9999)))

        verifyCommonActions(numberOfAuthCalls = 4)
      }
    }

    "when the journey stage is AfterUpfrontPaymentAnswers" - {

      "should return a BadRequest if the user has said they cannot make an upfront payment" in new JourneyItTest {
        stubCommonActions()

        insertJourneyForTest(TdAll.EpayeBta.journeyAfterExtremeDates.copy(
          _id                   = tdAll.journeyId,
          correlationId         = tdAll.correlationId,
          upfrontPaymentAnswers = UpfrontPaymentAnswers.NoUpfrontPayment
        ))

        val result: Throwable =
          journeyConnector.updateUpfrontPaymentAmount(
            tdAll.journeyId,
            tdAll.EpayeBta.updateUpfrontPaymentAmountRequest()
          ).failed.futureValue

        result.getMessage should include("""{"statusCode":400,"message":"UpdateUpfrontPaymentAmount update is not possible when an upfront payment has not been chosen"}""")

        verifyCommonActions(numberOfAuthCalls = 1)
      }

      "should not update the journey when upfront amount choice hasn't changed" in new JourneyItTest {
        stubCommonActions()

        insertJourneyForTest(TdAll.EpayeBta.journeyAfterExtremeDates.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
        journeyConnector.updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.EpayeBta.updateUpfrontPaymentAmountRequest()).futureValue
        journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterExtremeDates
        journeyConnector.updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.EpayeBta.updateUpfrontPaymentAmountRequest()).futureValue
        journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterExtremeDates

        verifyCommonActions(numberOfAuthCalls = 4)
      }
      "should update the journey when upfront payment amount has changed" in new JourneyItTest {
        stubCommonActions()

        insertJourneyForTest(TdAll.EpayeBta.journeyAfterExtremeDates.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
        journeyConnector.updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.EpayeBta.updateUpfrontPaymentAmountRequest()).futureValue
        journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterExtremeDates
        journeyConnector.updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.EpayeBta.updateUpfrontPaymentAmountRequest().copy(AmountInPence(9999))).futureValue
        journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterUpfrontPaymentAmount.copy(
          upfrontPaymentAmount = UpfrontPaymentAmount(AmountInPence(9999))
        )

        verifyCommonActions(numberOfAuthCalls = 4)
      }
    }

    "should throw a Bad Request when journey is in stage SubmittedArrangement" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterSubmittedArrangement().copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      val result: Throwable = journeyConnector.updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.EpayeBta.updateUpfrontPaymentAmountRequest().copy(AmountInPence(12))).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"Cannot update UpfrontPaymentAmount when journey is in completed state"}""")

      verifyCommonActions(numberOfAuthCalls = 1)
    }
  }
}
