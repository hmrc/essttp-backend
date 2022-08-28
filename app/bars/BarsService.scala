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
import essttp.bars.model.{BarsUpdateStatusParams, BarsVerifyStatus, BarsVerifyStatusResponse, TaxIdIndex}
import essttp.rootmodel.TaxId

import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BarsService @Inject() (
    barsRepo: BarsRepo
)(implicit ec: ExecutionContext) {

  /*
   * get current count of calls to verify endpoint for this taxId
   */
  def status(id: TaxId): Future[BarsVerifyStatusResponse] =
    find(id).map {
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
  def update(params: BarsUpdateStatusParams): Future[Unit] =
    OptionT[Future, BarsVerifyStatus](find(params.taxId))
      .fold(BarsVerifyStatus(params.taxId)) { status =>
        val expiry: Option[Instant] =
          if (status.verifyCalls >= params.maxAttempts)
            Some(Instant.now.plus(24, ChronoUnit.HOURS))
          else None

        status.copy(
          verifyCalls           = status.verifyCalls + 1,
          lastUpdated           = Instant.now,
          lockoutExpiryDateTime = expiry
        )
      }
      .flatMap(upsert)

  private def find(taxId: TaxId): Future[Option[BarsVerifyStatus]] =
    barsRepo.findById(TaxIdIndex(taxId))

  def upsert(barsStatus: BarsVerifyStatus): Future[Unit] =
    barsRepo.upsert(barsStatus)

}
