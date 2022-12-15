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

package services

import cats.syntax.eq._
import com.google.inject.{Inject, Singleton}
import config.AppConfig
import connectors.EmailVerificationConnector
import essttp.emailverification.{EmailVerificationResult, GetEmailVerificationResultRequest, StartEmailVerificationJourneyRequest, StartEmailVerificationJourneyResponse}
import essttp.utils.HttpResponseUtils.HttpResponseOps
import models.emailverification.RequestEmailVerificationRequest.EmailDetails
import models.emailverification.{RequestEmailVerificationRequest, RequestEmailVerificationSuccess}
import play.api.http.Status.{CREATED, INTERNAL_SERVER_ERROR, NOT_FOUND, UNAUTHORIZED}
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

import java.net.URI
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmailVerificationService @Inject() (
    connector: EmailVerificationConnector,
    appConfig: AppConfig
)(implicit ec: ExecutionContext) {

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

    connector.requestEmailVerification(emailVerificationRequest).map{ response =>
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
      case u: UpstreamErrorResponse if u.statusCode === UNAUTHORIZED => StartEmailVerificationJourneyResponse.Locked
    }
  }

  def getVerificationResult(request: GetEmailVerificationResultRequest)(implicit hc: HeaderCarrier): Future[EmailVerificationResult] =
    connector.getVerificationStatus(request.credId).map{ statusResponse =>
      statusResponse.emails.find(_.emailAddress === request.email.value.decryptedValue) match {
        case None =>
          throw UpstreamErrorResponse("Verification result not found for email address", NOT_FOUND)

        case Some(status) =>
          (status.verified, status.locked) match {
            case (true, false) =>
              EmailVerificationResult.Verified
            case (false, true) =>
              EmailVerificationResult.Locked
            case _ =>
              throw UpstreamErrorResponse(s"Got unexpected combination of verified=${status.verified.toString} and " +
                s"locked=${status.locked.toString} in email verification status response", INTERNAL_SERVER_ERROR)
          }
      }

    }

}
