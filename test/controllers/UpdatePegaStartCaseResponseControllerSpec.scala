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

package controllers

import essttp.journey.model.Journey
import essttp.rootmodel.pega.{PegaCaseId, StartCaseResponse}
import testsupport.ItSpec
import testsupport.testdata.TdAll

class UpdatePegaStartCaseResponseControllerSpec extends ItSpec with UpdateJourneyControllerSpec {

  "POST /journey/:journeyId/update-pega-start-case-response" - {
    "should throw Bad Request when Journey is in a stage" - {

      "[BeforeObtainedCanPayWithinSixMonths]" in new JourneyItTest {
        stubCommonActions()

        journeyConnector.Epaye.startJourneyBta(TdAll.EpayeBta.sjRequest).futureValue
        val result: Throwable = journeyConnector.updatePegaStartCaseResponse(tdAll.journeyId, tdAll.startCaseResponse).failed.futureValue
        result.getMessage should include("""{"statusCode":400,"message":"UpdatePegaStartCaseResponse update is not possible in that state: [Started]"}""")

        verifyCommonActions(numberOfAuthCalls = 2)
      }

      "[AfterEnteredMonthlyPaymentAmount]" in new JourneyItTest {
        insertJourneyForTest(tdAll.EpayeBta.journeyAfterMonthlyPaymentAmount)
        stubCommonActions()

        val result: Throwable = journeyConnector.updatePegaStartCaseResponse(tdAll.journeyId, tdAll.startCaseResponse).failed.futureValue
        result.getMessage should include("""{"statusCode":400,"message":"update PEGA start case response not expected after entered monthly payment amount"}""")

        verifyCommonActions(numberOfAuthCalls = 1)
      }
    }

    "should update the journey when an existing value didn't exist before for" - {

      "Epaye" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.EpayeBta.journeyAfterCanPayWithinSixMonthsNotRequired,
          tdAll.startCaseResponse
        )(
            journeyConnector.updatePegaStartCaseResponse,
            tdAll.EpayeBta.journeyAfterStartedPegaCase
          )(this)
      }

      "Vat" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.VatBta.journeyAfterCanPayWithinSixMonthsNotRequired,
          tdAll.startCaseResponse
        )(
            journeyConnector.updatePegaStartCaseResponse,
            tdAll.VatBta.journeyAfterStartedPegaCase
          )(this)
      }

      "Sa" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.SaBta.journeyAfterCanPayWithinSixMonths,
          tdAll.startCaseResponse
        )(
            journeyConnector.updatePegaStartCaseResponse,
            tdAll.SaBta.journeyAfterStartedPegaCase
          )(this)
      }

      "Sia" in new JourneyItTest {
        testUpdateWithoutExistingValue(
          tdAll.SiaPta.journeyAfterCanPayWithinSixMonths,
          tdAll.startCaseResponse
        )(
            journeyConnector.updatePegaStartCaseResponse,
            tdAll.SiaPta.journeyAfterStartedPegaCase
          )(this)
      }
    }

    "should update the journey when a isAccountHolder already existed" - {

      val differentResponse = StartCaseResponse(PegaCaseId("different-case-id"))

      "Epaye when the current stage is" - {

          def testEpayeBta[J <: Journey](initialJourney: J)(existingValue: J => StartCaseResponse)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentResponse,
                journeyConnector.updatePegaStartCaseResponse(_, _)(context.request),
                context.tdAll.EpayeBta.journeyAfterStartedPegaCase.copy(startCaseResponse = differentResponse)
              )(context)

        "StartedPegaCase" in new JourneyItTest {
          testEpayeBta(tdAll.EpayeBta.journeyAfterStartedPegaCase)(_.startCaseResponse)(this)
        }

      }

      "Vat when the current stage is" - {

          def testVatBta[J <: Journey](initialJourney: J)(existingValue: J => StartCaseResponse)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentResponse,
                journeyConnector.updatePegaStartCaseResponse(_, _)(context.request),
                context.tdAll.VatBta.journeyAfterStartedPegaCase.copy(startCaseResponse = differentResponse)
              )(context)

        "StartedPegaCase" in new JourneyItTest {
          testVatBta(tdAll.VatBta.journeyAfterStartedPegaCase)(_.startCaseResponse)(this)
        }

      }

      "Sa when the current stage is" - {

          def testSaBta[J <: Journey](initialJourney: J)(existingValue: J => StartCaseResponse)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentResponse,
                journeyConnector.updatePegaStartCaseResponse(_, _)(context.request),
                context.tdAll.SaBta.journeyAfterStartedPegaCase.copy(startCaseResponse = differentResponse)
              )(context)

        "StartedPegaCase" in new JourneyItTest {
          testSaBta(tdAll.SaBta.journeyAfterStartedPegaCase)(_.startCaseResponse)(this)
        }

      }

      "Sia when the current stage is" - {

          def testSiaPta[J <: Journey](initialJourney: J)(existingValue: J => StartCaseResponse)(context: JourneyItTest): Unit =
            testUpdateWithExistingValue(initialJourney)(
              _.journeyId,
              existingValue(initialJourney)
            )(
                differentResponse,
                journeyConnector.updatePegaStartCaseResponse(_, _)(context.request),
                context.tdAll.SiaPta.journeyAfterStartedPegaCase.copy(startCaseResponse = differentResponse)
              )(context)

        "StartedPegaCase" in new JourneyItTest {
          testSiaPta(tdAll.SiaPta.journeyAfterStartedPegaCase)(_.startCaseResponse)(this)
        }

      }

    }

  }
}

