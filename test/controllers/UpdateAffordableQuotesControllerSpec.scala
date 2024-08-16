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

import essttp.journey.model.Journey
import essttp.rootmodel.ttp.affordablequotes.AffordableQuotesResponse
import paymentsEmailVerification.models.EmailVerificationResult
import testsupport.ItSpec
import testsupport.testdata.TdAll

class UpdateAffordableQuotesControllerSpec extends ItSpec with UpdateJourneyControllerSpec {

  "POST /journey/:journeyId/update-affordable-quotes" - {

    "should throw Bad Request when Journey is in a stage" - {
      "[BeforeStartDatesResponse]" in new JourneyItTest {
        stubCommonActions()

        journeyConnector.Epaye.startJourneyBta(tdAll.EpayeBta.sjRequest).futureValue
        val result: Throwable = journeyConnector.updateAffordableQuotes(tdAll.journeyId, TdAll.EpayeBta.updateAffordableQuotesResponse()).failed.futureValue
        result.getMessage should include("""{"statusCode":400,"message":"UpdateAffordableQuotes is not possible in that state: [Started]"}""")

        verifyCommonActions(numberOfAuthCalls = 2)
      }

      "[AfterStartedPegaCase]" in new JourneyItTest {
        insertJourneyForTest(tdAll.EpayeBta.journeyAfterStartedPegaCase)
        stubCommonActions()

        val result: Throwable = journeyConnector.updateAffordableQuotes(tdAll.journeyId, TdAll.EpayeBta.updateAffordableQuotesResponse()).failed.futureValue
        result.getMessage should include("""{"statusCode":400,"message":"Not expecting to update AffordableQuotes after starting PEGA case"}""")

        verifyCommonActions(numberOfAuthCalls = 1)
      }

      "[AfterCheckedPaymentPlan] when the user has gone through the affordability journey" in new JourneyItTest {
        insertJourneyForTest(tdAll.EpayeBta.journeyAfterCheckedPaymentPlanWithAffordability)
        stubCommonActions()

        val result: Throwable = journeyConnector.updateAffordableQuotes(tdAll.journeyId, TdAll.EpayeBta.updateAffordableQuotesResponse()).failed.futureValue
        result.getMessage should include("""{"statusCode":400,"message":"Cannot update AffordableQuotesResponse on affordability journey"}""")

        verifyCommonActions(numberOfAuthCalls = 1)
      }

    }

    "should update the journey when an existing value didn't exist before for" - {

      "Epaye" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.EpayeBta.journeyAfterStartDatesResponse,
          TdAll.EpayeBta.updateAffordableQuotesResponse()
        )(
            journeyConnector.updateAffordableQuotes,
            tdAll.EpayeBta.journeyAfterAffordableQuotesResponse
          )(this)
      }

