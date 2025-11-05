/*
 * Copyright 2025 HM Revenue & Customs
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
import essttp.rootmodel.bank.{BankDetails, TypeOfBankAccount, TypesOfBankAccount}
import paymentsEmailVerification.models.EmailVerificationResult
import testsupport.ItSpec
import testsupport.testdata.TdAll

class UpdateTypeOfBankAccountControllerSpec extends ItSpec, UpdateJourneyControllerSpec {

  "POST /journey/:journeyId/update-type-of-bank-account" - {
    "should throw Bad Request when Journey is in a stage [BeforeEnteredCanYouSetUpDirectDebit]" in new JourneyItTest {
      stubCommonActions()

      journeyConnector.Epaye.startJourneyBta(TdAll.EpayeBta.sjRequest).futureValue

      val result: Throwable = journeyConnector
        .updateTypeOfBankAccount(tdAll.journeyId, TypesOfBankAccount.Personal)
        .failed
        .futureValue
      result.getMessage should include(
        """{"statusCode":400,"message":"UpdateTypeOfBankAccount is not possible in that state: [Started]"}"""
      )

      verifyCommonActions(numberOfAuthCalls = 2)
    }

    "should update the journey when an existing value didn't exist before for" - {

      "Epaye" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.EpayeBta.journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true),
          TypesOfBankAccount.Personal
        )(
          journeyConnector.updateTypeOfBankAccount,
          tdAll.EpayeBta.journeyAfterChosenTypeOfBankAccount(TypesOfBankAccount.Personal)
        )(this)
      }

      "Vat" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.VatBta.journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true),
          TypesOfBankAccount.Personal
        )(
          journeyConnector.updateTypeOfBankAccount,
          tdAll.VatBta.journeyAfterChosenTypeOfBankAccount(TypesOfBankAccount.Personal)
        )(this)
      }

      "Sa" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.SaBta.journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true),
          TypesOfBankAccount.Business
        )(
          journeyConnector.updateTypeOfBankAccount,
          tdAll.SaBta.journeyAfterChosenTypeOfBankAccount(TypesOfBankAccount.Business)
        )(this)
      }

      "Simp" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.SimpPta.journeyAfterEnteredCanYouSetUpDirectDebitNoAffordability(isAccountHolder = true),
          TypesOfBankAccount.Business
        )(
          journeyConnector.updateTypeOfBankAccount,
          tdAll.SimpPta.journeyAfterChosenTypeOfBankAccount(TypesOfBankAccount.Business)
        )(this)
      }
    }

    "should update the journey when a value already existed" - {

      "Epaye when the current stage is" - {

        def testEpayeBta[J <: Journey](
          initialJourney: J
        )(existingValue: J => TypeOfBankAccount)(context: JourneyItTest): Unit = {
          val differentValue = existingValue(initialJourney) match {
            case TypesOfBankAccount.Personal => TypesOfBankAccount.Business
            case TypesOfBankAccount.Business => TypesOfBankAccount.Personal
          }

          testUpdateWithExistingValue(initialJourney)(
            _.journeyId,
            existingValue(initialJourney)
          )(
            differentValue,
            journeyConnector.updateTypeOfBankAccount(_, _)(using context.request),
            context.tdAll.EpayeBta.journeyAfterChosenTypeOfBankAccount(differentValue)
          )(context)
        }

        "ChosenTypeOfBankAccount" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterChosenTypeOfBankAccount(TypesOfBankAccount.Business))(
            _.typeOfBankAccount
          )(
            this
          )
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(_.typeOfBankAccount)(
            this
          )
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(_.typeOfBankAccount)(
            this
          )
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testEpayeBta(
            tdAll.EpayeBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true)
          )(_.typeOfBankAccount)(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterSelectedEmailNoAffordability)(_.typeOfBankAccount)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testEpayeBta(
            tdAll.EpayeBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified)
          )(_.typeOfBankAccount)(this)
        }

      }

      "Vat when the current stage is" - {

        def testVatBta[J <: Journey](
          initialJourney: J
        )(existingValue: J => TypeOfBankAccount)(context: JourneyItTest): Unit = {
          val differentValue = existingValue(initialJourney) match {
            case TypesOfBankAccount.Personal => TypesOfBankAccount.Business
            case TypesOfBankAccount.Business => TypesOfBankAccount.Personal
          }

          testUpdateWithExistingValue(initialJourney)(
            _.journeyId,
            existingValue(initialJourney)
          )(
            differentValue,
            journeyConnector.updateTypeOfBankAccount(_, _)(using context.request),
            context.tdAll.VatBta.journeyAfterChosenTypeOfBankAccount(differentValue)
          )(context)
        }

        "ChosenTypeOfBankAccount" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterChosenTypeOfBankAccount(TypesOfBankAccount.Business))(
            _.typeOfBankAccount
          )(
            this
          )
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(_.typeOfBankAccount)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(_.typeOfBankAccount)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(
            _.typeOfBankAccount
          )(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterSelectedEmailNoAffordability)(_.typeOfBankAccount)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(
            _.typeOfBankAccount
          )(this)
        }

      }

      "Sa when the current stage is" - {

        def testSaBta[J <: Journey](
          initialJourney: J
        )(existingValue: J => TypeOfBankAccount)(context: JourneyItTest): Unit = {
          val differentValue = existingValue(initialJourney) match {
            case TypesOfBankAccount.Personal => TypesOfBankAccount.Business
            case TypesOfBankAccount.Business => TypesOfBankAccount.Personal
          }

          testUpdateWithExistingValue(initialJourney)(
            _.journeyId,
            existingValue(initialJourney)
          )(
            differentValue,
            journeyConnector.updateTypeOfBankAccount(_, _)(using context.request),
            context.tdAll.SaBta.journeyAfterChosenTypeOfBankAccount(differentValue)
          )(context)
        }

        "ChosenTypeOfBankAccount" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterChosenTypeOfBankAccount(TypesOfBankAccount.Personal))(_.typeOfBankAccount)(
            this
          )
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(_.typeOfBankAccount)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(_.typeOfBankAccount)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(
            _.typeOfBankAccount
          )(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterSelectedEmailNoAffordability)(_.typeOfBankAccount)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(
            _.typeOfBankAccount
          )(this)
        }

      }

      "Simp when the current stage is" - {

        def testSimpPta[J <: Journey](
          initialJourney: J
        )(existingValue: J => TypeOfBankAccount)(context: JourneyItTest): Unit = {
          val differentValue = existingValue(initialJourney) match {
            case TypesOfBankAccount.Personal => TypesOfBankAccount.Business
            case TypesOfBankAccount.Business => TypesOfBankAccount.Personal
          }

          testUpdateWithExistingValue(initialJourney)(
            _.journeyId,
            existingValue(initialJourney)
          )(
            differentValue,
            journeyConnector.updateTypeOfBankAccount(_, _)(using context.request),
            context.tdAll.SimpPta.journeyAfterChosenTypeOfBankAccount(differentValue)
          )(context)
        }

        "ChosenTypeOfBankAccount" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterChosenTypeOfBankAccount(TypesOfBankAccount.Business))(
            _.typeOfBankAccount
          )(
            this
          )
        }

        "EnteredDirectDebitDetails" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterEnteredDirectDebitDetailsNoAffordability())(_.typeOfBankAccount)(this)
        }

        "ConfirmedDirectDebitDetails" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterConfirmedDirectDebitDetailsNoAffordability)(_.typeOfBankAccount)(this)
        }

        "AgreedTermsAndConditions" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterAgreedTermsAndConditionsNoAffordability(isEmailAddressRequired = true))(
            _.typeOfBankAccount
          )(this)
        }

        "SelectedEmailToBeVerified" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterSelectedEmail)(_.typeOfBankAccount)(this)
        }

        "EmailVerificationComplete" in new JourneyItTest {
          testSimpPta(tdAll.SimpPta.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified))(
            _.typeOfBankAccount
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
        .updateTypeOfBankAccount(tdAll.journeyId, TypesOfBankAccount.Personal)
        .failed
        .futureValue
      result.getMessage should include(
        """{"statusCode":400,"message":"Cannot update TypeOfBankAccount when journey is in completed state"}"""
      )

      verifyCommonActions(numberOfAuthCalls = 1)
    }
  }
}
