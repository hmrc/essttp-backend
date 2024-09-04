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
import essttp.rootmodel.{AmountInPence, UpfrontPaymentAmount}
import paymentsEmailVerification.models.EmailVerificationResult
import testsupport.ItSpec
import testsupport.testdata.TdAll

class UpdateUpfrontPaymentAmountControllerSpec extends ItSpec with UpdateJourneyControllerSpec {

  import UpdateUpfrontPaymentAmountControllerSpec.UpfrontPaymentAnswersOps

  "POST /journey/:journeyId/update-upfront-payment-amount" - {
    "should throw Bad Request when Journey is in a stage [BeforeAnsweredCanPayUpfront]" in new JourneyItTest {
      stubCommonActions()

      journeyConnector.Epaye.startJourneyBta(TdAll.EpayeBta.sjRequest).futureValue
      val result: Throwable = journeyConnector.updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.EpayeBta.updateUpfrontPaymentAmountRequest()).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"UpdateUpfrontPaymentAmount update is not possible in that state: [Started]"}""")

      verifyCommonActions(numberOfAuthCalls = 2)
    }

    "should update the journey when an existing value didn't exist before for" - {

      "Epaye" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.EpayeBta.journeyAfterCanPayUpfrontYes,
          TdAll.EpayeBta.updateUpfrontPaymentAmountRequest()
        )(
            journeyConnector.updateUpfrontPaymentAmount,
            tdAll.EpayeBta.journeyAfterUpfrontPaymentAmount
          )(this)
      }

      "Vat" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.VatBta.journeyAfterCanPayUpfrontYes,
          TdAll.VatBta.updateUpfrontPaymentAmountRequest()
        )(
            journeyConnector.updateUpfrontPaymentAmount,
            tdAll.VatBta.journeyAfterUpfrontPaymentAmount
          )(this)
      }

      "Sa" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.SaBta.journeyAfterCanPayUpfrontYes,
          TdAll.SaBta.updateUpfrontPaymentAmountRequest()
        )(
            journeyConnector.updateUpfrontPaymentAmount,
            tdAll.SaBta.journeyAfterUpfrontPaymentAmount
          )(this)
      }

      "Sia" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.SiaPta.journeyAfterCanPayUpfrontYes,
          TdAll.SiaPta.updateUpfrontPaymentAmountRequest()
        )(
            journeyConnector.updateUpfrontPaymentAmount,
            tdAll.SiaPta.journeyAfterUpfrontPaymentAmount
          )(this)
      }
    }

    "should update the journey when a value already existed" - {

      "Epaye when the current stage is" - {

        val differentUpfrontPaymentAmount = UpfrontPaymentAmount(AmountInPence(65482))

          def testEpayeBta[J <: Journey](initialJourney: J)(existingValue: J => UpfrontPaymentAmount)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentUpfrontPaymentAmount,
                journeyConnector.updateUpfrontPaymentAmount(_, _)(context.request),
                context.tdAll.EpayeBta.journeyAfterUpfrontPaymentAmount.copy(upfrontPaymentAmount = differentUpfrontPaymentAmount)
              )(context)

        "EnteredUpfrontPaymentAmount" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterUpfrontPaymentAmount)(_.upfrontPaymentAmount)(this)
        }

        "RetrievedExtremeDates" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterExtremeDates)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "RetrievedAffordabilityResult" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterInstalmentAmounts)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "ObtainedCanPayWithinSixMonthsAnswers" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterCanPayWithinSixMonthsNotRequired)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "StartedPegaCase" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterStartedPegaCase)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "EnteredMonthlyPaymentAmount" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterMonthlyPaymentAmount)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "EnteredDayOfMonth" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterDayOfMonth)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "RetrievedStartDates" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterStartDatesResponse)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "RetrievedAffordableQuotes" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterAffordableQuotesResponse)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "ChosenPaymentPlan" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterSelectedPaymentPlan)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterCheckedPaymentPlanNonAffordability)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "EnteredDetailsAboutBankAccount" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEnteredDetailsAboutBankAccountNoAffordability(isAccountHolder = true))(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterSelectedEmailNoAffordability)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

      }

      "Vat when the current stage is" - {

        val differentUpfrontPaymentAmount = UpfrontPaymentAmount(AmountInPence(65482))

          def testVatBta[J <: Journey](initialJourney: J)(existingValue: J => UpfrontPaymentAmount)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentUpfrontPaymentAmount,
                journeyConnector.updateUpfrontPaymentAmount(_, _)(context.request),
                context.tdAll.VatBta.journeyAfterUpfrontPaymentAmount.copy(upfrontPaymentAmount = differentUpfrontPaymentAmount)
              )(context)

        "EnteredUpfrontPaymentAmount" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterUpfrontPaymentAmount)(_.upfrontPaymentAmount)(this)
        }

        "RetrievedExtremeDates" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterExtremeDates)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "RetrievedAffordabilityResult" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterInstalmentAmounts)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "ObtainedCanPayWithinSixMonthsAnswers" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterCanPayWithinSixMonthsNotRequired)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "StartedPegaCase" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterStartedPegaCase)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "EnteredMonthlyPaymentAmount" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterMonthlyPaymentAmount)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "EnteredDayOfMonth" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterDayOfMonth)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "RetrievedStartDates" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterStartDatesResponse)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "RetrievedAffordableQuotes" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterAffordableQuotesResponse)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "ChosenPaymentPlan" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterSelectedPaymentPlan)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterCheckedPaymentPlanNonAffordability)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "EnteredDetailsAboutBankAccount" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEnteredDetailsAboutBankAccountNoAffordability(isAccountHolder = true))(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterSelectedEmailNoAffordability)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

      }

      "Sa when the current stage is" - {

        val differentUpfrontPaymentAmount = UpfrontPaymentAmount(AmountInPence(65482))

          def testSaBta[J <: Journey](initialJourney: J)(existingValue: J => UpfrontPaymentAmount)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentUpfrontPaymentAmount,
                journeyConnector.updateUpfrontPaymentAmount(_, _)(context.request),
                context.tdAll.SaBta.journeyAfterUpfrontPaymentAmount.copy(upfrontPaymentAmount = differentUpfrontPaymentAmount)
              )(context)

        "EnteredUpfrontPaymentAmount" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterUpfrontPaymentAmount)(_.upfrontPaymentAmount)(this)
        }

        "RetrievedExtremeDates" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterExtremeDates)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "RetrievedAffordabilityResult" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterInstalmentAmounts)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "ObtainedCanPayWithinSixMonthsAnswers" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterCanPayWithinSixMonths)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "StartedPegaCase" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterStartedPegaCase)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "EnteredMonthlyPaymentAmount" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterMonthlyPaymentAmount)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "EnteredDayOfMonth" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterDayOfMonth)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "RetrievedStartDates" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterStartDatesResponse)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "RetrievedAffordableQuotes" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterAffordableQuotesResponse)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "ChosenPaymentPlan" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterSelectedPaymentPlan)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterCheckedPaymentPlanNonAffordability)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "EnteredDetailsAboutBankAccount" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEnteredDetailsAboutBankAccountNoAffordability(isAccountHolder = true))(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterSelectedEmailNoAffordability)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

      }

      "Sia when the current stage is" - {

        val differentUpfrontPaymentAmount = UpfrontPaymentAmount(AmountInPence(65482))

          def testSiaPta[J <: Journey](initialJourney: J)(existingValue: J => UpfrontPaymentAmount)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentUpfrontPaymentAmount,
                journeyConnector.updateUpfrontPaymentAmount(_, _)(context.request),
                context.tdAll.SiaPta.journeyAfterUpfrontPaymentAmount.copy(upfrontPaymentAmount = differentUpfrontPaymentAmount)
              )(context)

        "EnteredUpfrontPaymentAmount" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterUpfrontPaymentAmount)(_.upfrontPaymentAmount)(this)
        }

        "RetrievedExtremeDates" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterExtremeDates)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "RetrievedAffordabilityResult" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterInstalmentAmounts)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "ObtainedCanPayWithinSixMonthsAnswers" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterCanPayWithinSixMonths)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "StartedPegaCase" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterStartedPegaCase)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "EnteredMonthlyPaymentAmount" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterMonthlyPaymentAmount)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "EnteredDayOfMonth" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterDayOfMonth)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "RetrievedStartDates" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterStartDatesResponse)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "RetrievedAffordableQuotes" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterAffordableQuotesResponse)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "ChosenPaymentPlan" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterSelectedPaymentPlan)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterCheckedPaymentPlanNonAffordability)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "EnteredDetailsAboutBankAccount" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterEnteredDetailsAboutBankAccountNoAffordability(isAccountHolder = true))(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterSelectedEmail)(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified))(_.upfrontPaymentAnswers.upfrontPaymentAmount)(this)
        }

      }

    }

    "should return a BadRequest if the user has said they cannot make an upfront payment when the journey is at" - {

      "AnsweredCanPayUpfront" in new JourneyItTest {
        stubCommonActions()

        insertJourneyForTest(tdAll.EpayeBta.journeyAfterCanPayUpfrontNo)

        val result: Throwable =
          journeyConnector.updateUpfrontPaymentAmount(
            tdAll.journeyId,
            tdAll.EpayeBta.updateUpfrontPaymentAmountRequest()
          ).failed.futureValue

        result.getMessage should include(
          """{"statusCode":400,"message":"UpdateUpfrontPaymentAmount update is not possible when user has selected [No] for CanPayUpfront""".stripMargin
        )

        verifyCommonActions(numberOfAuthCalls = 1)
      }

      "AfterUpfrontPaymentAnswers" in new JourneyItTest {
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
    }

    "should throw a Bad Request when journey is in stage SubmittedArrangement" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterSubmittedArrangementNoAffordability().copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      val result: Throwable = journeyConnector.updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.EpayeBta.updateUpfrontPaymentAmountRequest().copy(AmountInPence(12))).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"Cannot update UpfrontPaymentAmount when journey is in completed state"}""")

      verifyCommonActions(numberOfAuthCalls = 1)
    }
  }
}

object UpdateUpfrontPaymentAmountControllerSpec {

  implicit class UpfrontPaymentAnswersOps(val u: UpfrontPaymentAnswers) extends AnyVal {
    def upfrontPaymentAmount: UpfrontPaymentAmount = u match {
      case UpfrontPaymentAnswers.NoUpfrontPayment               => sys.error("No upfront payment amount available")
      case UpfrontPaymentAnswers.DeclaredUpfrontPayment(amount) => amount
    }
  }

}
