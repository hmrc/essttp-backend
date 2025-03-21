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

import essttp.journey.model.{Journey, PaymentPlanAnswers}
import essttp.rootmodel.pega.PegaCaseId
import essttp.rootmodel.{AmountInPence, MonthlyPaymentAmount}
import paymentsEmailVerification.models.EmailVerificationResult
import testsupport.ItSpec
import testsupport.testdata.TdAll

class UpdateHasCheckedInstalmentPlanControllerSpec extends ItSpec, UpdateJourneyControllerSpec {

  "POST /journey/:journeyId/update-has-checked-plan" - {
    "should throw Bad Request when Journey is in a stage [BeforeSelectedPaymentPlan]" in new JourneyItTest {
      stubCommonActions()

      journeyConnector.Epaye.startJourneyBta(TdAll.EpayeBta.sjRequest).futureValue
      val result: Throwable = journeyConnector
        .updateHasCheckedPaymentPlan(
          tdAll.journeyId,
          tdAll.paymentPlanAnswersNoAffordability
        )
        .failed
        .futureValue
      result.getMessage should include(
        """{"statusCode":400,"message":"UpdateHasCheckedInstalmentPlan is not possible in that state: [Started]"}"""
      )

      verifyCommonActions(numberOfAuthCalls = 2)
    }

    "should update the journey when an existing value didn't exist before for" - {

      "Epaye on affordability journey" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.EpayeBta.journeyAfterStartedPegaCase,
          tdAll.paymentPlanAnswersWithAffordability
        )(
          journeyConnector.updateHasCheckedPaymentPlan,
          tdAll.EpayeBta.journeyAfterCheckedPaymentPlanWithAffordability.copy(pegaCaseId = Some(PegaCaseId("case-id")))
        )(this)
      }

