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
import essttp.utils.RequestSupport.hc
import play.api.libs.json.Json
import play.api.libs.ws.writeableOf_JsValue
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.StringContextOps
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BarsVerifyStatusConnector(httpClient: HttpClientV2, baseUrl: String)(using ExecutionContext) {

  def status(taxId: TaxId)(using RequestHeader): Future[BarsVerifyStatusResponse] =
    httpClient
      .post(url"$baseUrl/essttp-backend/bars/verify/status")
      .withBody(Json.toJson(BarsUpdateVerifyStatusParams(taxId)))
      .execute[BarsVerifyStatusResponse]

  def update(taxId: TaxId)(using RequestHeader): Future[BarsVerifyStatusResponse] =
    httpClient
      .post(url"$baseUrl/essttp-backend/bars/verify/update")
      .withBody(Json.toJson(BarsUpdateVerifyStatusParams(taxId)))
      .execute[BarsVerifyStatusResponse]

  @Inject()
  def this(httpClient: HttpClientV2, servicesConfig: ServicesConfig)(using ExecutionContext) = this(
    httpClient,
    servicesConfig.baseUrl("essttp-backend")
  )
}
