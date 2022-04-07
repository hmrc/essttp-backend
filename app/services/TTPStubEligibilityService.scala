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

package services

import cats.data.EitherT
import connectors.EligibilityStubConnector
import essttp.rootmodel._
import model.OverduePayments
import services.EligibilityService.{GenericError, ServiceError}
import services.TTPStubEligibilityService.liftError
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TTPStubEligibilityService @Inject() (connector: EligibilityStubConnector) extends EligibilityService {

  override def eligibilityData(regime: TaxRegime, id: TaxId)(implicit hc: HeaderCarrier, ec: ExecutionContext): EitherT[Future, ServiceError, OverduePayments] = for {
    d <- connector.eligibilityData(regime, id).leftMap(liftError)
  } yield d

}

object TTPStubEligibilityService {
  def liftError(e: EligibilityStubConnector.ServiceError): EligibilityService.ServiceError = GenericError(e.message)
}