      "Epaye on non-affordability journey" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.EpayeBta.journeyAfterSelectedPaymentPlan,
          tdAll.paymentPlanAnswersNoAffordability
        )(
          journeyConnector.updateHasCheckedPaymentPlan,
          tdAll.EpayeBta.journeyAfterCheckedPaymentPlanNonAffordability
        )(this)
      }

      "Vat on affordability journey" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.VatBta.journeyAfterStartedPegaCase,
          tdAll.paymentPlanAnswersWithAffordability
        )(
          journeyConnector.updateHasCheckedPaymentPlan,
          tdAll.VatBta.journeyAfterCheckedPaymentPlanWithAffordability.copy(pegaCaseId = Some(PegaCaseId("case-id")))
        )(this)
      }

      "Vat on non-affordability journey" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.VatBta.journeyAfterSelectedPaymentPlan,
          tdAll.paymentPlanAnswersNoAffordability
        )(
          journeyConnector.updateHasCheckedPaymentPlan,
          tdAll.VatBta.journeyAfterCheckedPaymentPlanNonAffordability
        )(this)
      }

      "Sa on affordability journey" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.SaBta.journeyAfterStartedPegaCase,
          tdAll.paymentPlanAnswersWithAffordability
        )(
          journeyConnector.updateHasCheckedPaymentPlan,
          tdAll.SaBta.journeyAfterCheckedPaymentPlanWithAffordability
        )(this)
      }

      "Sa on non-affordability journey" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.SaBta.journeyAfterSelectedPaymentPlan,
          tdAll.paymentPlanAnswersNoAffordability
        )(
          journeyConnector.updateHasCheckedPaymentPlan,
          tdAll.SaBta.journeyAfterCheckedPaymentPlanNonAffordability
        )(this)
      }

      "Simp on non-affordability journey" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.SimpPta.journeyAfterSelectedPaymentPlan,
          tdAll.paymentPlanAnswersNoAffordability
        )(
          journeyConnector.updateHasCheckedPaymentPlan,
          tdAll.SimpPta.journeyAfterCheckedPaymentPlanNonAffordability
        )(this)
      }
    }

    "should update the journey when a value already existed" - {

      val differentAnswers = TdAll.paymentPlanAnswersNoAffordability.copy(
        monthlyPaymentAmount = MonthlyPaymentAmount(AmountInPence(6723478L))
      )

      "Epaye when the current stage is" - {

        def testEpayeBta[J <: Journey](
          initialJourney: J
        )(existingValue: J => PaymentPlanAnswers)(context: JourneyItTest): Unit =
          testUpdateWithExistingValue(initialJourney)(
            _.journeyId,
            existingValue(initialJourney)
          )(
            differentAnswers,
            journeyConnector.updateHasCheckedPaymentPlan(_, _)(using context.request),
            context.tdAll.EpayeBta.journeyAfterCheckedPaymentPlanNonAffordability.copy(
              paymentPlanAnswers = differentAnswers
            )
          )(context)

        "CheckedPaymentPlan" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterCheckedPaymentPlanNonAffordability)(_.paymentPlanAnswers)(this)
        }

        "EnteredCanYouSetUpDirectDebit" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true))(
            _.paymentPlanAnswers
          )(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(_.paymentPlanAnswers)(
            this
          )
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(_.paymentPlanAnswers)(
            this
          )
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testEpayeBta(
            tdAll.EpayeBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true)
          )(_.paymentPlanAnswers)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterSelectedEmailNoAffordability)(_.paymentPlanAnswers)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testEpayeBta(
            tdAll.EpayeBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified)
          )(_.paymentPlanAnswers)(this)
        }

      }

      "Vat when the current stage is" - {

        def testVatBta[J <: Journey](
          initialJourney: J
        )(existingValue: J => PaymentPlanAnswers)(context: JourneyItTest): Unit =
          testUpdateWithExistingValue(initialJourney)(
            _.journeyId,
            existingValue(initialJourney)
          )(
            differentAnswers,
            journeyConnector.updateHasCheckedPaymentPlan(_, _)(using context.request),
            context.tdAll.VatBta.journeyAfterCheckedPaymentPlanNonAffordability.copy(
              paymentPlanAnswers = differentAnswers
            )
          )(context)

        "CheckedPaymentPlan" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterCheckedPaymentPlanNonAffordability)(_.paymentPlanAnswers)(this)
        }

        "EnteredCanYouSetUpDirectDebit" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true))(
            _.paymentPlanAnswers
          )(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(_.paymentPlanAnswers)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(_.paymentPlanAnswers)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(
            _.paymentPlanAnswers
          )(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterSelectedEmailNoAffordability)(_.paymentPlanAnswers)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(
            _.paymentPlanAnswers
          )(this)
        }

      }

      "Sa when the current stage is" - {

        def testSaBta[J <: Journey](
          initialJourney: J
        )(existingValue: J => PaymentPlanAnswers)(context: JourneyItTest): Unit =
          testUpdateWithExistingValue(initialJourney)(
            _.journeyId,
            existingValue(initialJourney)
          )(
            differentAnswers,
            journeyConnector.updateHasCheckedPaymentPlan(_, _)(using context.request),
            context.tdAll.SaBta.journeyAfterCheckedPaymentPlanNonAffordability.copy(
              paymentPlanAnswers = differentAnswers
            )
          )(context)

        "CheckedPaymentPlan" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterCheckedPaymentPlanNonAffordability)(_.paymentPlanAnswers)(this)
        }

        "EnteredCanYouSetUpDirectDebit" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true))(
            _.paymentPlanAnswers
          )(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(_.paymentPlanAnswers)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(_.paymentPlanAnswers)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(
            _.paymentPlanAnswers
          )(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterSelectedEmailNoAffordability)(_.paymentPlanAnswers)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(
            _.paymentPlanAnswers
          )(this)
        }

      }

      "Simp when the current stage is" - {

        def testSimpPta[J <: Journey](
          initialJourney: J
        )(existingValue: J => PaymentPlanAnswers)(context: JourneyItTest): Unit =
          testUpdateWithExistingValue(initialJourney)(
            _.journeyId,
            existingValue(initialJourney)
          )(
            differentAnswers,
            journeyConnector.updateHasCheckedPaymentPlan(_, _)(using context.request),
            context.tdAll.SimpPta.journeyAfterCheckedPaymentPlanNonAffordability.copy(
              paymentPlanAnswers = differentAnswers
            )
          )(context)

        "CheckedPaymentPlan" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterCheckedPaymentPlanNonAffordability)(_.paymentPlanAnswers)(this)
        }

        "EnteredCanYouSetUpDirectDebit" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true))(
            _.paymentPlanAnswers
          )(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(_.paymentPlanAnswers)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(_.paymentPlanAnswers)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(
            _.paymentPlanAnswers
          )(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterSelectedEmail)(_.paymentPlanAnswers)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified))(
            _.paymentPlanAnswers
          )(this)
        }

      }

    }

    "should throw a Bad Request when journey is in stage SubmittedArrangement" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(
        TdAll.EpayeBta
          .journeyAfterSubmittedArrangementNoAffordability()
          .copy(_id = tdAll.journeyId)
          .copy(correlationId = tdAll.correlationId)
      )
      val result: Throwable = journeyConnector
        .updateHasCheckedPaymentPlan(tdAll.journeyId, tdAll.paymentPlanAnswersNoAffordability)
        .failed
        .futureValue
      result.getMessage should include(
        """{"statusCode":400,"message":"Cannot update HasCheckedPaymentPlan when journey is in completed state"}"""
      )

      verifyCommonActions(numberOfAuthCalls = 1)
    }
  }
}
