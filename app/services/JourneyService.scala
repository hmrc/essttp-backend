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

import essttp.journey.model.{Journey, JourneyId}
import essttp.rootmodel.SessionId
import journey.JourneyLogger
import play.api.mvc.Request
import repository.JourneyRepo

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class JourneyService @Inject() (
  journeyRepo: JourneyRepo
)(using ExecutionContext) {

  def findLatestJourney(sessionId: SessionId): Future[Option[Journey]] =
    journeyRepo.findLatestJourney(sessionId)

  def get(journeyId: JourneyId)(using request: Request[?]): Future[Journey] =
    find(journeyId).map { maybeJourney =>
      maybeJourney.getOrElse(throw new RuntimeException(s"Expected journey to be found ${request.path}"))
    }

  private def find(journeyId: JourneyId): Future[Option[Journey]] =
    journeyRepo.findById(journeyId)

  def upsert(journey: Journey)(using Request[?]): Future[Journey] = {
    JourneyLogger.debug("Upserting new journey")
    journeyRepo.upsert(journey).map(_ => journey)

  }
}
