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

package journey

import essttp.journey.model.{Journey, OriginatedSjRequest, Stage}
import essttp.rootmodel.SessionId

import java.time.{Clock, LocalDateTime}
import javax.inject.{Inject, Singleton}

@Singleton
class JourneyFactory @Inject() (
    journeyIdGenerator: JourneyIdGenerator,
    clock:              Clock
) {

  def makeJourney(
      originatedSjRequest: OriginatedSjRequest,
      sessionId:           SessionId
  ): Journey = originatedSjRequest match {

    case OriginatedSjRequest.Epaye(origin, sjRequest) =>
      Journey.Epaye.AfterStarted(
        _id       = journeyIdGenerator.nextJourneyId(),
        origin    = origin,
        sjRequest = sjRequest,
        createdOn = LocalDateTime.now(clock),
        sessionId = sessionId,
        stage     = Stage.AfterStarted.New
      )
  }
}
