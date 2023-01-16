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

import email.EmailVerificationStatusRepo
import essttp.emailverification.EmailVerificationState.{AlreadyVerified, TooManyDifferentEmailAddresses, TooManyPasscodeAttempts, TooManyPasscodeJourneysStarted}
import essttp.emailverification._
import essttp.rootmodel.ttp.eligibility.CustomerDetail
import essttp.rootmodel.{Email, GGCredId}
import journey.EmailVerificationController
import models.emailverification.EmailVerificationResultResponse.EmailResult
import models.emailverification.RequestEmailVerificationSuccess
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Result
import play.api.test.Helpers._
import testsupport.ItSpec
import testsupport.stubs.{AuditConnectorStub, EmailVerificationStub}
import testsupport.testdata.TdAll
import uk.gov.hmrc.crypto.Sensitive.SensitiveString
import uk.gov.hmrc.http.UpstreamErrorResponse

import java.time.temporal.ChronoUnit
import java.time.{Instant, LocalDateTime, ZoneOffset}
import java.util.UUID
import scala.concurrent.Future

class EmailVerificationControllerSpec extends ItSpec {

  val controller: EmailVerificationController = app.injector.instanceOf[EmailVerificationController]

  val emailVerificationStatusRepo: EmailVerificationStatusRepo = app.injector.instanceOf[EmailVerificationStatusRepo]

