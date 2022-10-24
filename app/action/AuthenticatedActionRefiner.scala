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
import com.google.inject.Inject
import play.api.Logger
import play.api.mvc.Results.{InternalServerError, Unauthorized}
import play.api.mvc.{ActionRefiner, MessagesControllerComponents, Request, Result}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.{AuthConnector, AuthProviders, AuthorisationException, AuthorisedFunctions, NoActiveSession}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendHeaderCarrierProvider

import scala.concurrent.{ExecutionContext, Future}

class AuthenticatedActionRefiner @Inject() (
    val authConnector: AuthConnector,
    cc:                MessagesControllerComponents
)(
    implicit
    ec: ExecutionContext
) extends ActionRefiner[Request, AuthenticatedRequest] with BackendHeaderCarrierProvider with AuthorisedFunctions {

  private val logger = Logger(getClass)

  override protected def refine[A](request: Request[A]): Future[Either[Result, AuthenticatedRequest[A]]] = {
    authorised(AuthProviders(GovernmentGateway)) {
      Future.successful(Right(model.AuthenticatedRequest(request)))
    }(hc(request), ec).recover {
      case _: NoActiveSession =>
        Left(Unauthorized)

      case e: AuthorisationException =>
        logger.warn(s"Unauthorised because of ${e.reason}, please investigate why", e)
        Left(InternalServerError)
    }
  }

  override protected def executionContext: ExecutionContext = cc.executionContext

}
