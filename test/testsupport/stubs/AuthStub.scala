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
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, stubFor, urlPathEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.{AuthProviders, AuthenticateHeaderParser}

object AuthStub {

  private val authoriseUrl: String = "/auth/authorise"

  def authorise(): StubMapping =
    stubFor(
      post(urlPathEqualTo(authoriseUrl))
        .willReturn(aResponse().withStatus(200).withBody("{}"))
    )

  def authoriseError(authorisationExceptionString: String): StubMapping =
    stubFor(
      post(
        urlPathEqualTo(authoriseUrl)
      )
        .willReturn(
          aResponse()
            .withStatus(401)
            .withHeader(AuthenticateHeaderParser.WWW_AUTHENTICATE, s"""MDTP detail="$authorisationExceptionString"""")
        )
    )

  def ensureAuthoriseCalled(numberOfAuthCalls: Int, authProviders: AuthProviders = AuthProviders(GovernmentGateway)) =
    verify(
      exactly(numberOfAuthCalls),
      postRequestedFor(urlPathEqualTo(authoriseUrl))
        .withRequestBody(
          equalToJson(
            s"""{
              |  "authorise": [
              |    ${authProviders.toJson.toString()}
              |  ],
              |  "retrieve": [ ]
              |}
              |""".stripMargin
          )
        )
    )

}
