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

package controllers

import email.EmailVerificationStatusRepo
import essttp.emailverification.{EmailVerificationResult, EmailVerificationState, EmailVerificationStateResultRequest, EmailVerificationStatus, GetEmailVerificationResultRequest, NumberOfPasscodeJourneysStarted, StartEmailVerificationJourneyRequest, StartEmailVerificationJourneyResponse}
import essttp.rootmodel.{Email, GGCredId}
import essttp.testdata.emailverificationstatus.EmailVerificationStatusTestData
import journey.EmailVerificationController
import models.emailverification.EmailVerificationResultResponse.EmailResult
import models.emailverification.RequestEmailVerificationSuccess
import play.api.mvc.Result
import play.api.test.Helpers._
import testsupport.ItSpec
import testsupport.stubs.EmailVerificationStub
import uk.gov.hmrc.crypto.Sensitive.SensitiveString
import uk.gov.hmrc.http.UpstreamErrorResponse

import java.util.UUID
import scala.concurrent.Future

class EmailVerificationControllerSpec extends ItSpec {

  val controller: EmailVerificationController = app.injector.instanceOf[EmailVerificationController]

  "POST /email-verification/start" - {

    "return a redirect url if a journey is successfully started" in new JourneyItTest {
      val redirectUri: String = "/redirect"

      val startRequest = StartEmailVerificationJourneyRequest(
        GGCredId("credId"),
        "continue",
        "origin",
        "deskpro",
        "accessibility",
        "title",
        "back",
        "enter",
        Email(SensitiveString("email@test.com")),
        "en",
        isLocal = false
      )

      stubCommonActions()
      EmailVerificationStub.requestEmailVerification(Right(RequestEmailVerificationSuccess(redirectUri)))

      val result: Future[Result] = controller.startEmailVerificationJourney(request.withBody(startRequest))
      status(result) shouldBe OK
      contentAsJson(result).as[StartEmailVerificationJourneyResponse] shouldBe StartEmailVerificationJourneyResponse.Success(redirectUri)

      EmailVerificationStub.verifyRequestEmailVerification(startRequest)
    }

    "prefix the uri in the redirecturi with the email-verification-frontend host and port if the redirectUri if the " +
      "environment is local and the uri is absolute" in new JourneyItTest {
        val redirectUri: String = "/redirect"

        val startRequest = StartEmailVerificationJourneyRequest(
          GGCredId("credId"),
          "continue",
          "origin",
          "deskpro",
          "accessibility",
          "title",
          "back",
          "enter",
          Email(SensitiveString("email@test.com")),
          "en",
          isLocal = true
        )

        stubCommonActions()
        EmailVerificationStub.requestEmailVerification(Right(RequestEmailVerificationSuccess(redirectUri)))

        val result: Future[Result] = controller.startEmailVerificationJourney(request.withBody(startRequest))
        status(result) shouldBe OK
        contentAsJson(result).as[StartEmailVerificationJourneyResponse] shouldBe StartEmailVerificationJourneyResponse.Success(s"http://localhost:9890$redirectUri")

        EmailVerificationStub.verifyRequestEmailVerification(startRequest)
      }

    "maintain the redirectUri in the email verification response if the environment is local and the uri is absolute" in new JourneyItTest {
      val redirectUri: String = "http:///host:12345/redirect"

      val startRequest = StartEmailVerificationJourneyRequest(
        GGCredId("credId"),
        "continue",
        "origin",
        "deskpro",
        "accessibility",
        "title",
        "back",
        "enter",
        Email(SensitiveString("email@test.com")),
        "en",
        isLocal = true
      )

      stubCommonActions()
      EmailVerificationStub.requestEmailVerification(Right(RequestEmailVerificationSuccess(redirectUri)))

      val result: Future[Result] = controller.startEmailVerificationJourney(request.withBody(startRequest))
      status(result) shouldBe OK
      contentAsJson(result).as[StartEmailVerificationJourneyResponse] shouldBe StartEmailVerificationJourneyResponse.Success(redirectUri)

      EmailVerificationStub.verifyRequestEmailVerification(startRequest)
    }

    "return a locked response if the email-verification service gives a 401 (UNAUTHORIZED) response" in new JourneyItTest {
      val startRequest = StartEmailVerificationJourneyRequest(
        GGCredId("credId"),
        "continue",
        "origin",
        "deskpro",
        "accessibility",
        "title",
        "back",
        "enter",
        Email(SensitiveString("email@test.com")),
        "en",
        isLocal = false
      )

      stubCommonActions()
      EmailVerificationStub.requestEmailVerification(Left(UNAUTHORIZED))

      val result: Future[Result] = controller.startEmailVerificationJourney(request.withBody(startRequest))
      status(result) shouldBe OK
      contentAsJson(result).as[StartEmailVerificationJourneyResponse] shouldBe StartEmailVerificationJourneyResponse.Locked

      EmailVerificationStub.verifyRequestEmailVerification(startRequest)
    }

  }

