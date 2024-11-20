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

import essttp.journey.model.{Journey, OriginatedSjRequest, Stage}
import essttp.rootmodel.{SessionId, TaxRegime}

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
  ): Journey = originatedSjRequest match {

    case OriginatedSjRequest.Epaye(origin, sjRequest) =>
      Journey.Epaye.Started(
        _id                  = journeyIdGenerator.nextJourneyId(),
        origin               = origin,
        sjRequest            = sjRequest,
        createdOn            = Instant.now(clock),
        sessionId            = sessionId,
        stage                = Stage.AfterStarted.Started,
        correlationId        = correlationIdGenerator.nextCorrelationId(),
        affordabilityEnabled = Some(affordabilityEnablerService.affordabilityEnabled(TaxRegime.Epaye)),
        pegaCaseId           = None
      )

    case OriginatedSjRequest.Vat(origin, sjRequest) =>
      Journey.Vat.Started(
        _id                  = journeyIdGenerator.nextJourneyId(),
        origin               = origin,
        sjRequest            = sjRequest,
        createdOn            = Instant.now(clock),
        sessionId            = sessionId,
        stage                = Stage.AfterStarted.Started,
        correlationId        = correlationIdGenerator.nextCorrelationId(),
        affordabilityEnabled = Some(affordabilityEnablerService.affordabilityEnabled(TaxRegime.Vat)),
        pegaCaseId           = None
      )

    case OriginatedSjRequest.Sa(origin, sjRequest) =>
      Journey.Sa.Started(
        _id                  = journeyIdGenerator.nextJourneyId(),
        origin               = origin,
        sjRequest            = sjRequest,
        createdOn            = Instant.now(clock),
        sessionId            = sessionId,
        stage                = Stage.AfterStarted.Started,
        correlationId        = correlationIdGenerator.nextCorrelationId(),
        affordabilityEnabled = Some(affordabilityEnablerService.affordabilityEnabled(TaxRegime.Sa)),
        pegaCaseId           = None
      )

    case OriginatedSjRequest.Sia(origin, sjRequest) =>
      Journey.Sia.Started(
        _id                  = journeyIdGenerator.nextJourneyId(),
        origin               = origin,
        sjRequest            = sjRequest,
        createdOn            = Instant.now(clock),
        sessionId            = sessionId,
        stage                = Stage.AfterStarted.Started,
        correlationId        = correlationIdGenerator.nextCorrelationId(),
        affordabilityEnabled = Some(affordabilityEnablerService.affordabilityEnabled(TaxRegime.Sia)),
        pegaCaseId           = None
      )
  }
}
