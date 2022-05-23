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

import essttp.journey.JourneyConnector
import essttp.journey.model.{JourneyId, SjResponse}
import essttp.testdata.TdAll
import play.api.mvc.Request
import testsupport.ItSpec

class JourneyControllerSpec extends ItSpec {

  def journeyConnector: JourneyConnector = app.injector.instanceOf[JourneyConnector]

  private val testNameJourneyStages: String =
    "[StartJourney]" +
      "[UpdateTaxId]" +
      "[UpdateEligibilityCheck]" +
      "[UpdateCanPayUpfront]" +
      "[UpdateUpfrontPaymentAmount]"

  s"[Epaye.Bta][Happy path with upfront payment]$testNameJourneyStages" in {
    val tdAll = new TdAll {
      override val journeyId: JourneyId = journeyIdGenerator.readNextJourneyId()
    }

    implicit val request: Request[_] = tdAll.request
    val response: SjResponse = journeyConnector.Epaye.startJourneyBta(tdAll.EpayeBta.sjRequest).futureValue

    /** Start journey */
    response shouldBe tdAll.EpayeBta.sjResponse
    journeyConnector.getJourney(response.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterStarted

    /** Update tax id */
    journeyConnector.updateTaxId(tdAll.journeyId, tdAll.EpayeBta.updateTaxIdRequest()).futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterDetermineTaxIds

    /** Update eligibility result */
    journeyConnector.updateEligibilityCheckResult(tdAll.journeyId, tdAll.EpayeBta.updateEligibilityCheckRequest()).futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterEligibilityCheckEligible

    /** Update CanPayUpfront */
    journeyConnector.updateCanPayUpfront(tdAll.journeyId, tdAll.EpayeBta.updateCanPayUpfrontYesRequest()).futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterCanPayUpfrontYes

    /** Update UpfrontPaymentAmount */
    journeyConnector.updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.EpayeBta.updateUpfrontPaymentAmountRequest()).futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterUpfrontPaymentAmount
  }

  s"[Epaye.GovUk][Happy path with upfront payment]$testNameJourneyStages" in {
    val tdAll = new TdAll {
      override val journeyId: JourneyId = journeyIdGenerator.readNextJourneyId()
    }

    implicit val request: Request[_] = tdAll.request
    val response: SjResponse = journeyConnector.Epaye.startJourneyGovUk(tdAll.EpayeGovUk.sjRequest).futureValue

    /** Start journey * */
    response shouldBe tdAll.EpayeGovUk.sjResponse
    journeyConnector.getJourney(response.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterStarted

    /** Update tax id * */
    journeyConnector.updateTaxId(tdAll.journeyId, tdAll.EpayeGovUk.updateTaxIdRequest()).futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterDetermineTaxIds

    /** Update eligibility result * */
    journeyConnector.updateEligibilityCheckResult(tdAll.journeyId, tdAll.EpayeGovUk.updateEligibilityCheckRequest()).futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterEligibilityCheckEligible

    /** Update CanPayUpfront * */
    journeyConnector.updateCanPayUpfront(tdAll.journeyId, tdAll.EpayeGovUk.updateCanPayUpfrontYesRequest()).futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterCanPayUpfrontYes

    /** Update UpfrontPaymentAmount */
    journeyConnector.updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.EpayeGovUk.updateUpfrontPaymentAmountRequest()).futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeGovUk.journeyAfterUpfrontPaymentAmount
  }

  s"[Epaye.DetachedUrl][Happy path with upfront payment]$testNameJourneyStages" in {
    val tdAll = new TdAll {
      override val journeyId: JourneyId = journeyIdGenerator.readNextJourneyId()
    }

    implicit val request: Request[_] = tdAll.request
    val response: SjResponse = journeyConnector.Epaye.startJourneyDetachedUrl(tdAll.EpayeDetachedUrl.sjRequest).futureValue

    /** Start journey * */
    response shouldBe tdAll.EpayeDetachedUrl.sjResponse
    journeyConnector.getJourney(response.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterStarted

    /** Update tax id * */
    journeyConnector.updateTaxId(tdAll.journeyId, tdAll.EpayeDetachedUrl.updateTaxIdRequest()).futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterDetermineTaxIds

    /** Update eligibility result * */
    journeyConnector.updateEligibilityCheckResult(tdAll.journeyId, tdAll.EpayeDetachedUrl.updateEligibilityCheckRequest()).futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterEligibilityCheckEligible

    /** Update CanPayUpfront * */
    journeyConnector.updateCanPayUpfront(tdAll.journeyId, tdAll.EpayeDetachedUrl.updateCanPayUpfrontYesRequest()).futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterCanPayUpfrontYes

    /** Update UpfrontPaymentAmount */
    journeyConnector.updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.EpayeDetachedUrl.updateUpfrontPaymentAmountRequest()).futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeDetachedUrl.journeyAfterUpfrontPaymentAmount
  }
}