  "POST /email-verification/start" - {

      def startEmailVerificationJourneyRequest(isLocal: Boolean) = StartEmailVerificationJourneyRequest(
        GGCredId(s"authId-${UUID.randomUUID().toString}"),
        "continue",
        "origin",
        "deskpro",
        "accessibility",
        "title",
        "back",
        "enter",
        Email(SensitiveString(s"email${UUID.randomUUID().toString}@test.com")),
        "en",
        isLocal = isLocal
      )

      def emailVerificationStatus(ggCredId: GGCredId, email: Email): EmailVerificationStatus = EmailVerificationStatus(
        _id                             = UUID.randomUUID().toString,
        credId                          = ggCredId,
        email                           = email,
        numberOfPasscodeJourneysStarted = NumberOfPasscodeJourneysStarted(1),
        verificationResult              = None,
        createdAt                       = Instant.now,
        lastUpdated                     = Instant.now
      )

    "return a redirect url if a journey is successfully started" in new JourneyItTest {
      val redirectUri: String = "/redirect"
      val startRequest: StartEmailVerificationJourneyRequest = startEmailVerificationJourneyRequest(false)
      insertJourneyForTest(TdAll.EpayeBta.journeyAfterSelectedEmail
        .copy(_id = tdAll.journeyId)
        .copy(correlationId = tdAll.correlationId)
        .copy(emailToBeVerified = startRequest.email)
        .copy(eligibilityCheckResult = TdAll.EpayeBta.journeyAfterSelectedEmail.eligibilityCheckResult.copy(
          customerDetails = Some(List(CustomerDetail(Some(startRequest.email), None)))
        )))
      stubCommonActions()
      EmailVerificationStub.requestEmailVerification(Right(RequestEmailVerificationSuccess(redirectUri)))

      val result: Future[Result] = controller.startEmailVerificationJourney(tdAll.journeyId)(request.withBody(startRequest))
      status(result) shouldBe OK
      contentAsJson(result).as[StartEmailVerificationJourneyResponse] shouldBe StartEmailVerificationJourneyResponse.Success(redirectUri)

      EmailVerificationStub.verifyRequestEmailVerification(startRequest)
      AuditConnectorStub.verifyEventAudited(
        "EmailVerificationRequested",
        Json.parse(
          s"""
             |{
             |  "origin" : "Bta",
             |  "taxType" : "Epaye",
             |  "taxDetail" : {
             |    "employerRef" : "864FZ00049",
             |    "accountsOfficeRef" : "123PA44545546"
             |  },
             |  "correlationId" : "${tdAll.correlationId.value.toString}",
             |  "emailAddress" : "${startRequest.email.value.decryptedValue}",
             |  "emailSource" : "ETMP",
             |  "result" : "Started"
             |}
             |""".stripMargin
        ).as[JsObject]
      )
    }

    "prefix the uri in the redirecturi with the email-verification-frontend host and port if the redirectUri if the " +
      "environment is local and the uri is absolute" in new JourneyItTest {
        val redirectUri: String = "/redirect"
        val startRequest: StartEmailVerificationJourneyRequest = startEmailVerificationJourneyRequest(true)

        insertJourneyForTest(TdAll.EpayeBta.journeyAfterSelectedEmail.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
        stubCommonActions()
        EmailVerificationStub.requestEmailVerification(Right(RequestEmailVerificationSuccess(redirectUri)))

        val result: Future[Result] = controller.startEmailVerificationJourney(tdAll.journeyId)(request.withBody(startRequest))
        status(result) shouldBe OK
        contentAsJson(result).as[StartEmailVerificationJourneyResponse] shouldBe StartEmailVerificationJourneyResponse.Success(s"http://localhost:9890$redirectUri")

        EmailVerificationStub.verifyRequestEmailVerification(startRequest)
      }

    "maintain the redirectUri in the email verification response if the environment is local and the uri is absolute" in new JourneyItTest {
      val redirectUri: String = "http:///host:12345/redirect"
      val startRequest: StartEmailVerificationJourneyRequest = startEmailVerificationJourneyRequest(true)

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterSelectedEmail.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      stubCommonActions()
      EmailVerificationStub.requestEmailVerification(Right(RequestEmailVerificationSuccess(redirectUri)))

      val result: Future[Result] = controller.startEmailVerificationJourney(tdAll.journeyId)(request.withBody(startRequest))
      status(result) shouldBe OK
      contentAsJson(result).as[StartEmailVerificationJourneyResponse] shouldBe StartEmailVerificationJourneyResponse.Success(redirectUri)

      EmailVerificationStub.verifyRequestEmailVerification(startRequest)
    }

    "return a locked response if the email-verification service gives a 401 (UNAUTHORIZED) response" in new JourneyItTest {
      val startRequest: StartEmailVerificationJourneyRequest = startEmailVerificationJourneyRequest(false)

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterSelectedEmail.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      stubCommonActions()
      EmailVerificationStub.requestEmailVerification(Left(UNAUTHORIZED))

      val result: Future[Result] = controller.startEmailVerificationJourney(tdAll.journeyId)(request.withBody(startRequest))
      status(result) shouldBe OK
      contentAsJson(result).as[StartEmailVerificationJourneyResponse] shouldBe StartEmailVerificationJourneyResponse.Error(EmailVerificationState.TooManyPasscodeAttempts)

      EmailVerificationStub.verifyRequestEmailVerification(startRequest)
      AuditConnectorStub.verifyEventAudited(
        "EmailVerificationRequested",
        Json.parse(
          s"""
             |{
             |  "origin" : "Bta",
             |  "taxType" : "Epaye",
             |  "taxDetail" : {
             |    "employerRef" : "864FZ00049",
             |    "accountsOfficeRef" : "123PA44545546"
             |  },
             |  "correlationId" : "${tdAll.correlationId.value.toString}",
             |  "emailAddress" : "${startRequest.email.value.decryptedValue}",
             |  "emailSource" : "TEMP",
             |  "result" : "${TooManyPasscodeAttempts.entryName}"
             |}
             |""".stripMargin
        ).as[JsObject]
      )
    }

    "return 'AlreadyVerified' and not call email-verification if email is already verified" in new JourneyItTest {
      stubCommonActions()
      val startRequest: StartEmailVerificationJourneyRequest = startEmailVerificationJourneyRequest(false)

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterSelectedEmail.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      emailVerificationStatusRepo.upsert(emailVerificationStatus(startRequest.credId, startRequest.email)
        .copy(verificationResult = Some(EmailVerificationResult.Verified))).futureValue

      val result: Future[Result] = controller.startEmailVerificationJourney(tdAll.journeyId)(request.withBody(startRequest))
      status(result) shouldBe OK
      contentAsJson(result).as[StartEmailVerificationJourneyResponse] shouldBe StartEmailVerificationJourneyResponse.Error(EmailVerificationState.AlreadyVerified)
      EmailVerificationStub.verifyNoneRequestVerification()
      AuditConnectorStub.verifyEventAudited(
        "EmailVerificationRequested",
        Json.parse(
          s"""
             |{
             |  "origin" : "Bta",
             |  "taxType" : "Epaye",
             |  "taxDetail" : {
             |    "employerRef" : "864FZ00049",
             |    "accountsOfficeRef" : "123PA44545546"
             |  },
             |  "correlationId" : "${tdAll.correlationId.value.toString}",
             |  "emailAddress" : "${startRequest.email.value.decryptedValue}",
             |  "emailSource" : "TEMP",
             |  "result" : "${AlreadyVerified.entryName}"
             |}
             |""".stripMargin
        ).as[JsObject]
      )
    }

    "return 'TooManyPasscodeAttempts' if that is the status in mongo" in new JourneyItTest {
      stubCommonActions()
      val startRequest: StartEmailVerificationJourneyRequest = startEmailVerificationJourneyRequest(false)

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterSelectedEmail.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      emailVerificationStatusRepo.upsert(emailVerificationStatus(startRequest.credId, startRequest.email)
        .copy(verificationResult = Some(EmailVerificationResult.Locked))).futureValue

      val result: Future[Result] = controller.startEmailVerificationJourney(tdAll.journeyId)(request.withBody(startRequest))
      status(result) shouldBe OK
      contentAsJson(result).as[StartEmailVerificationJourneyResponse] shouldBe StartEmailVerificationJourneyResponse.Error(EmailVerificationState.TooManyPasscodeAttempts)
      EmailVerificationStub.verifyNoneRequestVerification()
      AuditConnectorStub.verifyEventAudited(
        "EmailVerificationRequested",
        Json.parse(
          s"""
             |{
             |  "origin" : "Bta",
             |  "taxType" : "Epaye",
             |  "taxDetail" : {
             |    "employerRef" : "864FZ00049",
             |    "accountsOfficeRef" : "123PA44545546"
             |  },
             |  "correlationId" : "${tdAll.correlationId.value.toString}",
             |  "emailAddress" : "${startRequest.email.value.decryptedValue}",
             |  "emailSource" : "TEMP",
             |  "result" : "${TooManyPasscodeAttempts.entryName}"
             |}
             |""".stripMargin
        ).as[JsObject]
      )
    }

    "return 'TooManyPasscodeJourneysStarted' if that is the status in mongo" in new JourneyItTest {
      stubCommonActions()
      val startRequest: StartEmailVerificationJourneyRequest = startEmailVerificationJourneyRequest(false)

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterSelectedEmail.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      emailVerificationStatusRepo.upsert(emailVerificationStatus(startRequest.credId, startRequest.email)
        .copy(numberOfPasscodeJourneysStarted = NumberOfPasscodeJourneysStarted(5))).futureValue

      val result: Future[Result] = controller.startEmailVerificationJourney(tdAll.journeyId)(request.withBody(startRequest))
      status(result) shouldBe OK
      contentAsJson(result).as[StartEmailVerificationJourneyResponse] shouldBe StartEmailVerificationJourneyResponse.Error(EmailVerificationState.TooManyPasscodeJourneysStarted)
      EmailVerificationStub.verifyNoneRequestVerification()
      AuditConnectorStub.verifyEventAudited(
        "EmailVerificationRequested",
        Json.parse(
          s"""
             |{
             |  "origin" : "Bta",
             |  "taxType" : "Epaye",
             |  "taxDetail" : {
             |    "employerRef" : "864FZ00049",
             |    "accountsOfficeRef" : "123PA44545546"
             |  },
             |  "correlationId" : "${tdAll.correlationId.value.toString}",
             |  "emailAddress" : "${startRequest.email.value.decryptedValue}",
             |  "emailSource" : "TEMP",
             |  "result" : "${TooManyPasscodeJourneysStarted.entryName}"
             |}
             |""".stripMargin
        ).as[JsObject]
      )
    }

    "return 'TooManyDifferentEmailAddresses' if that is the status in mongo" in new JourneyItTest {
      stubCommonActions()
      val startRequest: StartEmailVerificationJourneyRequest = startEmailVerificationJourneyRequest(false)

      insertJourneyForTest(TdAll.EpayeBta.journeyAfterSelectedEmail.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      val emailVerificationStatus: EmailVerificationStatus = EmailVerificationStatus(
        _id                             = UUID.randomUUID().toString,
        credId                          = startRequest.credId,
        email                           = startRequest.email,
        numberOfPasscodeJourneysStarted = NumberOfPasscodeJourneysStarted(1),
        verificationResult              = None,
        createdAt                       = Instant.now,
        lastUpdated                     = Instant.now
      )
      val nineOtherEntries: Seq[EmailVerificationStatus] =
        (1 to 9).map(_ => emailVerificationStatus.copy(
          _id   = UUID.randomUUID().toString,
          email = Email(SensitiveString(s"email${UUID.randomUUID().toString}@test.com"))
        ))
      emailVerificationStatusRepo.upsert(emailVerificationStatus)
      nineOtherEntries.foreach(emailVerificationStatusRepo.upsert(_).futureValue)
      emailVerificationStatusRepo.findAllEntries(startRequest.credId).futureValue.size shouldBe 10

      val result: Future[Result] = controller.startEmailVerificationJourney(tdAll.journeyId)(request.withBody(startRequest))
      status(result) shouldBe OK
      contentAsJson(result).as[StartEmailVerificationJourneyResponse] shouldBe StartEmailVerificationJourneyResponse.Error(EmailVerificationState.TooManyDifferentEmailAddresses)
      EmailVerificationStub.verifyNoneRequestVerification()
      AuditConnectorStub.verifyEventAudited(
        "EmailVerificationRequested",
        Json.parse(
          s"""
             |{
             |  "origin" : "Bta",
             |  "taxType" : "Epaye",
             |  "taxDetail" : {
             |    "employerRef" : "864FZ00049",
             |    "accountsOfficeRef" : "123PA44545546"
             |  },
             |  "correlationId" : "${tdAll.correlationId.value.toString}",
             |  "emailAddress" : "${startRequest.email.value.decryptedValue}",
             |  "emailSource" : "TEMP",
             |  "result" : "${TooManyDifferentEmailAddresses.entryName}"
             |}
             |""".stripMargin
        ).as[JsObject]
      )
    }

  }

  "POST /email-verification/result" - {

    "return a 'Verified' response if the email address has been verified with the GG cred id" in new JourneyItTest {
      insertJourneyForTest(TdAll.EpayeBta.journeyAfterSelectedEmail.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      stubCommonActions()
      val ggCredId = GGCredId(s"authId-${UUID.randomUUID().toString}")
      val email = Email(SensitiveString(s"email${UUID.randomUUID().toString}@test.com"))
      val getResultRequest = GetEmailVerificationResultRequest(ggCredId, email)
      EmailVerificationStub.getVerificationResult(ggCredId, Right(List(EmailResult(email.value.decryptedValue, verified = true, locked = false))))

      val result: Future[Result] = controller.getEmailVerificationResult(tdAll.journeyId)(request.withBody(getResultRequest))
      status(result) shouldBe OK
      contentAsJson(result).as[EmailVerificationResult] shouldBe EmailVerificationResult.Verified
      EmailVerificationStub.verifyNoneGetVerificationStatus(ggCredId)
      AuditConnectorStub.verifyEventAudited(
        "EmailVerificationResult",
        Json.parse(
          s"""
             |{
             |  "origin" : "Bta",
             |  "taxType" : "Epaye",
             |  "taxDetail" : {
             |    "employerRef" : "864FZ00049",
             |    "accountsOfficeRef" : "123PA44545546"
             |  },
             |  "correlationId" : "${tdAll.correlationId.value.toString}",
             |  "emailAddress" : "${email.value.decryptedValue}",
             |  "emailSource" : "TEMP",
             |  "result" : "Success"
             |}
             |""".stripMargin
        ).as[JsObject]
      )
    }

    "return a 'TooManyPasscodeAttempts' response if the email address has been locked with the GG cred id" in new JourneyItTest {
      insertJourneyForTest(TdAll.EpayeBta.journeyAfterSelectedEmail.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      stubCommonActions()
      val ggCredId = GGCredId(s"authId-${UUID.randomUUID().toString}")
      val email = Email(SensitiveString(s"email${UUID.randomUUID().toString}@test.com"))
      val getResultRequest = GetEmailVerificationResultRequest(ggCredId, email)
      EmailVerificationStub.getVerificationResult(ggCredId, Right(List(EmailResult(email.value.decryptedValue, verified = false, locked = true))))

      val result: Future[Result] = controller.getEmailVerificationResult(tdAll.journeyId)(request.withBody(getResultRequest))
      status(result) shouldBe OK
      contentAsJson(result).as[EmailVerificationResult] shouldBe EmailVerificationResult.Locked
      EmailVerificationStub.verifyNoneGetVerificationStatus(ggCredId)
      AuditConnectorStub.verifyEventAudited(
        "EmailVerificationResult",
        Json.parse(
          s"""
             |{
             |  "origin" : "Bta",
             |  "taxType" : "Epaye",
             |  "taxDetail" : {
             |    "employerRef" : "864FZ00049",
             |    "accountsOfficeRef" : "123PA44545546"
             |  },
             |  "correlationId" : "${tdAll.correlationId.value.toString}",
             |  "emailAddress" : "${email.value.decryptedValue}",
             |  "emailSource" : "TEMP",
             |  "result" : "Failed",
             |  "failureReason" : "TooManyPasscodeAttempts"
             |}
             |""".stripMargin
        ).as[JsObject]
      )
    }

    "return 'Verified' response when there are < 10 entries for a given cred id and email verification return verified" in new JourneyItTest {
      insertJourneyForTest(TdAll.EpayeBta.journeyAfterSelectedEmail.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
      stubCommonActions()
      val ggCredId = GGCredId(s"authId-${UUID.randomUUID().toString}")
      val email = Email(SensitiveString(s"email${UUID.randomUUID().toString}@test.com"))
      val getResultRequest = GetEmailVerificationResultRequest(ggCredId, email)
      EmailVerificationStub.getVerificationResult(ggCredId, Right(List(EmailResult(email.value.decryptedValue, verified = true, locked = false))))

      val emailVerificationStatus: EmailVerificationStatus = EmailVerificationStatus(
        _id                             = UUID.randomUUID().toString,
        credId                          = ggCredId,
        email                           = email,
        numberOfPasscodeJourneysStarted = NumberOfPasscodeJourneysStarted(1),
        verificationResult              = None,
        createdAt                       = Instant.now,
        lastUpdated                     = Instant.now
      )
      val eightOtherEntries: Seq[EmailVerificationStatus] =
        (1 to 8).map(_ => emailVerificationStatus.copy(
          _id   = UUID.randomUUID().toString,
          email = Email(SensitiveString(s"email${UUID.randomUUID().toString}@test.com"))
        ))

      emailVerificationStatusRepo.upsert(emailVerificationStatus)
      eightOtherEntries.foreach(emailVerificationStatusRepo.upsert)

      val result: Future[Result] = controller.getEmailVerificationResult(tdAll.journeyId)(request.withBody(getResultRequest))
      status(result) shouldBe OK
      contentAsJson(result).as[EmailVerificationResult] shouldBe EmailVerificationResult.Verified
      EmailVerificationStub.verifyNoneGetVerificationStatus(ggCredId)
      AuditConnectorStub.verifyEventAudited(
        "EmailVerificationResult",
        Json.parse(
          s"""
             |{
             |  "origin" : "Bta",
             |  "taxType" : "Epaye",
             |  "taxDetail" : {
             |    "employerRef" : "864FZ00049",
             |    "accountsOfficeRef" : "123PA44545546"
             |  },
             |  "correlationId" : "${tdAll.correlationId.value.toString}",
             |  "emailAddress" : "${email.value.decryptedValue}",
             |  "emailSource" : "TEMP",
             |  "result" : "Success"
             |}
             |""".stripMargin
        ).as[JsObject]
      )
    }

    "should throw an error when" - {
      val ggCredId = GGCredId(s"authId-${UUID.randomUUID().toString}")
      val email = Email(SensitiveString(s"email${UUID.randomUUID().toString}@test.com"))
      val getResultRequest = GetEmailVerificationResultRequest(ggCredId, email)

        def testIsUpstreamErrorResponse(result: Future[Result], expectedStatusCode: Int = INTERNAL_SERVER_ERROR): Unit = {
          result.failed.futureValue match {
            case e: UpstreamErrorResponse => e.statusCode shouldBe expectedStatusCode
            case e                        => fail(s"Expected UpstreamErrorResponse but got ${e.toString}")
          }
          ()
        }

      "a result cannot be found for the given email address" in new JourneyItTest {
        insertJourneyForTest(TdAll.EpayeBta.journeyAfterSelectedEmail.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
        stubCommonActions()
        EmailVerificationStub.getVerificationResult(ggCredId, Right(List()))

        testIsUpstreamErrorResponse(
          controller.getEmailVerificationResult(tdAll.journeyId)(request.withBody(getResultRequest)),
          NOT_FOUND
        )
        AuditConnectorStub.verifyNoAuditEvent()
      }

      "verified=true and locked=true for the given email address" in new JourneyItTest {
        insertJourneyForTest(TdAll.EpayeBta.journeyAfterSelectedEmail.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
        stubCommonActions()
        EmailVerificationStub.getVerificationResult(ggCredId, Right(List(EmailResult(email.value.decryptedValue, verified = true, locked = true))))

        testIsUpstreamErrorResponse(controller.getEmailVerificationResult(tdAll.journeyId)(request.withBody(getResultRequest)))
        AuditConnectorStub.verifyNoAuditEvent()
      }

      "verified=false and locked=false for the given email address" in new JourneyItTest {
        insertJourneyForTest(TdAll.EpayeBta.journeyAfterSelectedEmail.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId))
        stubCommonActions()
        EmailVerificationStub.getVerificationResult(ggCredId, Right(List(EmailResult(email.value.decryptedValue, verified = false, locked = false))))

        testIsUpstreamErrorResponse(controller.getEmailVerificationResult(tdAll.journeyId)(request.withBody(getResultRequest)))
        AuditConnectorStub.verifyNoAuditEvent()
      }

    }

  }

  "POST /email-verification/earliest-created-at" - {
    "should return the earliest created at date" in new JourneyItTest {
      stubCommonActions()
      val credId: GGCredId = GGCredId(s"authId-${UUID.randomUUID().toString}")
      val emailVerificationStatusEarlier: EmailVerificationStatus = EmailVerificationStatus(
        _id                             = UUID.randomUUID().toString,
        credId                          = credId,
        email                           = Email(SensitiveString(s"email${UUID.randomUUID().toString}@test.com")),
        numberOfPasscodeJourneysStarted = NumberOfPasscodeJourneysStarted(1),
        verificationResult              = None,
        createdAt                       = Instant.now,
        lastUpdated                     = Instant.now
      )
      val emailVerificationStatusLater: EmailVerificationStatus = EmailVerificationStatus(
        _id                             = UUID.randomUUID().toString,
        credId                          = credId,
        email                           = Email(SensitiveString(s"email${UUID.randomUUID().toString}@test.com")),
        numberOfPasscodeJourneysStarted = NumberOfPasscodeJourneysStarted(1),
        verificationResult              = None,
        createdAt                       = emailVerificationStatusEarlier.createdAt.plus(1, ChronoUnit.MINUTES),
        lastUpdated                     = Instant.now
      )

      emailVerificationStatusRepo.upsert(emailVerificationStatusEarlier).futureValue
      emailVerificationStatusRepo.upsert(emailVerificationStatusLater).futureValue
      val result: Future[Result] = controller.getEarliestCreatedAt(request.withBody(credId))
      status(result) shouldBe OK
      contentAsJson(result).as[LocalDateTime].withNano(0) shouldBe LocalDateTime.ofInstant(emailVerificationStatusEarlier.createdAt, ZoneOffset.UTC).withNano(0)
    }
  }

}