  "POST /email-verification/result" - {

    val ggCredId = GGCredId("id")
    val email = Email(SensitiveString("email@test.com"))
    val getResultRequest = GetEmailVerificationResultRequest(ggCredId, email)

    "return a 'Verified' response if the email address has been verified with the GG cred id" in new JourneyItTest {
      stubCommonActions()
      EmailVerificationStub.getVerificationResult(ggCredId, Right(List(EmailResult(email.value.decryptedValue, verified = true, locked = false))))

      val result: Future[Result] = controller.getEmailVerificationResult(request.withBody(getResultRequest))
      status(result) shouldBe OK
      contentAsJson(result).as[EmailVerificationResult] shouldBe EmailVerificationResult.Verified
    }

    "return a 'Locked' response if the email address has been locked with the GG cred id" in new JourneyItTest {
      stubCommonActions()
      EmailVerificationStub.getVerificationResult(ggCredId, Right(List(EmailResult(email.value.decryptedValue, verified = false, locked = true))))

      val result: Future[Result] = controller.getEmailVerificationResult(request.withBody(getResultRequest))
      status(result) shouldBe OK
      contentAsJson(result).as[EmailVerificationResult] shouldBe EmailVerificationResult.Locked
    }

    "should an error when" - {

        def testIsUpstreamErrorResponse(result: Future[Result], expectedStatusCode: Int = INTERNAL_SERVER_ERROR): Unit = {
          result.failed.futureValue match {
            case e: UpstreamErrorResponse => e.statusCode shouldBe expectedStatusCode
            case e                        => fail(s"Expected UpstreamErrorResponse but got ${e.toString}")
          }
          ()
        }

      "a result cannot be found for the given email address" in new JourneyItTest {
        stubCommonActions()
        EmailVerificationStub.getVerificationResult(ggCredId, Right(List()))

        testIsUpstreamErrorResponse(
          controller.getEmailVerificationResult(request.withBody(getResultRequest)),
          NOT_FOUND
        )
      }

      "verified=true and locked=true for the given email address" in new JourneyItTest {
        stubCommonActions()
        EmailVerificationStub.getVerificationResult(ggCredId, Right(List(EmailResult(email.value.decryptedValue, verified = true, locked = true))))

        testIsUpstreamErrorResponse(controller.getEmailVerificationResult(request.withBody(getResultRequest)))
      }

      "verified=false and locked=false for the given email address" in new JourneyItTest {
        stubCommonActions()
        EmailVerificationStub.getVerificationResult(ggCredId, Right(List(EmailResult(email.value.decryptedValue, verified = false, locked = false))))

        testIsUpstreamErrorResponse(controller.getEmailVerificationResult(request.withBody(getResultRequest)))
      }

    }

  }

