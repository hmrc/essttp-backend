/*
 * Copyright 2024 HM Revenue & Customs
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

package repository

import config.AppConfig
import essttp.crypto.CryptoFormat
import essttp.crypto.CryptoFormat.OperationalCryptoFormat
import essttp.journey.model.Journey
import essttp.rootmodel.TaxId
import org.mongodb.scala.model.{IndexModel, IndexOptions, Indexes}
import play.api.libs.json.{Json, OFormat}
import repository.JourneyByTaxIdRepo._
import repository.Repo.{Id, IdExtractor}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.Codecs
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats.Implicits.jatInstantFormat

import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

/**
 * Journeys to be stored for slightly longer than a session. Facilitates recreating
 * session data when coming back from PEGA
 */
class JourneyByTaxIdRepo @Inject() (
    mongoComponent: MongoComponent,
    config:         AppConfig
)(implicit ec: ExecutionContext, cryptoFormat: OperationalCryptoFormat)
  extends Repo[TaxId, JourneyWithTaxId](
    collectionName = "journeyByTaxId",
    mongoComponent = mongoComponent,
    indexes        = JourneyByTaxIdRepo.indexes(config.journeyByTaxIdRepoTtl),
    extraCodecs    = Codecs.playFormatSumCodecs(Journey.format),
    replaceIndexes = true
  ) {

}

object JourneyByTaxIdRepo {

  final case class JourneyWithTaxId(taxId: TaxId, journey: Journey, lastUpdated: Instant)

  object JourneyWithTaxId {

    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    implicit def format(implicit cryptoFormat: CryptoFormat): OFormat[JourneyWithTaxId] =
      Json.format
  }

  implicit val journeyId: Id[TaxId] = new Id[TaxId] {
    override def value(i: TaxId): String = i.value
  }

  implicit val journeyIdExtractor: IdExtractor[JourneyWithTaxId, TaxId] = new IdExtractor[JourneyWithTaxId, TaxId] {
    override def id(j: JourneyWithTaxId): TaxId = j.taxId
  }

  def indexes(cacheTtl: FiniteDuration): Seq[IndexModel] = Seq(
    IndexModel(
      keys         = Indexes.ascending("lastUpdated"),
      indexOptions = IndexOptions().expireAfter(cacheTtl.toSeconds, TimeUnit.SECONDS).name("lastUpdatedIdx")
    ),
    IndexModel(
      keys = Indexes.ascending("taxId.value")
    )
  )

}
