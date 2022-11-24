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

package testsupport

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.verification.LoggedRequest
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatest._
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{JsValue, Json}

import scala.jdk.CollectionConverters._
import scala.concurrent.ExecutionContext

trait RichMatchers
  extends Matchers
  with TryValues
  with EitherValues
  with OptionValues
  with AppendedClues
  with ScalaFutures
  with StreamlinedXml
  with Inside
  with Eventually
  with IntegrationPatience
  with JsonSyntax {

  implicit lazy val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  @SuppressWarnings(Array("org.wartremover.warts.ExplicitImplicitTypes", "org.wartremover.warts.PublicInference"))
  implicit def toLoggedRequestOps(lr: LoggedRequest) = new {
    def getBodyAsJson: JsValue = Json.parse(lr.getBodyAsString)
  }

  /**
   * Returns recorded by WireMock request.
   * Asserts there was only one request made to wire mock.
   * Use it in Connector unit tests.
   */
  def getRecordedRequest(): LoggedRequest = {
    val allRecordedRequests = WireMock.getAllServeEvents().asScala.map(_.getRequest)
    allRecordedRequests.toList match {
      case r :: Nil => r
      case _        => fail("there suppose to be only one request recorded")
    }
  }

  def assertThereWasOnlyOneRequest(): LoggedRequest = getRecordedRequest()

}
