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

package controllers

import essttp.journey.JourneyConnector
import essttp.rootmodel.{EmpRef, Nino, SaUtr, Vrn}
import testsupport.ItSpec
import testsupport.testdata.TdAll

class UpdateTaxIdControllerSpec extends ItSpec {

  def journeyConnector: JourneyConnector = app.injector.instanceOf[JourneyConnector]

  "POST /journey/:journeyId/update-tax-id" - {
    "should throw Bad Request when Journey is in a stage [AfterComputedTaxId]" in new JourneyItTest {
      stubCommonActions()

      insertJourneyForTest(
        TdAll.EpayeBta.journeyAfterEligibilityCheckEligible
          .copy(_id = tdAll.journeyId)
          .copy(correlationId = tdAll.correlationId)
      )
      val result: Throwable =
        journeyConnector.updateTaxId(tdAll.journeyId, tdAll.EpayeBta.updateTaxIdRequest()).failed.futureValue
      result.getMessage should include(
        """{"statusCode":400,"message":"UpdateTaxId is not possible in this stage, why is it happening? Debug me... [EligibilityChecked]"}"""
      )

      verifyCommonActions(numberOfAuthCalls = 1)
    }

    "Journey.Epaye" - {
      "should throw Bad Request when passed a VRN in stage [Started]" in new JourneyItTest {
        stubCommonActions()

        insertJourneyForTest(
          TdAll.EpayeBta.journeyAfterStarted.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId)
        )
        val result: Throwable =
          journeyConnector.updateTaxId(tdAll.journeyId, Vrn("thisshouldfailthetest")).failed.futureValue
        result.getMessage should include("""{"statusCode":400,"message":"Why is there a Vrn, this is for Epaye..."}""")

        verifyCommonActions(numberOfAuthCalls = 1)
      }
      "should throw Bad Request when passed a SaUtr in stage [Started]" in new JourneyItTest {
        stubCommonActions()

        insertJourneyForTest(
          TdAll.EpayeBta.journeyAfterStarted.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId)
        )
        val result: Throwable =
          journeyConnector.updateTaxId(tdAll.journeyId, SaUtr("thisshouldfailthetest")).failed.futureValue
        result.getMessage should include(
          """{"statusCode":400,"message":"Why is there a SaUtr, this is for Epaye..."}"""
        )

        verifyCommonActions(numberOfAuthCalls = 1)
      }
      "should throw Bad Request when passed a Nino in stage [Started]" in new JourneyItTest {
        stubCommonActions()

        insertJourneyForTest(
          TdAll.EpayeBta.journeyAfterStarted.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId)
        )
        val result: Throwable =
          journeyConnector.updateTaxId(tdAll.journeyId, Nino("thisshouldfailthetest")).failed.futureValue
        result.getMessage should include("""{"statusCode":400,"message":"Why is there a Nino, this is for Epaye..."}""")

