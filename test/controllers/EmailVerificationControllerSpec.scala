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

import essttp.emailverification.{EmailVerificationResult, GetEmailVerificationResultRequest, StartEmailVerificationJourneyRequest, StartEmailVerificationJourneyResponse}
import essttp.rootmodel.{Email, GGCredId}
import journey.EmailVerificationController
import models.emailverification.EmailVerificationResultResponse.EmailResult
import models.emailverification.RequestEmailVerificationSuccess
import play.api.mvc.Result
import play.api.test.Helpers._
import testsupport.ItSpec
import testsupport.stubs.EmailVerificationStub
import uk.gov.hmrc.crypto.Sensitive.SensitiveString
import uk.gov.hmrc.http.UpstreamErrorResponse

import scala.concurrent.Future

class EmailVerificationControllerSpec extends ItSpec {

  val controller = app.injector.instanceOf[EmailVerificationController]

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
}
