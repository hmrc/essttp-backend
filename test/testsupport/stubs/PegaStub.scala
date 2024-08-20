/*
 * Copyright 2024 HM Revenue & Customs
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

package testsupport.stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import essttp.rootmodel.pega.PegaCaseId
import models.pega.{PegaGetCaseResponse, PegaOauthToken, PegaStartCaseResponse}
import play.api.libs.json.Json

import java.util.Base64

object PegaStub {

  type HttpStatus = Int

  private val oauthUrlPath = "/prweb/PRRestService/oauth2/v1/token"

  private val startCaseUrlPath = "/prweb/api/payments/v1/aa/createorupdatecase"

  private def getCaseUrlPath(caseId: PegaCaseId) = s"/prweb/api/payments/v1/aa/getcase/${caseId.value}"

  def stubOauthToken(result: Either[HttpStatus, PegaOauthToken]): StubMapping =
    stubFor(
      post(urlPathEqualTo(oauthUrlPath))
        .willReturn(
          result.fold(
            aResponse().withStatus(_),
            token => aResponse().withStatus(200).withBody(
              s"""{
                 |  "access_token": "${token.accessToken}",
                 |  "token_type": "${token.tokenType}",
                 |  "expires_in": ${token.expiresIn.toString}
                 |}
                 |""".stripMargin
            )
          )
        )
    )

  def stubStartCase(result: Either[HttpStatus, PegaStartCaseResponse]): StubMapping =
    stubFor(
      post(urlPathEqualTo(startCaseUrlPath))
        .willReturn(
          result.fold(
            aResponse().withStatus(_),
            response => {
              val assignments =
                response.data.caseInfo.assignments
                  .map(a => s"""{ "ID": "${a.ID}" }""")
                  .mkString(",")

              aResponse().withStatus(200).withBody(
                s"""{
                   |  "ID": "${response.ID}",
                   |  "data": {
                   |    "caseInfo": {
                   |      "assignments": [ $assignments ]
                   |    }
                   |  }
                   |}""".stripMargin
              )
            }
          )
        )
    )

  def stubGetCase(caseId: PegaCaseId, result: Either[HttpStatus, PegaGetCaseResponse]): StubMapping =
    stubFor(
      get(urlPathEqualTo(getCaseUrlPath(caseId)))
        .willReturn(
          result.fold(
            aResponse().withStatus(_),
            response => {
              aResponse().withStatus(200).withBody(Json.toJson(response).toString)
            }
          )
        )
    )

  def verifyOauthCalled(username: String, password: String): Unit = {
    val expectedEncodedCreds =
      new String(Base64.getEncoder.encode(s"$username:$password".getBytes("UTF-8")), "UTF-8")

    verify(
      exactly(1),
      postRequestedFor(urlPathEqualTo(oauthUrlPath))
        .withRequestBody(equalTo("grant_type=client_credentials"))
        .withHeader("Authorization", equalTo(s"Basic $expectedEncodedCreds"))
        .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
    )
  }

  def verifyStartCaseCalled(pegaOauthToken: PegaOauthToken, expectedRequestJson: String): Unit =
    verify(
      exactly(1),
      postRequestedFor(urlPathEqualTo(startCaseUrlPath))
        .withRequestBody(equalToJson(expectedRequestJson))
        .withHeader("Authorization", equalTo(s"Bearer ${pegaOauthToken.accessToken}"))
    )

  def verifyGetCaseCalled(pegaOauthToken: PegaOauthToken, caseId: PegaCaseId): Unit =
    verify(
      exactly(1),
      getRequestedFor(urlPathEqualTo(getCaseUrlPath(caseId)))
        .withHeader("Authorization", equalTo(s"Bearer ${pegaOauthToken.accessToken}"))
    )

}
