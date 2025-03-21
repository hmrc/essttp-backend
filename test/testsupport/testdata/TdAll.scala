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

package testsupport.testdata

import testsupport.testdata.epaye._
import testsupport.testdata.sa._
import testsupport.testdata.simp.{TdJourneySimpDetachedUrl, TdJourneySimpGovUk, TdJourneySimpMobile, TdJourneySimpPta, TdSimp}
import testsupport.testdata.vat._

/** Test Data (Td) all test instances
  */
object TdAll extends TdAll

/** Test Data (Td) all test instances
  *
  * Override what you need.
  */
trait TdAll
    extends AnyRef
    with TdBase
    with TdEpaye
    with TdJourneyEpayeBta
    with TdJourneyEpayeGovUk
    with TdJourneyEpayeDetachedUrl
    with TdJourneyEpayeEpayeService
    with TdVat
    with TdJourneyVatBta
    with TdJourneyVatGovUk
    with TdJourneyVatDetachedUrl
    with TdJourneyVatVatService
    with TdJourneyVatVatPenalties
    with TdSa
    with TdJourneySaBta
    with TdJourneySaPta
    with TdJourneySaMobile
    with TdJourneySaGovUk
    with TdJourneySaDetachedUrl
    with TdJourneySaItsaViewAndChange
    with TdSimp
    with TdJourneySimpPta
    with TdJourneySimpMobile
    with TdJourneySimpGovUk
    with TdJourneySimpDetachedUrl
