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

package bars

import cats.data.OptionT
import config.AppConfig
import essttp.bars.model._
import essttp.rootmodel.TaxId

import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BarsVerifyStatusService @Inject() (
    barsRepo:  BarsVerifyStatusRepo,
    appConfig: AppConfig
)(implicit ec: ExecutionContext) {

  /*
   * get current count of calls to verify endpoint for this taxId
   */
  def status(taxId: TaxId): Future[BarsVerifyStatusResponse] =
    find(taxId).map {
      case Some(barsStatus) => BarsVerifyStatusResponse(barsStatus)
      case None =>
        BarsVerifyStatusResponse(
          attempts              = 0,
          lockoutExpiryDateTime = None
        )
    }

  /*
   * increment the verify call count for this taxId
   * and if it exceeds maxAttempts then set the expiryDateTime field
   */
  def update(taxId: TaxId): Future[BarsVerifyStatusResponse] =
    OptionT[Future, BarsVerifyStatus](find(taxId))
      .fold(BarsVerifyStatus(taxId)) { status =>
        val newVerifyCalls = status.verifyCalls + 1
        val expiry: Option[Instant] =
          if (newVerifyCalls >= appConfig.barsVerifyMaxAttempts)
            Some(Instant.now.plus(24, ChronoUnit.HOURS))
          else None

        status.copy(
          verifyCalls           = newVerifyCalls,
          lastUpdated           = Instant.now,
          lockoutExpiryDateTime = expiry
        )
      }
      .flatMap(status => upsert(status).map(_ => BarsVerifyStatusResponse(status)))

  private def find(taxId: TaxId): Future[Option[BarsVerifyStatus]] =
    barsRepo.findById(TaxIdIndex(taxId))

  private def upsert(barsStatus: BarsVerifyStatus): Future[Unit] =
    barsRepo.upsert(barsStatus)

}
