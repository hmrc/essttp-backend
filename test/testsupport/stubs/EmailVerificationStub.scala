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

package testsupport.stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import essttp.emailverification.StartEmailVerificationJourneyRequest
import essttp.rootmodel.GGCredId
import models.emailverification.EmailVerificationResultResponse.EmailResult
import models.emailverification.RequestEmailVerificationSuccess
import play.api.http.Status.{CREATED, OK}
import play.api.libs.json.Json

object EmailVerificationStub {

  private val requestVerificationUrl: String = "/email-verification/verify-email"

  private def getVerificationStatusUrl(ggCredId: GGCredId): String = s"/email-verification/verification-status/${ggCredId.value}"

  type HttpStatus = Int

  def requestEmailVerification(result: Either[HttpStatus, RequestEmailVerificationSuccess]): StubMapping =
    stubFor(
      post(urlPathEqualTo(requestVerificationUrl))
        .willReturn{
          result.fold(
            status => aResponse().withStatus(status),
            { success =>
              val body = Json.parse(s"""{ "redirectUri": "${success.redirectUri}" }""")
              aResponse().withStatus(CREATED).withBody(Json.prettyPrint(body))
            }
          )
        }
    )

  def verifyRequestEmailVerification(
      request: StartEmailVerificationJourneyRequest
  ): Unit =
    verify(
      exactly(1),
      postRequestedFor(urlPathEqualTo(requestVerificationUrl))
        .withRequestBody(
          equalToJson(
            s"""{
               |  "credId": "${request.credId.value}",
               |  "continueUrl": "${request.continueUrl}",
               |  "origin": "${request.origin}",
               |  "deskproServiceName": "${request.deskproServiceName}",
               |  "accessibilityStatementUrl": "${request.accessibilityStatementUrl}",
               |  "pageTitle": "${request.pageTitle}",
               |  "backUrl": "${request.backUrl}",
               |  "email": {
               |      "address": "${request.email.value.decryptedValue}",
               |      "enterUrl": "${request.enterEmailUrl}"
               |  },
               |  "lang":"${request.lang}"
               |}
               |""".stripMargin
          )
        )
    )

  def getVerificationResult(ggCredId: GGCredId, result: Either[HttpStatus, List[EmailResult]]): StubMapping =
    stubFor(
      get(urlPathEqualTo(getVerificationStatusUrl(ggCredId)))
        .willReturn{
          result.fold(
            status => aResponse().withStatus(status),
            { success =>
              val emailsJson = success.map(emailStatus =>
                s"""{
                   |  "emailAddress": "${emailStatus.emailAddress}",
                   |  "verified": ${emailStatus.verified.toString},
                   |  "locked": ${emailStatus.locked.toString}
                   |}
                   |""".stripMargin)
              val body = Json.parse(
                s"""
                   |{
                   |  "emails": [ ${emailsJson.mkString(",")} ]
                   |}
                   |""".stripMargin
              )
              aResponse().withStatus(OK).withBody(Json.prettyPrint(body))
            }
          )
        }
    )

  def verifyNoneRequestVerification(): Unit =
    verify(exactly(0), postRequestedFor(urlPathEqualTo(requestVerificationUrl)))

  def verifyNoneGetVerificationStatus(ggCredId: GGCredId): Unit =
    verify(exactly(0), postRequestedFor(urlPathEqualTo(getVerificationStatusUrl(ggCredId))))

}
