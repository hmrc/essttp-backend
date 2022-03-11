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

import config.AppConfig
import journey.model.{Journey, JourneyId}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.indexes._
import reactivemongo.bson.BSONDocument
import repository.Repo

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{DurationInt, FiniteDuration}

@Singleton
final class JourneyRepo @Inject() (
                                    reactiveMongoComponent: ReactiveMongoComponent,
                                    config: AppConfig)(implicit ec: ExecutionContext)
  extends Repo[Journey, JourneyId]("journey", reactiveMongoComponent) {

  override def indexes: Seq[Index] = Seq(
    Index(
      key = Seq("lastUpdated1" -> IndexType.Ascending),
      name = Some("lastUpdatedIdx1"),
      options = BSONDocument("expireAfterSeconds" -> journeyTtl.toSeconds)
    )
  )

  private val journeyTtl: FiniteDuration = 90.days
}