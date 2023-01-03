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

package bars

import bars.BarsVerifyStatusRepo._
import config.AppConfig
import essttp.bars.model.{BarsVerifyStatus, TaxIdIndex}
import org.mongodb.scala.model.{IndexModel, IndexOptions, Indexes}
import repository.Repo
import repository.Repo.{Id, IdExtractor}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.Codecs

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
final class BarsVerifyStatusRepo @Inject() (
    mongoComponent: MongoComponent,
    config:         AppConfig
)(implicit ec: ExecutionContext)
  extends Repo[TaxIdIndex, BarsVerifyStatus](
    collectionName = "bars",
    mongoComponent = mongoComponent,
    indexes        = BarsVerifyStatusRepo.indexes(config.barsVerifyRepoTtl.toSeconds),
    extraCodecs    = Codecs.playFormatCodecsBuilder(BarsVerifyStatus.format).build,
    replaceIndexes = true
  )

object BarsVerifyStatusRepo {
  implicit val taxId: Id[TaxIdIndex] = (i: TaxIdIndex) => i.value
  implicit val taxIdExtractor: IdExtractor[BarsVerifyStatus, TaxIdIndex] = (b: BarsVerifyStatus) => b._id

  def indexes(cacheTtlInSeconds: Long): Seq[IndexModel] = Seq(
    IndexModel(
      keys         = Indexes.ascending("lastUpdated"),
      indexOptions = IndexOptions().expireAfter(cacheTtlInSeconds, TimeUnit.SECONDS).name("lastUpdatedIdx")
    )
  )
}

