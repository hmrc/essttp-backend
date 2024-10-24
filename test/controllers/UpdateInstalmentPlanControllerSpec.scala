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
import essttp.rootmodel.ttp.affordablequotes.PaymentPlan
import paymentsEmailVerification.models.EmailVerificationResult
import testsupport.ItSpec
import testsupport.testdata.TdAll

class UpdateInstalmentPlanControllerSpec extends ItSpec with UpdateJourneyControllerSpec {

  "POST /journey/:journeyId/update-selected-plan" - {
    "should throw Bad Request when Journey is in a stage" - {
      "[BeforeAffordableQuotesResponse]" in new JourneyItTest {
        stubCommonActions()

        journeyConnector.Epaye.startJourneyBta(TdAll.EpayeBta.sjRequest).futureValue
        val result: Throwable = journeyConnector.updateChosenPaymentPlan(tdAll.journeyId, TdAll.EpayeBta.updateSelectedPaymentPlanRequest()).failed.futureValue
        result.getMessage should include("""{"statusCode":400,"message":"UpdateSelectedPaymentPlan is not possible in that state: [Started]"}""")

        verifyCommonActions(numberOfAuthCalls = 2)
      }

      "[AfterStartedPegaCase]" in new JourneyItTest {
        insertJourneyForTest(tdAll.EpayeBta.journeyAfterStartedPegaCase)
        stubCommonActions()

        val result: Throwable = journeyConnector.updateChosenPaymentPlan(tdAll.journeyId, tdAll.EpayeBta.updateSelectedPaymentPlanRequest()).failed.futureValue
        result.getMessage should include("""{"statusCode":400,"message":"Not expecting to update SelectedPaymentPlan after starting PEGA case"}""")

        verifyCommonActions(numberOfAuthCalls = 1)
      }

      "[AfterCheckedPaymentPlan] when the user has gone through the affordability journey" in new JourneyItTest {
        insertJourneyForTest(tdAll.EpayeBta.journeyAfterCheckedPaymentPlanWithAffordability)
        stubCommonActions()

        val result: Throwable = journeyConnector.updateChosenPaymentPlan(tdAll.journeyId, TdAll.EpayeBta.updateSelectedPaymentPlanRequest()).failed.futureValue
        result.getMessage should include("""{"statusCode":400,"message":"Cannot update SelectedPaymentPlan on affordability journey"}""")

        verifyCommonActions(numberOfAuthCalls = 1)
      }
    }

    "should update the journey when an existing value didn't exist before for" - {

      "Epaye" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.EpayeBta.journeyAfterAffordableQuotesResponse,
          TdAll.EpayeBta.updateSelectedPaymentPlanRequest()
        )(
            journeyConnector.updateChosenPaymentPlan,
            tdAll.EpayeBta.journeyAfterSelectedPaymentPlan
          )(this)
      }

