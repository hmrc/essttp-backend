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

package essttp.bars

import essttp.bars.model.{BarsUpdateVerifyStatusParams, BarsVerifyStatusResponse}
import essttp.rootmodel.TaxId
import essttp.utils.RequestSupport._
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BarsVerifyStatusConnector(httpClient: HttpClient, baseUrl: String)(implicit ec: ExecutionContext) {

  def status(taxId: TaxId)(implicit request: RequestHeader): Future[BarsVerifyStatusResponse] =
    httpClient.POST[BarsUpdateVerifyStatusParams, BarsVerifyStatusResponse](s"$baseUrl/essttp-backend/bars/verify/status", BarsUpdateVerifyStatusParams(taxId))

  def update(taxId: TaxId)(implicit request: RequestHeader): Future[BarsVerifyStatusResponse] =
    httpClient.POST[BarsUpdateVerifyStatusParams, BarsVerifyStatusResponse](s"$baseUrl/essttp-backend/bars/verify/update", BarsUpdateVerifyStatusParams(taxId))

  @Inject()
  def this(httpClient: HttpClient, servicesConfig: ServicesConfig)(implicit ec: ExecutionContext) = this(
    httpClient,
    servicesConfig.baseUrl("essttp-backend")
  )
}
