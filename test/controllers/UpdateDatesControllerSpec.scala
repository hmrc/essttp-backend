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

import essttp.journey.model.{Journey, UpfrontPaymentAnswers}
import essttp.rootmodel.dates.InitialPaymentDate
import essttp.rootmodel.dates.extremedates.ExtremeDatesResponse
import essttp.rootmodel.dates.startdates.StartDatesResponse
import paymentsEmailVerification.models.EmailVerificationResult
import testsupport.ItSpec
import testsupport.testdata.TdAll

class UpdateDatesControllerSpec extends ItSpec with UpdateJourneyControllerSpec {

  "POST /journey/:journeyId/update-extreme-dates" - {
    "should throw Bad Request when Journey is in a stage [BeforeUpfrontPaymentAnswers]" in new JourneyItTest {
      stubCommonActions()

      journeyConnector.Epaye.startJourneyBta(TdAll.EpayeBta.sjRequest).futureValue
      val result: Throwable = journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.EpayeBta.updateExtremeDatesRequest()).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"UpdateExtremeDatesResponse update is not possible in that state: [Started]"}""")

      verifyCommonActions(numberOfAuthCalls = 2)
    }

    "should update the journey when an existing value didn't exist before for" - {

      "Epaye when" - {

        "the user has selected no upfront payment" in new JourneyItTest {
          testUpdateWithoutExistingValue(
            tdAll.EpayeBta.journeyAfterCanPayUpfrontNo,
            TdAll.EpayeBta.updateExtremeDatesRequest()
          )(
              journeyConnector.updateExtremeDates,
              tdAll.EpayeBta.journeyAfterExtremeDates.copy(upfrontPaymentAnswers = UpfrontPaymentAnswers.NoUpfrontPayment)
            )(this)
        }

        "the user has selected an upfront payment" in new JourneyItTest {
          testUpdateWithoutExistingValue(
            tdAll.EpayeBta.journeyAfterUpfrontPaymentAmount,
            TdAll.EpayeBta.updateExtremeDatesRequest()
          )(
              journeyConnector.updateExtremeDates,
              tdAll.EpayeBta.journeyAfterExtremeDates
            )(this)
        }
      }

      "Vat when" - {

        "the user has selected no upfront payment" in new JourneyItTest {
          testUpdateWithoutExistingValue(
            tdAll.VatBta.journeyAfterCanPayUpfrontNo,
            TdAll.VatBta.updateExtremeDatesRequest()
          )(
              journeyConnector.updateExtremeDates,
              tdAll.VatBta.journeyAfterExtremeDates.copy(upfrontPaymentAnswers = UpfrontPaymentAnswers.NoUpfrontPayment)
            )(this)
        }

        "the user has selected an upfront payment" in new JourneyItTest {
          testUpdateWithoutExistingValue(
            tdAll.VatBta.journeyAfterUpfrontPaymentAmount,
            TdAll.VatBta.updateExtremeDatesRequest()
          )(
              journeyConnector.updateExtremeDates,
              tdAll.VatBta.journeyAfterExtremeDates
            )(this)
        }
      }

      "Sa when" - {

        "the user has selected no upfront payment" in new JourneyItTest {
          testUpdateWithoutExistingValue(
            tdAll.SaBta.journeyAfterCanPayUpfrontNo,
            TdAll.SaBta.updateExtremeDatesRequest()
          )(
              journeyConnector.updateExtremeDates,
              tdAll.SaBta.journeyAfterExtremeDates.copy(upfrontPaymentAnswers = UpfrontPaymentAnswers.NoUpfrontPayment)
            )(this)
        }

        "the user has selected an upfront payment" in new JourneyItTest {
          testUpdateWithoutExistingValue(
            tdAll.SaBta.journeyAfterUpfrontPaymentAmount,
            TdAll.SaBta.updateExtremeDatesRequest()
          )(
              journeyConnector.updateExtremeDates,
              tdAll.SaBta.journeyAfterExtremeDates
            )(this)
        }
      }

    }

    "should update the journey when extreme dates already existed" - {

      val differentExtremeDates = TdAll.extremeDatesWithUpfrontPayment.copy(
        initialPaymentDate = Some(InitialPaymentDate(TdAll.initialPaymentDate.value.plusYears(1)))
      )

      "Epaye when the current stage is" - {

          def testEpayeBta[J <: Journey](initialJourney: J)(existingValue: J => ExtremeDatesResponse)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentExtremeDates,
                journeyConnector.updateExtremeDates(_, _)(context.request),
                context.tdAll.EpayeBta.journeyAfterExtremeDates.copy(extremeDatesResponse = differentExtremeDates)
              )(context)

        "RetrievedExtremeDates" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterExtremeDates)(_.extremeDatesResponse)(this)
        }

        "RetrievedAffordabilityResult" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterInstalmentAmounts)(_.extremeDatesResponse)(this)
        }

        "EnteredMonthlyPaymentAmount" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterMonthlyPaymentAmount)(_.extremeDatesResponse)(this)
        }

        "EnteredDayOfMonth" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterDayOfMonth)(_.extremeDatesResponse)(this)
        }

        "RetrievedStartDates" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterStartDatesResponse)(_.extremeDatesResponse)(this)
        }

        "RetrievedAffordableQuotes" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterAffordableQuotesResponse)(_.extremeDatesResponse)(this)
        }

        "ChosenPaymentPlan" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterSelectedPaymentPlan)(_.extremeDatesResponse)(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterCheckedPaymentPlan)(_.extremeDatesResponse)(this)
        }

        "EnteredDetailsAboutBankAccount" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder = true))(_.extremeDatesResponse)(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEnteredDirectDebitDetails())(_.extremeDatesResponse)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterConfirmedDirectDebitDetails)(_.extremeDatesResponse)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterAgreedTermsAndConditions(isEmailAddressRequired = true))(_.extremeDatesResponse)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterSelectedEmail)(_.extremeDatesResponse)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified))(_.extremeDatesResponse)(this)
        }

      }

      "Vat when the current stage is" - {

          def testVatBta[J <: Journey](initialJourney: J)(existingValue: J => ExtremeDatesResponse)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentExtremeDates,
                journeyConnector.updateExtremeDates(_, _)(context.request),
                context.tdAll.VatBta.journeyAfterExtremeDates.copy(extremeDatesResponse = differentExtremeDates)
              )(context)

        "RetrievedExtremeDates" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterExtremeDates)(_.extremeDatesResponse)(this)
        }

        "RetrievedAffordabilityResult" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterInstalmentAmounts)(_.extremeDatesResponse)(this)
        }

        "EnteredMonthlyPaymentAmount" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterMonthlyPaymentAmount)(_.extremeDatesResponse)(this)
        }

        "EnteredDayOfMonth" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterDayOfMonth)(_.extremeDatesResponse)(this)
        }

        "RetrievedStartDates" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterStartDatesResponse)(_.extremeDatesResponse)(this)
        }

        "RetrievedAffordableQuotes" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterAffordableQuotesResponse)(_.extremeDatesResponse)(this)
        }

        "ChosenPaymentPlan" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterSelectedPaymentPlan)(_.extremeDatesResponse)(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterCheckedPaymentPlan)(_.extremeDatesResponse)(this)
        }

        "EnteredDetailsAboutBankAccount" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder = true))(_.extremeDatesResponse)(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEnteredDirectDebitDetails())(_.extremeDatesResponse)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterConfirmedDirectDebitDetails)(_.extremeDatesResponse)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterAgreedTermsAndConditions(isEmailAddressRequired = true))(_.extremeDatesResponse)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterSelectedEmail)(_.extremeDatesResponse)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified))(_.extremeDatesResponse)(this)
        }

      }

      "Sa when the current stage is" - {

          def testSaBta[J <: Journey](initialJourney: J)(existingValue: J => ExtremeDatesResponse)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentExtremeDates,
                journeyConnector.updateExtremeDates(_, _)(context.request),
                context.tdAll.SaBta.journeyAfterExtremeDates.copy(extremeDatesResponse = differentExtremeDates)
              )(context)

        "RetrievedExtremeDates" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterExtremeDates)(_.extremeDatesResponse)(this)
        }

        "RetrievedAffordabilityResult" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterInstalmentAmounts)(_.extremeDatesResponse)(this)
        }

        "EnteredMonthlyPaymentAmount" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterMonthlyPaymentAmount)(_.extremeDatesResponse)(this)
        }

        "EnteredDayOfMonth" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterDayOfMonth)(_.extremeDatesResponse)(this)
        }

        "RetrievedStartDates" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterStartDatesResponse)(_.extremeDatesResponse)(this)
        }

        "RetrievedAffordableQuotes" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterAffordableQuotesResponse)(_.extremeDatesResponse)(this)
        }

        "ChosenPaymentPlan" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterSelectedPaymentPlan)(_.extremeDatesResponse)(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterCheckedPaymentPlan)(_.extremeDatesResponse)(this)
        }

        "EnteredDetailsAboutBankAccount" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder = true))(_.extremeDatesResponse)(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEnteredDirectDebitDetails())(_.extremeDatesResponse)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterConfirmedDirectDebitDetails)(_.extremeDatesResponse)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterAgreedTermsAndConditions(isEmailAddressRequired = true))(_.extremeDatesResponse)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterSelectedEmail)(_.extremeDatesResponse)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified))(_.extremeDatesResponse)(this)
        }

      }

    }

    "should throw a Bad Request when journey is in stage SubmittedArrangement" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterSubmittedArrangement().copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      val result: Throwable = journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.EpayeBta.updateExtremeDatesRequest()).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"Cannot update ExtremeDates when journey is in completed state"}""")

      verifyCommonActions(numberOfAuthCalls = 1)
    }
  }

  "POST /journey/:journeyId/update-start-dates" - {
    "should throw Bad Request when Journey is in a stage [BeforeEnteredDayOfMonth]" in new JourneyItTest {
      stubCommonActions()

      journeyConnector.Epaye.startJourneyBta(TdAll.EpayeBta.sjRequest).futureValue
      val result: Throwable = journeyConnector.updateStartDates(tdAll.journeyId, tdAll.EpayeBta.updateStartDatesResponse()).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"UpdateStartDates is not possible when we don't have a chosen day of month, stage: [ Started ]"}""")

      verifyCommonActions(numberOfAuthCalls = 2)
    }

    "should update the journey when an existing value didn't exist before for" - {

      "Epaye" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.EpayeBta.journeyAfterDayOfMonth,
          TdAll.EpayeBta.updateStartDatesResponse()
        )(
            journeyConnector.updateStartDates,
            tdAll.EpayeBta.journeyAfterStartDatesResponse
          )(this)
      }

      "Vat" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.VatBta.journeyAfterDayOfMonth,
          TdAll.VatBta.updateStartDatesResponse()
        )(
            journeyConnector.updateStartDates,
            tdAll.VatBta.journeyAfterStartDatesResponse
          )(this)
      }

      "Sa" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.SaBta.journeyAfterDayOfMonth,
          TdAll.SaBta.updateStartDatesResponse()
        )(
            journeyConnector.updateStartDates,
            tdAll.SaBta.journeyAfterStartDatesResponse
          )(this)
      }
    }

    "should update the journey when start dates response has changed" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterDayOfMonth.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))

      val result1 = journeyConnector.updateStartDates(tdAll.journeyId, tdAll.EpayeBta.updateStartDatesResponse()).futureValue
      result1 shouldBe tdAll.EpayeBta.journeyAfterStartDatesResponse
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterStartDatesResponse

      val result2 = journeyConnector.updateStartDates(tdAll.journeyId, TdAll.startDatesResponseWithoutInitialPayment).futureValue
      val expectedUpdatedJourney2 = tdAll.EpayeBta.journeyAfterStartDatesResponse.copy(startDatesResponse = TdAll.startDatesResponseWithoutInitialPayment)
      result2 shouldBe expectedUpdatedJourney2
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe expectedUpdatedJourney2

      verifyCommonActions(numberOfAuthCalls = 4)
    }

    "should update the journey when start dates already existed" - {

      val differentStartDates = TdAll.startDatesResponseWithInitialPayment.copy(
        initialPaymentDate = Some(InitialPaymentDate(TdAll.initialPaymentDate.value.plusYears(1)))
      )

      "Epaye when the current stage is" - {

          def testEpayeBta[J <: Journey](initialJourney: J)(existingValue: J => StartDatesResponse)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentStartDates,
                journeyConnector.updateStartDates(_, _)(context.request),
                context.tdAll.EpayeBta.journeyAfterStartDatesResponse.copy(startDatesResponse = differentStartDates)
              )(context)

        "RetrievedStartDates" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterStartDatesResponse)(_.startDatesResponse)(this)
        }

        "RetrievedAffordableQuotes" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterAffordableQuotesResponse)(_.startDatesResponse)(this)
        }

        "ChosenPaymentPlan" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterSelectedPaymentPlan)(_.startDatesResponse)(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterCheckedPaymentPlan)(_.startDatesResponse)(this)
        }

        "EnteredDetailsAboutBankAccount" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder = true))(_.startDatesResponse)(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEnteredDirectDebitDetails())(_.startDatesResponse)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterConfirmedDirectDebitDetails)(_.startDatesResponse)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterAgreedTermsAndConditions(isEmailAddressRequired = true))(_.startDatesResponse)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterSelectedEmail)(_.startDatesResponse)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified))(_.startDatesResponse)(this)
        }

      }

      "Vat when the current stage is" - {

          def testVatBta[J <: Journey](initialJourney: J)(existingValue: J => StartDatesResponse)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentStartDates,
                journeyConnector.updateStartDates(_, _)(context.request),
                context.tdAll.VatBta.journeyAfterStartDatesResponse.copy(startDatesResponse = differentStartDates)
              )(context)

        "RetrievedStartDates" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterStartDatesResponse)(_.startDatesResponse)(this)
        }

        "RetrievedAffordableQuotes" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterAffordableQuotesResponse)(_.startDatesResponse)(this)
        }

        "ChosenPaymentPlan" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterSelectedPaymentPlan)(_.startDatesResponse)(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterCheckedPaymentPlan)(_.startDatesResponse)(this)
        }

        "EnteredDetailsAboutBankAccount" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder = true))(_.startDatesResponse)(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEnteredDirectDebitDetails())(_.startDatesResponse)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterConfirmedDirectDebitDetails)(_.startDatesResponse)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterAgreedTermsAndConditions(isEmailAddressRequired = true))(_.startDatesResponse)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterSelectedEmail)(_.startDatesResponse)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified))(_.startDatesResponse)(this)
        }

      }

      "Sa when the current stage is" - {

          def testSaBta[J <: Journey](initialJourney: J)(existingValue: J => StartDatesResponse)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentStartDates,
                journeyConnector.updateStartDates(_, _)(context.request),
                context.tdAll.SaBta.journeyAfterStartDatesResponse.copy(startDatesResponse = differentStartDates)
              )(context)

        "RetrievedStartDates" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterStartDatesResponse)(_.startDatesResponse)(this)
        }

        "RetrievedAffordableQuotes" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterAffordableQuotesResponse)(_.startDatesResponse)(this)
        }

        "ChosenPaymentPlan" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterSelectedPaymentPlan)(_.startDatesResponse)(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterCheckedPaymentPlan)(_.startDatesResponse)(this)
        }

        "EnteredDetailsAboutBankAccount" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEnteredDetailsAboutBankAccount(isAccountHolder = true))(_.startDatesResponse)(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEnteredDirectDebitDetails())(_.startDatesResponse)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterConfirmedDirectDebitDetails)(_.startDatesResponse)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterAgreedTermsAndConditions(isEmailAddressRequired = true))(_.startDatesResponse)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterSelectedEmail)(_.startDatesResponse)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified))(_.startDatesResponse)(this)
        }

      }

    }

    "should throw a Bad Request when journey is in stage SubmittedArrangement" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterSubmittedArrangement().copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      val result: Throwable = journeyConnector.updateExtremeDates(tdAll.journeyId, tdAll.EpayeBta.updateExtremeDatesRequest()).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"Cannot update ExtremeDates when journey is in completed state"}""")

      verifyCommonActions(numberOfAuthCalls = 1)
    }
  }
}
