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
import essttp.rootmodel.CanPayUpfront
import paymentsEmailVerification.models.EmailVerificationResult
import testsupport.ItSpec
import testsupport.testdata.TdAll

class UpdateCanPayUpfrontControllerSpec extends ItSpec with UpdateJourneyControllerSpec {

  import UpdateCanPayUpfrontControllerSpec.UpfrontPaymentAnswersOps

  "POST /journey/:journeyId/update-can-pay-upfront" - {
    "should throw Bad Request when Journey is in a stage [BeforeObtainedWhyCannotPayInFull]" in new JourneyItTest {
      stubCommonActions()

      journeyConnector.Epaye.startJourneyBta(TdAll.EpayeBta.sjRequest).futureValue
      journeyConnector.updateTaxId(tdAll.journeyId, tdAll.EpayeBta.updateTaxIdRequest()).futureValue
      journeyConnector.updateEligibilityCheckResult(tdAll.journeyId, tdAll.EpayeBta.updateEligibilityCheckRequest()).futureValue

      val result: Throwable = journeyConnector.updateCanPayUpfront(tdAll.journeyId, tdAll.EpayeBta.updateCanPayUpfrontYesRequest()).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"UpdateCanPayUpfront is not possible in that state."}""")

      verifyCommonActions(numberOfAuthCalls = 4)
    }

    "should update the journey when an existing value didn't exist before for" - {

      "Epaye" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.EpayeBta.journeyAfterWhyCannotPayInFullNotRequired,
          TdAll.EpayeBta.updateCanPayUpfrontYesRequest()
        )(
            journeyConnector.updateCanPayUpfront,
            tdAll.EpayeBta.journeyAfterCanPayUpfrontYes
          )(this)
      }

      "Vat" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.VatBta.journeyAfterWhyCannotPayInFullNotRequired,
          TdAll.VatBta.updateCanPayUpfrontNoRequest()
        )(
            journeyConnector.updateCanPayUpfront,
            tdAll.VatBta.journeyAfterCanPayUpfrontNo
          )(this)
      }

      "Sa" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.SaBta.journeyAfterWhyCannotPayInFullNotRequired,
          TdAll.SaBta.updateCanPayUpfrontYesRequest()
        )(
            journeyConnector.updateCanPayUpfront,
            tdAll.SaBta.journeyAfterCanPayUpfrontYes
          )(this)
      }

      "Sia" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.SiaPta.journeyAfterWhyCannotPayInFullNotRequired,
          TdAll.SiaPta.updateCanPayUpfrontYesRequest()
        )(
            journeyConnector.updateCanPayUpfront,
            tdAll.SiaPta.journeyAfterCanPayUpfrontYes
          )(this)
      }
    }

    "should update the journey when a value already existed" - {

      "Epaye when the current stage is" - {

          def testEpayeBta[J <: Journey](initialJourney: J)(existingValue: J => CanPayUpfront)(context: JourneyItTest): Unit = {
            val differentValue = CanPayUpfront(!existingValue(initialJourney).value)

            val expectedUpdatedJourney =
              if (differentValue.value) context.tdAll.EpayeBta.journeyAfterCanPayUpfrontYes
              else context.tdAll.EpayeBta.journeyAfterCanPayUpfrontNo

            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentValue,
                journeyConnector.updateCanPayUpfront(_, _)(context.request),
                expectedUpdatedJourney
              )(context)
          }

        "AnsweredCanPayUpfront" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterCanPayUpfrontNo)(_.canPayUpfront)(this)
        }

        "EnteredUpfrontPaymentAmount" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterUpfrontPaymentAmount)(_.canPayUpfront)(this)
        }

        "RetrievedExtremeDates" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterExtremeDates)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "RetrievedAffordabilityResult" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterInstalmentAmounts)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "ObtainedCanPayWithinSixMonthsAnswers" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterCanPayWithinSixMonthsNotRequired)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "StartedPegaJourney" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterStartedPegaCase)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "EnteredMonthlyPaymentAmount" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterMonthlyPaymentAmount)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "EnteredDayOfMonth" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterDayOfMonth)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "RetrievedStartDates" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterStartDatesResponse)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "RetrievedAffordableQuotes" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterAffordableQuotesResponse)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "ChosenPaymentPlan" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterSelectedPaymentPlan)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterCheckedPaymentPlanNonAffordability)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "EnteredDetailsAboutBankAccount" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEnteredDetailsAboutBankAccountNoAffordability(isAccountHolder = true))(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterSelectedEmailNoAffordability)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

      }

      "Vat when the current stage is" - {

          def testVatBta[J <: Journey](initialJourney: J)(existingValue: J => CanPayUpfront)(context: JourneyItTest): Unit = {
            val differentValue = CanPayUpfront(!existingValue(initialJourney).value)

            val expectedUpdatedJourney =
              if (differentValue.value) context.tdAll.VatBta.journeyAfterCanPayUpfrontYes
              else context.tdAll.VatBta.journeyAfterCanPayUpfrontNo

            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentValue,
                journeyConnector.updateCanPayUpfront(_, _)(context.request),
                expectedUpdatedJourney
              )(context)
          }

        "AnsweredCanPayUpfront" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterCanPayUpfrontNo)(_.canPayUpfront)(this)
        }

        "EnteredUpfrontPaymentAmount" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterUpfrontPaymentAmount)(_.canPayUpfront)(this)
        }

        "RetrievedExtremeDates" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterExtremeDates)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "RetrievedAffordabilityResult" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterInstalmentAmounts)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "ObtainedCanPayWithinSixMonthsAnswers" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterCanPayWithinSixMonthsNotRequired)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "StartedPegaJourney" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterStartedPegaCase)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "EnteredMonthlyPaymentAmount" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterMonthlyPaymentAmount)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "EnteredDayOfMonth" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterDayOfMonth)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "RetrievedStartDates" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterStartDatesResponse)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "RetrievedAffordableQuotes" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterAffordableQuotesResponse)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "ChosenPaymentPlan" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterSelectedPaymentPlan)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterCheckedPaymentPlanNonAffordability)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "EnteredDetailsAboutBankAccount" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEnteredDetailsAboutBankAccountNoAffordability(isAccountHolder = true))(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterSelectedEmailNoAffordability)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

      }

      "Sa when the current stage is" - {

          def testSaBta[J <: Journey](initialJourney: J)(existingValue: J => CanPayUpfront)(context: JourneyItTest): Unit = {
            val differentValue = CanPayUpfront(!existingValue(initialJourney).value)

            val expectedUpdatedJourney =
              if (differentValue.value) context.tdAll.SaBta.journeyAfterCanPayUpfrontYes
              else context.tdAll.SaBta.journeyAfterCanPayUpfrontNo

            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentValue,
                journeyConnector.updateCanPayUpfront(_, _)(context.request),
                expectedUpdatedJourney
              )(context)
          }

        "AnsweredCanPayUpfront" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterCanPayUpfrontNo)(_.canPayUpfront)(this)
        }

        "EnteredUpfrontPaymentAmount" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterUpfrontPaymentAmount)(_.canPayUpfront)(this)
        }

        "RetrievedExtremeDates" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterExtremeDates)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "RetrievedAffordabilityResult" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterInstalmentAmounts)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "ObtainedCanPayWithinSixMonthsAnswers" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterCanPayWithinSixMonths)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "StartedPegaJourney" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterStartedPegaCase)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "EnteredMonthlyPaymentAmount" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterMonthlyPaymentAmount)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "EnteredDayOfMonth" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterDayOfMonth)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "RetrievedStartDates" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterStartDatesResponse)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "RetrievedAffordableQuotes" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterAffordableQuotesResponse)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "ChosenPaymentPlan" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterSelectedPaymentPlan)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterCheckedPaymentPlanNonAffordability)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "EnteredDetailsAboutBankAccount" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEnteredDetailsAboutBankAccountNoAffordability(isAccountHolder = true))(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterSelectedEmailNoAffordability)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

      }

      "Sia when the current stage is" - {

          def testSiaPta[J <: Journey](initialJourney: J)(existingValue: J => CanPayUpfront)(context: JourneyItTest): Unit = {
            val differentValue = CanPayUpfront(!existingValue(initialJourney).value)

            val expectedUpdatedJourney =
              if (differentValue.value) context.tdAll.SiaPta.journeyAfterCanPayUpfrontYes
              else context.tdAll.SiaPta.journeyAfterCanPayUpfrontNo

            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentValue,
                journeyConnector.updateCanPayUpfront(_, _)(context.request),
                expectedUpdatedJourney
              )(context)
          }

        "AnsweredCanPayUpfront" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterCanPayUpfrontNo)(_.canPayUpfront)(this)
        }

        "EnteredUpfrontPaymentAmount" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterUpfrontPaymentAmount)(_.canPayUpfront)(this)
        }

        "RetrievedExtremeDates" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterExtremeDates)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "RetrievedAffordabilityResult" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterInstalmentAmounts)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "ObtainedCanPayWithinSixMonthsAnswers" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterCanPayWithinSixMonths)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "StartedPegaJourney" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterStartedPegaCase)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "EnteredMonthlyPaymentAmount" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterMonthlyPaymentAmount)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "EnteredDayOfMonth" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterDayOfMonth)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "RetrievedStartDates" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterStartDatesResponse)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "RetrievedAffordableQuotes" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterAffordableQuotesResponse)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "ChosenPaymentPlan" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterSelectedPaymentPlan)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "CheckedPaymentPlan" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterCheckedPaymentPlanNonAffordability)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "EnteredDetailsAboutBankAccount" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterEnteredDetailsAboutBankAccountNoAffordability(isAccountHolder = true))(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterSelectedEmail)(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified))(_.upfrontPaymentAnswers.asCanPayUpfront)(this)
        }

      }

    }

    "should throw a Bad Request when journey is in stage SubmittedArrangement" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterSubmittedArrangementNoAffordability().copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      val result: Throwable = journeyConnector.updateCanPayUpfront(tdAll.journeyId, tdAll.EpayeBta.updateCanPayUpfrontYesRequest()).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"Cannot update AnsweredCanPayUpFront when journey is in completed state"}""")

      verifyCommonActions(numberOfAuthCalls = 1)
    }
  }

}

object UpdateCanPayUpfrontControllerSpec {

  implicit class UpfrontPaymentAnswersOps(val u: UpfrontPaymentAnswers) extends AnyVal {
    def asCanPayUpfront: CanPayUpfront = CanPayUpfront(u match {
      case UpfrontPaymentAnswers.NoUpfrontPayment          => false
      case _: UpfrontPaymentAnswers.DeclaredUpfrontPayment => true
    })
  }

}
