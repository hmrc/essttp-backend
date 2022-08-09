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
import essttp.journey.model.JourneyId
import essttp.rootmodel.AmountInPence
import essttp.testdata.TdAll
import play.api.mvc.RequestHeader
import testsupport.ItSpec

class UpdateMonthlyPaymentAmountControllerSpec extends ItSpec {

  def journeyConnector: JourneyConnector = app.injector.instanceOf[JourneyConnector]

  def primeJourneyToAffordabilityResult(journeyId: JourneyId)(implicit requestHeader: RequestHeader): Unit = {
    journeyConnector.Epaye.startJourneyBta(TdAll.EpayeBta.sjRequest).futureValue
    journeyConnector.updateTaxId(journeyId, TdAll.EpayeBta.updateTaxIdRequest()).futureValue
    journeyConnector.updateEligibilityCheckResult(journeyId, TdAll.EpayeBta.updateEligibilityCheckRequest()).futureValue
    journeyConnector.updateCanPayUpfront(journeyId, TdAll.EpayeBta.updateCanPayUpfrontYesRequest()).futureValue
    journeyConnector.updateUpfrontPaymentAmount(journeyId, TdAll.EpayeBta.updateUpfrontPaymentAmountRequest()).futureValue
    journeyConnector.updateExtremeDates(journeyId, TdAll.EpayeBta.updateExtremeDatesRequest()).futureValue
    journeyConnector.updateAffordabilityResult(journeyId, TdAll.EpayeBta.updateInstalmentAmountsRequest()).futureValue
  }

  "POST /journey/:journeyId/update-monthly-payment-amount" - {
    "should throw Bad Request when Journey is in a stage [BeforeRetrievedAffordabilityResult]" in new JourneyItTest {
      journeyConnector.Epaye.startJourneyBta(TdAll.EpayeBta.sjRequest).futureValue
      val result: Throwable = journeyConnector.updateMonthlyPaymentAmount(tdAll.journeyId, TdAll.EpayeBta.updateMonthlyPaymentAmountRequest()).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"UpdateMonthlyPaymentAmount update is not possible in that state: [Started]"}""")
    }
    "should not update the journey when Monthly payment amount hasn't changed" in new JourneyItTest {
      insertJourneyForTest(TdAll.EpayeBta.journeyAfterInstalmentAmounts.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      journeyConnector.updateMonthlyPaymentAmount(tdAll.journeyId, TdAll.EpayeBta.updateMonthlyPaymentAmountRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterMonthlyPaymentAmount
      journeyConnector.updateMonthlyPaymentAmount(tdAll.journeyId, TdAll.EpayeBta.updateMonthlyPaymentAmountRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterMonthlyPaymentAmount
    }
    "should update the journey when Monthly payment amount has changed" in new JourneyItTest {
      insertJourneyForTest(TdAll.EpayeBta.journeyAfterInstalmentAmounts.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      journeyConnector.updateMonthlyPaymentAmount(tdAll.journeyId, TdAll.EpayeBta.updateMonthlyPaymentAmountRequest()).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterMonthlyPaymentAmount
      journeyConnector.updateMonthlyPaymentAmount(tdAll.journeyId, TdAll.EpayeBta.updateMonthlyPaymentAmountRequest().copy(AmountInPence(999))).futureValue
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterMonthlyPaymentAmount.copy(monthlyPaymentAmount = this.tdAll.monthlyPaymentAmount.copy(AmountInPence(999)))
    }
    "should throw a Bad Request when journey is in stage SubmittedArrangement" in new JourneyItTest {
      insertJourneyForTest(TdAll.EpayeBta.journeyAfterSubmittedArrangement.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      val result: Throwable = journeyConnector.updateMonthlyPaymentAmount(tdAll.journeyId, tdAll.EpayeBta.updateMonthlyPaymentAmountRequest()).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"Cannot update MonthlyAmount when journey is in completed state"}""")
    }
  }
}