        verifyCommonActions(numberOfAuthCalls = 1)
      }
      "update a tax ID when given an EmpRef" in new JourneyItTest {
        stubCommonActions()

        insertJourneyForTest(
          TdAll.EpayeBta.journeyAfterStarted.copy(
            _id = tdAll.journeyId,
            correlationId = tdAll.correlationId
          )
        )

        val result = journeyConnector.updateTaxId(tdAll.journeyId, tdAll.empRef).futureValue
        result shouldBe TdAll.EpayeBta.journeyAfterDetermineTaxIds.copy(
          _id = tdAll.journeyId,
          correlationId = tdAll.correlationId
        )

        verifyCommonActions(numberOfAuthCalls = 1)
      }

    }

    "Journey.Vat" - {
      "should throw Bad Request when passed an EmpRef in stage [Started]" in new JourneyItTest {
        stubCommonActions()

        insertJourneyForTest(
          TdAll.VatBta.journeyAfterStarted.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId)
        )
        val result: Throwable =
          journeyConnector.updateTaxId(tdAll.journeyId, EmpRef("thisshouldfailthetest")).failed.futureValue
        result.getMessage should include("""{"statusCode":400,"message":"Why is there a EmpRef, this is for Vat..."}""")

        verifyCommonActions(numberOfAuthCalls = 1)
      }
      "should throw Bad Request when passed an SaUtr in stage [Started]" in new JourneyItTest {
        stubCommonActions()

        insertJourneyForTest(
          TdAll.VatBta.journeyAfterStarted.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId)
        )
        val result: Throwable =
          journeyConnector.updateTaxId(tdAll.journeyId, SaUtr("thisshouldfailthetest")).failed.futureValue
        result.getMessage should include("""{"statusCode":400,"message":"Why is there a SaUtr, this is for Vat..."}""")

        verifyCommonActions(numberOfAuthCalls = 1)
      }
      "should throw Bad Request when passed an Nino in stage [Started]" in new JourneyItTest {
        stubCommonActions()

        insertJourneyForTest(
          TdAll.VatBta.journeyAfterStarted.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId)
        )
        val result: Throwable =
          journeyConnector.updateTaxId(tdAll.journeyId, Nino("thisshouldfailthetest")).failed.futureValue
        result.getMessage should include("""{"statusCode":400,"message":"Why is there a Nino, this is for Vat..."}""")

        verifyCommonActions(numberOfAuthCalls = 1)
      }
      "update a tax ID when given an Vrn" in new JourneyItTest {
        stubCommonActions()

        insertJourneyForTest(
          TdAll.VatBta.journeyAfterStarted.copy(
            _id = tdAll.journeyId,
            correlationId = tdAll.correlationId
          )
        )

        val result = journeyConnector.updateTaxId(tdAll.journeyId, tdAll.vrn).futureValue
        result shouldBe TdAll.VatBta.journeyAfterDetermineTaxIds.copy(
          _id = tdAll.journeyId,
          correlationId = tdAll.correlationId
        )

        verifyCommonActions(numberOfAuthCalls = 1)
      }
    }

    "Journey.Sa" - {
      "should throw Bad Request when passed an EmpRef in stage [Started]" in new JourneyItTest {
        stubCommonActions()

        insertJourneyForTest(
          TdAll.SaBta.journeyAfterStarted.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId)
        )
        val result: Throwable =
          journeyConnector.updateTaxId(tdAll.journeyId, EmpRef("thisshouldfailthetest")).failed.futureValue
        result.getMessage should include("""{"statusCode":400,"message":"Why is there a EmpRef, this is for Sa..."}""")

        verifyCommonActions(numberOfAuthCalls = 1)
      }
      "should throw Bad Request when passed an Vrn in stage [Started]" in new JourneyItTest {
        stubCommonActions()

        insertJourneyForTest(
          TdAll.SaBta.journeyAfterStarted.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId)
        )
        val result: Throwable =
          journeyConnector.updateTaxId(tdAll.journeyId, Vrn("thisshouldfailthetest")).failed.futureValue
        result.getMessage should include("""{"statusCode":400,"message":"Why is there a Vrn, this is for Sa..."}""")

        verifyCommonActions(numberOfAuthCalls = 1)
      }
      "should throw Bad Request when passed an Nino in stage [Started]" in new JourneyItTest {
        stubCommonActions()

        insertJourneyForTest(
          TdAll.SaBta.journeyAfterStarted.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId)
        )
        val result: Throwable =
          journeyConnector.updateTaxId(tdAll.journeyId, Nino("thisshouldfailthetest")).failed.futureValue
        result.getMessage should include("""{"statusCode":400,"message":"Why is there a Nino, this is for Sa..."}""")

        verifyCommonActions(numberOfAuthCalls = 1)
      }
      "update a tax ID when given an SaUtr" in new JourneyItTest {
        stubCommonActions()

        insertJourneyForTest(
          TdAll.SaBta.journeyAfterStarted.copy(
            _id = tdAll.journeyId,
            correlationId = tdAll.correlationId
          )
        )

        val result = journeyConnector.updateTaxId(tdAll.journeyId, tdAll.saUtr).futureValue
        result shouldBe TdAll.SaBta.journeyAfterDetermineTaxIds.copy(
          _id = tdAll.journeyId,
          correlationId = tdAll.correlationId
        )

        verifyCommonActions(numberOfAuthCalls = 1)
      }
    }

    "Journey.Simp" - {
      "should throw Bad Request when passed an EmpRef in stage [Started]" in new JourneyItTest {
        stubCommonActions()

        insertJourneyForTest(
          TdAll.SimpPta.journeyAfterStarted.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId)
        )
        val result: Throwable =
          journeyConnector.updateTaxId(tdAll.journeyId, EmpRef("thisshouldfailthetest")).failed.futureValue
        result.getMessage should include(
          """{"statusCode":400,"message":"Why is there a EmpRef, this is for Simp..."}"""
        )

        verifyCommonActions(numberOfAuthCalls = 1)
      }
      "should throw Bad Request when passed an Vrn in stage [Started]" in new JourneyItTest {
        stubCommonActions()

        insertJourneyForTest(
          TdAll.SimpPta.journeyAfterStarted.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId)
        )
        val result: Throwable =
          journeyConnector.updateTaxId(tdAll.journeyId, Vrn("thisshouldfailthetest")).failed.futureValue
        result.getMessage should include("""{"statusCode":400,"message":"Why is there a Vrn, this is for Simp..."}""")

        verifyCommonActions(numberOfAuthCalls = 1)
      }
      "should throw Bad Request when passed an SaUtr in stage [Started]" in new JourneyItTest {
        stubCommonActions()

        insertJourneyForTest(
          TdAll.SimpPta.journeyAfterStarted.copy(_id = tdAll.journeyId).copy(correlationId = tdAll.correlationId)
        )
        val result: Throwable =
          journeyConnector.updateTaxId(tdAll.journeyId, SaUtr("thisshouldfailthetest")).failed.futureValue
        result.getMessage should include("""{"statusCode":400,"message":"Why is there a SaUtr, this is for Simp..."}""")

        verifyCommonActions(numberOfAuthCalls = 1)
      }
      "update a tax ID when given an Nino" in new JourneyItTest {
        stubCommonActions()

        insertJourneyForTest(
          TdAll.SimpPta.journeyAfterStarted.copy(
            _id = tdAll.journeyId,
            correlationId = tdAll.correlationId
          )
        )

        val result = journeyConnector.updateTaxId(tdAll.journeyId, tdAll.nino).futureValue
        result shouldBe TdAll.SimpPta.journeyAfterDetermineTaxIds.copy(
          _id = tdAll.journeyId,
          correlationId = tdAll.correlationId
        )

        verifyCommonActions(numberOfAuthCalls = 1)
      }
    }

  }

}
