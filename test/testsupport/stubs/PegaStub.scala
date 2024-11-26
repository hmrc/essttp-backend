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
import models.pega.{PegaOauthToken, PegaStartCaseResponse}

import java.util.Base64
import scala.jdk.CollectionConverters._

object PegaStub {

  type HttpStatus = Int

  private val oauthUrlPath = "/prweb/PRRestService/oauth2/v1/token"

  private val startCaseUrlPath = "/prweb/api/payments/v1/aa/createorupdatecase"

  private def getCaseUrlPath(caseId: PegaCaseId) =
    s"/prweb/api/payments/v1/cases/${caseId.value}"

  val getCaseRequestQueryParams = Map(
    "viewType" -> equalTo("none"),
    "pageName" -> equalTo("GetCaseDetailsWrapper"),
    "getBusinessDataOnly" -> equalTo("true")
  ).asJava

  val correlationIdRegex = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$".r

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

  def stubStartCase(result: Either[HttpStatus, PegaStartCaseResponse], expiredToken: Boolean = false): StubMapping = {
    if (expiredToken) {
      val scenarioName = "StartCaseScenario"
      val initialState = "Initial"
      val failedState = "Failed"

      stubFor(post(urlPathEqualTo(startCaseUrlPath))
        .inScenario(scenarioName)
        .whenScenarioStateIs(initialState)
        .willReturn(aResponse().withStatus(401))
        .willSetStateTo(failedState))

      stubFor(post(urlPathEqualTo(startCaseUrlPath))
        .inScenario(scenarioName)
        .whenScenarioStateIs(failedState)
        .willReturn(
          result.fold(
            aResponse().withStatus(_),
            response => aResponse().withStatus(200).withBody(generateResponseBody(response))
          )
        ))
    } else {
      stubFor(
        post(urlPathEqualTo(startCaseUrlPath))
          .willReturn(
            result.fold(
              aResponse().withStatus(_),
              response => aResponse().withStatus(200).withBody(generateResponseBody(response))
            )
          )
      )
    }
  }

  private def generateResponseBody(response: PegaStartCaseResponse): String =
    s"""{
       |  "ID": "${response.ID}"
       |}""".stripMargin

  def stubGetCase(
      caseId:       PegaCaseId,
      result:       Either[HttpStatus, String],
      expiredToken: Boolean                    = false
  ): StubMapping = {
    if (expiredToken) {
      val scenarioName = "GetCaseScenario"
      val initialState = "Started"
      val failedState = "FirstFail"

      stubFor(get(urlPathEqualTo(getCaseUrlPath(caseId)))
        .withQueryParams(getCaseRequestQueryParams)
        .inScenario(scenarioName)
        .whenScenarioStateIs(initialState)
        .willReturn(aResponse().withStatus(401))
        .willSetStateTo(failedState))

      stubFor(get(urlPathEqualTo(getCaseUrlPath(caseId)))
        .withQueryParams(getCaseRequestQueryParams)
        .inScenario(scenarioName)
        .whenScenarioStateIs(failedState)
        .willReturn(
          result.fold(
            aResponse().withStatus(_),
            response => {
              aResponse().withStatus(200).withBody(response)
            }
          )
        ))
    } else {
      stubFor(
        get(urlPathEqualTo(getCaseUrlPath(caseId)))
          .withQueryParams(getCaseRequestQueryParams)
          .willReturn(
            result.fold(
              aResponse().withStatus(_),
              response => {
                aResponse().withStatus(200).withBody(response)
              }
            )
          )
      )
    }
  }

  def verifyOauthCalled(username: String, password: String, numberOfTimes: Int = 1): Unit = {
    val expectedEncodedCreds =
      new String(Base64.getEncoder.encode(s"$username:$password".getBytes("UTF-8")), "UTF-8")

    verify(
      exactly(numberOfTimes),
      postRequestedFor(urlPathEqualTo(oauthUrlPath))
        .withRequestBody(equalTo("grant_type=client_credentials"))
        .withHeader("Authorization", equalTo(s"Basic $expectedEncodedCreds"))
        .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
    )
  }

  def verifyStartCaseCalled(pegaOauthToken: PegaOauthToken, expectedRequestJson: String, numberOfTimes: Int = 1): Unit =
    verify(
      exactly(numberOfTimes),
      postRequestedFor(urlPathEqualTo(startCaseUrlPath))
        .withRequestBody(equalToJson(expectedRequestJson))
        .withHeader("Authorization", equalTo(s"Bearer ${pegaOauthToken.accessToken}"))
        .withHeader("correlationid", matching(correlationIdRegex.regex))
    )

  def verifyGetCaseCalled(pegaOauthToken: PegaOauthToken, caseId: PegaCaseId, numberOfTimes: Int = 1): Unit =
    verify(
      exactly(numberOfTimes),
      getRequestedFor(urlPathEqualTo(getCaseUrlPath(caseId)))
        .withHeader("Authorization", equalTo(s"Bearer ${pegaOauthToken.accessToken}"))
        .withHeader("correlationid", matching(correlationIdRegex.regex))
    )

}
