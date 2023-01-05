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

package services

import cats.syntax.eq._
import com.google.inject.{Inject, Singleton}
import config.AppConfig
import connectors.EmailVerificationConnector
import email.EmailVerificationStatusService
import essttp.emailverification.EmailVerificationState.{OkToBeVerified, TooManyDifferentEmailAddresses, TooManyPasscodeAttempts, TooManyPasscodeJourneysStarted}
import essttp.emailverification._
import essttp.rootmodel.Email
import essttp.utils.HttpResponseUtils.HttpResponseOps
import models.emailverification.RequestEmailVerificationRequest.EmailDetails
import models.emailverification.{RequestEmailVerificationRequest, RequestEmailVerificationSuccess}
import play.api.http.Status.{CREATED, INTERNAL_SERVER_ERROR, NOT_FOUND, UNAUTHORIZED}
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

import java.net.URI
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmailVerificationService @Inject() (
    appConfig:                      AppConfig,
    connector:                      EmailVerificationConnector,
    emailVerificationStatusService: EmailVerificationStatusService
)(implicit ec: ExecutionContext) {

  private val maxPasscodeJourneysPerEmailAddress: Int = appConfig.emailVerificationStatusMaxAttemptsPerEmail
  private val maxNumberOfDifferentEmails: Int = appConfig.emailVerificationStatusMaxUniqueEmailsAllowed

  def startEmailVerificationJourney(request: StartEmailVerificationJourneyRequest)(implicit hc: HeaderCarrier): Future[StartEmailVerificationJourneyResponse] = {
    val emailVerificationRequest = RequestEmailVerificationRequest(
      request.credId,
      request.continueUrl,
      request.origin,
      request.deskproServiceName,
      request.accessibilityStatementUrl,
      request.pageTitle,
      request.backUrl,
      EmailDetails(
        request.email,
        request.enterEmailUrl
      ),
      request.lang
    )

      def makeEmailVerificationCall: Future[StartEmailVerificationJourneyResponse] = {
        connector.requestEmailVerification(emailVerificationRequest).map { response =>
          if (response.status === CREATED) {
            response.parseJSON[RequestEmailVerificationSuccess]
              .fold(
                msg => throw UpstreamErrorResponse(msg, INTERNAL_SERVER_ERROR),
                { success =>
                  val redirectUrl =
                    if (request.isLocal && !URI.create(success.redirectUri).isAbsolute) {
                      s"${appConfig.emailVerificationFrontendLocalUrl}${success.redirectUri}"
                    } else {
                      success.redirectUri
                    }
                  StartEmailVerificationJourneyResponse.Success(redirectUrl)
                }
              )
          } else {
            throw UpstreamErrorResponse(s"Call to request email verification came back with unexpected status ${response.status.toString}", response.status)
          }
        }.recover {
          case u: UpstreamErrorResponse if u.statusCode === UNAUTHORIZED =>
            StartEmailVerificationJourneyResponse.Error(TooManyPasscodeAttempts)
        }
      }

    emailVerificationStatusService.updateNumberOfPasscodeJourneys(emailVerificationRequest.credId, emailVerificationRequest.email.address).flatMap { _ =>
      emailVerificationStatusService.findEmailVerificationStatuses(request.credId).flatMap {
        case Some(value) => getState(request.email, value) match {
          case EmailVerificationState.OkToBeVerified                 => makeEmailVerificationCall
          case EmailVerificationState.AlreadyVerified                => Future.successful(StartEmailVerificationJourneyResponse.AlreadyVerified)
          case EmailVerificationState.TooManyPasscodeAttempts        => Future.successful(StartEmailVerificationJourneyResponse.Error(TooManyPasscodeAttempts))
          case EmailVerificationState.TooManyPasscodeJourneysStarted => Future.successful(StartEmailVerificationJourneyResponse.Error(TooManyPasscodeJourneysStarted))
          case EmailVerificationState.TooManyDifferentEmailAddresses => Future.successful(StartEmailVerificationJourneyResponse.Error(TooManyDifferentEmailAddresses))
        }
        case None => makeEmailVerificationCall
      }
    }
  }

  def getVerificationResult(request: GetEmailVerificationResultRequest)(implicit hc: HeaderCarrier): Future[EmailVerificationResult] = {

      def isVerifiedOrNot: Future[EmailVerificationResult] = connector.getVerificationStatus(request.credId).flatMap { statusResponse =>
        statusResponse.emails.find(_.emailAddress === request.email.value.decryptedValue) match {
          case None =>
            throw UpstreamErrorResponse("Verification result not found for email address", NOT_FOUND)
          case Some(status) =>
            (status.verified, status.locked) match {
              case (true, false) =>
                emailVerificationStatusService.updateEmailVerificationStatusResult(request.credId, request.email, EmailVerificationResult.Verified)
                  .map(_ => EmailVerificationResult.Verified)
              case (false, true) =>
                emailVerificationStatusService.updateEmailVerificationStatusResult(request.credId, request.email, EmailVerificationResult.Locked)
                  .map(_ => EmailVerificationResult.Locked)
              case _ =>
                throw UpstreamErrorResponse(s"Got unexpected combination of verified=${status.verified.toString} and " +
                  s"locked=${status.locked.toString} in email verification status response", INTERNAL_SERVER_ERROR)
            }
        }
      }

    emailVerificationStatusService.findEmailVerificationStatuses(request.credId).map {
      case Some(value) => getState(request.email, value)
      case None        => OkToBeVerified
    }.flatMap {
      case EmailVerificationState.OkToBeVerified                 => isVerifiedOrNot
      case EmailVerificationState.AlreadyVerified                => Future.successful(EmailVerificationResult.Verified)
      case EmailVerificationState.TooManyPasscodeAttempts        => Future.successful(EmailVerificationResult.Locked)
      case EmailVerificationState.TooManyPasscodeJourneysStarted => Future.successful(EmailVerificationResult.Locked)
      case EmailVerificationState.TooManyDifferentEmailAddresses => Future.successful(EmailVerificationResult.Locked)
    }
  }

  private def getState(currentEmail: Email, statuses: List[EmailVerificationStatus]): EmailVerificationState = {
    if (statuses.isEmpty) OkToBeVerified
    else {
      val statusForCurrentEmail: Option[EmailVerificationStatus] = statuses.find(_.email === currentEmail)
      val alreadyVerified: Boolean = statusForCurrentEmail.exists(_.verificationResult.contains(EmailVerificationResult.Verified))
      val tooManyPasscodeAttempts: Boolean = statusForCurrentEmail.exists(_.verificationResult.contains(EmailVerificationResult.Locked))
      val tooManyPasscodeJourneysStarted: Boolean = statusForCurrentEmail.exists(_.numberOfPasscodeJourneysStarted.value >= maxPasscodeJourneysPerEmailAddress)
      val tooManyDifferentEmailAddresses: Boolean = statuses.sizeIs >= maxNumberOfDifferentEmails

      if (alreadyVerified) EmailVerificationState.AlreadyVerified
      else if (tooManyDifferentEmailAddresses) EmailVerificationState.TooManyDifferentEmailAddresses
      else if (tooManyPasscodeAttempts) EmailVerificationState.TooManyPasscodeAttempts
      else if (tooManyPasscodeJourneysStarted) EmailVerificationState.TooManyPasscodeJourneysStarted
      else EmailVerificationState.OkToBeVerified
    }
  }

}
