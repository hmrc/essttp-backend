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

package essttp.bars

import essttp.bars.model.{BarsGetStatusParams, BarsUpdateStatusParams, BarsVerifyStatusResponse}
import essttp.utils.RequestSupport._
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.http.HttpReads.Implicits.{readUnit => _, _}
import essttp.utils.HttpReadsUnitThrowingException.readUnit
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BarsVerifyStatusConnector(httpClient: HttpClient, baseUrl: String)(implicit ec: ExecutionContext) {

  def status(params: BarsGetStatusParams)(implicit request: RequestHeader): Future[BarsVerifyStatusResponse] =
    httpClient.POST[BarsGetStatusParams, BarsVerifyStatusResponse](s"$baseUrl/essttp-backend/bars/verify/status", params)

  def update(params: BarsUpdateStatusParams)(implicit request: RequestHeader): Future[Unit] =
    httpClient.POST[BarsUpdateStatusParams, Unit](s"$baseUrl/essttp-backend/bars/verify/update", params)

  @Inject()
  def this(httpClient: HttpClient, servicesConfig: ServicesConfig)(implicit ec: ExecutionContext) = this(
    httpClient,
    servicesConfig.baseUrl("essttp-backend")
  )
}
