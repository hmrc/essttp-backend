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

import essttp.crypto.CryptoFormat.OperationalCryptoFormat
import essttp.journey.JourneyConnector
import essttp.rootmodel.AmountInPence
import essttp.testdata.TdAll
import journey.JourneyInFinalStateSpec.TestScenario
import org.scalatest.Assertion
import play.api.libs.json.{JsNull, Writes}
import testsupport.ItSpec
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HttpClient, HttpResponse}

import scala.collection.immutable

class JourneyInFinalStateSpec extends ItSpec {

  def journeyConnector: JourneyConnector = app.injector.instanceOf[JourneyConnector]

  "should not be able to update journey once it is completed" in new JourneyItTest {
    stubCommonActions()

    val httpClient: HttpClient = app.injector.instanceOf[HttpClient]
    implicit val cryptoFormat: OperationalCryptoFormat = app.injector.instanceOf[OperationalCryptoFormat]

    def makeUpdate[A](url: String, payload: A)(implicit writes: Writes[A]): HttpResponse =
      httpClient.POST[A, HttpResponse](
        url     = s"$baseUrl/essttp-backend/journey/${tdAll.journeyId.value}$url",
        body    = payload,
        headers = Seq("Authorization" -> TdAll.authorization.value)
      ).futureValue

    def extractAndAssert(testScenario: TestScenario): Assertion = {
      testScenario.httpResponse.status shouldBe testScenario.expectedStatusCode withClue s"Response body wasn't ${testScenario.expectedMessage}"
      testScenario.httpResponse.body shouldBe testScenario.expectedMessage withClue s"Response body wasn't ${testScenario.expectedMessage}"
    }

    insertJourneyForTest(TdAll.EpayeBta.journeyAfterSubmittedArrangement.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))

    val scenarios: immutable.Seq[TestScenario] = List(
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
        httpResponse       = makeUpdate("/update-upfront-payment-amount", tdAll.EpayeBta.updateUpfrontPaymentAmountRequest().copy(AmountInPence(13))),
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
        httpResponse       = makeUpdate("/update-details-about-bank-account", tdAll.EpayeBta.updateDetailsAboutBankAccountRequest(isAccountHolder = true)),
        expectedStatusCode = 400,
        expectedMessage    = """{"statusCode":400,"message":"Cannot update DetailsAboutBankAccount when journey is in completed state"}"""
      ),
      TestScenario(
        httpResponse       = makeUpdate("/update-direct-debit-details", tdAll.EpayeBta.updateDirectDebitDetailsRequest),
        expectedStatusCode = 400,
        expectedMessage    = """{"statusCode":400,"message":"Cannot update DirectDebitDetails when journey is in completed state"}"""
      ),
      TestScenario(
        httpResponse       = makeUpdate("/update-has-confirmed-direct-debit-details", JsNull),
        expectedStatusCode = 400,
        expectedMessage    = """{"statusCode":400,"message":"Cannot update ConfirmedDirectDebitDetails when journey is in completed state"}"""
      ),
      TestScenario(
        httpResponse       = makeUpdate("/update-has-agreed-terms-and-conditions", tdAll.EpayeBta.updateAgreedTermsAndConditionsRequest(isEmailAddressRequired = false)),
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