  "EmailVerificationStatus" - {

      def emailVerificationStatusRepo: EmailVerificationStatusRepo = app.injector.instanceOf[EmailVerificationStatusRepo]
      def insertEmailVerificationStatusForTest(emailVerificationStatuses: EmailVerificationStatus*): Unit =
        emailVerificationStatuses.foreach(emailVerificationStatusRepo.upsert(_).futureValue)

    "POST /email-verification/verification-state" - {

      "return OkToBeVerified if no entries are found" in new JourneyItTest {
        stubCommonActions()
        val ggCredId: GGCredId = GGCredId(s"authId-${UUID.randomUUID().toString}")
        val email: Email = Email(SensitiveString("email@test.com"))
        val getEmailVerificationResultRequest: GetEmailVerificationResultRequest = GetEmailVerificationResultRequest(ggCredId, email)
        val result: Future[Result] = controller.state(request.withBody(getEmailVerificationResultRequest))
        status(result) shouldBe OK
        contentAsJson(result).as[EmailVerificationState] shouldBe EmailVerificationState.OkToBeVerified
      }

      "return OkToBeVerified if less than 10 entries are found, " +
        "with none of those entries having numberOfPasscodeJourneysStarted >= 5, " +
        "or having EmailVerificationResult as Locked or Verified" in new JourneyItTest {
          stubCommonActions()
          val ggCredId: GGCredId = GGCredId(s"authId-${UUID.randomUUID().toString}")
          val email: Email = Email(SensitiveString("email@test.com"))
          val getEmailVerificationResultRequest: GetEmailVerificationResultRequest = GetEmailVerificationResultRequest(ggCredId, email)
          insertEmailVerificationStatusForTest(EmailVerificationStatusTestData.emailVerificationStatus(email, ggCredId.value.drop(7)))
          insertEmailVerificationStatusForTest(EmailVerificationStatusTestData.createXNumberOfStatuses(7): _*)
          val result: Future[Result] = controller.state(request.withBody(getEmailVerificationResultRequest))
          status(result) shouldBe OK
          contentAsJson(result).as[EmailVerificationState] shouldBe EmailVerificationState.OkToBeVerified
        }

      "return AlreadyVerified if an EmailVerificationStatus is found with EmailVerificationResult Verified" in new JourneyItTest {
        stubCommonActions()
        val ggCredId: GGCredId = GGCredId(s"authId-${UUID.randomUUID().toString}")
        val email: Email = Email(SensitiveString("email@test.com"))
        val getEmailVerificationResultRequest: GetEmailVerificationResultRequest = GetEmailVerificationResultRequest(ggCredId, email)
        insertEmailVerificationStatusForTest(
          EmailVerificationStatusTestData.emailVerificationStatus()
            .copy(
              _id                = ggCredId.value.drop(7),
              credId             = ggCredId,
              verificationResult = Some(EmailVerificationResult.Verified)
            )
        )
        val result: Future[Result] = controller.state(request.withBody(getEmailVerificationResultRequest))
        status(result) shouldBe OK
        contentAsJson(result).as[EmailVerificationState] shouldBe EmailVerificationState.AlreadyVerified
      }

      "return TooManyPasscodeAttempts if EmailVerificationStatus is found with an EmailVerificationResult of Locked" in new JourneyItTest {
        stubCommonActions()
        val ggCredId: GGCredId = GGCredId(s"authId-${UUID.randomUUID().toString}")
        val email: Email = Email(SensitiveString("email@test.com"))
        val getEmailVerificationResultRequest: GetEmailVerificationResultRequest = GetEmailVerificationResultRequest(ggCredId, email)
        insertEmailVerificationStatusForTest(
          EmailVerificationStatusTestData.emailVerificationStatus()
            .copy(
              _id                = ggCredId.value.drop(7),
              credId             = ggCredId,
              verificationResult = Some(EmailVerificationResult.Locked)
            )
        )
        val result: Future[Result] = controller.state(request.withBody(getEmailVerificationResultRequest))
        status(result) shouldBe OK
        contentAsJson(result).as[EmailVerificationState] shouldBe EmailVerificationState.TooManyPasscodeAttempts
      }

      "return TooManyPasscodeJourneysStarted if EmailVerificationStatus is found with numberOfPasscodeJourneysStarted >= 5" in new JourneyItTest {
        stubCommonActions()
        val ggCredId: GGCredId = GGCredId(s"authId-${UUID.randomUUID().toString}")
        val email: Email = Email(SensitiveString("email@test.com"))
        val getEmailVerificationResultRequest: GetEmailVerificationResultRequest = GetEmailVerificationResultRequest(ggCredId, email)
        insertEmailVerificationStatusForTest(
          EmailVerificationStatusTestData.emailVerificationStatus()
            .copy(
              _id                             = ggCredId.value.drop(7),
              credId                          = ggCredId,
              numberOfPasscodeJourneysStarted = NumberOfPasscodeJourneysStarted(5)
            )
        )
        val result: Future[Result] = controller.state(request.withBody(getEmailVerificationResultRequest))
        status(result) shouldBe OK
        contentAsJson(result).as[EmailVerificationState] shouldBe EmailVerificationState.TooManyPasscodeJourneysStarted
      }

      "return TooManyDifferentEmailAddresses if there are >= 10 different entries for a given ggCredId" in new JourneyItTest {
        stubCommonActions()
        val ggCredId: GGCredId = GGCredId(s"authId-${UUID.randomUUID().toString}")
        val email: Email = Email(SensitiveString("email@test.com"))
        insertEmailVerificationStatusForTest(EmailVerificationStatusTestData.createXNumberOfStatuses(10, credId = ggCredId): _*)
        val getEmailVerificationResultRequest: GetEmailVerificationResultRequest = GetEmailVerificationResultRequest(ggCredId, email)
        val result: Future[Result] = controller.state(request.withBody(getEmailVerificationResultRequest))
        status(result) shouldBe OK
        contentAsJson(result).as[EmailVerificationState] shouldBe EmailVerificationState.TooManyDifferentEmailAddresses
      }
    }

    "POST /email-verification/verification-state/update" - {

      "create a new record with numberOfPasscodeJourneysStarted = 1" in new JourneyItTest {
        stubCommonActions()
        val ggCredId: GGCredId = GGCredId(s"authId-${UUID.randomUUID().toString}")
        val email: Email = Email(SensitiveString("email@test.com"))
        val getEmailVerificationResultRequest: GetEmailVerificationResultRequest = GetEmailVerificationResultRequest(ggCredId, email)
        val result: Future[Result] = controller.updateState(request.withBody(getEmailVerificationResultRequest))
        status(result) shouldBe OK
        val records: List[EmailVerificationStatus] = emailVerificationStatusRepo.findAllEntries(ggCredId).futureValue
        records.size shouldBe 1
        val recordInserted: Option[EmailVerificationStatus] = records.headOption
        recordInserted.value.email shouldBe email
        recordInserted.value.credId shouldBe ggCredId
        recordInserted.value.numberOfPasscodeJourneysStarted.value shouldBe 1
      }

      "create a new record if credId matches but email does not" in new JourneyItTest {
        stubCommonActions()
        val ggCredId: GGCredId = GGCredId(s"authId-${UUID.randomUUID().toString}")
        val email: Email = Email(SensitiveString("email@test.com"))
        val getEmailVerificationResultRequest: GetEmailVerificationResultRequest = GetEmailVerificationResultRequest(ggCredId, email)

        insertEmailVerificationStatusForTest(
          EmailVerificationStatusTestData.emailVerificationStatus(
            EmailVerificationStatusTestData.email("thisone@isdifferent.com"), ggCredId.value.drop(7)
          ).copy(credId = ggCredId)
        )

        val result: Future[Result] = controller.updateState(request.withBody(getEmailVerificationResultRequest))
        status(result) shouldBe OK
        val records: List[EmailVerificationStatus] = emailVerificationStatusRepo.findAllEntries(ggCredId).futureValue
        records.size shouldBe 2
      }

      "update a record when credId and email match" in new JourneyItTest {
        stubCommonActions()
        val ggCredId: GGCredId = GGCredId(s"authId-${UUID.randomUUID().toString}")
        val email: Email = Email(SensitiveString("email@test.com"))
        val getEmailVerificationResultRequest: GetEmailVerificationResultRequest = GetEmailVerificationResultRequest(ggCredId, email)
        val update1: Future[Result] = controller.updateState(request.withBody(getEmailVerificationResultRequest))
        status(update1) shouldBe OK

        def records: List[EmailVerificationStatus] = emailVerificationStatusRepo.findAllEntries(ggCredId).futureValue
        records.size shouldBe 1

        val update2: Future[Result] = controller.updateState(request.withBody(getEmailVerificationResultRequest))
        status(update2) shouldBe OK

        records.size shouldBe 1

        val recordInserted: Option[EmailVerificationStatus] = records.headOption
        recordInserted.value.email shouldBe email
        recordInserted.value.credId shouldBe ggCredId
        recordInserted.value.numberOfPasscodeJourneysStarted.value shouldBe 2
      }
    }

    "POST /email-verification/verification-state/result-update" - {
      "update the verificationResult to Verified" in new JourneyItTest {
        stubCommonActions()
        val ggCredId: GGCredId = GGCredId(s"authId-${UUID.randomUUID().toString}")
        val email: Email = Email(SensitiveString("email@test.com"))
        val resultUpdateRequest: EmailVerificationStateResultRequest =
          EmailVerificationStateResultRequest(GetEmailVerificationResultRequest(ggCredId, email), EmailVerificationResult.Verified)

        insertEmailVerificationStatusForTest(
          EmailVerificationStatusTestData.emailVerificationStatus()
            .copy(
              _id    = ggCredId.value.drop(7),
              credId = ggCredId
            )
        )

        val result: Future[Result] = controller.updateStateResult(request.withBody(resultUpdateRequest))
        status(result) shouldBe OK
        val records: List[EmailVerificationStatus] = emailVerificationStatusRepo.findAllEntries(ggCredId).futureValue
        records.size shouldBe 1
        val recordInserted: Option[EmailVerificationStatus] = records.headOption
        recordInserted.value.email shouldBe email
        recordInserted.value.credId shouldBe ggCredId
        recordInserted.value.numberOfPasscodeJourneysStarted.value shouldBe 1
        recordInserted.value.verificationResult shouldBe Some(EmailVerificationResult.Verified)
      }

      "update the verificationResult to Locked" in new JourneyItTest {
        stubCommonActions()
        val ggCredId: GGCredId = GGCredId(s"authId-${UUID.randomUUID().toString}")
        val email: Email = Email(SensitiveString("email@test.com"))
        val resultUpdateRequest: EmailVerificationStateResultRequest =
          EmailVerificationStateResultRequest(GetEmailVerificationResultRequest(ggCredId, email), EmailVerificationResult.Locked)

        insertEmailVerificationStatusForTest(
          EmailVerificationStatusTestData.emailVerificationStatus()
            .copy(
              _id    = ggCredId.value.drop(7),
              credId = ggCredId
            )
        )

        val result: Future[Result] = controller.updateStateResult(request.withBody(resultUpdateRequest))
        status(result) shouldBe OK
        val records: List[EmailVerificationStatus] = emailVerificationStatusRepo.findAllEntries(ggCredId).futureValue
        records.size shouldBe 1
        val recordInserted: Option[EmailVerificationStatus] = records.headOption
        recordInserted.value.email shouldBe email
        recordInserted.value.credId shouldBe ggCredId
        recordInserted.value.numberOfPasscodeJourneysStarted.value shouldBe 2
        recordInserted.value.verificationResult shouldBe Some(EmailVerificationResult.Locked)
      }
    }
  }

}
