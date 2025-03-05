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

package services

import essttp.journey.model.{Journey, OriginatedSjRequest}
import essttp.rootmodel.SessionId

import java.time.{Clock, Instant}
import javax.inject.{Inject, Singleton}

@Singleton
class JourneyFactory @Inject() (
  journeyIdGenerator:          JourneyIdGenerator,
  correlationIdGenerator:      CorrelationIdGenerator,
  clock:                       Clock,
  affordabilityEnablerService: AffordabilityEnablerService
) {

  def makeJourney(
    originatedSjRequest: OriginatedSjRequest,
    sessionId:           SessionId
  ): Journey =
    Journey.Started(
      _id = journeyIdGenerator.nextJourneyId(),
      origin = originatedSjRequest.origin,
      sjRequest = originatedSjRequest.sjRequest,
      createdOn = Instant.now(clock),
      sessionId = sessionId,
      correlationId = correlationIdGenerator.nextCorrelationId(),
      affordabilityEnabled =
        Some(affordabilityEnablerService.affordabilityEnabled(originatedSjRequest.origin.taxRegime)),
      pegaCaseId = None
    )
}