      "Vat" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.VatBta.journeyAfterStartDatesResponse,
          TdAll.VatBta.updateAffordableQuotesResponse()
        )(
            journeyConnector.updateAffordableQuotes,
            tdAll.VatBta.journeyAfterAffordableQuotesResponse
          )(this)
      }

      "Sa" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.SaBta.journeyAfterStartDatesResponse,
          TdAll.SaBta.updateAffordableQuotesResponse()
        )(
            journeyConnector.updateAffordableQuotes,
            tdAll.SaBta.journeyAfterAffordableQuotesResponse
          )(this)
      }
    }

    "should update the journey when an Instalment amounts already existed" - {

      val differentAffordableQuotes =
        TdAll.affordableQuotesResponse.copy(paymentPlans = List(TdAll.paymentPlan(436)))

      "Epaye when the current stage is" - {

          def testEpayeBta[J <: Journey](initialJourney: J)(existingValue: J => AffordableQuotesResponse)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentAffordableQuotes,
                journeyConnector.updateAffordableQuotes(_, _)(context.request),
                context.tdAll.EpayeBta.journeyAfterAffordableQuotesResponse.copy(affordableQuotesResponse = differentAffordableQuotes)
              )(context)

        "RetrievedAffordableQuotes" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterAffordableQuotesResponse)(_.affordableQuotesResponse)(this)
        }

        "ChosenPaymentPlan" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterSelectedPaymentPlan)(_.affordableQuotesResponse)(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterCheckedPaymentPlanNonAffordability)(_.paymentPlanAnswers.nonAffordabilityAnswers.affordableQuotesResponse)(this)
        }

        "EnteredDetailsAboutBankAccount" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEnteredDetailsAboutBankAccountNoAffordability(isAccountHolder = true))(_.paymentPlanAnswers.nonAffordabilityAnswers.affordableQuotesResponse)(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(_.paymentPlanAnswers.nonAffordabilityAnswers.affordableQuotesResponse)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(_.paymentPlanAnswers.nonAffordabilityAnswers.affordableQuotesResponse)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(_.paymentPlanAnswers.nonAffordabilityAnswers.affordableQuotesResponse)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterSelectedEmailNoAffordability)(_.paymentPlanAnswers.nonAffordabilityAnswers.affordableQuotesResponse)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(_.paymentPlanAnswers.nonAffordabilityAnswers.affordableQuotesResponse)(this)
        }

      }

      "Vat when the current stage is" - {

          def testVatBta[J <: Journey](initialJourney: J)(existingValue: J => AffordableQuotesResponse)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentAffordableQuotes,
                journeyConnector.updateAffordableQuotes(_, _)(context.request),
                context.tdAll.VatBta.journeyAfterAffordableQuotesResponse.copy(affordableQuotesResponse = differentAffordableQuotes)
              )(context)

        "RetrievedAffordableQuotes" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterAffordableQuotesResponse)(_.affordableQuotesResponse)(this)
        }

        "ChosenPaymentPlan" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterSelectedPaymentPlan)(_.affordableQuotesResponse)(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterCheckedPaymentPlanNonAffordability)(_.paymentPlanAnswers.nonAffordabilityAnswers.affordableQuotesResponse)(this)
        }

        "EnteredDetailsAboutBankAccount" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEnteredDetailsAboutBankAccountNoAffordability(isAccountHolder = true))(_.paymentPlanAnswers.nonAffordabilityAnswers.affordableQuotesResponse)(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(_.paymentPlanAnswers.nonAffordabilityAnswers.affordableQuotesResponse)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(_.paymentPlanAnswers.nonAffordabilityAnswers.affordableQuotesResponse)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(_.paymentPlanAnswers.nonAffordabilityAnswers.affordableQuotesResponse)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterSelectedEmailNoAffordability)(_.paymentPlanAnswers.nonAffordabilityAnswers.affordableQuotesResponse)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(_.paymentPlanAnswers.nonAffordabilityAnswers.affordableQuotesResponse)(this)
        }

      }

      "Sa when the current stage is" - {

          def testSaBta[J <: Journey](initialJourney: J)(existingValue: J => AffordableQuotesResponse)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentAffordableQuotes,
                journeyConnector.updateAffordableQuotes(_, _)(context.request),
                context.tdAll.SaBta.journeyAfterAffordableQuotesResponse.copy(affordableQuotesResponse = differentAffordableQuotes)
              )(context)

        "RetrievedAffordableQuotes" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterAffordableQuotesResponse)(_.affordableQuotesResponse)(this)
        }

        "ChosenPaymentPlan" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterSelectedPaymentPlan)(_.affordableQuotesResponse)(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterCheckedPaymentPlanNonAffordability)(_.paymentPlanAnswers.nonAffordabilityAnswers.affordableQuotesResponse)(this)
        }

        "EnteredDetailsAboutBankAccount" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEnteredDetailsAboutBankAccountNoAffordability(isAccountHolder = true))(_.paymentPlanAnswers.nonAffordabilityAnswers.affordableQuotesResponse)(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(_.paymentPlanAnswers.nonAffordabilityAnswers.affordableQuotesResponse)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(_.paymentPlanAnswers.nonAffordabilityAnswers.affordableQuotesResponse)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(_.paymentPlanAnswers.nonAffordabilityAnswers.affordableQuotesResponse)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterSelectedEmailNoAffordability)(_.paymentPlanAnswers.nonAffordabilityAnswers.affordableQuotesResponse)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(_.paymentPlanAnswers.nonAffordabilityAnswers.affordableQuotesResponse)(this)
        }

      }

    }

    "should throw a Bad Request when journey is in stage SubmittedArrangement" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterSubmittedArrangementNoAffordability().copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      val result: Throwable = journeyConnector.updateAffordableQuotes(tdAll.journeyId, tdAll.EpayeBta.updateAffordableQuotesResponse()).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"Cannot update AffordableQuotes when journey is in completed state"}""")

      verifyCommonActions(numberOfAuthCalls = 1)
    }

  }
}
