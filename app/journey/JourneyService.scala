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

import journey.model.{Journey, JourneyId}
import play.api.mvc.Request
import repository.RepoResultChecker._

import java.time.Clock
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class JourneyService @Inject() (
    journeyRepo: JourneyRepo,
    clock:       Clock
)(implicit ec: ExecutionContext) {

  def get()(implicit request: Request[_]): Future[Journey] = {
    find().map { maybeJourney =>
      maybeJourney.getOrElse(throw new RuntimeException(s"Expected journey to be found ${request.path}"))
    }
  }

  private def find()(implicit request: Request[_]): Future[Option[Journey]] = {
    val journeyId: JourneyId =
      request
        .session
        .data
        .get("JourneyId")
        .map(JourneyId.apply)
        .getOrElse(throw new RuntimeException(s"JourneyId not present in request session ${request.path}"))

    journeyRepo.findById(journeyId)
  }

  def upsert(journey: Journey)(implicit request: Request[_]): Future[Unit] =
    journeyRepo
      .upsert(journey._id, journey)
      .checkResult
}
