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

import essttp.journey.model.{Journey, JourneyId}
import essttp.rootmodel.SessionId
import play.api.mvc.Request
import repository.RepoResultChecker._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class JourneyService @Inject() (
    journeyRepo: JourneyRepo
)(implicit ec: ExecutionContext) {

  def findLatestJourney(sessionId: SessionId): Future[Option[Journey]] = {
    journeyRepo.findLatestJourney(sessionId)
  }

  def get(journeyId: JourneyId)(implicit request: Request[_]): Future[Journey] = {
    find(journeyId).map { maybeJourney =>
      maybeJourney.getOrElse(throw new RuntimeException(s"Expected journey to be found ${request.path}"))
    }
  }

  private def find(journeyId: JourneyId): Future[Option[Journey]] = {
    journeyRepo.findById(journeyId)
  }

  def upsert(journey: Journey)(implicit request: Request[_]): Future[Unit] = {
    JourneyLogger.debug("Upserting new journey")
    journeyRepo
      .upsert(journey._id, journey)
      .checkResult
  }
}
