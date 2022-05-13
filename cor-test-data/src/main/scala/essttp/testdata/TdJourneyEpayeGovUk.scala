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

package essttp.testdata

import essttp.journey.model._
import essttp.utils.JsonSyntax._
import essttp.utils.ResourceReader._
import play.api.libs.json.JsObject

import scala.language.reflectiveCalls

trait TdJourneyEpayeGovUk { dependencies: TdBase with TdEpaye =>

  object EpayeGovUk {

    def sjRequest = SjRequest.Epaye.Empty()

    def sjResponse = SjResponse(
      nextUrl   = NextUrl(s"http://localhost:9215/set-up-a-payment-plan?traceId=${dependencies.traceId.value}"),
      journeyId = dependencies.journeyId
    )

    def postPath: String = "/epaye/gov-uk/journey/start"
    def sjRequestJson: JsObject = read("testdata/epaye/govuk/SjRequest.json").asJson

    def journeyAfterStarted: Journey.Epaye.AfterStarted = Journey.Epaye.AfterStarted(
      _id       = dependencies.journeyId,
      origin    = Origins.Epaye.GovUk,
      createdOn = dependencies.createdOn,
      sjRequest = sjRequest,
      sessionId = dependencies.sessionId,
      stage     = Stage.AfterStarted.Started
    )

    def journeyAfterStartedJson: JsObject = read("testdata/epaye/govuk/JourneyAfterStarted.json").asJson
  }
}
