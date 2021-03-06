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

package journey

import essttp.journey.JourneyConnector
import essttp.journey.model.JourneyId
import essttp.testdata.TdAll
import journey.JourneyInFinalStateSpec.TestScenario
import org.scalatest.Assertion
import play.api.libs.json.{JsNull, Writes}
import play.api.mvc.Request
import testsupport.ItSpec
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HttpClient, HttpResponse}

class JourneyInFinalStateSpec extends ItSpec {

  def journeyConnector: JourneyConnector = app.injector.instanceOf[JourneyConnector]

  "should not be able to update journey once it is completed" in {
    val tdAll = new TdAll {
      override val journeyId: JourneyId = journeyIdGenerator.readNextJourneyId()
    }
    implicit val request: Request[_] = tdAll.request
    val httpClient = app.injector.instanceOf[HttpClient]

      def makeUpdate[A](url: String, payload: A)(implicit writes: Writes[A]): HttpResponse =
        httpClient.POST[A, HttpResponse](
          url  = s"$baseUrl/essttp-backend/journey/${tdAll.journeyId.value}$url",
          body = payload
        ).futureValue

      def extractAndAssert(testScenario: TestScenario): Assertion = {
        testScenario.httpResponse.status shouldBe testScenario.expectedStatusCode withClue s"Response body wasn't ${testScenario.expectedMessage}"
        testScenario.httpResponse.body shouldBe testScenario.expectedMessage withClue s"Response body wasn't ${testScenario.expectedMessage}"
      }

    journeyConnector.Epaye.startJourneyBta(tdAll.EpayeBta.sjRequest).futureValue
    journeyConnector.updateTaxId(tdAll.journeyId, tdAll.EpayeBta.updateTaxIdRequest()).futureValue
    journeyConnector.updateEligibilityCheckResult(tdAll.journeyId, tdAll.EpayeBta.updateEligibilityCheckRequest()).futureValue
    journeyConnector.updateCanPayUpfront(tdAll.journeyId, tdAll.EpayeBta.updateCanPayUpfrontYesRequest()).futureValue
    journeyConnector.updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.EpayeBta.updateUpfrontPaymentAmountRequest()).futureValue
    journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.EpayeBta.updateExtremeDatesRequest()).futureValue
    journeyConnector.updateAffordabilityResult(tdAll.journeyId, tdAll.EpayeBta.updateInstalmentAmountsRequest()).futureValue
    journeyConnector.updateMonthlyPaymentAmount(tdAll.journeyId, tdAll.EpayeBta.updateMonthlyPaymentAmountRequest()).futureValue
    journeyConnector.updateDayOfMonth(tdAll.journeyId, tdAll.EpayeBta.updateDayOfMonthRequest()).futureValue
    journeyConnector.updateStartDates(tdAll.journeyId, tdAll.EpayeBta.updateStartDatesResponse()).futureValue
    journeyConnector.updateAffordableQuotes(tdAll.journeyId, tdAll.EpayeBta.updateAffordableQuotesResponse()).futureValue
    journeyConnector.updateChosenPaymentPlan(tdAll.journeyId, tdAll.EpayeBta.updateSelectedPaymentPlanRequest()).futureValue
    journeyConnector.updateHasCheckedPaymentPlan(tdAll.journeyId).futureValue
    journeyConnector.updateChosenTypeOfBankAccount(tdAll.journeyId, tdAll.EpayeBta.updateChosenTypeOfBankAccountRequest()).futureValue
    journeyConnector.updateDirectDebitDetails(tdAll.journeyId, tdAll.EpayeBta.updateDirectDebitDetailsRequest(isAccountHolder = true)).futureValue
    journeyConnector.updateHasConfirmedDirectDebitDetails(tdAll.journeyId).futureValue
    journeyConnector.updateHasAgreedTermsAndConditions(tdAll.journeyId).futureValue
    journeyConnector.updateArrangement(tdAll.journeyId, tdAll.EpayeBta.updateArrangementRequest()).futureValue
    // journey complete

    val scenarios = List(
      TestScenario(
        httpResponse       = makeUpdate("/update-eligibility-result", tdAll.EpayeBta.updateEligibilityCheckRequest()),
        expectedStatusCode = 400,
        expectedMessage    = """{"statusCode":400,"message":"Cannot update EligibilityCheckResult when journey is in completed state"}"""
      ),
      TestScenario(
        httpResponse       = makeUpdate("/update-can-pay-upfront", tdAll.EpayeBta.updateCanPayUpfrontYesRequest()),
        expectedStatusCode = 400,
        expectedMessage    = """{"statusCode":400,"message":"Cannot update AnsweredCanPayUpFront when journey is in completed state"}"""
      ),
      TestScenario(
        httpResponse       = makeUpdate("/update-upfront-payment-amount", tdAll.EpayeBta.updateUpfrontPaymentAmountRequest()),
        expectedStatusCode = 400,
        expectedMessage    = """{"statusCode":400,"message":"Cannot update UpfrontPaymentAmount when journey is in completed state"}"""
      ),
      TestScenario(
        httpResponse       = makeUpdate("/update-extreme-dates", tdAll.EpayeBta.updateExtremeDatesRequest()),
        expectedStatusCode = 400,
        expectedMessage    = """{"statusCode":400,"message":"Cannot update ExtremeDates when journey is in completed state"}"""
      ),
      TestScenario(
        httpResponse       = makeUpdate("/update-affordability-result", tdAll.EpayeBta.updateInstalmentAmountsRequest()),
        expectedStatusCode = 400,
        expectedMessage    = """{"statusCode":400,"message":"Cannot update AffordabilityResult when journey is in completed state"}"""
      ),
      TestScenario(
        httpResponse       = makeUpdate("/update-monthly-payment-amount", tdAll.EpayeBta.updateMonthlyPaymentAmountRequest()),
        expectedStatusCode = 400,
        expectedMessage    = """{"statusCode":400,"message":"Cannot update MonthlyAmount when journey is in completed state"}"""
      ),
      TestScenario(
        httpResponse       = makeUpdate("/update-day-of-month", tdAll.EpayeBta.updateDayOfMonthRequest()),
        expectedStatusCode = 400,
        expectedMessage    = """{"statusCode":400,"message":"Cannot update DayOfMonth when journey is in completed state"}"""
      ),
      TestScenario(
        httpResponse       = makeUpdate("/update-start-dates", tdAll.EpayeBta.updateStartDatesResponse()),
        expectedStatusCode = 400,
        expectedMessage    = """{"statusCode":400,"message":"Cannot update StartDates when journey is in completed state"}"""
      ),
      TestScenario(
        httpResponse       = makeUpdate("/update-affordable-quotes", tdAll.EpayeBta.updateAffordableQuotesResponse()),
        expectedStatusCode = 400,
        expectedMessage    = """{"statusCode":400,"message":"Cannot update AffordableQuotes when journey is in completed state"}"""
      ),
      TestScenario(
        httpResponse       = makeUpdate("/update-selected-plan", tdAll.EpayeBta.updateSelectedPaymentPlanRequest()),
        expectedStatusCode = 400,
        expectedMessage    = """{"statusCode":400,"message":"Cannot update ChosenPlan when journey is in completed state"}"""
      ),
      TestScenario(
        httpResponse       = makeUpdate("/update-has-checked-plan", JsNull),
        expectedStatusCode = 400,
        expectedMessage    = """{"statusCode":400,"message":"Cannot update HasCheckedPaymentPlan when journey is in completed state"}"""
      ),
      TestScenario(
        httpResponse       = makeUpdate("/update-chosen-type-of-bank-account", tdAll.EpayeBta.updateChosenTypeOfBankAccountRequest()),
        expectedStatusCode = 400,
        expectedMessage    = """{"statusCode":400,"message":"Cannot update TypeOfBankAccount when journey is in completed state"}"""
      ),
      TestScenario(
        httpResponse       = makeUpdate("/update-direct-debit-details", tdAll.EpayeBta.updateDirectDebitDetailsRequest(true)),
        expectedStatusCode = 400,
        expectedMessage    = """{"statusCode":400,"message":"Cannot update DirectDebitDetails when journey is in completed state"}"""
      ),
      TestScenario(
        httpResponse       = makeUpdate("/update-has-confirmed-direct-debit-details", JsNull),
        expectedStatusCode = 400,
        expectedMessage    = """{"statusCode":400,"message":"Cannot update ConfirmedDirectDebitDetails when journey is in completed state"}"""
      ),
      TestScenario(
        httpResponse       = makeUpdate("/update-has-agreed-terms-and-conditions", JsNull),
        expectedStatusCode = 400,
        expectedMessage    = """{"statusCode":400,"message":"Cannot update AgreedTermsAndConditions when journey is in completed state"}"""
      ),
      TestScenario(
        httpResponse       = makeUpdate("/update-arrangement", tdAll.EpayeBta.updateArrangementRequest()),
        expectedStatusCode = 400,
        expectedMessage    = """{"statusCode":400,"message":"Cannot update SubmittedArrangement when journey is in completed state"}"""
      )
    )

    scenarios.foreach(scenario => extractAndAssert(scenario))
  }
}

object JourneyInFinalStateSpec {
  final case class TestScenario(httpResponse: HttpResponse, expectedStatusCode: Int, expectedMessage: String)
}
