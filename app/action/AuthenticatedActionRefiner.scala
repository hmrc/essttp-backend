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

package action

import action.model.AuthenticatedRequest
import cats.data.EitherT
import com.google.inject.Inject
import config.AppConfig
import essttp.utils.RequestSupport._
import play.api.Logger
import play.api.mvc.Results.{InternalServerError, Unauthorized}
import play.api.mvc.{ActionRefiner, MessagesControllerComponents, Request, Result}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.{AuthProviders, AuthorisationException, AuthorisedFunctions, NoActiveSession}
import uk.gov.hmrc.http.{Authorization, HeaderCarrier}

import scala.concurrent.{ExecutionContext, Future}

class AuthenticatedActionRefiner @Inject() (
    af:        AuthorisedFunctions,
    appConfig: AppConfig,
    cc:        MessagesControllerComponents
)(
    implicit
    ec: ExecutionContext
) extends ActionRefiner[Request, AuthenticatedRequest] {

  private val logger = Logger(getClass)

  override protected def refine[A](request: Request[A]): Future[Either[Result, AuthenticatedRequest[A]]] = {
    val result =
      EitherT.fromOption[Future](request.headers.get("Authorization"), Unauthorized)
        .flatMapF{ authorization =>
          // authorization in HeaderCarrier will not be populated even if it's in the request header if a session id is
          // provided - stick it explicitly in the HeaderCarrier here to make the authorised call work below
          val headerCarrier: HeaderCarrier = hc(request).copy(authorization = Some(Authorization(authorization)))

          af.authorised(AuthProviders(GovernmentGateway)) {
            Future.successful(Right(model.AuthenticatedRequest(request)))
          }(headerCarrier, ec).recover {
            case _: NoActiveSession =>
              Left(Unauthorized)

            case e: AuthorisationException =>
              logger.error(s"Unauthorised because of ${e.reason}, please investigate why", e)
              Left(InternalServerError)
          }
        }

    result.value
  }

  override protected def executionContext: ExecutionContext = cc.executionContext

}
