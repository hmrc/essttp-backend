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

import essttp.journey.model.{Journey, JourneyId}
import essttp.rootmodel.{Email, IsEmailAddressRequired}
import paymentsEmailVerification.models.EmailVerificationResult
import testsupport.ItSpec
import testsupport.testdata.TdAll
import uk.gov.hmrc.crypto.Sensitive.SensitiveString

import scala.concurrent.Future

class UpdateChosenEmailControllerSpec extends ItSpec with UpdateJourneyControllerSpec {

  "POST /journey/:journeyId/update-chosen-email" - {
    "should throw Bad Request when Journey is in a stage [BeforeAgreedTermsAndConditions]" in new JourneyItTest {
      stubCommonActions()

      journeyConnector.Epaye.startJourneyBta(TdAll.EpayeBta.sjRequest).futureValue
      val result: Throwable = journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.EpayeBta.updateSelectedEmailRequest()).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"UpdateChosenEmail is not possible in that state: [Started]"}""")

      verifyCommonActions(numberOfAuthCalls = 2)
    }

    "should update the journey when an existing isAccountHolder didn't exist before for" - {

      "Epaye" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.EpayeBta.journeyAfterAgreedTermsAndConditionsNoAffordability(true),
          tdAll.EpayeBta.updateSelectedEmailRequest()
        )(
            journeyConnector.updateSelectedEmailToBeVerified,
            tdAll.EpayeBta.journeyAfterSelectedEmailNoAffordability.copy(emailToBeVerified = tdAll.EpayeBta.updateSelectedEmailRequest())
          )(this)
      }

      "Vat" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.VatBta.journeyAfterAgreedTermsAndConditionsNoAffordability(true),
          tdAll.VatBta.updateSelectedEmailRequest()
        )(
            journeyConnector.updateSelectedEmailToBeVerified,
            tdAll.VatBta.journeyAfterSelectedEmailNoAffordability.copy(emailToBeVerified = tdAll.VatBta.updateSelectedEmailRequest())
          )(this)
      }

      "Sa" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.SaBta.journeyAfterAgreedTermsAndConditionsNoAffordability(true),
          tdAll.SaBta.updateSelectedEmailRequest()
        )(
            journeyConnector.updateSelectedEmailToBeVerified,
            tdAll.SaBta.journeyAfterSelectedEmailNoAffordability.copy(emailToBeVerified = tdAll.SaBta.updateSelectedEmailRequest())
          )(this)
      }

      "Sia" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.SiaPta.journeyAfterAgreedTermsAndConditionsNoAffordability(true),
          tdAll.SiaPta.updateSelectedEmailRequest()
        )(
            journeyConnector.updateSelectedEmailToBeVerified,
            tdAll.SiaPta.journeyAfterSelectedEmail.copy(emailToBeVerified = tdAll.SaBta.updateSelectedEmailRequest())
          )(this)
      }
    }

    "should update the journey when an email already existed" - {

      val differentEmail = Email(SensitiveString("different.email@test.com"))

        def testUpdate[J <: Journey, R](initialJourney: J, value: R)(
            existingJourneyId:  J => JourneyId,
            doUpdate:           (JourneyId, R) => Future[Journey],
            expectedNewJourney: Journey
        )(context: JourneyItTest): Unit = {
          import context.request

          val journeyId = existingJourneyId(initialJourney)

          stubCommonActions()

          context.insertJourneyForTest(initialJourney)

          val result = doUpdate(journeyId, value).futureValue
          result shouldBe expectedNewJourney
          journeyConnector.getJourney(journeyId).futureValue shouldBe expectedNewJourney

          verifyCommonActions(numberOfAuthCalls = 2)
          ()
        }

      "Epaye when" - {

          def testEpayeBta[J <: Journey](initialJourney: J)(value: J => Email)(context: JourneyItTest): Unit =
            testUpdate(initialJourney, value(initialJourney))(
              _.journeyId,
              journeyConnector.updateSelectedEmailToBeVerified(_, _)(context.request),
              context.tdAll.EpayeBta.journeyAfterSelectedEmailNoAffordability.copy(emailToBeVerified = value(initialJourney))
            )(context)

        "the isAccountHolder is the same and" - {

          "the current stage is" - {

            "SelectedEmailToBeVerified" in new JourneyItTest {
              testEpayeBta(tdAll.EpayeBta.journeyAfterSelectedEmailNoAffordability)(_.emailToBeVerified)(this)
            }

            "EmailVerificationComplete" in new JourneyItTest {
              testEpayeBta(tdAll.EpayeBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(_.emailToBeVerified)(this)
            }

          }
        }

        "the isAccountHolder is the different and" - {

          "the current stage is" - {

            "SelectedEmailToBeVerified" in new JourneyItTest {
              testEpayeBta(tdAll.EpayeBta.journeyAfterSelectedEmailNoAffordability)(_ => differentEmail)(this)
            }

            "EmailVerificationComplete" in new JourneyItTest {
              testEpayeBta(tdAll.EpayeBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(_ => differentEmail)(this)
            }

          }
        }

      }

      "Vat when" - {

          def testVatBta[J <: Journey](initialJourney: J)(value: J => Email)(context: JourneyItTest): Unit =
            testUpdate(initialJourney, value(initialJourney))(
              _.journeyId,
              journeyConnector.updateSelectedEmailToBeVerified(_, _)(context.request),
              context.tdAll.VatBta.journeyAfterSelectedEmailNoAffordability.copy(emailToBeVerified = value(initialJourney))
            )(context)

        "the isAccountHolder is the same and" - {

          "the current stage is" - {

            "SelectedEmailToBeVerified" in new JourneyItTest {
              testVatBta(tdAll.VatBta.journeyAfterSelectedEmailNoAffordability)(_.emailToBeVerified)(this)
            }

            "EmailVerificationComplete" in new JourneyItTest {
              testVatBta(tdAll.VatBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(_.emailToBeVerified)(this)
            }

          }
        }

        "the isAccountHolder is the different and" - {

          "the current stage is" - {

            "SelectedEmailToBeVerified" in new JourneyItTest {
              testVatBta(tdAll.VatBta.journeyAfterSelectedEmailNoAffordability)(_ => differentEmail)(this)
            }

            "EmailVerificationComplete" in new JourneyItTest {
              testVatBta(tdAll.VatBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(_ => differentEmail)(this)
            }

          }
        }

      }

      "Sa when" - {

          def testSaBta[J <: Journey](initialJourney: J)(value: J => Email)(context: JourneyItTest): Unit =
            testUpdate(initialJourney, value(initialJourney))(
              _.journeyId,
              journeyConnector.updateSelectedEmailToBeVerified(_, _)(context.request),
              context.tdAll.SaBta.journeyAfterSelectedEmailNoAffordability.copy(emailToBeVerified = value(initialJourney))
            )(context)

        "the isAccountHolder is the same and" - {

          "the current stage is" - {

            "SelectedEmailToBeVerified" in new JourneyItTest {
              testSaBta(tdAll.SaBta.journeyAfterSelectedEmailNoAffordability)(_.emailToBeVerified)(this)
            }

            "EmailVerificationComplete" in new JourneyItTest {
              testSaBta(tdAll.SaBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(_.emailToBeVerified)(this)
            }

          }
        }

        "the isAccountHolder is the different and" - {

          "the current stage is" - {

            "SelectedEmailToBeVerified" in new JourneyItTest {
              testSaBta(tdAll.SaBta.journeyAfterSelectedEmailNoAffordability)(_ => differentEmail)(this)
            }

            "EmailVerificationComplete" in new JourneyItTest {
              testSaBta(tdAll.SaBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Verified))(_ => differentEmail)(this)
            }

          }
        }

      }

      "Sia when" - {

          def testSiaPta[J <: Journey](initialJourney: J)(value: J => Email)(context: JourneyItTest): Unit =
            testUpdate(initialJourney, value(initialJourney))(
              _.journeyId,
              journeyConnector.updateSelectedEmailToBeVerified(_, _)(context.request),
              context.tdAll.SiaPta.journeyAfterSelectedEmail.copy(emailToBeVerified = value(initialJourney))
            )(context)

        "the isAccountHolder is the same and" - {

          "the current stage is" - {

            "SelectedEmailToBeVerified" in new JourneyItTest {
              testSiaPta(tdAll.SiaPta.journeyAfterSelectedEmail)(_.emailToBeVerified)(this)
            }

            "EmailVerificationComplete" in new JourneyItTest {
              testSiaPta(tdAll.SiaPta.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified))(_.emailToBeVerified)(this)
            }

          }
        }

        "the isAccountHolder is the different and" - {

          "the current stage is" - {

            "SelectedEmailToBeVerified" in new JourneyItTest {
              testSiaPta(tdAll.SiaPta.journeyAfterSelectedEmail)(_ => differentEmail)(this)
            }

            "EmailVerificationComplete" in new JourneyItTest {
              testSiaPta(tdAll.SiaPta.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified))(_ => differentEmail)(this)
            }

          }
        }

      }

    }

    "change the journey state even if the email in the journey is the same" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(
        TdAll.EpayeBta.journeyAfterEmailVerificationResultNoAffordability(EmailVerificationResult.Locked)
          .copy(_id = tdAll.journeyId)
          .copy(correlationId = tdAll.correlationId)
      )

      val result: Journey = journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.EpayeBta.updateSelectedEmailRequest()).futureValue
      result shouldBe tdAll.EpayeBta.journeyAfterSelectedEmailNoAffordability
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterSelectedEmailNoAffordability

      verifyCommonActions(numberOfAuthCalls = 2)
    }

    "should throw a Bad Request when isEmailAddressRequired in journey is false" in new JourneyItTest {
      stubCommonActions()
      insertJourneyForTest(TdAll.EpayeBta.journeyAfterAgreedTermsAndConditionsNoAffordability(false)
        .copy(_id = tdAll.journeyId)
        .copy(correlationId = tdAll.correlationId))
      val result: Throwable = journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.EpayeBta.updateSelectedEmailRequest()).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"Cannot update selected email address when isEmailAddressRequired is false for journey."}""")
      verifyCommonActions(numberOfAuthCalls = 1)
    }

    "should throw a Bad Request when journey is in stage SubmittedArrangement" in new JourneyItTest {
      stubCommonActions()
      insertJourneyForTest(
        TdAll.EpayeBta.journeyAfterSubmittedArrangementNoAffordability()
          .copy(_id = tdAll.journeyId)
          .copy(correlationId = tdAll.correlationId)
          .copy(isEmailAddressRequired = IsEmailAddressRequired(value = true))
      )
      val result: Throwable = journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.EpayeBta.updateSelectedEmailRequest()).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"Cannot update ChosenEmail when journey is in completed state."}""")
      verifyCommonActions(numberOfAuthCalls = 1)
    }
  }

}