      "Vat" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.VatBta.journeyAfterAffordableQuotesResponse,
          TdAll.VatBta.updateSelectedPaymentPlanRequest()
        )(
            journeyConnector.updateChosenPaymentPlan,
            tdAll.VatBta.journeyAfterSelectedPaymentPlan
          )(this)
      }

      "Sa" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.SaBta.journeyAfterAffordableQuotesResponse,
          TdAll.SaBta.updateSelectedPaymentPlanRequest()
        )(
            journeyConnector.updateChosenPaymentPlan,
            tdAll.SaBta.journeyAfterSelectedPaymentPlan
          )(this)
      }

      "Sia" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.SiaPta.journeyAfterAffordableQuotesResponse,
          TdAll.SiaPta.updateSelectedPaymentPlanRequest()
        )(
            journeyConnector.updateChosenPaymentPlan,
            tdAll.SiaPta.journeyAfterSelectedPaymentPlan
          )(this)
      }
    }

    "should update the journey when a isAccountHolder already existed" - {

      val differentPaymentPlan = TdAll.paymentPlan(3498)

      "Epaye when the current stage is" - {

          def testEpayeBta[J <: Journey](initialJourney: J)(existingValue: J => PaymentPlan)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentPaymentPlan,
                journeyConnector.updateChosenPaymentPlan(_, _)(context.request),
                context.tdAll.EpayeBta.journeyAfterSelectedPaymentPlan.copy(selectedPaymentPlan = differentPaymentPlan)
              )(context)

        "ChosenPaymentPlan" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterSelectedPaymentPlan)(_.selectedPaymentPlan)(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterCheckedPaymentPlanNonAffordability)(_.paymentPlanAnswers.nonAffordabilityAnswers.selectedPaymentPlan)(this)
        }

        "EnteredCanYouSetUpDirectDebit" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true))(_.paymentPlanAnswers.nonAffordabilityAnswers.selectedPaymentPlan)(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(_.paymentPlanAnswers.nonAffordabilityAnswers.selectedPaymentPlan)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(_.paymentPlanAnswers.nonAffordabilityAnswers.selectedPaymentPlan)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(_.paymentPlanAnswers.nonAffordabilityAnswers.selectedPaymentPlan)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterSelectedEmailNoAffordability)(_.paymentPlanAnswers.nonAffordabilityAnswers.selectedPaymentPlan)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(_.paymentPlanAnswers.nonAffordabilityAnswers.selectedPaymentPlan)(this)
        }

      }

      "Vat when the current stage is" - {

          def testVatBta[J <: Journey](initialJourney: J)(existingValue: J => PaymentPlan)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentPaymentPlan,
                journeyConnector.updateChosenPaymentPlan(_, _)(context.request),
                context.tdAll.VatBta.journeyAfterSelectedPaymentPlan.copy(selectedPaymentPlan = differentPaymentPlan)
              )(context)

        "ChosenPaymentPlan" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterSelectedPaymentPlan)(_.selectedPaymentPlan)(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterCheckedPaymentPlanNonAffordability)(_.paymentPlanAnswers.nonAffordabilityAnswers.selectedPaymentPlan)(this)
        }

        "EnteredCanYouSetUpDirectDebit" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true))(_.paymentPlanAnswers.nonAffordabilityAnswers.selectedPaymentPlan)(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(_.paymentPlanAnswers.nonAffordabilityAnswers.selectedPaymentPlan)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(_.paymentPlanAnswers.nonAffordabilityAnswers.selectedPaymentPlan)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(_.paymentPlanAnswers.nonAffordabilityAnswers.selectedPaymentPlan)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterSelectedEmailNoAffordability)(_.paymentPlanAnswers.nonAffordabilityAnswers.selectedPaymentPlan)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(_.paymentPlanAnswers.nonAffordabilityAnswers.selectedPaymentPlan)(this)
        }

      }

      "Sa when the current stage is" - {

          def testSaBta[J <: Journey](initialJourney: J)(existingValue: J => PaymentPlan)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentPaymentPlan,
                journeyConnector.updateChosenPaymentPlan(_, _)(context.request),
                context.tdAll.SaBta.journeyAfterSelectedPaymentPlan.copy(selectedPaymentPlan = differentPaymentPlan)
              )(context)

        "ChosenPaymentPlan" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterSelectedPaymentPlan)(_.selectedPaymentPlan)(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterCheckedPaymentPlanNonAffordability)(_.paymentPlanAnswers.nonAffordabilityAnswers.selectedPaymentPlan)(this)
        }

        "EnteredCanYouSetUpDirectDebit" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true))(_.paymentPlanAnswers.nonAffordabilityAnswers.selectedPaymentPlan)(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(_.paymentPlanAnswers.nonAffordabilityAnswers.selectedPaymentPlan)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(_.paymentPlanAnswers.nonAffordabilityAnswers.selectedPaymentPlan)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(_.paymentPlanAnswers.nonAffordabilityAnswers.selectedPaymentPlan)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterSelectedEmailNoAffordability)(_.paymentPlanAnswers.nonAffordabilityAnswers.selectedPaymentPlan)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(_.paymentPlanAnswers.nonAffordabilityAnswers.selectedPaymentPlan)(this)
        }

      }

      "Sia when the current stage is" - {

          def testSiaPta[J <: Journey](initialJourney: J)(existingValue: J => PaymentPlan)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentPaymentPlan,
                journeyConnector.updateChosenPaymentPlan(_, _)(context.request),
                context.tdAll.SiaPta.journeyAfterSelectedPaymentPlan.copy(selectedPaymentPlan = differentPaymentPlan)
              )(context)

        "ChosenPaymentPlan" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterSelectedPaymentPlan)(_.selectedPaymentPlan)(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterCheckedPaymentPlanNonAffordability)(_.paymentPlanAnswers.nonAffordabilityAnswers.selectedPaymentPlan)(this)
        }

        "EnteredCanYouSetUpDirectDebit" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true))(_.paymentPlanAnswers.nonAffordabilityAnswers.selectedPaymentPlan)(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(_.paymentPlanAnswers.nonAffordabilityAnswers.selectedPaymentPlan)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(_.paymentPlanAnswers.nonAffordabilityAnswers.selectedPaymentPlan)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(_.paymentPlanAnswers.nonAffordabilityAnswers.selectedPaymentPlan)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterSelectedEmail)(_.paymentPlanAnswers.nonAffordabilityAnswers.selectedPaymentPlan)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified))(_.paymentPlanAnswers.nonAffordabilityAnswers.selectedPaymentPlan)(this)
        }

      }

    }

    "should throw a Bad Request when journey is in stage SubmittedArrangement" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterSubmittedArrangementNoAffordability().copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      val result: Throwable = journeyConnector.updateChosenPaymentPlan(tdAll.journeyId, tdAll.EpayeBta.updateSelectedPaymentPlanRequest()).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"Cannot update ChosenPlan when journey is in completed state"}""")

      verifyCommonActions(numberOfAuthCalls = 1)
    }
  }
}
