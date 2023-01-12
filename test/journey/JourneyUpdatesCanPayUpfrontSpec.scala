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

package journey

import essttp.journey.JourneyConnector
import essttp.journey.model.{CorrelationId, JourneyId, SjResponse}
import essttp.rootmodel.{AmountInPence, UpfrontPaymentAmount}
import play.api.mvc.Request
import testsupport.ItSpec
import testsupport.testdata.TdAll

class JourneyUpdatesCanPayUpfrontSpec extends ItSpec {
  def journeyConnector: JourneyConnector = app.injector.instanceOf[JourneyConnector]

  def putJourneyIntoEligibilityCheckedState(tdAll: TdAll)(implicit request: Request[_]): Unit = {
    journeyConnector.updateTaxId(tdAll.journeyId, tdAll.EpayeBta.updateTaxIdRequest()).futureValue
    journeyConnector.updateEligibilityCheckResult(tdAll.journeyId, tdAll.EpayeBta.updateEligibilityCheckRequest()).futureValue
    ()
  }

  "[Epaye.Bta][Update CanPayUpfront from yes to no to yes, round trip]" in {
    stubCommonActions()

    val tdAll = new TdAll {
      override val journeyId: JourneyId = journeyIdGenerator.readNextJourneyId()
      override val correlationId: CorrelationId = correlationIdGenerator.readNextCorrelationId()
    }

    implicit val request: Request[_] = tdAll.request
    val response: SjResponse = journeyConnector.Epaye.startJourneyBta(tdAll.EpayeBta.sjRequest).futureValue
    response shouldBe tdAll.EpayeBta.sjResponse
    putJourneyIntoEligibilityCheckedState(tdAll)

    /** Update CanPayUpfront as YES */
    journeyConnector.updateCanPayUpfront(tdAll.journeyId, tdAll.EpayeBta.updateCanPayUpfrontYesRequest()).futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterCanPayUpfrontYes

    /** Update CanPayUpfront as No */
    journeyConnector.updateCanPayUpfront(tdAll.journeyId, tdAll.EpayeBta.updateCanPayUpfrontNoRequest()).futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterCanPayUpfrontNo

    /** Update CanPayUpfront as YES */
    journeyConnector.updateCanPayUpfront(tdAll.journeyId, tdAll.EpayeBta.updateCanPayUpfrontYesRequest()).futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterCanPayUpfrontYes

    verifyCommonActions(numberOfAuthCalls = 9)
  }

  "[Epaye.Bta][CanPayUpfront.Yes, Update UpfrontPaymentAmount with value, then change to CanPayFrontNo]" in {
    stubCommonActions()

    val tdAll = new TdAll {
      override val journeyId: JourneyId = journeyIdGenerator.readNextJourneyId()
      override val correlationId: CorrelationId = correlationIdGenerator.readNextCorrelationId()
    }

    implicit val request: Request[_] = tdAll.request
    val response: SjResponse = journeyConnector.Epaye.startJourneyBta(tdAll.EpayeBta.sjRequest).futureValue
    response shouldBe tdAll.EpayeBta.sjResponse
    putJourneyIntoEligibilityCheckedState(tdAll)

    /** Update CanPayUpfront as YES */
    journeyConnector.updateCanPayUpfront(tdAll.journeyId, tdAll.EpayeBta.updateCanPayUpfrontYesRequest()).futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterCanPayUpfrontYes

    /** Update UpfrontPaymentAmount */
    journeyConnector.updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.EpayeBta.updateUpfrontPaymentAmountRequest()).futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterUpfrontPaymentAmount

    /** user decides not to pay upfront, Update CanPayUpfront as No */
    journeyConnector.updateCanPayUpfront(tdAll.journeyId, tdAll.EpayeBta.updateCanPayUpfrontNoRequest()).futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterCanPayUpfrontNo

    verifyCommonActions(numberOfAuthCalls = 9)
  }

  "[Epaye.Bta][Update UpfrontPaymentAmount with new value]" in {
    stubCommonActions()

    val tdAll = new TdAll {
      override val journeyId: JourneyId = journeyIdGenerator.readNextJourneyId()
      override val correlationId: CorrelationId = correlationIdGenerator.readNextCorrelationId()
    }

    implicit val request: Request[_] = tdAll.request
    val response: SjResponse = journeyConnector.Epaye.startJourneyBta(tdAll.EpayeBta.sjRequest).futureValue
    response shouldBe tdAll.EpayeBta.sjResponse
    putJourneyIntoEligibilityCheckedState(tdAll)

    journeyConnector.updateCanPayUpfront(tdAll.journeyId, tdAll.EpayeBta.updateCanPayUpfrontYesRequest()).futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterCanPayUpfrontYes

    /** Update UpfrontPaymentAmount */
    journeyConnector.updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.EpayeBta.updateUpfrontPaymentAmountRequest()).futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterUpfrontPaymentAmount

    /** Update UpfrontPaymentAmount with different value */
    journeyConnector.updateUpfrontPaymentAmount(tdAll.journeyId, tdAll.EpayeBta.anotherUpdateUpfrontPaymentAmountRequest()).futureValue
    journeyConnector.getJourney(tdAll.journeyId).futureValue shouldBe tdAll.EpayeBta.journeyAfterUpfrontPaymentAmount.copy(upfrontPaymentAmount = UpfrontPaymentAmount(AmountInPence(1001)))

    verifyCommonActions(numberOfAuthCalls = 9)
  }
}
