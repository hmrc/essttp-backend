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

    "should update the journey when an existing value didn't exist before for" - {

      "Epaye" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.EpayeBta.journeyAfterAgreedTermsAndConditions(true),
          tdAll.EpayeBta.updateSelectedEmailRequest()
        )(
            journeyConnector.updateSelectedEmailToBeVerified,
            tdAll.EpayeBta.journeyAfterSelectedEmail.copy(emailToBeVerified = tdAll.EpayeBta.updateSelectedEmailRequest())
          )(this)
      }

      "Vat" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.VatBta.journeyAfterAgreedTermsAndConditions(true),
          tdAll.VatBta.updateSelectedEmailRequest()
        )(
            journeyConnector.updateSelectedEmailToBeVerified,
            tdAll.VatBta.journeyAfterSelectedEmail.copy(emailToBeVerified = tdAll.VatBta.updateSelectedEmailRequest())
          )(this)
      }

      "Sa" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.SaBta.journeyAfterAgreedTermsAndConditions(true),
          tdAll.SaBta.updateSelectedEmailRequest()
        )(
            journeyConnector.updateSelectedEmailToBeVerified,
            tdAll.SaBta.journeyAfterSelectedEmail.copy(emailToBeVerified = tdAll.SaBta.updateSelectedEmailRequest())
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
              context.tdAll.EpayeBta.journeyAfterSelectedEmail.copy(emailToBeVerified = value(initialJourney))
            )(context)

        "the value is the same and" - {

          "the current stage is" - {

            "SelectedEmailToBeVerified" in new JourneyItTest {
              testEpayeBta(tdAll.EpayeBta.journeyAfterSelectedEmail)(_.emailToBeVerified)(this)
            }

            "EmailVerificationComplete" in new JourneyItTest {
              testEpayeBta(tdAll.EpayeBta.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified))(_.emailToBeVerified)(this)
            }

          }
        }

        "the value is the different and" - {

          "the current stage is" - {

            "SelectedEmailToBeVerified" in new JourneyItTest {
              testEpayeBta(tdAll.EpayeBta.journeyAfterSelectedEmail)(_ => differentEmail)(this)
            }

            "EmailVerificationComplete" in new JourneyItTest {
              testEpayeBta(tdAll.EpayeBta.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified))(_ => differentEmail)(this)
            }

          }
        }

      }

      "Vat when" - {

          def testVatBta[J <: Journey](initialJourney: J)(value: J => Email)(context: JourneyItTest): Unit =
            testUpdate(initialJourney, value(initialJourney))(
              _.journeyId,
              journeyConnector.updateSelectedEmailToBeVerified(_, _)(context.request),
              context.tdAll.VatBta.journeyAfterSelectedEmail.copy(emailToBeVerified = value(initialJourney))
            )(context)

        "the value is the same and" - {

          "the current stage is" - {

            "SelectedEmailToBeVerified" in new JourneyItTest {
              testVatBta(tdAll.VatBta.journeyAfterSelectedEmail)(_.emailToBeVerified)(this)
            }

            "EmailVerificationComplete" in new JourneyItTest {
              testVatBta(tdAll.VatBta.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified))(_.emailToBeVerified)(this)
            }

          }
        }

        "the value is the different and" - {

          "the current stage is" - {

            "SelectedEmailToBeVerified" in new JourneyItTest {
              testVatBta(tdAll.VatBta.journeyAfterSelectedEmail)(_ => differentEmail)(this)
            }

            "EmailVerificationComplete" in new JourneyItTest {
              testVatBta(tdAll.VatBta.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified))(_ => differentEmail)(this)
            }

          }
        }

      }

      "Sa when" - {

          def testSaBta[J <: Journey](initialJourney: J)(value: J => Email)(context: JourneyItTest): Unit =
            testUpdate(initialJourney, value(initialJourney))(
              _.journeyId,
              journeyConnector.updateSelectedEmailToBeVerified(_, _)(context.request),
              context.tdAll.SaBta.journeyAfterSelectedEmail.copy(emailToBeVerified = value(initialJourney))
            )(context)

        "the value is the same and" - {

          "the current stage is" - {

            "SelectedEmailToBeVerified" in new JourneyItTest {
              testSaBta(tdAll.SaBta.journeyAfterSelectedEmail)(_.emailToBeVerified)(this)
            }

            "EmailVerificationComplete" in new JourneyItTest {
              testSaBta(tdAll.SaBta.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified))(_.emailToBeVerified)(this)
            }

          }
        }

        "the value is the different and" - {

          "the current stage is" - {

            "SelectedEmailToBeVerified" in new JourneyItTest {
              testSaBta(tdAll.SaBta.journeyAfterSelectedEmail)(_ => differentEmail)(this)
            }

            "EmailVerificationComplete" in new JourneyItTest {
              testSaBta(tdAll.SaBta.journeyAfterEmailVerificationResult(EmailVerificationResult.Verified))(_ => differentEmail)(this)
            }

          }
        }

      }

    }

    "change the journey state even if the email in the journey is the same" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(
        TdAll.EpayeBta.journeyAfterEmailVerificationResult(EmailVerificationResult.Locked)
          .copy(_id = tdAll.journeyId)
          .copy(correlationId = tdAll.correlationId)
      )

      val result: Journey = journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.EpayeBta.updateSelectedEmailRequest()).futureValue
      result shouldBe tdAll.EpayeBta.journeyAfterSelectedEmail
      journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterSelectedEmail

      verifyCommonActions(numberOfAuthCalls = 2)
    }

    "should throw a Bad Request when isEmailAddressRequired in journey is false" in new JourneyItTest {
      stubCommonActions()
      insertJourneyForTest(TdAll.EpayeBta.journeyAfterAgreedTermsAndConditions(false)
        .copy(_id = tdAll.journeyId)
        .copy(correlationId = tdAll.correlationId))
      val result: Throwable = journeyConnector.updateSelectedEmailToBeVerified(tdAll.journeyId, tdAll.EpayeBta.updateSelectedEmailRequest()).failed.futureValue
      result.getMessage should include("""{"statusCode":400,"message":"Cannot update selected email address when isEmailAddressRequired is false for journey."}""")
      verifyCommonActions(numberOfAuthCalls = 1)
    }

    "should throw a Bad Request when journey is in stage SubmittedArrangement" in new JourneyItTest {
      stubCommonActions()
      insertJourneyForTest(
        TdAll.EpayeBta.journeyAfterSubmittedArrangement()
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
